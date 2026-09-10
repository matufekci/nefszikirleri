package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import com.example.util.rememberShouldReduceMotion
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.LocalAppColors
import com.example.ui.viewmodel.DhikrUiState
import com.example.ui.viewmodel.ZikirViewModel

/**
 * Manevi Açılış ve Kurulum Ekranı (Intro & Onboarding Orchestrator)
 *
 * Akış Adımları:
 * 1. Karşılama Sayfası (WelcomePage)
 * 2. Dil ve Bölgesel Ayar Sayfası (LanguageSelectionPage)
 * 3. Oturum Açma & Bulut Senkronizasyonu (CompletionPage)
 */
@Composable
fun IntroOnboardingScreen(
    state: DhikrUiState,
    viewModel: ZikirViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()

    var currentLang by remember(state.settings.lang) { mutableStateOf(state.settings.lang) }
    var currentStep by rememberSaveable { mutableStateOf(0) }
    val totalSteps = 3

    val shouldReduceMotion = rememberShouldReduceMotion()

    // Sonsuz Lüks Ambiyans & Parıltı Animasyonları
    val infiniteTransition = rememberInfiniteTransition(label = "intro_infinite")

    val rawHaloScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )

    val rawShimmerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_rotation"
    )

    val rawHaloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    val haloScale = if (shouldReduceMotion) 1f else rawHaloScale
    val shimmerRotation = if (shouldReduceMotion) 0f else rawShimmerRotation
    val haloAlpha = if (shouldReduceMotion) 0.5f else rawHaloAlpha

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        // 1. Zemin Ambiyans Gradyanı
        OnboardingHaloBackground(haloScale = haloScale)

        // 2. Ana Akış Sütunu
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ÜST KISIM: İlerleme İndikatörleri ve Atlama Butonu
            OnboardingTopBar(
                currentStep = currentStep,
                totalSteps = totalSteps,
                currentLang = currentLang,
                onSkip = onComplete
            )

            // ORTA KISIM: Sayfa Akışı (Kaydırılabilir Container ve Geçiş Animasyonları)
            val stepScrollState = rememberScrollState()
            LaunchedEffect(currentStep) {
                stepScrollState.scrollTo(0)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(stepScrollState),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (shouldReduceMotion) {
                            EnterTransition.None togetherWith ExitTransition.None
                        } else if (targetState > initialState) {
                            (slideInHorizontally { width -> width / 2 } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width / 2 } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width / 2 } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width / 2 } + fadeOut()
                            )
                        }
                    },
                    label = "intro_step_content"
                ) { step ->
                    when (step) {
                        0 -> WelcomePage(
                            haloScale = haloScale,
                            haloAlpha = haloAlpha,
                            shimmerRotation = shimmerRotation,
                            lang = currentLang
                        )
                        1 -> LanguageSelectionPage(
                            currentLang = currentLang,
                            onSelectLang = { code ->
                                currentLang = code
                                viewModel.setLanguage(code)
                            }
                        )
                        2 -> CompletionPage(
                            currentUser = currentUser,
                            isSyncing = isCloudSyncing,
                            onSignIn = { viewModel.signInWithGoogle(context) },
                            onSignOut = { viewModel.signOut() },
                            lang = currentLang
                        )
                    }
                }
            }

            // ALT KISIM: İleri / Geri Navigasyon Kontrolleri
            OnboardingBottomBar(
                currentStep = currentStep,
                totalSteps = totalSteps,
                currentLang = currentLang,
                onBack = { if (currentStep > 0) currentStep-- },
                onNext = {
                    if (currentStep < totalSteps - 1) {
                        currentStep++
                    } else {
                        onComplete()
                    }
                }
            )
        }
    }
}
