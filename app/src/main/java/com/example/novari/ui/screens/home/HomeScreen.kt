package com.example.novari.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.novari.R
import com.example.novari.ui.screens.transactions.AddExpenseViewModel

/**
 * Home dashboard screen. UI-only: all data arrives via [uiState] and every
 * user action is surfaced through a callback, so this composable can be
 * wired to a ViewModel later without changing anything below.
 *
 * All icons throughout this file are drawable resources from the project's
 * res/drawable folder (not Material Icons) — replace the placeholder
 * R.drawable.ic_* names with your project's actual resource names.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEnableAutoTracking: () -> Unit = {},
    onAddExpenseManually: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    onEditTransaction: (Transaction) -> Unit = {},
    onDeleteTransaction: (Transaction) -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    viewModel: AddExpenseViewModel = hiltViewModel(),
) {
    var selectedTransactionId by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            .padding(top = 28.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            HomeHeader(userName = uiState.userName)
        }

        item {
            MonthlyExpenseCard(
                amount = uiState.monthlyExpense,
                percentageChange = uiState.monthlyPercentageChange,
                isLessThanLastMonth = uiState.isLessThanLastMonth
            )
        }

        item {
            TodayExpenseCard(
                amount = uiState.todayExpense,
                message = uiState.todayMessage
            )
        }

        item {
            AutoTrackingCard(
                onEnableAutoTracking = onEnableAutoTracking,
                onAddExpenseManually = onAddExpenseManually
            )
        }

        item {
            RecentTransactionsSection(
                transactions = uiState.transactions,
                selectedTransactionId = selectedTransactionId,
                onTransactionClick = { transaction ->
                    selectedTransactionId =
                        if (selectedTransactionId == transaction.id) null else transaction.id
                    onTransactionClick(transaction)
                },
                onEditTransaction = onEditTransaction,
                onDeleteTransaction = { transaction ->
                    onDeleteTransaction(transaction)
                    selectedTransactionId = null
                },
                onSeeAllTransactions = onSeeAllTransactions
            )
        }

        item {
            InsightCard(message = uiState.insightMessage)
        }
    }
}

/**
 * Greeting + notification bell shown at the top of the Home screen.
 */
@Composable
private fun HomeHeader(
    userName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {

            Text(
                text = "Good morning 👋",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        IconButton(onClick = { /*  open notifications */ }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = "Notifications",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * "This month" expense summary card. Intentionally has no chart —
 * amount + comparison text only, per requirements.
 */
@Composable
private fun MonthlyExpenseCard(
    amount: Int,
    percentageChange: Int,
    isLessThanLastMonth: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "This month",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "₹${formatAmount(amount)}",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "spent",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        /*Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(
                    id = if (isLessThanLastMonth) R.drawable.ic_trending_down else R.drawable.ic_trending_up
                ),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$percentageChange% ${if (isLessThanLastMonth) "less" else "more"} than last month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }*/
    }
}

/**
 * "Today" expense card with a calendar icon, amount, and short message.
 */
@Composable
private fun TodayExpenseCard(
    amount: Int,
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_calender_new),
                contentDescription = null,
                modifier = Modifier.size(35.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "₹${formatAmount(amount)}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "spent",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Prompt card for enabling automatic expense tracking.
 * UI + callbacks only — no SMS reading/permissions/parsing here.
 */
@Composable
private fun AutoTrackingCard(
    onEnableAutoTracking: () -> Unit,
    onAddExpenseManually: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_shield_auto_track),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Let Novari do the heavy lifting.",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.enable_automatic_tracking_via_transaction),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onEnableAutoTracking,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = stringResource(R.string.enable_auto_tracking), color = Color.White)
        }

        TextButton(
            onClick = onAddExpenseManually,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.add_expense_manually))
        }
    }
}

/**
 * "Recent transactions" header + list. Selecting a row reveals Edit/Delete
 * for that row only (single expanded item at a time), matching the
 * reference's swipe/selected-state interaction without full swipe gestures.
 */
@Composable
private fun RecentTransactionsSection(
    transactions: List<Transaction>,
    selectedTransactionId: String?,
    onTransactionClick: (Transaction) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onSeeAllTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent transactions",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onSeeAllTransactions) {
                Text(text = "See all →", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
        ) {
            transactions.forEachIndexed { index, transaction ->
                TransactionItem(
                    transaction = transaction,
                    isSelected = transaction.id == selectedTransactionId,
                    onClick = { onTransactionClick(transaction) },
                    onEdit = { onEditTransaction(transaction) },
                    onDelete = { onDeleteTransaction(transaction) }
                )
                if (index != transactions.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = transaction.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${transaction.category} · ${transaction.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "₹${formatAmount(transaction.amount)}",
                style = MaterialTheme.typography.labelLarge
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp).padding(start = 4.dp)
            )
        }

        AnimatedVisibility(visible = isSelected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Edit")
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Delete")
                }
            }
        }
    }
}

/**
 * "A little insight" card at the bottom of Home.
 */
@Composable
private fun InsightCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_insight_plant),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.a_little_insight),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatAmount(amount: Int): String {
    // Simple thousands-separator formatting (e.g. 24560 -> 24,560).
    return "%,d".format(amount)
}