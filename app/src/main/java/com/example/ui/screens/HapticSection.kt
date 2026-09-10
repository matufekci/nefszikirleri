package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors

/**
 * Zikir Dokunsal Titreşim ve Haptik Geri Bildirim Ayarları Bölümü
 */
@Composable
fun HapticSection(
    settings: AppSettings,
    onToggleHaptic: () -> Unit,
    onSetHapticTapMode: (String) -> Unit,
    onSetHapticMilestoneMode: (String) -> Unit,
    onIncrementUsage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(settings.lang)
    var hapticExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingsCollapsibleCard(
            title = strings.hapticFeedback.toTitleCase(),
            summary = getSettingsSummary("haptic", settings.lang),
            icon = Icons.Rounded.Vibration,
            isExpanded = hapticExpanded,
            onToggle = { hapticExpanded = !hapticExpanded },
            strings = strings
        ) {
            // 1. ANA TİTREŞİM AÇ/KAPAT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.hapticFeedback,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.text
                    )
                    Text(
                        text = strings.hapticFeedbackDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
                Switch(
                    checked = settings.hapticEnabled,
                    onCheckedChange = {
                        onIncrementUsage("haptic")
                        onToggleHaptic()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.bg,
                        checkedTrackColor = colors.primary
                    ),
                    modifier = Modifier.testTag("switch_haptic")
                )
            }

            if (settings.hapticEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                // 2. HER DOKUNUŞ TİTREŞİM ŞİDDETİ
                Text(
                    text = when (settings.lang.lowercase()) {
                        "ar" -> "شدة الاهتزاز عند اللمس"
                        "de" -> "Vibrationsstärke beim Tippen"
                        "fr" -> "Intensité de vibration au toucher"
                        "en" -> "Tap Vibration Intensity"
                        else -> "Her Dokunuş Titreşim Şiddeti"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(8.dp))

                val tapModes = listOf(
                    Triple("light", "Hafif", "Light"),
                    Triple("medium", "Orta", "Medium"),
                    Triple("strong", "Güçlü", "Strong")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tapModes.forEach { (modeKey, trLabel, enLabel) ->
                        val isSelected = settings.hapticTapMode == modeKey
                        val label = when (settings.lang.lowercase()) {
                            "tr" -> trLabel
                            "en" -> enLabel
                            "ar" -> when (modeKey) { "light" -> "خفيف"; "medium" -> "متوسط"; else -> "قوي" }
                            "de" -> when (modeKey) { "light" -> "Leicht"; "medium" -> "Mittel"; else -> "Stark" }
                            "fr" -> when (modeKey) { "light" -> "Léger"; "medium" -> "Moyen"; else -> "Fort" }
                            else -> trLabel
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.primary else colors.inputBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) colors.primary else colors.border
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clickable {
                                    onIncrementUsage("haptic")
                                    onSetHapticTapMode(modeKey)
                                }
                                .testTag("haptic_tap_mode_$modeKey")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) colors.bg else colors.text,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                // 3. 33 VE KATLARINDA MERHALE GERİ BİLDİRİMİ
                Text(
                    text = when (settings.lang.lowercase()) {
                        "ar" -> "اهتزاز مرحلة الـ 33 ومضاعفاتها"
                        "de" -> "33er-Meilenstein Vibration"
                        "fr" -> "Vibration d'étape (tous les 33)"
                        "en" -> "33rd Milestone Vibration"
                        else -> "33'lük Merhale Titreşim Deseni"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(8.dp))

                val milestoneModes = listOf(
                    Triple("double", "Çift Titreşim", "Double"),
                    Triple("long", "Uzun Titreşim", "Long"),
                    Triple("triple", "Üçlü Ritim", "Triple")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    milestoneModes.forEach { (modeKey, trLabel, enLabel) ->
                        val isSelected = settings.hapticMilestoneMode == modeKey
                        val label = when (settings.lang.lowercase()) {
                            "tr" -> trLabel
                            "en" -> enLabel
                            "ar" -> when (modeKey) { "double" -> "مزدوج"; "long" -> "طويل"; else -> "ثلاثي" }
                            "de" -> when (modeKey) { "double" -> "Doppelt"; "long" -> "Lang"; else -> "Dreifach" }
                            "fr" -> when (modeKey) { "double" -> "Double"; "long" -> "Long"; else -> "Triple" }
                            else -> trLabel
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.primary else colors.inputBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) colors.primary else colors.border
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clickable {
                                    onIncrementUsage("haptic")
                                    onSetHapticMilestoneMode(modeKey)
                                }
                                .testTag("haptic_milestone_mode_$modeKey")
                        ) {
                            Text(
                                text = label,
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
            }
        }
    }
}
