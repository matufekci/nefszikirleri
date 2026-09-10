package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

val LocalAppColors = staticCompositionLocalOf { AppPalettes.Emerald }

@Composable
fun NefsZikirTheme(
    themeName: String = "emerald",
    fontScale: Float = 1.15f,
    lang: String = "tr",
    content: @Composable () -> Unit
) {
    val appColors = AppPalettes.get(themeName)
    val layoutDirection = if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    val colorScheme = if (appColors.isDark) {
        darkColorScheme(
            primary = appColors.primary,
            secondary = appColors.secondary,
            background = appColors.bg,
            surface = appColors.surface,
            onPrimary = appColors.bg,
            onSecondary = appColors.bg,
            onBackground = appColors.text,
            onSurface = appColors.text
        )
    } else {
        lightColorScheme(
            primary = appColors.primary,
            secondary = appColors.secondary,
            background = appColors.bg,
            surface = appColors.surface,
            onPrimary = appColors.card,
            onSecondary = appColors.card,
            onBackground = appColors.text,
            onSurface = appColors.text
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !appColors.isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !appColors.isDark
            }
        }
    }
    val systemFontScale = LocalDensity.current.fontScale.coerceIn(0.5f, 2.0f)
    val appFontScale = fontScale.coerceIn(0.7f, 1.5f)
    // Multiply but clamp to reasonable range to avoid huge text
    val effectiveScale = (appFontScale * systemFontScale).coerceIn(0.7f, 2.0f)

    // Custom density sağla
    val adjustedDensity = Density(
        density = LocalDensity.current.density,
        fontScale = effectiveScale
    )

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalDensity provides adjustedDensity,
        LocalLayoutDirection provides layoutDirection
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
