package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.LocalAppColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2026 Trend Ethereal Spiritual Ambient Background
 * Tüm temalarla kusursuz uyumlu, göz yormayan, dikkat dağıtmayan
 * fakat manevi derinliği ve modern estetiği artıran hafif animasyonlu arka plan katmanı.
 */
@Composable
fun SpiritualAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_spiritual_bg")

    // Yavaş ve derin nefes alma fazı (12 saniyelik döngü)
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.025f,
        targetValue = 0.055f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_pulse"
    )

    // Çok yavaş geometrik dönüş fazı (60 saniyelik döngü)
    val slowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_rotation"
    )

    val primaryColor = colors.primary
    val goldColor = colors.gold
    val isDark = colors.isDark

    Box(modifier = modifier.fillMaxSize()) {
        // GÜNDÜZ MODU: Gözü dinlendiren, hafif kontrastlı, yumuşak mat parşömen & gölge derinliği
        if (!isDark) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Temel sıcak bej / adaçayı göz dostu mat taban
                drawRect(
                    color = colors.bg
                )

                // 2. Fiziksel Derinlik & Vinyet Gradyanı (Köşelerde yumuşak ışık düşümü)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF000000).copy(alpha = 0.022f),
                            Color(0xFF000000).copy(alpha = 0.048f)
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.42f),
                        radius = size.width * 1.05f
                    )
                )

                // 3. Hafif üst ve alt kontrast degrade derinliği (Aşırı parlamayı kırar)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF000000).copy(alpha = 0.020f),
                            Color.Transparent,
                            Color(0xFF000000).copy(alpha = 0.035f)
                        )
                    )
                )

                // 4. Çember ve kart arkası için çok hafif soft aydınlatma odağı
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.surface.copy(alpha = 0.90f),
                            colors.bg.copy(alpha = 0.95f),
                            colors.bg
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.38f),
                        radius = size.width * 0.85f
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width * 0.5f, size.height * 0.38f)
                )
            }
        } else {
            // GECE MODU: Derin atmosferik vinyet & yumuşak zemin ışık hüzmesi
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Temel zemin rengi
                drawRect(
                    color = colors.bg
                )

                // Köşe derinlik vinyeti (Obsidian / Derin Gece hissi)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF000000).copy(alpha = 0.25f),
                            Color(0xFF000000).copy(alpha = 0.55f)
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.40f),
                        radius = size.width * 1.1f
                    )
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Üst & Alt Atmosferik Işık Hareleri (Ambient Radial Glow Orbs)
            val glowMult = colors.glowIntensity / 0.40f
            val topGlowRadius = width * 0.75f
            val topCenter = Offset(width * 0.3f, height * 0.15f)
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = (pulseAlpha * (if (isDark) 1.25f else 0.75f) * glowMult).coerceIn(0f, 1f)),
                        primaryColor.copy(alpha = (pulseAlpha * 0.40f * glowMult).coerceIn(0f, 1f)),
                        Color.Transparent
                    ),
                    center = topCenter,
                    radius = topGlowRadius
                ),
                radius = topGlowRadius,
                center = topCenter
            )

            val bottomGlowRadius = width * 0.85f
            val bottomCenter = Offset(width * 0.75f, height * 0.85f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        goldColor.copy(alpha = (pulseAlpha * (if (isDark) 0.95f else 0.55f) * glowMult).coerceIn(0f, 1f)),
                        goldColor.copy(alpha = (pulseAlpha * 0.25f * glowMult).coerceIn(0f, 1f)),
                        Color.Transparent
                    ),
                    center = bottomCenter,
                    radius = bottomGlowRadius
                ),
                radius = bottomGlowRadius,
                center = bottomCenter
            )

            // 2. Çok Naif İslami Geometrik Hatlar (Subtle Sacred Geometry)
            val centerGeo = Offset(width * 0.5f, height * 0.42f)
            val geoRadius = width * 0.55f
            val rotRad = Math.toRadians(slowRotation.toDouble())

            val numPoints = if (colors.isMetallic) 12 else 8
            val path = Path()
            for (i in 0 until numPoints * 2) {
                val r = if (i % 2 == 0) geoRadius else (geoRadius * (if (colors.isMetallic) 0.75f else 0.62f))
                val angle = (i * PI / numPoints) + rotRad
                val x = centerGeo.x + (r * cos(angle)).toFloat()
                val y = centerGeo.y + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            drawPath(
                path = path,
                color = if (colors.isMetallic) colors.gold.copy(alpha = pulseAlpha * 0.65f) else primaryColor.copy(alpha = pulseAlpha * 0.5f),
                style = Stroke(width = if (colors.isMetallic) 1.4f else 1.2f)
            )

            // İç zarif daire
            drawCircle(
                color = colors.reflectionColor.copy(alpha = pulseAlpha * (if (colors.isMetallic) 0.50f else 0.35f)),
                radius = geoRadius * 0.45f,
                center = centerGeo,
                style = Stroke(width = 1f)
            )
        }

        content()
    }
}
