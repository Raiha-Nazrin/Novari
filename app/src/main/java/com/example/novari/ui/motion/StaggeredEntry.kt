package com.example.novari.ui.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.example.novari.ui.theme.NovariMotion
import kotlinx.coroutines.delay

@Composable
fun StaggeredEntry(
    index: Int,
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val reducedMotion = LocalReducedMotion.current
    val progress = remember { Animatable(if (reducedMotion) 1f else 0f) }
    var hasPlayed by rememberSaveable { mutableStateOf(reducedMotion) }

    LaunchedEffect(visible) {
        if (!visible || hasPlayed) return@LaunchedEffect
        hasPlayed = true
        if (reducedMotion) {
            progress.snapTo(1f)
        } else {
            delay(index * NovariMotion.StaggerStepMs)
            progress.animateTo(1f, NovariMotion.Float)
        }
    }

    val riseDp = NovariMotion.StaggerRiseDp
    Box(
        modifier = modifier.graphicsLayer {
            val p = progress.value
            alpha = p
            translationY = (1f - p) * riseDp * density
        }
    ) { content() }
}
