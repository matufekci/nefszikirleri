package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.example.ui.screens.OnboardingBottomBar
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Karsilama ekranindaki alt barin ERISILEBILIRLIK etiketleri.
 *
 * Regresyon: ikonlarin `contentDescription` degerleri sabit Turkce'ydi
 * ("Geri", "Ileri"). TalkBack kullanan Arapca/Almanca/Fransizca/Ingilizce bir
 * kullanici butonlari Turkce duyuyordu; ustelik gorunur etiket zaten 5 dilde
 * uretiliyordu, yani ayni metin iki kez yazilmisti ve biri cevrilmemisti.
 *
 * Bu test `OnboardingBottomBar`'i gercekten compose edip degisen satirlari
 * calistiriyor ve secilen dilin etiketinin semantik agacta oldugunu,
 * eski sabit Turkce etiketin ise olmadigini dogruluyor.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class OnboardingA11yLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun composeBar(lang: String, step: Int = 1) {
        composeTestRule.setContent {
            OnboardingBottomBar(
                currentStep = step,
                totalSteps = 3,
                currentLang = lang,
                onBack = {},
                onNext = {}
            )
        }
    }

    @Test
    fun `Turkce secilince etiketler Turkce`() {
        composeBar("tr")
        composeTestRule.onNodeWithContentDescription("Geri", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithContentDescription("Devam Et", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `Ingilizce secilince etiketler Ingilizce ve sabit Turkce etiket kalmiyor`() {
        composeBar("en")
        composeTestRule.onNodeWithContentDescription("Back", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithContentDescription("Continue", useUnmergedTree = true).assertExists()
        // Eski sabit metinler: "Geri" ve "İleri".
        composeTestRule.onNodeWithContentDescription("Geri", useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("İleri", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `Arapca secilince etiketler Arapca`() {
        composeBar("ar")
        composeTestRule.onNodeWithContentDescription("رجوع", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithContentDescription("متابعة", useUnmergedTree = true).assertExists()
    }

    // NOT: her test yalnizca BIR kez setContent cagirabilir (Compose test
    // kurali). Bu yuzden adimlar ayri testlerde dogrulaniyor.
    @Test
    fun `ilk adimda ileri etiketi Get Started`() {
        composeBar("en", step = 0)
        composeTestRule.onNodeWithContentDescription("Get Started", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `son adimda ileri etiketi Start Dhikr`() {
        composeBar("en", step = 2) // totalSteps - 1 -> son adim
        composeTestRule.onNodeWithContentDescription("Start Dhikr", useUnmergedTree = true).assertExists()
    }
}
