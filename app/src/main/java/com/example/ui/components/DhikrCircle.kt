package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.util.NumberFormatter
import com.example.util.rememberShouldReduceMotion

/**
 * 2026 Ultra Lüks Zikir Çemberi Orkestratör Bileşeni
 */
@Composable
fun DhikrCircle(
    ringSize: Dp,
    progress: Float,
    displayCount: Long,
    targetCount: Long,
    isCountdownMode: Boolean,
    remainingLabel: String,
    arabicText: String,
    transliteration: String,
    lang: String = "tr",
    isZenMode: Boolean = false,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppColors.current
    val palette = remember(theme.id) { LuxuryCirclePalettes.get(theme) }

    val actualCount = if (isCountdownMode) (targetCount - displayCount).coerceAtLeast(0L) else displayCount
    val hasStarted = actualCount > 0L || progress > 0.0001f

    // Reduced Motion / Zen Mode algılaması
    val shouldReduceMotion = rememberShouldReduceMotion(isZenMode)

    // Animasyon Durumları
    val animState = rememberDhikrCircleAnimations(
        progress = progress,
        onTap = onTap,
        shouldReduceMotion = shouldReduceMotion
    )
    val animatedProgress by animState.animatedProgress
    val breathingAura by animState.breathingAura
    val bezelShimmerAngle by animState.bezelShimmerAngle
    val particleOrbitAngle by animState.particleOrbitAngle
    val sparkleTwinkle by animState.sparkleTwinkle
    val specularPhase by animState.specularPhase
    val tipLuster by animState.tipLuster
    val arabicGlowPulse by animState.arabicGlowPulse

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. GENİŞ RADYAL DIŞ AMBİYANS IŞIĞI
        val ambientGlowIntensity = if (shouldReduceMotion) 0.30f else (0.28f + (animatedProgress * 0.42f)) * breathingAura + (animState.tapLuminescence.value * 0.45f)
        val ambientGlowSize = ringSize + 100.dp

        Canvas(modifier = Modifier.size(ambientGlowSize)) {
            drawAmbientGlowBackground(palette = palette, intensity = ambientGlowIntensity)
        }

        // 2. 3D LÜKS ZİKİR ÇEMBERİ VE İÇ KADRAN
        val canvasSize = ringSize + 120.dp

        Box(
            modifier = Modifier
                .size(ringSize)
                .graphicsLayer {
                    scaleX = animState.tapScale.value
                    scaleY = animState.tapScale.value
                }
                .dhikrCircleSemantics(
                    count = displayCount,
                    target = targetCount,
                    lang = lang
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    animState.triggerTap()
                }
                .testTag("giant_tap_button"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(canvasSize)
                    .graphicsLayer { scaleX = -1f } // Tavaf yönü (Saat yönünün tersi) için yatayda aynalama
            ) {
                val strokeWidth = if (ringSize < 240.dp) 11.dp.toPx() else 14.5.dp.toPx()
                val ringDiameter = ringSize.toPx() - 12.dp.toPx()
                val radius = (ringDiameter / 2f) - (strokeWidth / 2f) - 6.dp.toPx()
                val center = Offset(size.width / 2f, size.height / 2f)
                val currentTapLum = animState.tapLuminescence.value

                // A. İÇ DERİNLİK DİSKİ
                drawInnerDisc(
                    center = center,
                    radius = radius,
                    strokeWidth = strokeWidth,
                    palette = palette,
                    isDark = theme.isDark
                )

                // B. TEMAYA ÖZEL SANATSAL DIŞ EFEKTLER (Reduced motion kapalıysa çizilir)
                if (!shouldReduceMotion) {
                    when (palette.effectType) {
                        CircleEffectType.ROSE_PETAL_AURA -> {
                            drawRosePetalAuraEffect(
                                center = center,
                                radius = radius,
                                strokeWidth = strokeWidth,
                                rotationAngle = bezelShimmerAngle,
                                particleAngle = particleOrbitAngle,
                                twinkle = sparkleTwinkle,
                                palette = palette,
                                isDark = theme.isDark
                            )
                        }
                        CircleEffectType.HADRA_NOOR_EMERALD -> {
                            drawHadraNoorEmeraldEffect(
                                center = center,
                                radius = radius,
                                strokeWidth = strokeWidth,
                                rotationAngle = bezelShimmerAngle,
                                particleAngle = particleOrbitAngle,
                                twinkle = sparkleTwinkle,
                                palette = palette,
                                isDark = theme.isDark
                            )
                        }
                        CircleEffectType.KISVE_GOLD_LATTICE -> {
                            drawKisveGoldLatticeEffect(
                                center = center,
                                radius = radius,
                                strokeWidth = strokeWidth,
                                rotationAngle = bezelShimmerAngle,
                                particleAngle = particleOrbitAngle,
                                twinkle = sparkleTwinkle,
                                palette = palette,
                                isDark = theme.isDark
                            )
                        }
                        CircleEffectType.OBSIDIAN_GLASS_SMOKE -> {
                            drawObsidianGlassSmokeEffect(
                                center = center,
                                radius = radius,
                                strokeWidth = strokeWidth,
                                rotationAngle = bezelShimmerAngle,
                                particleAngle = particleOrbitAngle,
                                twinkle = sparkleTwinkle,
                                palette = palette,
                                isDark = theme.isDark
                            )
                        }
                    }
                }

                // C. 3D METALİK DIŞ ÇERÇEVE BEZEL
                drawMetallicBezel(
                    center = center,
                    radius = radius,
                    strokeWidth = strokeWidth,
                    bezelShimmerAngle = bezelShimmerAngle,
                    palette = palette,
                    isDark = theme.isDark,
                    tapLuminescence = currentTapLum
                )

                // D. FİZİKSEL YÖNLÜ TABAN İZİ & ÇİFT KENAR PAH IŞIKLARI
                drawBaseTrackAndChamfers(
                    center = center,
                    radius = radius,
                    strokeWidth = strokeWidth,
                    palette = palette,
                    isDark = theme.isDark
                )

                // 10'luk Merhale Çentikleri ve 4 Büyük Menzil Kutup İncileri
                drawMilestoneMarkers(
                    center = center,
                    radius = radius,
                    animatedProgress = animatedProgress,
                    palette = palette,
                    isDark = theme.isDark,
                    tapLuminescence = currentTapLum
                )

                // E. DİNAMİK ANA İLERLEME ÇUBUĞU & LİDER IŞIK BAŞI
                drawProgressArcAndTip(
                    center = center,
                    radius = radius,
                    strokeWidth = strokeWidth,
                    animatedProgress = animatedProgress,
                    hasStarted = hasStarted,
                    palette = palette,
                    specularPhase = specularPhase,
                    tipLuster = tipLuster,
                    sparkleTwinkle = sparkleTwinkle,
                    tapLuminescence = currentTapLum
                )
            }

            // ÇEMBER İÇİ MERKEZ METİNLERİ VE BÜYÜK SAYILAR
            val currentFontScale = LocalDensity.current.fontScale
            val isConstrainedRing = ringSize < 240.dp || currentFontScale > 1.25f
            val isExtremeFontScale = currentFontScale > 1.35f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = if (isConstrainedRing) 8.dp else 14.dp)
            ) {
                // Arapça Lafız
                val isGoldTheme = palette.arabicText == Color(0xFFFBBF24) ||
                        palette.arabicText == Color(0xFFD4AF37) ||
                        palette.arabicText == Color(0xFFFEF3C7)

                val arabicBaseStyle = when {
                    isExtremeFontScale -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    isConstrainedRing -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    else -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                }

                val arabicGlowColor = if (isGoldTheme) {
                    Color(0xFFFDE047).copy(alpha = 0.72f * arabicGlowPulse)
                } else {
                    palette.arabicText.copy(alpha = 0.40f * arabicGlowPulse)
                }

                val arabicFinalStyle = arabicBaseStyle.copy(
                    shadow = Shadow(
                        color = arabicGlowColor,
                        offset = Offset(0f, 0f),
                        blurRadius = if (isGoldTheme) 16f * arabicGlowPulse else 8f * arabicGlowPulse
                    )
                )

                Text(
                    text = arabicText,
                    style = arabicFinalStyle,
                    color = palette.arabicText,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (transliteration.isNotBlank()) {
                    Spacer(modifier = Modifier.height(if (isExtremeFontScale) 0.dp else 1.dp))
                    Text(
                        text = transliteration,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = palette.subText,
                            fontSize = if (isExtremeFontScale) 10.sp else MaterialTheme.typography.labelSmall.fontSize
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(if (isExtremeFontScale) 1.dp else 4.dp))

                // BÜYÜK DİJİTAL SAYAÇ
                Text(
                    text = NumberFormatter.format(displayCount, lang),
                    style = when {
                        isExtremeFontScale -> MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        isConstrainedRing -> MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        else -> MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    },
                    color = palette.countText,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(if (isExtremeFontScale) 1.dp else 2.dp))

                // Hedef & İlerleme Rozeti
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isCountdownMode) remainingLabel else "/ ${NumberFormatter.format(targetCount, lang)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = palette.subText,
                            fontSize = if (isExtremeFontScale) 10.sp else MaterialTheme.typography.labelSmall.fontSize
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = palette.badgeBg,
                        border = BorderStroke(1.dp, palette.badgeBorder)
                    ) {
                        val percentage = if (targetCount > 0) {
                            (displayCount.toDouble() / targetCount.toDouble() * 100.0).toInt()
                        } else {
                            0
                        }
                        Text(
                            text = "%$percentage",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.badgeText,
                                fontSize = if (isExtremeFontScale) 10.sp else MaterialTheme.typography.labelSmall.fontSize
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Geriye dönük uyumluluk için takma ad (Alias)
 */
@Composable
fun LuxuryDhikrCircle(
    ringSize: Dp,
    progress: Float,
    displayCount: Long,
    targetCount: Long,
    isCountdownMode: Boolean,
    remainingLabel: String,
    arabicText: String,
    transliteration: String,
    lang: String = "tr",
    isZenMode: Boolean = false,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    DhikrCircle(
        ringSize = ringSize,
        progress = progress,
        displayCount = displayCount,
        targetCount = targetCount,
        isCountdownMode = isCountdownMode,
        remainingLabel = remainingLabel,
        arabicText = arabicText,
        transliteration = transliteration,
        lang = lang,
        isZenMode = isZenMode,
        onTap = onTap,
        modifier = modifier
    )
}
