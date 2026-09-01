package com.example.novari.ui.screens.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.example.novari.core.database.dao.CategoryDao
import com.example.novari.core.database.dao.MerchantSummary
import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.TransactionSource
import com.example.novari.core.model.TransactionType
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.permissions.AutoTrackingPromptStore
import com.example.novari.sms.health.SmsDetectionHealthRepository
import com.example.novari.sms.permission.SmsPermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

private class FakeTransactionRepository(
    private val recent: List<TransactionEntity>
) : TransactionRepository {
    override suspend fun create(transaction: TransactionEntity) {}
    override suspend fun update(transaction: TransactionEntity) {}
    override suspend fun delete(transaction: TransactionEntity) {}
    override suspend fun releaseForReparse(transaction: TransactionEntity) {}
    override suspend fun findById(id: String): TransactionEntity? = null
    override fun observeById(id: String): Flow<TransactionEntity?> = MutableStateFlow(null)
    override suspend fun findBySourceReference(reference: String): TransactionEntity? = null
    override fun observeActive(): Flow<List<TransactionEntity>> = MutableStateFlow(recent)
    override fun observeRecent(limit: Int): Flow<List<TransactionEntity>> =
        MutableStateFlow(recent.take(limit))
    override fun observeBetween(startInclusive: Long, endInclusive: Long): Flow<List<TransactionEntity>> =
        MutableStateFlow(recent.filter { it.transactionDate in startInclusive..endInclusive })
    override fun searchActive(query: String): Flow<List<TransactionEntity>> = MutableStateFlow(recent)
    override fun observeSearch(
        merchantQuery: String?,
        merchantKeys: Set<String>,
        categoryIds: Set<String>,
        minAmountMinor: Long?,
        maxAmountMinor: Long?,
        startInclusive: Long?,
        endInclusive: Long?
    ): Flow<List<TransactionEntity>> = MutableStateFlow(emptyList())
    override fun observeMerchants(): Flow<List<MerchantSummary>> = MutableStateFlow(emptyList())
}

private class FakeSmsDetectionHealthRepository : SmsDetectionHealthRepository {
    override fun observeProcessedCount(): Flow<Int> = flowOf(0)
    override fun observeIgnoredCount(): Flow<Int> = flowOf(0)
    override fun observeLastSuccessfulSweepAt(): Flow<Long?> = flowOf(null)
}

private class FakeSmsPermissionChecker(
    private val canRead: Boolean = true,
    private val canReceive: Boolean = true
) : SmsPermissionChecker {
    override fun canRead(): Boolean = canRead
    override fun canReceive(): Boolean = canReceive
}

private class FakeCategoryDao(
    private val categories: List<CategoryEntity> = emptyList()
) : CategoryDao {
    override suspend fun insert(entity: CategoryEntity) {}
    override suspend fun insertAll(entities: List<CategoryEntity>) {}
    override suspend fun update(entity: CategoryEntity) {}
    override fun observeActive(): Flow<List<CategoryEntity>> = MutableStateFlow(categories)
    override suspend fun findById(id: String): CategoryEntity? = categories.find { it.id == id }
    override suspend fun findByName(name: String): CategoryEntity? =
        categories.find { it.name.equals(name, ignoreCase = true) }
    override suspend fun reactivate(id: String, now: Long) {}
    override suspend fun deactivate(id: String, now: Long) {}
}

/**
 * DataStore's initial read happens on a real IO dispatcher, so these tests
 * use a real (unconfined) Main dispatcher and wait on the flow directly
 * rather than virtual test time, which can't advance IO-dispatched work.
 */
class HomeViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun promptStoreFor(file: File, alreadyVisited: Boolean): AutoTrackingPromptStore {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { file })
        val store = AutoTrackingPromptStore(dataStore)
        if (alreadyVisited) {
            runBlocking { store.markSetupVisited() }
        }
        return store
    }

    private fun transaction(id: String, dateMillis: Long) = TransactionEntity(
        id = id,
        amountMinor = 10_000L,
        currencyCode = "INR",
        merchant = "Merchant $id",
        categoryId = null,
        transactionType = TransactionType.EXPENSE,
        transactionDate = dateMillis,
        notes = null,
        source = TransactionSource.MANUAL,
        sourceReference = null,
        createdAt = dateMillis,
        updatedAt = dateMillis,
        deletedAt = null,
        revision = 1L
    )

    @Test
    fun `starts with Loading prompt visibility before the store emits`() {
        val store = promptStoreFor(tempFolder.newFile("vm1.preferences_pb"), alreadyVisited = false)
        val viewModel = HomeViewModel(
            transactionRepository = FakeTransactionRepository(emptyList()),
            categoryDao = FakeCategoryDao(),
            autoTrackingPromptStore = store,
            smsDetectionHealthRepository = FakeSmsDetectionHealthRepository(),
            smsPermissionChecker = FakeSmsPermissionChecker()
        )

        assertEquals(AutoTrackingPromptVisibility.Loading, viewModel.uiState.value.autoTrackingPrompt)
    }

    @Test
    fun `exposes only the transactions the repository returns for the recent-limit query`() = runBlocking {
        // The repository is responsible for enforcing the "latest 5" cap (via
        // observeRecent's LIMIT clause) — the ViewModel just renders whatever
        // it's handed, so this confirms the ViewModel doesn't re-truncate or
        // reorder that result itself.
        val transactions = listOf(transaction("txn_1", dateMillis = 1_000L))
        val store = promptStoreFor(tempFolder.newFile("vm2.preferences_pb"), alreadyVisited = true)
        val viewModel = HomeViewModel(
            transactionRepository = FakeTransactionRepository(transactions),
            categoryDao = FakeCategoryDao(),
            autoTrackingPromptStore = store,
            smsDetectionHealthRepository = FakeSmsDetectionHealthRepository(),
            smsPermissionChecker = FakeSmsPermissionChecker()
        )

        val state = withTimeout(5_000) {
            viewModel.uiState.first { it.autoTrackingPrompt != AutoTrackingPromptVisibility.Loading }
        }

        assertEquals(1, state.transactions.size)
        assertEquals("txn_1", state.transactions.first().id)
        assertEquals("Merchant txn_1", state.transactions.first().title)
    }

    @Test
    fun `falls back to Uncategorized when a transaction has no matching category`() = runBlocking {
        val transactions = listOf(transaction("txn_1", dateMillis = 1_000L))
        val store = promptStoreFor(tempFolder.newFile("vm3.preferences_pb"), alreadyVisited = true)
        val viewModel = HomeViewModel(
            transactionRepository = FakeTransactionRepository(transactions),
            categoryDao = FakeCategoryDao(),
            autoTrackingPromptStore = store,
            smsDetectionHealthRepository = FakeSmsDetectionHealthRepository(),
            smsPermissionChecker = FakeSmsPermissionChecker()
        )

        val state = withTimeout(5_000) {
            viewModel.uiState.first { it.transactions.isNotEmpty() }
        }

        assertEquals("Uncategorized", state.transactions.first().category)
    }
}
