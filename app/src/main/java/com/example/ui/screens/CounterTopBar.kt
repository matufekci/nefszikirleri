package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.data.model.AppStrings
import com.example.ui.components.HapticIcons
import com.example.ui.theme.LocalAppColors

/**
 * Zikir Sayacı Üst Kontrol Barı (Titreşim Kademesi & Zen Odaklanma Modu)
 */
@Composable
fun CounterTopBar(
    settings: AppSettings,
    onCycleHapticMode: () -> Unit,
    onToggleZenMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(settings.lang)

    val hapticIcon = HapticIcons.forMode(
        enabled = settings.hapticEnabled,
        tapMode = settings.hapticTapMode
    )
    val hapticDesc = when {
        !settings.hapticEnabled -> strings.hapticOff
        settings.hapticTapMode == "light" -> "${strings.hapticTitle} (${strings.hapticTapLight})"
        settings.hapticTapMode == "medium" -> "${strings.hapticTitle} (${strings.hapticTapMedium})"
        else -> "${strings.hapticTitle} (${strings.hapticTapStrong})"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sol Üst: 3 Kademeli Titreşim Butonu
        IconButton(
            onClick = onCycleHapticMode,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.inputBg)
                .border(
                    1.dp,
                    if (settings.hapticEnabled) colors.gold.copy(alpha = 0.6f) else colors.border,
                    CircleShape
                )
                .testTag("btn_vibration_toggle")
        ) {
            Icon(
                imageVector = hapticIcon,
                contentDescription = hapticDesc,
                tint = if (settings.hapticEnabled) colors.gold else colors.textMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(19.dp)
            )
        }

        // Sağ Üst: Zen / Odaklanma Modu Butonu
        IconButton(
            onClick = { onToggleZenMode(true) },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.inputBg)
                .border(1.dp, colors.border, CircleShape)
                .testTag("btn_zen_mode")
        ) {
            Icon(
                imageVector = Icons.Rounded.Fullscreen,
                contentDescription = strings.zenMode,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
