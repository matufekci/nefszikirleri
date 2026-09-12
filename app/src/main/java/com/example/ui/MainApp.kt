package com.example.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.graphics.Color
import com.example.ui.components.AnimatedIconSplash
import kotlinx.coroutines.delay
import com.example.ui.components.SpiritualAmbientBackground
import com.example.ui.components.SyncConflictDialog
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
import androidx.core.content.edit
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.example.ui.theme.AppPalettes
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NefsZikirTheme
import com.example.ui.viewmodel.ZikirViewModel
import com.example.util.rememberShouldReduceMotion

// statusBarsIgnoringVisibility deneysel (ExperimentalLayoutApi) isaretli.
// Bu API'yi bilerek kullaniyoruz: tam ekran modunda durum cubugu gizlense
// de ayni yuksekligi raporlamasi gerekiyor, aksi halde ust bar zipliyor.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainApp(viewModel: ZikirViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Bulut/yerel çakışması. Bu akış eskiden HİÇBİR yerde izlenmiyordu;
    // diyalog bu yüzden hiç görünmüyor, "Geri Yükle" sessizce hiçbir şey
    // yüklemeden bitiyordu.
    val syncConflict by viewModel.syncConflictState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shouldReduceMotion = rememberShouldReduceMotion()

    // SharedPreferences to track if user completed the initial intro onboarding
    val prefs = remember { context.getSharedPreferences("nefs_app_prefs", Context.MODE_PRIVATE) }
    var showIntro by remember {
        mutableStateOf(!prefs.getBoolean("intro_completed", false))
    }

    // Bildirimler ayarsız ve otomatiktir (tempo matematiği + hareketsizlik ağı).
    // Android 13+ izni ilk açılışta bir kez istenir; ret halinde bildirimler
    // sessizce devre dışı kalır, uygulama çalışmaya devam eder.
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* sonuc bilincli olarak islenmiyor */ }
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
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
        fontScale = state.settings.fontScale,
        lang = state.settings.lang
    ) {
        val colors = LocalAppColors.current
        val strings = AppStrings.get(state.settings.lang)

        if (showIntro) {
            IntroOnboardingScreen(
                state = state,
                viewModel = viewModel,
                onComplete = {
                    prefs.edit { putBoolean("intro_completed", true) }
                    showIntro = false
                }
            )
        } else {
            SpiritualAmbientBackground {
                // Splash, ölçülen sürenin tam iki katı kadar görünür:
                // hydration bittiğinde, o ana dek geçen süre kadar daha
                // beklenir, sonra ana ekrana geçilir.
                val splashStart = remember { System.currentTimeMillis() }
                var showSplash by remember { mutableStateOf(true) }
                LaunchedEffect(state.isHydrated) {
                    if (state.isHydrated) {
                        val elapsed =
                            (System.currentTimeMillis() - splashStart).coerceAtLeast(0L)
                        delay(elapsed)
                        showSplash = false
                    }
                }
                if (showSplash) {
                    AnimatedIconSplash(
                        primary = colors.primary,
                        textColor = colors.text,
                        title = strings.title,
                        reduceMotion = shouldReduceMotion
                    )
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isWideScreen = maxWidth >= 600.dp

                    Scaffold(
                        topBar = {
                            // Üstte sadece güvenli durum çubuğu boşluğu (Safe status bar insets)
                            //
                            // ONEMLI: statusBarsPadding() KULLANILMIYOR. Tam ekran (zen)
                            // modunda sistem barlari gizlendigi icin statusBarsPadding
                            // sifira cokuyor ve tum icerik bir anda yukari zipliyordu.
                            // statusBarsIgnoringVisibility, bar gizli olsa da ayni
                            // yuksekligi raporlar; boylece ust barin yeri sabit kalir.
                            Spacer(
                                modifier = Modifier.windowInsetsPadding(
                                    WindowInsets.statusBarsIgnoringVisibility
                                )
                            )
                        },
                        bottomBar = {
                            if (!isWideScreen) {
                                // Alt sekmeler tam ekrana gecince ANI kaybolmasin;
                                // suzulerek asagi insin. Aksi halde ekranin alt
                                // boslugu bir anda buyuyor ve gecis "keskin"
                                // gorunuyordu.
                                AnimatedVisibility(
                                    visible = !state.isZenMode,
                                    enter = fadeIn(tween(300)) + slideInVertically(
                                        tween(420, easing = FastOutSlowInEasing)
                                    ) { it / 2 },
                                    exit = fadeOut(tween(220)) + slideOutVertically(
                                        tween(420, easing = FastOutSlowInEasing)
                                    ) { it / 2 }
                                ) {
                                    DhikrBottomBar(
                                        currentTab = state.tab,
                                        lang = state.settings.lang,
                                        onTabSelected = { viewModel.setTab(it) }
                                    )
                                }
                            }
                        },
                        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
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
                                AnimatedContent(
                                    targetState = state.tab,
                                    transitionSpec = {
                                        if (shouldReduceMotion) {
                                            EnterTransition.None togetherWith ExitTransition.None
                                        } else {
                                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                                        }
                                    },
                                    label = "tab_screen_transition"
                                ) { targetTab ->
                                    when (targetTab) {
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
                    }

                    // MODALS & CELEBRATIONS

            // Bulut yedeği bulundu / çakışma: kullanıcıya NE YAPILACAĞI SORULUR.
            // (Google girişi sonrası otomatik çalışır; sekmeden bağımsız
            // görünebilmesi için kök composable'da tutuluyor.)
            syncConflict?.let { conflict ->
                SyncConflictDialog(
                    lang = state.settings.lang,
                    remoteBackupTimestamp = conflict.lastSyncedAt,
                    onDismissRequest = { viewModel.dismissSyncConflict() },
                    onKeepLocal = { viewModel.resolveConflictWithLocalOverwrite() },
                    onUseRemote = { viewModel.resolveConflictWithRemote() },
                    onMerge = { viewModel.resolveConflictWithMerge() }
                )
            }

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
