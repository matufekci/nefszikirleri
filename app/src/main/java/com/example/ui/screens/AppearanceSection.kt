package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import java.util.Locale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.data.model.AppStrings
import com.example.ui.theme.AppPalettes
import com.example.ui.theme.LocalAppColors

/**
 * Görünüm, Tema, Dil ve Yazı Boyutu Ayarları Bölümü
 */
@Composable
fun AppearanceSection(
    settings: AppSettings,
    onSetTheme: (String) -> Unit,
    onSetLanguage: (String) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onIncrementUsage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(settings.lang)

    var themeExpanded by rememberSaveable { mutableStateOf(false) }
    var languageExpanded by rememberSaveable { mutableStateOf(false) }
    var fontScaleExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. TEMA SEÇİMİ
        SettingsCollapsibleCard(
            title = strings.themeTitle.toTitleCase(),
            summary = getSettingsSummary("theme", settings.lang),
            icon = Icons.Rounded.Palette,
            isExpanded = themeExpanded,
            onToggle = { themeExpanded = !themeExpanded },
            strings = strings
        ) {
            val chunks = AppPalettes.ALL.chunked(2)
            chunks.forEachIndexed { rowIndex, rowList ->
                if (rowIndex > 0) Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowList.forEach { palette ->
                        val isSelected = settings.themeName == palette.id ||
                                (settings.themeName == "emerald" && palette.id == "hadra_gece") ||
                                (settings.themeName == "rahle" && palette.id == "hadra_gunduz") ||
                                (settings.themeName == "obsidian" && palette.id == "siyah")

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) palette.primary.copy(alpha = 0.18f) else colors.inputBg
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) palette.primary else colors.border,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    onIncrementUsage("theme")
                                    onSetTheme(palette.id)
                                    themeExpanded = false
                                }
                                .padding(vertical = 12.dp, horizontal = 10.dp)
                                .testTag("theme_picker_${palette.id}")
                        ) {
                            // Theme Color Dual Swatch (Primary + Background)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(palette.bg)
                                    .border(1.5.dp, palette.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(palette.primary)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = palette.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    ),
                                    color = if (isSelected) palette.primary else colors.text,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (rowList.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. DİL SEÇİMİ
        SettingsCollapsibleCard(
            title = strings.language.toTitleCase(),
            summary = getSettingsSummary("lang", settings.lang),
            icon = Icons.Rounded.Language,
            isExpanded = languageExpanded,
            onToggle = { languageExpanded = !languageExpanded },
            strings = strings
        ) {
            val languages = listOf(
                Pair("tr", "Türkçe 🇹🇷"),
                Pair("ar", "العربية 🇸🇦"),
                Pair("en", "English 🇬🇧"),
                Pair("de", "Deutsch 🇩🇪"),
                Pair("fr", "Français 🇫🇷")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                languages.take(3).forEach { (code, label) ->
                    val isSelected = settings.lang == code
                    LanguageChip(
                        label = label,
                        isSelected = isSelected,
                        onClick = {
                            onIncrementUsage("language")
                            onSetLanguage(code)
                            languageExpanded = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                languages.drop(3).forEach { (code, label) ->
                    val isSelected = settings.lang == code
                    LanguageChip(
                        label = label,
                        isSelected = isSelected,
                        onClick = {
                            onIncrementUsage("language")
                            onSetLanguage(code)
                            languageExpanded = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. YAZI BOYUTU (FONT SCALE)
        SettingsCollapsibleCard(
            title = strings.fontScaleTitle.toTitleCase(),
            summary = getSettingsSummary("font", settings.lang),
            icon = Icons.Rounded.FormatSize,
            isExpanded = fontScaleExpanded,
            onToggle = { fontScaleExpanded = !fontScaleExpanded },
            strings = strings
        ) {
            val scales = listOf(
                Pair(1.0f, strings.fontScaleSmall),
                Pair(1.15f, strings.fontScaleNormal),
                Pair(1.30f, strings.fontScaleLarge),
                Pair(1.45f, strings.fontScaleHuge)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                scales.forEach { (scaleVal, scaleLabel) ->
                    val isSelected = kotlin.math.abs(settings.fontScale - scaleVal) < 0.05f
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) colors.primary else colors.inputBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) colors.primary else colors.border
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clickable {
                                onIncrementUsage("fontScale")
                                onSetFontScale(scaleVal)
                            }
                            .testTag("font_scale_$scaleLabel")
                    ) {
                        Text(
                            text = scaleLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSelected) colors.bg else colors.text,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val context = LocalContext.current
            val systemFontScale = context.resources.configuration.fontScale
            val appFontScale = settings.fontScale
            val effectiveScale = (appFontScale * systemFontScale).coerceAtMost(2.0f)

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colors.inputBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                val formattedApp = String.format(Locale.US, "%.2fx", appFontScale)
                val formattedSys = String.format(Locale.US, "%.2fx", systemFontScale)
                val formattedEff = String.format(Locale.US, "%.2fx", effectiveScale)

                val textInfo = when (settings.lang) {
                    "tr" -> "Uygulama font ölçeği: $formattedApp | Sistem: $formattedSys | Efektif: $formattedEff"
                    "ar" -> "مقياس خط التطبيق: $formattedApp | النظام: $formattedSys | الفعلي: $formattedEff"
                    "de" -> "App-Schriftgrad: $formattedApp | System: $formattedSys | Effektiv: $formattedEff"
                    "fr" -> "Police app : $formattedApp | Système : $formattedSys | Effectif : $formattedEff"
                    else -> "App font scale: $formattedApp | System: $formattedSys | Effective: $formattedEff"
                }
                Text(
                    text = textInfo,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = colors.textMuted,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
