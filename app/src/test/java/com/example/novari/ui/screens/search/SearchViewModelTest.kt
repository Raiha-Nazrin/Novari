package com.example.novari.ui.screens.search

import com.example.novari.core.database.dao.MerchantSummary
import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.SearchField
import com.example.novari.core.model.TransactionSource
import com.example.novari.core.model.TransactionType
import com.example.novari.domain.repository.AddCategoryResult
import com.example.novari.domain.repository.CategoryRepository
import com.example.novari.domain.repository.RecentSearch
import com.example.novari.domain.repository.RecentSearchRepository
import com.example.novari.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeTransactionRepository(
    private val all: List<TransactionEntity> = emptyList(),
    private val error: Throwable? = null
) : TransactionRepository {
    override suspend fun create(transaction: TransactionEntity) {}
    override suspend fun update(transaction: TransactionEntity) {}
    override suspend fun delete(transaction: TransactionEntity) {}
    override suspend fun releaseForReparse(transaction: TransactionEntity) {}
    override suspend fun findById(id: String): TransactionEntity? = null
    override fun observeById(id: String): Flow<TransactionEntity?> = MutableStateFlow(null)
    override suspend fun findBySourceReference(reference: String): TransactionEntity? = null
    override fun observeActive(): Flow<List<TransactionEntity>> = MutableStateFlow(all)
    override fun observeRecent(limit: Int): Flow<List<TransactionEntity>> = MutableStateFlow(all.take(limit))
    override fun observeBetween(startInclusive: Long, endInclusive: Long): Flow<List<TransactionEntity>> =
        MutableStateFlow(all)
    override fun searchActive(query: String): Flow<List<TransactionEntity>> = MutableStateFlow(all)

    override fun observeSearch(
        merchantQuery: String?,
        merchantKeys: Set<String>,
        categoryIds: Set<String>,
        minAmountMinor: Long?,
        maxAmountMinor: Long?,
        startInclusive: Long?,
        endInclusive: Long?
    ): Flow<List<TransactionEntity>> {
        if (error != null) return flow { throw error }
        val filtered = all.filter { entity ->
            val matchesMerchant = merchantQuery == null ||
                entity.merchant?.contains(merchantQuery, ignoreCase = true) == true
            val matchesMerchantKeys = merchantKeys.isEmpty() ||
                entity.merchant?.trim()?.uppercase() in merchantKeys
            val matchesCategory = categoryIds.isEmpty() || entity.categoryId in categoryIds
            val matchesMin = minAmountMinor == null || entity.amountMinor >= minAmountMinor
            val matchesMax = maxAmountMinor == null || entity.amountMinor <= maxAmountMinor
            val matchesDate = startInclusive == null ||
                entity.transactionDate in startInclusive..(endInclusive ?: Long.MAX_VALUE)
            matchesMerchant && matchesMerchantKeys && matchesCategory && matchesMin && matchesMax && matchesDate
        }
        return MutableStateFlow(filtered)
    }

    override fun observeMerchants(): Flow<List<MerchantSummary>> {
        val summaries = all
            .mapNotNull { it.merchant?.trim()?.takeIf { m -> m.isNotEmpty() } }
            .groupBy { it.uppercase() }
            .map { (_, merchants) ->
                MerchantSummary(
                    merchant = merchants.first(),
                    transactionCount = merchants.size,
                    lastTransactionDate = 0L
                )
            }
        return MutableStateFlow(summaries)
    }
}

private class FakeCategoryRepository(
    private val categories: List<CategoryEntity> = emptyList()
) : CategoryRepository {
    override fun observeActive(): Flow<List<CategoryEntity>> = MutableStateFlow(categories)
    override suspend fun findById(id: String): CategoryEntity? = categories.find { it.id == id }
    override suspend fun addUserCategory(name: String): AddCategoryResult = AddCategoryResult.Success("new")
    override suspend fun rename(id: String, newName: String): AddCategoryResult = AddCategoryResult.Success(id)
    override suspend fun deactivate(id: String) {}
}

private class FakeRecentSearchRepository : RecentSearchRepository {
    private val recent = MutableStateFlow<List<RecentSearch>>(emptyList())
    val recordedQueries = mutableListOf<String>()

    override fun observeRecent(): Flow<List<RecentSearch>> = recent

    override suspend fun record(query: String, searchField: SearchField) {
        recordedQueries += query
        recent.value = listOf(RecentSearch(query, searchField)) + recent.value.filterNot { it.query == query }
    }

    override suspend fun remove(query: String) {
        recent.value = recent.value.filterNot { it.query == query }
    }

    override suspend fun clearAll() {
        recent.value = emptyList()
    }
}

class SearchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * uiState is a WhileSubscribed StateFlow, so it only starts producing once
     * something collects it. This keeps a collector alive and drives virtual
     * time (past debounce, etc.) so [SearchViewModel.uiState].value is current.
     */
    private fun TestScope.settledStateOf(viewModel: SearchViewModel): SearchUiState {
        val job = launch(testDispatcher) { viewModel.uiState.collect {} }
        advanceUntilIdle()
        val state = viewModel.uiState.value
        job.cancel()
        return state
    }

    private fun category(id: String, name: String) = CategoryEntity(
        id = id,
        name = name,
        iconKey = null,
        isSystem = false,
        isActive = true,
        createdAt = 0L,
        updatedAt = 0L
    )

    private fun transaction(
        id: String,
        merchant: String?,
        categoryId: String? = null,
        amountMinor: Long = 10_000L,
        transactionDate: Long = 1_000L
    ) = TransactionEntity(
        id = id,
        amountMinor = amountMinor,
        currencyCode = "INR",
        merchant = merchant,
        categoryId = categoryId,
        transactionType = TransactionType.EXPENSE,
        transactionDate = transactionDate,
        notes = null,
        source = TransactionSource.MANUAL,
        sourceReference = null,
        createdAt = transactionDate,
        updatedAt = transactionDate,
        deletedAt = null,
        revision = 1L
    )

    @Test
    fun `no query and no filters shows recent searches instead of results`() = runTest(testDispatcher) {
        val recentSearchRepository = FakeRecentSearchRepository()
        recentSearchRepository.record("Swiggy", SearchField.MERCHANT)
        val viewModel = SearchViewModel(
            transactionRepository = FakeTransactionRepository(),
            categoryRepository = FakeCategoryRepository(),
            recentSearchRepository = recentSearchRepository
        )

        val state = settledStateOf(viewModel)

        assertTrue(state.showRecentSearches)
        assertEquals(listOf("Swiggy"), state.recentSearches)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun `merchant search field filters results by merchant text`() = runTest(testDispatcher) {
        val transactions = listOf(
            transaction("t1", merchant = "Swiggy"),
            transaction("t2", merchant = "Amazon")
        )
        val viewModel = SearchViewModel(
            transactionRepository = FakeTransactionRepository(transactions),
            categoryRepository = FakeCategoryRepository(),
            recentSearchRepository = FakeRecentSearchRepository()
        )

        viewModel.onQueryChange("swig")
        val state = settledStateOf(viewModel)

        assertEquals(1, state.results.size)
        assertEquals("t1", state.results.first().id)
    }

    @Test
    fun `category search field filters by selected category ids, ignoring the merchant text`() =
        runTest(testDispatcher) {
            val groceries = category("cat_groceries", "Groceries")
            val transactions = listOf(
                transaction("t1", merchant = "BigBasket", categoryId = groceries.id),
                transaction("t2", merchant = "Swiggy", categoryId = "cat_food")
            )
            val viewModel = SearchViewModel(
                transactionRepository = FakeTransactionRepository(transactions),
                categoryRepository = FakeCategoryRepository(listOf(groceries)),
                recentSearchRepository = FakeRecentSearchRepository()
            )

            viewModel.onSearchFieldSelected(SearchField.CATEGORY)
            viewModel.setCategoryFilter(setOf(groceries.id))
            val state = settledStateOf(viewModel)

            assertEquals(1, state.results.size)
            assertEquals("t1", state.results.first().id)
        }

    @Test
    fun `amount search field parses an exact rupee amount from the query text`() = runTest(testDispatcher) {
        val transactions = listOf(
            transaction("t1", merchant = "A", amountMinor = 50_000L),
            transaction("t2", merchant = "B", amountMinor = 12_000L)
        )
        val viewModel = SearchViewModel(
            transactionRepository = FakeTransactionRepository(transactions),
            categoryRepository = FakeCategoryRepository(),
            recentSearchRepository = FakeRecentSearchRepository()
        )

        viewModel.onSearchFieldSelected(SearchField.AMOUNT)
        viewModel.onQueryChange("500")
        val state = settledStateOf(viewModel)

        assertEquals(1, state.results.size)
        assertEquals("t1", state.results.first().id)
    }

    @Test
    fun `a search with no matches reports the empty-result state, not an error`() = runTest(testDispatcher) {
        val transactions = listOf(transaction("t1", merchant = "Swiggy"))
        val viewModel = SearchViewModel(
            transactionRepository = FakeTransactionRepository(transactions),
            categoryRepository = FakeCategoryRepository(),
            recentSearchRepository = FakeRecentSearchRepository()
        )

        viewModel.onQueryChange("nonexistent-merchant")
        val state = settledStateOf(viewModel)

        assertTrue(state.isEmptyResult)
        assertNull(state.errorMessage)
    }

    @Test
    fun `a repository failure surfaces as an error state, not a crash`() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(
            transactionRepository = FakeTransactionRepository(error = IllegalStateException("boom")),
            categoryRepository = FakeCategoryRepository(),
            recentSearchRepository = FakeRecentSearchRepository()
        )

        viewModel.onQueryChange("swiggy")
        val state = settledStateOf(viewModel)

        assertTrue(state.results.isEmpty())
        assertEquals(false, state.isLoading)
        assertEquals("Something went wrong. Please try again.", state.errorMessage)
    }

    @Test
    fun `typing does not record a recent search, only an explicit submit does`() = runTest(testDispatcher) {
        val recentSearchRepository = FakeRecentSearchRepository()
        val viewModel = SearchViewModel(
            transactionRepository = FakeTransactionRepository(listOf(transaction("t1", merchant = "Swiggy"))),
            categoryRepository = FakeCategoryRepository(),
            recentSearchRepository = recentSearchRepository
        )

        viewModel.onQueryChange("Swiggy")
        settledStateOf(viewModel)
        assertTrue(recentSearchRepository.recordedQueries.isEmpty())

        viewModel.onSubmitSearch()
        advanceUntilIdle()

        assertEquals(listOf("Swiggy"), recentSearchRepository.recordedQueries)
    }
}
