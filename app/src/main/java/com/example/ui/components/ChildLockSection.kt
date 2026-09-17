package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppStrings
import com.example.ui.UiText
import com.example.ui.theme.LocalAppColors
import com.example.util.ChildLockPrefs

/**
 * Ayarlar'daki katlanır çocuk kilidi kartı.
 * - Açma: switch doğrudan (kilidi çocuk değil yetişkin başlatır).
 * - Kapatma GÜVENLİDİR: switch'e basınca 3 sn basılı tutma halkası +
 *   basit çarpma sorusu gelir; ikisi de geçilmeden kilit kapanmaz.
 *   Böylece çocuk switch'i görse bile kilidi devre dışı bırakamaz.
 */
@Composable
fun ChildLockSection(
    lang: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)
    var enabled by remember { mutableStateOf(ChildLockPrefs.isEnabled(context)) }
    var showSafeOff by remember { mutableStateOf(false) }

    com.example.ui.screens.SettingsCollapsibleCard(
        title = UiText.childLock.get(lang),
        icon = Icons.Default.Lock,
        isExpanded = isExpanded,
        onToggle = onToggle,
        strings = strings
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                UiText.childLockDesc.get(lang),
                color = colors.textMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    UiText.childLock.get(lang),
                    color = colors.text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = { on ->
                        if (on) {
                            enabled = true
                            ChildLockPrefs.setEnabled(context, true)
                        } else {
                            // Güvenli kapatma: doğrulama akışını başlat
                            showSafeOff = true
                        }
                    }
                )
            }
        }
    }

    if (showSafeOff) {
        SafeUnlockDialog(
            lang = lang,
            onDismiss = { showSafeOff = false },
            onVerified = {
                enabled = false
                ChildLockPrefs.setEnabled(context, false)
                showSafeOff = false
            }
        )
    }
}

/**
 * Güvenli kapatma doğrulaması: halka 3 sn basılı tutulur, ardından
 * basit çarpma sorusu gelir. İkisi geçilmeden kilit kapanmaz.
 */
@Composable
private fun SafeUnlockDialog(
    lang: String,
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)
    var holdDone by remember { mutableStateOf(false) }
    var pressing by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (pressing) 1f else 0f,
        animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
        label = "safeOffHold"
    )
    LaunchedEffect(progress, pressing) {
        if (pressing && progress >= 0.999f) {
            pressing = false
            holdDone = true
        }
    }

    val (a, b) = remember { (2..9).random() to (2..9).random() }
    val correct = a * b
    val options = remember {
        val wrongs = mutableSetOf<Int>()
        while (wrongs.size < 3) {
            val w = correct + listOf(-9, -7, -5, 5, 7, 9, 11).random()
            if (w != correct && w > 0) wrongs.add(w)
        }
        (listOf(correct) + wrongs.toList()).shuffled()
    }
    var wrongTry by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        title = { Text(UiText.adultCheckTitle.get(lang), color = colors.text) },
        text = {
            Column {
                if (!holdDone) {
                    // 1. aşama: 3 sn basılı tut
                    Text(
                        UiText.lockHoldHint.get(lang),
                        color = colors.text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(50))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        pressing = true
                                        tryAwaitRelease()
                                        pressing = false
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(72.dp),
                            color = colors.primary,
                            strokeWidth = 5.dp,
                            trackColor = colors.border.copy(alpha = 0.3f)
                        )
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                } else {
                    // 2. aşama: çarpma sorusu
                    Text(
                        "$a × $b = ?",
                        color = colors.text,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        options.forEach { option ->
                            OutlinedButton(onClick = {
                                if (option == correct) onVerified() else wrongTry = true
                            }) {
                                Text(option.toString(), color = colors.text)
                            }
                        }
                    }
                    if (wrongTry) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            UiText.adultCheckWrong.get(lang),
                            color = colors.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = colors.textMuted)
            }
        }
    )
}
