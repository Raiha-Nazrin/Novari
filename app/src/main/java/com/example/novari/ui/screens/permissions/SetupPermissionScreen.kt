package com.example.novari.ui.screens.permissions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.novari.R
import com.example.novari.permissions.PermissionStatus
import com.example.novari.permissions.PermissionType
import com.example.novari.ui.haptics.rememberNovariHaptics
import com.example.novari.ui.motion.StaggeredEntry
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariMotion
import com.example.novari.ui.theme.NovariTypography

@Composable
fun SetupPermissionScreen(
    uiState: SetupPermissionUiState = SetupPermissionUiState(),
    onPermissionAction: (PermissionType) -> Unit = {},
    onClose: () -> Unit = {},
    onMaybeLater: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptics = rememberNovariHaptics()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        StaggeredEntry(index = 0, visible = true) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_shield),
                    contentDescription = null,
                    modifier = Modifier

                        .height(180.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        StaggeredEntry(index = 1, visible = true) {
            Text(
                text = stringResource(R.string.almost_ready),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                style = NovariTypography.displayLarge.copy(
                    fontSize = 20.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        StaggeredEntry(index = 2, visible = true) {
            Text(
                text = stringResource(R.string.novari_can_automate_your_expense_tracking),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = NovariTypography.labelMedium.copy(
                    fontSize = 12.sp
                )

            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        val onSmsAction = remember(onPermissionAction) {
            {
                haptics.press()
                onPermissionAction(PermissionType.SMS)
            }
        }
        val onNotificationsAction = remember(onPermissionAction) {
            {
                haptics.press()
                onPermissionAction(PermissionType.NOTIFICATIONS)
            }
        }

        StaggeredEntry(index = 3, visible = true) {
            PermissionCard(
                icon = painterResource(R.drawable.ic_sms_illust),
                title = stringResource(R.string.sms),
                subTitle = stringResource(R.string.automatically_understand_your_spending),
                description = stringResource(R.string.automatically_detect_transaction_messages_so_you_don_t_type_them_manually),
                status = uiState.sms,
                onAction = onSmsAction
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        StaggeredEntry(index = 4, visible = true) {
            PermissionCard(
                icon = painterResource(R.drawable.ic_notification_illus),
                title = stringResource(R.string.notifications),
                subTitle = stringResource(R.string.stay_on_top_of_your_pending),
                description = stringResource(R.string.get_useful_alerts_when_novari_detects_important_spending_trends),
                status = uiState.notifications,
                onAction = onNotificationsAction,
                optional = true
            )
        }


        Spacer(modifier = Modifier.weight(1f))


        TextButton(
            onClick = onMaybeLater,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = stringResource(R.string.maybe_later),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}


@Composable
private fun PermissionCard(
    icon: Painter,
    title: String,
    subTitle: String,
    description: String,
    status: PermissionStatus,
    onAction: () -> Unit,
    optional: Boolean? = false
) {
    val haptics = rememberNovariHaptics()
    val previousStatus = remember { mutableStateOf(status) }
    LaunchedEffect(status) {
        if (status == PermissionStatus.GRANTED && previousStatus.value != PermissionStatus.GRANTED) {
            haptics.confirm()
        }
        previousStatus.value = status
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFE5E5E5),
                shape = RoundedCornerShape(16.dp)
            )
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = icon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    style = NovariTypography.bodyLarge.copy(
                        fontSize = 14.sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF202124)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subTitle,
                    style = NovariTypography.bodyMedium.copy(
                        fontSize = 12.sp
                    ),
                    lineHeight = 19.sp,
                    color = Color(0xFF14736D)
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = description,
                    style = NovariTypography.bodyMedium.copy(
                        fontSize = 12.sp
                    ),
                    lineHeight = 18.sp,
                    color = Color(0xFF666666)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            color = Color(0xFFE5E5E5),
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(12.dp))


        val isGranted = status == PermissionStatus.GRANTED
        val isDenied = status == PermissionStatus.DENIED || status == PermissionStatus.PERMANENTLY_DENIED

        val labelColor by animateColorAsState(
            targetValue = if (isDenied) NovariColors.Error else Color(0xFF666666),
            animationSpec = NovariMotion.Colour,
            label = "permissionLabelColor"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = !isGranted,
                    onClick = onAction
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if(optional == false){
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Color(0xFF606060),
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

            }

            Text(
                text = when {
                    isDenied -> stringResource(R.string.denied)
                    optional == false -> stringResource(R.string.access_is_controlled_by_you)
                    else -> stringResource(R.string.optional)
                },
                modifier = Modifier.weight(1f),
                style = NovariTypography.bodyMedium.copy(
                    fontSize = 12.sp
                ),
                color = labelColor
            )

            AnimatedContent(
                targetState = status,
                transitionSpec = {
                    (slideInVertically(animationSpec = NovariMotion.Offset) { it / 2 } + fadeIn(animationSpec = NovariMotion.Float))
                        .togetherWith(slideOutVertically(animationSpec = NovariMotion.Offset) { -it / 2 } + fadeOut(animationSpec = NovariMotion.Float))
                },
                label = "permissionAction"
            ) { targetStatus ->
                val targetGranted = targetStatus == PermissionStatus.GRANTED
                val targetDenied = targetStatus == PermissionStatus.DENIED ||
                    targetStatus == PermissionStatus.PERMANENTLY_DENIED

                val actionText = when {
                    targetGranted -> stringResource(R.string.enabled)
                    targetStatus == PermissionStatus.PERMANENTLY_DENIED -> stringResource(R.string.open_settings)
                    else -> stringResource(R.string.allow_access)
                }
                val actionIcon = when {
                    targetGranted -> Icons.Filled.Check
                    targetStatus == PermissionStatus.PERMANENTLY_DENIED -> Icons.Filled.Settings
                    else -> Icons.AutoMirrored.Filled.ArrowForward
                }
                val actionTint by animateColorAsState(
                    targetValue = if (targetDenied) NovariColors.Error else Color(0xFF14736D),
                    animationSpec = NovariMotion.Colour,
                    label = "permissionActionTint"
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = actionText,
                        style = NovariTypography.bodyLarge.copy(
                            fontSize = 12.sp
                        ),
                        fontWeight = FontWeight.Medium,
                        color = actionTint
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionText,
                        tint = actionTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
