package com.example.novari.ui.screens.home

import androidx.annotation.DrawableRes
import com.example.novari.R


/**
 * A single recent transaction.
 * Modeled as data rather than three hardcoded UI blocks so the list can
 * later be swapped for a real repository/database result with no UI changes.
 *
 * [iconRes] is a drawable resource from the project's res/drawable folder.
 */
data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val date: String,
    val amount: Int,
    @DrawableRes val iconRes: Int
)

/**
 * Everything HomeScreen needs to render, in one place.
 * This is intentionally plain data (no ViewModel) for now — see architecture
 * notes — but its shape is what a ViewModel would expose via StateFlow later.
 */
data class HomeUiState(
    val userName: String = "Arjun",
    val monthlyExpense: Int = 24560 ,
    val monthlyPercentageChange: Int = 8,
    val isLessThanLastMonth: Boolean = true,
    val todayExpense: Int = 840,
    val todayMessage: String = "You're doing okay today.",
    val transactions: List<Transaction> = emptyList(),
    val insightMessage: String = "You've spent less on dining this week."
)

/**
 * Mock data standing in for a repository until the data layer exists.
 * Kept separate from the composables per the "no business/mock data inside
 * UI" rule — HomeScreen only ever receives a HomeUiState.
 * actual category-icon resource names (or a category-to-icon mapping if
 * one already exists elsewhere in the project).
 */
val mockHomeUiState = HomeUiState(
    userName = "Arjun",
    monthlyExpense = 24560,
    monthlyPercentageChange = 8,
    isLessThanLastMonth = true,
    todayExpense = 840,
    todayMessage = "You're doing okay today.",
    transactions = listOf(
        Transaction(
            id = "txn_1",
            title = "Lunch",
            category = "Food",
            date = "Today",
            amount = 420,
            iconRes = R.drawable.ic_food
        ),
        Transaction(
            id = "txn_2",
            title = "Auto",
            category = "Transport",
            date = "Today",
            amount = 120,
            iconRes = R.drawable.ic_transport
        ),
        Transaction(
            id = "txn_3",
            title = "Grocery",
            category = "Shopping",
            date = "Yesterday",
            amount = 1240,
            iconRes = R.drawable.ic_shopping
        )
    ),
    insightMessage = "You've spent less on dining this week."
)