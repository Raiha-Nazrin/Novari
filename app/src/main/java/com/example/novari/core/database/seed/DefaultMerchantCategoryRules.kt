package com.example.novari.core.database.seed

import com.example.novari.core.database.entity.MerchantCategoryRuleEntity
import com.example.novari.core.database.entity.MerchantRuleSource
import com.example.novari.core.util.TransactionId

data class DefaultMerchantCategoryRule(
    val merchantPattern: String,
    val categoryId: String
)

// Common Indian merchants/VPAs, one keyword per rule. Matched by containment against the
// parsed (uppercased) merchant string, so "SWIGGY" also catches "SWIGGY*BANGALORE" etc.
val DEFAULT_MERCHANT_CATEGORY_RULES = listOf(
    // Food
    DefaultMerchantCategoryRule("SWIGGY", "cat_food"),
    DefaultMerchantCategoryRule("ZOMATO", "cat_food"),
    DefaultMerchantCategoryRule("DOMINOS", "cat_food"),
    DefaultMerchantCategoryRule("DOMINO'S", "cat_food"),
    DefaultMerchantCategoryRule("PIZZAHUT", "cat_food"),
    DefaultMerchantCategoryRule("MCDONALD", "cat_food"),
    DefaultMerchantCategoryRule("STARBUCKS", "cat_food"),
    DefaultMerchantCategoryRule("BURGERKING", "cat_food"),
    DefaultMerchantCategoryRule("KFC", "cat_food"),
    DefaultMerchantCategoryRule("BLINKIT", "cat_food"),
    DefaultMerchantCategoryRule("ZEPTO", "cat_food"),
    DefaultMerchantCategoryRule("BIGBASKET", "cat_food"),
    DefaultMerchantCategoryRule("INSTAMART", "cat_food"),

    // Transport
    DefaultMerchantCategoryRule("UBER", "cat_transport"),
    DefaultMerchantCategoryRule("OLA", "cat_transport"),
    DefaultMerchantCategoryRule("RAPIDO", "cat_transport"),
    DefaultMerchantCategoryRule("IRCTC", "cat_transport"),
    DefaultMerchantCategoryRule("REDBUS", "cat_transport"),
    DefaultMerchantCategoryRule("METRO", "cat_transport"),
    DefaultMerchantCategoryRule("PETROL", "cat_transport"),
    DefaultMerchantCategoryRule("FASTAG", "cat_transport"),
    DefaultMerchantCategoryRule("INDIANOIL", "cat_transport"),
    DefaultMerchantCategoryRule("HPCL", "cat_transport"),
    DefaultMerchantCategoryRule("BPCL", "cat_transport"),

    // Shopping
    DefaultMerchantCategoryRule("AMAZON", "cat_shopping"),
    DefaultMerchantCategoryRule("FLIPKART", "cat_shopping"),
    DefaultMerchantCategoryRule("MYNTRA", "cat_shopping"),
    DefaultMerchantCategoryRule("AJIO", "cat_shopping"),
    DefaultMerchantCategoryRule("MEESHO", "cat_shopping"),
    DefaultMerchantCategoryRule("NYKAA", "cat_shopping"),
    DefaultMerchantCategoryRule("RELIANCE DIGITAL", "cat_shopping"),
    DefaultMerchantCategoryRule("DMART", "cat_shopping"),

    // Health
    DefaultMerchantCategoryRule("APOLLO PHARMACY", "cat_health"),
    DefaultMerchantCategoryRule("APOLLOPHARMACY", "cat_health"),
    DefaultMerchantCategoryRule("PHARMEASY", "cat_health"),
    DefaultMerchantCategoryRule("NETMEDS", "cat_health"),
    DefaultMerchantCategoryRule("1MG", "cat_health"),
    DefaultMerchantCategoryRule("PRACTO", "cat_health"),
    DefaultMerchantCategoryRule("HOSPITAL", "cat_health"),
    DefaultMerchantCategoryRule("DIAGNOSTIC", "cat_health"),

    // Entertainment
    DefaultMerchantCategoryRule("NETFLIX", "cat_entertainment"),
    DefaultMerchantCategoryRule("HOTSTAR", "cat_entertainment"),
    DefaultMerchantCategoryRule("PRIME VIDEO", "cat_entertainment"),
    DefaultMerchantCategoryRule("SPOTIFY", "cat_entertainment"),
    DefaultMerchantCategoryRule("BOOKMYSHOW", "cat_entertainment"),
    DefaultMerchantCategoryRule("PVR", "cat_entertainment"),
    DefaultMerchantCategoryRule("INOX", "cat_entertainment"),
    DefaultMerchantCategoryRule("JIOCINEMA", "cat_entertainment"),

    // Travel
    DefaultMerchantCategoryRule("MAKEMYTRIP", "cat_travel"),
    DefaultMerchantCategoryRule("GOIBIBO", "cat_travel"),
    DefaultMerchantCategoryRule("YATRA", "cat_travel"),
    DefaultMerchantCategoryRule("INDIGO", "cat_travel"),
    DefaultMerchantCategoryRule("AIRINDIA", "cat_travel"),
    DefaultMerchantCategoryRule("OYO", "cat_travel"),
    DefaultMerchantCategoryRule("AIRBNB", "cat_travel")
)

fun DefaultMerchantCategoryRule.toEntity(now: Long): MerchantCategoryRuleEntity = MerchantCategoryRuleEntity(
    id = TransactionId.new(),
    merchantPattern = merchantPattern,
    categoryId = categoryId,
    source = MerchantRuleSource.SEED,
    hitCount = 0,
    createdAt = now,
    updatedAt = now
)
