package com.example.novari.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.ui.motion.LocalReducedMotion
import com.example.novari.ui.theme.NovariMotion
import kotlinx.coroutines.delay

private const val SPLASH_DISPLAY_DURATION_MS = 1600L

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val reducedMotion = LocalReducedMotion.current
    val scale = remember { Animatable(if (reducedMotion) 1f else 0.85f) }
    val alpha = remember { Animatable(if (reducedMotion) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            scale.animateTo(1f, NovariMotion.Float)
        }
        alpha.animateTo(1f, NovariMotion.Float)
        delay(SPLASH_DISPLAY_DURATION_MS)
        onSplashFinished()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_splash_app_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.alpha = alpha.value
                    }
            )
        }
    }
}