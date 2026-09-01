package com.example.novari.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.core.model.TransactionType
import com.example.novari.ui.model.Transaction
import com.example.novari.ui.theme.NovariColors

/**
 * Row for a single [Transaction] — icon, title, category/date, signed amount.
 * Shared by HomeScreen's "Recent transactions" and TransactionListScreen so
 * both render the same design off the same data shape.
 *
 * Tapping the row toggles [isSelected] (owned by the caller), which reveals
 * Edit/Delete actions for that row only.
 */
@Composable
fun TransactionRowItem(
    transaction: Transaction,
    isSelected: Boolean,
    onClick: () -> Unit,
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
                    .background(NovariColors.SurfaceHigh),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = transaction.title, style = MaterialTheme.typography.bodyLarge)
                    if (transaction.isAutoDetected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        AutoDetectedBadge()
                    }
                }
                Text(
                    text = "${transaction.category} · ${transaction.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovariColors.Slate
                )
            }

            val isIncome = transaction.type == TransactionType.INCOME
            Text(
                text = "${if (isIncome) "+" else "-"}₹${formatTransactionAmount(transaction.amount.toLong())}",
                style = MaterialTheme.typography.labelLarge,
                color = if (isIncome) NovariColors.Teal else NovariColors.Error
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = NovariColors.Slate,
                modifier = Modifier
                    .size(20.dp)
                    .padding(start = 4.dp)
            )
        }
    }
}

/** Simple thousands-separator formatting (e.g. 24560 -> 24,560). */
fun formatTransactionAmount(amount: Long): String = "%,d".format(amount)

/** Tag for a [Transaction] captured automatically from an SMS -- lets the user tell what to double-check. */
@Composable
private fun AutoDetectedBadge() {
    Text(
        text = "Auto",
        style = MaterialTheme.typography.labelSmall,
        color = NovariColors.Teal,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NovariColors.PaleTeal)
            .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}
