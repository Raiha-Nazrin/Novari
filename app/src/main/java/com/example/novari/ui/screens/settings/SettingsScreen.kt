package com.example.novari.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.novari.BuildConfig
import com.example.novari.R
import com.example.novari.ui.components.ScreenHeader
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariShape

@Composable
fun SettingsScreen(
    onEditProfile: () -> Unit = {},
    onAppearanceClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onContactUsClick: () -> Unit = {},
    onDetectionHealthClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
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
            onSupportClick = onContactUsClick
        )
    }
}

@Composable
private fun ProfileHeader(
    onNotificationClick: () -> Unit
) {
    ScreenHeader(
        title = stringResource(R.string.settings),
        subtitle = stringResource(R.string.manage_your_novari_experience)
    )
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
        shape = NovariShape.card,
        color = NovariColors.Surface,
        border = BorderStroke(
            width = 1.dp,
            color = NovariColors.Border
        )
    ) {
        Column {

            PreferenceItem(
                icon = painterResource(R.drawable.ic_theme),
                title = "Appearance",
                subtitle = "Choose how Novari looks",
                onClick = onAppearanceClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = painterResource(R.drawable.ic_notifications),
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
    onSupportClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = NovariShape.card,
        color = NovariColors.Surface,
        border = BorderStroke(
            width = 1.dp,
            color = NovariColors.Border
        )
    ) {
        Column {

            PreferenceItem(
                icon = painterResource(R.drawable.ic_about_novari),
                title = "About Team",
                subtitle = "Learn more about Novari",
                onClick = onAboutNovariClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = painterResource(R.drawable.ic_terms_conditions),
                title = "Privacy Policy",
                subtitle = "How Novari protects your data",
                onClick = onPrivacyPolicyClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = painterResource(R.drawable.ic_terms_conditions),
                title = "Terms",
                subtitle = "Rules for using Novari",
                onClick = onTermsClick
            )

            PreferenceDivider()

            PreferenceItem(
                icon = painterResource(R.drawable.ic_support),
                title = "Support",
                subtitle = "Get help when you need it",
                onClick = onSupportClick
            )
        }
    }
}

@Composable
private fun PreferenceItem(
    icon: Painter,
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
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

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