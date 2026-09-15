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

@Composable
fun SettingsCollapsibleCard(
    title: String,
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
