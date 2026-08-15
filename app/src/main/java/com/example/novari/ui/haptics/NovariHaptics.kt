package com.example.novari.ui.haptics

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Immutable
class NovariHaptics internal constructor(private val delegate: HapticFeedback) {

    fun tick() = delegate.performHapticFeedback(
        if (Build.VERSION.SDK_INT >= 30) HapticFeedbackType.SegmentTick
        else HapticFeedbackType.TextHandleMove
    )

    fun confirm() = delegate.performHapticFeedback(
        if (Build.VERSION.SDK_INT >= 34) HapticFeedbackType.Confirm
        else HapticFeedbackType.LongPress
    )

    fun press() = delegate.performHapticFeedback(HapticFeedbackType.VirtualKey)
}

@Composable
fun rememberNovariHaptics(): NovariHaptics {
    val delegate = LocalHapticFeedback.current
    return remember(delegate) { NovariHaptics(delegate) }
}
