package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp




enum class ThemeStyle {
    NEON_CYBER,
    GLASS_LIGHT,
    DEEP_TECH,
    SOFT_MINIMAL,
    OLED_GLOW,
    METALLIC_GOLD,
    OBSIDIAN_GLASS
}

data class ActionShadowSpec(
    val elevation: Dp = 4.dp,
    val spotAlpha: Float = 0.35f,
    val ambientAlpha: Float = 0.15f,
    val spotColor: Color = Color.Black,
    val ambientColor: Color = Color.Black
)

data class AppThemeColors(
    val id: String,
    val name: String,
    val bg: Color,
    val surface: Color,
    val card: Color,
    val inputBg: Color,
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val gold: Color,
    val text: Color,
    val textMuted: Color,
    val border: Color,
    val success: Color,
    val error: Color,
    val isDark: Boolean,
    val glowColor: Color = primary.copy(alpha = 0.25f),
    val themeStyle: ThemeStyle = ThemeStyle.NEON_CYBER,
    val isMetallic: Boolean = false,
    val specularIntensity: Float = 0.65f,
    val glowIntensity: Float = 0.40f,
    val reflectionColor: Color = Color.White,
    val actionShadow: ActionShadowSpec = ActionShadowSpec()
)

/**
 * Temaya duyarlı, yumuşak ve kısa mesafeli mikro gölge Modifier'ı.
 * Aktif temanın optik yoğunluk, bulanıklık yarıçapı ve renk derinliğine göre otomatik uyarlanır.
 */
fun Modifier.actionButtonShadow(
    colors: AppThemeColors,
    shape: Shape = RoundedCornerShape(12.dp),
    customElevation: Dp? = null,
    overrideSpotColor: Color? = null
): Modifier {
    val spec = colors.actionShadow
    val elevation = customElevation ?: spec.elevation
    val spot = overrideSpotColor ?: spec.spotColor.copy(alpha = spec.spotAlpha)
    val ambient = spec.ambientColor.copy(alpha = spec.ambientAlpha)
    return this.shadow(
        elevation = elevation,
        shape = shape,
        spotColor = spot,
        ambientColor = ambient
    )
}



@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current
    when (colors.themeStyle) {
        ThemeStyle.METALLIC_GOLD -> {
            Surface(
                shape = shape,
                color = colors.card,
                border = BorderStroke(
                    1.2.dp,
                    Brush.sweepGradient(
                        listOf(
                            colors.gold.copy(alpha = 0.85f),
                            Color(0xFFFFFBEB).copy(alpha = 0.95f),
                            colors.primary.copy(alpha = 0.40f),
                            Color(0xFFD4AF37).copy(alpha = 0.85f)
                        )
                    )
                ),
                modifier = modifier.shadow(
                    elevation = 14.dp,
                    shape = shape,
                    spotColor = colors.gold.copy(alpha = 0.55f),
                    ambientColor = colors.primary.copy(alpha = 0.25f)
                ),
                content = content
            )
        }
        ThemeStyle.OBSIDIAN_GLASS -> {
            Surface(
                shape = shape,
                color = colors.card.copy(alpha = 0.92f),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.22f),
                            colors.border.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    )
                ),
                modifier = modifier.shadow(
                    elevation = 8.dp,
                    shape = shape,
                    spotColor = Color(0x66000000),
                    ambientColor = Color(0x40000000)
                ),
                content = content
            )
        }
        ThemeStyle.NEON_CYBER -> {
            Surface(
                shape = shape,
                color = colors.card,
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.65f),
                            colors.border.copy(alpha = 0.35f)
                        )
                    )
                ),
                modifier = modifier.shadow(12.dp, shape, spotColor = colors.glowColor, ambientColor = colors.glowColor),
                content = content
            )
        }
        ThemeStyle.GLASS_LIGHT -> {
            Surface(
                shape = shape,
                color = colors.card.copy(alpha = 0.90f),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.85f),
                            colors.border.copy(alpha = 0.45f)
                        )
                    )
                ),
                modifier = modifier.shadow(
                    elevation = 8.dp,
                    shape = shape,
                    spotColor = colors.primary.copy(alpha = 0.12f),
                    ambientColor = Color(0x0A000000)
                ),
                content = content
            )
        }
        ThemeStyle.DEEP_TECH -> {
            Surface(
                shape = shape,
                color = colors.card,
                border = BorderStroke(1.2.dp, Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.8f), colors.card.copy(alpha=0f)))),
                modifier = modifier,
                content = content
            )
        }
        ThemeStyle.SOFT_MINIMAL -> {
            Surface(
                shape = shape,
                color = colors.card,
                border = BorderStroke(1.dp, colors.border),
                modifier = modifier.shadow(
                    elevation = if (colors.isDark) 0.dp else 4.dp,
                    shape = shape,
                    spotColor = Color(0x1F000000),
                    ambientColor = Color(0x0F000000)
                ),
                content = content
            )
        }
        ThemeStyle.OLED_GLOW -> {
             Surface(
                shape = shape,
                color = colors.card,
                border = BorderStroke(1.dp, colors.border),
                modifier = modifier.shadow(6.dp, shape, spotColor = colors.primary, ambientColor = colors.primary),
                content = content
            )
        }
    }
}

object AppPalettes {
    // 1. BEYAZ (Eski: Gündüz • Hadrâ & Beyaz / Sage-Cream)
    val HadraGunduz = AppThemeColors(
        id = "hadra_gunduz",
        name = "Beyaz",
        bg = Color(0xFFE8EFEA), // Gözü yormayan yumuşak mat adaçayı / bej zemin
        surface = Color(0xFFF3F7F4),
        card = Color(0xFFFFFFFF),
        inputBg = Color(0xFFDFE9E2),
        primary = Color(0xFF047857),
        primaryVariant = Color(0xFF065F46),
        secondary = Color(0xFF10B981),
        gold = Color(0xFFB45309),
        text = Color(0xFF13221A),
        textMuted = Color(0xFF455A4F),
        border = Color(0xFFCADBD0),
        success = Color(0xFF047857),
        error = Color(0xFFDC2626),
        isDark = false,
        glowColor = Color(0xFF047857).copy(alpha = 0.18f),
        themeStyle = ThemeStyle.GLASS_LIGHT,
        isMetallic = false,
        specularIntensity = 0.70f,
        glowIntensity = 0.22f,
        reflectionColor = Color(0xFFFFFFFF),
        actionShadow = ActionShadowSpec(
            elevation = 3.dp,
            spotAlpha = 0.18f,
            ambientAlpha = 0.08f,
            spotColor = Color(0xFF047857),
            ambientColor = Color(0x12000000)
        )
    )

    // 2. YEŞİL (Yeşil Zemin & Altın Sarısı Vurgu ve Yazılar)
    val HadraGece = AppThemeColors(
        id = "hadra_gece",
        name = "Yeşil",
        bg = Color(0xFF071F14),
        surface = Color(0xFF0C291B),
        card = Color(0xFF113523),
        inputBg = Color(0xFF184730),
        primary = Color(0xFFFBBF24), // Altın Sarısı (Bold/vurgulu yazılar ve aktif ögeler)
        primaryVariant = Color(0xFFD97706), // Sıcak Kehribar / Koyu Altın
        secondary = Color(0xFFFDE68A), // Açık Parlak Altın
        gold = Color(0xFFFBBF24), // Altın Sarısı
        text = Color(0xFFF0FDF4),
        textMuted = Color(0xFF86A79C),
        border = Color(0xFFFBBF24).copy(alpha = 0.35f),
        success = Color(0xFF10B981),
        error = Color(0xFFEF4444),
        isDark = true,
        glowColor = Color(0xFFFBBF24).copy(alpha = 0.48f),
        themeStyle = ThemeStyle.NEON_CYBER,
        isMetallic = false,
        specularIntensity = 0.82f,
        glowIntensity = 0.50f,
        reflectionColor = Color(0xFFFFFBEB),
        actionShadow = ActionShadowSpec(
            elevation = 4.dp,
            spotAlpha = 0.42f,
            ambientAlpha = 0.20f,
            spotColor = Color(0xFFFBBF24),
            ambientColor = Color(0xFF071F14)
        )
    )

    // 3. SİYAH (Eski: Gece • Oniks & Siyah / Obsidian Black)
    val Siyah = AppThemeColors(
        id = "siyah",
        name = "Siyah",
        bg = Color(0xFF050507),
        surface = Color(0xFF0C0D10),
        card = Color(0xFF14161B),
        inputBg = Color(0xFF1D2027),
        primary = Color(0xFF94A3B8),
        primaryVariant = Color(0xFF64748B),
        secondary = Color(0xFFCBD5E1),
        gold = Color(0xFFE2E8F0),
        text = Color(0xFFF8FAFC),
        textMuted = Color(0xFF94A3B8),
        border = Color(0xFF94A3B8).copy(alpha = 0.35f),
        success = Color(0xFF10B981),
        error = Color(0xFFEF4444),
        isDark = true,
        glowColor = Color(0xFF64748B).copy(alpha = 0.35f),
        themeStyle = ThemeStyle.OBSIDIAN_GLASS,
        isMetallic = false,
        specularIntensity = 0.60f,
        glowIntensity = 0.35f,
        reflectionColor = Color(0xFFF1F5F9),
        actionShadow = ActionShadowSpec(
            elevation = 4.5.dp,
            spotAlpha = 0.50f,
            ambientAlpha = 0.25f,
            spotColor = Color(0xFF000000),
            ambientColor = Color(0xFF1E293B)
        )
    )

    // 4. PEMBE LÜKS (Kadınlar için: derin erik zemin, canlı pembe + roze altın,
    //    yüksek parlama/metalik yansıma ile ışıltılı "cicili bicili" his)
    val PembeLux = AppThemeColors(
        id = "pembe_lux",
        name = "Pembe Lüks",
        bg = Color(0xFF241019), // deri erik / bordo-kakül zemin
        surface = Color(0xFF2F1622),
        card = Color(0xFF3B1B2C),
        inputBg = Color(0xFF4A2439),
        primary = Color(0xFFEE6FAE), // canlı orkide pembesi
        primaryVariant = Color(0xFFD14E93),
        secondary = Color(0xFFF9A8CD),
        gold = Color(0xFFE5B769), // roze altın vurgu
        text = Color(0xFFFCEEF5),
        textMuted = Color(0xFFD5A9C0),
        border = Color(0xFFEE6FAE).copy(alpha = 0.38f),
        success = Color(0xFF10B981),
        error = Color(0xFFEF4444),
        isDark = true,
        glowColor = Color(0xFFEE6FAE).copy(alpha = 0.30f),
        themeStyle = ThemeStyle.OLED_GLOW,
        isMetallic = true,
        specularIntensity = 0.90f,
        glowIntensity = 0.40f,
        reflectionColor = Color(0xFFF9CFE4),
        actionShadow = ActionShadowSpec(
            elevation = 4.5.dp,
            spotAlpha = 0.45f,
            ambientAlpha = 0.22f,
            spotColor = Color(0xFFEE6FAE),
            ambientColor = Color(0xFF3B1B2C)
        )
    )

    // Renk Temaları Listesi (Beyaz, Yeşil, Siyah, Pembe Lüks) - Canonical 4
    val ALL = listOf(HadraGunduz, HadraGece, Siyah, PembeLux)

    // Legacy Aliases for backward compat
    val Emerald = HadraGece
    val Rahle = HadraGunduz
    val Obsidian = Siyah
    val Beyaz = HadraGunduz
    val Yesil = HadraGece
    val CyberNeon = HadraGece
    val GlassCloud = HadraGunduz
    val DeepTech = HadraGece
    val SoftMinimal = HadraGunduz
    val OledGlow = Siyah

    // Full mapping table: legacy 10 + aliases -> canonical
    private val legacyMapping = mapOf(
        // White family -> HadraGunduz
        "hadra_gunduz" to HadraGunduz, "hadra_light" to HadraGunduz, "rahle" to HadraGunduz,
        "light" to HadraGunduz, "inci" to HadraGunduz, "white" to HadraGunduz,
        "hadra_white" to HadraGunduz, "beyaz" to HadraGunduz, "olive" to HadraGunduz,
        "sahara" to HadraGunduz, "amethyst" to HadraGunduz, "rose" to HadraGunduz,
        // Green family -> HadraGece
        "hadra_gece" to HadraGece, "hadra_dark" to HadraGece, "emerald" to HadraGece,
        "hadra" to HadraGece, "yesil" to HadraGece, "green" to HadraGece,
        "night" to HadraGece, "leyl" to HadraGece, "kisve" to HadraGece,
        "turq" to HadraGece, "kudus" to HadraGece, "iznik" to HadraGece,
        "gul" to HadraGece, "amber" to HadraGece, "kandil" to HadraGece,
        // Black family -> Siyah
        "siyah" to Siyah, "obsidian" to Siyah, "black" to Siyah,
        "pembe" to PembeLux, "pembe_lux" to PembeLux, "pink" to PembeLux,
        "pinky" to PembeLux,
        "oniks" to Siyah, "oled" to Siyah, "pure_black" to Siyah
    )

    fun normalizeId(id: String): String {
        val lower = id.lowercase()
        // If direct match in ALL, return its id
        ALL.find { it.id.equals(lower, ignoreCase = true) }?.let { return it.id }
        // Check legacy mapping
        legacyMapping[lower]?.let { return it.id }
        return "hadra_gece" // default fallback
    }

    fun get(id: String): AppThemeColors {
        val lower = id.lowercase()
        ALL.find { it.id.equals(lower, ignoreCase = true) }?.let { return it }
        return legacyMapping[lower] ?: HadraGece
    }

    fun getAllCanonicalIds(): List<String> = ALL.map { it.id }
    fun getLegacyIds(): List<String> = legacyMapping.keys.toList()
}
