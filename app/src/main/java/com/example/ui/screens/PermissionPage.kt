package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors

/**
 * İzin ve Hatırlatıcı Açıklama Sayfası (PermissionPage)
 * Manevi vird bildirimleri ve dokunmatik geri bildirim (haptic) rehberi.
 */
@Composable
fun PermissionPage(
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
            border = BorderStroke(1.dp, colors.border),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (colors.isDark) 0.dp else 4.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = Color(0x10000000)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = colors.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.5.dp, colors.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = strings.remindersTitle,
                            tint = colors.primary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (lang) {
                        "ar" -> "التذكير الروحي والاهتزاز"
                        "de" -> "Spirituelle Erinnerungen & Haptik"
                        "fr" -> "Rappels Spirituels & Haptique"
                        "en" -> "Spiritual Reminders & Haptics"
                        else -> "Manevi Hatırlatıcı & Hissiyat"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.text,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (lang) {
                        "ar" -> "للحفاظ على وردك اليومي وثباتك على مراتب النفس، يوصى بالسماح بالإشعارات اللطيفة."
                        "de" -> "Damit Sie Ihren täglichen Dhikr nicht vergessen, empfehlen wir sanfte Erinnerungen."
                        "fr" -> "Pour maintenir votre wird quotidien, nous recommandons d'activer les rappels."
                        "en" -> "To maintain your daily spiritual consistency, gentle reminders keep your heart aligned."
                        else -> "Günlük virdinizi aksatmamak ve istikrarınızı korumak için nazik manevi bildirimler önerilir."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Özellik 1: Bildirimler
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.inputBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = colors.gold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (lang) {
                                    "ar" -> "تنبيهات الأوراد"
                                    "de" -> "Dhikr-Benachrichtigung"
                                    "fr" -> "Alertes de Wird"
                                    "en" -> "Dhikr Reminders"
                                    else -> "Vird Hatırlatması"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.text
                            )
                            Text(
                                text = when (lang) {
                                    "ar" -> "تذكير في الصباح والمساء"
                                    "de" -> "Morgens und abends"
                                    "fr" -> "Matin et soir"
                                    "en" -> "Morning & Evening prompts"
                                    else -> "Sabah ve akşam manevi hatırlatmalar"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Özellik 2: Haptic Titreşim
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.inputBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Vibration,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (lang) {
                                    "ar" -> "اهتزاز التسبيح الواقعي"
                                    "de" -> "Realistische Haptik"
                                    "fr" -> "Haptique Réaliste"
                                    "en" -> "Realistic Haptic Touch"
                                    else -> "Gerçekçi Tesbih Titreşimi"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.text
                            )
                            Text(
                                text = when (lang) {
                                    "ar" -> "إحساس التسبيح دون النظر للشاشة"
                                    "de" -> "Dhikr ohne auf den Bildschirm zu schauen"
                                    "fr" -> "Zikr sans regarder l'écran"
                                    "en" -> "Dhikr count feedback without looking"
                                    else -> "Ekrana bakmadan sayımı hissetme kolaylığı"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
