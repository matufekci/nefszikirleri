package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Tune
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
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors

/**
 * Sayaç ve Vird Davranış Ayarları Bölümü (Geri Sayım, Ekranı Açık Tutma, Tur Yönetimi ve Günlük Hatırlatıcılar)
 */
@Composable
fun CounterSection(
    settings: AppSettings,
    onToggleCountdown: () -> Unit,
    onToggleKeepAwake: () -> Unit,
    onShowRoundModal: (Boolean) -> Unit,
    onIncrementUsage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(settings.lang)

    var counterExpanded by rememberSaveable { mutableStateOf(false) }
    var roundsExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. SAYICI, VİRD VE HATIRLATICILAR (SAYIM, EKRAN VE GÜNLÜK BİLDİRİMLER)
        SettingsCollapsibleCard(
            title = strings.counterPrefsTitle.toTitleCase(),
            icon = Icons.Rounded.Tune,
            isExpanded = counterExpanded,
            onToggle = { counterExpanded = !counterExpanded },
            strings = strings
        ) {
            // Geri Sayım Modu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.countdownMode,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.text
                    )
                    Text(
                        text = strings.countdownModeDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
                Switch(
                    checked = settings.countdownMode,
                    onCheckedChange = {
                        onIncrementUsage("counter")
                        onToggleCountdown()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.bg,
                        checkedTrackColor = colors.primary
                    ),
                    modifier = Modifier.testTag("switch_countdown_mode")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Ekranı Açık Tut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.keepAwake,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.text
                    )
                    Text(
                        text = strings.keepAwakeDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
                Switch(
                    checked = settings.keepAwakeEnabled,
                    onCheckedChange = {
                        onIncrementUsage("habits")
                        onToggleKeepAwake()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.bg,
                        checkedTrackColor = colors.primary
                    ),
                    modifier = Modifier.testTag("switch_keep_awake")
                )
            }

        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. TAMAMLANAN TUR VE TUR YÖNETİMİ
        SettingsCollapsibleCard(
            title = strings.roundsTitle.toTitleCase(),
            icon = Icons.Rounded.EmojiEvents,
            isExpanded = roundsExpanded,
            onToggle = { roundsExpanded = !roundsExpanded },
            strings = strings
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${strings.roundsTitle}: ${settings.completedRounds}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = colors.gold
                        )
                    )
                    Text(
                        text = strings.roundsDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onIncrementUsage("rounds")
                    onShowRoundModal(true)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.bg
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("btn_trigger_round_modal")
            ) {
                Text(strings.startNewRoundBtn, fontWeight = FontWeight.Black)
            }
        }
    }
}
