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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppStrings
import com.example.data.model.Zikir
import com.example.data.model.ZikirContent
import com.example.ui.theme.LocalAppColors
import com.example.util.NumberFormatter

/**
 * İstatistikler veya Terkip Zikirleri listesindeki herhangi bir zikirye (1-15) tıklandığında
 * zikirnin durumunu (Aktif/Sırada/Bitti), Arapça lafzını, hedefini, kalanını ve ilerlemesini gösteren modal.
 */
@Composable
fun TerkipDetailDialog(
    zikir: Zikir,
    activeLevelId: Int,
    lang: String,
    onSelectZikir: (Int) -> Unit,
    onFastJump: ((Int) -> Unit)? = null,
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

    val isDone = zikir.count >= zikir.target
    val isActive = zikir.id == activeLevelId
    val isLocked = zikir.id > activeLevelId && !isDone
    val localizedName = ZikirContent.getZikirName(zikir.id, lang)
    val arabicText = ZikirContent.getArabicText(zikir.id)
    val progress = (zikir.count.toFloat() / zikir.target.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progress * 100).toInt()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .testTag("terkip_detail_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = colors.card,
                border = BorderStroke(
                    1.5.dp,
                    if (isActive) colors.gold else colors.border.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .scale(scaleAnim.value)
                    .shadow(
                        elevation = if (isActive) 18.dp else 8.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = if (isActive) colors.gold else Color.Black
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Level ID & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${NumberFormatter.formatNumber(zikir.id.toLong(), lang)}. ${strings.terkipLevelTitle}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_close_terkip_detail")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.closeBtn,
                                tint = colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Arabic Calligraphy in Rounded Box
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = colors.inputBg,
                        border = BorderStroke(1.dp, colors.gold.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = arabicText,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 36.sp
                                ),
                                color = colors.gold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = localizedName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = colors.text,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Indicator
                    val statusText = when {
                        isDone -> strings.statusDone
                        isActive -> strings.statusActive
                        else -> strings.statusWaiting
                    }
                    val statusColor = when {
                        isDone -> Color(0xFF4CAF50)
                        isActive -> colors.gold
                        else -> colors.textMuted
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    isDone -> Icons.Rounded.CheckCircle
                                    isActive -> Icons.Rounded.PlayArrow
                                    else -> Icons.Rounded.Lock
                                },
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Details
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
                                    text = strings.totalDone,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = colors.textMuted
                                )
                                Text(
                                    text = "${NumberFormatter.formatNumber(zikir.count, lang)} / ${NumberFormatter.formatNumber(zikir.target, lang)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    ),
                                    color = colors.text
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isDone) Color(0xFF4CAF50) else if (isActive) colors.gold else colors.primary,
                                trackColor = colors.border.copy(alpha = 0.3f),
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.remainingZikir,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = colors.textMuted
                                )
                                Text(
                                    text = NumberFormatter.formatNumber((zikir.target - zikir.count).coerceAtLeast(0L), lang),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = colors.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Buttons
                    if (!isLocked) {
                        Button(
                            onClick = {
                                onSelectZikir(zikir.id)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.bg
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_select_terkip_level")
                        ) {
                            Text(
                                text = strings.selectZikirPrompt,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.lockReasonMsg,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    textAlign = TextAlign.Center
                                ),
                                color = colors.textMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            if (onFastJump != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onFastJump(zikir.id)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.gold,
                                        contentColor = Color.Black
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("btn_fast_jump_terkip")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Bolt,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = strings.fastJumpBtn,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
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
}
