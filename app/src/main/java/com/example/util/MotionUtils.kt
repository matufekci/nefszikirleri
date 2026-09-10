package com.example.util

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Erişilebilirlik - Reduced Motion (Hareketi Azalt) ve Animasyon Ölçeği Yönetim Yardımcısı
 */
object MotionUtils {
    fun isReduceMotionEnabled(context: Context, isZenMode: Boolean = false): Boolean {
        if (isZenMode) return true
        return try {
            val animatorScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            val transitionScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1f
            )
            animatorScale == 0f || transitionScale == 0f
        } catch (e: Exception) {
            false
        }
    }
}

@Composable
fun rememberShouldReduceMotion(isZenMode: Boolean = false): Boolean {
    val context = LocalContext.current
    return remember(context, isZenMode) {
        MotionUtils.isReduceMotionEnabled(context, isZenMode)
    }
}
