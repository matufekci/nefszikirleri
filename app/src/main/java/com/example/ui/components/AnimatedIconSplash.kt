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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Kıvılcım yerleşimi: açı (derece), merkeze uzaklık (dp), yarıçap (dp), faz kayması. */
private data class SparkleSpec(
    val angleDeg: Float,
    val distDp: Float,
    val sizeDp: Float,
    val phase: Float
)

/** Dört köşeli yıldız (ışıltı) çizer. */
private fun DrawScope.drawSparkle(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path()
    val k = r * 0.14f
    path.moveTo(cx, cy - r)
    path.quadraticBezierTo(cx + k, cy - k, cx + r, cy)
    path.quadraticBezierTo(cx + k, cy + k, cx, cy + r)
    path.quadraticBezierTo(cx - k, cy + k, cx - r, cy)
    path.quadraticBezierTo(cx - k, cy - k, cx, cy - r)
    path.close()
    drawPath(path, color)
}

/**
 * İlk açılış ekranı: veri yüklenirken (isHydrated == false) uygulamanın
 * kendi ikonu ışıltılı, tam animasyonlu gösterilir.
 *
 * Katmanlar (alttan üste):
 * 1. Çift katmanlı nefes alan radyal parlama (dış halo + sıkı çekirdek)
 * 2. İkonun çevresinde yanıp sönen dört köşeli yıldız kıvılcımları
 * 3. Ters yönde dönen ince ikinci halka (derinlik hissi)
 * 4. Zikir halkası motifli ana çift yay
 * 5. İkon: spring ile zıplayarak belirir, hafif eğiklikten düzelir,
 *    üzerinden eğik bir ışık süpürmesi (shine sweep) geçer, kenarında
 *    nefes alan ince altın çerçeve parlar
 * 6. Başlık: gecikmeli belirir, üzerinde yavaş bir altın shimmer kayar
 *
 * "Hareketi azalt" erişilebilirlik ayarı açıkken tüm animasyonlar statik
 * tek kareye iner (kıvılcımlar ve süpürme kapalı, ikon tam boyutta).
 *
 * GÜVENLİ İKON YÜKLEME: Launcher mipmap'leri (ic_launcher_round) API 26+
 * cihazlarda adaptive-icon XML → layer-list → JPG zincirinden çözülür ve bu
 * zincir Compose painterResource ile cihazda çökmeye yol açtı (cihaz raporu,
 * commit d6b05e4). Bu yüzden ikon doğrudan ham görsel asset'inden
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
    val iconRotation = remember { Animatable(if (reduceMotion) 0f else -7f) }
    val glowAlpha = remember { Animatable(if (reduceMotion) 0.55f else 0f) }
    val ringAngle = remember { Animatable(0f) }
    val sparklePhase = remember { Animatable(if (reduceMotion) 0.25f else 0f) }
    val shimmer = remember { Animatable(0f) }
    var titleVisible by remember { mutableStateOf(reduceMotion) }

    // Kıvılcım yerleşimi — deterministik (her kompozisyonda aynı)
    val sparkles = remember {
        listOf(
            SparkleSpec(angleDeg = -62f, distDp = 96f, sizeDp = 8f, phase = 0.00f),
            SparkleSpec(angleDeg = 28f, distDp = 104f, sizeDp = 6f, phase = 0.18f),
            SparkleSpec(angleDeg = 148f, distDp = 92f, sizeDp = 7f, phase = 0.36f),
            SparkleSpec(angleDeg = 205f, distDp = 100f, sizeDp = 5.5f, phase = 0.54f),
            SparkleSpec(angleDeg = 88f, distDp = 112f, sizeDp = 6.5f, phase = 0.72f),
            SparkleSpec(angleDeg = -14f, distDp = 86f, sizeDp = 5f, phase = 0.88f)
        )
    }

    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            launch {
                iconScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessVeryLow
                    )
                )
            }
            launch {
                iconRotation.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessVeryLow
                    )
                )
            }
            launch {
                while (true) {
                    glowAlpha.animateTo(0.85f, tween(4500, easing = FastOutSlowInEasing))
                    glowAlpha.animateTo(0.35f, tween(4500, easing = FastOutSlowInEasing))
                }
            }
            launch {
                while (true) {
                    ringAngle.animateTo(360f, tween(12500, easing = LinearEasing))
                    ringAngle.snapTo(0f)
                }
            }
            launch {
                while (true) {
                    sparklePhase.animateTo(1f, tween(7800, easing = LinearEasing))
                    sparklePhase.snapTo(0f)
                }
            }
            launch {
                while (true) {
                    shimmer.animateTo(1f, tween(6600, easing = LinearEasing))
                    delay(2100)
                    shimmer.snapTo(0f)
                }
            }
            launch {
                delay(700)
                titleVisible = true
            }
        }
    }

    val gold = lerp(primary, Color(0xFFFFE9B0), 0.55f)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                // 1) Çift katmanlı nefes alan parlama
                Canvas(modifier = Modifier.size(230.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primary.copy(alpha = 0.38f * glowAlpha.value),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                gold.copy(alpha = 0.26f * glowAlpha.value),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension * 0.30f
                    )
                }
                // 2) Yanıp sönen yıldız kıvılcımları
                if (!reduceMotion) {
                    Canvas(modifier = Modifier.size(248.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val sparkleColor = lerp(primary, Color.White, 0.35f)
                        sparkles.forEach { spec ->
                            val tw = 0.5f + 0.5f *
                                sin((sparklePhase.value + spec.phase) * 2f * PI.toFloat())
                            val alpha = 0.10f + 0.80f * tw * tw * tw
                            val r = spec.sizeDp.dp.toPx() * (0.55f + 0.45f * tw)
                            val rad = spec.angleDeg * PI.toFloat() / 180f
                            val sx = cx + spec.distDp.dp.toPx() * cos(rad)
                            val sy = cy + spec.distDp.dp.toPx() * sin(rad)
                            drawSparkle(cx = sx, cy = sy, r = r, color = sparkleColor.copy(alpha = alpha))
                        }
                    }
                }
                // 3) Ters yönde dönen ince ikinci halka
                Canvas(
                    modifier = Modifier
                        .size(168.dp)
                        .rotate(-ringAngle.value * 0.6f)
                ) {
                    val stroke = 1.5.dp.toPx()
                    val arcSize = Size(size.width - stroke * 2f, size.height - stroke * 2f)
                    drawArc(
                        color = gold.copy(alpha = 0.38f),
                        startAngle = 60f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(stroke, stroke),
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = gold.copy(alpha = 0.18f),
                        startAngle = 240f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(stroke, stroke),
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                // 4) Ana çift yay (zikir halkası motifi)
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
                // 5) İkon + ışık süpürmesi + nefes alan çerçeve
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(112.dp)
                        .scale(iconScale.value)
                        .rotate(iconRotation.value)
                        .clip(CircleShape)
                ) {
                    if (iconPainter != null) {
                        Image(
                            painter = iconPainter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(primary))
                    }
                    Canvas(modifier = Modifier.matchParentSize()) {
                        // Nefes alan ince altın çerçeve
                        drawCircle(
                            color = gold.copy(alpha = 0.16f + 0.14f * glowAlpha.value),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        // Eğik ışık süpürmesi (shine sweep)
                        if (!reduceMotion) {
                            val w = size.width
                            val h = size.height
                            val bandW = w * 0.55f
                            val bandLeft = -w * 0.9f + shimmer.value * (w * 2.1f)
                            rotate(degrees = 20f) {
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.30f),
                                            Color.Transparent
                                        ),
                                        start = Offset(bandLeft, 0f),
                                        end = Offset(bandLeft + bandW, 0f)
                                    ),
                                    topLeft = Offset(bandLeft, -h * 0.3f),
                                    size = Size(bandW, h * 1.6f)
                                )
                            }
                        }
                    }
                }
            }
            // 6) Başlık — tam ortanın hemen altında, süpürmeyle senkron altın parıltı
            AnimatedVisibility(
                visible = titleVisible,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 128.dp),
                enter = fadeIn(tween(1600)) + slideInVertically(tween(1600)) { it / 3 }
            ) {
                // Süpürme ortadan geçerken başlık altına çalar (0→1→0)
                val titleGlow = 1f - abs(shimmer.value * 2f - 1f)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 4.sp,
                    color = if (reduceMotion) textColor
                    else lerp(textColor, gold, 0.5f * titleGlow),
                    modifier = Modifier.padding(top = 22.dp)
                )
            }
        }
    }
}
