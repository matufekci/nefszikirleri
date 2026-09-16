package com.example

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * GERCEK CIHAZ/EMULATOR testleri (Prompt 13.1).
 *
 * Neden var: Robolectric birim testleri (185 adet) JVM uzerinde kosuyor ve
 * gercek Compose penceresi, gercek dokunma, gercek Activity yasam dongusu ve
 * gercek TalkBack semantics agaci URETMİYOR. Bu yuzden su uc davranis bugune
 * kadar hic cihazda dogrulanmamisti:
 *   1. Dokunusun sayaci GERCEKTEN artirip artirmadigi,
 *   2. Activity recreation sonrasi durumun korunmasi (SavedStateHandle),
 *   3. Secili sekme bilgisinin semantics agacina (TalkBack) ulasmasi.
 *
 * Bu sinif yalnizca MEVCUT davranisi olcer; hicbir uretim kodunu degistirmez.
 * Test tag'leri zaten uretim kodunda mevcuttu (test icin eklenmedi).
 */
@RunWith(AndroidJUnit4::class)
class CounterFlowInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    companion object {
        /** [MainApp] giris ekranini bu tercihle atliyor. */
        private const val PREFS_NAME = "nefs_app_prefs"
        private const val KEY_INTRO_COMPLETED = "intro_completed"

        /**
         * @BeforeClass, method seviyesindeki @Rule'dan ONCE kosar; boylece
         * Activity baslatilmadan once onboarding atlanmis olur. Aksi halde
         * testler karsilama ekraninda takilirdi.
         */
        @JvmStatic
        @BeforeClass
        fun skipIntroOnboarding() {
            InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_INTRO_COMPLETED, true)
                .commit()
        }
    }

    // ---------------------------------------------------------------- helpers

    /** "count / target" bicimindeki stateDescription'dan sayaci okur. */
    private fun readCountOrNull(): Long? {
        val nodes = composeRule.onAllNodesWithTag("giant_tap_button").fetchSemanticsNodes()
        if (nodes.isEmpty()) return null
        val stateDesc = nodes[0].config.getOrNull(SemanticsProperties.StateDescription) ?: return null
        val countPart = stateDesc.split("/").firstOrNull()?.trim() ?: return null
        val digits = countPart.filter { it.isDigit() }
        return digits.toLongOrNull()
    }

    /** Splash (min 3000 ms) gecene kadar sayac dugumunu bekler. */
    private fun waitCounter(timeoutMs: Long = 25_000L) {
        composeRule.waitUntil(timeoutMs) { readCountOrNull() != null }
        assertTrue(
            "Sayac ekrana gelmedi (giant_tap_button/stateDescription bulunamadi)",
            readCountOrNull() != null
        )
    }

    private fun waitCount(expected: Long, timeoutMs: Long = 15_000L) {
        composeRule.waitUntil(timeoutMs) { readCountOrNull() == expected }
    }

    // ------------------------------------------------------------------ tests

    /**
     * QA plani A3 + A9: uc dokunus sayaci TAM uc artirmali.
     * Kayip veya cift sayim burada yakalanir.
     */
    @Test
    fun threeTaps_incrementCounterByExactlyThree() {
        waitCounter()
        val before = readCountOrNull()!!

        repeat(3) {
            composeRule.onNodeWithTag("giant_tap_button").performClick()
        }

        waitCount(before + 3)
        assertEquals("Uc dokunus sayaci tam uc artirmali", before + 3, readCountOrNull())
    }

    /**
     * QA plani A4/A8: Activity recreation (configuration change / process
     * recreation benzeri) sonrasi sayac korunmali. SavedStateHandle +
     * Room birlikte dogru calisiyorsa deger ayni kalir.
     */
    @Test
    fun recreation_preservesCounterValue() {
        waitCounter()
        val before = readCountOrNull()!!

        composeRule.onNodeWithTag("giant_tap_button").performClick()
        waitCount(before + 1)
        assertEquals(before + 1, readCountOrNull())

        composeRule.activity.recreate()

        waitCounter()
        waitCount(before + 1)
        assertEquals(
            "Recreation sonrasi sayac korunmali",
            before + 1,
            readCountOrNull()
        )
    }

    /** selected=true tasiyan tag'leri dondurur. */
    private fun selectedTags(tags: List<String>): List<String> = tags.filter { tag ->
        val nodes = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes()
        nodes.isNotEmpty() && nodes[0].config.getOrNull(SemanticsProperties.Selected) == true
    }

    /**
     * QA plani E2: Prompt 9.1 duzeltmesinin cihaz dogrulamasi.
     *
     * KAPSAM NOTU (koddan dogrulandi): `.semantics { selected = isSelected }`
     * DhikrTabBar.kt:258'de YALNIZCA yan sekmelerin bulundugu `else` dalina
     * uygulanmis; merkez "zikir" sekmesi (isCenterAction = true, satir
     * ~159-181) `selected` TASIMIYOR. Bu yuzden test YAN sekmeler uzerinden
     * olcuyor. Merkez sekmedeki eksik ayrica raporlandi ve burada "beklenen
     * davranis" olarak sabitlenmedi (bir hatayi testle dondurmak istemiyoruz).
     *
     * Ikinci duzeltme: 5 sekme var (liste, istatistik, zikir, bilgi, ayarlar);
     * onceki surum 4 tag kontrol ediyordu ve `bilgi` eksikti.
     */
    @Test
    fun sideTabSelection_isExposedToSemantics_exactlyOneSelected() {
        waitCounter()

        composeRule.onNodeWithTag("tab_liste").performClick()

        val sideTabs = listOf("tab_liste", "tab_istatistik", "tab_bilgi", "tab_ayarlar")
        composeRule.waitUntil(15_000) { selectedTags(sideTabs).size == 1 }

        val selected = selectedTags(sideTabs)
        assertEquals(
            "Yan sekmelerden tam olarak biri selected isaretlenmeli (bulunan: $selected)",
            1,
            selected.size
        )
        assertTrue("Secili sekme tiklanan sekme olmali", selected.contains("tab_liste"))
    }

    /**
     * QA plani B12 + B13 (UI kismi):
     *  - Gizlilik politikasi satiri AYARLAR'da gorunur olmali (Play: politika
     *    uygulama icinden erisilebilir olmali).
     *  - URL henuz yapilandirilmadigi icin satira dokunmak CRASH üretmemeli.
     *  - Oturum acik olmadigi icin "Hesabi Sil" butonu GORUNMEMELI.
     */
    @Test
    fun settingsScreen_showsPrivacyRow_andHidesDeleteAccountWhenSignedOut() {
        waitCounter()

        composeRule.onNodeWithTag("tab_ayarlar").performClick()

        composeRule.waitUntil(15_000) {
            composeRule.onAllNodesWithTag("account_collapsible_header")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 1. Gizlilik politikasi satiri mevcut
        composeRule.onNodeWithTag("btn_privacy_policy").assertExists()

        // 2. Oturum yok -> hesap silme butonu olmamali
        composeRule.onAllNodesWithTag("btn_delete_account").assertCountEquals(0)

        // 3. URL bos oldugu icin dokunmak crash uretmemeli (guard mesaji gosterir)
        composeRule.onNodeWithTag("btn_privacy_policy").performClick()
        composeRule.waitForIdle()

        // Uygulama hala ayakta: ayarlar ekrani duruyor
        composeRule.onNodeWithTag("btn_privacy_policy").assertExists()
    }
}
