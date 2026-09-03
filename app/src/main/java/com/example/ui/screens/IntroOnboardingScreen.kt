package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppStrings
import com.example.ui.theme.AppPalettes
import com.example.ui.theme.LocalAppColors
import com.example.ui.viewmodel.DhikrUiState
import com.example.ui.viewmodel.ZikirViewModel
import com.google.firebase.auth.FirebaseUser

/**
 * Manevi Açılış ve Kurulum Ekranı (Intro & Onboarding)
 * 
 * Özellikler:
 * 1. Karşılama Ekranı (Manevi Logo ve Başlık)
 * 2. Dil Seçimi (TR, AR, EN, DE, FR)
 * 3. Google Hesabı & Bulut Yedekleme Oturumu Açma
 */
@Composable
fun IntroOnboardingScreen(
    state: DhikrUiState,
    viewModel: ZikirViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()
    var currentLang by remember(state.settings.lang) { mutableStateOf(state.settings.lang) }
    val strings = AppStrings.get(currentLang)

    var currentStep by remember { mutableStateOf(0) } // 0: Karşılama, 1: Dil, 2: Oturum Açma (Google & Bulut)
    val totalSteps = 3

    // Animasyon Efektleri
    val infiniteTransition = rememberInfiniteTransition(label = "intro_infinite")
    
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )

    val shimmerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_rotation"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        // 1. ZEMİN ARKA PLAN AMBİYANS IŞIĞI (Lüks zümrüt & altın dinlendirici gradyan)
        Canvas(modifier = Modifier.fillMaxSize()) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ÜST KISIM: İlerleme Çubuğu ve Doğrudan Başla
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Adım İndikatörleri
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

                if (currentStep > 0) {
                    TextButton(
                        onClick = onComplete,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
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

            // ORTA KISIM: Adım İçeriği (Yüksek font ölçekleri ve küçük ekranlar için responsive kaydırma ve genişlik sınırlaması)
            val stepScrollState = rememberScrollState()
            LaunchedEffect(currentStep) {
                stepScrollState.scrollTo(0)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(stepScrollState),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width / 2 } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width / 2 } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width / 2 } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width / 2 } + fadeOut()
                            )
                        }
                    },
                    label = "intro_step_content"
                ) { step ->
                    when (step) {
                        0 -> WelcomeStepContent(
                            haloScale = haloScale,
                            haloAlpha = haloAlpha,
                            shimmerRotation = shimmerRotation,
                            lang = currentLang
                        )
                        1 -> LanguageStepContent(
                            currentLang = currentLang,
                            onSelectLang = { code ->
                                currentLang = code
                                viewModel.setLanguage(code)
                            }
                        )
                        2 -> LoginStepContent(
                            currentUser = currentUser,
                            isSyncing = isCloudSyncing,
                            onSignIn = { viewModel.signInWithGoogle(context) },
                            onSignOut = { viewModel.signOut() },
                            lang = currentLang
                        )
                    }
                }
            }

            // ALT KISIM: İleri / Geri Butonları (Tamamen Responsive, Taşma ve Kırpılma Korumalı)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- },
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
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = strings.backBtn,
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
                    onClick = {
                        if (currentStep < totalSteps - 1) {
                            currentStep++
                        } else {
                            onComplete()
                        }
                    },
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
                            contentDescription = strings.nextBtn,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1. ADIM: Ultra Lüks Karşılama ve Amblem
 */
@Composable
private fun WelcomeStepContent(
    haloScale: Float,
    haloAlpha: Float,
    shimmerRotation: Float,
    lang: String
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Manevi Amblem ve Lüks Işık Halkası
        Box(
            modifier = Modifier.size(220.dp),
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
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
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
                    contentDescription = strings.appLogo,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Besmele
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            ),
            color = colors.gold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Ana Başlık
        Text(
            text = when (lang) {
                "ar" -> "أذكار مراتب النفس"
                "de" -> "Dhikr der Nafs-Stufen"
                "fr" -> "Degrés de l'Âme & Zikr"
                "en" -> "Spiritual Stations of Nafs"
                else -> "Nefs Zikirleri"
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            ),
            color = colors.text,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Açıklama
        Text(
            text = when (lang) {
                "ar" -> "تركيبة ١٥ مرتبة لتزكية النفس، متابعة الورد اليومي، هداية روحية وسكينة للقلب بتصميم راقٍ ومريح."
                "de" -> "15 spirituelle Nafs-Stufen, täglicher Dhikr-Tracker, spirituelle Weisheit und Seelenfrieden."
                "fr" -> "15 degrés spirituels de l'âme, suivi du wird quotidien, sagesse spirituelle et paix du cœur."
                "en" -> "15 Spiritual stations of Nafs, daily dhikr tracker, spiritual wisdom, and radiant peace for the heart."
                else -> "Nefs Zikirleri, günlük vird takibi ve ruhu dinlendiren estetik ile kalbinize huzur."
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp
            ),
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * 2. ADIM: Dil Seçimi (Language Selection)
 */
@Composable
private fun LanguageStepContent(
    currentLang: String,
    onSelectLang: (String) -> Unit
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(currentLang)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = colors.primary.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Language,
                    contentDescription = strings.language,
                    tint = colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = when (currentLang) {
                "ar" -> "اختر لغتك"
                "de" -> "Wählen Sie Ihre Sprache"
                "fr" -> "Choisissez Votre Langue"
                "en" -> "Select Your Language"
                else -> "Dilinizi Seçin"
            },
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black
            ),
            color = colors.text,
            textAlign = TextAlign.Center
        )

        Text(
            text = when (currentLang) {
                "ar" -> "ستعرض واجهة التطبيق وشروحات الأذكار والورد بهذه اللغة فوراً."
                "de" -> "Die App-Oberfläche und Dhikr-Bedeutungen werden in dieser Sprache angezeigt."
                "fr" -> "L'interface et les explications seront affichées dans cette langue."
                "en" -> "App interface and dhikr meanings will be instantly displayed in this language."
                else -> "Uygulama arayüzü ve zikir açıklamaları bu dilde sunulacaktır."
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        val languages = listOf(
            Triple("tr", "Türkçe", "🇹🇷"),
            Triple("ar", "العربية", "🇸🇦"),
            Triple("en", "English", "🇬🇧"),
            Triple("de", "Deutsch", "🇩🇪"),
            Triple("fr", "Français", "🇫🇷")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            languages.forEach { (code, name, flag) ->
                val isSelected = currentLang.equals(code, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.card,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) colors.primary else colors.border
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (colors.isDark) 0.dp else if (isSelected) 3.dp else 1.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = if (isSelected) colors.primary.copy(alpha = 0.25f) else Color(0x10000000),
                            ambientColor = Color(0x08000000)
                        )
                        .clickable { onSelectLang(code) }
                        .testTag("intro_lang_$code")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text(
                                text = flag,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) colors.primary else colors.text,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = strings.selected,
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. ADIM: Karşılama Ekranı Oturum Açma (Google & Bulut Yedekleme) Adımı
 */
@Composable
private fun LoginStepContent(
    currentUser: FirebaseUser?,
    isSyncing: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    lang: String
) {
    val colors = LocalAppColors.current
    val strings = AppStrings.get(lang)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // İkon ve Başlık Kartı
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.card,
            border = BorderStroke(
                1.5.dp,
                if (currentUser != null) colors.gold.copy(alpha = 0.6f) else colors.border
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (colors.isDark) 0.dp else 4.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = if (currentUser != null) colors.gold.copy(alpha = 0.35f) else Color(0x10000000)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rozet / İkon Halesi
                Surface(
                    shape = CircleShape,
                    color = if (currentUser != null) colors.gold.copy(alpha = 0.18f) else colors.primary.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.5.dp,
                        if (currentUser != null) colors.gold.copy(alpha = 0.6f) else colors.primary.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.Rounded.CloudDone else Icons.Rounded.CloudSync,
                            contentDescription = strings.cloudSyncTitle,
                            tint = if (currentUser != null) colors.gold else colors.primary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Başlık
                Text(
                    text = if (currentUser != null) {
                        currentUser.displayName ?: when (lang) {
                            "ar" -> "تم تسجيل الدخول"
                            "de" -> "Angemeldet"
                            "fr" -> "Connecté"
                            "en" -> "Signed In"
                            else -> "Giriş Yapıldı"
                        }
                    } else {
                        when (lang) {
                            "ar" -> "تسجيل الدخول والمزامنة"
                            "de" -> "Anmeldung & Cloud"
                            "fr" -> "Connexion & Cloud"
                            "en" -> "Sign In & Cloud Sync"
                            else -> "Oturum Açma & Bulut"
                        }
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.text,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Alt Başlık / E-posta
                Text(
                    text = if (currentUser != null) {
                        currentUser.email ?: when (lang) {
                            "ar" -> "حساب Google متصل بنجاح"
                            "de" -> "Google-Konto verknüpft"
                            "fr" -> "Compte Google associé"
                            "en" -> "Google account linked"
                            else -> "Google hesabı bağlandı"
                        }
                    } else {
                        when (lang) {
                            "ar" -> "احفظ أورادك بأمان في السحابة"
                            "de" -> "Sichern Sie Ihre Daten in der Cloud"
                            "fr" -> "Sauvegardez vos données dans le cloud"
                            "en" -> "Keep your dhikrs safe in the cloud"
                            else -> "Zikirlerinizi bulutta güvenle yedekleyin"
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(18.dp))

                // Kullanıcı Durumuna Göre Aksiyon
                if (currentUser == null) {
                    Text(
                        text = when (lang) {
                            "ar" -> "سجل دخولك لتستعيد أورادك وإحصائياتك بنقرة واحدة عند تغيير هاتفك."
                            "de" -> "Melden Sie sich an, um Ihre Zikr-Daten bei Gerätewechsel mit einem Klick wiederherzustellen."
                            "fr" -> "Connectez-vous pour restaurer vos dhikrs en un clic en cas de changement d'appareil."
                            "en" -> "Sign in to easily restore your dhikrs and stats anytime you switch devices."
                            else -> "Cihaz değiştirseniz bile zikirleriniz ve hatimleriniz kaybolmaz, tek tıkla geri yüklenir."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onSignIn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.bg
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .testTag("btn_intro_google_sign_in"),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = colors.bg
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = when (lang) {
                                "ar" -> "تسجيل الدخول بواسطة Google"
                                "de" -> "Mit Google anmelden"
                                "fr" -> "Se connecter avec Google"
                                "en" -> "Sign in with Google"
                                else -> "Google ile Oturum Aç"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.primary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (lang) {
                                    "ar" -> "تم تفعيل النسخ الاحتياطي السحابي"
                                    "de" -> "Cloud-Backup ist aktiv"
                                    "fr" -> "Sauvegarde cloud activée"
                                    "en" -> "Cloud sync is enabled"
                                    else -> "Bulut senkronizasyonu hazır ve aktif"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .testTag("btn_intro_sign_out"),
                        enabled = !isSyncing
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (lang) {
                                "ar" -> "تسجيل الخروج"
                                "de" -> "Abmelden"
                                "fr" -> "Se déconnecter"
                                "en" -> "Sign Out"
                                else -> "Oturumu Kapat"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
