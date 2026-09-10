package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.data.model.AppStrings
import com.example.data.model.Zikir
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.actionButtonShadow
import com.example.util.NumberFormatter

/**
 * Zikir Sayacı Hızlı Eylemler Bölümü (+1.000, +5.000, +10.000 ve Sonraki Zikir/Hatmi Tamamla Bannerı)
 */
@Composable
fun QuickActionsSection(
    settings: AppSettings,
    isCompleted100: Boolean,
    isKhatmReady: Boolean,
    nextZikir: Zikir?,
    nextZikirName: String,
    onQuickAdd: (Long) -> Unit,
    onCompleteKhatm: () -> Unit,
    onSelectNextZikir: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(settings.lang)

    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Sonraki Zikre Geç veya Hatmi Tamamla Bannerı
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
                        .clickable { onCompleteKhatm() }
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
                                val khatmTitle = when (settings.lang.lowercase()) {
                                    "ar" -> "ختم الورد المبارك"
                                    "de" -> "Khatm abschließen"
                                    "fr" -> "Terminer le Khatm"
                                    "en" -> "Complete Khatm"
                                    else -> "Hatmi Tamamla"
                                }
                                val nextRoundDesc = when (settings.lang.lowercase()) {
                                    "ar" -> "بدء الجولة ${settings.completedRounds + 2} من كلمة التوحيد"
                                    "de" -> "Runde ${settings.completedRounds + 2} starten (Kelime-i Tevhid)"
                                    "fr" -> "Démarrer le tour ${settings.completedRounds + 2} (Kelime-i Tevhid)"
                                    "en" -> "Start Round ${settings.completedRounds + 2} from Kelime-i Tevhid"
                                    else -> "${settings.completedRounds + 2}. Tura Başla (Kelime-i Tevhid)"
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
                                val btnActionText = when (settings.lang.lowercase()) {
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
                        .clickable { onSelectNextZikir(nextZikir.id) }
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
                                val nextLabel = when (settings.lang.lowercase()) {
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
                                val btnActionText = when (settings.lang.lowercase()) {
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

        // 2. Hızlı Ekle Butonları (+1.000, +5.000, +10.000)
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
                        .heightIn(min = 48.dp)
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
                        .clickable { onQuickAdd(amount) }
                        .testTag("quick_add_$amount")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+${NumberFormatter.format(amount, settings.lang)}",
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
    }
}
