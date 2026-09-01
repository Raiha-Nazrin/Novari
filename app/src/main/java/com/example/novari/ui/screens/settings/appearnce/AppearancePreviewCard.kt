package com.example.novari.ui.screens.settings.appearnce

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.ui.motion.LocalReducedMotion
import com.example.novari.ui.theme.AppearanceSettings
import com.example.novari.ui.theme.LocalNovariColors
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariShape
import com.example.novari.ui.theme.NovariSpacing
import com.example.novari.ui.theme.ThemeMode
import com.example.novari.ui.theme.darkScheme
import com.example.novari.ui.theme.lightScheme
import com.example.novari.ui.theme.toMaterialScheme

@Composable
fun AppearancePreviewCard(draft: AppearanceSettings) {
    val reducedMotion = LocalReducedMotion.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = NovariColors.Surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = NovariColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.preview),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Crossfade(
                targetState = draft,
                animationSpec = if (reducedMotion) tween(0) else tween(220),
                label = "appearance-preview"
            ) { settings ->
                PreviewContent(settings)
            }
        }
    }
}

@Composable
private fun PreviewContent(settings: AppearanceSettings) {
    val isDark = settings.theme == ThemeMode.DARK
    val previewScheme = if (isDark) darkScheme(settings.accent) else lightScheme(settings.accent)

    CompositionLocalProvider(LocalNovariColors provides previewScheme) {
        MaterialTheme(
            colorScheme = previewScheme.toMaterialScheme(isDark),
            typography = MaterialTheme.typography
        ) {
            val previewDescription = stringResource(R.string.preview_content_description)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NovariShape.card)
                    .clearAndSetSemantics {
                        contentDescription = previewDescription
                    },
                shape = NovariShape.card,
                color = NovariColors.Background
            ) {
                Column(
                    modifier = Modifier.padding(NovariSpacing.lg)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.preview_greeting),
                            style = MaterialTheme.typography.titleSmall
                        )

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(color = NovariColors.Teal, shape = CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(NovariSpacing.md))

                    Text(
                        text = stringResource(R.string.preview_amount),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = stringResource(R.string.preview_period),
                        style = MaterialTheme.typography.bodySmall,
                        color = NovariColors.Slate
                    )

                    Spacer(modifier = Modifier.height(NovariSpacing.lg))

                    LinearProgressIndicator(
                        progress = { 0.68f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(NovariShape.pill),
                        color = NovariColors.Teal,
                        trackColor = NovariColors.PaleTeal
                    )

                    Spacer(modifier = Modifier.height(NovariSpacing.xs))

                    Text(
                        text = stringResource(R.string.preview_budget_progress),
                        style = MaterialTheme.typography.bodySmall,
                        color = NovariColors.Slate
                    )

                    Spacer(modifier = Modifier.height(NovariSpacing.lg))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = NovariShape.chip,
                        color = NovariColors.SurfaceHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(NovariSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color = NovariColors.Teal, shape = CircleShape)
                                )

                                Spacer(modifier = Modifier.width(NovariSpacing.sm))

                                Text(
                                    text = stringResource(R.string.preview_category),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Text(
                                text = stringResource(R.string.preview_amount_spent),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(NovariSpacing.lg))

                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NovariColors.Teal,
                            contentColor = NovariColors.Surface,
                            disabledContainerColor = NovariColors.Teal,
                            disabledContentColor = NovariColors.Surface
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.preview_cta),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
