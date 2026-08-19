package com.example.novari.ui.screens.transactions.transaction_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.core.database.dao.CategoryDao
import com.example.novari.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private data class Filters(
    val query: String = "",
    val quickFilter: QuickFilter = QuickFilter.ALL,
    val selectedCategories: Set<String> = emptySet()
)

/**
 * Backs TransactionListScreen with the current calendar month's transactions
 * from the database. [Filters.quickFilter] only ever narrows within that same
 * month today ("All" vs "This month" are equivalent until other months are
 * browsable), and category/date filtering wait on their pickers — both are
 * threaded through the pipeline already so wiring a real picker later is a
 * one-line change in [setCategoryFilter] / a new setter, not a rewrite.
 */
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryDao: CategoryDao
) : ViewModel() {

    private val currentMonth = YearMonth.now()
    private val filters = MutableStateFlow(Filters())

    private val monthTransactions = currentMonth.toEpochMillisRange().let { range ->
        transactionRepository.observeBetween(range.first, range.last)
    }

    val uiState: StateFlow<TransactionListUiState> = combine(
        monthTransactions,
        categoryDao.observeActive(),
        filters
    ) { transactions, categories, currentFilters ->
        val categoriesById = categories.associateBy { it.id }
        val dayGroups = buildDayGroups(
            entities = transactions,
            categoriesById = categoriesById,
            query = currentFilters.query,
            categoryIds = currentFilters.selectedCategories
        )
        TransactionListUiState(
            monthLabel = currentMonth.toMonthLabel(),
            transactionCount = dayGroups.sumOf { it.transactions.size },
            dayGroups = dayGroups,
            isLoading = false,
            query = currentFilters.query,
            quickFilter = currentFilters.quickFilter,
            selectedCategories = currentFilters.selectedCategories
        )
    }.catch { error ->
        Log.e("TransactionListViewModel", "Failed to load transactions", error)
        emit(TransactionListUiState(monthLabel = currentMonth.toMonthLabel(), isLoading = false))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionListUiState(monthLabel = currentMonth.toMonthLabel())
    )

    fun onQueryChange(query: String) {
        filters.update { it.copy(query = query) }
    }

    fun onQuickFilterSelected(filter: QuickFilter) {
        filters.update { it.copy(quickFilter = filter) }
    }

    fun setCategoryFilter(categoryIds: Set<String>) {
        filters.update { it.copy(selectedCategories = categoryIds) }
    }
}
