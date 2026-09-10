package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Badge
import com.example.data.model.UiTranslations
import com.example.data.model.ZikirHistory
import com.example.ui.components.BadgeHeroIcon
import com.example.ui.theme.AppThemeColors
import com.example.ui.theme.LocalAppColors

/**
 * Katlanabilir İstatistik Kartı Container'ı
 */
@Composable
fun StatCollapsibleCard(
    title: String,
    icon: ImageVector? = null,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = colors.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        modifier = modifier
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

/**
 * Vakit Yoğunluğu İkonu
 */
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

/**
 * Rozet Öğesi Kartı
 */
@Composable
fun BadgeItem(
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

/**
 * Geçmiş Kayıtlarını Gruplama Modeli ve Fonksiyonu
 */
data class HistoryGroup(
    val id: Long,
    val zikirId: Int,
    val type: String,
    val totalAmount: Long,
    val lastTimestamp: Long,
    val items: List<ZikirHistory>
)

fun groupHistory(history: List<ZikirHistory>): List<HistoryGroup> {
    val groups = mutableListOf<HistoryGroup>()
    var currentGroup = mutableListOf<ZikirHistory>()
    for (item in history) {
        if (currentGroup.isEmpty()) {
            currentGroup.add(item)
        } else {
            val last = currentGroup.last()
            if (item.zikirId == last.zikirId && item.type == last.type && item.type == "add" && item.amount == 1L && last.amount == 1L) {
                currentGroup.add(item)
            } else {
                groups.add(
                    HistoryGroup(
                        id = currentGroup.first().id,
                        zikirId = currentGroup.first().zikirId,
                        type = currentGroup.first().type,
                        totalAmount = currentGroup.sumOf { it.amount },
                        lastTimestamp = currentGroup.first().timestamp,
                        items = currentGroup.toList()
                    )
                )
                currentGroup.clear()
                currentGroup.add(item)
            }
        }
    }
    if (currentGroup.isNotEmpty()) {
        groups.add(
            HistoryGroup(
                id = currentGroup.first().id,
                zikirId = currentGroup.first().zikirId,
                type = currentGroup.first().type,
                totalAmount = currentGroup.sumOf { it.amount },
                lastTimestamp = currentGroup.first().timestamp,
                items = currentGroup.toList()
            )
        )
    }
    return groups
}
