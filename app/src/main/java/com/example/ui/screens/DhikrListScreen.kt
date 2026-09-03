package com.example.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.text.*
import androidx.compose.ui.text.input.*
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*
import com.example.util.*
import android.content.*
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import com.example.ui.components.*


@Composable
fun DhikrListScreen(
    state: DhikrUiState,
    viewModel: ZikirViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(state.settings.lang)
    var selectedTerkipForStatusDialog by remember { mutableStateOf<Zikir?>(null) }
    var selectedZikirForVirtueDialog by remember { mutableStateOf<Zikir?>(null) }

    val totalZikirsCount = state.zikirs.size
    val totalProgressPercent = if (totalZikirsCount > 0) {
        (state.completedCount.toFloat() / totalZikirsCount.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedOverallProgress by animateFloatAsState(
        targetValue = totalProgressPercent,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "overall_progress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 15 ZİKİR SİLSİLE YOLU & KARTLARI
        itemsIndexed(state.zikirs, key = { _, zikir -> zikir.id }) { index, zikir ->
            val zName = ZikirContent.getZikirName(zikir.id, state.settings.lang)
            val zikirDef = ZikirContent.getZikirDefinition(zikir.id)
            val arabicText = zikirDef.arabicText
            val isSelected = zikir.id == state.selectedId
            val isCompleted = zikir.count >= zikir.target
            val isUnlocked = viewModel.isZikirUnlocked(zikir.id)
            val progress = if (zikir.target > 0) (zikir.count.toFloat() / zikir.target.toFloat()).coerceIn(0f, 1f) else 0f
            val remainingCount = (zikir.target - zikir.count).coerceAtLeast(0L)

            // Kalan gün ve tahmini bitiş hesabı
            val estDaysBadge: String? = if (isUnlocked && !isCompleted && zikir.count > 0) {
                val dailyAvg = if (state.currentAveragePerDay > 0) state.currentAveragePerDay else state.settings.dailyTarget.coerceAtLeast(1000L)
                val daysLeft = if (dailyAvg > 0) (remainingCount + dailyAvg - 1) / dailyAvg else 0L
                if (daysLeft in 1..999) {
                    "~${NumberFormatter.formatNumber(daysLeft, state.settings.lang)} ${strings.dayUnit}"
                } else null
            } else null

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ==========================================
                // SOL: SİLSİLE / YOL ÇİZGİSİ (Timeline Stepper)
                // ==========================================
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    // Üst Bağlantı Çizgisi
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight(0.5f)
                                .align(Alignment.TopCenter)
                                .background(
                                    if (isCompleted || isSelected) colors.primary.copy(alpha = 0.6f) else colors.border.copy(alpha = 0.4f)
                                )
                        )
                    }

                    // Alt Bağlantı Çizgisi
                    if (index < state.zikirs.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight(0.5f)
                                .align(Alignment.BottomCenter)
                                .background(
                                    if (isCompleted) colors.primary.copy(alpha = 0.6f) else colors.border.copy(alpha = 0.4f)
                                )
                        )
                    }

                    // Düğüm Noktası (Node Circle)
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isSelected -> colors.primary
                            isCompleted -> colors.gold
                            isUnlocked -> colors.inputBg
                            else -> colors.border.copy(alpha = 0.35f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            when {
                                isSelected -> colors.primaryVariant
                                isCompleted -> colors.gold
                                else -> colors.border
                            }
                        ),
                        modifier = Modifier
                            .size(if (isSelected) 22.dp else 18.dp)
                            .shadow(if (isSelected || isCompleted) 3.dp else 0.dp, CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = strings.completedBadge,
                                    tint = colors.bg,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else if (!isUnlocked) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = strings.badgeStatusLocked,
                                    tint = colors.textMuted.copy(alpha = 0.6f),
                                    modifier = Modifier.size(10.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) colors.bg else colors.primary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // ==========================================
                // SAĞ: ZİKİR KARTI (Akordiyon / Daraltılabilir)
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when {
                        isSelected -> colors.primary.copy(alpha = 0.08f)
                        isCompleted -> colors.card
                        else -> colors.card
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.6.dp else 1.dp,
                        color = when {
                            isSelected -> colors.primary
                            isCompleted -> colors.gold.copy(alpha = 0.5f)
                            else -> colors.border
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            selectedTerkipForStatusDialog = zikir
                        }
                        .testTag("list_zikir_card_${zikir.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        // Üst Başlık Satırı
                        val listFontScale = LocalDensity.current.fontScale
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = zName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = if (listFontScale > 1.25f) 14.sp else MaterialTheme.typography.titleMedium.fontSize
                                            ),
                                            color = colors.text,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (isCompleted) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = strings.completedBadge,
                                                tint = colors.gold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = arabicText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.primary,
                                            fontSize = if (listFontScale > 1.25f) 13.sp else MaterialTheme.typography.bodyMedium.fontSize
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Sağ Alan: İlerleme & Aksiyonlar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (estDaysBadge != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = colors.inputBg,
                                        border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.primary.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = estDaysBadge,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = colors.primary
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = colors.primary.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.primary)
                                    ) {
                                        Text(
                                            text = strings.tabZikir,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = colors.primary
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Yüzde Göstergesi
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isCompleted) colors.gold.copy(alpha = 0.15f) else colors.inputBg
                                ) {
                                    Text(
                                        text = "%${(progress * 100).toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCompleted) colors.gold else colors.primary
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                // Manevi Faziletler Bilgi Dialog Butonu
                                IconButton(
                                    onClick = { selectedZikirForVirtueDialog = zikir },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(colors.inputBg)
                                        .testTag("info_btn_${zikir.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Info,
                                        contentDescription = strings.zikirInfoModalTitle,
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Detayları Her Zaman Göster
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${NumberFormatter.format(zikir.count, state.settings.lang)} / ${NumberFormatter.format(zikir.target, state.settings.lang)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        // İlerleme Çubuğu
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(colors.border.copy(alpha = 0.4f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isCompleted) colors.gold else colors.primary
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

    // 1. Zikrin Durumu Modalı (Kart Tıklaması)
    selectedTerkipForStatusDialog?.let { zikir ->
        TerkipDetailDialog(
            zikir = zikir,
            activeLevelId = state.selectedId,
            lang = state.settings.lang,
            onSelectZikir = { id ->
                viewModel.selectZikir(id)
                viewModel.setTab("zikir")
                selectedTerkipForStatusDialog = null
            },
            onFastJump = { targetId ->
                viewModel.openFastJumpDialog(targetId)
                selectedTerkipForStatusDialog = null
            },
            onDismiss = { selectedTerkipForStatusDialog = null }
        )
    }

    // 2. Manevi Faziletler Modalı (Sağdaki Bilgi Butonu Tıklaması)
    selectedZikirForVirtueDialog?.let { zikir ->
        ZikirInfoDialog(
            zikirId = zikir.id,
            lang = state.settings.lang,
            targetCount = zikir.target,
            onDismiss = { selectedZikirForVirtueDialog = null }
        )
    }
}

