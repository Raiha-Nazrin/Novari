package com.example.novari.ui.screens.addexpense

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.ui.common.formatAmount

/**
 * Static "Saving expense" screen shown (eventually) while an expense save
 * is in flight. UI only, per requirements — no state, no real progress
 * value, no navigation wiring. `amount` is a plain display string so this
 * can later be fed the actual amount being saved without any layout
 * changes; the progress ring is a fixed value, not driven by any save
 * operation yet.
 *
 * Reuses [AddExpenseTopBar] from AddExpenseScreen.kt rather than
 * duplicating the back arrow / title / subtitle.
 */
@Composable
fun SavingExpenseScreen(
    amount: Int = 420,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        AddExpenseTopBar(onBack = onBack)

        // Centers the content body vertically on the screen like the reference
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Amount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "₹${formatAmount(amount)}.00",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(48.dp))

            SavingProgressIllustration()

            Spacer(modifier = Modifier.height(44.dp))

            Text(
                text = "Saving expense...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Please wait a moment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Subtle offset to push the optical center slightly upward
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * Circular ring with a receipt icon + checkmark badge in the center,
 * matching the reference. Built from Material3's own CircularProgressIndicator
 * (reused, not hand-rolled) with a fixed progress value — this is a static
 * illustration, not an actual loading indicator, so it doesn't animate.
 */
@Composable
private fun SavingProgressIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(170.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft outer track
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(170.dp),
            strokeWidth = 3.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
            trackColor = MaterialTheme.colorScheme.surface
        )

        // Active stroke arc
        CircularProgressIndicator(
            progress = { 0.40f },
            modifier = Modifier.size(170.dp),
            strokeWidth = 4.dp,
            strokeCap = StrokeCap.Round,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        )

        // Soft circular background container
        Box(
            modifier = Modifier
                .size(116.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                // Receipt / invoice icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_invoice),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(46.dp)
                )

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_tick),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}