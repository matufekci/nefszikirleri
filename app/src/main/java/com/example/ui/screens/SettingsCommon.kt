package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppStrings
import com.example.data.model.UiTranslations
import com.example.ui.theme.LocalAppColors

/**
 * Ayarlar ekranında ortak kullanılan kartlar ve başlık formatlayıcılar
 */
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

fun getSettingsSummary(key: String, lang: String): String {
    val l = lang.lowercase()
    return when (key) {
        "theme" -> when (l) {
            "ar" -> "تخصيص الألوان والمظهر العام للتطبيق."
            "de" -> "Passen Sie die Farben und das allgemeine Erscheinungsbild der App an."
            "fr" -> "Personnalisez les couleurs et l'apparence générale de l'application."
            "en" -> "Customize the overall color and appearance of the application."
            else -> "Uygulamanın genel renk ve görünümünü kişiselleştirin."
        }
        "lang" -> when (l) {
            "ar" -> "تغيير لغة التطبيق وفقاً لتفضيلاتك."
            "de" -> "Ändern Sie die Sprache der Anwendung nach Ihren Wünschen."
            "fr" -> "Changez la langue de l'application selon vos préférences."
            "en" -> "Change the application language to your preference."
            else -> "Uygulama dilini kendi tercihinize göre değiştirin."
        }
        "counter_and_reminders" -> when (l) {
            "ar" -> "إدارة نمط العداد والشاشة والتنبيهات اليومية المنظمة."
            "de" -> "Verwalten Sie Zählermodus, Bildschirmverhalten und geplante Erinnerungen."
            "fr" -> "Gérez le mode compteur, l'écran et les rappels programmés."
            "en" -> "Manage counter mode, screen behavior, and scheduled reminders."
            else -> "Sayım yönü, ekran davranışı ve planlı hatırlatıcıları tek yerden yönetin."
        }
        "counter" -> when (l) {
            "ar" -> "إدارة تفضيلات الورد اليومي واتجاه العداد والأهداف الشخصية."
            "de" -> "Verwalten Sie tägliche Ziele und Zählrichtungen für Ihr persönliches Vird."
            "fr" -> "Gérez les préférences de dhikr comme les objectifs quotidiens et le sens du compteur."
            "en" -> "Manage personal dhikr preferences like daily targets and counter direction."
            else -> "Günlük hedef ve sayaç yönü gibi kişisel vird tercihlerinizi yönetin."
        }
        "reminders" -> when (l) {
            "ar" -> "تعيين تنبيهات وتذكيرات يومية للمحافظة على ورد الذكر."
            "de" -> "Stellen Sie tägliche Erinnerungen ein, um Ihre Zikr-Gewohnheit zu pflegen."
            "fr" -> "Définissez des rappels quotidiens pour maintenir votre habitude de dhikr."
            "en" -> "Set daily reminders to maintain your dhikr habit."
            else -> "Zikir alışkanlığınızı korumak için günlük hatırlatıcılar kurun."
        }
        "font" -> when (l) {
            "ar" -> "ضبط حجم ونقاء النصوص داخل التطبيق لتسهيل القراءة."
            "de" -> "Passen Sie die Lesbarkeit und Textgröße in der App an."
            "fr" -> "Ajustez la lisibilité et la taille du texte dans l'application."
            "en" -> "Adjust the readability and size of texts in the app."
            else -> "Uygulama içindeki metinlerin okunaklılığını ve büyüklüğünü ayarlayın."
        }
        "haptic" -> when (l) {
            "ar" -> "التحكم في الاهتزاز اللمسي والتغذية الراجعة أثناء التسبيح."
            "de" -> "Verwalten Sie das haptische Vibrationsfeedback beim Zikr."
            "fr" -> "Gérez les retours de vibration haptique lors de la récitation du dhikr."
            "en" -> "Manage the haptic vibration feedback you feel while reciting dhikr."
            else -> "Zikir çekerken hissedeceğiniz dokunsal titreşim geri bildirimlerini yönetin."
        }
        "sound" -> when (l) {
            "ar" -> "التحكم في المؤثرات الصوتية وردود الفعل الصوتية للتطبيق."
            "de" -> "Verwalten Sie Soundeffekte und Audio-Feedback der Anwendung."
            "fr" -> "Gérez les effets sonores et les retours audio de l'application."
            "en" -> "Manage sound effects and audio feedback of the application."
            else -> "Uygulamanın ses efektleri ve işitsel geri bildirimlerini yönetin."
        }
        "rounds" -> when (l) {
            "ar" -> "عرض الجولات المكتملة وبدء جولة ختم جديدة عند الرغبة."
            "de" -> "Sehen Sie abgeschlossene Runden ein und starten Sie bei Bedarf eine neue Runde."
            "fr" -> "Consultez les tours terminés et commencez un nouveau tour si vous le souhaitez."
            "en" -> "View completed rounds and start a new round if desired."
            else -> "Tamamlanan turlarınızı görün ve dilerseniz yeni bir tura başlayın."
        }
        "backup_and_sync" -> when (l) {
            "ar" -> "النسخ الاحتياطي السحابي عبر Google وتصدير/استيراد الملفات."
            "de" -> "Google Cloud-Sicherung und lokaler Datei-Export/Import."
            "fr" -> "Sauvegarde cloud Google et export/import de fichiers locaux."
            "en" -> "Google Cloud backup and local file export/import."
            else -> "Google bulut senkronizasyonu ve yerel dosya yedeği (dışa/içe aktarma)."
        }
        else -> ""
    }
}

@Composable
fun SettingsCollapsibleCard(
    title: String,
    summary: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    strings: UiTranslations = AppStrings.get("tr"),
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border),
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
                    .heightIn(min = 48.dp)
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
fun LanguageChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) colors.primary else colors.inputBg,
        border = BorderStroke(
            1.dp,
            if (isSelected) colors.primary else colors.border
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
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
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
