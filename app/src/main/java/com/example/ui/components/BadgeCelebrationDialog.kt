package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppStrings
import com.example.data.model.Badge
import com.example.ui.theme.LocalAppColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BadgeCelebrationDialog(
    badge: Badge,
    lang: String,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)
    val cardScale = remember { Animatable(0.3f) }
    val particleProgress = remember { Animatable(0f) }
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        cardScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        particleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
        )
    }

    LaunchedEffect(Unit) {
        pulseScale.animateTo(
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            // Floating burst of sparkles & celebratory vector particles
            val particles = remember {
                List(18) { i ->
                    val angle = (i * 2 * Math.PI) / 18.0
                    val dist = 135.0 + (i % 3) * 35.0
                    val targetX = (cos(angle) * dist).toFloat()
                    val targetY = (sin(angle) * dist).toFloat()
                    Triple(targetX, targetY, i)
                }
            }

            particles.forEach { (tx, ty, pIndex) ->
                val progress = particleProgress.value
                val curX = (tx * progress).toInt()
                val curY = (ty * progress).toInt()
                val alpha = (1f - progress * 0.35f).coerceIn(0f, 1f)
                val scale = (0.5f + progress * 0.75f).coerceIn(0.2f, 1.3f)

                ParticleIcon(
                    index = pIndex,
                    tint = if (pIndex % 2 == 0) colors.gold else colors.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .offset { IntOffset(curX, curY) }
                        .alpha(alpha)
                        .scale(scale)
                )
            }

            // Central Royal Card
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = colors.card,
                border = BorderStroke(2.dp, colors.gold),
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .scale(cardScale.value)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(30.dp),
                        spotColor = colors.gold
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = strings.badgeCelebrationHeader,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = colors.gold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Glowing Badge Icon
                    Box(
                        modifier = Modifier
                            .scale(pulseScale.value)
                            .size(82.dp)
                            .shadow(12.dp, CircleShape, spotColor = colors.gold)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(colors.gold.copy(alpha = 0.3f), colors.primary.copy(alpha = 0.15f), colors.card)
                                )
                            )
                            .border(2.5.dp, colors.gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgeHeroIcon(badgeId = badge.id, tint = colors.gold, size = 48.dp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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

                    Text(
                        text = badge.desc,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.inputBg,
                        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.badgeCelebrationBlessing,
                            style = MaterialTheme.typography.labelSmall.copy(
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.textMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.bg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_dismiss_badge_dialog")
                    ) {
                        Text(
                            text = strings.celebrationContinueBtn,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
