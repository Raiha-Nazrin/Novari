package com.example.novari.domain.repository

import com.example.novari.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun create(transaction: TransactionEntity)
    suspend fun update(transaction: TransactionEntity)
    suspend fun delete(transaction: TransactionEntity)
    suspend fun findById(id: String): TransactionEntity?
    suspend fun findBySourceReference(reference: String): TransactionEntity?
    fun observeActive(): Flow<List<TransactionEntity>>
    fun searchActive(query: String): Flow<List<TransactionEntity>>
}
