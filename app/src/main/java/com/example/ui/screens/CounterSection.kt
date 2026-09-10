package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Remove
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
import com.example.data.model.ReminderSlot
import com.example.ui.theme.LocalAppColors
import java.util.Locale

/**
 * Sayaç ve Vird Davranış Ayarları Bölümü (Geri Sayım, Ekranı Açık Tutma, Tur Yönetimi ve Günlük Hatırlatıcılar)
 */
@Composable
fun CounterSection(
    settings: AppSettings,
    reminderSlots: List<ReminderSlot>,
    onToggleCountdown: () -> Unit,
    onToggleKeepAwake: () -> Unit,
    onShowRoundModal: (Boolean) -> Unit,
    onReminderToggleRequested: (Boolean) -> Unit,
    onUpdateReminderSlot: (ReminderSlot, Int, Int) -> Unit,
    onRemoveReminderSlot: (Long) -> Unit,
    onAddReminderSlot: (Int, Int) -> Unit,
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
            summary = getSettingsSummary("counter_and_reminders", settings.lang),
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

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // GÜNLÜK HATIRLATICI ÖZELLİĞİ (Doğrudan buraya ekleniyor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.dailyReminders,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.text
                    )
                    Text(
                        text = strings.dailyRemindersDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
                Switch(
                    checked = settings.reminderEnabled,
                    onCheckedChange = {
                        onIncrementUsage("habits")
                        onReminderToggleRequested(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.bg,
                        checkedTrackColor = colors.primary
                    ),
                    modifier = Modifier.testTag("switch_reminders")
                )
            }

            if (settings.reminderEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                reminderSlots.forEach { slot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(colors.inputBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val incHourDesc = when (settings.lang.lowercase()) {
                                "ar" -> "زيادة الساعة"
                                "de" -> "Stunde erhöhen"
                                "fr" -> "Augmenter l'heure"
                                "en" -> "Increase hour"
                                else -> "Saati artır"
                            }
                            val decHourDesc = when (settings.lang.lowercase()) {
                                "ar" -> "إنقاص الساعة"
                                "de" -> "Stunde verringern"
                                "fr" -> "Diminuer l'heure"
                                "en" -> "Decrease hour"
                                else -> "Saati azalt"
                            }
                            val incMinDesc = when (settings.lang.lowercase()) {
                                "ar" -> "زيادة الدقائق"
                                "de" -> "Minute erhöhen"
                                "fr" -> "Augmenter les minutes"
                                "en" -> "Increase minute"
                                else -> "Dakikayı artır"
                            }
                            val decMinDesc = when (settings.lang.lowercase()) {
                                "ar" -> "إنقاص الدقائق"
                                "de" -> "Minute verringern"
                                "fr" -> "Diminuer les minutes"
                                "en" -> "Decrease minute"
                                else -> "Dakikayı azalt"
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { onUpdateReminderSlot(slot, 1, 0) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = incHourDesc, modifier = Modifier.size(16.dp), tint = colors.primary)
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d", slot.hour),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = colors.text
                                )
                                IconButton(
                                    onClick = { onUpdateReminderSlot(slot, -1, 0) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Rounded.Remove, contentDescription = decHourDesc, modifier = Modifier.size(16.dp), tint = colors.primary)
                                }
                            }
                            Text(
                                ":",
                                fontWeight = FontWeight.Black,
                                color = colors.text,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { onUpdateReminderSlot(slot, 0, 15) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = incMinDesc, modifier = Modifier.size(16.dp), tint = colors.primary)
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d", slot.minute),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = colors.text
                                )
                                IconButton(
                                    onClick = { onUpdateReminderSlot(slot, 0, -15) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Rounded.Remove, contentDescription = decMinDesc, modifier = Modifier.size(16.dp), tint = colors.primary)
                                }
                            }
                        }

                        IconButton(
                            onClick = { onRemoveReminderSlot(slot.id) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = strings.deleteBtn,
                                tint = colors.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                if (reminderSlots.size < 5) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            onAddReminderSlot(8, 0)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = strings.addReminderBtn, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.addReminderBtn)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. TAMAMLANAN TUR VE TUR YÖNETİMİ
        SettingsCollapsibleCard(
            title = strings.roundsTitle.toTitleCase(),
            summary = getSettingsSummary("rounds", settings.lang),
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
