package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppStrings
import com.example.ui.theme.LocalAppColors
import com.example.util.NumberFormatter

@Composable
fun RoundCompletedDialog(
    completedRounds: Int,
    lang: String,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)
    
    val roundNum = completedRounds + 1
    val titleText = strings.roundCompletedTitle.replace("{0}", NumberFormatter.formatNumber(roundNum.toLong(), lang))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.card,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(2.dp, colors.gold, RoundedCornerShape(24.dp))
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BadgeHeroIcon(badgeId = "streak_21", tint = colors.gold, size = 64.dp)
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = colors.gold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.roundCompletedDesc,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.text,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.gold,
                        contentColor = colors.bg
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_start_new_round")
                ) {
                    Text(
                        text = strings.startNewRoundBtn,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    )
                }
            }
        }
    }
}
