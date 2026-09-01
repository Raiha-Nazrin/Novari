package com.example.novari.ui.screens.home

import com.example.novari.sms.health.SmsDetectionHealthState
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
    val monthlyExpense: Long = 0L,
    val monthlyPercentageChange: Int = 0,
    val isLessThanLastMonth: Boolean = true,
    val todayExpense: Long = 0L,
    val todayMessage: String = "",
    val transactions: List<Transaction> = emptyList(),
    val insightMessage: String? = null,
    val autoTrackingPrompt: AutoTrackingPromptVisibility = AutoTrackingPromptVisibility.Loading,
    val smsHealth: SmsDetectionHealthState = SmsDetectionHealthState()
)
