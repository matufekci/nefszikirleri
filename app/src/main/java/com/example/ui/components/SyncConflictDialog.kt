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

@Composable
fun SyncConflictDialog(
    onDismissRequest: () -> Unit,
    onKeepLocal: () -> Unit,
    onUseRemote: () -> Unit,
    onMerge: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Senkronizasyon Çakışması") },
        text = { 
            Column {
                Text("Farklı bir cihazdan (veya önceki bir oturumdan) gelen verilerle yerel verileriniz arasında çakışma algılandı. Lütfen ne yapmak istediğinizi seçin.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onMerge, modifier = Modifier.fillMaxWidth()) {
                    Text("Birleştir (En Güvenli - Max Sayaçlar)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onUseRemote, modifier = Modifier.fillMaxWidth()) {
                    Text("Buluttan Al (Yerel verinin üzerine yazar)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onKeepLocal, modifier = Modifier.fillMaxWidth()) {
                    Text("Yerel Veriyi Koru (Bulutun üzerine yazar)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("İptal")
            }
        }
    )
}
