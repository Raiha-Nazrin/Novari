package com.example.novari.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "merchant_category_rule",
    indices = [Index(value = ["merchantPattern"], unique = true)]
)
data class MerchantCategoryRuleEntity(
    @PrimaryKey
    val id: String,
    // Normalized (uppercased, trimmed) merchant keyword matched against a parsed merchant via
    // containment, e.g. "SWIGGY" matches "SWIGGY*BANGALORE".
    val merchantPattern: String,
    val categoryId: String,
    val source: MerchantRuleSource,
    val hitCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)

enum class MerchantRuleSource {
    SEED,
    LEARNED
}
