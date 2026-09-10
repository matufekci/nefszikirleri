package com.example.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalAppColors

/**
 * Onboarding Üst Barı: Dinamik Adım İndikatörü ve Hızlı Atlama Butonu
 */
@Composable
fun OnboardingTopBar(
    currentStep: Int,
    totalSteps: Int,
    currentLang: String,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Adım İndikatör Çubukları
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until totalSteps) {
                val isActive = i == currentStep
                val isPast = i < currentStep
                val animWidth by animateDpAsState(
                    targetValue = if (isActive) 26.dp else 8.dp,
                    label = "step_indicator_width"
                )
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(animWidth)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                isActive -> colors.gold
                                isPast -> colors.primary
                                else -> colors.border.copy(alpha = 0.6f)
                            }
                        )
                )
            }
        }

        // Hızlı Atlama Butonu (İlk adım haricinde görünür)
        if (currentStep > 0) {
            TextButton(
                onClick = onSkip,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.testTag("intro_skip_button")
            ) {
                Text(
                    text = when (currentLang) {
                        "ar" -> "تخطي والبدء"
                        "de" -> "Überspringen"
                        "fr" -> "Passer"
                        "en" -> "Skip & Start"
                        else -> "Doğrudan Başla"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.textMuted
                )
            }
        } else {
            Spacer(modifier = Modifier.width(60.dp))
        }
    }
}

/**
 * Onboarding Alt Barı: Geri ve İleri / Başla Aksiyon Butonları
 */
@Composable
fun OnboardingBottomBar(
    currentStep: Int,
    totalSteps: Int,
    currentLang: String,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.border),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.card,
                    contentColor = colors.text
                ),
                modifier = Modifier
                    .weight(0.9f)
                    .heightIn(min = 52.dp)
                    .shadow(
                        elevation = if (colors.isDark) 0.dp else 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = Color(0x14000000),
                        ambientColor = Color(0x0A000000)
                    )
                    .testTag("intro_back_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Geri",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (currentLang) {
                            "ar" -> "رجوع"
                            "de" -> "Zurück"
                            "fr" -> "Retour"
                            "en" -> "Back"
                            else -> "Geri"
                        },
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = Color.White
            ),
            modifier = Modifier
                .weight(if (currentStep > 0) 2.1f else 1f)
                .heightIn(min = 52.dp)
                .shadow(
                    elevation = if (colors.isDark) 6.dp else 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = colors.primary.copy(alpha = 0.45f),
                    ambientColor = Color(0x1F000000)
                )
                .testTag("intro_next_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (currentStep) {
                        0 -> when (currentLang) {
                            "ar" -> "ابدأ التخصيص"
                            "de" -> "Starten"
                            "fr" -> "Commencer"
                            "en" -> "Get Started"
                            else -> "Başla"
                        }
                        totalSteps - 1 -> when (currentLang) {
                            "ar" -> "ابدأ الذكر (بسم الله)"
                            "de" -> "Dhikr beginnen"
                            "fr" -> "Commencer le Dhikr"
                            "en" -> "Start Dhikr"
                            else -> "Zikre Başla (Bismillah)"
                        }
                        else -> when (currentLang) {
                            "ar" -> "متابعة"
                            "de" -> "Weiter"
                            "fr" -> "Continuer"
                            "en" -> "Continue"
                            else -> "Devam Et"
                        }
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (currentStep == totalSteps - 1) 14.5.sp else 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (currentStep == totalSteps - 1) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "İleri",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Arka Plan Ambiyans Işığı (Lüks zümrüt & altın dinlendirici radyal gradyan)
 */
@Composable
fun OnboardingHaloBackground(
    haloScale: Float,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width * 0.5f, size.height * 0.32f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.primary.copy(alpha = if (colors.isDark) 0.28f else 0.12f),
                    colors.gold.copy(alpha = if (colors.isDark) 0.15f else 0.06f),
                    Color.Transparent
                ),
                center = center,
                radius = size.width * 0.85f * haloScale
            ),
            center = center,
            radius = size.width * 0.85f * haloScale
        )
    }
}

/**
 * Manevi Logo ve Dönen Yörünge Animasyon Bileşeni
 */
@Composable
fun OnboardingEmblemWithOrbit(
    haloScale: Float,
    haloAlpha: Float,
    shimmerRotation: Float,
    appLogoDescription: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dış Altın Parıltı Halesi
        Box(
            modifier = Modifier
                .size(210.dp)
                .scale(haloScale)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            colors.gold.copy(alpha = haloAlpha * 0.6f),
                            colors.primary.copy(alpha = haloAlpha * 0.8f),
                            colors.gold.copy(alpha = haloAlpha * 0.3f),
                            colors.secondary.copy(alpha = haloAlpha * 0.7f),
                            colors.gold.copy(alpha = haloAlpha * 0.6f)
                        )
                    )
                )
                .blur(20.dp)
        )

        // Dönen İnce Altın Yörünge
        Canvas(
            modifier = Modifier
                .size(190.dp)
                .rotate(shimmerRotation)
        ) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        colors.gold,
                        Color.Transparent,
                        colors.primary,
                        Color.Transparent,
                        colors.gold
                    )
                ),
                radius = size.width / 2f,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Ana Özel Tasarım Zikir Amblem Logosu
        Box(
            modifier = Modifier
                .size(160.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = colors.gold.copy(alpha = 0.5f),
                    ambientColor = colors.primary.copy(alpha = 0.3f)
                )
                .clip(CircleShape)
                .border(2.5.dp, colors.gold.copy(alpha = 0.85f), CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_nefs_app_icon_1787782143783),
                contentDescription = appLogoDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
