package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1. GÜL TEMASI EFEKTİ: Ethereal Gül Yaprağı Hüzmeleri & Pembe Altın Parıltılar
 */
fun DrawScope.drawRosePetalAuraEffect(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    rotationAngle: Float,
    particleAngle: Float,
    twinkle: Float,
    palette: DhikrCirclePalette,
    isDark: Boolean
) {
    // A. Narin Gül Kokusu ve Işık Halesi (Ethereal Rose Fragrance Diffusion)
    val numPetalAuras = 8
    val petalAuraRadius = radius + strokeWidth * 0.90f
    val baseAuraAlpha = if (isDark) 0.09f else 0.11f

    rotate(rotationAngle * 0.18f, pivot = center) {
        for (i in 0 until numPetalAuras) {
            val angleDeg = (i * (360f / numPetalAuras))
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val auraCenter = Offset(
                (center.x + petalAuraRadius * cos(angleRad)).toFloat(),
                (center.y + petalAuraRadius * sin(angleRad)).toFloat()
            )

            val auraGlow = strokeWidth * 2.2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        (if (isDark) Color(0xFFE11D48) else Color(0xFFFB7185)).copy(alpha = baseAuraAlpha),
                        Color.Transparent
                    ),
                    center = auraCenter,
                    radius = auraGlow
                ),
                radius = auraGlow,
                center = auraCenter
            )
        }
    }

    // B. Çember Etrafında Uçuşan Ultra-Realistic Şeffaf Gül Yaprakları (Floating 3D Translucent Rose Petals)
    RealisticRosePetalEngine.petalSpecs.forEachIndexed { index, spec ->
        // Organik Rüzgar Dalgalanması ve Yörünge Hareketi
        val orbitFlutter = sin(Math.toRadians((particleAngle * 2.2 + index * 40.0))).toFloat() * 4.5f
        val currentOrbitAngle = (particleAngle * spec.orbitSpeed + spec.baseAngleDeg + orbitFlutter) - 90f
        val angleRad = Math.toRadians(currentOrbitAngle.toDouble())

        // Çember ekseninde içeri-dışarı süzülme (Radial drift & float)
        val radialDrift = sin(Math.toRadians((particleAngle * 1.6 + index * 28.0))).toFloat() * strokeWidth * 0.28f
        val pRadius = (radius * spec.radiusRatio) + radialDrift

        val pPos = Offset(
            (center.x + pRadius * cos(angleRad)).toFloat(),
            (center.y + pRadius * sin(angleRad)).toFloat()
        )

        // 3D Yuvarlanma, Takla ve Kendi Ekseni Etrafında Dönüş (Tumble & Flutter)
        val tumbleDeg = (particleAngle * spec.tumbleSpeed * 1.8f) + (index * 55f)
        val spinFlutter = sin(Math.toRadians((particleAngle * 2.5 + index * 33.0))).toFloat() * 18f
        val spinDeg = spec.baseAngleDeg + (particleAngle * spec.spinSpeed) + spinFlutter

        val phaseAlpha = (spec.baseAlpha * (0.85f + 0.15f * twinkle)).coerceIn(0.1f, 1f)

        // Ultra-realistic şeffaf gül yaprağı render motorunu çağır
        RealisticRosePetalEngine.drawSingleRealisticPetal(
            drawScope = this,
            center = pPos,
            widthPx = spec.widthDp.dp.toPx(),
            heightPx = spec.heightDp.dp.toPx(),
            tumbleDeg = tumbleDeg,
            spinDeg = spinDeg,
            flutterRad = angleRad.toFloat(),
            curlFactor = spec.curlFactor,
            isDark = isDark,
            alpha = phaseAlpha
        )
    }
}

/**
 * 2. HADRÂ ZÜMRÜT TEMASI EFEKTİ: İlahi Nur 12 Köşeli Geometrik Yıldız Işınları & Zümrüt Parçacıklar
 */
fun DrawScope.drawHadraNoorEmeraldEffect(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    rotationAngle: Float,
    particleAngle: Float,
    twinkle: Float,
    palette: DhikrCirclePalette,
    isDark: Boolean
) {
    // A. 12 Köşeli İslami Geometrik Nur Halesi (12-Fold Sacred Noor Lattice)
    val outerRingRadius = radius + strokeWidth * 0.85f
    val baseAlpha = if (isDark) 0.35f else 0.22f

    rotate(rotationAngle * 0.15f, pivot = center) {
        val numRays = 12
        for (i in 0 until numRays) {
            val angleDeg = i * (360f / numRays)
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val rayEnd = Offset(
                (center.x + (outerRingRadius + strokeWidth * 0.6f) * cos(angleRad)).toFloat(),
                (center.y + (outerRingRadius + strokeWidth * 0.6f) * sin(angleRad)).toFloat()
            )
            val rayStart = Offset(
                (center.x + (radius + strokeWidth * 0.2f) * cos(angleRad)).toFloat(),
                (center.y + (radius + strokeWidth * 0.2f) * sin(angleRad)).toFloat()
            )

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = baseAlpha),
                        Color(0xFFFBBF24).copy(alpha = baseAlpha * 0.8f),
                        Color.Transparent
                    ),
                    start = rayStart,
                    end = rayEnd
                ),
                start = rayStart,
                end = rayEnd,
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // İnce Geometrik Dış Daire
        drawCircle(
            brush = Brush.sweepGradient(
                listOf(
                    Color(0xFF10B981).copy(alpha = baseAlpha),
                    Color(0xFFFBBF24).copy(alpha = baseAlpha * 1.2f),
                    Color(0xFF00F5A0).copy(alpha = baseAlpha * 0.8f),
                    Color(0xFF10B981).copy(alpha = baseAlpha)
                ),
                center = center
            ),
            radius = outerRingRadius,
            center = center,
            style = Stroke(width = 1.2.dp.toPx())
        )
    }

    // B. Yörüngede Dönen Zümrüt & Altın Işık Parçacıkları
    val emeraldSparkles = listOf(
        Triple(18f, 1.05f, Color(0xFFFEF08A)),
        Triple(54f, 0.94f, Color(0xFF34D399)),
        Triple(98f, 1.10f, Color(0xFFFDE047)),
        Triple(142f, 0.96f, Color(0xFF00F5A0)),
        Triple(188f, 1.06f, Color(0xFFFFF9C4)),
        Triple(230f, 0.92f, Color(0xFF10B981)),
        Triple(284f, 1.08f, Color(0xFFFBBF24)),
        Triple(326f, 0.95f, Color(0xFF6EE7B7))
    )

    emeraldSparkles.forEachIndexed { index, (baseAngle, rFactor, color) ->
        val pAngleRad = Math.toRadians((particleAngle + baseAngle + (index * 14.0)) - 90.0)
        val pRadius = radius * rFactor
        val pPos = Offset(
            (center.x + pRadius * cos(pAngleRad)).toFloat(),
            (center.y + pRadius * sin(pAngleRad)).toFloat()
        )

        val phaseTwinkle = ((twinkle + (index * 0.18f)) % 1.0f)
        val pAlpha = (0.25f + phaseTwinkle * 0.70f).coerceIn(0f, 1f)

        if (index % 2 == 0) {
            val pRay = strokeWidth * (0.30f + phaseTwinkle * 0.15f)
            RealisticSparkleEngine.drawMicroSparkle(
                drawScope = this,
                center = pPos,
                rayLength = pRay,
                color = color,
                alpha = pAlpha,
                rotationDeg = particleAngle * 1.5f + index * 30f
            )
        } else {
            drawCircle(
                color = color.copy(alpha = pAlpha),
                radius = strokeWidth * (0.09f + phaseTwinkle * 0.06f),
                center = pPos
            )
        }
    }
}

/**
 * 3. KİSVE TEMASI EFEKTİ: Kabe Kisvesi İpek Dokuma & 24K Saf Altın İşleme
 */
fun DrawScope.drawKisveGoldLatticeEffect(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    rotationAngle: Float,
    particleAngle: Float,
    twinkle: Float,
    palette: DhikrCirclePalette,
    isDark: Boolean
) {
    // A. 8 Köşeli Selçuklu / Kabe Kisvesi Altın Hat Rozeti
    val outerRingRadius = radius + strokeWidth * 0.88f
    val baseAlpha = 0.38f

    rotate(rotationAngle * 0.12f, pivot = center) {
        val numPoints = 8
        val path = Path()
        val rOuter = outerRingRadius + strokeWidth * 0.45f
        val rInner = outerRingRadius - strokeWidth * 0.20f

        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) rOuter else rInner
            val angle = (i * PI / numPoints)
            val x = center.x + (r * cos(angle)).toFloat()
            val y = center.y + (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(
            path = path,
            color = Color(0xFFD4AF37).copy(alpha = baseAlpha),
            style = Stroke(width = 1.2.dp.toPx())
        )

        // Dış konsantrik altın halka
        drawCircle(
            brush = Brush.sweepGradient(
                listOf(
                    Color(0xFFD4AF37).copy(alpha = baseAlpha * 1.2f),
                    Color(0xFFFEF3C7).copy(alpha = baseAlpha * 1.5f),
                    Color(0xFFB45309).copy(alpha = baseAlpha * 0.9f),
                    Color(0xFFD4AF37).copy(alpha = baseAlpha * 1.2f)
                ),
                center = center
            ),
            radius = outerRingRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
    }

    // B. Kehribar & Saf Altın Parçacıkları
    val goldSparkles = listOf(
        Triple(22f, 1.06f, Color(0xFFFFFBEB)),
        Triple(68f, 0.94f, Color(0xFFD4AF37)),
        Triple(112f, 1.09f, Color(0xFFFEF3C7)),
        Triple(158f, 0.95f, Color(0xFFFBBF24)),
        Triple(202f, 1.07f, Color(0xFFFFFBEB)),
        Triple(248f, 0.93f, Color(0xFFD4AF37)),
        Triple(292f, 1.08f, Color(0xFFFEF08A)),
        Triple(338f, 0.96f, Color(0xFFF59E0B))
    )

    goldSparkles.forEachIndexed { index, (baseAngle, rFactor, color) ->
        val pAngleRad = Math.toRadians((particleAngle + baseAngle + (index * 15.0)) - 90.0)
        val pRadius = radius * rFactor
        val pPos = Offset(
            (center.x + pRadius * cos(pAngleRad)).toFloat(),
            (center.y + pRadius * sin(pAngleRad)).toFloat()
        )

        val phaseTwinkle = ((twinkle + (index * 0.17f)) % 1.0f)
        val pAlpha = (0.35f + phaseTwinkle * 0.65f).coerceIn(0f, 1f)

        if (index % 2 == 0) {
            val pRay = strokeWidth * (0.34f + phaseTwinkle * 0.18f)
            RealisticSparkleEngine.drawMicroSparkle(
                drawScope = this,
                center = pPos,
                rayLength = pRay,
                color = color,
                alpha = pAlpha,
                rotationDeg = particleAngle * 1.2f + index * 45f
            )
        } else {
            drawCircle(
                color = color.copy(alpha = pAlpha),
                radius = strokeWidth * (0.10f + phaseTwinkle * 0.08f),
                center = pPos
            )
        }
    }
}

/**
 * 4. OBSİDİYAN & ONİKS TEMASI EFEKTİ: Füme Kristal Cam Kırılması & Titanyum Işık Hüzmeleri
 */
fun DrawScope.drawObsidianGlassSmokeEffect(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    rotationAngle: Float,
    particleAngle: Float,
    twinkle: Float,
    palette: DhikrCirclePalette,
    isDark: Boolean
) {
    // A. 12 Köşeli Füme Cam & Titanyum Işık Çerçevesi
    val outerRingRadius = radius + strokeWidth * 0.85f
    val baseAlpha = 0.30f

    rotate(rotationAngle * 0.10f, pivot = center) {
        val numPoints = 12
        val path = Path()
        val rOuter = outerRingRadius + strokeWidth * 0.40f
        val rInner = outerRingRadius - strokeWidth * 0.15f

        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) rOuter else rInner
            val angle = (i * PI / numPoints)
            val x = center.x + (r * cos(angle)).toFloat()
            val y = center.y + (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(
            path = path,
            color = Color(0xFF94A3B8).copy(alpha = baseAlpha),
            style = Stroke(width = 1.0.dp.toPx())
        )

        // Dış füme cam kordon yansıması
        drawCircle(
            brush = Brush.sweepGradient(
                listOf(
                    Color(0xFF94A3B8).copy(alpha = baseAlpha * 1.1f),
                    Color(0xFFF1F5F9).copy(alpha = baseAlpha * 1.4f),
                    Color(0xFF334155).copy(alpha = baseAlpha * 0.8f),
                    Color(0xFF94A3B8).copy(alpha = baseAlpha * 1.1f)
                ),
                center = center
            ),
            radius = outerRingRadius,
            center = center,
            style = Stroke(width = 0.9.dp.toPx())
        )
    }

    // B. Titanyum & Buzlu Platin Işık Parçacıkları
    val obsidianSparkles = listOf(
        Triple(15f, 1.05f, Color(0xFFF8FAFC)),
        Triple(60f, 0.95f, Color(0xFFCBD5E1)),
        Triple(105f, 1.08f, Color(0xFF94A3B8)),
        Triple(150f, 0.94f, Color(0xFFF1F5F9)),
        Triple(195f, 1.06f, Color(0xFFE2E8F0)),
        Triple(240f, 0.95f, Color(0xFF64748B)),
        Triple(285f, 1.07f, Color(0xFFF8FAFC)),
        Triple(330f, 0.96f, Color(0xFFCBD5E1))
    )

    obsidianSparkles.forEachIndexed { index, (baseAngle, rFactor, color) ->
        val pAngleRad = Math.toRadians((particleAngle + baseAngle + (index * 15.0)) - 90.0)
        val pRadius = radius * rFactor
        val pPos = Offset(
            (center.x + pRadius * cos(pAngleRad)).toFloat(),
            (center.y + pRadius * sin(pAngleRad)).toFloat()
        )

        val phaseTwinkle = ((twinkle + (index * 0.16f)) % 1.0f)
        val pAlpha = (0.25f + phaseTwinkle * 0.60f).coerceIn(0f, 1f)

        if (index % 2 == 0) {
            val pRay = strokeWidth * (0.30f + phaseTwinkle * 0.15f)
            RealisticSparkleEngine.drawMicroSparkle(
                drawScope = this,
                center = pPos,
                rayLength = pRay,
                color = color,
                alpha = pAlpha,
                rotationDeg = particleAngle * 1.4f + index * 35f
            )
        } else {
            drawCircle(
                color = color.copy(alpha = pAlpha),
                radius = strokeWidth * (0.09f + phaseTwinkle * 0.06f),
                center = pPos
            )
        }
    }
}
