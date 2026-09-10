package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Zikir Çemberi Animasyon State Modeli
 */
data class DhikrCircleAnimationState(
    val animatedProgress: State<Float>,
    val tapScale: Animatable<Float, AnimationVector1D>,
    val tapLuminescence: Animatable<Float, AnimationVector1D>,
    val breathingAura: State<Float>,
    val bezelShimmerAngle: State<Float>,
    val particleOrbitAngle: State<Float>,
    val sparkleTwinkle: State<Float>,
    val specularPhase: State<Float>,
    val tipLuster: State<Float>,
    val arabicGlowPulse: State<Float>,
    val arabicShimmerOffset: State<Float>,
    val triggerTap: () -> Unit
)

/**
 * 2026 Ethereal Animasyon ve Organik Dokunma Yaylanması Yönetimi
 * Reduced Motion desteği ile güncellenmiştir.
 */
@Composable
fun rememberDhikrCircleAnimations(
    progress: Float,
    onTap: () -> Unit,
    shouldReduceMotion: Boolean = false
): DhikrCircleAnimationState {
    val coroutineScope = rememberCoroutineScope()

    val tapScale = remember { Animatable(1.0f) }
    val tapLuminescence = remember { Animatable(0f) }

    if (shouldReduceMotion) {
        val staticProgress = remember(progress) { mutableStateOf(progress.coerceIn(0f, 1f)) }
        val staticAura = remember { mutableStateOf(0.6f) }
        val staticZero = remember { mutableStateOf(0f) }
        val staticOne = remember { mutableStateOf(1.0f) }

        val triggerTap: () -> Unit = remember(onTap) {
            {
                onTap()
            }
        }

        return DhikrCircleAnimationState(
            animatedProgress = staticProgress,
            tapScale = tapScale,
            tapLuminescence = tapLuminescence,
            breathingAura = staticAura,
            bezelShimmerAngle = staticZero,
            particleOrbitAngle = staticZero,
            sparkleTwinkle = staticZero,
            specularPhase = staticZero,
            tipLuster = staticOne,
            arabicGlowPulse = staticOne,
            arabicShimmerOffset = staticZero,
            triggerTap = triggerTap
        )
    }

    val animatedProgress = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "luxury_circle_progress"
    )

    // Ethereal Loops
    val infiniteTransition = rememberInfiniteTransition(label = "luxury_circle_loops")

    val breathingAura = infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_aura"
    )

    val bezelShimmerAngle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bezel_shimmer_angle"
    )

    val particleOrbitAngle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_orbit_angle"
    )

    val sparkleTwinkle = infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_twinkle"
    )

    val specularPhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "specular_phase"
    )

    val tipLuster = infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tip_luster"
    )

    val arabicGlowPulse = infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arabic_glow_pulse"
    )

    val arabicShimmerOffset = infiniteTransition.animateFloat(
        initialValue = -120f,
        targetValue = 320f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arabic_shimmer_offset"
    )

    val triggerTap: () -> Unit = remember(onTap) {
        {
            coroutineScope.launch {
                tapScale.animateTo(0.962f, tween(35, easing = LinearEasing))
                tapScale.animateTo(1.0f, spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMediumLow))
            }
            coroutineScope.launch {
                tapLuminescence.snapTo(1.0f)
                tapLuminescence.animateTo(0f, tween(450, easing = FastOutSlowInEasing))
            }
            onTap()
        }
    }

    return DhikrCircleAnimationState(
        animatedProgress = animatedProgress,
        tapScale = tapScale,
        tapLuminescence = tapLuminescence,
        breathingAura = breathingAura,
        bezelShimmerAngle = bezelShimmerAngle,
        particleOrbitAngle = particleOrbitAngle,
        sparkleTwinkle = sparkleTwinkle,
        specularPhase = specularPhase,
        tipLuster = tipLuster,
        arabicGlowPulse = arabicGlowPulse,
        arabicShimmerOffset = arabicShimmerOffset,
        triggerTap = triggerTap
    )
}
