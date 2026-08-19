package com.example.novari.ui.screens.transactions.transaction_list

import com.example.novari.ui.model.Transaction

/** Quick filter chips above the list — "All" shows the whole month unmodified. */
enum class QuickFilter {
    ALL,
    THIS_MONTH
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

/**
 * Everything TransactionListScreen needs to render.
 * [categoryFilterCount] and date-range filtering are surfaced for the filter
 * chips but always empty for now — category/date pickers are not built yet.
 */
data class TransactionListUiState(
    val monthLabel: String = "",
    val transactionCount: Int = 0,
    val dayGroups: List<TransactionDayGroup> = emptyList(),
    val isLoading: Boolean = true,
    val query: String = "",
    val quickFilter: QuickFilter = QuickFilter.ALL,
    val selectedCategories: Set<String> = emptySet()
) {
    val isEmpty: Boolean get() = !isLoading && dayGroups.isEmpty()
}
