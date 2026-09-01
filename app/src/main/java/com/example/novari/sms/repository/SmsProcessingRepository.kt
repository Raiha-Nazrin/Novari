package com.example.novari.sms.repository

import com.example.novari.core.database.entity.SmsProcessingEntity
import com.example.novari.core.database.entity.SmsProcessingStatus
import kotlinx.coroutines.flow.Flow

interface SmsProcessingRepository {
    suspend fun findByFingerprint(fingerprints: List<String>): SmsProcessingEntity?

    /** Returns false when the fingerprint's unique index rejected the insert (already processed). */
    suspend fun save(record: SmsProcessingEntity): Boolean

    /** Clears processing records with the given status so a future sweep reparses those messages. */
    suspend fun deleteByStatus(status: SmsProcessingStatus)

    /** Clears a single processing record so a future sweep re-derives that one message. */
    suspend fun deleteById(id: String)

    /** PROCESSED rows stamped by a parser older than [currentRevision] -- candidates to re-derive. */
    suspend fun findStaleProcessed(currentRevision: Int): List<SmsProcessingEntity>

    /** Live count of successfully booked SMS transactions -- detection health screen. */
    fun observeProcessedCount(): Flow<Int>

    /** Live count of messages dismissed as non-financial noise. */
    fun observeSilentlyIgnoredCount(): Flow<Int>
}
