package com.example.novari.data.repository

import com.example.novari.core.database.dao.MerchantCategoryRuleDao
import com.example.novari.core.database.entity.MerchantCategoryRuleEntity
import com.example.novari.core.database.entity.MerchantRuleSource
import com.example.novari.core.util.TransactionId
import com.example.novari.domain.repository.MerchantCategoryRuleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomMerchantCategoryRuleRepository @Inject constructor(
    private val dao: MerchantCategoryRuleDao
) : MerchantCategoryRuleRepository {

    override suspend fun categoryForMerchant(merchant: String): String? {
        val normalized = normalize(merchant)
        if (normalized.isEmpty()) return null
        return dao.findMatch(normalized)?.categoryId
    }

    override suspend fun learnFromCorrection(merchant: String, categoryId: String) {
        val normalized = normalize(merchant)
        if (normalized.isEmpty()) return

        val now = System.currentTimeMillis()
        val existing = dao.findByPattern(normalized)
        if (existing != null) {
            dao.update(
                existing.copy(
                    categoryId = categoryId,
                    source = MerchantRuleSource.LEARNED,
                    hitCount = existing.hitCount + 1,
                    updatedAt = now
                )
            )
        } else {
            dao.insert(
                MerchantCategoryRuleEntity(
                    id = TransactionId.new(),
                    merchantPattern = normalized,
                    categoryId = categoryId,
                    source = MerchantRuleSource.LEARNED,
                    hitCount = 1,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    private fun normalize(merchant: String): String = merchant.uppercase().trim()
}
