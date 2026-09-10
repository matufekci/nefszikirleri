package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Geniş Radyal Dış Ambiyans Işığı Çizimi
 */
fun DrawScope.drawAmbientGlowBackground(
    palette: DhikrCirclePalette,
    intensity: Float
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val ambientRadius = size.width / 2f
    drawCircle(
        brush = Brush.radialGradient(
            colors = palette.ambientGlow.map { it.copy(alpha = (it.alpha * intensity).coerceIn(0f, 1f)) },
            center = center,
            radius = ambientRadius
        ),
        radius = ambientRadius,
        center = center
    )
}

/**
 * A. İÇ DERİNLİK DİSKİ (İpeksi Kadran & Çukur Gölgesi)
 */
fun DrawScope.drawInnerDisc(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    palette: DhikrCirclePalette,
    isDark: Boolean
) {
    val lightOffset = Offset(center.x - radius * 0.22f, center.y - radius * 0.22f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = palette.innerDisc,
            center = lightOffset,
            radius = radius * 1.05f
        ),
        radius = radius - (strokeWidth * 0.5f),
        center = center
    )

    // İç çukur temas gölgesi (Cavity Crevice Shadow)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                if (isDark) Color.Black.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.07f)
            ),
            center = center,
            radius = radius - (strokeWidth * 0.5f)
        ),
        radius = radius - (strokeWidth * 0.5f),
        center = center
    )

    // İnce iç çukur konturu
    drawCircle(
        color = palette.innerBorder,
        radius = radius - (strokeWidth * 0.5f),
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
}

/**
 * C. 3D METALİK DIŞ ÇERÇEVE BEZEL (Dönen İpeksi Işık Parıltısı)
 */
fun DrawScope.drawMetallicBezel(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    bezelShimmerAngle: Float,
    palette: DhikrCirclePalette,
    isDark: Boolean,
    tapLuminescence: Float
) {
    rotate(bezelShimmerAngle, pivot = center) {
        drawCircle(
            brush = Brush.sweepGradient(
                palette.outerBezel.map { it.copy(alpha = if (isDark) 0.35f else 0.50f) },
                center = center
            ),
            radius = radius,
            center = center,
            style = Stroke(
                width = strokeWidth * (1.18f + tapLuminescence * 0.25f),
                cap = StrokeCap.Butt
            )
        )
    }
}

/**
 * D. FİZİKSEL YÖNLÜ TABAN İZİ (Directional Base Track & Micro-Chamfer Highlights)
 */
fun DrawScope.drawBaseTrackAndChamfers(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    palette: DhikrCirclePalette,
    isDark: Boolean
) {
    // D1. Ring Tabanı Dış Temas Gölgesi (Ambient Occlusion Under Ring)
    drawCircle(
        color = if (isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.08f),
        radius = radius + (strokeWidth * 0.48f),
        center = center,
        style = Stroke(width = strokeWidth * 0.20f)
    )

    // D2. Yönlü Işıkla Gölgelendirilmiş Fiziksel Torus Tabanı (315° Key Light Directional Base)
    drawCircle(
        brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0.00f to palette.baseTrackLight,
                0.25f to palette.baseTrack,
                0.50f to palette.baseTrackDark,
                0.75f to palette.baseTrack,
                1.00f to palette.baseTrackLight
            ),
            center = center
        ),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth)
    )

    // D3. Çift Kenar Pah Işıkları (Dual-Edge Micro-Chamfer Highlights)
    // Dış Pah Işığı (Outer Rim)
    drawArc(
        brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0.00f to palette.outerRimHighlight.copy(alpha = if (isDark) 0.35f else 0.55f),
                0.30f to Color.Transparent,
                0.70f to Color.Transparent,
                1.00f to palette.outerRimHighlight.copy(alpha = if (isDark) 0.35f else 0.55f)
            ),
            center = center
        ),
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = Offset(center.x - (radius + strokeWidth * 0.5f), center.y - (radius + strokeWidth * 0.5f)),
        size = Size((radius + strokeWidth * 0.5f) * 2f, (radius + strokeWidth * 0.5f) * 2f),
        style = Stroke(width = 0.8.dp.toPx())
    )

    // İç Pah Işığı (Inner Rim Reflection)
    drawArc(
        brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0.00f to Color.Transparent,
                0.35f to palette.innerRimHighlight.copy(alpha = if (isDark) 0.25f else 0.40f),
                0.65f to palette.innerRimHighlight.copy(alpha = if (isDark) 0.25f else 0.40f),
                1.00f to Color.Transparent
            ),
            center = center
        ),
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = Offset(center.x - (radius - strokeWidth * 0.5f), center.y - (radius - strokeWidth * 0.5f)),
        size = Size((radius - strokeWidth * 0.5f) * 2f, (radius - strokeWidth * 0.5f) * 2f),
        style = Stroke(width = 0.8.dp.toPx())
    )

    // D4. Sabit Fiziksel Tepe Parıltısı (Fixed Specular Luster on Torus Crown at 315°)
    drawArc(
        brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0.00f to palette.specularHighlight.copy(alpha = if (isDark) 0.40f else 0.60f),
                0.06f to Color.Transparent,
                0.94f to Color.Transparent,
                1.00f to palette.specularHighlight.copy(alpha = if (isDark) 0.40f else 0.60f)
            ),
            center = center
        ),
        startAngle = -28f,
        sweepAngle = 56f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = Stroke(width = strokeWidth * 0.26f, cap = StrokeCap.Round)
    )
}

/**
 * 10'luk Merhale Çentikleri ve 4 Büyük Menzil Kutup İncileri (%25, %50, %75, %100)
 */
fun DrawScope.drawMilestoneMarkers(
    center: Offset,
    radius: Float,
    animatedProgress: Float,
    palette: DhikrCirclePalette,
    isDark: Boolean,
    tapLuminescence: Float
) {
    for (step in 1..20) {
        val stepRatio = step / 20f
        val markerAngleDeg = (stepRatio * 360.0) - 90.0
        val markerRad = Math.toRadians(markerAngleDeg)
        val isQuarterMilestone = (step % 5 == 0) // %25, %50, %75, %100
        val isPassed = animatedProgress >= stepRatio

        val markerRadiusPx = if (isQuarterMilestone) {
            if (isPassed) 2.6.dp.toPx() else 1.8.dp.toPx()
        } else {
            if (isPassed) 1.2.dp.toPx() else 0.8.dp.toPx()
        }

        val markerCenter = Offset(
            (center.x + radius * cos(markerRad)).toFloat(),
            (center.y + radius * sin(markerRad)).toFloat()
        )

        val markerColor = when {
            isPassed && isQuarterMilestone -> palette.tipCore.copy(alpha = 0.90f + tapLuminescence * 0.10f)
            isPassed -> palette.progressArc.last().copy(alpha = 0.70f)
            isQuarterMilestone -> palette.innerBorder.copy(alpha = 0.50f)
            else -> palette.innerBorder.copy(alpha = 0.25f)
        }

        // A. Gömülü Mikro Yuva (Embedded Socket Crevice)
        drawCircle(
            color = Color.Black.copy(alpha = if (isDark) 0.45f else 0.18f),
            radius = markerRadiusPx + 0.6.dp.toPx(),
            center = markerCenter
        )

        // B. İnci / Mücevher Gövdesi
        drawCircle(
            color = markerColor,
            radius = markerRadiusPx,
            center = markerCenter
        )

        // C. Küresel Mikro Tepe Parıltısı (Spherical Micro Highlight)
        if (markerRadiusPx > 1.dp.toPx()) {
            drawCircle(
                color = Color.White.copy(alpha = if (isPassed) 0.85f else 0.40f),
                radius = markerRadiusPx * 0.35f,
                center = Offset(markerCenter.x - markerRadiusPx * 0.28f, markerCenter.y - markerRadiusPx * 0.28f)
            )
        }
    }
}

/**
 * E. DİNAMİK ANA İLERLEME ÇUBUĞU (Uzun Soluklu Hedef İlerleme Arkı & Lider Işık Başı)
 */
fun DrawScope.drawProgressArcAndTip(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    animatedProgress: Float,
    hasStarted: Boolean,
    palette: DhikrCirclePalette,
    specularPhase: Float,
    tipLuster: Float,
    sparkleTwinkle: Float,
    tapLuminescence: Float
) {
    val trueProgressDegrees = (animatedProgress * 360f).coerceIn(0f, 360f)
    val sweepDegrees = when {
        !hasStarted -> 0f
        animatedProgress >= 0.999f -> 360f
        else -> maxOf(trueProgressDegrees, 2.5f)
    }

    if (hasStarted && sweepDegrees > 0.5f) {
        val progressFraction = (sweepDegrees / 360f).coerceIn(0.001f, 1f)
        val softLeadRatio = (0.018f).coerceAtMost(progressFraction * 0.35f)

        // Dinamik Renk Durakları (Başlangıç noktasını 0 derecede şeffaftan pürüzsüz başlatan soft gradyan)
        val arcColors = palette.progressArc
        val arcColorStops = if (arcColors.size > 1) {
            val list = mutableListOf<Pair<Float, Color>>()
            list.add(0f to arcColors.first().copy(alpha = 0f))
            list.add(softLeadRatio to arcColors.first().copy(alpha = 0.75f))
            
            val remainingColors = arcColors.drop(1)
            remainingColors.forEachIndexed { i, c ->
                val progressPortion = (i + 1).toFloat() / remainingColors.size.toFloat()
                val t = softLeadRatio + progressPortion * (progressFraction - softLeadRatio)
                list.add(t.coerceIn(0f, 1f) to c)
            }
            if (progressFraction < 0.999f) {
                list.add((progressFraction + 0.0005f).coerceAtMost(1f) to Color.Transparent)
                list.add(1.0f to Color.Transparent)
            }
            list.toTypedArray()
        } else {
            arrayOf(
                0f to arcColors.first().copy(alpha = 0f),
                softLeadRatio to arcColors.first().copy(alpha = 0.75f),
                progressFraction to arcColors.first(),
                (progressFraction + 0.0005f).coerceAtMost(1f) to Color.Transparent,
                1f to Color.Transparent
            )
        }

        val bloomColors = palette.progressBloom
        val bloomColorStops = if (bloomColors.size > 1) {
            val list = mutableListOf<Pair<Float, Color>>()
            list.add(0f to Color.Transparent)
            list.add(softLeadRatio to bloomColors.first().copy(alpha = (bloomColors.first().alpha * (0.60f + tapLuminescence * 0.20f)).coerceIn(0f, 1f)))

            val remainingBloom = bloomColors.drop(1)
            remainingBloom.forEachIndexed { i, c ->
                val progressPortion = (i + 1).toFloat() / remainingBloom.size.toFloat()
                val t = softLeadRatio + progressPortion * (progressFraction - softLeadRatio)
                list.add(t.coerceIn(0f, 1f) to c.copy(alpha = (c.alpha * (0.80f + tapLuminescence * 0.20f)).coerceIn(0f, 1f)))
            }
            if (progressFraction < 0.999f) {
                list.add((progressFraction + 0.0005f).coerceAtMost(1f) to Color.Transparent)
                list.add(1.0f to Color.Transparent)
            }
            list.toTypedArray()
        } else {
            arrayOf(
                0f to Color.Transparent,
                softLeadRatio to bloomColors.first().copy(alpha = 0.5f),
                progressFraction to bloomColors.first(),
                (progressFraction + 0.0005f).coerceAtMost(1f) to Color.Transparent,
                1f to Color.Transparent
            )
        }

        // Hacimli Çekirdek Renk Durakları (Luminous Waveguide Core)
        val coreColorStops = if (arcColors.size > 1) {
            val list = mutableListOf<Pair<Float, Color>>()
            list.add(0f to Color.Transparent)
            list.add(softLeadRatio to palette.tipCore.copy(alpha = 0.40f))
            val remainingColors = arcColors.drop(1)
            remainingColors.forEachIndexed { i, _ ->
                val progressPortion = (i + 1).toFloat() / remainingColors.size.toFloat()
                val t = softLeadRatio + progressPortion * (progressFraction - softLeadRatio)
                list.add(t.coerceIn(0f, 1f) to palette.tipCore.copy(alpha = (0.35f + 0.45f * (i.toFloat() / remainingColors.size.toFloat())).coerceIn(0f, 1f)))
            }
            if (progressFraction < 0.999f) {
                list.add((progressFraction + 0.0005f).coerceAtMost(1f) to Color.Transparent)
                list.add(1.0f to Color.Transparent)
            }
            list.toTypedArray()
        } else {
            arrayOf(
                0f to Color.Transparent,
                softLeadRatio to palette.tipCore.copy(alpha = 0.40f),
                progressFraction to palette.tipCore.copy(alpha = 0.80f),
                (progressFraction + 0.0005f).coerceAtMost(1f) to Color.Transparent,
                1f to Color.Transparent
            )
        }

        rotate(-90f, pivot = center) {
            // 1. Dış Yüzey Işık Sızması (Surface Spill / Ambient Bloom)
            val bloomStroke = strokeWidth * (1.30f + tapLuminescence * 0.35f)
            drawArc(
                brush = Brush.sweepGradient(
                    colorStops = bloomColorStops,
                    center = center
                ),
                startAngle = 0f,
                sweepAngle = sweepDegrees,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = bloomStroke, cap = StrokeCap.Butt)
            )

            // 2. Ana Parlayan 3D Işık Gövdesi (Rich Enamel Arc Body)
            drawArc(
                brush = Brush.sweepGradient(
                    colorStops = arcColorStops,
                    center = center
                ),
                startAngle = 0f,
                sweepAngle = sweepDegrees,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )

            // 3. Hacimli İç Işık Çekirdeği (Volumetric Inner Core Waveguide)
            drawArc(
                brush = Brush.sweepGradient(
                    colorStops = coreColorStops,
                    center = center
                ),
                startAngle = 0f,
                sweepAngle = sweepDegrees,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth * 0.36f, cap = StrokeCap.Butt)
            )

            // 4. Akışkan Sıvı Işık Dalgası (Specular Flow Wave)
            if (sweepDegrees > 4f) {
                val waveCenter = specularPhase * sweepDegrees
                val waveWidth = (sweepDegrees * 0.52f).coerceIn(24f, 135f)
                val waveStart = (waveCenter - waveWidth / 2f).coerceIn(0f, (sweepDegrees - waveWidth).coerceAtLeast(0f))
                
                val waveFractionStart = (waveStart / 360f).coerceIn(0f, 1f)
                val waveFractionEnd = ((waveStart + waveWidth) / 360f).coerceIn(0f, 1f)
                val waveFractionMid = (waveFractionStart + waveFractionEnd) / 2f
                val waveAlpha = (0.30f + tapLuminescence * 0.18f).coerceIn(0f, 1f)

                drawArc(
                    brush = Brush.sweepGradient(
                        colorStops = arrayOf(
                            (waveFractionStart - 0.001f).coerceAtLeast(0f) to Color.Transparent,
                            waveFractionStart to Color.Transparent,
                            (waveFractionStart + (waveFractionMid - waveFractionStart) * 0.45f) to Color.White.copy(alpha = waveAlpha * 0.35f),
                            waveFractionMid to Color.White.copy(alpha = waveAlpha),
                            (waveFractionMid + (waveFractionEnd - waveFractionMid) * 0.55f) to Color.White.copy(alpha = waveAlpha * 0.35f),
                            waveFractionEnd to Color.Transparent,
                            (waveFractionEnd + 0.001f).coerceAtMost(1f) to Color.Transparent
                        ),
                        center = center
                    ),
                    startAngle = waveStart,
                    sweepAngle = waveWidth,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth * 0.46f, cap = StrokeCap.Butt)
                )
            }
        }

        // 5. LİDER UÇTA PARLAYAN KRİSTAL İNCİ & ELMAS IŞIK YILDIZI
        val currentAngleRad = Math.toRadians((sweepDegrees.toDouble()) - 90.0)
        val tipCenter = Offset(
            (center.x + radius * cos(currentAngleRad)).toFloat(),
            (center.y + radius * sin(currentAngleRad)).toFloat()
        )

        val currentTipScale = tipLuster * (1.0f + tapLuminescence * 0.35f)

        // Dış Radyal Işık Halesi
        drawCircle(
            brush = Brush.radialGradient(
                palette.tipGlow.map { it.copy(alpha = it.alpha.coerceIn(0f, 1f)) },
                center = tipCenter,
                radius = strokeWidth * 1.75f * currentTipScale
            ),
            radius = strokeWidth * 1.75f * currentTipScale,
            center = tipCenter
        )

        // Kristal İnci Çekirdeği
        drawCircle(
            color = palette.tipCore.copy(alpha = 0.95f),
            radius = strokeWidth * 0.62f * currentTipScale,
            center = tipCenter
        )

        // Saf Beyaz Işık Noktası
        drawCircle(
            color = Color.White.copy(alpha = 0.90f),
            radius = strokeWidth * 0.36f * currentTipScale,
            center = tipCenter
        )

        // Ultra-Gerçekçi Elmas Işık Parıltısı (Realistic Diamond Optical Flare)
        val starRayLen = strokeWidth * 1.30f * currentTipScale * (0.85f + 0.15f * sparkleTwinkle)
        if (starRayLen > 0.5f) {
            val starAlpha = (0.95f + tapLuminescence * 0.05f).coerceIn(0f, 1f)
            val haloCol = palette.tipGlow.firstOrNull() ?: palette.tipCore
            RealisticSparkleEngine.drawDiamondTipFlare(
                drawScope = this,
                center = tipCenter,
                rayLength = starRayLen,
                haloColor = haloCol,
                alpha = starAlpha,
                rotationDeg = sweepDegrees
            )
        }
    }
}
