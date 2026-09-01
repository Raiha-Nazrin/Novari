package com.example.novari.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.core.database.dao.CategoryDao
import com.example.novari.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

/**
 * Backs InsightsScreen with real spend data aggregated from the transactions
 * table: month-to-date vs the same span last month, this week vs last week,
 * a category breakdown, and the categories that moved the most.
 *
 * "Today" is captured once per ViewModel instance rather than recomputed per
 * emission -- if the screen is left open across midnight the ranges won't
 * shift until the next process/ViewModel restart, which mirrors how
 * HomeViewModel treats "today" for date labels.
 */
@HiltViewModel
class InsightsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryDao: CategoryDao
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()
    private val monthStart: LocalDate = today.withDayOfMonth(1)
    private val weekStart: LocalDate = today.minusDays((today.dayOfWeek.value - 1).toLong())

    private val monthRange = monthToDateRange(today)
    private val previousMonthRange = previousMonthToDateRange(today)
    private val weekRange = weekToDateRange(today)
    private val previousWeekRange = previousWeekToDateRange(today)

    val uiState: StateFlow<InsightsUiState> = combine(
        transactionRepository.observeBetween(monthRange.first, monthRange.second),
        transactionRepository.observeBetween(previousMonthRange.first, previousMonthRange.second),
        transactionRepository.observeBetween(weekRange.first, weekRange.second),
        transactionRepository.observeBetween(previousWeekRange.first, previousWeekRange.second),
        categoryDao.observeActive()
    ) { currentMonthTx, previousMonthTx, currentWeekTx, previousWeekTx, categories ->
        val categoriesById = categories.associateBy { it.id }

        val monthlySpentMinor = sumExpenseMinor(currentMonthTx)
        val previousMonthlySpentMinor = sumExpenseMinor(previousMonthTx)
        val monthlyChange = percentChange(monthlySpentMinor, previousMonthlySpentMinor)

        val weeklySpentMinor = sumExpenseMinor(currentWeekTx)
        val previousWeeklySpentMinor = sumExpenseMinor(previousWeekTx)
        val weeklyChange = percentChange(weeklySpentMinor, previousWeeklySpentMinor)

        InsightsUiState(
            monthLabel = monthLabel(today),
            monthlySpent = monthlySpentMinor / 100L,
            monthlyPercentageChange = abs(monthlyChange),
            isLessThanLastMonth = monthlyChange <= 0,
            spendingTrendPoints = buildSpendingTrendPoints(currentMonthTx, monthStart, today),
            categories = buildCategoryBreakdown(currentMonthTx, categoriesById),
            weeklyValues = buildWeeklyValues(currentWeekTx, weekStart),
            weeklyTotal = weeklySpentMinor / 100L,
            weeklyPercentageChange = abs(weeklyChange),
            isLessThanLastWeek = weeklyChange <= 0,
            whatChanged = buildWhatChanged(currentMonthTx, previousMonthTx, categoriesById)
        )
    }.catch { error ->
        Timber.tag("InsightsViewModel").e(error, "Failed to load insights")
        emit(InsightsUiState(monthLabel = monthLabel(today)))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InsightsUiState(monthLabel = monthLabel(today))
    )
}
