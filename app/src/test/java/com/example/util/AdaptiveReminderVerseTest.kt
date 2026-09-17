package com.example.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Hareketsizlik hatırlatıcısının ayet havuzunu ve dönüş sırasını doğrular:
 * 5 uyarı ayeti, ardından 5 müjde ayeti; döngü bitince başa sarar.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdaptiveReminderVerseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun herDildeBesUyariBesMujdeAyetiVar() {
        for (lang in listOf("tr", "ar", "en", "de", "fr")) {
            val verses = AppStrings.get(lang).spiritualVerses
            assertEquals("$lang uyarı ayeti sayısı", 5, verses.count { it.type == "warning" })
            assertEquals("$lang müjde ayeti sayısı", 5, verses.count { it.type == "glad_tidings" })
        }
    }

    @Test
    fun ayetlerOnceUyariSonraMujdeSirasiylaGeliyor() {
        val warnings = (1..5).map { AdaptiveReminderManager.getInactivityVerse(context) }
        assertTrue("ilk 5 ayet uyarı olmalı", warnings.all { it.type == "warning" })

        val gladTidings = (1..5).map { AdaptiveReminderManager.getInactivityVerse(context) }
        assertTrue("sonraki 5 ayet müjde olmalı", gladTidings.all { it.type == "glad_tidings" })
    }

    @Test
    fun birDonguIcindeAyniAyetTekrarEtmiyor() {
        val cycle = (1..10).map { AdaptiveReminderManager.getInactivityVerse(context) }
        assertEquals(10, cycle.map { it.verseText }.toSet().size)
    }

    @Test
    fun donguBitinceBasaSariyor() {
        val first = AdaptiveReminderManager.getInactivityVerse(context)
        // 10 çağrı sonrası sayaç başa döner (0. indeks yine ilk uyarı ayeti).
        repeat(9) { AdaptiveReminderManager.getInactivityVerse(context) }
        val afterCycle = AdaptiveReminderManager.getInactivityVerse(context)
        assertEquals(first.verseText, afterCycle.verseText)
        assertEquals(first.type, afterCycle.type)
    }

    @Test
    fun ayetMetinleriBosDegil() {
        val cycle = (1..10).map { AdaptiveReminderManager.getInactivityVerse(context) }
        assertTrue(cycle.all { it.surah.isNotBlank() && it.verseText.isNotBlank() })
    }
}
