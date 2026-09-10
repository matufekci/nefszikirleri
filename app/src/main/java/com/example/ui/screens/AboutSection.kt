package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors

/**
 * Uygulama Hakkında, Sürüm ve Manevi Rehberlik Bölümü
 */
@Composable
fun AboutSection(
    lang: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = colors.card,
            border = BorderStroke(1.2.dp, colors.gold.copy(alpha = 0.5f)),
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = if (colors.isDark) 0.dp else 3.dp,
                    shape = CircleShape,
                    spotColor = colors.gold.copy(alpha = 0.35f)
                )
        ) {
            Image(
                painter = painterResource(id = com.example.R.drawable.img_nefs_app_icon_1787782143783),
                contentDescription = strings.appLogo,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nefs Zikirleri",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                color = colors.text
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = colors.gold.copy(alpha = 0.16f),
            border = BorderStroke(0.8.dp, colors.gold.copy(alpha = 0.4f)),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "v2.0 • Ultra Edition",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.gold
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (lang.lowercase()) {
                "ar" -> "تطبيق أذكار وتزكية النفس المبارك"
                "de" -> "Spirituelle Nafs-Zikr & Vird Begleiter"
                "fr" -> "Compagnon spirituel des dhikrs de l'âme"
                "en" -> "Spiritual Stations & Nafs Dhikr Companion"
                else -> "Nefs Zikirleri Rehberi"
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted
        )
    }
}
