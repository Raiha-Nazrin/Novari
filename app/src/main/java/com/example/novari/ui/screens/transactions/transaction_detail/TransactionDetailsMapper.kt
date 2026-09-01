package com.example.novari.ui.screens.transactions.transaction_detail

import androidx.annotation.DrawableRes
import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.database.entity.iconFor
import com.example.novari.core.model.TransactionSource
import com.example.novari.core.model.TransactionType
import com.example.novari.ui.components.formatTransactionAmount
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val UNCATEGORIZED = "Uncategorized"

private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM")
private val fullDateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")

/** Everything TransactionDetailsScreen needs to render one transaction. */
data class TransactionDetailsUiModel(
    val id: String,
    val merchant: String,
    val category: String,
    @DrawableRes val categoryIconRes: Int,
    val amount: String,
    val dateLabel: String,
    val dateTime: String,
    val note: String?,
    val isIncome: Boolean,
    val isAutoDetected: Boolean
)

/**
 * Maps a persisted [TransactionEntity] to the shape TransactionDetailsScreen
 * renders. Mirrors [com.example.novari.ui.screens.home.toHomeTransaction] so
 * currency/category/date rules stay consistent across Home, the list, and
 * this screen.
 */
fun TransactionEntity.toDetailsUiModel(
    categoriesById: Map<String, CategoryEntity>,
    today: LocalDate = LocalDate.now(),
    zoneId: ZoneId = ZoneId.systemDefault()
): TransactionDetailsUiModel {
    val category = categoryId?.let { categoriesById[it] }
    val date = Instant.ofEpochMilli(transactionDate).atZone(zoneId).toLocalDate()

    return TransactionDetailsUiModel(
        id = id,
        merchant = merchant?.takeIf { it.isNotBlank() } ?: notes?.takeIf { it.isNotBlank() } ?: "Transaction",
        category = category?.name ?: UNCATEGORIZED,
        categoryIconRes = category.iconFor(),
        amount = formatTransactionAmount(amountMinor / 100L),
        dateLabel = date.toDayHeaderLabel(today),
        dateTime = Instant.ofEpochMilli(transactionDate).atZone(zoneId).format(fullDateTimeFormatter),
        note = notes,
        isIncome = transactionType == TransactionType.INCOME,
        isAutoDetected = source == TransactionSource.SMS
    )
}

private fun LocalDate.toDayHeaderLabel(today: LocalDate): String = when (this) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> format(shortDateFormatter)
}
