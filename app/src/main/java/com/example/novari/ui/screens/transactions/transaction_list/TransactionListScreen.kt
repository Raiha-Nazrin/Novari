package com.example.novari.ui.screens.transactions.transaction_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.novari.R
import com.example.novari.ui.components.CalendarBottomSheet
import com.example.novari.ui.components.CategoryBottomSheetContent
import com.example.novari.ui.components.ScreenHeader
import com.example.novari.ui.components.SearchFieldComponent
import com.example.novari.ui.components.TransactionRowItem
import com.example.novari.ui.components.formatTransactionAmount
import com.example.novari.ui.model.Transaction
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onBackClick: () -> Unit,
    onTransactionClick: (String) -> Unit = {},
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val selectedFilter = if (uiState.quickFilter == QuickFilter.THIS_MONTH) "This month" else "All"

    var showCategorySheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showCalendar by rememberSaveable {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var pendingCategorySelection by rememberSaveable {
        mutableStateOf(uiState.selectedCategories)
    }

    var collapsedDayGroups by rememberSaveable {
        mutableStateOf(emptySet<Long>())
    }

    var selectedTransactionId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NovariColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(28.dp))

                ScreenHeader(
                    onBackClick = { onBackClick() },
                    title = stringResource(R.string.transaction_history),
                    subtitle = stringResource(R.string.all_your_spending_organized_by_day)
                )

                Spacer(modifier = Modifier.height(25.dp))

                SearchFieldComponent(
                    value = uiState.query,
                    onValueChange = { viewModel.onQueryChange(it) },
                    onSearch = { },
                    onClear = { viewModel.onQueryChange("") }
                )

                Spacer(modifier = Modifier.height(20.dp))
                NovariFilterRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { filter ->
                        viewModel.onQuickFilterSelected(
                            if (filter == "This month") QuickFilter.THIS_MONTH else QuickFilter.ALL
                        )
                    },
                    onCategoryClick = {
                        pendingCategorySelection = uiState.selectedCategories
                        showCategorySheet = true
                    },
                    onDateClick = {
                        showCalendar = true
                    },
                    dateFilterLabel = uiState.dateFilterLabel,
                    dateFilterActive = uiState.dateFilter != DateFilter.None
                )

                Spacer(modifier = Modifier.height(24.dp))
                MonthSummaryRow(
                    monthLabel = uiState.rangeLabel,
                    transactionCount = uiState.transactionCount
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
            if (uiState.isEmpty) {
                item {
                    EmptyTransactionsMessage(hasActiveFilters = uiState.hasActiveFilters)
                }
            }

            uiState.dayGroups.forEach { dayGroup ->
                val isCollapsed = dayGroup.dateMillis in collapsedDayGroups

                item(key = "header_${dayGroup.dateMillis}") {
                    DayGroupHeader(
                        label = dayGroup.label,
                        total = dayGroup.total,
                        isCollapsed = isCollapsed,
                        onToggle = {
                            collapsedDayGroups = if (isCollapsed) {
                                collapsedDayGroups - dayGroup.dateMillis
                            } else {
                                collapsedDayGroups + dayGroup.dateMillis
                            }
                        }
                    )
                }

                item(key = "transactions_${dayGroup.dateMillis}") {
                    AnimatedVisibility(
                        visible = !isCollapsed,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        DayGroupCard(
                            transactions = dayGroup.transactions,
                            selectedTransactionId = selectedTransactionId,
                            onTransactionClick = { transaction ->
                                onTransactionClick(transaction.id)
                            },
                            onEditTransaction = { /* wire to future edit flow */ },
                            onDeleteTransaction = { /* wire to future delete flow */ }
                        )
                    }
                }

                item(key = "spacer_${dayGroup.dateMillis}") {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            }
        }
    }

    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showCategorySheet = false
            },
            sheetState = sheetState,
            containerColor = NovariColors.Background,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = NovariColors.Border
                )
            }
        ) {
            CategoryBottomSheetContent(
                categories = uiState.categories,
                selectedCategoryIds = pendingCategorySelection,

                onSelectionChanged = {
                    pendingCategorySelection = it
                },

                onAddCategory = { name ->
                    viewModel.addCategory(name)
                },

                onApply = {
                    showCategorySheet = false
                    viewModel.setCategoryFilter(pendingCategorySelection)
                },

                onDismiss = {
                    showCategorySheet = false
                    viewModel.clearAddCategoryError()
                },

                addCategoryError = uiState.addCategoryError
            )
        }
    }

    if (showCalendar) {

        CalendarBottomSheet(
            initialFilter = uiState.dateFilter,

            onApply = { filter ->
                viewModel.setDateFilter(filter)
                showCalendar = false
            },

            onClear = {
                viewModel.clearDateFilter()
                showCalendar = false
            },

            onDismiss = {
                showCalendar = false
            }
        )
    }
}

@Composable
private fun MonthSummaryRow(
    monthLabel: String,
    transactionCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.titleMedium,
                color = NovariColors.DarkTeal
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = NovariColors.DarkTeal,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = stringResource(R.string.transactions_count, transactionCount),
            style = MaterialTheme.typography.bodyMedium,
            color = NovariColors.Slate
        )
    }
}

@Composable
private fun EmptyTransactionsMessage(
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(
            if (hasActiveFilters) {
                R.string.no_transactions_match_search
            } else {
                R.string.no_transactions_this_month
            }
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = NovariColors.Slate,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    )
}

@Composable
private fun DayGroupHeader(
    label: String,
    total: Int,
    isCollapsed: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = NovariColors.Navy
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "₹${formatTransactionAmount(total.toLong())}",
                style = MaterialTheme.typography.labelMedium,
                color = NovariColors.Navy
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = NovariColors.Slate,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(if (isCollapsed) 0f else 180f)
            )
        }
    }
}

@Composable
private fun DayGroupCard(
    transactions: List<Transaction>,
    selectedTransactionId: String?,
    onTransactionClick: (Transaction) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NovariColors.Surface)
            .border(1.dp, NovariColors.Border, RoundedCornerShape(20.dp))
    ) {
        transactions.forEachIndexed { index, transaction ->
            TransactionRowItem(
                transaction = transaction,
                isSelected = transaction.id == selectedTransactionId,
                onClick = { onTransactionClick(transaction) },
            )
            if (index != transactions.lastIndex) {
                HorizontalDivider(color = NovariColors.Border)
            }
        }
    }
}

@Composable
fun NovariFilterRow(
    modifier: Modifier = Modifier,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onCategoryClick: () -> Unit,
    onDateClick: () -> Unit,
    dateFilterLabel: String? = null,
    dateFilterActive: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NovariFilterChip(
            text = "All",
            selected = selectedFilter == "All",
            onClick = {
                onFilterSelected("All")
            }
        )

        NovariFilterChip(
            text = "This month",
            selected = selectedFilter == "This month",
            onClick = {
                onFilterSelected("This month")
            }
        )

        NovariFilterChip(
            text = "Category",
            showArrow = true,
            onClick = onCategoryClick
        )

        NovariFilterChip(
            text = dateFilterLabel ?: "Date",
            selected = dateFilterActive,
            showArrow = true,
            onClick = onDateClick
        )
    }
}

@Composable
fun NovariFilterChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    showArrow: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        NovariColors.Teal
    } else {
        NovariColors.Surface
    }

    val contentColor = if (selected) {
        NovariColors.Surface
    } else {
        NovariColors.Navy
    }

    Surface(
        modifier = modifier
            .height(35.dp)
            .clip(NovariShape.pill)
            .clickable(onClick = onClick),
        shape = NovariShape.pill,
        color = backgroundColor,
        border = if (!selected) {
            BorderStroke(
                width = 1.dp,
                color = NovariColors.Border
            )
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1
            )

            if (showArrow) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
