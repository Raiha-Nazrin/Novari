package com.example.novari.ui.screens.insights

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.TransactionType
import com.example.novari.ui.theme.NovariColors
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

private const val UNCATEGORIZED_NAME = "Uncategorized"
private val monthLabelFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
private val systemZone: ZoneId = ZoneId.systemDefault()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(systemZone).toLocalDate()

private fun LocalDate.startOfDayMillis(): Long =
    atStartOfDay(systemZone).toInstant().toEpochMilli()

private fun LocalDate.endOfDayMillis(): Long =
    plusDays(1).atStartOfDay(systemZone).toInstant().toEpochMilli() - 1

fun monthLabel(today: LocalDate): String = today.format(monthLabelFormatter)

/** Start of the current calendar month through the end of [today], inclusive. */
fun monthToDateRange(today: LocalDate): Pair<Long, Long> {
    val start = YearMonth.from(today).atDay(1)
    return start.startOfDayMillis() to today.endOfDayMillis()
}

/**
 * Same day-of-month span one calendar month back, so a monthly comparison is
 * fair while the current month is still in progress (e.g. "1st-21st" vs
 * "1st-21st last month" rather than the full previous month).
 */
fun previousMonthToDateRange(today: LocalDate): Pair<Long, Long> {
    val previousMonth = YearMonth.from(today).minusMonths(1)
    val start = previousMonth.atDay(1)
    val end = previousMonth.atDay(minOf(today.dayOfMonth, previousMonth.lengthOfMonth()))
    return start.startOfDayMillis() to end.endOfDayMillis()
}

/** Monday of the current week through the end of [today], inclusive. */
fun weekToDateRange(today: LocalDate): Pair<Long, Long> {
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    return monday.startOfDayMillis() to today.endOfDayMillis()
}

/** Same Mon-onward span one week back, for a like-for-like weekly comparison. */
fun previousWeekToDateRange(today: LocalDate): Pair<Long, Long> {
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val previousMonday = monday.minusWeeks(1)
    val previousEnd = previousMonday.plusDays((today.dayOfWeek.value - 1).toLong())
    return previousMonday.startOfDayMillis() to previousEnd.endOfDayMillis()
}

private fun List<TransactionEntity>.expensesOnly(): List<TransactionEntity> =
    filter { it.transactionType == TransactionType.EXPENSE }

fun sumExpenseMinor(transactions: List<TransactionEntity>): Long =
    transactions.expensesOnly().sumOf { it.amountMinor }

/** Signed percentage change of [current] relative to [previous], both in minor units. */
fun percentChange(current: Long, previous: Long): Int = when {
    previous == 0L && current == 0L -> 0
    previous == 0L -> 100
    else -> (((current - previous) * 100) / previous).toInt()
}

private fun CategoryEntity?.presentation(): Triple<ImageVector, Color, Color> = when (this?.iconKey) {
    "food" -> Triple(Icons.Default.Coffee, NovariColors.Food, NovariColors.FoodBackground)
    "shopping" -> Triple(Icons.Default.ShoppingBag, NovariColors.Shopping, NovariColors.ShoppingBackground)
    "transport" -> Triple(Icons.Default.DirectionsCar, NovariColors.Transport, NovariColors.TransportBackground)
    else -> Triple(Icons.Default.MoreHoriz, NovariColors.Others, NovariColors.OthersBackground)
}

/** Category breakdown for [transactions], sorted by spend, amounts in major currency units. */
fun buildCategoryBreakdown(
    transactions: List<TransactionEntity>,
    categoriesById: Map<String, CategoryEntity>
): List<InsightCategory> {
    val expenses = transactions.expensesOnly()
    val total = expenses.sumOf { it.amountMinor }
    if (total <= 0L) return emptyList()

    return expenses.groupBy { it.categoryId }
        .map { (categoryId, txns) ->
            val amountMinor = txns.sumOf { it.amountMinor }
            val category = categoryId?.let { categoriesById[it] }
            val (icon, color, backgroundColor) = category.presentation()
            InsightCategory(
                id = categoryId ?: UNCATEGORIZED_NAME,
                name = category?.name ?: UNCATEGORIZED_NAME,
                amount = amountMinor / 100L,
                percentage = ((amountMinor * 100) / total).toInt(),
                icon = icon,
                color = color,
                backgroundColor = backgroundColor
            )
        }
        .sortedByDescending { it.amount }
}

/** Normalized (0f..1f) spend per day of week, Monday-first, for [transactions] in the current week. */
fun buildWeeklyValues(transactions: List<TransactionEntity>, weekStart: LocalDate): List<Float> {
    val dailyTotals = LongArray(7)
    transactions.expensesOnly().forEach { tx ->
        val index = ChronoUnit.DAYS.between(weekStart, tx.transactionDate.toLocalDate()).toInt()
        if (index in 0..6) dailyTotals[index] += tx.amountMinor
    }
    val max = dailyTotals.maxOrNull()?.takeIf { it > 0L } ?: 1L
    return dailyTotals.map { (it.toFloat() / max.toFloat()).coerceIn(0f, 1f) }
}

/**
 * Up to the 3 categories with the biggest swing between [current] and
 * [previous] (both month-to-date), for the "What changed" card.
 */
fun buildWhatChanged(
    current: List<TransactionEntity>,
    previous: List<TransactionEntity>,
    categoriesById: Map<String, CategoryEntity>
): List<InsightCategoryChange> {
    val currentTotals = current.expensesOnly().groupBy { it.categoryId }.mapValues { it.value.sumOf { tx -> tx.amountMinor } }
    val previousTotals = previous.expensesOnly().groupBy { it.categoryId }.mapValues { it.value.sumOf { tx -> tx.amountMinor } }

    return (currentTotals.keys + previousTotals.keys)
        .mapNotNull { categoryId ->
            val curr = currentTotals[categoryId] ?: 0L
            val prev = previousTotals[categoryId] ?: 0L
            if (curr == 0L && prev == 0L) return@mapNotNull null

            val category = categoryId?.let { categoriesById[it] }
            val (icon, color, backgroundColor) = category.presentation()
            InsightCategoryChange(
                name = category?.name ?: UNCATEGORIZED_NAME,
                changePercent = percentChange(curr, prev),
                icon = icon,
                color = color,
                backgroundColor = backgroundColor
            )
        }
        .sortedByDescending { abs(it.changePercent) }
        .take(3)
}

/**
 * Cumulative month-to-date spend resampled to at most 8 points, normalized to
 * 0f..1f for [SpendingChart][com.example.novari.ui.screens.insights.charts.SpendingChart].
 * Empty when there's less than 2 days of data (the chart needs 2+ points).
 */
fun buildSpendingTrendPoints(transactions: List<TransactionEntity>, monthStart: LocalDate, today: LocalDate): List<Float> {
    val dayCount = ChronoUnit.DAYS.between(monthStart, today).toInt() + 1
    if (dayCount < 2) return emptyList()

    val dailyTotals = LongArray(dayCount)
    transactions.expensesOnly().forEach { tx ->
        val index = ChronoUnit.DAYS.between(monthStart, tx.transactionDate.toLocalDate()).toInt()
        if (index in 0 until dayCount) dailyTotals[index] += tx.amountMinor
    }

    val cumulative = LongArray(dayCount)
    var running = 0L
    for (i in 0 until dayCount) {
        running += dailyTotals[i]
        cumulative[i] = running
    }

    val pointCount = minOf(8, dayCount)
    val sampled = (0 until pointCount).map { i ->
        val index = if (pointCount == 1) dayCount - 1 else i * (dayCount - 1) / (pointCount - 1)
        cumulative[index]
    }

    val max = sampled.maxOrNull()?.takeIf { it > 0L } ?: 1L
    return sampled.map { (it.toFloat() / max.toFloat()).coerceIn(0f, 1f) }
}
