package com.example.novari.sms.processor

import com.example.novari.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Shared processing entry point for historical and real-time SMS.
 *
 * Planned pipeline:
 * classify -> parse -> validate -> fingerprint -> deduplicate -> create transaction
 */
class SmsTransactionProcessor @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend fun process(message: RawSmsMessage) {
        // TODO: Implement after manual transaction flow is stable.
        // Raw SMS remains in memory only and is never persisted.
    }
}
