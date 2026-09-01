package com.example.novari.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.novari.sms.health.SmsDetectionHealthState
import com.example.novari.ui.theme.NovariShape

/**
 * Persistent warning that SMS auto-tracking is silently not working -- permission revoked or
 * no successful sweep in the last 24h. Only rendered when [state].isDegraded; the caller
 * decides visibility so this stays a dumb display component.
 */
@Composable
fun SmsHealthBanner(state: SmsDetectionHealthState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(NovariShape.card)
            .clickable(onClick = onClick),
        shape = NovariShape.card,
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = state.degradedReason(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun SmsDetectionHealthState.degradedReason(): String = when {
    !canReadSms || !canReceiveSms -> "SMS permission was turned off. Auto-tracking has stopped."
    else -> "Auto-tracking hasn't run in a while. Your history may be missing transactions."
}
