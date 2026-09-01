package com.example.novari.sms.processor

import com.example.novari.core.database.dao.MerchantSummary
import com.example.novari.core.database.entity.SmsProcessingEntity
import com.example.novari.core.database.entity.SmsProcessingStatus
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.domain.repository.MerchantCategoryRuleRepository
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.sms.classifier.FinancialSmsClassifier
import com.example.novari.sms.model.RawSmsMessage
import com.example.novari.sms.parser.FinancialSmsParser
import com.example.novari.sms.repository.SmsProcessingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

private class FakeTransactionRepository : TransactionRepository {
    val created = mutableListOf<TransactionEntity>()
    private val bySourceReference = mutableMapOf<String, TransactionEntity>()

    override suspend fun create(transaction: TransactionEntity) {
        created += transaction
        bySourceReference[transaction.sourceReference ?: transaction.id] = transaction
    }

    override suspend fun update(transaction: TransactionEntity) {}
    override suspend fun delete(transaction: TransactionEntity) {}
    override suspend fun releaseForReparse(transaction: TransactionEntity) {}
    override suspend fun findById(id: String): TransactionEntity? = created.find { it.id == id }
    override fun observeById(id: String): Flow<TransactionEntity?> = MutableStateFlow(created.find { it.id == id })
    override suspend fun findBySourceReference(reference: String): TransactionEntity? =
        bySourceReference[reference]

    override fun observeActive(): Flow<List<TransactionEntity>> = MutableStateFlow(created)
    override fun observeRecent(limit: Int): Flow<List<TransactionEntity>> = MutableStateFlow(created.take(limit))
    override fun observeBetween(startInclusive: Long, endInclusive: Long): Flow<List<TransactionEntity>> =
        MutableStateFlow(created.filter { it.transactionDate in startInclusive..endInclusive })
    override fun searchActive(query: String): Flow<List<TransactionEntity>> = MutableStateFlow(created)
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

private class FakeSmsProcessingRepository : SmsProcessingRepository {
    val saved = mutableListOf<SmsProcessingEntity>()
    private val byFingerprint = mutableMapOf<String, SmsProcessingEntity>()

    override suspend fun findByFingerprint(fingerprints: List<String>): SmsProcessingEntity? =
        fingerprints.firstNotNullOfOrNull { byFingerprint[it] }

    override suspend fun save(record: SmsProcessingEntity): Boolean {
        if (byFingerprint.containsKey(record.fingerprint)) return false
        byFingerprint[record.fingerprint] = record
        saved += record
        return true
    }

    override suspend fun deleteByStatus(status: SmsProcessingStatus) {
        saved.removeAll { it.status == status }
        byFingerprint.values.removeAll { it.status == status }
    }

    override suspend fun deleteById(id: String) {
        saved.removeAll { it.id == id }
        byFingerprint.values.removeAll { it.id == id }
    }

    override suspend fun findStaleProcessed(currentRevision: Int): List<SmsProcessingEntity> =
        saved.filter { it.status == SmsProcessingStatus.PROCESSED && it.derivedRevision < currentRevision }

    override fun observeProcessedCount(): Flow<Int> =
        flow { emit(saved.count { it.status == SmsProcessingStatus.PROCESSED }) }

    override fun observeSilentlyIgnoredCount(): Flow<Int> =
        flow { emit(saved.count { it.status == SmsProcessingStatus.IGNORED }) }
}

private class FakeMerchantCategoryRuleRepository(
    private val rules: Map<String, String> = emptyMap()
) : MerchantCategoryRuleRepository {
    val learned = mutableListOf<Pair<String, String>>()

    override suspend fun categoryForMerchant(merchant: String): String? =
        rules.entries.firstOrNull { merchant.uppercase().contains(it.key) }?.value

    override suspend fun learnFromCorrection(merchant: String, categoryId: String) {
        learned += merchant to categoryId
    }
}

class SmsTransactionProcessorTest {

    private fun newProcessor(
        transactionRepository: FakeTransactionRepository = FakeTransactionRepository(),
        smsProcessingRepository: FakeSmsProcessingRepository = FakeSmsProcessingRepository(),
        merchantCategoryRuleRepository: FakeMerchantCategoryRuleRepository = FakeMerchantCategoryRuleRepository()
    ) = SmsTransactionProcessor(
        classifier = FinancialSmsClassifier(),
        parser = FinancialSmsParser(),
        transactionRepository = transactionRepository,
        smsProcessingRepository = smsProcessingRepository,
        merchantCategoryRuleRepository = merchantCategoryRuleRepository
    )

    @Test
    fun `financial message creates a transaction and a PROCESSED record`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val smsProcessingRepository = FakeSmsProcessingRepository()
        val processor = newProcessor(transactionRepository, smsProcessingRepository)

        processor.process(
            RawSmsMessage(
                sender = "HDFCBK",
                body = "Rs 450.00 debited from A/c XX1234 to SWIGGY. UPI Ref No 123456789012.",
                timestamp = System.currentTimeMillis()
            )
        )

        assertEquals(1, transactionRepository.created.size)
        assertEquals(1, smsProcessingRepository.saved.size)
        assertEquals(SmsProcessingStatus.PROCESSED, smsProcessingRepository.saved.single().status)
        assertEquals(SmsReparseGate.PARSER_VERSION, smsProcessingRepository.saved.single().derivedRevision)
    }

    @Test
    fun `non-financial message is IGNORED and creates no transaction`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val smsProcessingRepository = FakeSmsProcessingRepository()
        val processor = newProcessor(transactionRepository, smsProcessingRepository)

        processor.process(
            RawSmsMessage(
                sender = "HDFCBK",
                body = "Your account balance is Rs 45,000 as of today",
                timestamp = System.currentTimeMillis()
            )
        )

        assertEquals(0, transactionRepository.created.size)
        assertEquals(SmsProcessingStatus.IGNORED, smsProcessingRepository.saved.single().status)
    }

    @Test
    fun `financial message that fails to parse is FAILED and creates no transaction`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val smsProcessingRepository = FakeSmsProcessingRepository()
        val processor = newProcessor(transactionRepository, smsProcessingRepository)

        processor.process(
            RawSmsMessage(
                sender = "HDFCBK",
                // Contains a transaction verb (classifier accepts) but no parseable amount.
                body = "Your payment was received. Thank you.",
                timestamp = System.currentTimeMillis()
            )
        )

        assertEquals(0, transactionRepository.created.size)
        assertEquals(SmsProcessingStatus.FAILED, smsProcessingRepository.saved.single().status)
    }

    @Test
    fun `duplicate fingerprint is not reprocessed`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val smsProcessingRepository = FakeSmsProcessingRepository()
        val processor = newProcessor(transactionRepository, smsProcessingRepository)
        val message = RawSmsMessage(
            sender = "HDFCBK",
            body = "Rs 450.00 debited from A/c XX1234 to SWIGGY. UPI Ref No 123456789012.",
            timestamp = System.currentTimeMillis()
        )

        processor.process(message)
        processor.process(message)

        assertEquals(1, transactionRepository.created.size)
        assertEquals(1, smsProcessingRepository.saved.size)
    }

    @Test
    fun `sbi upi debit alert with no currency token is processed, not FAILED`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val smsProcessingRepository = FakeSmsProcessingRepository()
        val processor = newProcessor(transactionRepository, smsProcessingRepository)

        processor.process(
            RawSmsMessage(
                sender = "JK-SBIUPI-S",
                body = "Dear UPI user A/C X2851 debited by 30.00 on date 03Aug26 trf to SHABEER V K " +
                    "Refno 621555777395 If not u? call-1800111109 for other services-18001234-SBI",
                timestamp = System.currentTimeMillis()
            )
        )

        assertEquals(1, transactionRepository.created.size)
        assertEquals(3000L, transactionRepository.created.single().amountMinor)
        assertEquals(SmsProcessingStatus.PROCESSED, smsProcessingRepository.saved.single().status)
    }

    @Test
    fun `retail promo blast is IGNORED, not booked as a transaction`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val smsProcessingRepository = FakeSmsProcessingRepository()
        val processor = newProcessor(transactionRepository, smsProcessingRepository)

        processor.process(
            RawSmsMessage(
                sender = "VM-EASYBY-P",
                body = "LAST 5 DAYS! Flat Price Sale@EasyBuy-Kozhikode Everything under Rs199 on Sale " +
                    "Merchandise Ends 30 June Tees@99 Shirts@199 Kurtas@199 Kidswear@99 T&C",
                timestamp = System.currentTimeMillis()
            )
        )

        assertEquals(0, transactionRepository.created.size)
        assertEquals(SmsProcessingStatus.IGNORED, smsProcessingRepository.saved.single().status)
    }

    @Test
    fun `same amount and merchant on the same day without a reference number dedupes via fingerprint`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val smsProcessingRepository = FakeSmsProcessingRepository()
        val processor = newProcessor(transactionRepository, smsProcessingRepository)
        val body = "Rs 450.00 debited from A/c XX1234 to SWIGGY"
        val timestamp = System.currentTimeMillis()

        processor.process(RawSmsMessage(sender = "HDFCBK", body = body, timestamp = timestamp))
        processor.process(RawSmsMessage(sender = "HDFCBK", body = body, timestamp = timestamp + 60_000))

        assertEquals(1, transactionRepository.created.size)
        assertTrue(transactionRepository.created.single().sourceReference.orEmpty().startsWith("sms:"))
    }

    @Test
    fun `same message just across a local midnight boundary dedupes across the day bucket`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val smsProcessingRepository = FakeSmsProcessingRepository()
        val processor = newProcessor(transactionRepository, smsProcessingRepository)
        val body = "Rs 450.00 debited from A/c XX1234 to SWIGGY"
        val beforeMidnight = ZonedDateTime.of(2026, 8, 18, 23, 59, 0, 0, ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val afterMidnight = ZonedDateTime.of(2026, 8, 19, 0, 1, 0, 0, ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        // Real-time path fingerprints the message just before midnight; catch-up re-derives the
        // same message a moment later, now bucketed into the next day.
        processor.process(RawSmsMessage(sender = "HDFCBK", body = body, timestamp = beforeMidnight))
        processor.process(RawSmsMessage(sender = "HDFCBK", body = body, timestamp = afterMidnight))

        assertEquals(1, transactionRepository.created.size)
    }

    @Test
    fun `weak reference capture falls back to the fingerprint dedup key`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val smsProcessingRepository = FakeSmsProcessingRepository()
        val processor = newProcessor(transactionRepository, smsProcessingRepository)

        processor.process(
            RawSmsMessage(
                sender = "HDFCBK",
                body = "Rs 450.00 debited from A/c XX1234 to SWIGGY. Ref No: AB12",
                timestamp = System.currentTimeMillis()
            )
        )

        val sourceReference = transactionRepository.created.single().sourceReference.orEmpty()
        assertTrue("weak reference capture must not be trusted as the dedup key: $sourceReference", sourceReference.startsWith("sms:"))
    }

    @Test
    fun `matching merchant rule sets the category instead of leaving it null`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val smsProcessingRepository = FakeSmsProcessingRepository()
        val merchantCategoryRuleRepository = FakeMerchantCategoryRuleRepository(mapOf("SWIGGY" to "cat_food"))
        val processor = newProcessor(transactionRepository, smsProcessingRepository, merchantCategoryRuleRepository)

        processor.process(
            RawSmsMessage(
                sender = "HDFCBK",
                body = "Rs 450.00 debited from A/c XX1234 to SWIGGY. UPI Ref No 123456789012.",
                timestamp = System.currentTimeMillis()
            )
        )

        assertEquals("cat_food", transactionRepository.created.single().categoryId)
    }

    @Test
    fun `no matching merchant rule leaves the category null`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val smsProcessingRepository = FakeSmsProcessingRepository()
        val processor = newProcessor(transactionRepository, smsProcessingRepository)

        processor.process(
            RawSmsMessage(
                sender = "HDFCBK",
                body = "Rs 450.00 debited from A/c XX1234 to SWIGGY. UPI Ref No 123456789012.",
                timestamp = System.currentTimeMillis()
            )
        )

        assertEquals(null, transactionRepository.created.single().categoryId)
    }
}
