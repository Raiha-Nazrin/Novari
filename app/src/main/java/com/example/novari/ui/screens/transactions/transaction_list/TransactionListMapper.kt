package com.example.novari.ui.screens.transactions.transaction_list

import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.TransactionType
import com.example.novari.ui.screens.home.toHomeTransaction
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val monthLabelFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
private val dayHeaderFormatter = DateTimeFormatter.ofPattern("MMMM d")
private val chipDayMonthFormatter = DateTimeFormatter.ofPattern("d MMM")

/** "August 2026" for the given month. */
fun YearMonth.toMonthLabel(): String = atDay(1).format(monthLabelFormatter)

/** Epoch-millis range covering every millisecond of [this] month, in the local zone. */
fun YearMonth.toEpochMillisRange(zoneId: ZoneId = ZoneId.systemDefault()): LongRange {
    val start = atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val end = atEndOfMonth().atTime(23, 59, 59, 999_999_999).atZone(zoneId).toInstant().toEpochMilli()
    return start..end
}

/** Epoch-millis range covering every millisecond of the selected day(s), in the local zone. */
fun DateFilter.toEpochMillisRange(zoneId: ZoneId = ZoneId.systemDefault()): LongRange? =
    when (this) {
        DateFilter.None -> null
        is DateFilter.Single -> date.toDayRange(zoneId)
        is DateFilter.Range -> start.atStartOfDay(zoneId).toInstant().toEpochMilli()..
            end.atTime(23, 59, 59, 999_999_999).atZone(zoneId).toInstant().toEpochMilli()
    }

private fun LocalDate.toDayRange(zoneId: ZoneId): LongRange {
    val start = atStartOfDay(zoneId).toInstant().toEpochMilli()
    val end = atTime(23, 59, 59, 999_999_999).atZone(zoneId).toInstant().toEpochMilli()
    return start..end
}

/** Short label for the Date filter chip, e.g. "6 Aug" or "1 Aug – 15 Aug". */
fun DateFilter.chipLabel(): String? = when (this) {
    DateFilter.None -> null
    is DateFilter.Single -> date.format(chipDayMonthFormatter)
    is DateFilter.Range -> "${start.format(chipDayMonthFormatter)} – ${end.format(chipDayMonthFormatter)}"
}

/** "August 2026" for [DateFilter.None], otherwise the picked day/range spelled out. */
fun DateFilter.toRangeLabel(fallback: YearMonth): String = when (this) {
    DateFilter.None -> fallback.toMonthLabel()
    is DateFilter.Single -> date.format(dayHeaderFormatter)
    is DateFilter.Range -> "${start.format(chipDayMonthFormatter)} – ${end.format(chipDayMonthFormatter)}"
}

/**
 * Groups active [entities] by calendar day (newest first), applying [query]
 * against merchant/notes and [categoryIds] against categoryId — both no-ops
 * when empty, since category/date pickers aren't wired up yet.
 *
 * Each day's total nets expenses against income so a day with a refund
 * still reads as a single, sensible number.
 */
fun buildDayGroups(
    entities: List<TransactionEntity>,
    categoriesById: Map<String, CategoryEntity>,
    query: String = "",
    categoryIds: Set<String> = emptySet(),
    today: LocalDate = LocalDate.now(),
    zoneId: ZoneId = ZoneId.systemDefault()
): List<TransactionDayGroup> {
    val trimmedQuery = query.trim()

    val filtered = entities.filter { entity ->
        val matchesQuery = trimmedQuery.isBlank() ||
            entity.merchant?.contains(trimmedQuery, ignoreCase = true) == true ||
            entity.notes?.contains(trimmedQuery, ignoreCase = true) == true
        val matchesCategory = categoryIds.isEmpty() || entity.categoryId in categoryIds
        matchesQuery && matchesCategory
    }

    return filtered
        .groupBy { it.transactionDate.toLocalDate(zoneId) }
        .toSortedMap(compareByDescending { it })
        .map { (date, dayEntities) ->
            val netMinor = dayEntities.sumOf { entity ->
                if (entity.transactionType == TransactionType.INCOME) -entity.amountMinor else entity.amountMinor
            }
            TransactionDayGroup(
                dateMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                label = date.toDayHeaderLabel(today),
                total = (kotlin.math.abs(netMinor) / 100L).toInt(),
                transactions = dayEntities
                    .sortedByDescending { it.transactionDate }
                    .map { it.toHomeTransaction(categoriesById, today) }
            )
        }
}

private fun Long.toLocalDate(zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

private fun LocalDate.toDayHeaderLabel(today: LocalDate): String = when (this) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> format(dayHeaderFormatter)
}
