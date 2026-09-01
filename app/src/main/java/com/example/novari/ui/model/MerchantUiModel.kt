package com.example.novari.ui.model

import com.example.novari.core.database.dao.MerchantSummary

/**
 * A merchant row for the Search "Search by" merchant picker. [key] is the normalized
 * (uppercase, trimmed) form matched against in queries; [name] is the raw spelling to display.
 */
data class MerchantUiModel(
    val key: String,
    val name: String,
    val transactionCount: Int
)

fun MerchantSummary.toUiModel(): MerchantUiModel = MerchantUiModel(
    key = merchant.trim().uppercase(),
    name = merchant.trim(),
    transactionCount = transactionCount
)
