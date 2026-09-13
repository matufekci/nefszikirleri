package com.example.util

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Hamle 14: Accessibility improvements
 * - Ensures minimum 48dp touch target (WCAG)
 * - Handles fontScale extremes gracefully
 * - Provides helpers for content descriptions
 */
object AccessibilityHelper {
    const val MIN_TOUCH_TARGET_DP = 48

    fun isExtremeFontScale(fontScale: Float): Boolean = fontScale > 1.35f
    fun isLargeFontScale(fontScale: Float): Boolean = fontScale > 1.25f

    fun getAccessibleDescription(
        lang: String,
        tr: String,
        en: String,
        ar: String,
        de: String,
        fr: String
    ): String = when (lang.lowercase()) {
        "ar" -> ar
        "de" -> de
        "fr" -> fr
        "en" -> en
        else -> tr
    }
}

/**
 * Ensures minimum 48dp touch target for accessibility (WCAG 2.5.5)
 */
fun Modifier.minimumTouchTarget(minSize: Dp = 48.dp): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "minimumTouchTarget"
        value = minSize
    }
) {
    this.defaultMinSize(minWidth = minSize, minHeight = minSize)
}

/**
 * Checks if current font scale requires layout adjustments
 */
@ComposableHelper
fun shouldUseCompactLayout(): Boolean {
    // This is a composable helper - actual implementation uses LocalDensity
    return false // Placeholder, real check done in composables via LocalDensity.current.fontScale
}

// Annotation to mark composable helpers
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class ComposableHelper
