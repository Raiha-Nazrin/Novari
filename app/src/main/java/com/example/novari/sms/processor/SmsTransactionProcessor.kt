package com.example.novari.sms.processor

import com.example.novari.core.database.entity.SmsProcessingEntity
import com.example.novari.core.database.entity.SmsProcessingStatus
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.TransactionSource
import com.example.novari.core.util.TransactionId
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.sms.classifier.FinancialSmsClassifier
import com.example.novari.sms.model.RawSmsMessage
import com.example.novari.sms.parser.FinancialSmsParser
import com.example.novari.sms.repository.SmsProcessingRepository
import com.example.novari.sms.security.SmsFingerprint

class SmsTransactionProcessor(
    private val classifier: FinancialSmsClassifier,
    private val parser: FinancialSmsParser,
    private val transactionRepository: TransactionRepository,
    private val smsProcessingRepository: SmsProcessingRepository
) {
    suspend fun process(message: RawSmsMessage) {
        val fingerprint = SmsFingerprint.create(message.sender, message.body, message.timestamp)

        // Cheap optimization: skip re-parsing a message the catch-up sweep has already seen.
        // Not the correctness guard -- the insert-ignore below is, since goAsync and the sweep
        // can race on the same message.
        if (smsProcessingRepository.findByFingerprint(fingerprint) != null) return

        if (!classifier.isFinancial(message.sender, message.body)) {
            saveProcessing(message, fingerprint, SmsProcessingStatus.IGNORED, null)
            return
        }

        val parsed = parser.parse(message)
        if (parsed == null) {
            saveProcessing(message, fingerprint, SmsProcessingStatus.FAILED, null)
            return
        }

        val sourceReference = parsed.referenceNumber ?: "sms:$fingerprint"
        if (transactionRepository.findBySourceReference(sourceReference) != null) return

        val now = System.currentTimeMillis()
        val transactionId = TransactionId.new()

        // Claim the fingerprint via the unique-index insert-ignore first: this is the atomic
        // gate against the real-time and catch-up paths racing on the same message. Only on a
        // successful claim do we create the transaction.
        val claimed = saveProcessing(message, fingerprint, SmsProcessingStatus.PROCESSED, transactionId)
        if (!claimed) return

        transactionRepository.create(
            TransactionEntity(
                id = transactionId,
                amountMinor = parsed.amountMinor,
                currencyCode = parsed.currencyCode,
                merchant = parsed.merchant,
                categoryId = null,
                transactionType = parsed.transactionType,
                transactionDate = parsed.transactionDate,
                notes = null,
                source = TransactionSource.SMS,
                sourceReference = sourceReference,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
                revision = 1L
            )
        )
    }

    private suspend fun saveProcessing(
        message: RawSmsMessage,
        fingerprint: String,
        status: SmsProcessingStatus,
        transactionId: String?
    ): Boolean {
        val now = System.currentTimeMillis()
        return smsProcessingRepository.save(
            SmsProcessingEntity(
                id = TransactionId.new(),
                fingerprint = fingerprint,
                smsTimestamp = message.timestamp,
                sender = message.sender,
                status = status,
                transactionId = transactionId,
                processedAt = now,
                createdAt = now
            )
        )
    }
}
