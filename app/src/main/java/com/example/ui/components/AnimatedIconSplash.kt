package com.example.ui.components

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * İlk açılış ekranı: veri yüklenirken (isHydrated == false) eski yuvarlak
 * spinner yerine uygulamanın kendi ikonu animasyonlu gösterilir.
 *
 * - İkon yumuşak bir zıplamayla (spring) ölçeklenerek belirir
 * - Etrafında zikir halkası motifli, yavaş dönen çift yay
 * - Arkasında nefes alan (kısılıp açılan) radyal parlama
 * - Altında uygulama adı gecikmeli olarak belirir
 *
 * "Hareketi azalt" erişilebilirlik ayarı açıkken tüm animasyonlar statik
 * tek kareye iner.
 *
 * GÜVENLİ İKON YÜKLEME: Launcher mipmap'leri (ic_launcher_round) API 26+
 * cihazlarda adaptive-icon XML → layer-list → JPG zincirinden çözülür ve bu
 * zincir Compose painterResource ile cihazda çökmeye yol açtı (cihaz raporu,
 * commit d6b05e4). Bu yüzden ikon artık doğrudan ham görsel asset'inden
 * (R.drawable.img_nefs_app_icon_1787782143783 — launcher ikonlarının da
 * kullandığı aynı çizim) BitmapPainter olarak yüklenir; yükleme runCatching
 * ile korunur ve başarısız olursa ikon yerine tema renginde basit bir daire
 * gösterilir. Bu composable kaynak yükleme nedeniyle çökemez.
 */
@Composable
internal fun AnimatedIconSplash(
    primary: Color,
    textColor: Color,
    title: String,
    reduceMotion: Boolean
) {
    val context = LocalContext.current
    val iconPainter = remember {
        runCatching {
            (context.getDrawable(R.drawable.img_nefs_app_icon_1787782143783) as? BitmapDrawable)
                ?.bitmap
                ?.asImageBitmap()
                ?.let { BitmapPainter(it) }
        }.getOrNull()
    }

    val iconScale = remember { Animatable(if (reduceMotion) 1f else 0.55f) }
    val glowAlpha = remember { Animatable(if (reduceMotion) 0.55f else 0f) }
    val ringAngle = remember { Animatable(0f) }
    var titleVisible by remember { mutableStateOf(reduceMotion) }

    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            launch {
                iconScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                while (true) {
                    glowAlpha.animateTo(0.85f, tween(1500, easing = FastOutSlowInEasing))
                    glowAlpha.animateTo(0.35f, tween(1500, easing = FastOutSlowInEasing))
                }
            }
            launch {
                while (true) {
                    ringAngle.animateTo(360f, tween(4200, easing = LinearEasing))
                    ringAngle.snapTo(0f)
                }
            }
            launch {
                delay(250)
                titleVisible = true
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                // 1) Nefes alan radyal parlama
                Canvas(modifier = Modifier.size(220.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primary.copy(alpha = 0.38f * glowAlpha.value),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2f
                    )
                }
                // 2) Yavaş dönen çift yay (zikir halkası motifi)
                Canvas(
                    modifier = Modifier
                        .size(148.dp)
                        .rotate(ringAngle.value)
                ) {
                    val stroke = 3.dp.toPx()
                    val arcSize = Size(size.width - stroke * 2f, size.height - stroke * 2f)
                    drawArc(
                        color = primary.copy(alpha = 0.85f),
                        startAngle = 0f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(stroke, stroke),
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = primary.copy(alpha = 0.30f),
                        startAngle = 180f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(stroke, stroke),
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                // 3) Uygulama ikonu (yükleme başarısızsa yedek daire)
                val iconModifier = Modifier
                    .size(112.dp)
                    .scale(iconScale.value)
                    .clip(CircleShape)
                if (iconPainter != null) {
                    Image(
                        painter = iconPainter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = iconModifier
                    )
                } else {
                    Box(modifier = iconModifier.background(primary))
                }
            }
            AnimatedVisibility(
                visible = titleVisible,
                enter = fadeIn(tween(650)) + slideInVertically(tween(650)) { it / 3 }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 3.sp,
                    color = textColor,
                    modifier = Modifier.padding(top = 22.dp)
                )
            }
        }
    }
}
