package com.example.novari.sms.repository

import com.example.novari.core.database.entity.SmsProcessingEntity
import com.example.novari.core.database.entity.SmsProcessingStatus

interface SmsProcessingRepository {
    suspend fun findByFingerprint(fingerprint: String): SmsProcessingEntity?

    /** Returns false when the fingerprint's unique index rejected the insert (already processed). */
    suspend fun save(record: SmsProcessingEntity): Boolean

    /** Clears processing records with the given status so a future sweep re-parses those messages. */
    suspend fun deleteByStatus(status: SmsProcessingStatus)
}
