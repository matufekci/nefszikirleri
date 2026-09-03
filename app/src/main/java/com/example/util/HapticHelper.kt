package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Standardized Haptic Feedback Controller for Nefs Zikirleri.
 * Provides clear, predictable and calibrated vibrations based on the user's intensity preference.
 */
class HapticHelper(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private fun getIntensityScale(intensity: String): Float {
        return when (intensity.lowercase()) {
            "strong" -> 1.35f
            "light" -> 0.65f
            else -> 1.0f // "medium" default
        }
    }

    private fun scaleAmplitude(baseAmplitude: Int, scale: Float): Int {
        return (baseAmplitude * scale).toInt().coerceIn(1, 255)
    }

    private fun scaleDuration(baseDuration: Long, scale: Float): Long {
        return (baseDuration * scale).toLong().coerceAtLeast(8L)
    }

    /**
     * Standard +1 single tap vibration
     */
    fun tap(intensity: String = "medium") {
        val vib = vibrator ?: return
        val scale = getIntensityScale(intensity)
        val duration = scaleDuration(18L, scale)
        val amplitude = scaleAmplitude(160, scale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(duration)
        }
    }

    /**
     * Quick-add +33 button vibration (A crisp, distinct double micro-pulse scaled to intensity)
     */
    fun quickAdd33(intensity: String = "medium") {
        val vib = vibrator ?: return
        val scale = getIntensityScale(intensity)
        val amp1 = scaleAmplitude(160, scale)
        val amp2 = scaleAmplitude(200, scale)
        val d1 = scaleDuration(20L, scale)
        val d2 = scaleDuration(25L, scale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, d1, 35, d2)
            val amplitudes = intArrayOf(0, amp1, 0, amp2)
            vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(longArrayOf(0, d1, 35, d2), -1)
        }
    }

    /**
     * Quick-add +100 button vibration (A solid, firm single pulse scaled to intensity)
     */
    fun quickAdd100(intensity: String = "medium") {
        val vib = vibrator ?: return
        val scale = getIntensityScale(intensity)
        val duration = scaleDuration(42L, scale)
        val amplitude = scaleAmplitude(205, scale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(duration)
        }
    }

    /**
     * Quick-add +1000 button vibration (A deep double pulse scaled to intensity)
     */
    fun quickAdd1000(intensity: String = "medium") {
        val vib = vibrator ?: return
        val scale = getIntensityScale(intensity)
        val amp1 = scaleAmplitude(180, scale)
        val amp2 = scaleAmplitude(225, scale)
        val d1 = scaleDuration(30L, scale)
        val d2 = scaleDuration(45L, scale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, d1, 40, d2)
            val amplitudes = intArrayOf(0, amp1, 0, amp2)
            vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(longArrayOf(0, d1, 40, d2), -1)
        }
    }

    /**
     * Quick-add +5000 button vibration (A rich triple progressive pulse scaled to intensity)
     */
    fun quickAdd5000(intensity: String = "medium") {
        val vib = vibrator ?: return
        val scale = getIntensityScale(intensity)
        val amp1 = scaleAmplitude(170, scale)
        val amp2 = scaleAmplitude(205, scale)
        val amp3 = scaleAmplitude(245, scale)
        val d1 = scaleDuration(25L, scale)
        val d2 = scaleDuration(35L, scale)
        val d3 = scaleDuration(50L, scale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, d1, 30, d2, 30, d3)
            val amplitudes = intArrayOf(0, amp1, 0, amp2, 0, amp3)
            vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(longArrayOf(0, d1, 30, d2, 30, d3), -1)
        }
    }

    /**
     * Quick-add +10000 button vibration (A majestic 4-pulse feedback scaled to intensity)
     */
    fun quickAdd10000(intensity: String = "medium") {
        val vib = vibrator ?: return
        val scale = getIntensityScale(intensity)
        val amp1 = scaleAmplitude(160, scale)
        val amp2 = scaleAmplitude(190, scale)
        val amp3 = scaleAmplitude(220, scale)
        val amp4 = scaleAmplitude(250, scale)
        val d1 = scaleDuration(20L, scale)
        val d2 = scaleDuration(30L, scale)
        val d3 = scaleDuration(40L, scale)
        val d4 = scaleDuration(55L, scale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, d1, 25, d2, 25, d3, 25, d4)
            val amplitudes = intArrayOf(0, amp1, 0, amp2, 0, amp3, 0, amp4)
            vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(longArrayOf(0, d1, 25, d2, 25, d3, 25, d4), -1)
        }
    }

    /**
     * Milestone reached during single tap increments (% 33)
     */
    fun milestone33(intensity: String = "medium") {
        quickAdd33(intensity)
    }

    /**
     * Milestone reached during single tap increments (% 100)
     */
    fun milestone100(intensity: String = "medium") {
        val vib = vibrator ?: return
        val scale = getIntensityScale(intensity)
        val amp1 = scaleAmplitude(165, scale)
        val amp2 = scaleAmplitude(195, scale)
        val amp3 = scaleAmplitude(230, scale)
        val d1 = scaleDuration(20L, scale)
        val d2 = scaleDuration(28L, scale)
        val d3 = scaleDuration(38L, scale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, d1, 30, d2, 30, d3)
            val amplitudes = intArrayOf(0, amp1, 0, amp2, 0, amp3)
            vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(longArrayOf(0, d1, 30, d2, 30, d3), -1)
        }
    }

    /**
     * Celebratory vibration sequence on target completion (Refined and subtle)
     */
    fun celebration() {
        val vib = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 35, 45, 35, 45, 50)
            val amplitudes = intArrayOf(0, 120, 0, 140, 0, 160)
            vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(longArrayOf(0, 35, 45, 35, 45, 50), -1)
        }
    }
}
