package com.example.novari.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.novari.core.model.TransactionSource
import com.example.novari.core.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["transactionDate"]),
        Index(value = ["categoryId"]),
        Index(value = ["source"]),
        Index(value = ["sourceReference"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    // Store money in minor units: INR 450.75 -> 45075.
    val amountMinor: Long,
    val currencyCode: String,
    val merchant: String?,
    val categoryId: String?,
    val transactionType: TransactionType,
    val transactionDate: Long,
    val notes: String?,
    val source: TransactionSource,
    // Never store the complete SMS body here.
    val sourceReference: String?,
    val createdAt: Long,
    val updatedAt: Long,
    // Kept for future sync/delete propagation.
    val deletedAt: Long?,
    // Increment whenever the record changes.
    val revision: Long
)
