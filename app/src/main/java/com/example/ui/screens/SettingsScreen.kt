package com.example.ui.screens

import com.example.ui.UiText

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppStrings
import com.example.ui.components.BackupPasswordDialog
import com.example.ui.theme.LocalAppColors
import com.example.ui.viewmodel.DhikrUiState
import com.example.ui.viewmodel.ZikirViewModel

/**
 * Ana Ayarlar Ekranı Orkestratörü (SettingsScreen)
 */
@Composable
fun SettingsScreen(
    state: DhikrUiState,
    viewModel: ZikirViewModel,
    onShowIntro: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = AppStrings.get(state.settings.lang)

    // ViewModel State'leri
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val cloudSyncMessage by viewModel.cloudSyncMessage.collectAsStateWithLifecycle()
    val lastSyncTimestamp by viewModel.lastCloudSyncTimestamp.collectAsStateWithLifecycle()

    val showExportPasswordDialog by viewModel.showExportPasswordDialog.collectAsStateWithLifecycle()
    val showImportPasswordDialog by viewModel.showImportPasswordDialog.collectAsStateWithLifecycle()

    LaunchedEffect(cloudSyncMessage) {
        cloudSyncMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.dismissCloudSyncMessage()
        }
    }

    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. BULUT VE YEREL YEDEKLEME KARTI
        item {
            CloudSection(
                currentUser = currentUser,
                isCloudSyncing = isCloudSyncing,
                lastSyncTimestamp = lastSyncTimestamp,
                lang = state.settings.lang,
                onSignInWithGoogle = { ctx -> viewModel.signInWithGoogle(ctx) },
                onSignOut = { viewModel.signOut() },
                onBackupToCloud = { viewModel.backupToCloud() },
                onRestoreFromCloud = { viewModel.restoreFromCloud() },
                onRequestExportBackup = { viewModel.requestExportLocalBackup() },
                onLaunchImportFile = { importFileLauncher.launch("text/*") },
                context = context
            )
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // 2. GÖRÜNÜM, TEMA, DİL VE YAZI BOYUTU
        item {
            AppearanceSection(
                settings = state.settings,
                onSetTheme = { viewModel.setTheme(it) },
                onSetLanguage = { viewModel.setLanguage(it) },
                onSetFontScale = { viewModel.setFontScale(it) },
                onIncrementUsage = { viewModel.incrementSettingUsage(it) }
            )
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // 2b. ÇOCUK KİLİDİ
        item {
            com.example.ui.components.ChildLockRow(onChanged = {})
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // 3. SAYAÇ, VİRD, EKRAN DAVRANIŞLARI VE GÜNLÜK HATIRLATICILAR
        item {
            CounterSection(
                settings = state.settings,
                onToggleCountdown = { viewModel.toggleCountdown() },
                onToggleKeepAwake = { viewModel.toggleKeepAwake() },
                onShowRoundModal = { viewModel.setShowRoundModal(it) },
                onIncrementUsage = { viewModel.incrementSettingUsage(it) }
            )
        }


        item { Spacer(modifier = Modifier.height(14.dp)) }

        // 4. UYGULAMA HAKKINDA VE SÜRÜM
        item {
            AboutSection(
                lang = state.settings.lang
            )
        }

        item {
            Spacer(modifier = Modifier.heightIn(min = 40.dp))
        }
    }

    // DİYALOGLAR
    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = {
                Text(
                    text = strings.importStatsConfirmTitle,
                    fontWeight = FontWeight.Black,
                    color = colors.text
                )
            },
            text = {
                Text(
                    text = strings.importStatsConfirmMsg,
                    color = colors.textMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestImportLocalBackup(uri)
                        pendingImportUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(
                        text = strings.importStatsBackupBtn,
                        color = colors.bg,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text(text = strings.cancel, color = colors.textMuted)
                }
            },
            containerColor = colors.card
        )
    }

    if (showExportPasswordDialog) {
        BackupPasswordDialog(
            title = UiText.backupEncryptionTitle.get(state.settings.lang),
            message = UiText.backupEncryptionMessage.get(state.settings.lang),
            actionText = UiText.encryptAndShare.get(state.settings.lang),
            cancelText = UiText.cancel.get(state.settings.lang),
            onDismissRequest = { viewModel.dismissExportPasswordDialog() },
            onConfirm = { password ->
                viewModel.exportAndShareStatisticsBackup(context, password) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    if (showImportPasswordDialog) {
        BackupPasswordDialog(
            title = UiText.backupPasswordTitle.get(state.settings.lang),
            message = UiText.backupPasswordMessage.get(state.settings.lang),
            actionText = UiText.openBackup.get(state.settings.lang),
            cancelText = UiText.cancel.get(state.settings.lang),
            onDismissRequest = { viewModel.dismissImportPasswordDialog() },
            onConfirm = { password ->
                viewModel.importStatisticsBackup(
                    context = context,
                    password = password,
                    onSuccess = { restoredCount ->
                        Toast.makeText(context, UiText.backupRestoredToast.format(state.settings.lang, restoredCount), Toast.LENGTH_LONG).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

}
