package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeColors
import com.example.ui.theme.LocalAppColors
import com.example.util.NumberFormatter
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2026 Trend - Ultra Lüks 3D Zikir Çemberi
 * 
 * Her temaya özel görsel stil ve efekt mimarisi:
 * 1. GÜL TEMALARI (Gündüz & Gece): Ethereal gül yaprağı hüzmeleri, pembe altın & yakut degrade, narin uçuşan yaprak auraları.
 * 2. HADRÂ ZÜMRÜT TEMALARI (Gündüz & Gece): İlahi Nur geometrisi, 12 köşeli yıldız ışınları, derin zümrüt kristal derinliği ve altın parçacıklar.
 * 3. KİSVE TEMASI: Saf 24 Ayar dövme altın kordon, Kabe Kisvesi ipek dokuma rölyefi, kehribar ve elmas ışıltı hüzmeleri.
 */

enum class CircleEffectType {
    HADRA_NOOR_EMERALD,  // Zümrüt & Nur Işınları (Cam/Prizma)
    ROSE_PETAL_AURA,     // Gül Yaprağı Hüzmeleri & Yakut/Pembe Kuvars (Cam/Yüzey)
    KISVE_GOLD_LATTICE,  // Kabe Kisvesi & 24K Saf Metalik Altın Ayna Yansıması
    OBSIDIAN_GLASS_SMOKE // Füme Saf Siyah Oniks Kristal Cam Yansıması
}

data class DhikrCirclePalette(
    val effectType: CircleEffectType,
    val outerBezel: List<Color>,
    val innerDisc: List<Color>,
    val innerBorder: Color,
    val baseTrack: Color,
    val baseTrackLight: Color,
    val baseTrackDark: Color,
    val specularHighlight: Color,
    val outerRimHighlight: Color,
    val innerRimHighlight: Color,
    val progressArc: List<Color>,
    val progressBloom: List<Color>,
    val liquidWave: List<Color>,
    val tipGlow: List<Color>,
    val tipCore: Color,
    val arabicText: Color,
    val countText: Color,
    val subText: Color,
    val badgeBg: Color,
    val badgeBorder: Color,
    val badgeText: Color,
    val ambientGlow: List<Color>
)

object LuxuryCirclePalettes {

    fun get(theme: AppThemeColors): DhikrCirclePalette {
        return when (theme.id) {
            // 1. GÜNDÜZ • HADRÂ (Soft Adaçayı / Krem Mat & Zümrüt-Altın Porselen)
            "hadra_gunduz" -> DhikrCirclePalette(
                effectType = CircleEffectType.HADRA_NOOR_EMERALD,
                outerBezel = listOf(
                    Color(0xFFD4AF37),
                    Color(0xFF059669),
                    Color(0xFFF59E0B),
                    Color(0xFFFDE68A),
                    Color(0xFF047857),
                    Color(0xFFD4AF37)
                ),
                innerDisc = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF6FAF7),
                    Color(0xFFE6EFE8)
                ),
                innerBorder = Color(0xFFCADBD0),
                baseTrack = Color(0xFFC0D6C8).copy(alpha = 0.50f),
                baseTrackLight = Color(0xFFD8EBE0).copy(alpha = 0.70f),
                baseTrackDark = Color(0xFFA6C4B2).copy(alpha = 0.55f),
                specularHighlight = Color(0xFFFFFFFF).copy(alpha = 0.75f),
                outerRimHighlight = Color(0xFFFFFFFF).copy(alpha = 0.65f),
                innerRimHighlight = Color(0xFFFFFFFF).copy(alpha = 0.40f),
                progressArc = listOf(
                    Color(0xFF059669),
                    Color(0xFF10B981),
                    Color(0xFF34D399),
                    Color(0xFF6EE7B7),
                    Color(0xFFF59E0B),
                    Color(0xFFFBBF24),
                    Color(0xFFFFFBEB)
                ),
                progressBloom = listOf(
                    Color(0xFF059669).copy(alpha = 0.30f),
                    Color(0xFF10B981).copy(alpha = 0.50f),
                    Color(0xFF34D399).copy(alpha = 0.65f),
                    Color(0xFFFBBF24).copy(alpha = 0.75f),
                    Color(0xFFFEF08A).copy(alpha = 0.85f)
                ),
                liquidWave = listOf(
                    Color.Transparent,
                    Color(0xFF34D399).copy(alpha = 0.45f),
                    Color(0xFFFFFFFF).copy(alpha = 0.90f),
                    Color(0xFFFDE68A).copy(alpha = 0.60f),
                    Color.Transparent
                ),
                tipGlow = listOf(
                    Color(0xFFFEF08A).copy(alpha = 0.90f),
                    Color(0xFF10B981).copy(alpha = 0.50f),
                    Color.Transparent
                ),
                tipCore = Color(0xFFFFFBEB),
                arabicText = Color(0xFFD4AF37),
                countText = Color(0xFF11261C),
                subText = Color(0xFF455A4F),
                badgeBg = Color(0xFFB45309).copy(alpha = 0.12f),
                badgeBorder = Color(0xFFB45309).copy(alpha = 0.40f),
                badgeText = Color(0xFFB45309),
                ambientGlow = listOf(
                    Color(0xFF10B981).copy(alpha = 0.22f),
                    Color(0xFF047857).copy(alpha = 0.14f),
                    Color(0xFFFBBF24).copy(alpha = 0.10f),
                    Color.Transparent
                )
            )

            // 2. GÜNDÜZ • GÜL & BEYAZ (Soft Pudra Krem & Pembe Altın Seramik)
            "gul_gunduz" -> DhikrCirclePalette(
                effectType = CircleEffectType.ROSE_PETAL_AURA,
                outerBezel = listOf(
                    Color(0xFFE07A8A),
                    Color(0xFFF4B8C1),
                    Color(0xFFF59E0B),
                    Color(0xFFFDF0CD),
                    Color(0xFFD46074),
                    Color(0xFFE07A8A)
                ),
                innerDisc = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFFDF6F8),
                    Color(0xFFF8E9EC)
                ),
                innerBorder = Color(0xFFDECAD0),
                baseTrack = Color(0xFFE8D4DA).copy(alpha = 0.55f),
                baseTrackLight = Color(0xFFFCE8ED).copy(alpha = 0.72f),
                baseTrackDark = Color(0xFFD8B9C2).copy(alpha = 0.58f),
                specularHighlight = Color(0xFFFFFFFF).copy(alpha = 0.80f),
                outerRimHighlight = Color(0xFFFFFFFF).copy(alpha = 0.70f),
                innerRimHighlight = Color(0xFFFFFFFF).copy(alpha = 0.42f),
                progressArc = listOf(
                    Color(0xFFBE123C),
                    Color(0xFFE11D48),
                    Color(0xFFF43F5E),
                    Color(0xFFFB7185),
                    Color(0xFFFDA4AF),
                    Color(0xFFFDE68A),
                    Color(0xFFFFF5F7)
                ),
                progressBloom = listOf(
                    Color(0xFFBE123C).copy(alpha = 0.30f),
                    Color(0xFFE11D48).copy(alpha = 0.50f),
                    Color(0xFFFB7185).copy(alpha = 0.65f),
                    Color(0xFFFDA4AF).copy(alpha = 0.75f),
                    Color(0xFFFDE68A).copy(alpha = 0.85f)
                ),
                liquidWave = listOf(
                    Color.Transparent,
                    Color(0xFFFDA4AF).copy(alpha = 0.45f),
                    Color(0xFFFFFFFF).copy(alpha = 0.90f),
                    Color(0xFFFDE68A).copy(alpha = 0.60f),
                    Color.Transparent
                ),
                tipGlow = listOf(
                    Color(0xFFFDE68A).copy(alpha = 0.90f),
                    Color(0xFFFB7185).copy(alpha = 0.50f),
                    Color.Transparent
                ),
                tipCore = Color(0xFFFFF5F7),
                arabicText = Color(0xFF9F1239),
                countText = Color(0xFF28111A),
                subText = Color(0xFF6B4853),
                badgeBg = Color(0xFFBE123C).copy(alpha = 0.12f),
                badgeBorder = Color(0xFFBE123C).copy(alpha = 0.40f),
                badgeText = Color(0xFF9F1239),
                ambientGlow = listOf(
                    Color(0xFFFB7185).copy(alpha = 0.22f),
                    Color(0xFFBE123C).copy(alpha = 0.14f),
                    Color(0xFFFDE68A).copy(alpha = 0.10f),
                    Color.Transparent
                )
            )

            // 3. GECE • HADRÂ & ZÜMRÜT (Derin Gece Zümrüdü & Kristal Derinliği)
            "hadra_gece" -> DhikrCirclePalette(
                effectType = CircleEffectType.HADRA_NOOR_EMERALD,
                outerBezel = listOf(
                    Color(0xFF00F5A0),
                    Color(0xFF10B981),
                    Color(0xFFFBBF24),
                    Color(0xFFFEF08A),
                    Color(0xFF047857),
                    Color(0xFF00F5A0)
                ),
                innerDisc = listOf(
                    Color(0xFF0F3B25),
                    Color(0xFF0A291A),
                    Color(0xFF05170E)
                ),
                innerBorder = Color(0xFF10B981).copy(alpha = 0.40f),
                baseTrack = Color(0xFF0D3B27).copy(alpha = 0.65f),
                baseTrackLight = Color(0xFF17573A).copy(alpha = 0.80f),
                baseTrackDark = Color(0xFF061E13).copy(alpha = 0.90f),
                specularHighlight = Color(0xFFE6FFFA).copy(alpha = 0.60f),
                outerRimHighlight = Color(0xFF34D399).copy(alpha = 0.50f),
                innerRimHighlight = Color(0xFF00F5A0).copy(alpha = 0.35f),
                progressArc = listOf(
                    Color(0xFF059669),
                    Color(0xFF10B981),
                    Color(0xFF00F5A0),
                    Color(0xFF34D399),
                    Color(0xFF6EE7B7),
                    Color(0xFFFBBF24),
                    Color(0xFFFFFBEB)
                ),
                progressBloom = listOf(
                    Color(0xFF059669).copy(alpha = 0.35f),
                    Color(0xFF10B981).copy(alpha = 0.55f),
                    Color(0xFF00F5A0).copy(alpha = 0.70f),
                    Color(0xFF34D399).copy(alpha = 0.80f),
                    Color(0xFFFEF08A).copy(alpha = 0.90f)
                ),
                liquidWave = listOf(
                    Color.Transparent,
                    Color(0xFF00F5A0).copy(alpha = 0.50f),
                    Color(0xFFFFFFFF).copy(alpha = 0.95f),
                    Color(0xFFFEF08A).copy(alpha = 0.65f),
                    Color.Transparent
                ),
                tipGlow = listOf(
                    Color(0xFFFEF08A).copy(alpha = 0.95f),
                    Color(0xFF00F5A0).copy(alpha = 0.60f),
                    Color.Transparent
                ),
                tipCore = Color(0xFFFFFBEB),
                arabicText = Color(0xFFFBBF24),
                countText = Color(0xFFFFFFFF),
                subText = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                badgeBg = Color(0xFFFBBF24).copy(alpha = 0.22f),
                badgeBorder = Color(0xFFFBBF24).copy(alpha = 0.60f),
                badgeText = Color(0xFFFEF08A),
                ambientGlow = listOf(
                    Color(0xFF00F5A0).copy(alpha = 0.35f),
                    Color(0xFF10B981).copy(alpha = 0.25f),
                    Color(0xFFFBBF24).copy(alpha = 0.18f),
                    Color.Transparent
                )
            )

            // 4. GECE • GÜL & SİYAH (Gece Gülü & Derin Yakut Kristal)
            "gul_gece" -> DhikrCirclePalette(
                effectType = CircleEffectType.ROSE_PETAL_AURA,
                outerBezel = listOf(
                    Color(0xFFFB7185),
                    Color(0xFFE11D48),
                    Color(0xFFFDE68A),
                    Color(0xFFFDA4AF),
                    Color(0xFF881337),
                    Color(0xFFFB7185)
                ),
                innerDisc = listOf(
                    Color(0xFF261021),
                    Color(0xFF1A0A16),
                    Color(0xFF0E040C)
                ),
                innerBorder = Color(0xFFFB7185).copy(alpha = 0.40f),
                baseTrack = Color(0xFF3B162E).copy(alpha = 0.65f),
                baseTrackLight = Color(0xFF551E41).copy(alpha = 0.80f),
                baseTrackDark = Color(0xFF1C0816).copy(alpha = 0.90f),
                specularHighlight = Color(0xFFFFF0F5).copy(alpha = 0.60f),
                outerRimHighlight = Color(0xFFFB7185).copy(alpha = 0.50f),
                innerRimHighlight = Color(0xFFFDA4AF).copy(alpha = 0.35f),
                progressArc = listOf(
                    Color(0xFFBE123C),
                    Color(0xFFE11D48),
                    Color(0xFFF43F5E),
                    Color(0xFFFB7185),
                    Color(0xFFFDA4AF),
                    Color(0xFFFDE68A),
                    Color(0xFFFFF5F7)
                ),
                progressBloom = listOf(
                    Color(0xFFBE123C).copy(alpha = 0.35f),
                    Color(0xFFE11D48).copy(alpha = 0.55f),
                    Color(0xFFFB7185).copy(alpha = 0.70f),
                    Color(0xFFFDA4AF).copy(alpha = 0.80f),
                    Color(0xFFFDE68A).copy(alpha = 0.90f)
                ),
                liquidWave = listOf(
                    Color.Transparent,
                    Color(0xFFFDA4AF).copy(alpha = 0.50f),
                    Color(0xFFFFFFFF).copy(alpha = 0.95f),
                    Color(0xFFFDE68A).copy(alpha = 0.65f),
                    Color.Transparent
                ),
                tipGlow = listOf(
                    Color(0xFFFDE68A).copy(alpha = 0.95f),
                    Color(0xFFFB7185).copy(alpha = 0.60f),
                    Color.Transparent
                ),
                tipCore = Color(0xFFFFF5F7),
                arabicText = Color(0xFFFDA4AF),
                countText = Color(0xFFFFF1F2),
                subText = Color(0xFFFBCFE8).copy(alpha = 0.85f),
                badgeBg = Color(0xFFFB7185).copy(alpha = 0.22f),
                badgeBorder = Color(0xFFFB7185).copy(alpha = 0.60f),
                badgeText = Color(0xFFFDE68A),
                ambientGlow = listOf(
                    Color(0xFFFB7185).copy(alpha = 0.35f),
                    Color(0xFFE11D48).copy(alpha = 0.25f),
                    Color(0xFFFDE68A).copy(alpha = 0.18f),
                    Color.Transparent
                )
            )

            // 5. KİSVE (Ultra-Metalik 24K Ayna Altın & Ağır Krom Yansıma)
            "kisve" -> DhikrCirclePalette(
                effectType = CircleEffectType.KISVE_GOLD_LATTICE,
                outerBezel = listOf(
                    Color(0xFFD4AF37),
                    Color(0xFFFFFDF5),
                    Color(0xFF78350F),
                    Color(0xFFFDE68A),
                    Color(0xFF996515),
                    Color(0xFFFFFDF5),
                    Color(0xFFD4AF37)
                ),
                innerDisc = listOf(
                    Color(0xFF1F1B12),
                    Color(0xFF14120D),
                    Color(0xFF0A0906)
                ),
                innerBorder = Color(0xFFD4AF37).copy(alpha = 0.60f),
                baseTrack = Color(0xFF332B18).copy(alpha = 0.70f),
                baseTrackLight = Color(0xFF6B5824).copy(alpha = 0.90f),
                baseTrackDark = Color(0xFF181409).copy(alpha = 0.95f),
                specularHighlight = Color(0xFFFFFFFD).copy(alpha = 0.95f), // Ultra-yüksek metalik ayna parıltısı
                outerRimHighlight = Color(0xFFFEF3C7).copy(alpha = 0.85f),
                innerRimHighlight = Color(0xFFD4AF37).copy(alpha = 0.65f),
                progressArc = listOf(
                    Color(0xFF92400E),
                    Color(0xFFB45309),
                    Color(0xFFD4AF37),
                    Color(0xFFF59E0B),
                    Color(0xFFFBBF24),
                    Color(0xFFFEF08A),
                    Color(0xFFFFFDF5)
                ),
                progressBloom = listOf(
                    Color(0xFFB45309).copy(alpha = 0.40f),
                    Color(0xFFD4AF37).copy(alpha = 0.65f),
                    Color(0xFFF59E0B).copy(alpha = 0.80f),
                    Color(0xFFFBBF24).copy(alpha = 0.90f),
                    Color(0xFFFEF08A).copy(alpha = 0.95f)
                ),
                liquidWave = listOf(
                    Color.Transparent,
                    Color(0xFFFDE68A).copy(alpha = 0.60f),
                    Color(0xFFFFFFFF).copy(alpha = 0.98f),
                    Color(0xFFFEF08A).copy(alpha = 0.75f),
                    Color.Transparent
                ),
                tipGlow = listOf(
                    Color(0xFFFEF08A).copy(alpha = 0.98f),
                    Color(0xFFD4AF37).copy(alpha = 0.75f),
                    Color.Transparent
                ),
                tipCore = Color(0xFFFFFDF5),
                arabicText = Color(0xFFFEF3C7),
                countText = Color(0xFFFFFDF5),
                subText = Color(0xFFE7E5E4).copy(alpha = 0.90f),
                badgeBg = Color(0xFFD4AF37).copy(alpha = 0.28f),
                badgeBorder = Color(0xFFD4AF37).copy(alpha = 0.70f),
                badgeText = Color(0xFFFEF3C7),
                ambientGlow = listOf(
                    Color(0xFFD4AF37).copy(alpha = 0.45f),
                    Color(0xFFF59E0B).copy(alpha = 0.32f),
                    Color(0xFFFEF08A).copy(alpha = 0.22f),
                    Color.Transparent
                )
            )

            // 6. GECE • ONİKS & SAF SİYAH (Füme Obsidian Kristal Cam & Titanyum Yansıma)
            "siyah", "obsidian" -> DhikrCirclePalette(
                effectType = CircleEffectType.OBSIDIAN_GLASS_SMOKE,
                outerBezel = listOf(
                    Color(0xFF94A3B8),
                    Color(0xFFF1F5F9),
                    Color(0xFF1E293B),
                    Color(0xFFCBD5E1),
                    Color(0xFF0F172A),
                    Color(0xFF94A3B8)
                ),
                innerDisc = listOf(
                    Color(0xFF16181F),
                    Color(0xFF0D0E12),
                    Color(0xFF050507)
                ),
                innerBorder = Color(0xFF94A3B8).copy(alpha = 0.35f),
                baseTrack = Color(0xFF1E222B).copy(alpha = 0.65f),
                baseTrackLight = Color(0xFF333B4A).copy(alpha = 0.80f),
                baseTrackDark = Color(0xFF0B0D11).copy(alpha = 0.90f),
                specularHighlight = Color(0xFFF1F5F9).copy(alpha = 0.60f), // Buzlu titanyum-cam yansıması
                outerRimHighlight = Color(0xFFCBD5E1).copy(alpha = 0.45f),
                innerRimHighlight = Color(0xFF94A3B8).copy(alpha = 0.30f),
                progressArc = listOf(
                    Color(0xFF475569),
                    Color(0xFF64748B),
                    Color(0xFF94A3B8),
                    Color(0xFFCBD5E1),
                    Color(0xFFE2E8F0),
                    Color(0xFFF8FAFC)
                ),
                progressBloom = listOf(
                    Color(0xFF475569).copy(alpha = 0.30f),
                    Color(0xFF64748B).copy(alpha = 0.50f),
                    Color(0xFF94A3B8).copy(alpha = 0.65f),
                    Color(0xFFCBD5E1).copy(alpha = 0.75f),
                    Color(0xFFF8FAFC).copy(alpha = 0.85f)
                ),
                liquidWave = listOf(
                    Color.Transparent,
                    Color(0xFFCBD5E1).copy(alpha = 0.45f),
                    Color(0xFFFFFFFF).copy(alpha = 0.95f),
                    Color(0xFF94A3B8).copy(alpha = 0.60f),
                    Color.Transparent
                ),
                tipGlow = listOf(
                    Color(0xFFF8FAFC).copy(alpha = 0.90f),
                    Color(0xFF94A3B8).copy(alpha = 0.55f),
                    Color.Transparent
                ),
                tipCore = Color(0xFFFFFFFF),
                arabicText = Color(0xFFE2E8F0),
                countText = Color(0xFFF8FAFC),
                subText = Color(0xFF94A3B8).copy(alpha = 0.85f),
                badgeBg = Color(0xFF64748B).copy(alpha = 0.22f),
                badgeBorder = Color(0xFF64748B).copy(alpha = 0.55f),
                badgeText = Color(0xFFE2E8F0),
                ambientGlow = listOf(
                    Color(0xFF64748B).copy(alpha = 0.30f),
                    Color(0xFF475569).copy(alpha = 0.20f),
                    Color(0xFFCBD5E1).copy(alpha = 0.12f),
                    Color.Transparent
                )
            )

            else -> get(com.example.ui.theme.AppPalettes.HadraGece)
        }
    }
}

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
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppColors.current
    val palette = remember(theme.id) { LuxuryCirclePalettes.get(theme) }

    val actualCount = if (isCountdownMode) (targetCount - displayCount).coerceAtLeast(0L) else displayCount
    val hasStarted = actualCount > 0L || progress > 0.0001f

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "luxury_circle_progress"
    )

    // Tıklama reaksiyonu ve organik dokunma yaylanması
    val tapScale = remember { Animatable(1.0f) }
    val tapLuminescence = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // 2026 Trend Ethereal Loops
    val infiniteTransition = rememberInfiniteTransition(label = "luxury_circle_loops")

    val breathingAura by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_aura"
    )

    val bezelShimmerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bezel_shimmer_angle"
    )

    val particleOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_orbit_angle"
    )

    val sparkleTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_twinkle"
    )

    val specularPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "specular_phase"
    )

    val tipLuster by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tip_luster"
    )

    val arabicGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arabic_glow_pulse"
    )

    val arabicShimmerOffset by infiniteTransition.animateFloat(
        initialValue = -120f,
        targetValue = 320f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arabic_shimmer_offset"
    )

    val triggerTap = {
        coroutineScope.launch {
            tapScale.animateTo(0.962f, tween(35, easing = LinearEasing))
            tapScale.animateTo(1.0f, spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMediumLow))
        }
        coroutineScope.launch {
            tapLuminescence.snapTo(1.0f)
            tapLuminescence.animateTo(0f, tween(450, easing = FastOutSlowInEasing))
        }
        onTap()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. GENİŞ RADYAL DIŞ AMBİYANS IŞIĞI (Sınırsızca yayılan yumuşak hale)
        val ambientGlowIntensity = (0.28f + (animatedProgress * 0.42f)) * breathingAura + (tapLuminescence.value * 0.45f)
        val ambientGlowSize = ringSize + 100.dp

        Canvas(modifier = Modifier.size(ambientGlowSize)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val ambientRadius = size.width / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = palette.ambientGlow.map { it.copy(alpha = (it.alpha * ambientGlowIntensity).coerceIn(0f, 1f)) },
                    center = center,
                    radius = ambientRadius
                ),
                radius = ambientRadius,
                center = center
            )
        }

        // 2. 3D LÜKS ZİKİR ÇEMBERİ VE İÇ KADRAN
        // Parıltıların, elmas kırınım ışınlarının ve gül yapraklarının sınırsızca yayılabilmesi için
        // .clip(CircleShape) kaldırılmış ve çizim alanı genişletilmiştir.
        val canvasSize = ringSize + 120.dp

        Box(
            modifier = Modifier
                .size(ringSize)
                .graphicsLayer {
                    scaleX = tapScale.value
                    scaleY = tapScale.value
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    triggerTap()
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
                val currentTapLum = tapLuminescence.value

                // A. İÇ DERİNLİK DİSKİ (İpeksi Kadran & Çukur Gölgesi)
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
                            if (theme.isDark) Color.Black.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.07f)
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

                // B. TEMAYA ÖZEL SANATSAL DIŞ EFEKTLER (Gül Yaprakları / Zümrüt Nur / Kisve Dokuması)
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

                // C. 3D METALİK DIŞ ÇERÇEVE BEZEL (Dönen İpeksi Işık Parıltısı)
                rotate(bezelShimmerAngle, pivot = center) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            palette.outerBezel.map { it.copy(alpha = if (theme.isDark) 0.35f else 0.50f) },
                            center = center
                        ),
                        radius = radius,
                        center = center,
                        style = Stroke(
                            width = strokeWidth * (1.18f + currentTapLum * 0.25f),
                            cap = StrokeCap.Butt
                        )
                    )
                }

                // D. FİZİKSEL YÖNLÜ TABAN İZİ (Directional Base Track & Micro-Chamfer Highlights)
                // D1. Ring Tabanı Dış Temas Gölgesi (Ambient Occlusion Under Ring)
                drawCircle(
                    color = if (theme.isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.08f),
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
                            0.00f to palette.outerRimHighlight.copy(alpha = if (theme.isDark) 0.35f else 0.55f),
                            0.30f to Color.Transparent,
                            0.70f to Color.Transparent,
                            1.00f to palette.outerRimHighlight.copy(alpha = if (theme.isDark) 0.35f else 0.55f)
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
                            0.35f to palette.innerRimHighlight.copy(alpha = if (theme.isDark) 0.25f else 0.40f),
                            0.65f to palette.innerRimHighlight.copy(alpha = if (theme.isDark) 0.25f else 0.40f),
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
                            0.00f to palette.specularHighlight.copy(alpha = if (theme.isDark) 0.40f else 0.60f),
                            0.06f to Color.Transparent,
                            0.94f to Color.Transparent,
                            1.00f to palette.specularHighlight.copy(alpha = if (theme.isDark) 0.40f else 0.60f)
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

                // 10'luk Merhale Çentikleri ve 4 Büyük Menzil Kutup İncileri (%25, %50, %75, %100)
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
                        isPassed && isQuarterMilestone -> palette.tipCore.copy(alpha = 0.90f + currentTapLum * 0.10f)
                        isPassed -> palette.progressArc.last().copy(alpha = 0.70f)
                        isQuarterMilestone -> palette.innerBorder.copy(alpha = 0.50f)
                        else -> palette.innerBorder.copy(alpha = 0.25f)
                    }

                    // A. Gömülü Mikro Yuva (Embedded Socket Crevice)
                    drawCircle(
                        color = Color.Black.copy(alpha = if (theme.isDark) 0.45f else 0.18f),
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

                // E. DİNAMİK ANA İLERLEME ÇUBUĞU (Uzun Soluklu Hedef İlerleme Arkı & Lider Işık Başı)
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
                        // Başlangıç yumuşak geçişi (Soft fade-in at 0°)
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
                        list.add(softLeadRatio to bloomColors.first().copy(alpha = (bloomColors.first().alpha * (0.60f + currentTapLum * 0.20f)).coerceIn(0f, 1f)))

                        val remainingBloom = bloomColors.drop(1)
                        remainingBloom.forEachIndexed { i, c ->
                            val progressPortion = (i + 1).toFloat() / remainingBloom.size.toFloat()
                            val t = softLeadRatio + progressPortion * (progressFraction - softLeadRatio)
                            list.add(t.coerceIn(0f, 1f) to c.copy(alpha = (c.alpha * (0.80f + currentTapLum * 0.20f)).coerceIn(0f, 1f)))
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
                        val bloomStroke = strokeWidth * (1.30f + currentTapLum * 0.35f)
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

                        // 4. Akışkan Sıvı Işık Dalgası (Specular Flow Wave - İpeksi ve yumuşak)
                        if (sweepDegrees > 4f) {
                            val waveCenter = specularPhase * sweepDegrees
                            val waveWidth = (sweepDegrees * 0.52f).coerceIn(24f, 135f)
                            val waveStart = (waveCenter - waveWidth / 2f).coerceIn(0f, (sweepDegrees - waveWidth).coerceAtLeast(0f))
                            
                            val waveFractionStart = (waveStart / 360f).coerceIn(0f, 1f)
                            val waveFractionEnd = ((waveStart + waveWidth) / 360f).coerceIn(0f, 1f)
                            val waveFractionMid = (waveFractionStart + waveFractionEnd) / 2f
                            val waveAlpha = (0.30f + currentTapLum * 0.18f).coerceIn(0f, 1f)

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

                    // 5. LİDER UÇTA PARLAYAN KRİSTAL İNCİ & ELMAS IŞIK YILDIZI (İlk adımdan itibaren canlı ve belirgin)
                    val currentAngleRad = Math.toRadians((sweepDegrees.toDouble()) - 90.0)
                    val tipCenter = Offset(
                        (center.x + radius * cos(currentAngleRad)).toFloat(),
                        (center.y + radius * sin(currentAngleRad)).toFloat()
                    )

                    val currentTipScale = tipLuster * (1.0f + currentTapLum * 0.35f)

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
                        val starAlpha = (0.95f + currentTapLum * 0.05f).coerceIn(0f, 1f)
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

            // ÇEMBER İÇİ MERKEZ METİNLERİ VE BÜYÜK SAYILAR
            // Font ölçeği (LocalDensity) ve ringSize'a dinamik olarak uyum sağlayan responsive düzen
            val currentFontScale = LocalDensity.current.fontScale
            val isConstrainedRing = ringSize < 240.dp || currentFontScale > 1.25f
            val isExtremeFontScale = currentFontScale > 1.35f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = if (isConstrainedRing) 8.dp else 14.dp)
            ) {
                // Arapça Lafız (Altın Sarısı Işıltı ve Yumuşak Parıltı Efekti)
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
                        Text(
                            text = "%${((progress.coerceIn(0f, 1f)) * 100).toInt()}",
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
 * 1. GÜL TEMASI EFEKTİ: Ethereal Gül Yaprağı Hüzmeleri & Pembe Altın Parıltılar
 */
private fun DrawScope.drawRosePetalAuraEffect(
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
private fun DrawScope.drawHadraNoorEmeraldEffect(
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
private fun DrawScope.drawKisveGoldLatticeEffect(
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
private fun DrawScope.drawObsidianGlassSmokeEffect(
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
