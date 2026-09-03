package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppStrings
import com.example.data.model.Badge
import com.example.ui.theme.LocalAppColors
import com.example.util.NumberFormatter

/**
 * Nişanlara tıklandığında hem kazanılmış hem de henüz kilitli olan nişanların
 * şartlarını, ilerlemesini ve manevi hikmetini detaylıca gösteren diyalog penceresi.
 */
@Composable
fun BadgeDetailDialog(
    badge: Badge,
    lang: String,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)
    val scaleAnim = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .testTag("badge_detail_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = colors.card,
                border = BorderStroke(
                    1.5.dp,
                    if (badge.isUnlocked) colors.gold else colors.border.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .scale(scaleAnim.value)
                    .shadow(
                        elevation = if (badge.isUnlocked) 18.dp else 8.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = if (badge.isUnlocked) colors.gold else Color.Black
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Category & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (badge.isUnlocked) colors.gold.copy(alpha = 0.15f) else colors.primary.copy(alpha = 0.08f),
                            border = BorderStroke(
                                1.dp,
                                if (badge.isUnlocked) colors.gold.copy(alpha = 0.4f) else colors.border.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = if (badge.category == "istikrar") strings.badgeCategoryStreak else strings.badgeCategoryTerkip,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (badge.isUnlocked) colors.gold else colors.textMuted,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_close_badge_detail")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.closeBtn,
                                tint = colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badge Icon Circle
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                if (badge.isUnlocked) {
                                    Brush.radialGradient(
                                        listOf(
                                            colors.gold.copy(alpha = 0.35f),
                                            colors.primary.copy(alpha = 0.15f),
                                            colors.card
                                        )
                                    )
                                } else {
                                    Brush.radialGradient(
                                        listOf(
                                            colors.textMuted.copy(alpha = 0.15f),
                                            colors.card
                                        )
                                    )
                                }
                            )
                            .border(
                                width = 2.dp,
                                color = if (badge.isUnlocked) colors.gold else colors.border,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgeHeroIcon(
                            badgeId = badge.id,
                            tint = if (badge.isUnlocked) colors.gold else colors.textMuted,
                            size = 46.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Status Pill (Unlocked / Locked)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (badge.isUnlocked) Color(0xFF1B5E20).copy(alpha = 0.18f) else colors.textMuted.copy(alpha = 0.12f),
                        border = BorderStroke(
                            1.dp,
                            if (badge.isUnlocked) Color(0xFF4CAF50).copy(alpha = 0.6f) else colors.border
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (badge.isUnlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (badge.isUnlocked) Color(0xFF4CAF50) else colors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (badge.isUnlocked) strings.badgeStatusUnlocked else strings.badgeStatusLocked,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = if (badge.isUnlocked) Color(0xFF4CAF50) else colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description
                    Text(
                        text = badge.desc,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Requirements and Progress Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.inputBg,
                        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.badgeRequirement,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = colors.textMuted
                                )
                                Text(
                                    text = "%${NumberFormatter.formatNumber(badge.progressPercent.toLong(), lang)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    ),
                                    color = if (badge.isUnlocked) colors.gold else colors.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = badge.requirement,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                color = colors.primary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { (badge.progressPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (badge.isUnlocked) colors.gold else colors.primary,
                                trackColor = colors.border.copy(alpha = 0.3f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Button
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.bg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_dismiss_badge_modal")
                    ) {
                        Text(
                            text = strings.understandClose,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
