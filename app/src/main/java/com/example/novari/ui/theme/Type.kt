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
    ),

    displayMedium = TextStyle(
        fontFamily = NovariDisplayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        lineHeight = 44.sp,
    ),

    headlineLarge = TextStyle(
        fontFamily = NovariDisplayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),

    // Screen title (used across Home, Insights, Settings)
    headlineMedium = TextStyle(
        fontFamily = NovariDisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),

    // Primary amount / hero figure
    headlineSmall = TextStyle(
        fontFamily = NovariDisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),

    // Card title
    titleLarge = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
    ),

    // Section header
    titleMedium = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    ),

    // Sub-section / inline heading
    titleSmall = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),

    // UI / body
    bodyLarge = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),

    bodyMedium = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),

    bodySmall = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),

    labelLarge = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),

    labelMedium = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),

    // Eyebrow / overline
    labelSmall = TextStyle(
        fontFamily = NovariBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp,
    )
)