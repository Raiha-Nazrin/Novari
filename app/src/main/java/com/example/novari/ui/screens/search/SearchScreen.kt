package com.example.novari.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.novari.R
import com.example.novari.core.model.SearchField
import com.example.novari.ui.components.AmountRangeBottomSheet
import com.example.novari.ui.components.CalendarBottomSheet
import com.example.novari.ui.components.CategoryBottomSheetContent
import com.example.novari.ui.components.ScreenHeader
import com.example.novari.ui.components.TransactionRowItem
import com.example.novari.ui.model.Transaction
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariColors.DarkTeal
import com.example.novari.ui.theme.NovariShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onTransactionClick: (String) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var showAmountSheet by rememberSaveable { mutableStateOf(false) }
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    var pendingCategorySelection by rememberSaveable { mutableStateOf(uiState.selectedCategoryIds) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun submitSearch() {
        viewModel.onSubmitSearch()
        focusManager.clearFocus()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NovariColors.Background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                SearchHeader(
                    onBackClick = onBackClick,
                    onNotificationClick = onNotificationClick
                )
            }

            item {
                Spacer(modifier = Modifier.height(25.dp))

                SearchInput(
                    value = uiState.query,
                    searchField = uiState.searchField,
                    onValueChange = viewModel::onQueryChange,
                    onSearch = { submitSearch() },
                    onClear = viewModel::clearQuery
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.search_by),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(22.dp))

                SearchFilterGrid(
                    selectedField = uiState.searchField,
                    onMerchantClick = {
                        // TODO(merchant): open a merchant picker sheet here once merchant is a
                        // real entity (see MerchantCategoryRuleEntity for the normalization
                        // pattern it'll likely follow). Until then this only scopes the typed
                        // query to the free-text `merchant` column already on TransactionEntity.
                        viewModel.onSearchFieldSelected(SearchField.MERCHANT)
                    },
                    onCategoryClick = {
                        viewModel.onSearchFieldSelected(SearchField.CATEGORY)
                        pendingCategorySelection = uiState.selectedCategoryIds
                        showCategorySheet = true
                    },
                    onAmountClick = {
                        viewModel.onSearchFieldSelected(SearchField.AMOUNT)
                        showAmountSheet = true
                    },
                    onDateClick = {
                        viewModel.onSearchFieldSelected(SearchField.DATE)
                        showCalendar = true
                    }
                )
            }

            when {
                uiState.isLoading -> item {
                    Spacer(modifier = Modifier.height(48.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NovariColors.Teal)
                    }
                }

                uiState.errorMessage != null -> item {
                    Spacer(modifier = Modifier.height(48.dp))
                    SearchErrorMessage(
                        message = uiState.errorMessage.orEmpty(),
                        onRetry = viewModel::retry
                    )
                }

                uiState.hasSearched -> {
                    item {
                        Spacer(modifier = Modifier.height(48.dp))

                        Text(
                            text = stringResource(R.string.search_results),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (uiState.isEmptyResult) {
                        item {
                            Text(
                                text = stringResource(R.string.no_transactions_match_search),
                                style = MaterialTheme.typography.bodyMedium,
                                color = NovariColors.Slate,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                            )
                        }
                    } else {
                        items(items = uiState.results, key = { it.id }) { transaction ->
                            SearchResultRow(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction.id) }
                            )
                        }
                    }
                }

                uiState.showRecentSearches -> {
                    item {
                        Spacer(modifier = Modifier.height(48.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.recent_searches),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            TextButton(onClick = viewModel::onClearRecentSearches) {
                                Text(
                                    text = stringResource(R.string.clear_all),
                                    color = NovariColors.Slate
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(items = uiState.recentSearches, key = { it }) { search ->
                        RecentSearchItem(
                            search = search,
                            onClick = { viewModel.onRecentSearchClick(search) },
                            onRemove = { viewModel.onRemoveRecentSearch(search) }
                        )
                    }
                }
            }
        }
    }

    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = sheetState,
            containerColor = NovariColors.Background,
            dragHandle = { BottomSheetDefaults.DragHandle(color = NovariColors.Border) }
        ) {
            CategoryBottomSheetContent(
                categories = uiState.categories,
                selectedCategoryIds = pendingCategorySelection,
                onSelectionChanged = { pendingCategorySelection = it },
                onAddCategory = { name -> viewModel.addCategory(name) },
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

    if (showAmountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAmountSheet = false },
            sheetState = sheetState,
            containerColor = NovariColors.Background,
            dragHandle = { BottomSheetDefaults.DragHandle(color = NovariColors.Border) }
        ) {
            AmountRangeBottomSheet(
                initialRange = uiState.amountRange,
                onApply = { range ->
                    showAmountSheet = false
                    viewModel.setAmountRange(range)
                },
                onClear = {
                    showAmountSheet = false
                    viewModel.setAmountRange(AmountRange())
                },
                onDismiss = { showAmountSheet = false }
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
            onDismiss = { showCalendar = false }
        )
    }
}

@Composable
fun SearchHeader(
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    ScreenHeader(
        title = stringResource(R.string.search),
        subtitle = stringResource(R.string.find_a_transaction_in_seconds)
    )
}

private fun placeholderFor(searchField: SearchField): Int = when (searchField) {
    SearchField.MERCHANT -> R.string.search_placeholder_merchant
    SearchField.CATEGORY -> R.string.search_placeholder_category
    SearchField.AMOUNT -> R.string.search_placeholder_amount
    SearchField.DATE -> R.string.search_placeholder_date
}

@Composable
private fun SearchInput(
    value: String,
    searchField: SearchField,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    val shape = NovariShape.chip

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(shape)
            .background(NovariColors.Surface)
            .border(
                width = 1.dp,
                color = NovariColors.Border,
                shape = shape
            )
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = NovariColors.DarkTeal,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.size(24.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = NovariColors.Navy
            ),
            cursorBrush = SolidColor(NovariColors.Teal),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (searchField == SearchField.AMOUNT) KeyboardType.Number else KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch() }
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(placeholderFor(searchField)),
                        style = MaterialTheme.typography.bodyLarge,
                        color = NovariColors.Slate
                    )
                }

                innerTextField()
            }
        )

        if (value.isNotEmpty()) {
            Spacer(modifier = Modifier.size(12.dp))

            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = NovariColors.Mint,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.clear_search),
                    tint = NovariColors.Navy,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchFilterGrid(
    selectedField: SearchField,
    onMerchantClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onAmountClick: () -> Unit,
    onDateClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            SearchFilterCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_merchant),
                title = stringResource(R.string.merchant),
                selected = selectedField == SearchField.MERCHANT,
                onClick = onMerchantClick
            )

            SearchFilterCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_category),
                title = stringResource(R.string.category),
                selected = selectedField == SearchField.CATEGORY,
                onClick = onCategoryClick
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SearchFilterCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_amount),
                title = stringResource(R.string.amount),
                selected = selectedField == SearchField.AMOUNT,
                onClick = onAmountClick
            )

            SearchFilterCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_date),
                title = stringResource(R.string.date),
                selected = selectedField == SearchField.DATE,
                onClick = onDateClick
            )
        }
    }
}

@Composable
private fun SearchFilterCard(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = NovariShape.chip

    Row(
        modifier = modifier
            .height(60.dp)
            .clip(shape)
            .background(if (selected) NovariColors.Mint else NovariColors.Surface)
            .border(
                width = 1.dp,
                color = if (selected) NovariColors.Teal else NovariColors.Border,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = DarkTeal
        )

        Spacer(modifier = Modifier.size(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SearchResultRow(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NovariColors.Surface)
            .border(1.dp, NovariColors.Border, RoundedCornerShape(20.dp))
            .padding(horizontal = 4.dp)
    ) {
        TransactionRowItem(
            transaction = transaction,
            isSelected = false,
            onClick = onClick
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun SearchErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = NovariColors.Slate
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(R.string.retry),
                color = NovariColors.Teal
            )
        }
    }
}

@Composable
private fun RecentSearchItem(
    search: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = NovariColors.Slate,
                modifier = Modifier.size(25.dp)
            )

            Spacer(modifier = Modifier.size(26.dp))

            Text(
                text = search,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Remove $search",
                    tint = NovariColors.Slate,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        HorizontalDivider(
            color = NovariColors.Divider,
            thickness = 1.dp
        )
    }
}
