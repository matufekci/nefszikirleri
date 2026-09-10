package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppStrings
import com.example.ui.components.SpiritualFlameIcon
import com.example.ui.theme.LocalAppColors
import com.example.util.NumberFormatter

/**
 * Seri / İstikrar (Streak) Metrik Kartı
 */
@Composable
fun StreakSection(
    streak: Int,
    bestStreak: Int,
    lang: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border),
        modifier = modifier.heightIn(min = 95.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SpiritualFlameIcon(tint = colors.gold, size = 14.dp)
                Text(
                    text = strings.streakCardTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$streak ${strings.streakDay}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = colors.gold
                    ),
                    maxLines = 1
                )
            }
            Text(
                text = strings.bestStreakLabel.replace(
                    "{0}",
                    NumberFormatter.formatNumber(bestStreak.toLong(), lang)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
    }
}
