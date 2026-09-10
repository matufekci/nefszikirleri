package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.data.model.Badge
import com.example.data.model.ZikirContent
import com.example.ui.components.BadgeDetailDialog
import com.example.ui.theme.LocalAppColors
import com.example.ui.viewmodel.DhikrUiState
import com.example.util.NumberFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Genel İstatistikler ve Toplam Metrikler Bölümü (Toplam Zikir, Hız, Projeksiyon, Rozetler, Geçmiş ve Sıfırlama)
 */
@Composable
fun OverallStatsSection(
    state: DhikrUiState,
    badgesExpanded: Boolean,
    logsExpanded: Boolean,
    onToggleBadges: () -> Unit,
    onToggleLogs: () -> Unit,
    onResetAllZikirs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(state.settings.lang)

    var selectedBadge by remember { mutableStateOf<Badge?>(null) }
    var showResetAllDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. MANEVİ İSTİKRAR ROZETLERİ (Foldable / Collapsible Card)
        StatCollapsibleCard(
            title = strings.badgeTitle,
            icon = Icons.Rounded.EmojiEvents,
            isExpanded = badgesExpanded,
            onToggle = onToggleBadges
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = strings.badgesEarnedCount
                                .replace("{0}", state.badges.count { it.isUnlocked }.toString())
                                .replace("{1}", state.badges.size.toString()),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.badges.forEach { badge ->
                        BadgeItem(
                            badge = badge,
                            strings = strings,
                            colors = colors,
                            onClick = { selectedBadge = badge }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. SON İŞLEM GEÇMİŞİ (Recent Activity Logs)
        StatCollapsibleCard(
            title = strings.recentActivityTitle,
            icon = Icons.Rounded.History,
            isExpanded = logsExpanded,
            onToggle = onToggleLogs
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.card,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${state.history.size} ${strings.recordCount}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val groupedHistory = remember(state.history) { groupHistory(state.history).take(15) }
                    var expandedGroups by remember { mutableStateOf(setOf<Long>()) }
                    if (groupedHistory.isEmpty()) {
                        Text(
                            text = strings.noActivityYet,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    } else {
                        groupedHistory.forEachIndexed { idx, group ->
                            val zName = ZikirContent.getZikirName(group.zikirId, state.settings.lang)
                            val timeStr = try {
                                val sdf = SimpleDateFormat("dd.MM HH:mm:ss", Locale.getDefault())
                                sdf.format(Date(group.lastTimestamp))
                            } catch (e: Exception) { "" }
                            val isExpanded = expandedGroups.contains(group.id)
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clickable {
                                            if (group.items.size > 1) {
                                                expandedGroups = if (isExpanded) {
                                                    expandedGroups - group.id
                                                } else {
                                                    expandedGroups + group.id
                                                }
                                            }
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (group.type == "add") colors.primary.copy(alpha = 0.15f) else colors.error.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (group.type == "add") "＋" else "−",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = if (group.type == "add") colors.primary else colors.error
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = zName,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = colors.text
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = timeStr,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = colors.textMuted
                                                )
                                                if (group.items.size > 1) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (isExpanded) "▲" else "▼",
                                                        fontSize = 8.sp,
                                                        color = colors.textMuted
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${if (group.type == "add") "+" else "-"}${NumberFormatter.format(group.totalAmount, state.settings.lang)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = if (group.type == "add") colors.primary else colors.error
                                        )
                                    )
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(modifier = Modifier.padding(start = 34.dp, bottom = 4.dp)) {
                                        group.items.forEach { subItem ->
                                            val subTimeStr = try {
                                                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                                sdf.format(Date(subItem.timestamp))
                                            } catch (e: Exception) { "" }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "↳ $subTimeStr", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                                                Text(text = "+1", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.primary.copy(alpha=0.7f))
                                            }
                                        }
                                    }
                                }
                            }
                            if (idx < groupedHistory.size - 1) {
                                HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. TÜM ZİKİRLERİ SIFIRLAMA BUTONU
        OutlinedButton(
            onClick = { showResetAllDialog = true },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .heightIn(min = 48.dp)
                .testTag("btn_reset_all_zikirs")
        ) {
            Icon(
                imageVector = Icons.Rounded.RestartAlt,
                contentDescription = strings.resetAll,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = strings.resetAll,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 11.sp)
            )
        }
    }

    // Modal 1: Badge Detail Dialog
    selectedBadge?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            lang = state.settings.lang,
            onDismiss = { selectedBadge = null }
        )
    }

    // Modal 2: Reset All Zikirs Confirmation Dialog
    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            title = {
                Text(
                    text = strings.resetAll,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text
                )
            },
            text = {
                Text(
                    text = strings.resetAllConfirm,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllZikirs()
                        showResetAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.reset, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllDialog = false }) {
                    Text(strings.cancel, color = colors.textMuted)
                }
            },
            containerColor = colors.card,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

/**
 * Toplam Zikir KPI Kartı
 */
@Composable
fun TotalRecitedCard(
    totalDone: Long,
    lang: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border),
        modifier = modifier.heightIn(min = 95.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Numbers,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = strings.statTotalRecited,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = NumberFormatter.format(totalDone, lang),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = colors.primary
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * Genel Günlük Hız KPI Kartı
 */
@Composable
fun OverallSpeedCard(
    overallAveragePerDay: Long,
    overallRemaining: Long,
    lang: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border),
        modifier = modifier.heightIn(min = 95.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = strings.overallSpeed,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${NumberFormatter.format(overallAveragePerDay, lang)} ${strings.perDayShort}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = colors.primary
                ),
                maxLines = 1
            )
            Text(
                text = "${strings.totalRemaining} ${NumberFormatter.format(overallRemaining, lang)}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                maxLines = 1
            )
        }
    }
}

/**
 * Terkip Bitiş Projeksiyonu KPI Kartı
 */
@Composable
fun CompletionProjectionCard(
    overallEstimatedDate: Long?,
    completedCount: Int,
    lang: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border),
        modifier = modifier.heightIn(min = 95.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.HourglassBottom,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = strings.allTerkipFinish,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = NumberFormatter.formatDate(overallEstimatedDate ?: 0L, lang, strings.calculating),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = colors.primaryVariant
                ),
                maxLines = 1
            )
            Text(
                text = "$completedCount / 15 ${strings.levelFinished}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
    }
}
