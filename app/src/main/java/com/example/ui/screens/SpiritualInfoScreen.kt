package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.text.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.util.*
import android.content.*
import android.widget.Toast

@Composable
fun SpiritualInfoScreen(
    lang: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    fun shareSpiritualNotes() {
        val shareText = "${strings.sheikhTitle}\n" +
            "${strings.sheikhName}\n\n" +
            "${strings.notesTitle}\n" +
            "${strings.note1}\n\n" +
            "${strings.note2}\n\n" +
            "${strings.note3}\n\n" +
            "${strings.note4}"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, strings.notesTitle)
        context.startActivity(shareIntent)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // MANEVİ REHBER & NOTLAR KARTI
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.card,
            border = BorderStroke(1.5.dp, colors.gold.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // KADİRİ DERVİŞLERİNİN HİZMETÇİSİ
                Text(
                    text = strings.sheikhTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // SEYYİD ŞEYH MUHAMMED RUHİ KADİRİYYUL HÜSEYNi
                Text(
                    text = strings.sheikhName,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        lineHeight = 26.sp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFB45309), // Sıcak Koyu Altın
                                Color(0xFFFBBF24), // Parlak Varak
                                Color(0xFFD4AF37)  // Klasik Altın
                            )
                        ),
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.15f),
                            offset = Offset(1f, 1f),
                            blurRadius = 3f
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ZARİF ALTIN AYIRICI
                HorizontalDivider(
                    thickness = 1.2.dp,
                    color = colors.gold.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // NOTLAR BAŞLIĞI
                Text(
                    text = strings.notesTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = colors.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 1. NOT
                NoteItem(
                    text = strings.note1,
                    textColor = colors.text,
                    bulletColor = colors.gold
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.35f))
                Spacer(modifier = Modifier.height(14.dp))

                // 2. NOT
                NoteItem(
                    text = strings.note2,
                    textColor = colors.text,
                    bulletColor = colors.gold
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.35f))
                Spacer(modifier = Modifier.height(14.dp))

                // 3. NOT
                NoteItem(
                    text = strings.note3,
                    textColor = colors.text,
                    bulletColor = colors.gold
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.35f))
                Spacer(modifier = Modifier.height(14.dp))

                // 4. NOT
                NoteItem(
                    text = strings.note4,
                    textColor = colors.text,
                    bulletColor = colors.gold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Paylaş Butonu
        Button(
            onClick = { shareSpiritualNotes() },
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.bg
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .heightIn(min = 52.dp)
                .testTag("btn_share_spiritual_notes")
        ) {
            Icon(
                imageVector = Icons.Rounded.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.shareCardBtn,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(88.dp).navigationBarsPadding())
    }
}

@Composable
private fun NoteItem(
    text: String,
    textColor: Color,
    bulletColor: Color
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = 25.sp,
            fontWeight = FontWeight.Medium
        ),
        color = textColor,
        modifier = Modifier.fillMaxWidth()
    )
}
