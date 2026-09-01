package com.example.novari.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.core.database.dao.CategoryDao
import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.TransactionType
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.permissions.AutoTrackingPromptStore
import com.example.novari.sms.health.SmsDetectionHealthRepository
import com.example.novari.sms.health.SmsDetectionHealthState
import com.example.novari.sms.permission.SmsPermissionChecker
import com.example.novari.ui.screens.insights.monthToDateRange
import com.example.novari.ui.screens.insights.percentChange
import com.example.novari.ui.screens.insights.previousMonthToDateRange
import com.example.novari.ui.screens.insights.previousWeekToDateRange
import com.example.novari.ui.screens.insights.sumExpenseMinor
import com.example.novari.ui.screens.insights.weekToDateRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

private const val RECENT_TRANSACTIONS_LIMIT = 5

/**
 * Backs HomeScreen with real data: the [RECENT_TRANSACTIONS_LIMIT] latest
 * transactions from the database plus the auto-tracking prompt's visibility
 * (folded in here rather than kept in a separate AutoTrackingPromptViewModel,
 * so the screen collects a single state stream).
 *
 * userName is still a placeholder -- there's no user profile source yet.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryDao: CategoryDao,
    autoTrackingPromptStore: AutoTrackingPromptStore,
    smsDetectionHealthRepository: SmsDetectionHealthRepository,
    private val smsPermissionChecker: SmsPermissionChecker
) : ViewModel() {

    // Permission grants aren't observable, so this is refreshed explicitly -- see
    // refreshSmsPermissionState(), called from RefreshOnResume in HomeScreen so a permission
    // the user just changed in system Settings is reflected the moment they come back.
    private val today: LocalDate = LocalDate.now()
    private val weekRange = weekToDateRange(today)
    private val previousWeekRange = previousWeekToDateRange(today)
    private val monthRange = monthToDateRange(today)
    private val previousMonthRange = previousMonthToDateRange(today)
    private val todayStartMillis = today.atStartOfDay(java.time.ZoneId.systemDefault())
        .toInstant().toEpochMilli()

    private val smsPermissionState = MutableStateFlow(currentSmsPermissionSnapshot())

    private val smsHealth: StateFlow<SmsDetectionHealthState> = combine(
        smsPermissionState,
        smsDetectionHealthRepository.observeLastSuccessfulSweepAt(),
        smsDetectionHealthRepository.observeProcessedCount(),
        smsDetectionHealthRepository.observeIgnoredCount()
    ) { permission, lastSweepAt, processed, ignored ->
        SmsDetectionHealthState(
            canReadSms = permission.first,
            canReceiveSms = permission.second,
            lastSuccessfulSweepAt = lastSweepAt,
            processedCount = processed,
            ignoredCount = ignored
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SmsDetectionHealthState()
    )

    private val recentTransactions = combine(
        transactionRepository.observeRecent(RECENT_TRANSACTIONS_LIMIT),
        categoryDao.observeActive()
    ) { transactions, categories ->
        val categoriesById = categories.associateBy { it.id }
        Timber.tag("transactions ++").e(transactions.toString())
        transactions.map { it.toHomeTransaction(categoriesById) }
    }.catch { error ->
        Timber.tag("HomeViewModel").e(error, "Failed to load recent transactions")
        emit(emptyList())
    }

    private val autoTrackingPromptVisibility = autoTrackingPromptStore.hasVisitedSetup
        .map { hasVisited ->
            if (hasVisited) AutoTrackingPromptVisibility.Hidden else AutoTrackingPromptVisibility.Visible
        }

    private val weeklyInsightMessage = combine(
        transactionRepository.observeBetween(weekRange.first, weekRange.second),
        transactionRepository.observeBetween(previousWeekRange.first, previousWeekRange.second),
        categoryDao.observeActive()
    ) { currentWeekTx, previousWeekTx, categories ->
        buildWeeklyInsightMessage(currentWeekTx, previousWeekTx, categories.associateBy { it.id })
    }.catch { error ->
        Timber.tag("HomeViewModel").e(error, "Failed to build weekly insight")
        emit(null)
    }

    private val spendSummary = combine(
        transactionRepository.observeBetween(monthRange.first, monthRange.second),
        transactionRepository.observeBetween(previousMonthRange.first, previousMonthRange.second)
    ) { monthTx, previousMonthTx ->
        buildSpendSummary(monthTx, previousMonthTx, todayStartMillis)
    }.catch { error ->
        Timber.tag("HomeViewModel").e(error, "Failed to build spend summary")
        emit(HomeSpendSummary())
    }

    val uiState: StateFlow<HomeUiState> = combine(
        recentTransactions,
        autoTrackingPromptVisibility,
        smsHealth,
        weeklyInsightMessage,
        spendSummary
    ) { transactions, promptVisibility, health, insightMessage, summary ->
        HomeUiState(
            monthlyExpense = summary.monthlySpentMinor / 100L,
            monthlyPercentageChange = summary.monthlyPercentageChange,
            isLessThanLastMonth = summary.isLessThanLastMonth,
            todayExpense = summary.todaySpentMinor / 100L,
            todayMessage = summary.todayMessage,
            transactions = transactions,
            autoTrackingPrompt = promptVisibility,
            smsHealth = health,
            insightMessage = insightMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    /** Re-checks SMS permission state -- call on ON_RESUME so a change made in system Settings shows up. */
    fun refreshSmsPermissionState() {
        smsPermissionState.value = currentSmsPermissionSnapshot()
    }

    private fun currentSmsPermissionSnapshot(): Pair<Boolean, Boolean> =
        smsPermissionChecker.canRead() to smsPermissionChecker.canReceive()

    fun getGreeting(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning 👋"
            in 12..16 -> "Good afternoon 👋"
            else -> "Good evening 🌙"
        }
    }
}

/** Month-to-date and today spend, in minor units, for the "This month" and "Today" cards. */
private data class HomeSpendSummary(
    val monthlySpentMinor: Long = 0L,
    val monthlyPercentageChange: Int = 0,
    val isLessThanLastMonth: Boolean = true,
    val todaySpentMinor: Long = 0L,
    val todayMessage: String = ""
)

private fun buildSpendSummary(
    monthTx: List<TransactionEntity>,
    previousMonthTx: List<TransactionEntity>,
    todayStartMillis: Long
): HomeSpendSummary {
    val monthlySpentMinor = sumExpenseMinor(monthTx)
    val previousMonthlySpentMinor = sumExpenseMinor(previousMonthTx)
    val change = percentChange(monthlySpentMinor, previousMonthlySpentMinor)

    val todaySpentMinor = sumExpenseMinor(monthTx.filter { it.transactionDate >= todayStartMillis })

    return HomeSpendSummary(
        monthlySpentMinor = monthlySpentMinor,
        monthlyPercentageChange = abs(change),
        isLessThanLastMonth = change <= 0,
        todaySpentMinor = todaySpentMinor,
        todayMessage = buildTodayMessage(monthlySpentMinor, todaySpentMinor)
    )
}

/** Short line under the "Today" amount, comparing today's spend to this month's daily average so far. */
private fun buildTodayMessage(monthlySpentMinor: Long, todaySpentMinor: Long): String {
    if (todaySpentMinor <= 0L) return "No spending logged yet today."
    val dayOfMonth = LocalDate.now().dayOfMonth
    val dailyAverageMinor = monthlySpentMinor / dayOfMonth
    return when {
        dailyAverageMinor <= 0L -> "You're doing okay today."
        todaySpentMinor < dailyAverageMinor -> "You're under your usual daily spend."
        todaySpentMinor > dailyAverageMinor -> "Above your usual day so far."
        else -> "Right at your usual daily spend."
    }
}

/**
 * Message for the "A little insight" card: the category with the biggest
 * week-over-week spend swing, e.g. "You've spent 32% less on Dining this
 * week." Null when there's no current-week expense to compare, so the card
 * has nothing real to show and HomeScreen hides it.
 */
private fun buildWeeklyInsightMessage(
    currentWeek: List<TransactionEntity>,
    previousWeek: List<TransactionEntity>,
    categoriesById: Map<String, CategoryEntity>
): String? {
    val currentTotals = currentWeek
        .filter { it.transactionType == TransactionType.EXPENSE }
        .groupBy { it.categoryId }
        .mapValues { it.value.sumOf { tx -> tx.amountMinor } }
    val previousTotals = previousWeek
        .filter { it.transactionType == TransactionType.EXPENSE }
        .groupBy { it.categoryId }
        .mapValues { it.value.sumOf { tx -> tx.amountMinor } }

    val (categoryId, changePercent) = currentTotals.keys
        .mapNotNull { id ->
            val curr = currentTotals[id] ?: return@mapNotNull null
            if (curr <= 0L) return@mapNotNull null
            id to percentChange(curr, previousTotals[id] ?: 0L)
        }
        .filter { (_, change) -> change != 0 }
        .maxByOrNull { (_, change) -> abs(change) }
        ?: return null

    val categoryName = categoryId?.let { categoriesById[it]?.name } ?: "Uncategorized"
    val direction = if (changePercent > 0) "more" else "less"
    return "You've spent ${abs(changePercent)}% $direction on $categoryName this week."
}
