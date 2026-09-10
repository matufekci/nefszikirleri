package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors
import com.example.util.NumberFormatter

/**
 * Zikri Sıfırlama Onay Diyaloğu
 */
@Composable
fun ResetZikirDialog(
    zikirName: String,
    lang: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.resetThis, fontWeight = FontWeight.Bold, color = colors.text) },
        text = { Text("$zikirName - ${strings.resetConfirm}", color = colors.textMuted) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = colors.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(strings.reset, color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = colors.textMuted)
            }
        },
        containerColor = colors.card,
        shape = RoundedCornerShape(18.dp)
    )
}

/**
 * Manuel Sayı Ekleme ve Çıkarma Diyaloğu
 */
@Composable
fun ManualAmountDialog(
    lang: String,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)
    var manualAmountText by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.manualTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.text
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.manualDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = manualAmountText,
                    onValueChange = { manualAmountText = it.filter { ch -> ch.isDigit() } },
                    placeholder = { Text(strings.manualPlaceholder, color = colors.textMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.inputBg,
                        unfocusedContainerColor = colors.inputBg,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_amount")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val amt = manualAmountText.toLongOrNull()
                            if (amt != null && amt > 0) {
                                onAdd(amt)
                                manualAmountText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.bg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_manual_add")
                    ) {
                        Text(strings.addBtn, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val amt = manualAmountText.toLongOrNull()
                            if (amt != null && amt > 0) {
                                onRemove(amt)
                                manualAmountText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.error,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_manual_remove")
                    ) {
                        Text(strings.removeBtn, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = colors.textMuted)
            }
        },
        containerColor = colors.card,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Günlük Vird Hedefi Ayarlama Diyaloğu
 */
@Composable
fun DailyTargetDialog(
    settings: AppSettings,
    onAdjustTarget: (Long) -> Unit,
    onSetTarget: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(settings.lang)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.dailyTargetTitle,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.dailyTargetLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Stepper: -1000 [ Target ] +1000
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onAdjustTarget(-1000L) },
                        enabled = settings.dailyTarget > 1000L,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.inputBg,
                            contentColor = colors.text
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Text("− ${NumberFormatter.format(1000L, settings.lang)}", fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 13.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = NumberFormatter.format(settings.dailyTarget, settings.lang),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = strings.perDay,
                            style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted),
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = { onAdjustTarget(1000L) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.bg
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+ ${NumberFormatter.format(1000L, settings.lang)}", fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hazır Ön Tanımlı Hedefler
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(1000L, 3000L, 5000L, 10000L, 20000L).forEach { target ->
                        val isSelected = settings.dailyTarget == target
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) colors.primary else colors.inputBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) colors.primary else colors.border
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clickable { onSetTarget(target) }
                        ) {
                            Text(
                                text = NumberFormatter.format(target, settings.lang),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) colors.bg else colors.text,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(strings.save, color = colors.bg, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = colors.card,
        shape = RoundedCornerShape(20.dp)
    )
}
