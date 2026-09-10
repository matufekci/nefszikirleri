package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors
import com.google.firebase.auth.FirebaseUser

/**
 * 3. ADIM: Tamamlama ve Bulut Yedekleme Sayfası (CompletionPage)
 * Google ile güvenli oturum açma, bulut senkronizasyonu ve hazır olma durumu.
 */
@Composable
fun CompletionPage(
    currentUser: FirebaseUser?,
    isSyncing: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    lang: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.card,
            border = BorderStroke(
                1.5.dp,
                if (currentUser != null) colors.gold.copy(alpha = 0.6f) else colors.border
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (colors.isDark) 0.dp else 4.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = if (currentUser != null) colors.gold.copy(alpha = 0.35f) else Color(0x10000000)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rozet / İkon Halesi
                Surface(
                    shape = CircleShape,
                    color = if (currentUser != null) colors.gold.copy(alpha = 0.18f) else colors.primary.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.5.dp,
                        if (currentUser != null) colors.gold.copy(alpha = 0.6f) else colors.primary.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.Rounded.CloudDone else Icons.Rounded.CloudSync,
                            contentDescription = strings.cloudSyncTitle,
                            tint = if (currentUser != null) colors.gold else colors.primary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Başlık
                Text(
                    text = if (currentUser != null) {
                        currentUser.displayName ?: when (lang) {
                            "ar" -> "تم تسجيل الدخول"
                            "de" -> "Angemeldet"
                            "fr" -> "Connecté"
                            "en" -> "Signed In"
                            else -> "Giriş Yapıldı"
                        }
                    } else {
                        when (lang) {
                            "ar" -> "تسجيل الدخول والمزامنة"
                            "de" -> "Anmeldung & Cloud"
                            "fr" -> "Connexion & Cloud"
                            "en" -> "Sign In & Cloud Sync"
                            else -> "Oturum Açma & Bulut"
                        }
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.text,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Alt Başlık / E-posta
                Text(
                    text = if (currentUser != null) {
                        currentUser.email ?: when (lang) {
                            "ar" -> "حساب Google متصل بنجاح"
                            "de" -> "Google-Konto verknüpft"
                            "fr" -> "Compte Google associé"
                            "en" -> "Google account linked"
                            else -> "Google hesabı bağlandı"
                        }
                    } else {
                        when (lang) {
                            "ar" -> "احفظ أورادك بأمان في السحابة"
                            "de" -> "Sichern Sie Ihre Daten in der Cloud"
                            "fr" -> "Sauvegardez vos données dans le cloud"
                            "en" -> "Keep your dhikrs safe in the cloud"
                            else -> "Zikirlerinizi bulutta güvenle yedekleyin"
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(18.dp))

                // Kullanıcı Durumuna Göre İşlemler
                if (currentUser == null) {
                    Text(
                        text = when (lang) {
                            "ar" -> "سجل دخولك لتستعيد أورادك وإحصائياتك بنقرة واحدة عند تغيير هاتفك."
                            "de" -> "Melden Sie sich an, um Ihre Zikr-Daten bei Gerätewechsel mit einem Klick wiederherzustellen."
                            "fr" -> "Connectez-vous pour restaurer vos dhikrs en un clic en cas de changement d'appareil."
                            "en" -> "Sign in to easily restore your dhikrs and stats anytime you switch devices."
                            else -> "Cihaz değiştirseniz bile zikirleriniz ve hatimleriniz kaybolmaz, tek tıkla geri yüklenir."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onSignIn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.bg
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .testTag("btn_intro_google_sign_in"),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = colors.bg
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = when (lang) {
                                "ar" -> "تسجيل الدخول بواسطة Google"
                                "de" -> "Mit Google anmelden"
                                "fr" -> "Se connecter avec Google"
                                "en" -> "Sign in with Google"
                                else -> "Google ile Oturum Aç"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.primary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (lang) {
                                    "ar" -> "تم تفعيل النسخ الاحتياطي السحابي"
                                    "de" -> "Cloud-Backup ist aktiv"
                                    "fr" -> "Sauvegarde cloud activée"
                                    "en" -> "Cloud sync is enabled"
                                    else -> "Bulut senkronizasyonu hazır ve aktif"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("btn_intro_sign_out"),
                        enabled = !isSyncing
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = strings.signOutDesc,
                            tint = colors.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (lang) {
                                "ar" -> "تسجيل الخروج"
                                "de" -> "Abmelden"
                                "fr" -> "Se déconnecter"
                                "en" -> "Sign Out"
                                else -> "Oturumu Kapat"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
