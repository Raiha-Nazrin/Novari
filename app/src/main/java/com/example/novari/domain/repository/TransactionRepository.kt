package com.example.novari.domain.repository

import com.example.novari.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun create(transaction: TransactionEntity)
    suspend fun update(transaction: TransactionEntity)
    suspend fun delete(transaction: TransactionEntity)

    /**
     * Soft-deletes [transaction] and releases its [TransactionEntity.sourceReference] (set to
     * null) so a future sweep can insert a freshly re-derived row for the same message instead
     * of colliding with the unique index on the old one. Only for rows a parser-version bump
     * has confirmed the user never touched -- see SmsReparseGate.
     */
    suspend fun releaseForReparse(transaction: TransactionEntity)
    suspend fun findById(id: String): TransactionEntity?
    fun observeById(id: String): Flow<TransactionEntity?>
    suspend fun findBySourceReference(reference: String): TransactionEntity?
    fun observeActive(): Flow<List<TransactionEntity>>
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>
    fun observeBetween(startInclusive: Long, endInclusive: Long): Flow<List<TransactionEntity>>
    fun searchActive(query: String): Flow<List<TransactionEntity>>

    /** See [com.example.novari.core.database.dao.TransactionDao.observeSearch]. */
    fun observeSearch(
        merchantQuery: String?,
        categoryIds: Set<String>,
        minAmountMinor: Long?,
        maxAmountMinor: Long?,
        startInclusive: Long?,
        endInclusive: Long?
    ): Flow<List<TransactionEntity>>
}
