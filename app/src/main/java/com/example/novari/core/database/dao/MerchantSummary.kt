package com.example.novari.core.database.dao

/**
 * Projection over distinct merchants derived from [com.example.novari.core.database.entity.TransactionEntity].
 * There is no merchant table -- [merchant] is grouped by its normalized (uppercase, trimmed) form,
 * matching [com.example.novari.core.database.entity.MerchantCategoryRuleEntity.merchantPattern]'s convention.
 */
data class MerchantSummary(
    val merchant: String,
    val transactionCount: Int,
    val lastTransactionDate: Long
)
