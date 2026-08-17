package com.example.novari.ui.screens.insights

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.data.Category
import com.example.novari.data.categories
import com.example.novari.ui.components.NovariCard
import com.example.novari.ui.motion.StaggeredEntry
import com.example.novari.ui.screens.insights.charts.SpendingChart
import com.example.novari.ui.screens.insights.charts.WeeklyChart
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariShape
import com.example.novari.ui.theme.NovariSpacing
import java.text.NumberFormat
import java.util.Locale

private val AdaptiveTwoColumnThreshold = 600.dp

private val IndianLocale: Locale = Locale.Builder().setLanguage("en").setRegion("IN").build()

private fun formatRupees(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(IndianLocale)
    return "₹" + formatter.format(amount)
}

@Composable
fun InsightsScreen(
    modifier: Modifier = Modifier,
    onMonthClick: () -> Unit = {},
    onRangeClick: () -> Unit = {},
    onViewAllCategories: () -> Unit = {},
    onCategoryClick: (Category) -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = NovariSpacing.gutter,
                end = NovariSpacing.gutter,
                top = systemBarsPadding.calculateTopPadding() + NovariSpacing.lg,
                bottom = systemBarsPadding.calculateBottomPadding() + NovariSpacing.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(NovariSpacing.xl)
        ) {

            item(key = "header") {
                StaggeredEntry(index = 0, visible = true) {
                    InsightsHeader()
                }
            }

            item(key = "month_selector") {
                StaggeredEntry(index = 1, visible = true) {
                    MonthSelector(
                        onMonthClick = onMonthClick,
                        onRangeClick = onRangeClick
                    )
                }
            }

            item(key = "monthly_spending") {
                StaggeredEntry(index = 2, visible = true) {
                    MonthlySpendingCard()
                }
            }

            item(key = "categories") {
                StaggeredEntry(index = 3, visible = true) {
                    CategoriesCard(
                        onViewAllCategories = onViewAllCategories,
                        onCategoryClick = onCategoryClick
                    )
                }
            }

            item(key = "bottom_insights") {
                StaggeredEntry(index = 4, visible = true) {
                    BottomInsightsCards()
                }
            }
        }
    }
}

@Composable
private fun InsightsHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = stringResource(R.string.insights_title),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(NovariSpacing.sm))

        Text(
            text = stringResource(R.string.insights_subtitle),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MonthSelector(
    onMonthClick: () -> Unit,
    onRangeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "August 2026",
            style = MaterialTheme.typography.labelLarge,
            color = NovariColors.Teal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onMonthClick)
        )

        Spacer(modifier = Modifier.width(NovariSpacing.md))

        Surface(
            onClick = onRangeClick,
            shape = NovariShape.pill,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, NovariColors.Border),
            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = NovariSpacing.lg, vertical = NovariSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.insights_this_month_range),
                    style = MaterialTheme.typography.labelMedium,
                    color = NovariColors.Navy
                )

                Spacer(modifier = Modifier.width(NovariSpacing.sm))

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = stringResource(R.string.insights_change_range),
                    tint = NovariColors.Slate,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MonthlySpendingCard() {

    NovariCard {

        Text(
            text = stringResource(R.string.insights_this_month_label),
            style = MaterialTheme.typography.labelSmall,
            color = NovariColors.Muted
        )

        Spacer(modifier = Modifier.height(NovariSpacing.sm))

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = "${formatRupees(24560)} spent"
            }
        ) {
            Text(
                text = formatRupees(24560),
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(NovariSpacing.sm))

            Text(
                text = stringResource(R.string.insights_spent),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(NovariSpacing.md))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = NovariColors.Teal,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(NovariSpacing.xs))

            Text(
                text = stringResource(R.string.insights_less_than_last_month, 8),
                style = MaterialTheme.typography.labelMedium,
                color = NovariColors.Teal
            )
        }

        Spacer(modifier = Modifier.height(NovariSpacing.lg))

        SpendingChart(
            points = listOf(0.20f, 0.36f, 0.33f, 0.52f, 0.43f, 0.48f, 0.72f, 0.92f),
            selectedIndex = 5,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )
    }
}

@Composable
private fun CategoriesCard(
    onViewAllCategories: () -> Unit,
    onCategoryClick: (Category) -> Unit
) {

    NovariCard {

        Text(
            text = stringResource(R.string.insights_where_money_went),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(NovariSpacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onViewAllCategories) {
                Text(
                    text = stringResource(R.string.insights_view_all_categories),
                    style = MaterialTheme.typography.labelMedium,
                    color = NovariColors.Teal
                )

                Spacer(modifier = Modifier.width(NovariSpacing.xs))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = NovariColors.Teal,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(NovariSpacing.lg))

        Column(verticalArrangement = Arrangement.spacedBy(NovariSpacing.lg)) {
            categories.forEach { category ->
                CategoryRow(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(category.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(NovariSpacing.md))

        Column(modifier = Modifier.weight(1f)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = NovariColors.Navy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(NovariSpacing.md))

                Text(
                    text = formatRupees(category.amount),
                    style = MaterialTheme.typography.labelLarge,
                    color = NovariColors.Navy
                )

                Spacer(modifier = Modifier.width(NovariSpacing.md))

                Text(
                    text = stringResource(R.string.insights_percent_format, category.percentage),
                    style = MaterialTheme.typography.labelMedium,
                    color = NovariColors.Slate,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(NovariSpacing.sm))

            SpendingProgress(percentage = category.percentage, color = category.color)
        }
    }
}

@Composable
private fun SpendingProgress(
    percentage: Int,
    color: Color
) {
    val progress by animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        label = "categoryProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(NovariShape.pill)
            .background(NovariColors.Divider)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(6.dp)
                .clip(NovariShape.pill)
                .background(color)
        )
    }
}

@Composable
private fun BottomInsightsCards() {

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= AdaptiveTwoColumnThreshold) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NovariSpacing.md)
            ) {
                WeeklyRhythmCard(modifier = Modifier.weight(1f))
                WhatChangedCard(modifier = Modifier.weight(1f))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(NovariSpacing.md)
            ) {
                WeeklyRhythmCard(modifier = Modifier.fillMaxWidth())
                WhatChangedCard(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun WeeklyRhythmCard(
    modifier: Modifier = Modifier
) {

    NovariCard(modifier = modifier) {

        Text(
            text = stringResource(R.string.insights_weekly_rhythm),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(NovariSpacing.lg))

        val dayLabels = listOf(
            stringResource(R.string.insights_day_mon),
            stringResource(R.string.insights_day_tue),
            stringResource(R.string.insights_day_wed),
            stringResource(R.string.insights_day_thu),
            stringResource(R.string.insights_day_fri),
            stringResource(R.string.insights_day_sat),
            stringResource(R.string.insights_day_sun)
        )

        WeeklyChart(
            values = listOf(.40f, .62f, .58f, .76f, .72f, .34f, .55f),
            labels = dayLabels,
            labelStyle = MaterialTheme.typography.labelSmall.copy(color = NovariColors.Muted),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(NovariSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NovariSpacing.md)
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatRupees(2840),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(R.string.insights_this_week),
                    style = MaterialTheme.typography.labelMedium,
                    color = NovariColors.Slate
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = NovariColors.Teal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(NovariSpacing.xs))
                    Text(
                        text = stringResource(R.string.insights_percent_format, 12),
                        style = MaterialTheme.typography.labelLarge,
                        color = NovariColors.Teal
                    )
                }
                Text(
                    text = stringResource(R.string.insights_less_than_last_week),
                    style = MaterialTheme.typography.labelSmall,
                    color = NovariColors.Muted,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun WhatChangedCard(
    modifier: Modifier = Modifier
) {

    NovariCard(modifier = modifier) {

        Text(
            text = stringResource(R.string.insights_what_changed),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(NovariSpacing.md))

        Column(verticalArrangement = Arrangement.spacedBy(NovariSpacing.sm)) {

            ChangeItem(
                icon = Icons.Default.Coffee,
                title = stringResource(R.string.insights_dining),
                changePercent = -18,
                description = stringResource(R.string.insights_less_than_last_month_short),
                color = NovariColors.Food,
                backgroundColor = NovariColors.FoodBackground
            )

            ChangeItem(
                icon = Icons.Default.ShoppingBag,
                title = stringResource(R.string.insights_shopping),
                changePercent = 11,
                description = stringResource(R.string.insights_more_than_last_month_short),
                color = NovariColors.Shopping,
                backgroundColor = NovariColors.ShoppingBackground
            )

            ChangeItem(
                icon = Icons.Default.DirectionsCar,
                title = stringResource(R.string.insights_transport),
                changePercent = 0,
                description = stringResource(R.string.insights_about_the_same),
                color = NovariColors.Transport,
                backgroundColor = NovariColors.TransportBackground
            )
        }
    }
}

@Composable
private fun ChangeItem(
    icon: ImageVector,
    title: String,
    changePercent: Int,
    description: String,
    color: Color,
    backgroundColor: Color
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NovariShape.chip)
            .background(NovariColors.PaleTeal)
            .padding(NovariSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(NovariSpacing.sm))

        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = NovariColors.Navy,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(NovariSpacing.xs))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val trendColor = when {
                changePercent > 0 -> NovariColors.Error
                changePercent < 0 -> NovariColors.Teal
                else -> NovariColors.Slate
            }
            val trendIcon = when {
                changePercent > 0 -> Icons.AutoMirrored.Filled.TrendingUp
                changePercent < 0 -> Icons.AutoMirrored.Filled.TrendingDown
                else -> null
            }

            trendIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(NovariSpacing.xs))
            }

            Text(
                text = stringResource(R.string.insights_percent_format, kotlin.math.abs(changePercent)),
                style = MaterialTheme.typography.labelLarge,
                color = trendColor
            )
        }

        Spacer(modifier = Modifier.width(NovariSpacing.sm))

        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = NovariColors.Muted,
            maxLines = 2
        )
    }
}
