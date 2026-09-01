package com.example.novari.ui.screens.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.ui.theme.NovariColors
import kotlin.collections.filter

/**
 * Merchant picker, reached from Add Expense's Merchant row.
 *
 * Both [recentMerchants] and [allMerchants] default to local dummy data
 * ([mockRecentMerchants] / [mockAllMerchants]) but are accepted as
 * parameters so a real data source can be substituted later without
 * touching anything below — this composable itself has no data-fetching
 * logic. Search filtering is client-side over whatever lists it's given,
 * so it keeps working unchanged once those defaults are replaced.
 */
@Composable
fun MerchantScreen(
    onClose: () -> Unit = {},
    onMerchantSelected: (Merchant) -> Unit = {},
    recentMerchants: List<Merchant> = mockRecentMerchants,
    allMerchants: List<Merchant> = mockAllMerchants
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredRecent = remember(searchQuery, recentMerchants) {
        recentMerchants.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    val filteredAll = remember(searchQuery, allMerchants) {
        allMerchants.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()

            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        MerchantTopBar(onClose = onClose)

        MerchantSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredRecent.isNotEmpty()) {
                item { SectionHeader(title = "Recent") }
                item {
                    MerchantGroup(
                        merchants = filteredRecent,
                        onMerchantSelected = onMerchantSelected
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            if (filteredAll.isNotEmpty()) {
                item { SectionHeader(title = "All merchants") }
                item {
                    MerchantGroup(
                        merchants = filteredAll,
                        onMerchantSelected = onMerchantSelected
                    )
                }
            }

            if (filteredRecent.isEmpty() && filteredAll.isEmpty()) {
                item { EmptySearchState() }
            }
        }
    }
}

/**
 * Centered "Merchant" title with a close (X) button pinned to the end.
 */
@Composable
private fun MerchantTopBar(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Merchant",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Close",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Rounded, bordered search field. Uses BasicTextField + a manual
 * placeholder rather than TextField/OutlinedTextField so its visual style
 * matches this screen's cards exactly instead of Material's default
 * text-field chrome.
 */
@Composable
private fun MerchantSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = NovariColors.Navy.copy(alpha = 0.06f),
                ambientColor = NovariColors.Navy.copy(alpha = 0.04f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            // Reuses the same search icon as the bottom nav's Search tab.
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Search merchant...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

/**
 * One rounded, bordered container holding a group of merchant rows with
 * dividers between them — same visual pattern as Add Expense's details card.
 */
@Composable
private fun MerchantGroup(
    merchants: List<Merchant>,
    onMerchantSelected: (Merchant) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, NovariColors.Border.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
    ) {
        merchants.forEachIndexed { index, merchant ->
            MerchantRow(
                merchant = merchant,
                onClick = { onMerchantSelected(merchant) }
            )
            if (index != merchants.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                    thickness = 0.75.dp,
                    color = NovariColors.Divider
                )
            }
        }
    }
}

@Composable
private fun MerchantRow(
    merchant: Merchant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MerchantAvatar(merchant = merchant)

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = merchant.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Icon(
            // TODO: this project currently has both ic_chevron_right
            // (used in HomeScreen.kt) and ic_arrow_right (used in
            // AddExpenseScreen.kt) referring to the same chevron shape —
            // this reuses ic_arrow_right to match the Add Expense flow this
            // screen belongs to. Consolidate to one name once resolved.
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Logo placeholder: a colored circle showing the merchant's first initial.
 * Swap in [Merchant.logoRes] via painterResource once real brand logos are
 * available — this composable already supports that, it just isn't
 * populated in the dummy data yet.
 */
@Composable
private fun MerchantAvatar(
    merchant: Merchant,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(merchant.avatarColor),
        contentAlignment = Alignment.Center
    ) {
        if (merchant.logoRes != null) {
            Icon(
                painter = painterResource(id = merchant.logoRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = merchant.name.take(1).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EmptySearchState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No merchants found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}