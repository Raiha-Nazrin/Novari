package com.example.novari.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.core.database.dao.MerchantSummary
import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.SearchField
import com.example.novari.domain.repository.AddCategoryResult
import com.example.novari.domain.repository.CategoryRepository
import com.example.novari.domain.repository.RecentSearchRepository
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.ui.model.toUiModel
import com.example.novari.ui.screens.transactions.transaction_list.DateFilter
import com.example.novari.ui.screens.transactions.transaction_list.toEpochMillisRange
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

private const val SEARCH_DEBOUNCE_MS = 250L

private data class Filters(
    val query: String = "",
    val searchField: SearchField = SearchField.MERCHANT,
    val selectedCategoryIds: Set<String> = emptySet(),
    val selectedMerchantKeys: Set<String> = emptySet(),
    val amountRange: AmountRange = AmountRange(),
    val dateFilter: DateFilter = DateFilter.None
) {
    val hasActiveCriteria: Boolean
        get() = query.isNotBlank() ||
            selectedCategoryIds.isNotEmpty() ||
            selectedMerchantKeys.isNotEmpty() ||
            !amountRange.isEmpty ||
            dateFilter != DateFilter.None
}

private sealed interface SearchOutcome {
    data object Idle : SearchOutcome
    data object Loading : SearchOutcome
    data class Success(val transactions: List<TransactionEntity>) : SearchOutcome
    data class Error(val message: String) : SearchOutcome
}

/**
 * Backs SearchScreen. [Filters.searchField] decides which of [Filters.query],
 * [Filters.selectedCategoryIds], [Filters.amountRange] or [Filters.dateFilter]
 * actually drives the DB query -- see [Filters.toSearchQuery].
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val recentSearchRepository: RecentSearchRepository
) : ViewModel() {

    private val filters = MutableStateFlow(Filters())
    private val addCategoryError = MutableStateFlow<String?>(null)
    private val retrySignal = MutableStateFlow(0)

    private val categoriesFlow = categoryRepository.observeActive()
    private val merchantsFlow = transactionRepository.observeMerchants()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val searchOutcome: Flow<SearchOutcome> = combine(
        filters.debounce(SEARCH_DEBOUNCE_MS),
        categoriesFlow,
        retrySignal
    ) { current, categories, retry -> Triple(current, categories, retry) }
        .distinctUntilChanged()
        .flatMapLatest { (current, categories, _) ->
            if (!current.hasActiveCriteria) {
                flowOf(SearchOutcome.Idle)
            } else {
                current.toSearchQuery(categories).let { criteria ->
                    transactionRepository.observeSearch(
                        merchantQuery = criteria.merchantQuery,
                        merchantKeys = criteria.merchantKeys,
                        categoryIds = criteria.categoryIds,
                        minAmountMinor = criteria.minAmountMinor,
                        maxAmountMinor = criteria.maxAmountMinor,
                        startInclusive = criteria.startInclusive,
                        endInclusive = criteria.endInclusive
                    )
                }
                    .map<List<TransactionEntity>, SearchOutcome> {
                        SearchOutcome.Success(it)
                    }
                    .onStart { emit(SearchOutcome.Loading) }
                    .catch { error ->
                        Timber.tag("SearchViewModel").e(error, "Search failed")
                        emit(SearchOutcome.Error("Something went wrong. Please try again."))
                    }
            }
        }

    private val categoriesAndMerchants: Flow<Pair<List<CategoryEntity>, List<MerchantSummary>>> =
        combine(categoriesFlow, merchantsFlow) { categories, merchants -> categories to merchants }

    val uiState: StateFlow<SearchUiState> = combine(
        filters,
        searchOutcome,
        categoriesAndMerchants,
        recentSearchRepository.observeRecent(),
        addCategoryError
    ) { current, outcome, categoriesAndMerchants, recents, categoryError ->
        val (categories, merchants) = categoriesAndMerchants
        SearchUiState(
            query = current.query,
            searchField = current.searchField,
            categories = categories.map { it.toUiModel() },
            selectedCategoryIds = current.selectedCategoryIds,
            addCategoryError = categoryError,
            merchants = merchants.map { it.toUiModel() },
            selectedMerchantKeys = current.selectedMerchantKeys,
            amountRange = current.amountRange,
            dateFilter = current.dateFilter,
            recentSearches = recents.map { it.query },
            results = (outcome as? SearchOutcome.Success)?.transactions
                ?.toSearchResults(categories.associateBy { it.id })
                ?: emptyList(),
            isLoading = outcome is SearchOutcome.Loading,
            hasSearched = current.hasActiveCriteria,
            errorMessage = (outcome as? SearchOutcome.Error)?.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(query: String) {
        filters.update { it.copy(query = query) }
    }

    fun onSearchFieldSelected(field: SearchField) {
        filters.update { it.copy(searchField = field) }
    }

    /** Called on Enter / the search icon -- records the query, typing alone does not. */
    fun onSubmitSearch() {
        val current = filters.value
        if (!current.hasActiveCriteria) return
        viewModelScope.launch {
            recentSearchRepository.record(
                query = current.query.ifBlank { current.searchField.name },
                searchField = current.searchField
            )
        }
    }

    fun onRecentSearchClick(query: String) {
        filters.update { it.copy(query = query) }
        viewModelScope.launch {
            recentSearchRepository.record(query, filters.value.searchField)
        }
    }

    fun onRemoveRecentSearch(query: String) {
        viewModelScope.launch { recentSearchRepository.remove(query) }
    }

    fun onClearRecentSearches() {
        viewModelScope.launch { recentSearchRepository.clearAll() }
    }

    fun clearQuery() {
        filters.update { it.copy(query = "") }
    }

    fun setCategoryFilter(categoryIds: Set<String>) {
        filters.update { it.copy(selectedCategoryIds = categoryIds) }
    }

    fun setMerchantFilter(merchantKeys: Set<String>) {
        filters.update { it.copy(selectedMerchantKeys = merchantKeys) }
    }

    fun setAmountRange(range: AmountRange) {
        filters.update { it.copy(amountRange = range) }
    }

    fun setDateFilter(dateFilter: DateFilter) {
        filters.update { it.copy(dateFilter = dateFilter) }
    }

    fun clearDateFilter() {
        filters.update { it.copy(dateFilter = DateFilter.None) }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            when (categoryRepository.addUserCategory(name)) {
                is AddCategoryResult.Success -> addCategoryError.value = null
                AddCategoryResult.BlankName -> addCategoryError.value = "Enter a category name"
                AddCategoryResult.DuplicateName -> addCategoryError.value = "That category already exists"
            }
        }
    }

    fun clearAddCategoryError() {
        addCategoryError.value = null
    }

    fun retry() {
        retrySignal.update { it + 1 }
    }
}

private data class SearchQuery(
    val merchantQuery: String?,
    val merchantKeys: Set<String>,
    val categoryIds: Set<String>,
    val minAmountMinor: Long?,
    val maxAmountMinor: Long?,
    val startInclusive: Long?,
    val endInclusive: Long?
)

/** Resolves the active [Filters.searchField] scope into DAO-ready query parameters. */
private fun Filters.toSearchQuery(categories: List<CategoryEntity>): SearchQuery = when (searchField) {
    SearchField.MERCHANT -> SearchQuery(
        merchantQuery = query.trim().takeIf { it.isNotBlank() },
        merchantKeys = selectedMerchantKeys,
        categoryIds = emptySet(),
        minAmountMinor = null,
        maxAmountMinor = null,
        startInclusive = null,
        endInclusive = null
    )

    SearchField.CATEGORY -> {
        val trimmedQuery = query.trim()
        val matchedByName = if (trimmedQuery.isBlank()) {
            emptySet()
        } else {
            categories.filter { it.name.contains(trimmedQuery, ignoreCase = true) }
                .map { it.id }
                .toSet()
        }
        SearchQuery(
            merchantQuery = null,
            merchantKeys = emptySet(),
            categoryIds = selectedCategoryIds.ifEmpty { matchedByName },
            minAmountMinor = null,
            maxAmountMinor = null,
            startInclusive = null,
            endInclusive = null
        )
    }

    SearchField.AMOUNT -> {
        val range = amountRange.takeIf { !it.isEmpty } ?: parseAmountRangeQuery(query)
        SearchQuery(
            merchantQuery = null,
            merchantKeys = emptySet(),
            categoryIds = emptySet(),
            minAmountMinor = range.minMinor,
            maxAmountMinor = range.maxMinor,
            startInclusive = null,
            endInclusive = null
        )
    }

    SearchField.DATE -> {
        val range = dateFilter.toEpochMillisRange()
        SearchQuery(
            merchantQuery = null,
            merchantKeys = emptySet(),
            categoryIds = emptySet(),
            minAmountMinor = null,
            maxAmountMinor = null,
            startInclusive = range?.first,
            endInclusive = range?.last
        )
    }
}
