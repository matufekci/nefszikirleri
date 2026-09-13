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
    /**
     * Yalnizca SISTEMIN 'hareketi azalt' ayarina bakar.
     *
     * Eskiden `if (isZenMode) return true` satiri vardi; bu yuzden zen/tam
     * ekran moduna gecince halkanin etrafindaki isilti ve sanatsal dis
     * efekt katmani tamamen kapatiliyordu. Zen modu artik erisilebilirlik
     * ayarini ezmiyor; kullanici sistemi kapatmadiysa isiltilar kalir.
     */
    fun isReduceMotionEnabled(context: Context): Boolean {
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
fun rememberShouldReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        MotionUtils.isReduceMotionEnabled(context)
    }
}
