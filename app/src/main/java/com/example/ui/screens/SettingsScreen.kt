package com.example.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.text.*
import androidx.compose.ui.text.input.*
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*
import com.example.util.*
import android.content.*
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    
    val currentUser by viewModel.currentUser.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()
    val cloudSyncMessage by viewModel.cloudSyncMessage.collectAsState()
    val lastSyncTimestamp by viewModel.lastCloudSyncTimestamp.collectAsState()

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
    var accountExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    var counterAndRemindersExpanded by remember { mutableStateOf(false) }
    var roundsExpanded by remember { mutableStateOf(false) }
    var fontScaleExpanded by remember { mutableStateOf(false) }

    
    val currentLangLabel = when (state.settings.lang.lowercase()) {
        "ar" -> "العربية 🇸🇦"
        "en" -> "English 🇬🇧"
        "de" -> "Deutsch 🇩🇪"
        "fr" -> "Français 🇫🇷"
        else -> "Türkçe 🇹🇷"
    }
    val currentThemeLabel = com.example.ui.theme.AppPalettes.get(state.settings.themeName).name

    // Helper for capitalizing words (küçük harfle kalması gereken bağlaçlar gözetilir)
    fun String.toTitleCase(): String {
        val lowerConjunctions = setOf("ve", "ile", "veya", "de", "da", "ki", "and", "or", "with", "und", "oder", "et", "ou")
        val words = this.split(" ")
        return words.mapIndexed { index, word ->
            val cleanLower = word.lowercase()
            if (index > 0 && cleanLower in lowerConjunctions) {
                cleanLower
            } else {
                cleanLower.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }.joinToString(" ")
    }

    // Helper for localized summaries in TR, AR, EN, DE, FR
    fun getSummary(key: String): String {
        val lang = state.settings.lang.lowercase()
        return when (key) {
            "theme" -> when (lang) {
                "ar" -> "تخصيص الألوان والمظهر العام للتطبيق."
                "de" -> "Passen Sie die Farben und das allgemeine Erscheinungsbild der App an."
                "fr" -> "Personnalisez les couleurs et l'apparence générale de l'application."
                "en" -> "Customize the overall color and appearance of the application."
                else -> "Uygulamanın genel renk ve görünümünü kişiselleştirin."
            }
            "lang" -> when (lang) {
                "ar" -> "تغيير لغة التطبيق وفقاً لتفضيلاتك."
                "de" -> "Ändern Sie die Sprache der Anwendung nach Ihren Wünschen."
                "fr" -> "Changez la langue de l'application selon vos préférences."
                "en" -> "Change the application language to your preference."
                else -> "Uygulama dilini kendi tercihinize göre değiştirin."
            }
            "counter_and_reminders" -> when (lang) {
                "ar" -> "إدارة نمط العداد والشاشة والتنبيهات اليومية المنظمة."
                "de" -> "Verwalten Sie Zählermodus, Bildschirmverhalten und geplante Erinnerungen."
                "fr" -> "Gérez le mode compteur, l'écran et les rappels programmés."
                "en" -> "Manage counter mode, screen behavior, and scheduled reminders."
                else -> "Sayım yönü, ekran davranışı ve planlı hatırlatıcıları tek yerden yönetin."
            }
            "counter" -> when (lang) {
                "ar" -> "إدارة تفضيلات الورد اليومي واتجاه العداد والأهداف الشخصية."
                "de" -> "Verwalten Sie tägliche Ziele und Zählrichtungen für Ihr persönliches Vird."
                "fr" -> "Gérez les préférences de dhikr comme les objectifs quotidiens et le sens du compteur."
                "en" -> "Manage personal dhikr preferences like daily targets and counter direction."
                else -> "Günlük hedef ve sayaç yönü gibi kişisel vird tercihlerinizi yönetin."
            }
            "reminders" -> when (lang) {
                "ar" -> "تعيين تنبيهات وتذكيرات يومية للمحافظة على ورد الذكر."
                "de" -> "Stellen Sie tägliche Erinnerungen ein, um Ihre Zikr-Gewohnheit zu pflegen."
                "fr" -> "Définissez des rappels quotidiens pour maintenir votre habitude de dhikr."
                "en" -> "Set daily reminders to maintain your dhikr habit."
                else -> "Zikir alışkanlığınızı korumak için günlük hatırlatıcılar kurun."
            }
            "font" -> when (lang) {
                "ar" -> "ضبط حجم ونقاء النصوص داخل التطبيق لتسهيل القراءة."
                "de" -> "Passen Sie die Lesbarkeit und Textgröße in der App an."
                "fr" -> "Ajustez la lisibilité et la taille du texte dans l'application."
                "en" -> "Adjust the readability and size of texts in the app."
                else -> "Uygulama içindeki metinlerin okunaklılığını ve büyüklüğünü ayarlayın."
            }
            "haptic" -> when (lang) {
                "ar" -> "التحكم في الاهتزاز اللمسي والتغذية الراجعة أثناء التسبيح."
                "de" -> "Verwalten Sie das haptische Vibrationsfeedback beim Zikr."
                "fr" -> "Gérez les retours de vibration haptique lors de la récitation du dhikr."
                "en" -> "Manage the haptic vibration feedback you feel while reciting dhikr."
                else -> "Zikir çekerken hissedeceğiniz dokunsal titreşim geri bildirimlerini yönetin."
            }
            "rounds" -> when (lang) {
                "ar" -> "عرض الجولات المكتملة وبدء جولة ختم جديدة عند الرغبة."
                "de" -> "Sehen Sie abgeschlossene Runden ein und starten Sie bei Bedarf eine neue Runde."
                "fr" -> "Consultez les tours terminés et commencez un nouveau tour si vous le souhaitez."
                "en" -> "View completed rounds and start a new round if desired."
                else -> "Tamamlanan turlarınızı görün ve dilerseniz yeni bir tura başlayın."
            }
            "backup_and_sync" -> when (lang) {
                "ar" -> "النسخ الاحتياطي السحابي عبر Google وتصدير/استيراد الملفات."
                "de" -> "Google Cloud-Sicherung und lokaler Datei-Export/Import."
                "fr" -> "Sauvegarde cloud Google et export/import de fichiers locaux."
                "en" -> "Google Cloud backup and local file export/import."
                else -> "Google bulut senkronizasyonu ve yerel dosya yedeği (dışa/içe aktarma)."
            }
            else -> ""
        }
    }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { // YEDEKLEME VE SENKRONİZASYON KARTI (Google Bulut & Yerel Dosya Yedeği)
            val headerTitle = if (currentUser != null) {
                currentUser?.displayName ?: "Google Hesabı"
            } else {
                when (state.settings.lang.lowercase()) {
                    "ar" -> "النسخ الاحتياطي والمزامنة"
                    "de" -> "Sicherung & Synchronisation"
                    "fr" -> "Sauvegarde & Synchronisation"
                    "en" -> "Backup & Sync"
                    else -> "Yedekleme ve Senkronizasyon"
                }
            }
            val headerSubtitle = if (currentUser != null) {
                currentUser?.email ?: "Bağlandı"
            } else {
                getSummary("backup_and_sync")
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.card,
                border = BorderStroke(
                    1.2.dp,
                    if (currentUser != null) colors.gold.copy(alpha = 0.6f) else colors.border
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .shadow(
                        elevation = if (colors.isDark) 0.dp else 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = if (currentUser != null) colors.gold.copy(alpha = 0.35f) else Color(0x10000000)
                    )
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accountExpanded = !accountExpanded }
                            .testTag("account_collapsible_header"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (currentUser != null) colors.gold.copy(alpha = 0.18f) else colors.inputBg,
                                border = BorderStroke(1.dp, if (currentUser != null) colors.gold.copy(alpha = 0.5f) else colors.border),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (currentUser != null) Icons.Rounded.CloudDone else Icons.Rounded.CloudSync,
                                        contentDescription = strings.cloudSyncTitle,
                                        tint = if (currentUser != null) colors.gold else colors.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = headerTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = headerSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCloudSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = colors.gold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(
                                imageVector = if (accountExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                contentDescription = if (accountExpanded) strings.collapse else strings.expand,
                                tint = colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (accountExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = colors.border.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. KISIM: GOOGLE BULUT SENKRONİZASYONU
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudSync,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (state.settings.lang.lowercase()) {
                                    "ar" -> "المزامنة السحابية (Google)"
                                    "de" -> "Google Cloud-Synchronisation"
                                    "fr" -> "Synchronisation Cloud Google"
                                    "en" -> "Google Cloud Sync"
                                    else -> "Google Bulut Senkronizasyonu"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.text
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        if (currentUser == null) {
                            Text(
                                text = when (state.settings.lang.lowercase()) {
                                    "ar" -> "سجل الدخول باستخدام Google لمزامنة أذكارك وإحصائياتك وجولاتك تلقائياً عبر السحابة واستعادتها على أي جهاز."
                                    "de" -> "Melden Sie sich mit Google an, um Ihre Dhikrs, Statistiken und Runden in der Cloud zu sichern und geräteübergreifend wiederherzustellen."
                                    "fr" -> "Connectez-vous avec Google pour sauvegarder automatiquement vos dhikrs, statistiques et tours dans le cloud et les restaurer partout."
                                    "en" -> "Sign in with Google to securely sync your dhikrs, statistics, and rounds to the cloud and restore them on any device."
                                    else -> "Google ile giriş yaparak zikirlerinizi, istatistiklerinizi ve tamamlanan turlarınızı buluta yedekleyebilir; cihazınızı değiştirseniz dahi tek dokunuşla geri yükleyebilirsiniz."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.signInWithGoogle(context) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = colors.bg
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 46.dp)
                                    .testTag("btn_google_sign_in"),
                                enabled = !isCloudSyncing
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Google ile Giriş Yap",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        } else {
                            val lastSyncText = if (lastSyncTimestamp != null && lastSyncTimestamp!! > 0L) {
                                val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                                "Son Eşitleme: ${sdf.format(Date(lastSyncTimestamp!!))}"
                            } else {
                                "Otomatik Eşitleme Aktif"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = lastSyncText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = colors.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                
                                TextButton(
                                    onClick = { viewModel.signOut() },
                                    modifier = Modifier.testTag("btn_google_sign_out")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                                        contentDescription = strings.signOutDesc,
                                        tint = colors.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.logoutText,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.error
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.backupToCloud() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primary,
                                        contentColor = colors.bg
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                        .testTag("btn_cloud_backup"),
                                    enabled = !isCloudSyncing
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CloudUpload,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Buluta Yedekle",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.restoreFromCloud() },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = colors.text
                                    ),
                                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 44.dp)
                                        .testTag("btn_cloud_restore"),
                                    enabled = !isCloudSyncing
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CloudDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = colors.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Geri Yükle",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = colors.border.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. KISIM: ÇEVRİMDIŞI / DOSYA YEDEĞİ (DIŞA VE İÇE AKTARMA)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SaveAlt,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.exportStatsBackupTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.text
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.exportStatsBackupDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.exportAndShareStatisticsBackup(context) { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.gold,
                                    contentColor = colors.bg
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("btn_export_share_stats_backup")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.exportStatsBackupBtn,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    importFileLauncher.launch("text/*")
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, colors.gold.copy(alpha = 0.8f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colors.text
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("btn_import_stats_backup")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FileUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp),
                                    tint = colors.gold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.importStatsBackupBtn,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(14.dp)) }

        item { // 1. TEMA SEÇİMİ (Collapsible)
            SettingsCollapsibleCard(
                title = strings.themeTitle.toTitleCase(),
                summary = getSummary("theme"),
                icon = Icons.Rounded.Palette,
                isExpanded = themeExpanded,
                onToggle = { themeExpanded = !themeExpanded },
                strings = strings
            ) {
                val chunks = AppPalettes.ALL.chunked(2)
                chunks.forEachIndexed { rowIndex, rowList ->
                    if (rowIndex > 0) Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowList.forEach { palette ->
                            val isSelected = state.settings.themeName == palette.id || 
                                (state.settings.themeName == "emerald" && palette.id == "hadra_gece") ||
                                (state.settings.themeName == "rahle" && palette.id == "hadra_gunduz") ||
                                (state.settings.themeName == "obsidian" && palette.id == "siyah")
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) palette.primary.copy(alpha = 0.18f) else colors.inputBg
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) palette.primary else colors.border,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { 
                                        viewModel.incrementSettingUsage("theme")
                                        viewModel.setTheme(palette.id)
                                        themeExpanded = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 10.dp)
                                    .testTag("theme_picker_${palette.id}")
                            ) {
                                // Theme Color Dual Swatch (Primary + Background)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(palette.bg)
                                        .border(1.5.dp, palette.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(palette.primary)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = palette.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        color = if (isSelected) palette.primary else colors.text,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (rowList.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(14.dp)) }

        item { // 2. DİL SEÇİMİ (Collapsible)
            SettingsCollapsibleCard(
                title = strings.language.toTitleCase(),
                summary = getSummary("lang"),
                icon = Icons.Rounded.Language,
                isExpanded = languageExpanded,
                onToggle = { languageExpanded = !languageExpanded },
                strings = strings
            ) {
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
                            onClick = { 
                                viewModel.incrementSettingUsage("language")
                                viewModel.setLanguage(code)
                                languageExpanded = false
                            },
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
                            onClick = { 
                                viewModel.incrementSettingUsage("language")
                                viewModel.setLanguage(code)
                                languageExpanded = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(14.dp)) }

        item { // 3. SAYICI, VİRD VE HATIRLATICILAR (Birleştirilmiş & Kullanım Amacına Göre Sıralı Collapsible Kart)
            SettingsCollapsibleCard(
                title = strings.counterPrefsTitle.toTitleCase(),
                summary = getSummary("counter_and_reminders"),
                icon = Icons.Rounded.Tune,
                isExpanded = counterAndRemindersExpanded,
                onToggle = { counterAndRemindersExpanded = !counterAndRemindersExpanded },
                strings = strings
            ) {
                // 1. KULLANIM AMACI: SAYIM VE EKRAN DAVRANIŞI
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
                HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                // Ekranı Açık Tut (Zikir esnasında ekran kapanmasın)
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

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // 2. KULLANIM AMACI: PLANLI HATIRLATICILAR VE BİLDİRİMLER
                // Planlı Günlük Hatırlatıcılar (Zaman Dilimleri)
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

                if (state.settings.reminderEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    state.reminderSlots.forEach { slot ->
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
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(onClick = { viewModel.updateReminderSlot(slot, 1, 0) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp), tint = colors.primary)
                                    }
                                    Text(
                                        text = String.format("%02d", slot.hour),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = colors.text
                                    )
                                    IconButton(onClick = { viewModel.updateReminderSlot(slot, -1, 0) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Rounded.Remove, null, modifier = Modifier.size(16.dp), tint = colors.primary)
                                    }
                                }
                                Text(":", fontWeight = FontWeight.Black, color = colors.text, modifier = Modifier.padding(horizontal = 4.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(onClick = { viewModel.updateReminderSlot(slot, 0, 15) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp), tint = colors.primary)
                                    }
                                    Text(
                                        text = String.format("%02d", slot.minute),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = colors.text
                                    )
                                    IconButton(onClick = { viewModel.updateReminderSlot(slot, 0, -15) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Rounded.Remove, null, modifier = Modifier.size(16.dp), tint = colors.primary)
                                    }
                                }
                            }

                            IconButton(onClick = { viewModel.removeReminderSlot(slot.id) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = strings.deleteBtn, tint = colors.error, modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    if (state.reminderSlots.size < 5) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.addReminderSlot(8, 0)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.addReminderBtn)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(14.dp)) }

        item { // 6. YAZI BOYUTU (Collapsible)
            SettingsCollapsibleCard(
                title = strings.fontScaleTitle.toTitleCase(),
                summary = getSummary("font"),
                icon = Icons.Rounded.FormatSize,
                isExpanded = fontScaleExpanded,
                onToggle = { fontScaleExpanded = !fontScaleExpanded },
                strings = strings
            ) {
                    Text(
                        text = strings.fontScaleDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val scales = listOf(
                        Pair(1.0f, strings.fontScaleSmall),
                        Pair(1.15f, strings.fontScaleNormal),
                        Pair(1.30f, strings.fontScaleLarge),
                        Pair(1.45f, strings.fontScaleHuge)
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
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) colors.bg else colors.text,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
            }
        }
        item { Spacer(modifier = Modifier.height(14.dp)) }

        item { // 7. TAMAMLANAN TUR (Collapsible)
                SettingsCollapsibleCard(
                    title = strings.roundsTitle.toTitleCase(),
                    summary = getSummary("rounds"),
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
                            .heightIn(min = 48.dp)
                            .testTag("btn_trigger_round_modal")
                    ) {
                        Text(strings.startNewRoundBtn, fontWeight = FontWeight.Black)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(14.dp)) }

            // 10. UYGULAMA BİLGİSİ & SÜRÜM v2.0 LÜKS KART
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.card,
                        border = BorderStroke(1.2.dp, colors.gold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(
                                elevation = if (colors.isDark) 0.dp else 3.dp,
                                shape = CircleShape,
                                spotColor = colors.gold.copy(alpha = 0.35f)
                            )
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_nefs_app_icon_1787782143783),
                            contentDescription = strings.appLogo,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nefs Zikirleri",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = colors.text
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.gold.copy(alpha = 0.16f),
                        border = BorderStroke(0.8.dp, colors.gold.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "v2.0 • Ultra Edition",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.gold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (state.settings.lang.lowercase()) {
                            "ar" -> "تطبيق أذكار وتزكية النفس المبارك"
                            "de" -> "Spirituelle Nafs-Zikr & Vird Begleiter"
                            "fr" -> "Compagnon spirituel des dhikrs de l'âme"
                            "en" -> "Spiritual Stations & Nafs Dhikr Companion"
                            else -> "Nefs Zikirleri Rehberi"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.heightIn(min = 40.dp))
            }
        }

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
                        viewModel.importStatisticsBackup(
                            context = context,
                            uri = uri,
                            onSuccess = {
                                Toast.makeText(context, strings.importStatsBackupSuccess, Toast.LENGTH_LONG).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, "${strings.importStatsBackupError}: $err", Toast.LENGTH_LONG).show()
                            }
                        )
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
}

@Composable
private fun SettingsCollapsibleCard(
    title: String,
    summary: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    strings: UiTranslations = AppStrings.get("tr"),
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = colors.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .shadow(
                elevation = if (colors.isDark) 0.dp else 4.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0x1F000000),
                ambientColor = Color(0x0F000000)
            )
            .clip(RoundedCornerShape(22.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.inputBg,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.text
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!isExpanded && summary.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isExpanded) strings.collapse else strings.expand,
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))
                content()
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = colors.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .shadow(
                elevation = if (colors.isDark) 0.dp else 4.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0x1F000000),
                ambientColor = Color(0x0F000000)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = colors.primary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LanguageChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) colors.primary else colors.inputBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) colors.primary else colors.border
        ),
        modifier = modifier
            .clickable { onClick() }
            .testTag("lang_chip_$label")
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (isSelected) colors.bg else colors.text,
            modifier = Modifier.padding(vertical = 10.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1
        )
    }
}
