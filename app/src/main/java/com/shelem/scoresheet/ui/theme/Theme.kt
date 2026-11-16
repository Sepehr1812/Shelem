package com.shelem.scoresheet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import com.shelem.scoresheet.util.LocaleHelper

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = FeltGreenDark,
    primaryContainer = FeltGreenContainerDark,
    onPrimaryContainer = GoldAccentLight,
    secondary = FeltGreenLight,
    onSecondary = TextPrimaryDark,
    secondaryContainer = FeltGreenPrimary,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = GoldAccentLight,
    onTertiary = FeltGreenDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardBackgroundDark,
    onSurfaceVariant = TextSecondaryDark,
    error = PersianRed,
    onError = TextPrimaryDark,
    errorContainer = PersianRedContainer,
    onErrorContainer = PersianRed
)

private val LightColorScheme = lightColorScheme(
    primary = FeltGreenPrimary,
    onPrimary = CardBackgroundLight,
    primaryContainer = FeltGreenContainer,
    onPrimaryContainer = FeltGreenDark,
    secondary = GoldAccentDark,
    onSecondary = CardBackgroundLight,
    secondaryContainer = GoldAccentLight,
    onSecondaryContainer = FeltGreenDark,
    tertiary = EmeraldGreen,
    onTertiary = CardBackgroundLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardBackgroundLight,
    onSurfaceVariant = TextSecondaryLight,
    error = PersianRed,
    onError = CardBackgroundLight,
    errorContainer = PersianRedContainer,
    onErrorContainer = PersianRed
)

@Composable
fun ShelemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val context = LocalContext.current
    val isPersian = LocaleHelper.isPersian(context)
    val layoutDirection = if (isPersian) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
