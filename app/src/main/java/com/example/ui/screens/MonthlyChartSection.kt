package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors
import com.example.ui.viewmodel.DayChartItem

/**
 * 30 Günlük Aktivite Isı Haritası Bölümü (Monthly Activity Heatmap)
 */
@Composable
fun MonthlyChartSection(
    last30Days: List<DayChartItem>,
    lang: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    StatCollapsibleCard(
        title = strings.heatmapTitle,
        icon = Icons.Rounded.CalendarMonth,
        isExpanded = isExpanded,
        onToggle = onToggle,
        modifier = modifier
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
                val chunkedDays = last30Days.chunked(7)
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    chunkedDays.forEach { week ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                                        .weight(1f)
                                        .aspectRatio(1f)
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
                            val remainingInWeek = (7 - week.size).coerceAtLeast(0)
                            repeat(remainingInWeek) {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(1.dp)
                                )
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
}
