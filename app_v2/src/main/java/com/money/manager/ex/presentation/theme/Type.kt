package com.money.manager.ex.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// We assume Manrope is available or use Default
val Manrope = FontFamily.Default

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 57.6.sp,
        letterSpacing = (-0.02).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 31.2.sp,
        letterSpacing = (-0.01).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.6.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.05.sp
    )
)
