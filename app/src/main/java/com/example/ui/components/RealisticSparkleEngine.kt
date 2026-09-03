package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * Ultra-gerçekçi optik lens parıltısı ve elmas ışıltısı çizim fonksiyonları.
 * Düz çizgilerden ("+") oluşan yapay görünümleri gerçekçi kırınım yıldızları (diffraction spikes),
 * yumuşak ışık halesi (halo glow) ve organik Bézier kavisli optik ışık huzmelerine dönüştürür.
 */
object RealisticSparkleEngine {

    /**
     * Zikir çemberi ucu (Tip Indicator) için lüks elmas ışıltısı (Diamond Optical Flare).
     */
    fun drawDiamondTipFlare(
        drawScope: DrawScope,
        center: Offset,
        rayLength: Float,
        haloColor: Color,
        alpha: Float,
        rotationDeg: Float = 0f
    ) {
        if (rayLength <= 0.5f || alpha <= 0.01f) return

        // 1. Dış Hüzme Halesi (Diffuse Optical Glow)
        val haloRadius = rayLength * 1.6f
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha * 0.65f),
                    haloColor.copy(alpha = alpha * 0.40f),
                    Color.Transparent
                ),
                center = center,
                radius = haloRadius
            ),
            radius = haloRadius,
            center = center
        )

        drawScope.rotate(rotationDeg, pivot = center) {
            // 2. Birincil 4-Kollu Optik Kırınım Yıldızı (Primary Tapered Optical Star)
            val primaryStarPath = Path().apply {
                moveTo(center.x, center.y - rayLength)
                quadraticBezierTo(center.x, center.y, center.x + rayLength * 1.15f, center.y)
                quadraticBezierTo(center.x, center.y, center.x, center.y + rayLength)
                quadraticBezierTo(center.x, center.y, center.x - rayLength * 1.15f, center.y)
                quadraticBezierTo(center.x, center.y, center.x, center.y - rayLength)
                close()
            }
            drawScope.drawPath(
                path = primaryStarPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha * 0.95f),
                        haloColor.copy(alpha = alpha * 0.70f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = rayLength * 1.2f
                )
            )

            // 3. İkincil 45° Çapraz Kırınım Hüzmeleri (Secondary Diagonal Diffraction Rays)
            val diagRayLength = rayLength * 0.45f
            drawScope.rotate(45f, pivot = center) {
                val diagStarPath = Path().apply {
                    moveTo(center.x, center.y - diagRayLength)
                    quadraticBezierTo(center.x, center.y, center.x + diagRayLength, center.y)
                    quadraticBezierTo(center.x, center.y, center.x, center.y + diagRayLength)
                    quadraticBezierTo(center.x, center.y, center.x - diagRayLength, center.y)
                    quadraticBezierTo(center.x, center.y, center.x, center.y - diagRayLength)
                    close()
                }
                drawScope.drawPath(
                    path = diagStarPath,
                    color = Color.White.copy(alpha = alpha * 0.75f)
                )
            }
        }

        // 4. Saf Beyaz Odak Çekirdeği (Bright Specular Hotspot)
        drawScope.drawCircle(
            color = Color.White.copy(alpha = (alpha * 0.98f).coerceIn(0f, 1f)),
            radius = rayLength * 0.20f,
            center = center
        )
    }

    /**
     * Çember etrafında süzülen minyatür gerçekçi parıltı yıldızı (Micro Sparkle Star).
     * Düz "+" çizgileri yerine fotogerçekçi, zarif optik kırınım parıltısı çizer.
     */
    fun drawMicroSparkle(
        drawScope: DrawScope,
        center: Offset,
        rayLength: Float,
        color: Color,
        alpha: Float,
        rotationDeg: Float = 0f
    ) {
        if (rayLength <= 0.5f || alpha <= 0.01f) return

        // 1. Minyatür Işık Halesi (Micro Glow)
        val glowRadius = rayLength * 1.5f
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha * 0.55f),
                    color.copy(alpha = alpha * 0.35f),
                    Color.Transparent
                ),
                center = center,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = center
        )

        // 2. Kavisli 4-Köşeli Parıltı Yıldızı (Concave 4-Point Shimmer Star)
        drawScope.rotate(rotationDeg, pivot = center) {
            val starPath = Path().apply {
                moveTo(center.x, center.y - rayLength)
                quadraticBezierTo(center.x, center.y, center.x + rayLength, center.y)
                quadraticBezierTo(center.x, center.y, center.x, center.y + rayLength)
                quadraticBezierTo(center.x, center.y, center.x - rayLength, center.y)
                quadraticBezierTo(center.x, center.y, center.x, center.y - rayLength)
                close()
            }
            drawScope.drawPath(
                path = starPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha * 0.90f),
                        color.copy(alpha = alpha * 0.60f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = rayLength
                )
            )

            // İkincil çapraz mikro kırınım
            val diagLen = rayLength * 0.38f
            drawScope.rotate(45f, pivot = center) {
                val diagPath = Path().apply {
                    moveTo(center.x, center.y - diagLen)
                    quadraticBezierTo(center.x, center.y, center.x + diagLen, center.y)
                    quadraticBezierTo(center.x, center.y, center.x, center.y + diagLen)
                    quadraticBezierTo(center.x, center.y, center.x - diagLen, center.y)
                    quadraticBezierTo(center.x, center.y, center.x, center.y - diagLen)
                    close()
                }
                drawScope.drawPath(
                    path = diagPath,
                    color = Color.White.copy(alpha = alpha * 0.65f)
                )
            }
        }

        // 3. Merkez Işıltı Noktası (Hot Core Dot)
        drawScope.drawCircle(
            color = Color.White.copy(alpha = (alpha * 0.95f).coerceIn(0f, 1f)),
            radius = (rayLength * 0.18f).coerceAtLeast(0.8f),
            center = center
        )
    }
}
