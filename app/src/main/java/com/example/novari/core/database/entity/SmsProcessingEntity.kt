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
    val createdAt: Long
)

enum class SmsProcessingStatus {
    PROCESSED,
    IGNORED,
    FAILED
}
