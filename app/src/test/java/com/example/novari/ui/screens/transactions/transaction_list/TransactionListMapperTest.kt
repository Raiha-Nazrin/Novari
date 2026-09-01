package com.example.novari.ui.screens.transactions.transaction_list

import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.TransactionSource
import com.example.novari.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class TransactionListMapperTest {

    private val zoneId = ZoneId.systemDefault()
    private val today = LocalDate.of(2026, 8, 18)

    private fun epochMillisFor(date: LocalDate): Long =
        date.atStartOfDay(zoneId).toInstant().toEpochMilli()

    private fun transaction(
        id: String,
        amountMinor: Long = 42_000L,
        merchant: String? = "Lunch",
        notes: String? = null,
        categoryId: String? = null,
        date: LocalDate = today,
        type: TransactionType = TransactionType.EXPENSE
    ) = TransactionEntity(
        id = id,
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
    fun `groups transactions by calendar day, newest day first`() {
        val entities = listOf(
            transaction(id = "1", date = today),
            transaction(id = "2", date = today.minusDays(1)),
            transaction(id = "3", date = today)
        )

        val groups = buildDayGroups(entities, emptyMap(), today = today)

        assertEquals(2, groups.size)
        assertEquals("Today", groups[0].label)
        assertEquals(2, groups[0].transactions.size)
        assertEquals("Yesterday", groups[1].label)
        assertEquals(1, groups[1].transactions.size)
    }

    @Test
    fun `uses month-day label for dates before yesterday`() {
        val entities = listOf(transaction(id = "1", date = LocalDate.of(2026, 8, 7)))

        val groups = buildDayGroups(entities, emptyMap(), today = today)

        assertEquals("August 7", groups.single().label)
    }

    @Test
    fun `day total nets expenses against income`() {
        val entities = listOf(
            transaction(id = "1", amountMinor = 100_00L, type = TransactionType.EXPENSE, date = today),
            transaction(id = "2", amountMinor = 30_00L, type = TransactionType.INCOME, date = today)
        )

        val groups = buildDayGroups(entities, emptyMap(), today = today)

        assertEquals(70, groups.single().total)
    }

    @Test
    fun `query filters by merchant or notes, case-insensitively`() {
        val entities = listOf(
            transaction(id = "1", merchant = "Swiggy", date = today),
            transaction(id = "2", merchant = null, notes = "Coffee", date = today),
            transaction(id = "3", merchant = "Amazon", date = today)
        )

        val groups = buildDayGroups(entities, emptyMap(), query = "swig", today = today)

        assertEquals(1, groups.single().transactions.size)
        assertEquals("Swiggy", groups.single().transactions.single().title)
    }

    @Test
    fun `category filter narrows to matching categoryIds when non-empty`() {
        val entities = listOf(
            transaction(id = "1", categoryId = "cat_food", date = today),
            transaction(id = "2", categoryId = "cat_transport", date = today)
        )

        val groups = buildDayGroups(entities, emptyMap(), categoryIds = setOf("cat_food"), today = today)

        assertEquals(1, groups.single().transactions.size)
    }

    @Test
    fun `empty category filter matches everything`() {
        val entities = listOf(
            transaction(id = "1", categoryId = "cat_food", date = today),
            transaction(id = "2", categoryId = null, date = today)
        )

        val groups = buildDayGroups(entities, emptyMap(), categoryIds = emptySet(), today = today)

        assertEquals(2, groups.single().transactions.size)
    }

    @Test
    fun `month label formats as full month name and year`() {
        assertEquals("August 2026", YearMonth.of(2026, 8).toMonthLabel())
    }

    @Test
    fun `month range covers the first millisecond to the last millisecond of the month`() {
        val range = YearMonth.of(2026, 8).toEpochMillisRange(zoneId)

        val start = LocalDate.of(2026, 8, 1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfMonth = LocalDate.of(2026, 8, 31).atStartOfDay(zoneId).toInstant().toEpochMilli()

        assertEquals(start, range.first)
        assertTrue(range.last >= endOfMonth)
        assertTrue(range.last < LocalDate.of(2026, 9, 1).atStartOfDay(zoneId).toInstant().toEpochMilli())
    }
}
