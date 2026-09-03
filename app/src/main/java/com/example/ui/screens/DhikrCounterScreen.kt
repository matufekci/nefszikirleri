package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Refresh
import com.example.ui.components.HapticIcons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.data.model.Zikir
import com.example.data.model.ZikirContent
import com.example.ui.components.LuxuryDhikrCircle
import com.example.ui.components.SpiritualBeadsIcon
import com.example.ui.components.SpiritualCheckIcon
import com.example.ui.components.SpiritualGearIcon
import com.example.ui.components.SpiritualLockIcon
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.actionButtonShadow
import com.example.ui.viewmodel.DhikrUiState
import com.example.ui.viewmodel.ZikirViewModel
import com.example.util.NumberFormatter
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2026 Trend Responsive Zikir Sayacı Ekranı
 * - Sürekli parıldayan ve ilerledikçe ışık aurası katlanarak artan altın zikir çemberi
 * - Küçük ekranlardan tabletlere kadar ekranı tam dolduran, aşağıda boşluk bırakmayan adaptif mimari
 * - Göz yormayan, kompakt ve ergonomik kontrol elemanları
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrCounterScreen(
    state: DhikrUiState,
    viewModel: ZikirViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(state.settings.lang)

    val currentZikir = state.currentZikir
        ?: state.zikirs.find { it.id == state.selectedId }
        ?: state.zikirs.firstOrNull()
        ?: Zikir(id = 1, target = 100000L, count = 0L)

    val zikirName = ZikirContent.getZikirName(currentZikir.id, state.settings.lang)
    val arabicText = ZikirContent.getArabicText(currentZikir.id)
    val transliteration = ZikirContent.getZikirTransliteration(currentZikir.id, state.settings.lang)

    var manualAmountText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showZikirSelectorSheet by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }

    val isCompleted100 = currentZikir.count >= currentZikir.target
    val isAllZikirsCompleted = remember(state.zikirs) {
        state.zikirs.isNotEmpty() && state.zikirs.all { it.count >= it.target }
    }
    val isLastZikirRahim = currentZikir.id == 15
    val isKhatmReady = isCompleted100 && isLastZikirRahim && isAllZikirsCompleted

    val nextZikir = remember(currentZikir.id, state.zikirs) {
        val currentIndex = state.zikirs.indexOfFirst { it.id == currentZikir.id }
        if (currentIndex in 0 until state.zikirs.lastIndex) {
            state.zikirs[currentIndex + 1]
        } else null
    }
    val nextZikirName = remember(nextZikir, state.settings.lang) {
        if (nextZikir != null) ZikirContent.getZikirName(nextZikir.id, state.settings.lang) else ""
    }

    LaunchedEffect(currentZikir.id, currentZikir.count) {
        if (currentZikir.count == 0L) {
            viewModel.checkAndShowInitialSpiritualInfo(currentZikir.id)
        }
    }

    val remaining = (currentZikir.target - currentZikir.count).coerceAtLeast(0L)
    val progressPercent = if (currentZikir.target > 0) {
        (currentZikir.count.toFloat() / currentZikir.target.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "circular_progress"
    )

    val animatedVirdProgress by animateFloatAsState(
        targetValue = (state.todayPercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "vird_progress"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (state.settings.fullScreenTap || state.isZenMode) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.incrementCount(1L)
                    }
                } else Modifier
            )
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Responsive boyut hesaplamaları (Tablette merkezileştirilmiş ve kompakt, mobilde tam doluluk)
        val isWide = screenWidth >= 600.dp
        val isCompactHeight = screenHeight < 680.dp
        val horizontalPad = if (screenWidth < 360.dp) 6.dp else if (isWide) 16.dp else 10.dp
        val verticalPad = if (isCompactHeight) 4.dp else 8.dp

        // Çember çapı ekran yüksekliği, genişliği ve aktif font ölçeğine duyarlı dinamik hesaplama
        val currentFontScale = LocalDensity.current.fontScale
        val calculatedRingSize: Dp = if (isWide) {
            if (currentFontScale > 1.25f) 360.dp else 400.dp
        } else if (isCompactHeight || currentFontScale > 1.25f) {
            val scaleReduction = if (currentFontScale > 1.35f) 0.38f else if (currentFontScale > 1.15f) 0.40f else 0.44f
            (screenHeight * scaleReduction).coerceIn(190.dp, 260.dp)
        } else {
            min(screenWidth * 0.95f, screenHeight * 0.5f).coerceIn(250.dp, 380.dp)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPad, vertical = verticalPad),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 540.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (state.isZenMode) Arrangement.Center else Arrangement.SpaceBetween
            ) {

                // ==========================================
                // 1. ANA KALP ALANI (BAŞLIK, DİKEYLEMESİNE ORTALI ALTIN ÇEMBER, VİRD VE DURUM) - FORCED SYNC
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = colors.card,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (state.isZenMode) Modifier.fillMaxHeight() else Modifier.weight(1f))
                        .shadow(
                            elevation = if (colors.isDark) 14.dp else 8.dp,
                            shape = RoundedCornerShape(26.dp),
                            spotColor = colors.gold.copy(alpha = 0.35f),
                            ambientColor = colors.primary.copy(alpha = 0.20f)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = if (screenWidth < 360.dp) 10.dp else 14.dp,
                                vertical = if (isCompactHeight) 8.dp else 12.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {

                        // A. Üst Kontrol Butonları (Sol Üst: Titreşim Kademesi, Sağ Üst: Odaklanma Modu)
                        if (!state.isZenMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sol Üst: 3 Kademeli Titreşim Butonu
                                val hapticIcon = HapticIcons.forMode(
                                    enabled = state.settings.hapticEnabled,
                                    tapMode = state.settings.hapticTapMode
                                )
                                val hapticDesc = when {
                                    !state.settings.hapticEnabled -> strings.hapticOff
                                    state.settings.hapticTapMode == "light" -> "${strings.hapticTitle} (${strings.hapticTapLight})"
                                    state.settings.hapticTapMode == "medium" -> "${strings.hapticTitle} (${strings.hapticTapMedium})"
                                    else -> "${strings.hapticTitle} (${strings.hapticTapStrong})"
                                }

                                IconButton(
                                    onClick = { viewModel.cycleHapticMode() },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(colors.inputBg)
                                        .border(
                                            1.dp,
                                            if (state.settings.hapticEnabled) colors.gold.copy(alpha = 0.6f) else colors.border,
                                            CircleShape
                                        )
                                        .testTag("btn_vibration_toggle")
                                ) {
                                    Icon(
                                        imageVector = hapticIcon,
                                        contentDescription = hapticDesc,
                                        tint = if (state.settings.hapticEnabled) colors.gold else colors.textMuted.copy(alpha = 0.5f),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // Sağ Üst: Zen / Odaklanma Modu Butonu
                                IconButton(
                                    onClick = { viewModel.toggleZenMode(true) },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(colors.inputBg)
                                        .border(1.dp, colors.border, CircleShape)
                                        .testTag("btn_zen_mode")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Fullscreen,
                                        contentDescription = strings.zenMode,
                                        tint = colors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // B. DİKEYLEMESİNE KUSURSUZCA ORTALANMIŞ 2026 ULTRA LÜKS 3D ZİKİR ÇEMBERİ
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            val displayCount = if (state.settings.countdownMode) remaining else currentZikir.count
                            LuxuryDhikrCircle(
                                ringSize = calculatedRingSize,
                                progress = progressPercent,
                                displayCount = displayCount,
                                targetCount = currentZikir.target,
                                isCountdownMode = state.settings.countdownMode,
                                remainingLabel = strings.remainingZikir,
                                arabicText = arabicText,
                                transliteration = transliteration,
                                lang = state.settings.lang,
                                onTap = {
                                    viewModel.incrementCount(1L)
                                }
                            )
                        }

                        // C. KARTIN EN ALTINDAKİ ALAN: BUGÜNKÜ VİRD VE DURUM ŞERİDİ (Butonların hemen üstünde)
                        if (!state.isZenMode) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // GÜNLÜK VİRD ÇUBUĞU (Kompakt ve Taşmayan)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = colors.inputBg,
                                    border = BorderStroke(1.dp, colors.border),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showTargetDialog = true }
                                        .testTag("box_daily_vird_counter")
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f, fill = false)
                                            ) {
                                                Text(
                                                    text = "${strings.todayVird}: ${NumberFormatter.format(state.todayRecited, state.settings.lang)} / ${NumberFormatter.format(state.settings.dailyTarget, state.settings.lang)}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = colors.text,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                SpiritualGearIcon(tint = colors.textMuted, size = 13.dp)
                                            }
                                            Text(
                                                text = "%${state.todayPercent.toInt()}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.primary
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(colors.card)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(animatedVirdProgress)
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(colors.gold, colors.primary)
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 8.dp))

                                // DURUM BİLGİ ŞERİDİ (Başlangıç, Kalan, Tahmini Bitiş)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = strings.startDate,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textMuted,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = NumberFormatter.formatDate(currentZikir.startedAt, state.settings.lang, strings.notStarted),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.text,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = strings.remainingInThis,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textMuted,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = NumberFormatter.format(remaining, state.settings.lang),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.text,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = strings.estimatedFinish,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textMuted,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = NumberFormatter.formatDate(state.currentEstimatedDate, state.settings.lang, strings.calculating),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.text,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 2. KARTIN DIŞINDAKİ HIZLI EYLEMLER & KONTROL BUTONLARI (+1000, +5000, +10000 ve GERİ AL / MANUEL / SIFIRLA)
                // ==========================================
                if (!state.isZenMode) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (isCompactHeight) 4.dp else 8.dp)
                    ) {
                        // Sadece %100 olduğunda görünen göz yormayan, zarif "Sonraki Zikre Geç" VEYA "Hatmi Tamamla" butonu
                        AnimatedVisibility(
                            visible = (isCompleted100 && nextZikir != null) || isKhatmReady,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (isKhatmReady) {
                                // Hatmi Tamamla Butonu (Ya Rahim bittiğinde ve tüm zikirler %100 olduğunda)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = colors.card,
                                    border = BorderStroke(1.4.dp, colors.gold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            viewModel.startNewRound()
                                        }
                                        .actionButtonShadow(
                                            colors = colors,
                                            shape = RoundedCornerShape(16.dp),
                                            overrideSpotColor = colors.gold.copy(alpha = 0.50f)
                                        )
                                        .testTag("btn_complete_khatm_banner")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = colors.gold.copy(alpha = 0.22f),
                                                border = BorderStroke(1.2.dp, colors.gold),
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = colors.gold,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                val khatmTitle = when (state.settings.lang.lowercase()) {
                                                    "ar" -> "ختم الورد المبارك"
                                                    "de" -> "Khatm abschließen"
                                                    "fr" -> "Terminer le Khatm"
                                                    "en" -> "Complete Khatm"
                                                    else -> "Hatmi Tamamla"
                                                }
                                                val nextRoundDesc = when (state.settings.lang.lowercase()) {
                                                    "ar" -> "بدء الجولة ${state.settings.completedRounds + 2} من كلمة التوحيد"
                                                    "de" -> "Runde ${state.settings.completedRounds + 2} starten (Kelime-i Tevhid)"
                                                    "fr" -> "Démarrer le tour ${state.settings.completedRounds + 2} (Kelime-i Tevhid)"
                                                    "en" -> "Start Round ${state.settings.completedRounds + 2} from Kelime-i Tevhid"
                                                    else -> "${state.settings.completedRounds + 2}. Tura Başla (Kelime-i Tevhid)"
                                                }
                                                Text(
                                                    text = khatmTitle,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Black,
                                                        color = colors.gold
                                                    )
                                                )
                                                Text(
                                                    text = nextRoundDesc,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.text
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = colors.gold,
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val btnActionText = when (state.settings.lang.lowercase()) {
                                                    "ar" -> "إتمام"
                                                    "de" -> "Abschließen"
                                                    "fr" -> "Terminer"
                                                    "en" -> "Complete"
                                                    else -> "Tamamla"
                                                }
                                                Text(
                                                    text = btnActionText,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Black,
                                                        color = colors.bg
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                    contentDescription = null,
                                                    tint = colors.bg,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (nextZikir != null) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = colors.card,
                                    border = BorderStroke(1.2.dp, colors.gold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            viewModel.selectZikir(nextZikir.id)
                                        }
                                        .actionButtonShadow(
                                            colors = colors,
                                            shape = RoundedCornerShape(16.dp),
                                            overrideSpotColor = colors.gold.copy(alpha = 0.40f)
                                        )
                                        .testTag("btn_next_zikir_banner")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = colors.gold.copy(alpha = 0.18f),
                                                border = BorderStroke(1.dp, colors.gold),
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = colors.gold,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                val nextLabel = when (state.settings.lang.lowercase()) {
                                                    "ar" -> "الانتقال إلى الذكر التالي"
                                                    "de" -> "Zum nächsten Zikr wechseln"
                                                    "fr" -> "Passer au dhikr suivant"
                                                    "en" -> "Proceed to Next Dhikr"
                                                    else -> "Sıradaki Zikre Başla"
                                                }
                                                Text(
                                                    text = nextLabel,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.gold
                                                    )
                                                )
                                                Text(
                                                    text = nextZikirName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.text
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = colors.gold,
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val btnActionText = when (state.settings.lang.lowercase()) {
                                                    "ar" -> "ابدأ"
                                                    "de" -> "Starten"
                                                    "fr" -> "Commencer"
                                                    "en" -> "Start"
                                                    else -> "Başla"
                                                }
                                                Text(
                                                    text = btnActionText,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.bg
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                    contentDescription = null,
                                                    tint = colors.bg,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Hızlı Ekle Butonları (+1.000, +5.000, +10.000)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1000L, 5000L, 10000L).forEach { amount ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 42.dp)
                                        .actionButtonShadow(
                                            colors = colors,
                                            shape = RoundedCornerShape(12.dp),
                                            overrideSpotColor = colors.primary.copy(alpha = if (colors.isDark) 0.55f else 0.45f)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    colors.primary.copy(alpha = if (colors.isDark) 0.95f else 0.92f),
                                                    colors.primary
                                                )
                                            )
                                        )
                                        .border(
                                            width = 0.8.dp,
                                            brush = Brush.verticalGradient(
                                                listOf(
                                                    Color.White.copy(alpha = if (colors.isDark) 0.30f else 0.40f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.incrementCount(amount) }
                                        .testTag("quick_add_$amount")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "+${NumberFormatter.format(amount, state.settings.lang)}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.White,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 6.dp))

                        // Aksiyon ve Araç Çubuğu: Geri Al, Manuel İşlem, Sıfırlama (Yüksek font ölçeklerine tam uyumlu responsive boyutlar)
                        val fontScaleFactor = LocalDensity.current.fontScale
                        val actionMinHeight = if (fontScaleFactor > 1.25f) 46.dp else 42.dp

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Geri Al Butonu
                            OutlinedButton(
                                onClick = { viewModel.undoLastAction() },
                                enabled = state.canUndo,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = colors.card,
                                    contentColor = colors.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (state.canUndo) colors.primary.copy(alpha = 0.85f) else colors.border),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .heightIn(min = actionMinHeight)
                                    .actionButtonShadow(
                                        colors = colors,
                                        shape = RoundedCornerShape(12.dp),
                                        overrideSpotColor = if (state.canUndo) colors.primary.copy(alpha = 0.35f) else null
                                    )
                                    .testTag("btn_undo")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = strings.undo, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = strings.undo,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (fontScaleFactor > 1.25f) 11.sp else MaterialTheme.typography.labelMedium.fontSize
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Manuel Sayı Ekleme Açıcı
                            OutlinedButton(
                                onClick = { showManualDialog = true },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = colors.card,
                                    contentColor = colors.text
                                ),
                                border = BorderStroke(1.dp, colors.border),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .heightIn(min = actionMinHeight)
                                    .actionButtonShadow(
                                        colors = colors,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("btn_open_manual_ops")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = strings.manualTitle, modifier = Modifier.size(14.dp), tint = colors.textMuted)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = strings.manualTitle,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (fontScaleFactor > 1.25f) 11.sp else MaterialTheme.typography.labelMedium.fontSize
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Zikri Sıfırla Butonu
                            OutlinedButton(
                                onClick = { showResetDialog = true },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = colors.card,
                                    contentColor = colors.error
                                ),
                                border = BorderStroke(1.dp, colors.error.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .heightIn(min = actionMinHeight)
                                    .actionButtonShadow(
                                        colors = colors,
                                        shape = RoundedCornerShape(12.dp),
                                        overrideSpotColor = colors.error.copy(alpha = 0.35f)
                                    )
                                    .testTag("btn_open_reset_dialog")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = strings.resetThis,
                                        modifier = Modifier.size(15.dp),
                                        tint = colors.error
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = strings.resetThis,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (fontScaleFactor > 1.25f) 11.sp else MaterialTheme.typography.labelMedium.fontSize
                                        ),
                                        color = colors.error,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Zen Modu Kapatma Butonu
            AnimatedVisibility(
                visible = state.isZenMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.card.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable { viewModel.toggleZenMode(false) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = strings.cancel,
                            tint = colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.cancel,
                            color = colors.text,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // MODAL 1: HIZLI ZİKİR & ZİKİR SEÇİM BOTTOM SHEET
    // ==========================================
    if (showZikirSelectorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showZikirSelectorSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = colors.card,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = strings.listTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.zikirs) { z ->
                        val isUnlocked = viewModel.isZikirUnlocked(z.id, state.zikirs)
                        val isSelected = z.id == currentZikir.id
                        val name = ZikirContent.getZikirName(z.id, state.settings.lang)
                        val arabic = ZikirContent.getArabicText(z.id)
                        val percent = if (z.target > 0) (z.count * 100 / z.target) else 0

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isSelected -> colors.primary.copy(alpha = 0.15f)
                                isUnlocked -> colors.inputBg
                                else -> colors.inputBg.copy(alpha = 0.4f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) colors.primary else colors.border
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isUnlocked) {
                                    viewModel.selectZikir(z.id)
                                    showZikirSelectorSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (z.count >= z.target) {
                                        SpiritualCheckIcon(tint = colors.gold, size = 18.dp)
                                    } else if (isUnlocked) {
                                        SpiritualBeadsIcon(tint = colors.primary, size = 18.dp)
                                    } else {
                                        SpiritualLockIcon(tint = colors.textMuted, size = 16.dp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                            ),
                                            color = if (isUnlocked) colors.text else colors.textMuted
                                        )
                                        Text(
                                            text = "${NumberFormatter.format(z.count, state.settings.lang)} / ${NumberFormatter.format(z.target, state.settings.lang)} (%$percent)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textMuted
                                        )
                                    }
                                }

                                Text(
                                    text = arabic,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) colors.primary else colors.textMuted
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ==========================================
    // MODAL 2: MANUEL SAYI EKLEME & ÇIKARMA DİYALOĞU
    // ==========================================
    if (showManualDialog) {
        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            title = {
                Text(
                    text = strings.manualTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = strings.manualDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = manualAmountText,
                        onValueChange = { manualAmountText = it.filter { ch -> ch.isDigit() } },
                        placeholder = { Text(strings.manualPlaceholder, color = colors.textMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.inputBg,
                            unfocusedContainerColor = colors.inputBg,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_manual_amount")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val amt = manualAmountText.toLongOrNull()
                                if (amt != null && amt > 0) {
                                    viewModel.incrementCount(amt)
                                    manualAmountText = ""
                                    showManualDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.bg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp)
                                .testTag("btn_manual_add")
                        ) {
                            Text(strings.addBtn, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val amt = manualAmountText.toLongOrNull()
                                if (amt != null && amt > 0) {
                                    viewModel.decrementCount(amt)
                                    manualAmountText = ""
                                    showManualDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.error,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp)
                                .testTag("btn_manual_remove")
                        ) {
                            Text(strings.removeBtn, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) {
                    Text(strings.cancel, color = colors.textMuted)
                }
            },
            containerColor = colors.card,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ==========================================
    // MODAL 3: SIFIRLAMA ONAY DİYALOĞU
    // ==========================================
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(strings.resetThis, fontWeight = FontWeight.Bold, color = colors.text) },
            text = { Text("$zikirName - ${strings.resetConfirm}", color = colors.textMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetCurrentZikir()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.reset, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(strings.cancel, color = colors.textMuted)
                }
            },
            containerColor = colors.card,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // ==========================================
    // MODAL 4: GÜNLÜK VİRD HEDEFİ AYARLAMA DİYALOĞU
    // ==========================================
    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = {
                Text(
                    text = strings.dailyTargetTitle,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.dailyTargetLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Stepper: -1000 [ Target ] +1000
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { viewModel.adjustDailyTarget(-1000L) },
                            enabled = state.settings.dailyTarget > 1000L,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.inputBg,
                                contentColor = colors.text
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Text("− ${NumberFormatter.format(1000L, state.settings.lang)}", fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 13.sp)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = NumberFormatter.format(state.settings.dailyTarget, state.settings.lang),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = strings.perDay,
                                style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted),
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = { viewModel.adjustDailyTarget(1000L) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.bg
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ ${NumberFormatter.format(1000L, state.settings.lang)}", fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hazır Ön Tanımlı Hedefler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(1000L, 3000L, 5000L, 10000L, 20000L).forEach { target ->
                            val isSelected = state.settings.dailyTarget == target
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) colors.primary else colors.inputBg,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) colors.primary else colors.border
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setDailyTarget(target) }
                            ) {
                                Text(
                                    text = NumberFormatter.format(target, state.settings.lang),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) colors.bg else colors.text,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTargetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.save, color = colors.bg, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colors.card,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
