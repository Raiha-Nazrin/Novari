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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novari.R
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariTypography

@Composable
fun SettingsScreen(
    onEditProfile: () -> Unit = {},
    onAppearanceClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onBackupClick: () -> Unit = {}
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

        Spacer(modifier = Modifier.height(28.dp))

        // Profile Card
        ProfileAccountCard(
            name = "Arjun",
            accountType = "Personal account",
            onEditClick = onEditProfile
        )

        Spacer(modifier = Modifier.height(22.dp))


        // Preferences
        Text(
            text = stringResource(R.string.preferences),
            style = NovariTypography.headlineSmall,
            modifier = Modifier.padding(horizontal = 12.dp),
            fontSize = 18.sp
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
            style = NovariTypography.headlineSmall,
            modifier = Modifier.padding(horizontal = 12.dp),
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        AboutNovariCard(
            onAppearanceClick = onAppearanceClick,
            onNotificationsClick = onNotificationsClick,
            onPrivacyClick = onPrivacyClick,
            onBackupClick = onBackupClick
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
                text = stringResource(R.string.profile),
                style = NovariTypography.displayMedium,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.manage_your_novari_experience),
                style = NovariTypography.bodyLarge,
                color = NovariColors.Slate,
                fontSize = 13.sp
            )
        }

    }
}

@Composable
private fun ProfileAccountCard(
    name: String,
    accountType: String,
    onEditClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = NovariColors.Surface,
        border = BorderStroke(
            width = 1.dp,
            color = NovariColors.Border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Avatar
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = NovariColors.PaleTeal,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name
                        .firstOrNull()
                        ?.uppercase()
                        ?: "",
                    style = NovariTypography.headlineMedium,
                    color = NovariColors.Teal,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(22.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = NovariTypography.headlineSmall,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = accountType,
                    style = NovariTypography.bodyMedium,
                    fontSize = 13.sp
                )
            }

            IconButton(
                onClick = onEditClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit profile",
                    tint = NovariColors.Navy,
                    modifier = Modifier.size(25.dp)
                )
            }
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

            PreferenceDivider()

            PreferenceItem(
                icon = Icons.Outlined.Shield,
                title = "Privacy",
                subtitle = "Permissions and data controls",
                onClick = onPrivacyClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = Icons.Outlined.Cloud,
                title = "Backup",
                subtitle = "Manage your backup",
                onClick = onBackupClick
            )
        }
    }
}

@Composable
private fun AboutNovariCard(
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
                title = "About Team",
                subtitle = "Learn more about Novari",
                onClick = onAppearanceClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = Icons.Outlined.NotificationsNone,
                title = "Privacy Policy",
                subtitle = "How Novari protects your data",
                onClick = onNotificationsClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = Icons.Outlined.Shield,
                title = "Terms",
                subtitle = "Rules for using Novari",
                onClick = onPrivacyClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = Icons.Outlined.Cloud,
                title = "Support",
                subtitle = "Get help when you need it",
                onClick = onBackupClick
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
                style = NovariTypography.bodyLarge,
                color = NovariColors.Navy,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = NovariTypography.bodyMedium,
                color = NovariColors.Slate,
                fontSize = 12.sp
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