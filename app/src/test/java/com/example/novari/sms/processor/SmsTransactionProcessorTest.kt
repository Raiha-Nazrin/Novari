package com.example.novari.sms.processor

import com.example.novari.core.database.entity.SmsProcessingEntity
import com.example.novari.core.database.entity.SmsProcessingStatus
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.sms.classifier.FinancialSmsClassifier
import com.example.novari.sms.model.RawSmsMessage
import com.example.novari.sms.parser.FinancialSmsParser
import com.example.novari.sms.repository.SmsProcessingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeTransactionRepository : TransactionRepository {
    val created = mutableListOf<TransactionEntity>()
    private val bySourceReference = mutableMapOf<String, TransactionEntity>()

    override suspend fun create(transaction: TransactionEntity) {
        created += transaction
        bySourceReference[transaction.sourceReference ?: transaction.id] = transaction
    }

    override suspend fun update(transaction: TransactionEntity) {}
    override suspend fun delete(transaction: TransactionEntity) {}
    override suspend fun findById(id: String): TransactionEntity? = created.find { it.id == id }
    override suspend fun findBySourceReference(reference: String): TransactionEntity? =
        bySourceReference[reference]

    override fun observeActive(): Flow<List<TransactionEntity>> = MutableStateFlow(created)
    override fun searchActive(query: String): Flow<List<TransactionEntity>> = MutableStateFlow(created)
}

private class FakeSmsProcessingRepository : SmsProcessingRepository {
    val saved = mutableListOf<SmsProcessingEntity>()
    private val byFingerprint = mutableMapOf<String, SmsProcessingEntity>()

    override suspend fun findByFingerprint(fingerprint: String): SmsProcessingEntity? =
        byFingerprint[fingerprint]

    override suspend fun save(record: SmsProcessingEntity): Boolean {
        if (byFingerprint.containsKey(record.fingerprint)) return false
        byFingerprint[record.fingerprint] = record
        saved += record
        return true
    }
}

class SmsTransactionProcessorTest {

    private fun newProcessor(
        transactionRepository: FakeTransactionRepository = FakeTransactionRepository(),
        smsProcessingRepository: FakeSmsProcessingRepository = FakeSmsProcessingRepository()
    ) = SmsTransactionProcessor(
        classifier = FinancialSmsClassifier(),
        parser = FinancialSmsParser(),
        transactionRepository = transactionRepository,
        smsProcessingRepository = smsProcessingRepository
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
}
