package com.example.novari.ui.screens.settings.appearnce

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novari.R
import com.example.novari.ui.theme.AccentColor
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariTypography
import com.example.novari.ui.theme.ThemeMode

@Composable
fun AppearanceScreen(
    selectedTheme: ThemeMode = ThemeMode.LIGHT,
    selectedAccent: AccentColor = AccentColor.TEAL,
    onThemeSelected: (ThemeMode) -> Unit = {},
    onAccentSelected: (AccentColor) -> Unit = {},
    onBack: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NovariColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(
            start = 28.dp,
            end = 28.dp,
            top = 16.dp,
            bottom = 32.dp
        )
    ) {
        item {
            AppearanceHeader(
                onBack = onBack
            )
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))

            ThemeSection(
                selectedTheme = selectedTheme,
                onThemeSelected = onThemeSelected
            )
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))

            AccentColorSection(
                selectedAccent = selectedAccent,
                onAccentSelected = onAccentSelected
            )
        }
    }
}

@Composable
private fun AppearanceHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = NovariColors.Navy
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.appearance),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.customize_how_novari_looks_for_you),
                style = NovariTypography.bodyLarge,
                color = NovariColors.Slate
            )
        }
    }
}
@Composable
private fun ThemeSection(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    AppearanceCard {
        Text(
            text = stringResource(R.string.theme),
            style = NovariTypography.headlineSmall,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ThemeOption(
                modifier = Modifier.weight(1f),
                theme = ThemeMode.LIGHT,
                title = stringResource(R.string.light),
                subtitle = stringResource(R.string.clean_and_bright),
                selected = selectedTheme == ThemeMode.LIGHT,
                onClick = {
                    onThemeSelected(ThemeMode.LIGHT)
                }
            )

            ThemeOption(
                modifier = Modifier.weight(1f),
                theme = ThemeMode.DARK,
                title = stringResource(R.string.dark),
                subtitle = stringResource(R.string.easy_on_the_eyes),
                selected = selectedTheme == ThemeMode.DARK,
                onClick = {
                    onThemeSelected(ThemeMode.DARK)
                }
            )
        }
    }
}

@Composable
private fun ThemeOption(
    modifier: Modifier = Modifier,
    theme: ThemeMode,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor =
        if (selected) {
            NovariColors.Teal
        } else {
            NovariColors.Border
        }

    Box(
        modifier = modifier
            .height(144.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(NovariColors.Surface)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(
                        color = NovariColors.PaleTeal,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (theme) {
                        ThemeMode.LIGHT -> Icons.Outlined.LightMode
                        ThemeMode.DARK -> Icons.Outlined.DarkMode
                    },
                    contentDescription = null,
                    tint = NovariColors.DarkTeal,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = NovariTypography.titleSmall
            )

            Text(
                text = subtitle,
                style = NovariTypography.bodySmall
            )
        }

        SelectionIndicator(
            selected = selected,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun SelectionIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .border(
                width = 1.dp,
                color = if (selected) {
                    NovariColors.Teal
                } else {
                    NovariColors.Border
                },
                shape = CircleShape
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = NovariColors.Teal,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun AccentColorSection(
    selectedAccent: AccentColor,
    onAccentSelected: (AccentColor) -> Unit
) {
    AppearanceCard {
        Text(
            text = stringResource(R.string.accent_color),
            style = NovariTypography.headlineSmall,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccentColor.entries.forEach { accent ->
                AccentColorOption(
                    accent = accent,
                    selected = accent == selectedAccent,
                    onClick = {
                        onAccentSelected(accent)
                    }
                )
            }
        }
    }
}

@Composable
private fun AccentColorOption(
    accent: AccentColor,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = if (selected) {
                    NovariColors.DarkTeal
                } else {
                    Color.Transparent
                },
                shape = CircleShape
            )
            .padding(if (selected) 5.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = accent.seed,
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun AppearanceCard(
    content: @Composable ColumnScope.() -> Unit
) {
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
            modifier = Modifier.padding(
                horizontal = 24.dp,
                vertical = 20.dp
            ),
            content = content
        )
    }
}