import re

file_path = "app/src/main/java/com/example/ui/screens/SettingsScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

new_column_content = """    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = strings.settingsTitle,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            ),
            color = colors.text,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(bottom = 12.dp)
        )

        data class SettingsBlock(
            val id: String,
            val content: @Composable () -> Unit
        )

        val blocks = listOf(
            SettingsBlock("language") {
                // 1. LANGUAGE SELECTOR
                SettingsSectionCard(title = strings.language) {
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
                            val isSelected = state.settings.lang == code
                            LanguageChip(
                                label = label,
                                isSelected = isSelected,
                                onClick = { viewModel.incrementSettingUsage("language"); viewModel.setLanguage(code) },
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
                            val isSelected = state.settings.lang == code
                            LanguageChip(
                                label = label,
                                isSelected = isSelected,
                                onClick = { viewModel.incrementSettingUsage("language"); viewModel.setLanguage(code) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            SettingsBlock("theme") {
                // 2. THEME SELECTOR
                SettingsSectionCard(title = strings.themeTitle) {
                    val chunks = AppPalettes.ALL.chunked(4)
                    chunks.forEachIndexed { rowIndex, rowList ->
                        if (rowIndex > 0) Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowList.forEach { palette ->
                                val isSelected = state.settings.themeName == palette.id ||
                                         (palette.id == "emerald" && state.settings.themeName == "hadra") ||
                                        (palette.id == "kisve" && state.settings.themeName == "obsidian")
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) palette.primary.copy(alpha = 0.20f) else colors.inputBg
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) palette.primary else colors.border,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { viewModel.incrementSettingUsage("theme"); viewModel.setTheme(palette.id) }
                                        .padding(vertical = 10.dp, horizontal = 2.dp)
                                        .testTag("theme_picker_${palette.id}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(palette.primary)
                                            .border(2.dp, palette.card, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(palette.card)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = palette.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = if (isSelected) palette.primary else colors.text,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            },
            SettingsBlock("fontScale") {
                // 3. FONT SCALE
                SettingsSectionCard(title = strings.fontScaleTitle) {
                    Text(
                        text = strings.fontScaleDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val scales = listOf(
                        Pair(0.9f, strings.fontScaleSmall),
                        Pair(1.0f, strings.fontScaleNormal),
                        Pair(1.15f, strings.fontScaleLarge),
                        Pair(1.3f, strings.fontScaleHuge)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        scales.forEach { (scaleVal, scaleLabel) ->
                            val isSelected = kotlin.math.abs(state.settings.fontScale - scaleVal) < 0.05f
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) colors.primary else colors.inputBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) colors.primary else colors.border
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.incrementSettingUsage("fontScale"); viewModel.setFontScale(scaleVal) }
                                    .testTag("font_scale_$scaleLabel")
                            ) {
                                Text(
                                    text = scaleLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) colors.bg else colors.text,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            },
            SettingsBlock("counter") {
                // 4. COUNTER PREFERENCES
                SettingsSectionCard(title = strings.counterPrefsTitle) {
                    // Countdown Mode Switch
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
                            checked = state.settings.countdownMode,
                            onCheckedChange = { viewModel.incrementSettingUsage("counter"); viewModel.toggleCountdown() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.bg,
                                checkedTrackColor = colors.primary
                            ),
                            modifier = Modifier.testTag("switch_countdown_mode")
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Target Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.dailyTargetTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.text
                            )
                            Text(
                                text = strings.dailyTargetDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val newTarget = maxOf(500L, state.settings.dailyTarget - 1000L)
                                viewModel.incrementSettingUsage("counter")
                                viewModel.setDailyTarget(newTarget)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.inputBg)
                        ) {
                            Text("-1000", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.error)
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.primary.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { showTargetInputDialog = true }
                        ) {
                            Text(
                                text = "${state.settings.dailyTarget}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp
                                ),
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                val newTarget = minOf(999000L, state.settings.dailyTarget + 1000L)
                                viewModel.incrementSettingUsage("counter")
                                viewModel.setDailyTarget(newTarget)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.inputBg)
                        ) {
                            Text("+1000", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1000L, 3000L, 5000L, 10000L, 20000L).forEach { targetValue ->
                            val isSelected = state.settings.dailyTarget == targetValue
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) colors.primary else colors.inputBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) colors.primary else colors.border
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.incrementSettingUsage("counter"); viewModel.setDailyTarget(targetValue) }
                            ) {
                                Text(
                                    text = "${targetValue / 1000}k",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSelected) colors.bg else colors.text,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            SettingsBlock("haptic") {
                SettingsSectionCard(title = strings.hapticFeedbackTitle) {
                    // Haptic Feedback Switch
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
                            checked = state.settings.hapticEnabled,
                            onCheckedChange = { viewModel.incrementSettingUsage("haptic"); viewModel.toggleHaptic() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.bg,
                                checkedTrackColor = colors.primary
                            ),
                            modifier = Modifier.testTag("switch_haptic")
                        )
                    }

                    if (state.settings.hapticEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        // Tap Mode Selector
                        Text(
                            text = strings.hapticTapModeTitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Pair("light", strings.hapticTapLight),
                                Pair("medium", strings.hapticTapMedium),
                                Pair("strong", strings.hapticTapStrong)
                            ).forEach { (modeKey, modeLabel) ->
                                val isSelected = state.settings.hapticTapMode == modeKey
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) colors.primary else colors.inputBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) colors.primary else colors.border
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.incrementSettingUsage("haptic"); viewModel.setHapticTapMode(modeKey) }
                                ) {
                                    Text(
                                        text = modeLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) colors.bg else colors.text,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // Milestone Mode Selector
                        Text(
                            text = strings.hapticMilestoneTitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Pair("double", strings.hapticMilestoneDouble),
                                Pair("long", strings.hapticMilestoneLong),
                                Pair("triple", strings.hapticMilestoneTriple)
                            ).forEach { (modeKey, modeLabel) ->
                                val isSelected = state.settings.hapticMilestoneMode == modeKey
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) colors.primary else colors.inputBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) colors.primary else colors.border
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.incrementSettingUsage("haptic"); viewModel.setHapticMilestoneMode(modeKey) }
                                ) {
                                    Text(
                                        text = modeLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) colors.bg else colors.text,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            },
            SettingsBlock("habits") {
                SettingsSectionCard(title = strings.remindersTitle) {
                    // Target Reminder Switch (21:00 Daily)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.targetReminderTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.text
                            )
                            Text(
                                text = strings.targetReminderDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                        Switch(
                            checked = state.settings.targetReminderEnabled,
                            onCheckedChange = { viewModel.incrementSettingUsage("habits"); viewModel.toggleTargetReminder(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.bg,
                                checkedTrackColor = colors.primary
                            ),
                            modifier = Modifier.testTag("switch_target_reminder")
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Keep Awake Switch
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
                            checked = state.settings.keepAwakeEnabled,
                            onCheckedChange = { viewModel.incrementSettingUsage("habits"); viewModel.toggleKeepAwake() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.bg,
                                checkedTrackColor = colors.primary
                            ),
                            modifier = Modifier.testTag("switch_keep_awake")
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Reminders Master Switch
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
                            checked = state.settings.reminderEnabled,
                            onCheckedChange = { viewModel.incrementSettingUsage("habits"); viewModel.toggleReminder(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.bg,
                                checkedTrackColor = colors.primary
                            ),
                            modifier = Modifier.testTag("switch_reminders")
                        )
                    }
                }
            },
            SettingsBlock("rounds") {
                SettingsSectionCard(title = strings.roundsTitle) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${strings.roundsTitle}: ${state.settings.completedRounds}",
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
                        onClick = { viewModel.incrementSettingUsage("rounds"); viewModel.setShowRoundModal(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.bg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_trigger_round_modal")
                    ) {
                        Text(strings.startNewRoundBtn, fontWeight = FontWeight.Black)
                    }
                }
            }
        )

        // Sort blocks if autoReorder is enabled
        val statsStr = state.settings.settingsUsageStats
        val usageMap = remember(statsStr) {
            val map = mutableMapOf<String, Int>()
            try {
                val cleanStr = statsStr.removePrefix("{").removeSuffix("}").trim()
                if (cleanStr.isNotEmpty()) {
                    cleanStr.split(",").forEach { pair ->
                        val parts = pair.split(":")
                        if (parts.size == 2) {
                            val key = parts[0].trim().removeSurrounding("\\"")
                            val value = parts[1].trim().toIntOrNull() ?: 0
                            map[key] = value
                        }
                    }
                }
            } catch (e: Exception) {}
            map
        }

        val sortedBlocks = remember(blocks, usageMap, state.settings.autoReorderSettings) {
            if (state.settings.autoReorderSettings) {
                blocks.sortedByDescending { usageMap[it.id] ?: 0 }
            } else {
                blocks
            }
        }

        // Render sorted blocks
        sortedBlocks.forEach { block ->
            block.content()
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Smart Reordering Toggle (always at bottom)
        SettingsSectionCard(title = strings.autoReorderTitle) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.autoReorderTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.text
                    )
                    Text(
                        text = strings.autoReorderDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
                Switch(
                    checked = state.settings.autoReorderSettings,
                    onCheckedChange = { viewModel.toggleAutoReorder() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.bg,
                        checkedTrackColor = colors.primary
                    ),
                    modifier = Modifier.testTag("switch_auto_reorder")
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }"""

start_str = "    Column("
end_str = "        Spacer(modifier = Modifier.height(40.dp))\n    }"
start_idx = content.find(start_str)
end_idx = content.find(end_str) + len(end_str)

new_content = content[:start_idx] + new_column_content + content[end_idx:]

with open(file_path, "w") as f:
    f.write(new_content)

