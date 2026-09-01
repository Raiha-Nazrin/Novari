package com.example.novari.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariSpacing

/**
 * Canonical screen title block: optional back button, title, optional subtitle,
 * optional trailing action (e.g. a notification bell). Used by every top-level
 * screen so title/subtitle sizing, colour, and spacing stay identical app-wide.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // Back button — only shown when provided
        onBackClick?.let { onBack ->
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = NovariColors.Navy,
                modifier = Modifier.size(25.dp).clickable{onBack()}
            )

            Spacer(modifier = Modifier.height(NovariSpacing.md))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium
                )

                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(NovariSpacing.sm))

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NovariColors.Slate
                    )
                }
            }

            trailing?.invoke()
        }
    }
}
