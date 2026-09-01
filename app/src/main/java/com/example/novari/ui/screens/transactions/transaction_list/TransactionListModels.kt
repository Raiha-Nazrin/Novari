package com.example.novari.ui.screens.transactions.transaction_list

import com.example.novari.ui.model.CategoryUiModel
import com.example.novari.ui.model.Transaction
import java.time.LocalDate

/** Quick filter chips above the list — "All" shows the whole month unmodified. */
enum class QuickFilter {
    ALL,
    THIS_MONTH
}

/** Date filter picked from [com.example.novari.ui.components.CalendarBottomSheet]. */
sealed interface DateFilter {
    data object None : DateFilter
    data class Single(val date: LocalDate) : DateFilter
    data class Range(val start: LocalDate, val end: LocalDate) : DateFilter
}

/**
 * One calendar day's worth of transactions, as rendered under a "Today" /
 * "Yesterday" / "12 Aug" header with a running total for that day.
 */
data class TransactionDayGroup(
    val dateMillis: Long,
    val label: String,
    val total: Int,
    val transactions: List<Transaction>
)

/** Everything TransactionListScreen needs to render. */
data class TransactionListUiState(
    val rangeLabel: String = "",
    val transactionCount: Int = 0,
    val dayGroups: List<TransactionDayGroup> = emptyList(),
    val isLoading: Boolean = true,
    val query: String = "",
    val quickFilter: QuickFilter = QuickFilter.ALL,
    val categories: List<CategoryUiModel> = emptyList(),
    val selectedCategories: Set<String> = emptySet(),
    val addCategoryError: String? = null,
    val dateFilter: DateFilter = DateFilter.None,
    val dateFilterLabel: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && dayGroups.isEmpty()
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || selectedCategories.isNotEmpty() || dateFilter != DateFilter.None
}
