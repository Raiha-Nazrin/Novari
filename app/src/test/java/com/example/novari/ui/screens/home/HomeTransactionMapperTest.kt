package com.example.novari.ui.screens.home

import com.example.novari.R
import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.TransactionSource
import com.example.novari.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HomeTransactionMapperTest {

    private val today = LocalDate.of(2026, 8, 18)

    private fun epochMillisFor(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun transaction(
        amountMinor: Long = 42_000L,
        merchant: String? = "Lunch",
        notes: String? = null,
        categoryId: String? = null,
        date: LocalDate = today,
        type: TransactionType = TransactionType.EXPENSE
    ) = TransactionEntity(
        id = "txn_1",
        amountMinor = amountMinor,
        currencyCode = "INR",
        merchant = merchant,
        categoryId = categoryId,
        transactionType = type,
        transactionDate = epochMillisFor(date),
        notes = notes,
        source = TransactionSource.MANUAL,
        sourceReference = null,
        createdAt = 0L,
        updatedAt = 0L,
        deletedAt = null,
        revision = 1L
    )

    @Test
    fun `converts amountMinor from paise to rupees`() {
        val result = transaction(amountMinor = 42_000L).toHomeTransaction(emptyMap(), today)

        assertEquals(420, result.amount)
    }

    @Test
    fun `falls back to notes then a generic label when merchant is blank`() {
        val withNotes = transaction(merchant = null, notes = "Coffee").toHomeTransaction(emptyMap(), today)
        val withNeither = transaction(merchant = null, notes = null).toHomeTransaction(emptyMap(), today)

        assertEquals("Coffee", withNotes.title)
        assertEquals("Transaction", withNeither.title)
    }

    @Test
    fun `resolves category name and icon when the category id matches`() {
        val category = CategoryEntity(
            id = "cat_food",
            name = "Food",
            iconKey = "food",
            isSystem = true,
            isActive = true,
            createdAt = 0L,
            updatedAt = 0L
        )

        val result = transaction(categoryId = "cat_food")
            .toHomeTransaction(mapOf("cat_food" to category), today)

        assertEquals("Food", result.category)
        assertEquals(R.drawable.ic_food, result.iconRes)
    }

    @Test
    fun `falls back to Uncategorized when categoryId is null or unresolved`() {
        val noCategory = transaction(categoryId = null).toHomeTransaction(emptyMap(), today)
        val unknownCategory = transaction(categoryId = "missing").toHomeTransaction(emptyMap(), today)

        assertEquals("Uncategorized", noCategory.category)
        assertEquals(R.drawable.ic_category, noCategory.iconRes)
        assertEquals("Uncategorized", unknownCategory.category)
    }

    @Test
    fun `labels today, yesterday, and older dates correctly`() {
        val todayResult = transaction(date = today).toHomeTransaction(emptyMap(), today)
        val yesterdayResult = transaction(date = today.minusDays(1)).toHomeTransaction(emptyMap(), today)
        val olderResult = transaction(date = today.minusDays(10)).toHomeTransaction(emptyMap(), today)

        assertEquals("Today", todayResult.date)
        assertEquals("Yesterday", yesterdayResult.date)
        assertEquals("8 Aug", olderResult.date)
    }

    @Test
    fun `preserves transaction type`() {
        val income = transaction(type = TransactionType.INCOME).toHomeTransaction(emptyMap(), today)

        assertEquals(TransactionType.INCOME, income.type)
    }
}
