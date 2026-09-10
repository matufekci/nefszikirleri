package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppStrings
import com.example.data.model.Zikir
import com.example.data.model.ZikirContent
import com.example.ui.components.SpiritualBeadsIcon
import com.example.ui.components.SpiritualCheckIcon
import com.example.ui.components.SpiritualLockIcon
import com.example.ui.theme.LocalAppColors
import com.example.util.NumberFormatter

/**
 * Zikir Listesi ve Hızlı Zikir Seçim Bölümü (ModalBottomSheet)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZikirListSection(
    zikirs: List<Zikir>,
    currentZikirId: Int,
    lang: String,
    isZikirUnlocked: (Int, List<Zikir>) -> Boolean,
    onSelectZikir: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.card,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Text(
                text = strings.listTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.text
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(zikirs) { z ->
                    val isUnlocked = isZikirUnlocked(z.id, zikirs)
                    val isSelected = z.id == currentZikirId
                    val name = ZikirContent.getZikirName(z.id, lang)
                    val arabic = ZikirContent.getArabicText(z.id)
                    val percent = if (z.target > 0) (z.count * 100 / z.target) else 0

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            isSelected -> colors.primary.copy(alpha = 0.15f)
                            isUnlocked -> colors.inputBg
                            else -> colors.inputBg.copy(alpha = 0.4f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) colors.primary else colors.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isUnlocked) {
                                onSelectZikir(z.id)
                                onDismissRequest()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (z.count >= z.target) {
                                    SpiritualCheckIcon(tint = colors.gold, size = 18.dp)
                                } else if (isUnlocked) {
                                    SpiritualBeadsIcon(tint = colors.primary, size = 18.dp)
                                } else {
                                    SpiritualLockIcon(tint = colors.textMuted, size = 16.dp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        color = if (isUnlocked) colors.text else colors.textMuted
                                    )
                                    Text(
                                        text = "${NumberFormatter.format(z.count, lang)} / ${NumberFormatter.format(z.target, lang)} (%$percent)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textMuted
                                    )
                                }
                            }

                            Text(
                                text = arabic,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) colors.primary else colors.textMuted
                                )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
