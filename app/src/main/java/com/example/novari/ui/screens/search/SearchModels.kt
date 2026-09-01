package com.example.novari.ui.screens.search

import com.example.novari.core.model.SearchField
import com.example.novari.ui.model.CategoryUiModel
import com.example.novari.ui.model.MerchantUiModel
import com.example.novari.ui.model.Transaction
import com.example.novari.ui.screens.transactions.transaction_list.DateFilter
import com.example.novari.ui.screens.transactions.transaction_list.chipLabel

/** Inclusive min/max in minor units (paise). Either bound may be left open. */
data class AmountRange(
    val minMinor: Long? = null,
    val maxMinor: Long? = null
) {
    val isEmpty: Boolean get() = minMinor == null && maxMinor == null
}

/** Everything SearchScreen needs to render, mirroring TransactionListUiState's shape. */
data class SearchUiState(
    val query: String = "",
    val searchField: SearchField = SearchField.MERCHANT,
    val categories: List<CategoryUiModel> = emptyList(),
    val selectedCategoryIds: Set<String> = emptySet(),
    val addCategoryError: String? = null,
    val merchants: List<MerchantUiModel> = emptyList(),
    val selectedMerchantKeys: Set<String> = emptySet(),
    val amountRange: AmountRange = AmountRange(),
    val dateFilter: DateFilter = DateFilter.None,
    val recentSearches: List<String> = emptyList(),
    val results: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null
) {
    val dateFilterLabel: String?
        get() = dateFilter.chipLabel()

    val hasActiveFilters: Boolean
        get() = query.isNotBlank() ||
            selectedCategoryIds.isNotEmpty() ||
            selectedMerchantKeys.isNotEmpty() ||
            !amountRange.isEmpty ||
            dateFilter != DateFilter.None

    val isEmptyResult: Boolean
        get() = hasSearched && !isLoading && errorMessage == null && results.isEmpty()

    val showRecentSearches: Boolean
        get() = !hasSearched && recentSearches.isNotEmpty()
}

/**
 * Parses free-typed amount search text into a rupee range, in minor units.
 * "500" is an exact match (min == max); "100-500" (whitespace tolerant) is a
 * range. Anything else that isn't parseable as rupees yields an empty range,
 * which SearchViewModel treats as "no amount filter yet".
 */
fun parseAmountRangeQuery(text: String): AmountRange {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return AmountRange()

    val rangeParts = trimmed.split("-").map { it.trim() }
    if (rangeParts.size == 2) {
        val min = rangeParts[0].toDoubleOrNull()
        val max = rangeParts[1].toDoubleOrNull()
        if (min != null && max != null) {
            return AmountRange(minMinor = min.toMinorUnits(), maxMinor = max.toMinorUnits())
        }
    }

    val exact = trimmed.toDoubleOrNull() ?: return AmountRange()
    val minorUnits = exact.toMinorUnits()
    return AmountRange(minMinor = minorUnits, maxMinor = minorUnits)
}

private fun Double.toMinorUnits(): Long = Math.round(this * 100)
