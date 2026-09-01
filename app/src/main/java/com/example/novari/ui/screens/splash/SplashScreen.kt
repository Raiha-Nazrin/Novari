package com.example.novari.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novari.R
import com.example.novari.ui.motion.LocalReducedMotion
import com.example.novari.ui.theme.NovariMotion
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val SPLASH_DISPLAY_DURATION_MS = 1600L

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val reducedMotion = LocalReducedMotion.current

    val scale = remember {
        Animatable(
            if (reducedMotion) 1f else 0.85f
        )
    }

    val alpha = remember {
        Animatable(
            if (reducedMotion) 1f else 0f
        )
    }

    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = NovariMotion.Float
            )
        }

        alpha.animateTo(
            targetValue = 1f,
            animationSpec = NovariMotion.Float
        )

        delay(SPLASH_DISPLAY_DURATION_MS.milliseconds)

        onSplashFinished()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // Background
            Image(
                painter = painterResource(
                    id = R.drawable.img_splash_bg
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Logo + wordmark
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.alpha = alpha.value
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(
                        id = R.drawable.img_splash_app_logo
                    ),
                    contentDescription = "Novari",
                    modifier = Modifier.size(110.dp)
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "NOVARI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 45.sp
                )
            }
        }
    }
}