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
import androidx.compose.ui.unit.Density

val LocalAppColors = staticCompositionLocalOf { AppPalettes.Emerald }

@Composable
fun NefsZikirTheme(
    themeName: String = "emerald",
    fontScale: Float = 1.15f,
    content: @Composable () -> Unit
) {
    val appColors = AppPalettes.get(themeName)

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
    val currentDensity = LocalDensity.current
    val newDensity = Density(currentDensity.density, fontScale = currentDensity.fontScale * fontScale)

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalDensity provides newDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
