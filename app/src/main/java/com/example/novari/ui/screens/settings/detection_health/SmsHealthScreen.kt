package com.example.novari.ui.screens.settings.detection_health

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.novari.ui.components.RefreshOnResume
import com.example.novari.ui.components.ScreenHeader
import com.example.novari.ui.components.SmsHealthBanner
import com.example.novari.ui.theme.NovariColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timestampFormatter = DateTimeFormatter.ofPattern("d MMM, h:mm a")

/**
 * Turns "why is my transaction missing?" into something the user can answer themselves --
 * plan Phase 6.2. Shows live permission status, when auto-tracking last ran, and how many
 * messages fell into each outcome bucket.
 */
@Composable
fun SmsHealthScreen(
    onBackClick: () -> Unit,
    viewModel: SmsHealthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RefreshOnResume { viewModel.refreshSmsPermissionState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovariColors.Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp, bottom = 32.dp)
    ) {
        ScreenHeader(
            onBackClick = onBackClick,
            title = "Detection health",
            subtitle = "How Novari is tracking your SMS transactions"
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (state.isDegraded) {
            SmsHealthBanner(state = state, onClick = {})
            Spacer(modifier = Modifier.height(16.dp))
        }

        SectionCard {
            StatusRow(label = "Read SMS permission", isOk = state.canReadSms)
            RowDivider()
            StatusRow(label = "Receive SMS permission", isOk = state.canReceiveSms)
            RowDivider()
            InfoRow(label = "Last successful check", value = state.lastSuccessfulSweepAt.toDisplayLabel())
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Messages processed",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        SectionCard {
            InfoRow(label = "Booked as transactions", value = state.processedCount.toString())
            RowDivider()
            InfoRow(label = "Ignored (not financial)", value = state.ignoredCount.toString())
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = NovariColors.Surface,
        border = BorderStroke(1.dp, NovariColors.Border)
    ) {
        Column(content = content)
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = NovariColors.Border)
}

@Composable
private fun StatusRow(label: String, isOk: Boolean) {
    InfoRow(
        label = label,
        value = if (isOk) "Granted" else "Not granted",
        valueColor = if (isOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = NovariColors.Navy
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = NovariColors.Slate)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}

private fun Long?.toDisplayLabel(): String =
    this?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(timestampFormatter)
    } ?: "Never"
