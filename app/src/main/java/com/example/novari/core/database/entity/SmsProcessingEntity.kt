package com.example.novari.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_processing",
    indices = [Index(value = ["fingerprint"], unique = true)]
)
data class SmsProcessingEntity(
    @PrimaryKey
    val id: String,
    // Fingerprint/reference only. Never persist the complete SMS body.
    val fingerprint: String,
    val smsTimestamp: Long,
    val sender: String?,
    val status: SmsProcessingStatus,
    val transactionId: String?,
    val processedAt: Long?,
    val createdAt: Long,
    // Stamped with SmsReparseGate.PARSER_VERSION when a PROCESSED row is saved. Lets a parser
    // bump find rows that were parsed by an older, less accurate version and are worth
    // re-deriving -- see SmsReparseGate.reparseStaleProcessedRows.
    val derivedRevision: Int = 0
)

enum class SmsProcessingStatus {
    PROCESSED,
    IGNORED,
    FAILED
}
