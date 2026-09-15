package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp




data class ActionShadowSpec(
    val elevation: Dp = 4.dp,
    val spotAlpha: Float = 0.35f,
    val ambientAlpha: Float = 0.15f,
    val spotColor: Color = Color.Black,
    val ambientColor: Color = Color.Black
)

data class AppThemeColors(
    val id: String,
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
    val error: Color,
    val isDark: Boolean,
    val isMetallic: Boolean = false,
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




object AppPalettes {
    // 1. BEYAZ (Eski: Gündüz • Hadrâ & Beyaz / Sage-Cream)
    val HadraGunduz = AppThemeColors(
        id = "hadra_gunduz",
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
        error = Color(0xFFDC2626),
        isDark = false,
        isMetallic = false,
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
        error = Color(0xFFEF4444),
        isDark = true,
        isMetallic = false,
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
        error = Color(0xFFEF4444),
        isDark = true,
        isMetallic = false,
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

    // 4. PEMBE LÜKS (AÇIK TON: pudra/gül zemini, ahududu primary + roze altın;
    //    ışıltılı "cicili bicili" ama ferah, gündüz kullanıma uygun)
    val PembeLux = AppThemeColors(
        id = "pembe_lux",
        bg = Color(0xFFFFF1F6), // pudra pembe zemin
        surface = Color(0xFFFEE4EE),
        card = Color(0xFFFBD3E3),
        inputBg = Color(0xFFF8C3D9),
        primary = Color(0xFFD6367F), // ahududu pembesi (açık zeminde okunur)
        primaryVariant = Color(0xFFB02568),
        secondary = Color(0xFFE56FA8),
        gold = Color(0xFFC08A3E), // sıcak roze altın vurgu
        text = Color(0xFF471D33), // koyu erik metin
        textMuted = Color(0xFF8C5A72),
        border = Color(0xFFD6367F).copy(alpha = 0.35f),
        error = Color(0xFFEF4444),
        isDark = false,
        isMetallic = false,
        glowIntensity = 0.25f,
        reflectionColor = Color(0xFFFFFFFF),
        actionShadow = ActionShadowSpec(
            elevation = 4.5.dp,
            spotAlpha = 0.18f,
            ambientAlpha = 0.10f,
            spotColor = Color(0xFFD6367F),
            ambientColor = Color(0xFFFBD3E3)
        )
    )

    // Renk Temaları Listesi (Beyaz, Yeşil, Siyah, Pembe Lüks) - Canonical 4
    val ALL = listOf(HadraGunduz, HadraGece, Siyah, PembeLux)


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
}
