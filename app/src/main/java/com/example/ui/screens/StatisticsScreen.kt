package com.example.ui.screens
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
fun StatisticsScreen(
    state: DhikrUiState,
    viewModel: ZikirViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = AppStrings.get(state.settings.lang)
    var chartRange by remember { mutableStateOf(0) } // 0: 7 Gün, 1: 30 Gün, 2: 6 Ay
    var chartExpanded by remember { mutableStateOf(true) }
    var heatmapExpanded by remember { mutableStateOf(true) }
    var timeDensityExpanded by remember { mutableStateOf(true) }
    var badgesExpanded by remember { mutableStateOf(true) }
    var logsExpanded by remember { mutableStateOf(true) }
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }
    var showResetAllDialog by remember { mutableStateOf(false) }
    // Active Sequential Zikir (First uncompleted or 15)
    val activeZikir = state.zikirs.firstOrNull { it.count < it.target } ?: state.zikirs.lastOrNull()
    val totalTargetSum = state.zikirs.sumOf { it.target }.coerceAtLeast(1L)
    val totalProgressRatio = (state.totalDone.toFloat() / totalTargetSum.toFloat()).coerceIn(0f, 1f)
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
// 3. KPI METRİK KARTLARI (2x2 Grid)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Toplam Zikir
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 95.dp)
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
                        text = NumberFormatter.format(state.totalDone, state.settings.lang),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = colors.primary
                        ),
                        maxLines = 1
                    )
                }
            }
            // Seri / Streak
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 95.dp)
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
                        SpiritualFlameIcon(tint = colors.gold, size = 14.dp)
                        Text(
                            text = strings.streakCardTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${state.streak} ${strings.streakDay}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = colors.gold
                            ),
                            maxLines = 1
                        )
                    }
                    Text(
                        text = strings.bestStreakLabel.replace("{0}", NumberFormatter.formatNumber(state.bestStreak.toLong(), state.settings.lang)),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Genel Günlük Hız
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 95.dp)
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
                        text = "${NumberFormatter.format(state.overallAveragePerDay, state.settings.lang)} ${strings.perDayShort}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = colors.primary
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "${strings.totalRemaining} ${NumberFormatter.format(state.overallRemaining, state.settings.lang)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted,
                        maxLines = 1
                    )
                }
            }
            // Terkip Bitiş Projeksiyonu
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 95.dp)
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
                        text = NumberFormatter.formatDate(state.overallEstimatedDate, state.settings.lang, strings.calculating),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = colors.primaryVariant
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "${state.completedCount} / 15 ${strings.levelFinished}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // 4. İNTERAKTİF ÇOK DÖNEMLİ GRAFİK (7 Gün / 30 Gün / 6 Ay) (Foldable / Collapsible)
        StatCollapsibleCard(
            title = strings.chartTitle,
            icon = Icons.Rounded.ShowChart,
            isExpanded = chartExpanded,
            onToggle = { chartExpanded = !chartExpanded }
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Range Switcher (Sağa yaslı)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.inputBg)
                                .padding(2.dp)
                        ) {
                            listOf(strings.range7Days, strings.range30Days, strings.range6Months).forEachIndexed { idx, label ->
                                val isSelected = chartRange == idx
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) colors.primary else Color.Transparent)
                                        .clickable { chartRange = idx }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) colors.bg else colors.textMuted
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    when (chartRange) {
                        0 -> {
                            // 7 Days
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                state.last7Days.forEach { item ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (item.amount > 0) NumberFormatter.format(item.amount, state.settings.lang) else "-",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textMuted,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .height((100 * item.ratio).dp)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(if (item.amount > 0) colors.primary else colors.border)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.text
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            // 30 Days (Compact Sparkline bars)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                state.last30Days.forEachIndexed { idx, item ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(5.dp)
                                                .height((100 * item.ratio).dp)
                                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                                .background(if (item.amount > 0) colors.primary else colors.border.copy(alpha = 0.5f))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (idx % 5 == 0) {
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colors.textMuted
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // 6 Months
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                state.last6Months.forEach { item ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (item.amount > 0) NumberFormatter.format(item.amount, state.settings.lang) else "-",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textMuted,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(26.dp)
                                                .height((100 * item.ratio).dp)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(if (item.amount > 0) colors.primaryVariant else colors.border)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.text
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // 5. GÜNLÜK GAYRET ISI HARİTASI (30-Day Activity Heatmap) (Foldable / Collapsible)
        StatCollapsibleCard(
            title = strings.heatmapTitle,
            icon = Icons.Rounded.CalendarMonth,
            isExpanded = heatmapExpanded,
            onToggle = { heatmapExpanded = !heatmapExpanded }
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // 30 Günlük Grid (7 sütun x 5 satır)
                    val chunkedDays = state.last30Days.chunked(7)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        chunkedDays.forEach { week ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                week.forEach { dayItem ->
                                    val cellColor = when {
                                        dayItem.amount == 0L -> colors.inputBg
                                        dayItem.ratio < 0.3f -> colors.primary.copy(alpha = 0.35f)
                                        dayItem.ratio < 0.7f -> colors.primary.copy(alpha = 0.7f)
                                        else -> colors.primary
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(5.dp),
                                        color = cellColor,
                                        border = androidx.compose.foundation.BorderStroke(
                                            0.5.dp,
                                            if (dayItem.amount > 0) colors.primary.copy(alpha = 0.5f) else colors.border.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier
                                            .size(36.dp)
                                            .padding(1.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = dayItem.label.takeLast(2),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (dayItem.amount > 0 && dayItem.ratio >= 0.7f) colors.bg else colors.textMuted
                                                )
                                            )
                                        }
                                    }
                                }
                                // Kalan boşlukları doldur
                                repeat(7 - week.size) {
                                    Spacer(modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Heatmap Lejantı (Az -> Çok)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.heatmapLess,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        listOf(
                            colors.inputBg,
                            colors.primary.copy(alpha = 0.35f),
                            colors.primary.copy(alpha = 0.7f),
                            colors.primary
                        ).forEach { color ->
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = color,
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(1.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = strings.heatmapMore,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // 6. VAKİT YOĞUNLUK DAĞILIMI (Time of Day Heatmap) (Foldable / Collapsible)
        StatCollapsibleCard(
            title = strings.timeDensity,
            icon = Icons.Rounded.Schedule,
            isExpanded = timeDensityExpanded,
            onToggle = { timeDensityExpanded = !timeDensityExpanded }
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        state.timeSlots.forEachIndexed { idx, slot ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) {
                                Text(
                                    text = "%${(slot.percentage * 100).toInt()}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.primary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .weight(1f)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(colors.inputBg),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(if (slot.percentage > 0f) slot.percentage else 0.02f)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(colors.primary)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                TimeSlotCustomIcon(idx = idx, tint = colors.primary)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // 7. MANEVİ İSTİKRAR ROZETLERİ (Foldable / Collapsible Card)
        StatCollapsibleCard(
            title = strings.badgeTitle,
            icon = Icons.Rounded.EmojiEvents,
            isExpanded = badgesExpanded,
            onToggle = { badgesExpanded = !badgesExpanded }
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
        // 8. SON İŞLEM GEÇMİŞİ (Recent Activity Logs - İstikrar Nişanlarının Hemen Altında)
        StatCollapsibleCard(
            title = strings.recentActivityTitle,
            icon = Icons.Rounded.History,
            isExpanded = logsExpanded,
            onToggle = { logsExpanded = !logsExpanded }
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.card,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
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
                                
                                androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
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
        // 9. TÜM ZİKİRLERİ SIFIRLAMA BUTONU (Kompakt / Zarif Boyut)
        OutlinedButton(
            onClick = { showResetAllDialog = true },
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .heightIn(min = 40.dp)
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
        Spacer(modifier = Modifier.heightIn(min = 40.dp))
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
                        viewModel.resetAllZikirs()
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
@Composable
private fun BadgeItem(
    badge: Badge,
    strings: UiTranslations,
    colors: AppThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (badge.isUnlocked) colors.primary.copy(alpha = 0.08f) else colors.inputBg.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (badge.isUnlocked) colors.gold.copy(alpha = 0.5f) else colors.border.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (badge.isUnlocked) 2.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = colors.gold.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("badge_item_${badge.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeHeroIcon(
                badgeId = badge.id,
                tint = if (badge.isUnlocked) colors.gold else colors.textMuted,
                size = 40.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = if (badge.isUnlocked) colors.text else colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = badge.desc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = if (badge.isUnlocked) colors.primary else colors.textMuted.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (badge.isUnlocked) colors.gold.copy(alpha = 0.18f) else colors.inputBg,
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (badge.isUnlocked) colors.gold.copy(alpha = 0.4f) else colors.border.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (badge.isUnlocked) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = colors.gold,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = strings.badgeStatusUnlocked,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = colors.gold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = strings.badgeStatusLocked,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            color = colors.textMuted
                        )
                    }
                }
            }
        }
    }
}
data class HistoryGroup(
    val id: Long,
    val zikirId: Int,
    val type: String,
    val totalAmount: Long,
    val lastTimestamp: Long,
    val items: List<com.example.data.model.ZikirHistory>
)
fun groupHistory(history: List<com.example.data.model.ZikirHistory>): List<HistoryGroup> {
    val groups = mutableListOf<HistoryGroup>()
    var currentGroup = mutableListOf<com.example.data.model.ZikirHistory>()
    for (item in history) {
        if (currentGroup.isEmpty()) {
            currentGroup.add(item)
        } else {
            val last = currentGroup.last()
            if (item.zikirId == last.zikirId && item.type == last.type && item.type == "add" && item.amount == 1L && last.amount == 1L) {
                currentGroup.add(item)
            } else {
                groups.add(HistoryGroup(
                    id = currentGroup.first().id,
                    zikirId = currentGroup.first().zikirId,
                    type = currentGroup.first().type,
                    totalAmount = currentGroup.sumOf { it.amount },
                    lastTimestamp = currentGroup.first().timestamp,
                    items = currentGroup.toList()
                ))
                currentGroup.clear()
                currentGroup.add(item)
            }
        }
    }
    if (currentGroup.isNotEmpty()) {
        groups.add(HistoryGroup(
            id = currentGroup.first().id,
            zikirId = currentGroup.first().zikirId,
            type = currentGroup.first().type,
            totalAmount = currentGroup.sumOf { it.amount },
            lastTimestamp = currentGroup.first().timestamp,
            items = currentGroup.toList()
        ))
    }
    return groups
}
@Composable
private fun StatCollapsibleCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = colors.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .clip(RoundedCornerShape(22.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = colors.primary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))
                content()
            }
        }
    }
}
@Composable
fun TimeSlotCustomIcon(idx: Int, tint: Color, modifier: Modifier = Modifier) {
    val vectorIcon = when (idx) {
        0 -> Icons.Rounded.WbTwilight
        1 -> Icons.Rounded.WbSunny
        2 -> Icons.Rounded.NightsStay
        3 -> Icons.Rounded.DarkMode
        else -> Icons.Rounded.WbSunny
    }

    Box(
        modifier = modifier
            .size(26.dp)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                spotColor = tint.copy(alpha = 0.5f),
                ambientColor = tint.copy(alpha = 0.2f)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(tint.copy(alpha = 0.22f), tint.copy(alpha = 0.05f), Color.Transparent)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = vectorIcon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
    }
}
