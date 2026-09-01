package com.example.novari.ui.screens.home

import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.database.entity.iconFor
import com.example.novari.core.model.TransactionSource
import com.example.novari.ui.model.Transaction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val UNCATEGORIZED = "Uncategorized"

/**
 * Maps a persisted [TransactionEntity] to the [Transaction] shape HomeScreen
 * renders. Kept out of the ViewModel so the date/icon/currency rules are
 * easy to unit-test on their own.
 *
 * [categoriesById] resolves [TransactionEntity.categoryId] to a display name
 * and icon. No transaction is ever written with a category today (see
 * AddExpenseViewModel), so a missing/unknown id falls back to "Uncategorized"
 * with a generic icon rather than crashing or leaving a blank row.
 */
fun TransactionEntity.toHomeTransaction(
    categoriesById: Map<String, CategoryEntity>,
    today: LocalDate = LocalDate.now()
): Transaction {
    val category = categoryId?.let { categoriesById[it] }
    return Transaction(
        id = id,
        title = merchant?.takeIf { it.isNotBlank() } ?: notes?.takeIf { it.isNotBlank() } ?: "Transaction",
        category = category?.name ?: UNCATEGORIZED,
        date = transactionDate.toRelativeDateLabel(today),
        amount = (amountMinor / 100L).toInt(),
        iconRes = category.iconFor(),
        type = transactionType,
        isAutoDetected = source == TransactionSource.SMS
    )
}

private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM")

/**
 * "Today" / "Yesterday" for the last two days, otherwise a short date
 * (e.g. "12 Aug") — matches the labels the mock data used to show.
 */
private fun Long.toRelativeDateLabel(today: LocalDate): String {
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(shortDateFormatter)
    }
}
