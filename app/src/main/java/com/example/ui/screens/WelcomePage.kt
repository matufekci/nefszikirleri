package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors

/**
 * 1. ADIM: Karşılama Sayfası (WelcomePage)
 * Manevi logo, dönen altın yörünge, Besmele-i Şerif ve uygulama tanıtımı.
 */
@Composable
fun WelcomePage(
    haloScale: Float,
    haloAlpha: Float,
    shimmerRotation: Float,
    lang: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Manevi Amblem ve Lüks Işık Halkası
        OnboardingEmblemWithOrbit(
            haloScale = haloScale,
            haloAlpha = haloAlpha,
            shimmerRotation = shimmerRotation,
            appLogoDescription = strings.appLogo
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Besmele-i Şerif
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            ),
            color = colors.gold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Ana Başlık
        Text(
            text = when (lang) {
                "ar" -> "أذكار مراتب النفس"
                "de" -> "Dhikr der Nafs-Stufen"
                "fr" -> "Degrés de l'Âme & Zikr"
                "en" -> "Spiritual Stations of Nafs"
                else -> "Nefs Zikirleri"
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            ),
            color = colors.text,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Açıklama Metni
        Text(
            text = when (lang) {
                "ar" -> "تركيبة ١٥ مرتبة لتزكية النفس، متابعة الورد اليومي، هداية روحية وسكينة للقلب بتصميم راقٍ ومريح."
                "de" -> "15 spirituelle Nafs-Stufen, täglicher Dhikr-Tracker, spirituelle Weisheit und Seelenfrieden."
                "fr" -> "15 degrés spirituels de l'âme, suivi du wird quotidien, sagesse spirituelle et paix du cœur."
                "en" -> "15 Spiritual stations of Nafs, daily dhikr tracker, spiritual wisdom, and radiant peace for the heart."
                else -> "Nefs Zikirleri, günlük vird takibi ve ruhu dinlendiren estetik ile kalbinize huzur."
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp
            ),
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
