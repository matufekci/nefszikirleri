package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.actionButtonShadow

/**
 * Zikir Sayacı Alt Kontrol Butonları (Geri Al, Manuel Ekle/Çıkar, Sıfırla)
 */
@Composable
fun CounterBottomBar(
    canUndo: Boolean,
    lang: String,
    onUndo: () -> Unit,
    onOpenManual: () -> Unit,
    onOpenReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    val fontScaleFactor = LocalDensity.current.fontScale
    val actionMinHeight = if (fontScaleFactor > 1.25f) 46.dp else 42.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Geri Al Butonu
        OutlinedButton(
            onClick = onUndo,
            enabled = canUndo,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.card,
                contentColor = colors.primary
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (canUndo) colors.primary.copy(alpha = 0.85f) else colors.border),
            modifier = Modifier
                .weight(1.2f)
                .heightIn(min = actionMinHeight)
                .actionButtonShadow(
                    colors = colors,
                    shape = RoundedCornerShape(12.dp),
                    overrideSpotColor = if (canUndo) colors.primary.copy(alpha = 0.35f) else null
                )
                .testTag("btn_undo")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = strings.undo, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = strings.undo,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (fontScaleFactor > 1.25f) 11.sp else MaterialTheme.typography.labelMedium.fontSize
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 2. Manuel Sayı Ekleme Açıcı
        OutlinedButton(
            onClick = onOpenManual,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.card,
                contentColor = colors.text
            ),
            border = BorderStroke(1.dp, colors.border),
            modifier = Modifier
                .weight(1.2f)
                .heightIn(min = actionMinHeight)
                .actionButtonShadow(
                    colors = colors,
                    shape = RoundedCornerShape(12.dp)
                )
                .testTag("btn_open_manual_ops")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = strings.manualTitle, modifier = Modifier.size(14.dp), tint = colors.textMuted)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = strings.manualTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (fontScaleFactor > 1.25f) 11.sp else MaterialTheme.typography.labelMedium.fontSize
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 3. Zikri Sıfırla Butonu
        OutlinedButton(
            onClick = onOpenReset,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.card,
                contentColor = colors.error
            ),
            border = BorderStroke(1.dp, colors.error.copy(alpha = 0.45f)),
            modifier = Modifier
                .weight(1.2f)
                .heightIn(min = actionMinHeight)
                .actionButtonShadow(
                    colors = colors,
                    shape = RoundedCornerShape(12.dp),
                    overrideSpotColor = colors.error.copy(alpha = 0.35f)
                )
                .testTag("btn_open_reset_dialog")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = strings.resetThis,
                    modifier = Modifier.size(15.dp),
                    tint = colors.error
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = strings.resetThis,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (fontScaleFactor > 1.25f) 11.sp else MaterialTheme.typography.labelMedium.fontSize
                    ),
                    color = colors.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
