package com.example.novari.sms.processor

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.novari.core.database.entity.SmsProcessingEntity
import com.example.novari.core.database.entity.SmsProcessingStatus
import com.example.novari.sms.repository.SmsProcessingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

private val APPLIED_VERSION_KEY = intPreferencesKey("sms_reparse_applied_version")
private val LAST_PROCESSED_KEY = longPreferencesKey("sms_last_processed_timestamp")

private class FakeReparseSmsProcessingRepository : SmsProcessingRepository {
    val saved = mutableListOf<SmsProcessingEntity>()
    var deletedStatuses = mutableListOf<SmsProcessingStatus>()

    override suspend fun findByFingerprint(fingerprint: String): SmsProcessingEntity? = null

    override suspend fun save(record: SmsProcessingEntity): Boolean {
        saved += record
        return true
    }

    override suspend fun deleteByStatus(status: SmsProcessingStatus) {
        deletedStatuses += status
        saved.removeAll { it.status == status }
    }
}

class SmsReparseGateTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun newDataStore(file: File): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(produceFile = { file })

    @Test
    fun `first run clears FAILED records and rewinds the sync watermark`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse1.preferences_pb"))
        val now = System.currentTimeMillis()
        dataStore.edit { it[LAST_PROCESSED_KEY] = now }

        SmsReparseGate(repository, dataStore).reconcileIfNeeded()

        assertEquals(listOf(SmsProcessingStatus.FAILED), repository.deletedStatuses)
        val rewound = dataStore.data.first()[LAST_PROCESSED_KEY]
        assertTrue(rewound != null && rewound < now)
    }

    @Test
    fun `does not rewind past an already-earlier watermark`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse2.preferences_pb"))
        val earlier = System.currentTimeMillis() - java.util.concurrent.TimeUnit.DAYS.toMillis(200)
        dataStore.edit { it[LAST_PROCESSED_KEY] = earlier }

        SmsReparseGate(repository, dataStore).reconcileIfNeeded()

        assertEquals(earlier, dataStore.data.first()[LAST_PROCESSED_KEY])
    }

    @Test
    fun `runs only once`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse3.preferences_pb"))
        val gate = SmsReparseGate(repository, dataStore)

        gate.reconcileIfNeeded()
        gate.reconcileIfNeeded()

        assertEquals(1, repository.deletedStatuses.size)
    }

    @Test
    fun `no prior watermark still runs the cleanup and stamps the applied version`() = runTest {
        val repository = FakeReparseSmsProcessingRepository()
        val dataStore = newDataStore(tempFolder.newFile("reparse4.preferences_pb"))

        SmsReparseGate(repository, dataStore).reconcileIfNeeded()

        assertEquals(listOf(SmsProcessingStatus.FAILED), repository.deletedStatuses)
        assertTrue(dataStore.data.first()[APPLIED_VERSION_KEY]!! > 0)
        assertNull(dataStore.data.first()[LAST_PROCESSED_KEY])
    }
}
