package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val VazirFontFamily = FontFamily(
    Font(R.font.vazir_regular, FontWeight.Normal)
)

private val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = VazirFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = VazirFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = VazirFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = VazirFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = VazirFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = VazirFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = VazirFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = VazirFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = VazirFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = VazirFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = VazirFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = VazirFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = VazirFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = VazirFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = VazirFontFamily)
)


