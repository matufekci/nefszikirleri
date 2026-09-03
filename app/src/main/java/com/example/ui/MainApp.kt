package com.example.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.Color
import com.example.ui.components.SpiritualAmbientBackground
import com.example.data.model.AppStrings
import com.example.ui.components.BadgeCelebrationDialog
import com.example.ui.components.DhikrBottomBar
import com.example.ui.components.DhikrNavRail
import com.example.ui.components.ParticleCelebrationDialog
import com.example.ui.components.QuickAccessDrawer
import com.example.ui.components.RoundCompletedDialog
import com.example.ui.components.TerkipFastJumpDialog
import com.example.ui.components.TerkipSequenceWarningDialog
import com.example.ui.components.ZikirInfoDialog
import com.example.ui.screens.IntroOnboardingScreen
import com.example.ui.screens.DhikrCounterScreen
import com.example.ui.screens.DhikrListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpiritualInfoScreen
import com.example.ui.screens.StatisticsScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import com.example.ui.theme.AppPalettes
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NefsZikirTheme
import com.example.ui.viewmodel.ZikirViewModel

@Composable
fun MainApp(viewModel: ZikirViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // SharedPreferences to track if user completed the initial intro onboarding
    val prefs = remember { context.getSharedPreferences("nefs_app_prefs", Context.MODE_PRIVATE) }
    var showIntro by remember {
        mutableStateOf(!prefs.getBoolean("intro_completed", false))
    }

    // Keep screen awake effect
    DisposableEffect(state.settings.keepAwakeEnabled) {
        val window = (context as? Activity)?.window
        if (state.settings.keepAwakeEnabled) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    NefsZikirTheme(
        themeName = state.settings.themeName,
        fontScale = state.settings.fontScale
    ) {
        val colors = LocalAppColors.current
        val strings = AppStrings.get(state.settings.lang)

        if (showIntro) {
            IntroOnboardingScreen(
                state = state,
                viewModel = viewModel,
                onComplete = {
                    prefs.edit().putBoolean("intro_completed", true).apply()
                    showIntro = false
                }
            )
        } else {
            SpiritualAmbientBackground {
                if (!state.isHydrated) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colors.primary
                        )
                    }
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isWideScreen = maxWidth >= 600.dp

                    Scaffold(
                        topBar = {
                            // Üstte sadece güvenli durum çubuğu boşluğu (Safe status bar insets)
                            Spacer(modifier = Modifier.statusBarsPadding())
                        },
                        bottomBar = {
                            if (!isWideScreen && !state.isZenMode) {
                                DhikrBottomBar(
                                    currentTab = state.tab,
                                    lang = state.settings.lang,
                                    onTabSelected = { viewModel.setTab(it) }
                                )
                            }
                        },
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isWideScreen && !state.isZenMode) {
                                DhikrNavRail(
                                    currentTab = state.tab,
                                    lang = state.settings.lang,
                                    onTabSelected = { viewModel.setTab(it) }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            ) {
                                when (state.tab) {
                                    "zikir" -> DhikrCounterScreen(state = state, viewModel = viewModel)
                                    "liste" -> DhikrListScreen(state = state, viewModel = viewModel)
                                    "istatistik" -> StatisticsScreen(state = state, viewModel = viewModel)
                                    "bilgi" -> SpiritualInfoScreen(lang = state.settings.lang)
                                    "ayarlar" -> SettingsScreen(
                                        state = state,
                                        viewModel = viewModel,
                                        onShowIntro = { showIntro = true }
                                    )
                                }
                            }
                        }
                    }

                    // MODALS & CELEBRATIONS
            state.badgeCelebrationData?.let { badge ->
                BadgeCelebrationDialog(
                    badge = badge,
                    lang = state.settings.lang,
                    onDismiss = { viewModel.dismissBadgeCelebration() }
                )
            }

            state.celebrationData?.let { data ->
                ParticleCelebrationDialog(
                    data = data,
                    lang = state.settings.lang,
                    onDismiss = { viewModel.closeCelebration() },
                    onNextZikir = { nextId ->
                        viewModel.selectZikir(nextId)
                        viewModel.closeCelebration()
                    }
                )
            }

            state.infoModalZikirId?.let { zikirId ->
                val targetCount = state.zikirs.find { it.id == zikirId }?.target ?: 0L
                ZikirInfoDialog(
                    zikirId = zikirId,
                    lang = state.settings.lang,
                    targetCount = targetCount,
                    onDismiss = { viewModel.openInfoModal(null) }
                )
            }

            state.sequenceWarning?.let { warning ->
                TerkipSequenceWarningDialog(
                    attemptedZikirId = warning.attemptedZikirId,
                    requiredZikirId = warning.requiredZikirId,
                    lang = state.settings.lang,
                    onNavigateToRequired = { viewModel.navigateToRequiredZikir() },
                    onFastJumpToAttempted = { attemptedId -> viewModel.openFastJumpDialog(attemptedId) },
                    onDismiss = { viewModel.dismissSequenceWarning() }
                )
            }

            state.fastJumpDialogZikirId?.let { targetZikirId ->
                TerkipFastJumpDialog(
                    targetZikirId = targetZikirId,
                    lang = state.settings.lang,
                    onConfirm = { id -> viewModel.fastJumpToZikir(id) },
                    onDismiss = { viewModel.openFastJumpDialog(null) }
                )
            }

            if (state.showRoundModal) {
                RoundCompletedDialog(
                    completedRounds = state.settings.completedRounds,
                    lang = state.settings.lang,
                    onDismiss = { viewModel.startNewRound(); viewModel.setShowRoundModal(false) }
                )
            }
                    }
                }
            }
        }
    }
}
