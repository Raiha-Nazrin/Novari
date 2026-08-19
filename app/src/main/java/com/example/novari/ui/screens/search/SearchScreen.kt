package com.example.novari.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import com.example.novari.R
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariColors.DarkTeal
import com.example.novari.ui.theme.NovariTypography

@Composable
fun SearchScreen(
    onBackClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onMerchantClick: () -> Unit = {},
    onCategoryClick: () -> Unit = {},
    onAmountClick: () -> Unit = {},
    onDateClick: () -> Unit = {}
) {
    var query by remember {
        mutableStateOf("")
    }

    val recentSearches = remember {
        mutableStateListOf(
            "Swiggy",
            "Grocery",
            "Amazon",
            "Uber",
            "Zomato"
        )
    }

    val focusManager = LocalFocusManager.current

    fun performSearch(value: String) {
        val searchText = value.trim()

        if (searchText.isBlank()) {
            return
        }

        recentSearches.remove(searchText)
        recentSearches.add(0, searchText)

        if (recentSearches.size > 5) {
            recentSearches.removeAt(recentSearches.lastIndex)
        }

        query = searchText
        focusManager.clearFocus()
        onSearch(searchText)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NovariColors.Background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 32.dp),
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
                    value = query,
                    onValueChange = {
                        query = it
                    },
                    onSearch = {
                        performSearch(query)
                    },
                    onClear = {
                        query = ""
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.search_by),
                    style = NovariTypography.titleMedium,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(22.dp))

                SearchFilterGrid(
                    onMerchantClick = onMerchantClick,
                    onCategoryClick = onCategoryClick,
                    onAmountClick = onAmountClick,
                    onDateClick = onDateClick
                )
            }

            if (recentSearches.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = stringResource(R.string.recent_searches),
                        style = NovariTypography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(
                    items = recentSearches,
                    key = { it }
                ) { search ->
                    RecentSearchItem(
                        search = search,
                        onClick = {
                            query = search
                            performSearch(search)
                        },
                        onRemove = {
                            recentSearches.remove(search)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchHeader(
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.search),
                style = NovariTypography.headlineMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.find_a_transaction_in_seconds),
                style = MaterialTheme.typography.bodyMedium,
                color = NovariColors.Slate,
            )
        }

    }
}

@Composable
private fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    val shape = RoundedCornerShape(13.dp)

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
            textStyle = NovariTypography.bodyLarge.copy(
                color = NovariColors.Navy
            ),
            cursorBrush = SolidColor(NovariColors.Teal),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch()
                }
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_transactions),
                        style = NovariTypography.bodyLarge,
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
                        shape = androidx.compose.foundation.shape.CircleShape
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
                onClick = onMerchantClick
            )

            SearchFilterCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_category),
                title = stringResource(R.string.category),
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
                onClick = onAmountClick
            )

            SearchFilterCard(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_date),
                title = stringResource(R.string.date),
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
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .height(60.dp)
            .clip(shape)
            .background(NovariColors.Surface)
            .border(
                width = 1.dp,
                color = NovariColors.Border,
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
            style = NovariTypography.titleSmall,
            fontSize = 14.sp
        )
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
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.size(26.dp))

            Text(
                text = search,
                modifier = Modifier.weight(1f),
                style = NovariTypography.titleSmall
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Remove $search",
                    tint = NovariColors.Slate,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        androidx.compose.material3.HorizontalDivider(
            color = NovariColors.Divider,
            thickness = 1.dp
        )
    }
}