package com.example.novari.data.repository

import com.example.novari.core.database.dao.MerchantSummary
import com.example.novari.core.database.dao.TransactionDao
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.TransactionSource
import com.example.novari.domain.repository.MerchantCategoryRuleRepository
import com.example.novari.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomTransactionRepository @Inject constructor(
    private val dao: TransactionDao,
    private val merchantCategoryRuleRepository: MerchantCategoryRuleRepository
) : TransactionRepository {

    override suspend fun create(transaction: TransactionEntity) {
        dao.insert(transaction)
    }

    override suspend fun update(transaction: TransactionEntity) {
        // The only feedback loop that improves auto-categorization without a release: a user
        // correcting an SMS-derived category generalizes to a LEARNED merchant rule, which
        // outranks the SEED rules on future SMS parses.
        val merchant = transaction.merchant
        val newCategoryId = transaction.categoryId
        if (transaction.source == TransactionSource.SMS && merchant != null && newCategoryId != null) {
            val previous = dao.findById(transaction.id)
            if (previous != null && previous.categoryId != newCategoryId) {
                merchantCategoryRuleRepository.learnFromCorrection(merchant, newCategoryId)
            }
        }
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

    override suspend fun releaseForReparse(transaction: TransactionEntity) {
        val now = System.currentTimeMillis()
        dao.update(
            transaction.copy(
                deletedAt = now,
                updatedAt = now,
                revision = transaction.revision + 1,
                sourceReference = null
            )
        )
    }

    override suspend fun findById(id: String): TransactionEntity? =
        dao.findById(id)

    override fun observeById(id: String): Flow<TransactionEntity?> =
        dao.observeById(id)

    override suspend fun findBySourceReference(reference: String): TransactionEntity? =
        dao.findBySourceReference(reference)

    override fun observeActive(): Flow<List<TransactionEntity>> =
        dao.observeActive()

    override fun observeRecent(limit: Int): Flow<List<TransactionEntity>> =
        dao.observeRecent(limit)

    override fun observeBetween(startInclusive: Long, endInclusive: Long): Flow<List<TransactionEntity>> =
        dao.observeBetween(startInclusive, endInclusive)

    override fun searchActive(query: String): Flow<List<TransactionEntity>> =
        dao.searchActive(query)

    override fun observeSearch(
        merchantQuery: String?,
        merchantKeys: Set<String>,
        categoryIds: Set<String>,
        minAmountMinor: Long?,
        maxAmountMinor: Long?,
        startInclusive: Long?,
        endInclusive: Long?
    ): Flow<List<TransactionEntity>> = dao.observeSearch(
        merchantQuery = merchantQuery?.trim()?.takeIf { it.isNotEmpty() },
        hasMerchantFilter = merchantKeys.isNotEmpty(),
        merchantKeys = merchantKeys.toList(),
        hasCategoryFilter = categoryIds.isNotEmpty(),
        categoryIds = categoryIds.toList(),
        minAmountMinor = minAmountMinor,
        maxAmountMinor = maxAmountMinor,
        startInclusive = startInclusive,
        endInclusive = endInclusive
    )

    override fun observeMerchants(): Flow<List<MerchantSummary>> =
        dao.observeMerchants()
}
