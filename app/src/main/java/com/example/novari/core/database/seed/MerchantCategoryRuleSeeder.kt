package com.example.novari.core.database.seed

import com.example.novari.core.database.dao.MerchantCategoryRuleDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantCategoryRuleSeeder @Inject constructor(
    private val dao: MerchantCategoryRuleDao
) {
    suspend fun seedIfNeeded() {
        val now = System.currentTimeMillis()
        dao.insertAll(DEFAULT_MERCHANT_CATEGORY_RULES.map { it.toEntity(now) })
    }
}
