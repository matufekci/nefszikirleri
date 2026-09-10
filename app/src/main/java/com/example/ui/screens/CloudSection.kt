package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Google Bulut Senkronizasyonu ve Hesap Yönetimi Bölümü
 */
@Composable
fun CloudSection(
    currentUser: FirebaseUser?,
    isCloudSyncing: Boolean,
    lastSyncTimestamp: Long?,
    lang: String,
    onSignInWithGoogle: (Context) -> Unit,
    onSignOut: () -> Unit,
    onBackupToCloud: () -> Unit,
    onRestoreFromCloud: () -> Unit,
    onRequestExportBackup: () -> Unit,
    onLaunchImportFile: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)
    var accountExpanded by rememberSaveable { mutableStateOf(false) }

    val headerTitle = if (currentUser != null) {
        currentUser.displayName ?: "Google Hesabı"
    } else {
        when (lang.lowercase()) {
            "ar" -> "النسخ الاحتياطي والمزامنة"
            "de" -> "Sicherung & Synchronisation"
            "fr" -> "Sauvegarde & Synchronisation"
            "en" -> "Backup & Sync"
            else -> "Yedekleme ve Senkronizasyon"
        }
    }
    val headerSubtitle = if (currentUser != null) {
        currentUser.email ?: "Bağlandı"
    } else {
        getSettingsSummary("backup_and_sync", lang)
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = colors.card,
        border = BorderStroke(
            1.2.dp,
            if (currentUser != null) colors.gold.copy(alpha = 0.6f) else colors.border
        ),
        modifier = modifier
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
                        text = when (lang.lowercase()) {
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
                        text = when (lang.lowercase()) {
                            "ar" -> "سجل الدخول مع Google لمزامنة بياناتك واستعادتها بسهولة."
                            "de" -> "Mit Google anmelden, um Daten zu synchronisieren und wiederherzustellen."
                            "fr" -> "Connectez-vous avec Google pour synchroniser et restaurer vos données."
                            "en" -> "Sign in with Google to sync and restore your data on any device."
                            else -> "Google ile giriş yaparak verilerinizi bulutta güvenle senkronize edebilir ve dilediğiniz an geri yükleyebilirsiniz."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onSignInWithGoogle(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.bg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
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
                    val lastSyncText = if (lastSyncTimestamp != null && lastSyncTimestamp > 0L) {
                        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        "Son Eşitleme: ${sdf.format(Date(lastSyncTimestamp))}"
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
                            onClick = onSignOut,
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
                            onClick = onBackupToCloud,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.bg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
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
                            onClick = onRestoreFromCloud,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.text
                            ),
                            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
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

                // 2. KISIM: ÇEVRİMDIŞI / DOSYA YEDEĞİ
                BackupSection(
                    lang = lang,
                    onRequestExportBackup = onRequestExportBackup,
                    onLaunchImportFile = onLaunchImportFile
                )
            }
        }
    }
}
