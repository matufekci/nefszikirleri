package com.example

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
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
 * GERCEK CIHAZ/EMULATOR testleri.
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
 *
 * API NOTU (run 35086809758 derleyici ciktisindan ogrenildi):
 *  - `assertExists` bu Compose surumunde `androidx.compose.ui.test` icinde
 *    TOP-LEVEL fonksiyon DEGIL (import'u "Unresolved reference" verdi).
 *    Bu yuzden varlik kontrolu, importu derleyici tarafindan DOGRULANMIS olan
 *    `assertCountEquals` ile yapiliyor.
 *  - `SemanticsConfiguration.getOrNull(...)` bir extension ve import
 *    edilmediginde cozulemuyor. Import yolu tahminine girmek yerine yalnizca
 *    SINIF UYESI olan `contains` (`key in cfg`) ve `get` (`cfg[key]`)
 *    operatorleri kullaniliyor.
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
            grantNotificationPermission()
        }

        /**
         * KOK NEDEN DUZELTMESI (run 35100282128 logcat kaniti).
         *
         * [MainApp] ilk kompozisyonda POST_NOTIFICATIONS iznini istiyor
         * (MainApp.kt:90-98, API 33+). Izin verilmeyince sistem izin dialogu
         * MainActivity'nin UZERINDE kaliyor; Activity hic cizilmiyor, Compose
         * root pencereye baglanmiyor ve 4 test de "No compose hierarchies
         * found in the app" ile patliyordu. Logcat'te MainActivity icin tek
         * bir "Displayed" satiri yoktu, buna karsilik GrantPermissionsActivity
         * 4 kez gorunuyordu.
         *
         * CI'daki `adb shell pm grant` bunu COZMUYOR: connectedDebugAndroidTest
         * uygulamayi bu satirdan SONRA kuruyor, yani grant calisirken paket
         * henuz kurulu degil ve komut sessizce basarisiz oluyordu. Izin burada,
         * UiAutomation uzerinden veriliyor; @BeforeClass Activity
         * baslatilmadan once kostugu icin dialog hic cikmiyor.
         */
        private fun grantNotificationPermission() {
            if (android.os.Build.VERSION.SDK_INT <
                android.os.Build.VERSION_CODES.TIRAMISU
            ) {
                return // API 33 altinda bu izin runtime izni degil
            }
            try {
                val instrumentation = InstrumentationRegistry.getInstrumentation()
                // SDK'nin HERKESE ACIK UiAutomation API'sinde yalnizca 2
                // argumanli surum var; UserHandle/userId alan 3 argumanli
                // surumler gizli. Kanit (run 35102817524 derleyici ciktisi):
                //   "Too many arguments for 'fun grantRuntimePermission(
                //    p0: String!, p1: String!): Unit'"
                // 2 argumanli surum izni mevcut kullanici icin verir.
                instrumentation.uiAutomation.grantRuntimePermission(
                    instrumentation.targetContext.packageName,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            } catch (ignored: Throwable) {
                // Izin verilemezse test yine kosar; dialog mesaji teshis
                // probunda (activityProbe/probeKnownTags) gorunur.
            }
        }
    }

    // ---------------------------------------------------------------- helpers

    /**
     * "count / target" bicimindeki stateDescription'dan sayaci okur.
     * Yalnizca SemanticsConfiguration UYE operatorleri kullanilir.
     */
    private fun readCountOrNull(): Long? {
        val nodes = composeRule.onAllNodesWithTag("giant_tap_button").fetchSemanticsNodes()
        if (nodes.isEmpty()) return null
        val cfg = nodes[0].config
        val key = SemanticsProperties.StateDescription
        if (key !in cfg) return null
        val stateDesc = cfg[key]
        val countPart = stateDesc.split("/").firstOrNull()?.trim() ?: return null
        val digits = countPart.filter { it.isDigit() }
        return digits.toLongOrNull()
    }

    /** Bir tag'in semantics agacinda selected=true tasip tasimadigi. */
    private fun isTagSelected(tag: String): Boolean {
        val nodes = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes()
        if (nodes.isEmpty()) return false
        val cfg = nodes[0].config
        val key = SemanticsProperties.Selected
        return key in cfg && cfg[key]
    }

    /**
     * TESHIS: basarisizlik aninda ekranda GERCEKTEN hangi bilinen tag'lerin
     * oldugunu tek satirda dondurur. Boylece annotation'dan "uygulama hangi
     * ekranda kaldi" (karsilama mi, ayarlar mi, hic mi acilmadi) okunabiliyor.
     * Ham log dosyalari bu sandbox'tan okunamadigi icin (blob storage
     * erisilemez) teshis bilgisinin assertion mesajinda tasinmasi sart.
     *
     * Tag'lerin hepsi uretim kodunda dogrulandi (testTag envanteri).
     */
    private fun probeKnownTags(): String {
        return try {
            val probe = listOf(
                "giant_tap_button", "btn_intro_google_sign_in", "btn_intro_sign_out",
                "btn_google_sign_in", "tab_liste", "tab_zikir", "tab_ayarlar",
                "account_collapsible_header", "btn_privacy_policy", "btn_next_zikir"
            )
            val found = probe.filter { tag ->
                composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
            }
            if (found.isEmpty()) "HICBIRI YOK" else found.joinToString(",")
        } catch (t: Throwable) {
            "PROBE YAPILAMADI: ${t.javaClass.simpleName}"
        }
    }

    /**
     * TESHIS: "No compose hierarchies found in the app" hatasinin 3 olasi
     * nedeninden hangisi gecerli, bunu ayirt eder. Activity HIC
     * baslatilamadiysa `composeRule.activity` erisimi istisna firlatir;
     * baslatilip hemen bittiyse isFinishing/isDestroyed true doner; ikisi de
     * degilse Activity ayakta demektir ve sorun setContent tarafindadir.
     */
    private fun activityProbe(): String {
        return try {
            val act = composeRule.activity
            "sinif=${act.javaClass.name} finishing=${act.isFinishing} " +
                "destroyed=${act.isDestroyed}"
        } catch (t: Throwable) {
            "activity ERISILEMEDI: ${t.javaClass.simpleName}: ${t.message}"
        }
    }

    /**
     * Splash (min 3000 ms) gecene kadar sayac dugumunu bekler.
     *
     * waitUntil ZAMAN ASIMINDA ConditionNotMetException FIRLATIR; bu yuzden
     * try/catch ile yutuluyor ki asagidaki assertTrue kendi teshis mesajini
     * uretebilsin. Aksi halde annotation'da yalnizca Compose'un genel
     * "condition not met" metni gorunuyor, ekranda ne oldugu gorunmuyor.
     */
    private fun waitCounter(timeoutMs: Long = 25_000L) {
        try {
            composeRule.waitUntil(timeoutMs) { readCountOrNull() != null }
        } catch (ignored: Throwable) {
            // asagidaki assertTrue teshis mesajiyla raporlayacak
        }
        val count: Long? = try {
            readCountOrNull()
        } catch (t: Throwable) {
            // "No compose hierarchies found in the app" buraya dusuyor. Ham
            // istisna teshis bilgisi ICERMIYOR; bu yuzden gercek neden
            // (Activity baslatildi mi / ayakta mi / hangi ekrandayiz)
            // assertion mesajina gomuluyor.
            assertTrue(
                "Semantics agacina erisilemedi: ${t.javaClass.simpleName}: ${t.message} " +
                    "|| Activity: [${activityProbe()}] || Tag'ler: [${probeKnownTags()}]",
                false
            )
            null
        }
        assertTrue(
            "Sayac ekrana gelmedi (giant_tap_button/stateDescription bulunamadi). " +
                "Activity: [${activityProbe()}] || Ekranda bulunan bilinen tag'ler: " +
                "[${probeKnownTags()}]",
            count != null
        )
    }

    /** Beklenen degere kadar bekler; tutmazsa cagiran assertEquals raporlar. */
    private fun waitCount(expected: Long, timeoutMs: Long = 15_000L) {
        try {
            composeRule.waitUntil(timeoutMs) { readCountOrNull() == expected }
        } catch (ignored: Throwable) {
            // cagiran assertEquals gercek degeri zaten yazdiriyor
        }
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
     * recreation benzeri) sonrasi sayac korunmali. SavedStateHandle + Room
     * birlikte dogru calisiyorsa deger ayni kalir.
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

    /**
     * QA plani E2: secili sekme bilgisinin TalkBack'e ulastiginin cihaz
     * dogrulamasi.
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
        try {
            composeRule.waitUntil(15_000) { sideTabs.count { isTagSelected(it) } == 1 }
        } catch (ignored: Throwable) {
            // asagidaki assertEquals hangi tag'lerin selected oldugunu yazdiriyor
        }

        val selected = sideTabs.filter { isTagSelected(it) }
        assertEquals(
            "Yan sekmelerden tam olarak biri selected isaretlenmeli (bulunan: $selected, " +
                "ekrandaki tag'ler: [${probeKnownTags()}])",
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

        try {
            composeRule.waitUntil(15_000) {
                composeRule.onAllNodesWithTag("account_collapsible_header")
                    .fetchSemanticsNodes().isNotEmpty()
            }
        } catch (ignored: Throwable) {
            // asagidaki assert'ler teshis mesajiyla raporlayacak
        }

        // 0. Ayarlar ekrani gercekten acildi mi (acilmadiysa nedeni gorunsun)
        assertTrue(
            "Ayarlar ekrani acilmadi. Ekranda bulunan bilinen tag'ler: [${probeKnownTags()}]",
            composeRule.onAllNodesWithTag("account_collapsible_header")
                .fetchSemanticsNodes().isNotEmpty()
        )

        // 1. Gizlilik politikasi satiri mevcut (tam olarak bir adet)
        composeRule.onAllNodesWithTag("btn_privacy_policy").assertCountEquals(1)

        // 2. Oturum yok -> hesap silme butonu olmamali
        composeRule.onAllNodesWithTag("btn_delete_account").assertCountEquals(0)

        // 3. URL bos oldugu icin dokunmak crash uretmemeli (guard mesaji gosterir)
        composeRule.onNodeWithTag("btn_privacy_policy").performClick()
        composeRule.waitForIdle()

        // Uygulama hala ayakta: ayarlar ekrani duruyor
        composeRule.onAllNodesWithTag("btn_privacy_policy").assertCountEquals(1)
    }
}
