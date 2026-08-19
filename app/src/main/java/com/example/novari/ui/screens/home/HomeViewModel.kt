package com.example.novari.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.core.database.dao.CategoryDao
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.permissions.AutoTrackingPromptStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

private const val RECENT_TRANSACTIONS_LIMIT = 5

/**
 * Backs HomeScreen with real data: the [RECENT_TRANSACTIONS_LIMIT] latest
 * transactions from the database plus the auto-tracking prompt's visibility
 * (folded in here rather than kept in a separate AutoTrackingPromptViewModel,
 * so the screen collects a single state stream).
 *
 * userName/monthlyExpense/todayExpense/insightMessage are still placeholders
 * — they need aggregate queries and a user profile source that don't exist
 * yet, and are out of scope for wiring up the transaction list.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryDao: CategoryDao,
    autoTrackingPromptStore: AutoTrackingPromptStore
) : ViewModel() {

    private val recentTransactions = combine(
        transactionRepository.observeRecent(RECENT_TRANSACTIONS_LIMIT),
        categoryDao.observeActive()
    ) { transactions, categories ->
        val categoriesById = categories.associateBy { it.id }
        Log.e("transactions ++", transactions.toString())
        transactions.map { it.toHomeTransaction(categoriesById) }
    }.catch { error ->
        Log.e("HomeViewModel", "Failed to load recent transactions", error)
        emit(emptyList())
    }

    private val autoTrackingPromptVisibility = autoTrackingPromptStore.hasVisitedSetup
        .map { hasVisited ->
            if (hasVisited) AutoTrackingPromptVisibility.Hidden else AutoTrackingPromptVisibility.Visible
        }

    val uiState: StateFlow<HomeUiState> = combine(
        recentTransactions,
        autoTrackingPromptVisibility
    ) { transactions, promptVisibility ->
        HomeUiState(
            transactions = transactions,
            autoTrackingPrompt = promptVisibility
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun getGreeting(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning 👋"
            in 12..16 -> "Good afternoon 👋"
            else -> "Good evening 🌙"
        }
    }
}
