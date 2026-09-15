package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.ui.components.AnimatedIconSplash
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * AnimatedIconSplash regresyon testi.
 *
 * Geçmiş: d6b05e4'te splash, launcher mipmap'ini (adaptive-icon zinciri)
 * painterResource ile yüklüyordu ve cihazda her açılışta çökmeye yol açtı.
 * Bu testler kompozisyonun tamamını (güvenli ikon yükleme + while(true)
 * animasyon döngüleri + spring ölçekleme + başlık açığa çıkması) gerçek
 * kodla çalıştırır: herhangi bir aşama çökerse test kırmızıya döner.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class SplashAnimationTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun splash_tamAnimasyonDongulerini_cokmedenAtlatir() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            AnimatedIconSplash(
                primary = Color(0xFFC9A86A),
                textColor = Color.White,
                title = "Nefs Zikirleri",
                reduceMotion = false
            )
        }
        // ~21 saniye sanal zaman: halka dönüşü (12500ms) 1,7 tur + snapTo sarması,
        // parlama döngüsü (4500ms x2), kıvılcım (7800ms), süpürme (6600ms+2100ms),
        // spring ölçekleme ve 300ms gecikmeli başlık animasyonunu kapsar.
        repeat(210) { composeTestRule.mainClock.advanceTimeBy(100) }
        composeTestRule.waitForIdle()

        // Kompozisyon hayatta ve başlık açığa çıktı
        composeTestRule.onNodeWithText("Nefs Zikirleri").assertExists()
    }

    @Test
    fun splash_hareketiAzalt_acikken_statikCizilir() {
        composeTestRule.setContent {
            AnimatedIconSplash(
                primary = Color(0xFFC9A86A),
                textColor = Color.White,
                title = "Nefs Zikirleri",
                reduceMotion = true
            )
        }
        // reduceMotion'da başlık gecikmesiz görünür olmalı
        composeTestRule.onNodeWithText("Nefs Zikirleri").assertExists()
    }
}
