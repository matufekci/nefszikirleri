package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fiziksel ve botanik özelliklere göre modellenmiş Gerçekçi Şeffaf Gül Yaprağı (Realistic Transparent Rose Petal).
 * 3 Boyutlu yuvarlanma (3D Tumble), rüzgar salınımı (Wind Flutter), yaprak damarları ve çok katmanlı ışık geçirgenliği içerir.
 */
data class RosePetalSpec(
    val baseAngleDeg: Float,
    val radiusRatio: Float,
    val widthDp: Float,
    val heightDp: Float,
    val orbitSpeed: Float,
    val tumbleSpeed: Float,
    val spinSpeed: Float,
    val curlFactor: Float,
    val baseAlpha: Float
)

object RealisticRosePetalEngine {

    // Çember etrafında organik bir derinlikle süzülen 12 adet farklı boyut ve hızdaki gül yaprağı
    val petalSpecs = listOf(
        RosePetalSpec(baseAngleDeg = 12f, radiusRatio = 1.08f, widthDp = 18f, heightDp = 24f, orbitSpeed = 0.95f, tumbleSpeed = 1.20f, spinSpeed = 0.85f, curlFactor = 1.0f, baseAlpha = 0.82f),
        RosePetalSpec(baseAngleDeg = 44f, radiusRatio = 0.95f, widthDp = 13f, heightDp = 18f, orbitSpeed = 1.10f, tumbleSpeed = 1.45f, spinSpeed = -0.90f, curlFactor = -0.8f, baseAlpha = 0.65f),
        RosePetalSpec(baseAngleDeg = 78f, radiusRatio = 1.14f, widthDp = 22f, heightDp = 29f, orbitSpeed = 0.88f, tumbleSpeed = 0.95f, spinSpeed = 0.70f, curlFactor = 1.2f, baseAlpha = 0.88f),
        RosePetalSpec(baseAngleDeg = 112f, radiusRatio = 0.92f, widthDp = 15f, heightDp = 20f, orbitSpeed = 1.05f, tumbleSpeed = 1.30f, spinSpeed = -1.10f, curlFactor = -1.0f, baseAlpha = 0.70f),
        RosePetalSpec(baseAngleDeg = 145f, radiusRatio = 1.06f, widthDp = 20f, heightDp = 26f, orbitSpeed = 0.92f, tumbleSpeed = 1.15f, spinSpeed = 0.80f, curlFactor = 0.9f, baseAlpha = 0.85f),
        RosePetalSpec(baseAngleDeg = 178f, radiusRatio = 1.16f, widthDp = 14f, heightDp = 19f, orbitSpeed = 1.15f, tumbleSpeed = 1.50f, spinSpeed = -0.95f, curlFactor = -0.7f, baseAlpha = 0.68f),
        RosePetalSpec(baseAngleDeg = 210f, radiusRatio = 0.96f, widthDp = 21f, heightDp = 28f, orbitSpeed = 0.86f, tumbleSpeed = 1.05f, spinSpeed = 0.75f, curlFactor = 1.1f, baseAlpha = 0.86f),
        RosePetalSpec(baseAngleDeg = 242f, radiusRatio = 1.10f, widthDp = 16f, heightDp = 21f, orbitSpeed = 1.02f, tumbleSpeed = 1.35f, spinSpeed = -0.85f, curlFactor = -1.1f, baseAlpha = 0.72f),
        RosePetalSpec(baseAngleDeg = 275f, radiusRatio = 0.93f, widthDp = 19f, heightDp = 25f, orbitSpeed = 0.94f, tumbleSpeed = 1.10f, spinSpeed = 0.90f, curlFactor = 0.8f, baseAlpha = 0.80f),
        RosePetalSpec(baseAngleDeg = 308f, radiusRatio = 1.15f, widthDp = 12f, heightDp = 17f, orbitSpeed = 1.18f, tumbleSpeed = 1.60f, spinSpeed = -1.05f, curlFactor = -0.9f, baseAlpha = 0.62f),
        RosePetalSpec(baseAngleDeg = 338f, radiusRatio = 1.05f, widthDp = 23f, heightDp = 30f, orbitSpeed = 0.84f, tumbleSpeed = 0.90f, spinSpeed = 0.65f, curlFactor = 1.3f, baseAlpha = 0.90f),
        RosePetalSpec(baseAngleDeg = 358f, radiusRatio = 0.97f, widthDp = 14f, heightDp = 19f, orbitSpeed = 1.08f, tumbleSpeed = 1.40f, spinSpeed = -0.80f, curlFactor = -1.0f, baseAlpha = 0.68f)
    )

    /**
     * Tekil bir ultra-gerçekçi şeffaf gül yaprağını fiziksel 3D açısıyla çizer.
     */
    fun drawSingleRealisticPetal(
        drawScope: DrawScope,
        center: Offset,
        widthPx: Float,
        heightPx: Float,
        tumbleDeg: Float,
        spinDeg: Float,
        flutterRad: Float,
        curlFactor: Float,
        isDark: Boolean,
        alpha: Float
    ) {
        val hw = (widthPx / 2f).coerceAtLeast(1f)
        val hh = (heightPx / 2f).coerceAtLeast(1f)

        // 3D Yuvarlanma İllüzyonu (Pitch/Roll: Yaprak havada dönerken incelir ve diğer yüzü görünür)
        val tumbleRad = Math.toRadians(tumbleDeg.toDouble())
        val cosTumble = cos(tumbleRad).toFloat()
        val sinTumble = sin(tumbleRad).toFloat()

        // Minimum 0.08f genişlik bırakılarak kenar çizgisi (edge-on) hissi verilir
        val scaleX = if (abs(cosTumble) < 0.08f) 0.08f * (if (cosTumble >= 0) 1f else -1f) else cosTumble
        val scaleY = (1.0f + 0.12f * sinTumble).coerceIn(0.85f, 1.15f)

        val isBackFacing = scaleX < 0f
        // Gündüz ve gece temalarında güllerin narin, hafif ve şeffaf süzülmesi için saydamlık artırıldı
        val themeTranslucencyFactor = if (isDark) 0.42f else 0.38f
        val effectiveAlpha = (alpha * themeTranslucencyFactor * (if (isBackFacing) 0.82f else 1.0f)).coerceIn(0f, 1f)

        drawScope.rotate(degrees = spinDeg, pivot = center) {
            drawScope.scale(scaleX = scaleX, scaleY = scaleY, pivot = center) {

                // 1. Organik Gül Yaprağı Yolu (Organic Anatomical Rose Petal Contour)
                val petalPath = Path().apply {
                    val baseP = Offset(center.x, center.y + hh)
                    val tipP = Offset(center.x + (curlFactor * hw * 0.12f), center.y - hh * 0.88f)

                    moveTo(baseP.x, baseP.y)

                    // Sağ kavisli yanak (Genişleyen ve incelen kadife kenar)
                    cubicTo(
                        center.x + hw * (1.05f + curlFactor * 0.08f), center.y + hh * 0.35f,
                        center.x + hw * (1.15f - curlFactor * 0.05f), center.y - hh * 0.28f,
                        center.x + hw * 0.70f, center.y - hh * 0.78f
                    )

                    // Tepe girintisi (Doğal kalp biçimli narin gül kıvrımı)
                    cubicTo(
                        center.x + hw * 0.35f, center.y - hh * 1.02f,
                        tipP.x + hw * 0.10f, center.y - hh * 0.98f,
                        tipP.x, tipP.y
                    )

                    // Sol tepe kıvrımı
                    cubicTo(
                        tipP.x - hw * 0.10f, center.y - hh * 0.98f,
                        center.x - hw * 0.35f, center.y - hh * 1.02f,
                        center.x - hw * 0.70f, center.y - hh * 0.78f
                    )

                    // Sol kavisli yanak
                    cubicTo(
                        center.x - hw * (1.15f + curlFactor * 0.05f), center.y - hh * 0.28f,
                        center.x - hw * (1.05f - curlFactor * 0.08f), center.y + hh * 0.35f,
                        baseP.x, baseP.y
                    )
                    close()
                }

                // 0. 3D Yumuşak Zemin Gölgesi (Narin, yarı-saydam temas gölgesi)
                if (!isDark) {
                    val shadowOffset = Offset(center.x + 1.0.dp.toPx(), center.y + 1.5.dp.toPx())
                    drawPath(
                        path = petalPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF881337).copy(alpha = effectiveAlpha * 0.12f),
                                Color(0xFFBE123C).copy(alpha = effectiveAlpha * 0.06f),
                                Color.Transparent
                            ),
                            center = shadowOffset,
                            radius = hh * 1.35f
                        )
                    )
                }

                // 2. Renk ve Işık Geçirgenlik Gradyanı (Subsurface Scattering & Translucency)
                val petalBrush = if (isDark) {
                    // GÜL SİYAH: Koyu kadife yakut, derin gül şarabı ve tül gibi zarif saydam geçiş
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE11D48).copy(alpha = effectiveAlpha * 0.65f), // Aydınlık merkez ışığı
                            Color(0xFF9F1239).copy(alpha = effectiveAlpha * 0.72f), // Zengin kadife yakut
                            Color(0xFF4C0519).copy(alpha = effectiveAlpha * 0.55f), // Koyu kenar derinliği
                            Color(0xFF881337).copy(alpha = effectiveAlpha * 0.35f)  // Şeffaf dış geçiş
                        ),
                        center = Offset(center.x, center.y - hh * 0.1f),
                        radius = hh * 1.4f
                    )
                } else {
                    // GÜL BEYAZ: İpeksi porselen fildişi, narin pudra pembe ve tül saydamlığında carmine kenarlar
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFF7F9).copy(alpha = effectiveAlpha * 0.75f), // İpeksi porselen göbek
                            Color(0xFFFDE8ED).copy(alpha = effectiveAlpha * 0.68f), // Narin pudra gül
                            Color(0xFFF472B6).copy(alpha = effectiveAlpha * 0.58f), // Canlı pembe geçiş
                            Color(0xFFE11D48).copy(alpha = effectiveAlpha * 0.48f), // Zengin carmine gül kenarı
                            Color(0xFFBE123C).copy(alpha = effectiveAlpha * 0.35f)  // Belirgin dış allık
                        ),
                        center = Offset(center.x, center.y - hh * 0.15f),
                        radius = hh * 1.25f
                    )
                }

                // Ana gövde dolgusu (Translucent Velvet Body)
                drawPath(
                    path = petalPath,
                    brush = petalBrush
                )

                // 3. Yaprak Damarları (Botanical Vein Network)
                val veinColor = if (isDark) {
                    Color(0xFFFDA4AF).copy(alpha = effectiveAlpha * 0.25f)
                } else {
                    Color(0xFF9F1239).copy(alpha = effectiveAlpha * 0.30f)
                }
                val strokeWidth = 0.85.dp.toPx()

                // Ana Orta Damar (Central Midrib)
                val veinPath = Path().apply {
                    moveTo(center.x, center.y + hh * 0.85f)
                    quadraticBezierTo(
                        center.x + curlFactor * hw * 0.15f, center.y,
                        center.x + curlFactor * hw * 0.08f, center.y - hh * 0.70f
                    )
                }
                drawPath(
                    path = veinPath,
                    color = veinColor,
                    style = Stroke(width = strokeWidth * 1.1f, cap = StrokeCap.Round)
                )

                // Yan Kılcal Damarlar (Secondary Lateral Veins)
                val lateralVeinColor = veinColor.copy(alpha = veinColor.alpha * 0.65f)
                val latStroke = strokeWidth * 0.75f

                // Sağ yan damarlar
                drawLine(
                    color = lateralVeinColor,
                    start = Offset(center.x, center.y + hh * 0.45f),
                    end = Offset(center.x + hw * 0.55f, center.y + hh * 0.20f),
                    strokeWidth = latStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = lateralVeinColor,
                    start = Offset(center.x, center.y + hh * 0.10f),
                    end = Offset(center.x + hw * 0.65f, center.y - hh * 0.15f),
                    strokeWidth = latStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = lateralVeinColor,
                    start = Offset(center.x, center.y - hh * 0.25f),
                    end = Offset(center.x + hw * 0.48f, center.y - hh * 0.50f),
                    strokeWidth = latStroke,
                    cap = StrokeCap.Round
                )

                // Sol yan damarlar
                drawLine(
                    color = lateralVeinColor,
                    start = Offset(center.x, center.y + hh * 0.45f),
                    end = Offset(center.x - hw * 0.55f, center.y + hh * 0.20f),
                    strokeWidth = latStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = lateralVeinColor,
                    start = Offset(center.x, center.y + hh * 0.10f),
                    end = Offset(center.x - hw * 0.65f, center.y - hh * 0.15f),
                    strokeWidth = latStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = lateralVeinColor,
                    start = Offset(center.x, center.y - hh * 0.25f),
                    end = Offset(center.x - hw * 0.48f, center.y - hh * 0.50f),
                    strokeWidth = latStroke,
                    cap = StrokeCap.Round
                )

                // 4. Kıvrık Kenar Işıltısı (Curled Edge & Dewdrop Specular Highlight)
                val edgeHighlightColor = if (isDark) {
                    Color(0xFFFFF1F2).copy(alpha = effectiveAlpha * 0.35f)
                } else {
                    Color(0xFFBE123C).copy(alpha = effectiveAlpha * 0.40f)
                }

                // Kenar boyunca vuran belirgin kontur çizgisi
                drawPath(
                    path = petalPath,
                    color = edgeHighlightColor,
                    style = Stroke(width = 0.7.dp.toPx())
                )

                // Çiğ Damlası / Kristal Işıltı (Micro Dew Specular Dot)
                val dewCenter = Offset(center.x + hw * 0.22f, center.y - hh * 0.35f)
                drawCircle(
                    color = (if (isDark) Color.Black else Color(0xFF4C0519)).copy(alpha = effectiveAlpha * 0.22f),
                    radius = 1.3.dp.toPx(),
                    center = Offset(dewCenter.x, dewCenter.y + 0.5.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = effectiveAlpha * 0.65f),
                    radius = 1.0.dp.toPx(),
                    center = dewCenter
                )
            }
        }
    }
}
