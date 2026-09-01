package com.example.novari.ui.screens.insights

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Everything InsightsScreen needs to render, computed by [InsightsViewModel]
 * from real transaction data. Defaults render an empty-but-valid month so
 * previews and the first frame (before the DB flow emits) don't crash.
 */
data class InsightsUiState(
    val monthLabel: String = "",
    val monthlySpent: Long = 0L,
    val monthlyPercentageChange: Int = 0,
    val isLessThanLastMonth: Boolean = true,
    val spendingTrendPoints: List<Float> = emptyList(),
    val categories: List<InsightCategory> = emptyList(),
    val weeklyValues: List<Float> = List(7) { 0f },
    val weeklyTotal: Long = 0L,
    val weeklyPercentageChange: Int = 0,
    val isLessThanLastWeek: Boolean = true,
    val whatChanged: List<InsightCategoryChange> = emptyList()
)

/** One row in the "Where your money went" category breakdown. */
data class InsightCategory(
    val id: String,
    val name: String,
    val amount: Long,
    val percentage: Int,
    val icon: ImageVector,
    val color: Color,
    val backgroundColor: Color
)

/** One row in the "What changed" card: a category's spend vs the prior comparable period. */
data class InsightCategoryChange(
    val name: String,
    val changePercent: Int,
    val icon: ImageVector,
    val color: Color,
    val backgroundColor: Color
)
