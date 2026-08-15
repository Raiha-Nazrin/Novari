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
        fontSize = 48.sp,
        lineHeight = 52.sp,
        color = NovariColors.Navy
    ),

    displayMedium = TextStyle(
        fontFamily = NovariDisplayFont,
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

    headlineMedium = TextStyle(
        fontFamily = NovariDisplayFont,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        color = NovariColors.Navy
    ),

    headlineSmall = TextStyle(
        fontFamily = NovariDisplayFont,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        color = NovariColors.Navy
    ),

    // UI / body
    bodyLarge = TextStyle(
        fontFamily = NovariBodyFont,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        color = NovariColors.Navy
    ),

    bodyMedium = TextStyle(
        fontFamily = NovariBodyFont,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = NovariColors.Slate
    ),

    bodySmall = TextStyle(
        fontFamily = NovariBodyFont,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = NovariColors.Slate
    ),

    labelLarge = TextStyle(
        fontFamily = NovariBodyFont,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = NovariColors.Navy
    ),

    labelMedium = TextStyle(
        fontFamily = NovariBodyFont,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = NovariColors.Slate
    ),

    labelSmall = TextStyle(
        fontFamily = NovariBodyFont,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = NovariColors.Muted
    )
)