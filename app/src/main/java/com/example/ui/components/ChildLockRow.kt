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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppStrings
import com.example.ui.UiText
import com.example.ui.theme.LocalAppColors
import com.example.util.AdaptiveReminderManager
import com.example.util.ChildLockPrefs

/**
 * Ayarlar'daki çocuk kilidi satırı: başlık + açıklama + switch.
 * Durum SharedPreferences'ta tutulur (Room'a migration gerekmez).
 */
@Composable
fun ChildLockRow(onChanged: () -> Unit) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val lang = remember { AdaptiveReminderManager.getAppLanguage(context) }
    var enabled by remember { mutableStateOf(ChildLockPrefs.isEnabled(context)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(android.R.drawable.ic_lock_idle_lock),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    UiText.childLock.get(lang),
                    color = colors.text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    UiText.childLockDesc.get(lang),
                    color = colors.textMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    ChildLockPrefs.setEnabled(context, it)
                    onChanged()
                }
            )
        }
    }
}

/**
 * Sayaç ekranındaki kilit rozeti: 3 sn basılı tutma (ilerleme halkası ile) +
 * yetişkin doğrulama sorusu ile açılır.
 */
@Composable
fun ChildLockBadge(lang: String, onUnlocked: () -> Unit) {
    val colors = LocalAppColors.current
    var pressing by remember { mutableStateOf(false) }
    var showMath by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (pressing) 1f else 0f,
        animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
        label = "lockHold"
    )
    LaunchedEffect(progress, pressing) {
        if (pressing && progress >= 0.999f) {
            pressing = false
            showMath = true
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressing = true
                        tryAwaitRelease()
                        pressing = false
                    }
                )
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (progress > 0f) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(40.dp),
                color = colors.primary,
                strokeWidth = 3.dp,
                trackColor = Color.Transparent
            )
        }
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(android.R.drawable.ic_lock_idle_lock),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            UiText.lockHoldHint.get(lang),
            color = colors.textMuted,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 50.dp)
        )
    }

    if (showMath) {
        ChildLockMathDialog(
            lang = lang,
            onDismiss = { showMath = false },
            onSuccess = {
                showMath = false
                onUnlocked()
            }
        )
    }
}

/** Basit çarpma sorusuyla yetişkin doğrulaması. */
@Composable
private fun ChildLockMathDialog(lang: String, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)
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
        title = {
            Text(UiText.adultCheckTitle.get(lang), color = colors.text)
        },
        text = {
            Column {
                Text(
                    "$a × $b = ?",
                    color = colors.text,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { option ->
                        OutlinedButton(onClick = {
                            if (option == correct) {
                                ChildLockPrefs.setEnabled(context, false)
                                onSuccess()
                            } else {
                                wrongTry = true
                            }
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
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.dismiss, color = colors.textMuted)
            }
        }
    )
}
