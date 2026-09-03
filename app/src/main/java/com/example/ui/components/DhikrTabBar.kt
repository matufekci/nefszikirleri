package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow

data class TabItemData(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val isCenterAction: Boolean = false
)

@Composable
private fun AutoSizingTabLabel(
    text: String,
    color: Color,
    isBold: Boolean,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val widthDp = maxWidth
        val scaledFontSize = when {
            widthDp < 52.dp && text.length > 8 -> 8.5.sp
            widthDp < 60.dp && text.length > 7 -> 9.5.sp
            text.length > 9 -> 9.5.sp
            text.length > 7 -> 10.5.sp
            else -> 11.5.sp
        }

        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = scaledFontSize,
                letterSpacing = (-0.3).sp
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DhikrBottomBar(
    currentTab: String,
    lang: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    // Zikir sekmesi tam ortada (Index 2)
    val tabs = listOf(
        TabItemData("liste", strings.tabListe, Icons.AutoMirrored.Rounded.FormatListBulleted),
        TabItemData("istatistik", strings.tabIstatistik, Icons.Rounded.Insights),
        TabItemData("zikir", strings.tabZikir, Icons.Rounded.Adjust, isCenterAction = true),
        TabItemData("bilgi", strings.tabBilgi, Icons.Rounded.AutoStories),
        TabItemData("ayarlar", strings.tabAyarlar, Icons.Rounded.Settings)
    )

    val fontScale = LocalDensity.current.fontScale
    val dockHeight = if (fontScale > 1.25f) 80.dp else 74.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Modern Capsule Dock Surface
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = colors.card.copy(alpha = 0.96f),
            border = BorderStroke(1.2.dp, colors.border.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeight)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = colors.primary.copy(alpha = 0.25f),
                    ambientColor = colors.text.copy(alpha = 0.08f)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { item ->
                    if (item.isCenterAction) {
                        // Ortadaki Zikir Butonu Alanı
                        val isSelected = currentTab == item.key
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.06f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "center_scale"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1.25f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .scale(scale)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onTabSelected(item.key) }
                                    .testTag("tab_${item.key}")
                            ) {
                                // Yükseltilmiş Belirgin Parlayan Halka
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .shadow(
                                            elevation = if (isSelected) 10.dp else 4.dp,
                                            shape = CircleShape,
                                            spotColor = if (isSelected) colors.gold else colors.primary
                                        )
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = if (isSelected) listOf(
                                                    colors.primaryVariant,
                                                    colors.primary,
                                                    colors.surface
                                                ) else listOf(
                                                    colors.primary.copy(alpha = 0.85f),
                                                    colors.card
                                                )
                                            )
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.5.dp,
                                            brush = if (isSelected) {
                                                Brush.sweepGradient(
                                                    listOf(colors.primary.copy(alpha = 0.5f), colors.primary, colors.primary.copy(alpha = 0.5f))
                                                )
                                            } else {
                                                Brush.linearGradient(
                                                    listOf(colors.primary.copy(alpha = 0.5f), colors.border)
                                                )
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SpiritualBeadsIcon(
                                        tint = if (isSelected) colors.bg else colors.primary,
                                        size = 24.dp
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                AutoSizingTabLabel(
                                    text = item.label,
                                    color = if (isSelected) colors.gold else colors.primary,
                                    isBold = true,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    } else {
                        // Yan Sekmeler (Liste, İstatistik, Bilgi, Ayarlar)
                        val isSelected = currentTab == item.key
                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) colors.primary else colors.textMuted,
                            animationSpec = tween(220),
                            label = "tab_color"
                        )
                        val tabBgColor by animateColorAsState(
                            targetValue = if (isSelected) colors.primary.copy(alpha = 0.14f) else Color.Transparent,
                            animationSpec = tween(220),
                            label = "tab_bg"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(vertical = 2.dp, horizontal = 2.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(tabBgColor)
                                .clickable { onTabSelected(item.key) }
                                .testTag("tab_${item.key}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                AutoSizingTabLabel(
                                    text = item.label,
                                    color = iconColor,
                                    isBold = isSelected,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(12.dp)
                                            .height(2.5.dp)
                                            .clip(CircleShape)
                                            .background(colors.primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DhikrNavRail(
    currentTab: String,
    lang: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    val tabs = listOf(
        TabItemData("liste", strings.tabListe, Icons.AutoMirrored.Rounded.FormatListBulleted),
        TabItemData("istatistik", strings.tabIstatistik, Icons.Rounded.Insights),
        TabItemData("zikir", strings.tabZikir, Icons.Rounded.Adjust, isCenterAction = true),
        TabItemData("bilgi", strings.tabBilgi, Icons.Rounded.AutoStories),
        TabItemData("ayarlar", strings.tabAyarlar, Icons.Rounded.Settings)
    )

    Surface(
        color = colors.card,
        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f)),
        modifier = modifier
            .width(96.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SpiritualBeadsIcon(
                tint = colors.primary,
                size = 32.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                tabs.forEach { item ->
                    val isSelected = currentTab == item.key

                    if (item.isCenterAction) {
                        // Ortadaki Zikir Butonu (Rail İçin)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) colors.primary else colors.primary.copy(alpha = 0.12f))
                                .clickable { onTabSelected(item.key) }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) colors.gold else colors.primary.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) colors.bg else colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.label,
                                color = if (isSelected) colors.bg else colors.primary,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    } else {
                        val itemBg = if (isSelected) colors.primary.copy(alpha = 0.15f) else Color.Transparent
                        val contentColor = if (isSelected) colors.primary else colors.textMuted

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(itemBg)
                                .clickable { onTabSelected(item.key) }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = contentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.label,
                                color = contentColor,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
