package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    // Bildirim izni rasyonelini daha önce gösterdik mi? (MainApp ile aynı prefs dosyası)
    val prefs = remember { context.getSharedPreferences("nefs_app_prefs", Context.MODE_PRIVATE) }

    // ViewModel State'leri
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val cloudSyncMessage by viewModel.cloudSyncMessage.collectAsStateWithLifecycle()
    val lastSyncTimestamp by viewModel.lastCloudSyncTimestamp.collectAsStateWithLifecycle()

    val showExportPasswordDialog by viewModel.showExportPasswordDialog.collectAsStateWithLifecycle()
    val showImportPasswordDialog by viewModel.showImportPasswordDialog.collectAsStateWithLifecycle()

    var showNotificationRationaleDialog by rememberSaveable { mutableStateOf(false) }
    var isPermanentlyDenied by rememberSaveable { mutableStateOf(false) }

    // Settings'e yönlendirme launcher
    val appSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Settings'ten dönüşte izin kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                viewModel.incrementSettingUsage("habits")
                viewModel.toggleReminder(true)
            }
        }
    }

    // Bildirim İzin Yöneticisi - enhanced with permanently denied handling
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.incrementSettingUsage("habits")
            viewModel.toggleReminder(true)
        } else {
            // Check if permanently denied (user checked "Don't ask again")
            val activity = context as? Activity
            val shouldShowRationale = if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
            } else false

            if (!shouldShowRationale) {
                // Permanently denied - show settings dialog
                isPermanentlyDenied = true
                showNotificationRationaleDialog = true
            } else {
                val deniedMsg = when (state.settings.lang) {
                    "ar" -> "يجب منح إذن التنبيهات لاستلام التذكيرات"
                    "de" -> "Benachrichtigungsberechtigung ist erforderlich, um Erinnerungen zu erhalten"
                    "fr" -> "L'autorisation de notification est requise pour recevoir les rappels"
                    "en" -> "Notification permission is required to receive reminders"
                    else -> "Hatırlatıcıları alabilmek için bildirim iznine ihtiyacımız var"
                }
                Toast.makeText(context, deniedMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onReminderToggleRequested(enabled: Boolean) {
        if (!enabled) {
            viewModel.incrementSettingUsage("habits")
            viewModel.toggleReminder(false)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    viewModel.incrementSettingUsage("habits")
                    viewModel.toggleReminder(true)
                } else {
                    val activity = context as? Activity
                    val shouldShowRationale = if (activity != null) {
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
                    } else false
                    isPermanentlyDenied = !shouldShowRationale && 
                        (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED &&
                         !prefs.getBoolean("notification_rationale_shown", false))
                    // Track that we have shown rationale at least once
                    prefs.edit().putBoolean("notification_rationale_shown", true).apply()
                    showNotificationRationaleDialog = true
                }
            } else {
                viewModel.incrementSettingUsage("habits")
                viewModel.toggleReminder(true)
            }
        }
    }

    fun openAppNotificationSettings() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            }
            appSettingsLauncher.launch(intent)
        } catch (_: Exception) {
            // Fallback to app details
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                appSettingsLauncher.launch(intent)
            } catch (_: Exception) {}
        }
    }

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

        // 3. SAYAÇ, VİRD, EKRAN DAVRANIŞLARI VE GÜNLÜK HATIRLATICILAR
        item {
            CounterSection(
                settings = state.settings,
                reminderSlots = state.reminderSlots,
                onToggleCountdown = { viewModel.toggleCountdown() },
                onToggleKeepAwake = { viewModel.toggleKeepAwake() },
                onShowRoundModal = { viewModel.setShowRoundModal(it) },
                onReminderToggleRequested = { onReminderToggleRequested(it) },
                onUpdateReminderSlot = { slot, h, m -> viewModel.updateReminderSlot(slot, h, m) },
                onRemoveReminderSlot = { viewModel.removeReminderSlot(it) },
                onAddReminderSlot = { h, m -> viewModel.addReminderSlot(h, m) },
                onIncrementUsage = { viewModel.incrementSettingUsage(it) }
            )
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // 4. DOKUNSAL TİTREŞİM VE HAPTİK AYARLARI
        item {
            HapticSection(
                settings = state.settings,
                onToggleHaptic = { viewModel.toggleHaptic() },
                onSetHapticTapMode = { viewModel.setHapticTapMode(it) },
                onSetHapticMilestoneMode = { viewModel.setHapticMilestoneMode(it) },
                onIncrementUsage = { viewModel.incrementSettingUsage(it) }
            )
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // 5. UYGULAMA HAKKINDA VE SÜRÜM
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
            title = "Yedek Şifreleme",
            message = "Yedeğinizi AES-256 ile korumak için bir parola belirleyin:",
            actionText = "Şifrele ve Paylaş",
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
            title = "Yedek Parolası",
            message = "Şifrelenmiş yedeği açmak için parolayı girin (eski şifresiz yedekler için boş bırakabilirsiniz):",
            actionText = "Yedeği Aç",
            onDismissRequest = { viewModel.dismissImportPasswordDialog() },
            onConfirm = { password ->
                viewModel.importStatisticsBackup(
                    context = context,
                    password = password,
                    onSuccess = { restoredCount ->
                        Toast.makeText(context, "Yedek başarıyla geri yüklendi ($restoredCount zikir)", Toast.LENGTH_LONG).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (showNotificationRationaleDialog) {
        val titleText = when {
            isPermanentlyDenied -> when (state.settings.lang) {
                "ar" -> "تم رفض الإذن نهائياً"
                "de" -> "Berechtigung dauerhaft verweigert"
                "fr" -> "Permission refusée définitivement"
                "en" -> "Permission Permanently Denied"
                else -> "İzin Kalıcı Olarak Reddedildi"
            }
            else -> when (state.settings.lang) {
                "ar" -> "إذن التنبيهات مطلوب"
                "de" -> "Benachrichtigungsberechtigung"
                "fr" -> "Autorisation de notification"
                "en" -> "Notification Permission"
                else -> "Bildirim İzni Gerekli"
            }
        }
        val descText = when {
            isPermanentlyDenied -> when (state.settings.lang) {
                "ar" -> "لقد رفضت إذن التنبيهات نهائياً. يرجى تفعيله يدوياً من إعدادات التطبيق لتلقي تذكيرات الورد اليومي."
                "de" -> "Sie haben die Benachrichtigungsberechtigung dauerhaft verweigert. Bitte aktivieren Sie sie manuell in den App-Einstellungen, um tägliche Zikr-Erinnerungen zu erhalten."
                "fr" -> "Vous avez refusé définitivement l'autorisation de notification. Veuillez l'activer manuellement dans les paramètres de l'application pour recevoir les rappels quotidiens."
                "en" -> "You have permanently denied notification permission. Please enable it manually in app settings to receive daily dhikr reminders."
                else -> "Bildirim iznini kalıcı olarak reddettiniz. Günlük vird hatırlatıcılarını alabilmek için lütfen uygulama ayarlarından manuel olarak açın."
            }
            else -> when (state.settings.lang) {
                "ar" -> "نحتاج إلى إذن التنبيهات لنتمكن من إرسال التذكيرات اليومية للورد في أوقاتها المحددة. لن نرسل أي إشعارات مزعجة - فقط تذكيراتك المجدولة."
                "de" -> "Wir benötigen die Benachrichtigungsberechtigung, um Sie pünktlich an Ihren täglichen Zikr zu erinnern. Keine Spam-Benachrichtigungen - nur Ihre geplanten Erinnerungen."
                "fr" -> "Nous avons besoin de l'autorisation de notification pour vous envoyer des rappels quotidiens de dhikr. Pas de spam - seulement vos rappels programmés."
                "en" -> "We need notification permission to send you scheduled daily dhikr reminders. No spam - only your scheduled reminders."
                else -> "Hatırlatıcıları alabilmek için bildirim iznine ihtiyacımız var. Spam yok - sadece sizin planladığınız vakitlerde hatırlatma gönderiyoruz."
            }
        }
        val confirmBtnText = when {
            isPermanentlyDenied -> when (state.settings.lang) {
                "ar" -> "فتح الإعدادات"
                "de" -> "Einstellungen öffnen"
                "fr" -> "Ouvrir les paramètres"
                "en" -> "Open Settings"
                else -> "Ayarları Aç"
            }
            else -> when (state.settings.lang) {
                "ar" -> "متابعة وإذن"
                "de" -> "Erlauben"
                "fr" -> "Autoriser"
                "en" -> "Allow"
                else -> "İzin Ver"
            }
        }
        val cancelBtnText = when (state.settings.lang) {
            "ar" -> "إلغاء"
            "de" -> "Abbrechen"
            "fr" -> "Annuler"
            "en" -> "Cancel"
            else -> "Vazgeç"
        }

        AlertDialog(
            onDismissRequest = { 
                showNotificationRationaleDialog = false
                isPermanentlyDenied = false
            },
            icon = {
                Icon(
                    imageVector = if (isPermanentlyDenied) Icons.Rounded.Settings else Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.text
                )
            },
            text = {
                Text(
                    text = descText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationRationaleDialog = false
                        if (isPermanentlyDenied) {
                            openAppNotificationSettings()
                            isPermanentlyDenied = false
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.bg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(confirmBtnText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showNotificationRationaleDialog = false
                        isPermanentlyDenied = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.textMuted)
                ) {
                    Text(cancelBtnText)
                }
            },
            containerColor = colors.card,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
