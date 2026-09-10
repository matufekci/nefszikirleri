package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors

/**
 * 2. ADIM: Dil ve Bölgesel Ayar Sayfası (FeaturePages - Language Selection)
 */
@Composable
fun LanguageSelectionPage(
    currentLang: String,
    onSelectLang: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(currentLang)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = colors.primary.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Language,
                    contentDescription = strings.language,
                    tint = colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = when (currentLang) {
                "ar" -> "اختر لغتك"
                "de" -> "Wählen Sie Ihre Sprache"
                "fr" -> "Choisissez Votre Langue"
                "en" -> "Select Your Language"
                else -> "Dilinizi Seçin"
            },
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black
            ),
            color = colors.text,
            textAlign = TextAlign.Center
        )

        Text(
            text = when (currentLang) {
                "ar" -> "ستعرض واجهة التطبيق وشروحات الأذكار والورد بهذه اللغة فوراً."
                "de" -> "Die App-Oberfläche und Dhikr-Bedeutungen werden in dieser Sprache angezeigt."
                "fr" -> "L'interface et les explications seront affichées dans cette langue."
                "en" -> "App interface and dhikr meanings will be instantly displayed in this language."
                else -> "Uygulama arayüzü ve zikir açıklamaları bu dilde sunulacaktır."
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        val languages = listOf(
            Triple("tr", "Türkçe", "🇹🇷"),
            Triple("ar", "العربية", "🇸🇦"),
            Triple("en", "English", "🇬🇧"),
            Triple("de", "Deutsch", "🇩🇪"),
            Triple("fr", "Français", "🇫🇷")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            languages.forEach { (code, name, flag) ->
                val isSelected = currentLang.equals(code, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.card,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) colors.primary else colors.border
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (colors.isDark) 0.dp else if (isSelected) 3.dp else 1.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = if (isSelected) colors.primary.copy(alpha = 0.25f) else Color(0x10000000),
                            ambientColor = Color(0x08000000)
                        )
                        .clickable { onSelectLang(code) }
                        .testTag("intro_lang_$code")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = flag,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) colors.primary else colors.text,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = strings.selected,
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
