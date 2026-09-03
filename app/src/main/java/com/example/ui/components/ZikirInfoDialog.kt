package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppStrings
import com.example.data.model.ZikirContent
import com.example.ui.theme.LocalAppColors
import com.example.util.NumberFormatter

@Composable
fun ZikirInfoDialog(
    zikirId: Int,
    lang: String,
    targetCount: Long,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)
    val definition = ZikirContent.getZikirDefinition(zikirId)
    val zikirName = ZikirContent.getZikirName(zikirId, lang)
    val zikirDetail = ZikirContent.getZikirDetail(zikirId, lang)

    fun shareCard() {
        val shareText = "✦ ${strings.title} ✦\n\n" +
            "▪ ${strings.tabZikir}: $zikirName (${definition.arabicText})\n" +
            "▶ ${strings.target}: ${NumberFormatter.format(targetCount, lang)}\n\n" +
            "• ${strings.zikirInfoModalTitle}:\n$zikirDetail\n\n" +
            "✓ ${strings.sheikhName}"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "$zikirName - ${strings.zikirInfoModalTitle}")
        context.startActivity(shareIntent)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.card,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.5.dp, colors.border, RoundedCornerShape(24.dp))
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Arabic Calligraphy
                Text(
                    text = definition.arabicText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Translated Title
                Text(
                    text = zikirName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.primary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = colors.border)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = strings.zikirInfoModalTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = colors.text
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable rich spiritual content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .background(colors.inputBg, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = zikirDetail,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = colors.text
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Share Button
                OutlinedButton(
                    onClick = { shareCard() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.primary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_share_zikir_card")
                ) {
                    Text(
                        text = strings.shareCardBtn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.bg
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_close_info_modal")
                ) {
                    Text(
                        text = strings.closeBtn,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
