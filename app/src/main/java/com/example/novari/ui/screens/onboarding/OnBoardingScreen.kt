package com.example.novari.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import com.example.novari.data.onboardingPages
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.ui.haptics.rememberNovariHaptics
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariMotion
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    onSkip: () -> Unit = onOnboardingComplete
) {
    val pages = onboardingPages
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val haptics = rememberNovariHaptics()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .drop(1)
            .collect { haptics.tick() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        color = NovariColors.Slate
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                OnboardingPageContent(
                    page = pages[pageIndex],
                    isActive = pagerState.settledPage == pageIndex,
                    pageOffset = { pagerState.pageOffset(pageIndex) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageIndicator(
                    pageCount = pages.size,
                    pageOffsetForPage = { index -> pagerState.pageOffset(index) },
                    onDotClick = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page = index, animationSpec = NovariMotion.PagerSettle)
                        }
                    }
                )

                Box(modifier = Modifier.weight(1f))

                MorphingActionButton(
                    isLastPage = isLastPage,
                    onNext = {
                        haptics.press()
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1,
                                animationSpec = NovariMotion.PagerSettle
                            )
                        }
                    },
                    onComplete = {
                        haptics.confirm()
                        onOnboardingComplete()
                    }
                )
            }
        }
    }
}

private fun PagerState.pageOffset(page: Int): Float =
    (currentPage - page) + currentPageOffsetFraction

@Composable
private fun MorphingActionButton(
    isLastPage: Boolean,
    onNext: () -> Unit,
    onComplete: () -> Unit
) {
    var completing by remember { mutableStateOf(false) }

    val containerColor by animateColorAsState(
        targetValue = if (isLastPage) NovariColors.DarkTeal else Color.Transparent,
        animationSpec = NovariMotion.Colour,
        label = "ctaContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isLastPage) MaterialTheme.colorScheme.onPrimary else NovariColors.Teal,
        animationSpec = NovariMotion.Colour,
        label = "ctaContentColor"
    )
    val horizontalPadding by animateDpAsState(
        targetValue = if (isLastPage) 24.dp else 12.dp,
        animationSpec = NovariMotion.Dp,
        label = "ctaHorizontalPadding"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isLastPage) 28.dp else 24.dp,
        animationSpec = NovariMotion.Dp,
        label = "ctaCornerRadius"
    )

    Surface(
        onClick = {
            if (isLastPage) {
                if (!completing) {
                    completing = true
                    onComplete()
                }
            } else {
                onNext()
            }
        },
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier.animateContentSize(NovariMotion.Size)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = isLastPage,
                transitionSpec = {
                    (slideInVertically(animationSpec = NovariMotion.Offset) { it / 2 } + fadeIn(animationSpec = NovariMotion.Float))
                        .togetherWith(slideOutVertically(animationSpec = NovariMotion.Offset) { -it / 2 } + fadeOut(animationSpec = NovariMotion.Float))
                        .using(SizeTransform(clip = false))
                },
                label = "ctaLabel"
            ) { lastPage ->
                Text(
                    text = if (lastPage) stringResource(R.string.onboarding_start_journey)
                    else stringResource(R.string.next),
                    color = contentColor
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = contentColor
            )
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    pageOffsetForPage: (Int) -> Float,
    onDotClick: (Int) -> Unit = {},
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = NovariColors.Border,
    dotSize: Dp = 8.dp,
    spacing: Dp = 5.dp
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = spacing)
                    .size(dotSize)
                    .clickable { onDotClick(index) }
                    .drawBehind {
                        val fraction = 1f - (abs(pageOffsetForPage(index)) * 1.6f).coerceIn(0f, 1f)
                        val color = lerp(inactiveColor, activeColor, fraction)
                        drawCircle(color = color, radius = size.minDimension / 2f, center = Offset(size.width / 2f, size.height / 2f))
                    }
            )
        }
    }
}
