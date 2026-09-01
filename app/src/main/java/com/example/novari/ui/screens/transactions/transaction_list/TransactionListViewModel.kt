package com.example.novari.ui.screens.transactions.transaction_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.domain.repository.AddCategoryResult
import com.example.novari.domain.repository.CategoryRepository
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.ui.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

private data class Filters(
    val query: String = "",
    val quickFilter: QuickFilter = QuickFilter.ALL,
    val selectedCategories: Set<String> = emptySet(),
    val dateFilter: DateFilter = DateFilter.None
)

/**
 * Backs TransactionListScreen with transactions from the database.
 * [Filters.quickFilter] only ever narrows within the current calendar month
 * ("All" vs "This month" are equivalent until other months are browsable via
 * chips). [Filters.dateFilter], set from the calendar picker, overrides that
 * month window entirely and drives the DB query range directly.
 */
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val currentMonth = YearMonth.now()
    private val filters = MutableStateFlow(Filters())
    private val addCategoryError = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rangeTransactions = filters
        .map { it.dateFilter.toEpochMillisRange() ?: currentMonth.toEpochMillisRange() }
        .distinctUntilChanged()
        .flatMapLatest { range -> transactionRepository.observeBetween(range.first, range.last) }

    val uiState: StateFlow<TransactionListUiState> = combine(
        rangeTransactions,
        categoryRepository.observeActive(),
        filters,
        addCategoryError
    ) { transactions, categories, currentFilters, categoryError ->
        val categoriesById = categories.associateBy { it.id }
        val dayGroups = buildDayGroups(
            entities = transactions,
            categoriesById = categoriesById,
            query = currentFilters.query,
            categoryIds = currentFilters.selectedCategories
        )
        TransactionListUiState(
            rangeLabel = currentFilters.dateFilter.toRangeLabel(currentMonth),
            transactionCount = dayGroups.sumOf { it.transactions.size },
            dayGroups = dayGroups,
            isLoading = false,
            query = currentFilters.query,
            quickFilter = currentFilters.quickFilter,
            categories = categories.map { it.toUiModel() },
            selectedCategories = currentFilters.selectedCategories,
            addCategoryError = categoryError,
            dateFilter = currentFilters.dateFilter,
            dateFilterLabel = currentFilters.dateFilter.chipLabel()
        )
    }.catch { error ->
        Timber.tag("TransactionListViewModel").e(error, "Failed to load transactions")
        emit(TransactionListUiState(rangeLabel = currentMonth.toMonthLabel(), isLoading = false))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionListUiState(rangeLabel = currentMonth.toMonthLabel())
    )

    fun onQueryChange(query: String) {
        filters.update { it.copy(query = query) }
    }

    fun onQuickFilterSelected(filter: QuickFilter) {
        filters.update { it.copy(quickFilter = filter, dateFilter = DateFilter.None) }
    }

    fun setCategoryFilter(categoryIds: Set<String>) {
        filters.update { it.copy(selectedCategories = categoryIds) }
    }

    fun setDateFilter(dateFilter: DateFilter) {
        filters.update { it.copy(dateFilter = dateFilter, quickFilter = QuickFilter.ALL) }
    }

    fun clearDateFilter() {
        filters.update { it.copy(dateFilter = DateFilter.None, quickFilter = QuickFilter.THIS_MONTH) }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            when (val result = categoryRepository.addUserCategory(name)) {
                is AddCategoryResult.Success -> addCategoryError.value = null
                AddCategoryResult.BlankName -> addCategoryError.value = "Enter a category name"
                AddCategoryResult.DuplicateName -> addCategoryError.value = "That category already exists"
            }
        }
    }

    fun clearAddCategoryError() {
        addCategoryError.value = null
    }
}
