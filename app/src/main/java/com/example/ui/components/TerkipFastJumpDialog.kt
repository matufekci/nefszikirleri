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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.ZikirContent
import com.example.ui.theme.LocalAppColors
import com.example.util.NumberFormatter

/**
 * Hızlı İntikal (Sıçrama) Onay Dialogu.
 * Kullanıcı daha ileri bir zikirye (ör. Ya Kayyum) doğrudan geçmek istediğinde
 * önceki tüm zikirlerin otomatik tamamlanacağını açıkça bildirip onay alan şık modal.
 */
@Composable
fun TerkipFastJumpDialog(
    targetZikirId: Int,
    lang: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)
    val scaleAnim = remember { Animatable(0.75f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )
    }

    val targetName = ZikirContent.getZikirName(targetZikirId, lang)
    val targetArabic = ZikirContent.getArabicText(targetZikirId)
    val firstZikirName = ZikirContent.getZikirName(1, lang)
    val prevZikirName = ZikirContent.getZikirName((targetZikirId - 1).coerceAtLeast(1), lang)
    val previousRangeSummary = if (targetZikirId <= 2) {
        "1. $firstZikirName"
    } else {
        "1. $firstZikirName - ${targetZikirId - 1}. $prevZikirName"
    }

    val confirmMessage = strings.fastJumpConfirmMsg
        .replace("{0}", "${NumberFormatter.formatNumber(targetZikirId.toLong(), lang)}. $targetName")
        .replace("{1}", previousRangeSummary)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = colors.card,
                border = BorderStroke(1.8.dp, colors.gold.copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .scale(scaleAnim.value)
                    .shadow(20.dp, RoundedCornerShape(26.dp), spotColor = colors.gold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // İntikal İkonu
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(colors.gold.copy(alpha = 0.15f))
                            .border(2.dp, colors.gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = colors.gold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = strings.fastJumpTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = colors.gold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hedef Zikir Kartı
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = colors.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = targetArabic,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = colors.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${NumberFormatter.formatNumber(targetZikirId.toLong(), lang)}. ${strings.terkipLevelTitle}: $targetName",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                ),
                                color = colors.text,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Açıklama Metni
                    Text(
                        text = confirmMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 21.sp
                        ),
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Onayla ve Başla Butonu
                    Button(
                        onClick = {
                            onConfirm(targetZikirId)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.gold,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_confirm_fast_jump")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.fastJumpBtn,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // İptal Butonu
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_cancel_fast_jump")
                    ) {
                        Text(
                            text = strings.cancelAction,
                            color = colors.textMuted,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
