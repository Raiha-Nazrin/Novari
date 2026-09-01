package com.example.novari.domain.repository

interface MerchantCategoryRuleRepository {
    /** Best category match for a parsed merchant string, or null if no rule matches. */
    suspend fun categoryForMerchant(merchant: String): String?

    /**
     * Upserts a LEARNED rule for this merchant, outranking any SEED rule on future lookups.
     * Call whenever a user re-categorizes a transaction so the correction generalizes.
     */
    suspend fun learnFromCorrection(merchant: String, categoryId: String)
}
