package com.example.novari.ui.screens.home

import com.example.novari.ui.model.Transaction

/**
 * Visibility of the "Enable Auto Tracking" prompt card on HomeScreen.
 * [Loading] renders nothing, avoiding a flash of the card before the
 * persisted "has the user visited setup" flag is read.
 */
enum class AutoTrackingPromptVisibility {
    Loading,
    Visible,
    Hidden
}

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
    val insightMessage: String = "You've spent less on dining this week.",
    val autoTrackingPrompt: AutoTrackingPromptVisibility = AutoTrackingPromptVisibility.Loading
)
