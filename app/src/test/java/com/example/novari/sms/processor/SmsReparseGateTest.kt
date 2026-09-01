package com.example.novari.sms.processor

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.novari.core.database.dao.MerchantSummary
import com.example.novari.core.database.entity.SmsProcessingEntity
import com.example.novari.core.database.entity.SmsProcessingStatus
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.TransactionSource
import com.example.novari.core.model.TransactionType
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.sms.repository.SmsProcessingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

private val APPLIED_PARSER_VERSION_KEY = intPreferencesKey("sms_reparse_applied_parser_version")
private val APPLIED_CLASSIFIER_VERSION_KEY = intPreferencesKey("sms_reparse_applied_classifier_version")
private val LAST_PROCESSED_KEY = longPreferencesKey("sms_last_processed_timestamp")

private class FakeReparseSmsProcessingRepository : SmsProcessingRepository {
    val saved = mutableListOf<SmsProcessingEntity>()
    var deletedStatuses = mutableListOf<SmsProcessingStatus>()
    var stale = emptyList<SmsProcessingEntity>()
    val deletedIds = mutableListOf<String>()

    override suspend fun findByFingerprint(fingerprints: List<String>): SmsProcessingEntity? = null

    override suspend fun save(record: SmsProcessingEntity): Boolean {
        saved += record
        return true
    }

    override suspend fun deleteByStatus(status: SmsProcessingStatus) {
        deletedStatuses += status
        saved.removeAll { it.status == status }
    }

    override suspend fun deleteById(id: String) {
        deletedIds += id
        saved.removeAll { it.id == id }
    }

    override suspend fun findStaleProcessed(currentRevision: Int): List<SmsProcessingEntity> = stale

    override fun observeProcessedCount(): Flow<Int> = flowOf(0)

    override fun observeSilentlyIgnoredCount(): Flow<Int> = flowOf(0)
}

private class FakeReparseTransactionRepository : TransactionRepository {
    val transactions = mutableMapOf<String, TransactionEntity>()
    val released = mutableListOf<String>()

    override suspend fun create(transaction: TransactionEntity) {
        transactions[transaction.id] = transaction
    }

    override suspend fun update(transaction: TransactionEntity) {
        transactions[transaction.id] = transaction
    }

    override suspend fun delete(transaction: TransactionEntity) {
        transactions[transaction.id] = transaction.copy(deletedAt = System.currentTimeMillis())
    }

    override suspend fun releaseForReparse(transaction: TransactionEntity) {
        released += transaction.id
        transactions[transaction.id] = transaction.copy(
            deletedAt = System.currentTimeMillis(),
            sourceReference = null,
            revision = transaction.revision + 1
        )
    }

    override suspend fun findById(id: String): TransactionEntity? = transactions[id]
    override fun observeById(id: String): Flow<TransactionEntity?> = MutableStateFlow(transactions[id])
    override suspend fun findBySourceReference(reference: String): TransactionEntity? =
        transactions.values.find { it.sourceReference == reference }

    override fun observeActive(): Flow<List<TransactionEntity>> = MutableStateFlow(transactions.values.toList())
    override fun observeRecent(limit: Int): Flow<List<TransactionEntity>> = MutableStateFlow(transactions.values.take(limit))
    override fun observeBetween(startInclusive: Long, endInclusive: Long): Flow<List<TransactionEntity>> =
        MutableStateFlow(transactions.values.toList())
    override fun searchActive(query: String): Flow<List<TransactionEntity>> = MutableStateFlow(transactions.values.toList())
    override fun observeSearch(
        merchantQuery: String?,
        merchantKeys: Set<String>,
        categoryIds: Set<String>,
        minAmountMinor: Long?,
        maxAmountMinor: Long?,
        startInclusive: Long?,
        endInclusive: Long?
    ): Flow<List<TransactionEntity>> = MutableStateFlow(emptyList())
    override fun observeMerchants(): Flow<List<MerchantSummary>> = MutableStateFlow(emptyList())
}

private fun testTransaction(id: String, revision: Long = 1L, deletedAt: Long? = null) = TransactionEntity(
    id = id,
    amountMinor = 45000,
    currencyCode = "INR",
    merchant = "SWIGGY",
    categoryId = null,
    transactionType = TransactionType.EXPENSE,
    transactionDate = System.currentTimeMillis(),
    notes = null,
    source = TransactionSource.SMS,
    sourceReference = "sms:abc123",
    createdAt = System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis(),
    deletedAt = deletedAt,
    revision = revision
)

class SmsReparseGateTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun newDataStore(file: File): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(produceFile = { file })

    @Test
    fun `first run clears IGNORED and FAILED records and rewinds the sync watermark`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse1.preferences_pb"))
        val now = System.currentTimeMillis()
        dataStore.edit { it[LAST_PROCESSED_KEY] = now }

        SmsReparseGate(repository, FakeReparseTransactionRepository(), dataStore).reconcileIfNeeded()

        assertEquals(
            setOf(SmsProcessingStatus.IGNORED, SmsProcessingStatus.FAILED),
            repository.deletedStatuses.toSet()
        )
        val rewound = dataStore.data.first()[LAST_PROCESSED_KEY]
        assertTrue(rewound != null && rewound < now)
    }

    @Test
    fun `does not rewind past an already-earlier watermark`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse2.preferences_pb"))
        val earlier = System.currentTimeMillis() - java.util.concurrent.TimeUnit.DAYS.toMillis(200)
        dataStore.edit { it[LAST_PROCESSED_KEY] = earlier }

        SmsReparseGate(repository, FakeReparseTransactionRepository(), dataStore).reconcileIfNeeded()

        assertEquals(earlier, dataStore.data.first()[LAST_PROCESSED_KEY])
    }

    @Test
    fun `runs only once`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse3.preferences_pb"))
        val gate = SmsReparseGate(repository, FakeReparseTransactionRepository(), dataStore)

        gate.reconcileIfNeeded()
        val countAfterFirstRun = repository.deletedStatuses.size
        gate.reconcileIfNeeded()

        assertEquals(countAfterFirstRun, repository.deletedStatuses.size)
    }

    @Test
    fun `no prior watermark still runs the cleanup and stamps the applied versions`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse4.preferences_pb"))

        SmsReparseGate(repository, FakeReparseTransactionRepository(), dataStore).reconcileIfNeeded()

        assertEquals(
            setOf(SmsProcessingStatus.IGNORED, SmsProcessingStatus.FAILED),
            repository.deletedStatuses.toSet()
        )
        assertTrue(dataStore.data.first()[APPLIED_PARSER_VERSION_KEY]!! > 0)
        assertTrue(dataStore.data.first()[APPLIED_CLASSIFIER_VERSION_KEY]!! > 0)
        assertNull(dataStore.data.first()[LAST_PROCESSED_KEY])
    }

    @Test
    fun `second run with no version bump does nothing`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse5.preferences_pb"))
        val gate = SmsReparseGate(repository, FakeReparseTransactionRepository(), dataStore)

        gate.reconcileIfNeeded()
        repository.deletedStatuses.clear()
        gate.reconcileIfNeeded()

        assertTrue(repository.deletedStatuses.isEmpty())
    }

    @Test
    fun `stale PROCESSED row for an untouched transaction is released for reparse`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val transactionRepository = FakeReparseTransactionRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse6.preferences_pb"))
        val transaction = testTransaction(id = "txn1")
        transactionRepository.transactions[transaction.id] = transaction
        repository.stale = listOf(
            SmsProcessingEntity(
                id = "row1",
                fingerprint = "fp1",
                smsTimestamp = System.currentTimeMillis(),
                sender = "HDFCBK",
                status = SmsProcessingStatus.PROCESSED,
                transactionId = transaction.id,
                processedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                derivedRevision = 1
            )
        )

        SmsReparseGate(repository, transactionRepository, dataStore).reconcileIfNeeded()

        assertEquals(listOf(transaction.id), transactionRepository.released)
        assertNotNull(transactionRepository.transactions.getValue(transaction.id).deletedAt)
        assertNull(transactionRepository.transactions.getValue(transaction.id).sourceReference)
        assertEquals(listOf("row1"), repository.deletedIds)
    }

    @Test
    fun `stale PROCESSED row is left alone when the transaction was user-edited`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val transactionRepository = FakeReparseTransactionRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse7.preferences_pb"))
        val transaction = testTransaction(id = "txn2", revision = 2L)
        transactionRepository.transactions[transaction.id] = transaction
        repository.stale = listOf(
            SmsProcessingEntity(
                id = "row2",
                fingerprint = "fp2",
                smsTimestamp = System.currentTimeMillis(),
                sender = "HDFCBK",
                status = SmsProcessingStatus.PROCESSED,
                transactionId = transaction.id,
                processedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                derivedRevision = 1
            )
        )

        SmsReparseGate(repository, transactionRepository, dataStore).reconcileIfNeeded()

        assertTrue(transactionRepository.released.isEmpty())
        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test
    fun `stale PROCESSED row is left alone when the transaction was already deleted`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val transactionRepository = FakeReparseTransactionRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse8.preferences_pb"))
        val transaction = testTransaction(id = "txn3", deletedAt = System.currentTimeMillis())
        transactionRepository.transactions[transaction.id] = transaction
        repository.stale = listOf(
            SmsProcessingEntity(
                id = "row3",
                fingerprint = "fp3",
                smsTimestamp = System.currentTimeMillis(),
                sender = "HDFCBK",
                status = SmsProcessingStatus.PROCESSED,
                transactionId = transaction.id,
                processedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                derivedRevision = 1
            )
        )

        SmsReparseGate(repository, transactionRepository, dataStore).reconcileIfNeeded()

        assertTrue(transactionRepository.released.isEmpty())
        assertTrue(repository.deletedIds.isEmpty())
    }
}
