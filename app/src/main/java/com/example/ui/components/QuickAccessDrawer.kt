package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.data.model.Zikir
import com.example.data.model.ZikirContent
import com.example.ui.theme.LocalAppColors

@Composable
fun QuickAccessDrawer(
    isOpen: Boolean,
    zikirs: List<Zikir>,
    selectedId: Int,
    lang: String,
    themeName: String,
    hapticEnabled: Boolean,
    fullScreenTap: Boolean,
    onSelectZikir: (Int) -> Unit,
    onToggleHaptic: () -> Unit,
    onToggleFullScreenTap: () -> Unit,
    onCycleTheme: () -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable { onClose() },
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                color = colors.card,
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                modifier = Modifier
                    .width(268.dp)
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) {}
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                        spotColor = colors.gold.copy(alpha = 0.35f)
                    )
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                colors.gold.copy(alpha = 0.6f),
                                colors.border,
                                colors.gold.copy(alpha = 0.3f)
                            )
                        ),
                        RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = colors.gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.sidebarTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            ),
                            color = colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${strings.quickSwitch}:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // 15 Zikirs List
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        zikirs.forEach { zikir ->
                            val isSelected = zikir.id == selectedId
                            val isUnlocked = zikir.id <= 1 || (1 until zikir.id).all { prevId ->
                                val prev = zikirs.find { it.id == prevId }
                                prev != null && prev.count >= prev.target
                            }
                            val itemBg = if (isSelected) colors.primary else if (!isUnlocked) colors.inputBg.copy(alpha = 0.5f) else colors.inputBg
                            val itemTextColor = if (isSelected) colors.bg else if (!isUnlocked) colors.textMuted.copy(alpha = 0.6f) else colors.text
                            val zName = ZikirContent.getZikirName(zikir.id, lang)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(itemBg)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(1.dp, colors.gold, RoundedCornerShape(10.dp))
                                        } else {
                                            Modifier.border(0.5.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        }
                                    )
                                    .clickable {
                                        onSelectZikir(zikir.id)
                                        onClose()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${zikir.id}. $zName",
                                    color = itemTextColor,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (zikir.count >= zikir.target) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = strings.completedBadge,
                                        tint = if (isSelected) colors.bg else Color(0xFF22C55E),
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = strings.badgeStatusLocked,
                                        tint = if (isSelected) colors.bg.copy(alpha = 0.6f) else colors.textMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Action Tools
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (hapticEnabled) {
                                        Brush.verticalGradient(
                                            listOf(
                                                colors.primary,
                                                colors.primary.copy(alpha = 0.85f)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(
                                                colors.inputBg,
                                                colors.inputBg.copy(alpha = 0.7f)
                                            )
                                        )
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (hapticEnabled) colors.gold else colors.border.copy(alpha = 0.6f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onToggleHaptic() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Vibration,
                                contentDescription = strings.hapticTitle,
                                tint = if (hapticEnabled) colors.bg else colors.text,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (fullScreenTap) {
                                        Brush.verticalGradient(
                                            listOf(
                                                colors.primary,
                                                colors.primary.copy(alpha = 0.85f)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(
                                                colors.inputBg,
                                                colors.inputBg.copy(alpha = 0.7f)
                                            )
                                        )
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (fullScreenTap) colors.gold else colors.border.copy(alpha = 0.6f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onToggleFullScreenTap() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TouchApp,
                                contentDescription = strings.fullscreenTap,
                                tint = if (fullScreenTap) colors.bg else colors.text,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            colors.inputBg,
                                            colors.inputBg.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                                .border(1.dp, colors.border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .clickable { onCycleTheme() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = strings.themeTitle,
                                tint = colors.text,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Close Button
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.bg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(14.dp),
                                spotColor = colors.primary.copy(alpha = 0.4f)
                            )
                    ) {
                        Text(
                            text = strings.closeBtn,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

