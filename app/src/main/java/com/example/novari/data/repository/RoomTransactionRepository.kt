package com.example.novari.data.repository

import com.example.novari.core.database.dao.TransactionDao
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomTransactionRepository @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override suspend fun create(transaction: TransactionEntity) {
        dao.insert(transaction)
    }

    override suspend fun update(transaction: TransactionEntity) {
        dao.update(transaction)
    }

    override suspend fun delete(transaction: TransactionEntity) {
        val now = System.currentTimeMillis()
        dao.update(
            transaction.copy(
                deletedAt = now,
                updatedAt = now,
                revision = transaction.revision + 1
            )
        )
    }

    override suspend fun findById(id: String): TransactionEntity? =
        dao.findById(id)

    override suspend fun findBySourceReference(reference: String): TransactionEntity? =
        dao.findBySourceReference(reference)

    override fun observeActive(): Flow<List<TransactionEntity>> =
        dao.observeActive()

    override fun searchActive(query: String): Flow<List<TransactionEntity>> =
        dao.searchActive(query)
}
