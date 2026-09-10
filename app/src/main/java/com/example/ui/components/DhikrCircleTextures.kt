package com.example.ui.components

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AppThemeColors

/**
 * Zikir Çemberi Efekt Tipleri ve Temaya Özel Renk Paletleri
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
                specularHighlight = Color(0xFFFFFFFD).copy(alpha = 0.95f),
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
                specularHighlight = Color(0xFFF1F5F9).copy(alpha = 0.60f),
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
