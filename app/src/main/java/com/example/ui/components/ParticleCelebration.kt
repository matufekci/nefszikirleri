package com.example.ui.components
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
import com.example.data.model.ZikirContent
import com.example.ui.theme.LocalAppColors
import com.example.ui.viewmodel.CelebrationData
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
@Composable
fun ParticleCelebrationDialog(
    data: CelebrationData,
    lang: String,
    onDismiss: () -> Unit,
    onNextZikir: (Int) -> Unit
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)
    val cardScale = remember { Animatable(0.4f) }
    val particleProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        cardScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        particleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
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
            // Floating particle burst around the dialog
            val particles = remember {
                List(16) { i ->
                    val angle = (i * 2 * Math.PI) / 16.0
                    val dist = 140.0 + (i % 4) * 30.0
                    val targetX = (cos(angle) * dist).toFloat()
                    val targetY = (sin(angle) * dist).toFloat()
                    val pIndex = i
                    Triple(targetX, targetY, pIndex)
                }
            }
            particles.forEach { (tx, ty, pIndex) ->
                val progress = particleProgress.value
                val curX = (tx * progress).toInt()
                val curY = (ty * progress).toInt()
                val alpha = (1f - progress * 0.4f).coerceIn(0f, 1f)
                val scale = (0.4f + progress * 0.8f).coerceIn(0.2f, 1.4f)
                ParticleIcon(
                    index = pIndex as Int,
                    tint = colors.gold,
                    modifier = Modifier
                        .size(32.dp)
                        .offset { IntOffset(curX, curY) }
                        .alpha(alpha)
                        .scale(scale)
                )
            }
            // Central Royal Card
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = colors.card,
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .scale(cardScale.value)
                    .border(2.dp, colors.primary, RoundedCornerShape(28.dp))
                    .padding(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        ParticleIcon(index = 0, tint = colors.gold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        ParticleIcon(index = 2, tint = colors.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        ParticleIcon(index = 0, tint = colors.gold, modifier = Modifier.size(24.dp))
                    }
                    Text(
                        text = strings.mashaallah,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = colors.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = data.zikirName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = strings.congratsDesc,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.textMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    if (data.nextZikirId != null) {
                        val nextName = ZikirContent.getZikirName(data.nextZikirId, lang)
                        OutlinedButton(
                            onClick = {
                                onNextZikir(data.nextZikirId)
                                onDismiss()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.primary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_next_zikir")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${strings.nextZikirBtn}: $nextName",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.bg
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_celebration_dismiss")
                    ) {
                        Text(
                            text = strings.alhamdulillah,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
