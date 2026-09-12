package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.model.AppStrings
import com.example.data.model.Zikir
import com.example.data.model.ZikirContent
import com.example.ui.components.DhikrCircle
import com.example.ui.components.SpiritualBeadsIcon
import com.example.ui.components.SpiritualCheckIcon
import com.example.ui.components.SpiritualFlameIcon
import com.example.ui.theme.LocalAppColors
import com.example.ui.viewmodel.DhikrUiState
import com.example.ui.viewmodel.ZikirViewModel
import com.example.util.NumberFormatter
import kotlin.math.min

/**
 * Zikir Sayaç Ana Ekranı (Orchestrator Composable)
 */
@Composable
fun DhikrCounterScreen(
    state: DhikrUiState,
    viewModel: ZikirViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = AppStrings.get(state.settings.lang)

    // Modal & Sheet Dialog Durumları
    var showZikirSelectorSheet by rememberSaveable { mutableStateOf(false) }
    var showManualDialog by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showTargetDialog by rememberSaveable { mutableStateOf(false) }

    // Zen / Odaklanma Modu Durumu.
    // TEK dogruluk kaynagi ViewModel: MainApp alt sekmeleri state.isZenMode'a
    // gore gizliyor. Durum burada lokal tutuldugu icin tam ekrana gecince
    // sekmeler hic gizlenmiyordu.
    val isZenMode = state.isZenMode

    // Zen Modunda Sistem Barlarını Gizleme
    val activity = context as? Activity
    DisposableEffect(isZenMode) {
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isZenMode) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Aktif Zikir Verileri
    val currentZikir = state.currentZikir ?: state.zikirs.firstOrNull() ?: Zikir(1, 100000L, 0L)
    val zikirName = ZikirContent.getZikirName(currentZikir.id, state.settings.lang)
    val arabicText = ZikirContent.getArabicText(currentZikir.id)
    val virtue = ZikirContent.getZikirDetail(currentZikir.id, state.settings.lang)

    // Sıralı Kilit Mantığı Yardımcısı
    fun isZikirUnlocked(targetId: Int, allZikirs: List<Zikir>): Boolean {
        if (targetId <= 1) return true
        for (prevId in 1 until targetId) {
            val prev = allZikirs.find { it.id == prevId } ?: return false
            if (prev.count < prev.target) return false
        }
        return true
    }

    // Sonraki Zikir & Hatim Tamamlama Kontrolü
    val nextZikir = state.zikirs.find { it.id == currentZikir.id + 1 }
    val nextZikirName = if (nextZikir != null) ZikirContent.getZikirName(nextZikir.id, state.settings.lang) else ""
    val isCompleted100 = currentZikir.count >= currentZikir.target
    val isKhatmReady = currentZikir.id == 15 && isCompleted100 && state.zikirs.all { it.count >= it.target }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val isTabletOrLandscape = screenWidth > 600.dp
        val fontScale = LocalDensity.current.fontScale

        // Duyarlı Halka Boyutu Hesaplaması - Genişletildi
        val calculatedRingSize = when {
            isTabletOrLandscape -> min(screenHeight.value * 0.65f, 420f).dp
            fontScale > 1.3f -> min(screenHeight.value * 0.52f, 320f).dp
            screenHeight < 650.dp -> 260.dp
            screenHeight < 720.dp -> 290.dp
            screenHeight < 800.dp -> 320.dp
            else -> 350.dp
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isZenMode,
                transitionSpec = {
                    // Suzulme + solerek gecis: yeni ekran asagidan yukari
                    // suzulurken eski ekran ters yonde suzulup soluyor.
                    val slide = tween<Float>(560, easing = FastOutSlowInEasing)
                    val move = tween<IntOffset>(560, easing = FastOutSlowInEasing)
                    if (targetState) {
                        (slideInVertically(move) { it / 7 } + fadeIn(slide)) togetherWith
                            (slideOutVertically(move) { -it / 7 } + fadeOut(tween(340)))
                    } else {
                        (slideInVertically(move) { -it / 7 } + fadeIn(slide)) togetherWith
                            (slideOutVertically(move) { it / 7 } + fadeOut(tween(340)))
                    }
                },
                label = "zenModeTransition"
            ) { zenActive ->
                // Yumusak gecis: icerik ani degismek yerine suzulerek degisir.
                if (zenActive) {
                // Tam ekran modunda SADECE ust bar (titresim + cikis) ve cember kalir.
                val progress = if (currentZikir.target > 0) (currentZikir.count.toFloat() / currentZikir.target.toFloat()).coerceIn(0f, 1f) else 0f
                val transliteration = ZikirContent.getZikirTransliteration(currentZikir.id, state.settings.lang)

                // Ust bar NORMAL MODLA BIREBIR AYNI: ayni padding, ayni 640dp
                // genislik siniri, ayni CounterTopBar bileseni. Bu yuzden tam
                // ekrana gecince titresim ve tam ekran butonlarinin yeri
                // degismiyor. (Eskiden butonlar sag ustte alt alta duruyordu ve
                // Modifier.align gecis bileseninin icinde etkisiz kaliyordu.)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CounterTopBar(
                            settings = state.settings,
                            onCycleHapticMode = { viewModel.cycleHapticMode() },
                            onToggleZenMode = { viewModel.toggleZenMode(it) },
                            isZenMode = true
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        DhikrCircle(
                            ringSize = calculatedRingSize,
                            progress = progress,
                            displayCount = currentZikir.count,
                            targetCount = currentZikir.target,
                            isCountdownMode = state.settings.countdownMode,
                            arabicText = arabicText,
                            transliteration = transliteration,
                            lang = state.settings.lang,
                            isZenMode = true,
                            onTap = { viewModel.incrementCount(1) },
                            modifier = Modifier.testTag("dhikr_circle_tap_area")
                        )
                    }
                }
            } else {
                // Normal Mod
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // ==========================================
                    // 1. ÜST BÖLÜM: KONTROLLER
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Üst Bar (Titreşim & Zen Modu)
                        CounterTopBar(
                            settings = state.settings,
                            onCycleHapticMode = { viewModel.cycleHapticMode() },
                            onToggleZenMode = { viewModel.toggleZenMode(it) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ==========================================
                    // 2. ORTA BÖLÜM: DOKUNMATİK ZİKİR HALKASI
                    // ==========================================
                    Spacer(modifier = Modifier.height(4.dp))
                    val progress = if (currentZikir.target > 0) (currentZikir.count.toFloat() / currentZikir.target.toFloat()).coerceIn(0f, 1f) else 0f
                    val transliteration = ZikirContent.getZikirTransliteration(currentZikir.id, state.settings.lang)

                    DhikrCircle(
                        ringSize = calculatedRingSize,
                        progress = progress,
                        displayCount = currentZikir.count,
                        targetCount = currentZikir.target,
                        isCountdownMode = state.settings.countdownMode,
                        arabicText = arabicText,
                        transliteration = transliteration,
                        lang = state.settings.lang,
                        isZenMode = false,
                        onTap = { viewModel.incrementCount(1) },
                        modifier = Modifier.testTag("dhikr_circle_tap_area")
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // ==========================================
                    // 3. ALT BÖLÜM: GÜNLÜK VİRD & EYLEM BUTONLARI
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Günlük Vird İlerleme Barı (Hedef Düzenleme Tıklanabilir)
                        val dailyTarget = state.settings.dailyTarget.coerceAtLeast(1L)
                        val dailyProgress = if (dailyTarget > 0) state.todayRecited.toFloat() / dailyTarget.toFloat() else 0f
                        val dailyPercent = (dailyProgress * 100).toInt()

                        Card(
                            onClick = { showTargetDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.inputBg),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.border.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .testTag("btn_daily_target_edit")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SpiritualFlameIcon(tint = colors.gold, size = 14.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${strings.dailyTargetTitle}: ${NumberFormatter.format(state.todayRecited, state.settings.lang)} / ${NumberFormatter.format(dailyTarget, state.settings.lang)} (%$dailyPercent)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.text
                                    )
                                }
                            }
                        }

                        // Hızlı Ekleme ve Sonraki Zikir / Hatim Bannerı
                        QuickActionsSection(
                            settings = state.settings,
                            isCompleted100 = isCompleted100,
                            isKhatmReady = isKhatmReady,
                            nextZikir = nextZikir,
                            nextZikirName = nextZikirName,
                            onQuickAdd = { amount -> viewModel.incrementCount(amount) },
                            onCompleteKhatm = { viewModel.startNewRound() },
                            onSelectNextZikir = { nextId -> viewModel.selectZikir(nextId) }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Alt Aksiyon Butonları (Geri Al, Manuel, Sıfırla)
                        CounterBottomBar(
                            canUndo = state.canUndo,
                            lang = state.settings.lang,
                            onUndo = { viewModel.undoLastAction() },
                            onOpenManual = { showManualDialog = true },
                            onOpenReset = { showResetDialog = true }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            }
        }
    }

    // ==========================================
    // DİYALOGLAR VE BOTTOM SHEET'LER
    // ==========================================

    // Modal 1: Zikir Seçim Listesi (Bottom Sheet)
    if (showZikirSelectorSheet) {
        ZikirListSection(
            zikirs = state.zikirs,
            currentZikirId = currentZikir.id,
            lang = state.settings.lang,
            isZikirUnlocked = ::isZikirUnlocked,
            onSelectZikir = { selectedId -> viewModel.selectZikir(selectedId) },
            onDismissRequest = { showZikirSelectorSheet = false }
        )
    }

    // Modal 2: Manuel Sayı Ekleme / Çıkarma
    if (showManualDialog) {
        ManualAmountDialog(
            lang = state.settings.lang,
            onAdd = { amount ->
                viewModel.incrementCount(amount)
                showManualDialog = false
            },
            onRemove = { amount ->
                viewModel.decrementCount(amount)
                showManualDialog = false
            },
            onDismiss = { showManualDialog = false }
        )
    }

    // Modal 3: Sıfırlama Onay Diyaloğu
    if (showResetDialog) {
        ResetZikirDialog(
            zikirName = zikirName,
            lang = state.settings.lang,
            onConfirm = {
                viewModel.resetCurrentZikir()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    // Modal 4: Günlük Vird Hedefi Diyaloğu
    if (showTargetDialog) {
        DailyTargetDialog(
            settings = state.settings,
            onAdjustTarget = { delta -> viewModel.adjustDailyTarget(delta) },
            onSetTarget = { target -> viewModel.setDailyTarget(target) },
            onDismiss = { showTargetDialog = false }
        )
    }
}
