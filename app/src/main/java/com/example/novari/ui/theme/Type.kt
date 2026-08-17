package com.example.novari.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val NovariDisplayFont = SerifFontFamily
val NovariBodyFont = InterFontFamily

val NovariTypography = Typography(

    // Large editorial headings
    displayLarge = TextStyle(
        fontFamily = NovariDisplayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        color = NovariColors.Navy
    ),

    displayMedium = TextStyle(
        fontFamily = NovariDisplayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        color = NovariColors.Navy
    ),

    headlineLarge = TextStyle(
        fontFamily = NovariDisplayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
        color = NovariColors.Navy
    ),

    // Screen title (used across Home, Insights, Settings)
    headlineMedium = TextStyle(
        fontFamily = NovariDisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = NovariColors.Navy
    ),

    // Primary amount / hero figure
    headlineSmall = TextStyle(
        fontFamily = NovariDisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = NovariColors.Navy
    ),

    // Card title
    titleLarge = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        color = NovariColors.Navy
    ),

    // Section header
    titleMedium = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = NovariColors.Navy
    ),

    // Sub-section / inline heading
    titleSmall = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = NovariColors.Navy
    ),

    // UI / body
    bodyLarge = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = NovariColors.Navy
    ),

    bodyMedium = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = NovariColors.Slate
    ),

    bodySmall = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = NovariColors.Slate
    ),

    labelLarge = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = NovariColors.Navy
    ),

    labelMedium = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = NovariColors.Slate
    ),

    // Eyebrow / overline
    labelSmall = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp,
        color = NovariColors.Muted
    )
)