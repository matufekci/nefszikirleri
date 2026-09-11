package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.UiText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Bulut / yerel veri çakışmasının çözümü.
 *
 * Neden yeniden yazıldı:
 * 1. Bu diyalog uzun süre HİÇ RENDER EDİLMEDİ. `_syncConflictState` iki
 *    yerden dolduruluyor ama hiçbir composable `syncConflictState`'i
 *    izlemiyordu. Sonuç: "Geri Yükle" çakışma durumunda HİÇBİR ŞEY
 *    yüklemeden, üstelik onComplete(true) ile bitiyordu. Artık MainApp'te
 *    gösteriliyor.
 * 2. Metinler sabit Türkçe'ydi. Kullanıcı hangi dili seçtiyse o dil
 *    gösterilmeli; bu yüzden hepsi UiText üzerinden 5 dilde geliyor.
 */
@Composable
fun SyncConflictDialog(
    lang: String,
    remoteBackupTimestamp: Long,
    onDismissRequest: () -> Unit,
    onKeepLocal: () -> Unit,
    onUseRemote: () -> Unit,
    onMerge: () -> Unit
) {
    val dateText = if (remoteBackupTimestamp > 0L) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(remoteBackupTimestamp))
    } else {
        "-"
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(UiText.cloudBackupFoundTitle.get(lang)) },
        text = {
            Column {
                Text(UiText.cloudBackupFoundMessage.format(lang, dateText))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onMerge, modifier = Modifier.fillMaxWidth()) {
                    Text(UiText.cloudChoiceMerge.get(lang))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onUseRemote, modifier = Modifier.fillMaxWidth()) {
                    Text(UiText.cloudChoiceUseRemote.get(lang))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onKeepLocal, modifier = Modifier.fillMaxWidth()) {
                    Text(UiText.cloudChoiceKeepLocal.get(lang))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(UiText.cancel.get(lang))
            }
        }
    )
}
