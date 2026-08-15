package com.example.novari.ui.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novari.ui.motion.StaggeredEntry
import com.example.novari.ui.theme.NovariColors
import kotlin.math.abs


data class OnboardingPage(
    @StringRes val titleRes: Int,
    @StringRes val introRes: Int,
    @StringRes val bodyRes: Int,
    @StringRes val accentRes: Int,
    @DrawableRes val imageRes: Int
)


@Composable
private fun DrawableIllustration(page: OnboardingPage, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = page.imageRes),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}


private const val OFFSET_SPEED = 1.6f

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    pageOffset: () -> Float = { 0f },
    visual: @Composable (Modifier) -> Unit = { m -> DrawableIllustration(page, m) }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 160.dp),
            contentAlignment = Alignment.Center
        ) {
            visual(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val absOffset = (abs(pageOffset()) * OFFSET_SPEED).coerceIn(0f, 1f)
                        scaleX = 1f - 0.15f * absOffset
                        scaleY = 1f - 0.15f * absOffset
                        alpha = 1f - absOffset
                        translationX = pageOffset() * size.width * 0.2f
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .graphicsLayer {

                    val absOffset = (abs(pageOffset()) * OFFSET_SPEED).coerceIn(0f, 1f)
                    alpha = 1f - absOffset
                    translationX = pageOffset() * size.width * 0.08f
                }
        ) {
            StaggeredEntry(index = 0, visible = isActive) {
                Text(
                    text = stringResource(page.titleRes),
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            StaggeredEntry(index = 1, visible = isActive) {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 20.dp)
                        .size(width = 28.dp, height = 2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(NovariColors.Teal)
                )
            }

            StaggeredEntry(index = 2, visible = isActive) {
                Text(
                    text = stringResource(page.introRes),
                    style = OnboardingBodyStyle,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            StaggeredEntry(index = 3, visible = isActive) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(page.bodyRes))
                        append(' ')
                        withStyle(SpanStyle(color = NovariColors.Teal, fontWeight = FontWeight.SemiBold)) {
                            append(stringResource(page.accentRes))
                        }
                    },
                    style = OnboardingBodyStyle
                )
            }
        }
    }
}

private val OnboardingBodyStyle
    @Composable get() = MaterialTheme.typography.bodyLarge.copy(
        fontSize = 15.sp,
        lineHeight = 26.sp,
        color = NovariColors.Slate
    )
