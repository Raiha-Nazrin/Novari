package com.example.novari.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.ui.theme.NovariColors

@Composable
fun SettingsScreen(
    onEditProfile: () -> Unit = {},
    onAppearanceClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovariColors.Background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp, bottom = 32.dp)
    ) {

        // Header
        ProfileHeader(
            onNotificationClick = onNotificationsClick
        )


        Spacer(modifier = Modifier.height(22.dp))


        // Preferences
        Text(
            text = stringResource(R.string.preferences),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        PreferencesCard(
            onAppearanceClick = onAppearanceClick,
            onNotificationsClick = onNotificationsClick,
            onPrivacyClick = onPrivacyClick,
            onBackupClick = onBackupClick
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = stringResource(R.string.about_novari),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        AboutNovariCard(
            onAboutNovariClick = onAppearanceClick,
            onPrivacyPolicyClick = onPrivacyPolicyClick,
            onTermsClick = onTermsClick,
            onSupportClick = onBackupClick
        )
    }
}

@Composable
private fun ProfileHeader(
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
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.manage_your_novari_experience),
                style = MaterialTheme.typography.bodyMedium,
                color = NovariColors.Slate
            )
        }

    }
}

@Composable
private fun PreferencesCard(
    onAppearanceClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onBackupClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = NovariColors.Surface,
        border = BorderStroke(
            width = 1.dp,
            color = NovariColors.Border
        )
    ) {
        Column {

            PreferenceItem(
                icon = Icons.Outlined.Brightness6,
                title = "Appearance",
                subtitle = "Choose how Novari looks",
                onClick = onAppearanceClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = Icons.Outlined.NotificationsNone,
                title = "Notifications",
                subtitle = "Manage reminders and alerts",
                onClick = onNotificationsClick
            )

        }
    }
}

@Composable
private fun AboutNovariCard(
    onAboutNovariClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onSupportClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = NovariColors.Surface,
        border = BorderStroke(
            width = 1.dp,
            color = NovariColors.Border
        )
    ) {
        Column {

            PreferenceItem(
                icon = Icons.Outlined.Brightness6,
                title = "About Team",
                subtitle = "Learn more about Novari",
                onClick = onAboutNovariClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = Icons.Outlined.NotificationsNone,
                title = "Privacy Policy",
                subtitle = "How Novari protects your data",
                onClick = onPrivacyPolicyClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = Icons.Outlined.Shield,
                title = "Terms",
                subtitle = "Rules for using Novari",
                onClick = onTermsClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = Icons.Outlined.Cloud,
                title = "Support",
                subtitle = "Get help when you need it",
                onClick = onSupportClick
            )
        }
    }
}

@Composable
private fun PreferenceItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 14.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Icon container
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = NovariColors.PaleTeal,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NovariColors.Navy,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = NovariColors.Navy
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NovariColors.Slate
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = NovariColors.Navy,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun PreferenceDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 28.dp),
        thickness = 1.dp,
        color = NovariColors.Border
    )
}