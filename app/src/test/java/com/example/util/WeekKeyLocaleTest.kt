package com.example.util

import java.util.Calendar
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Hafta anahtarinin cihaz dilinden bagimsizligi (ISO-8601).
 *
 * Gecmiste olan hata: `getWeekYearKey`, `Calendar`'in `firstDayOfWeek` ve
 * `minimalDaysInFirstWeek` degerlerini CIHAZIN locale'inden aliyordu. Ayni gun
 * icin farkli dillerde farkli hafta numarasi cikiyordu (13 Eylul 2026 Pazar:
 * ISO'ya gore 2026-W37, hafta basi Pazar olan duzende 2026-W38). Haftalik
 * bildirim sayaci bu anahtara gore sifirlandigi icin kullanici telefon dilini
 * degistirdiginde sayac sifirlanip o hafta fazladan bildirim alabiliyordu.
 *
 * Beklenen degerler ISO hafta takviminden (hafta Pazartesi baslar, yilin ilk
 * haftasi en az 4 gun iceren haftadir).
 */
class WeekKeyLocaleTest {

    private lateinit var originalLocale: Locale

    @Before
    fun saveLocale() {
        originalLocale = Locale.getDefault()
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    /** Takvimi o dilin hafta ayarlariyla (firstDayOfWeek/minimalDays) kurar. */
    private fun calendarIn(locale: Locale, year: Int, month: Int, day: Int): Calendar {
        Locale.setDefault(locale)
        return Calendar.getInstance(locale).apply {
            clear()
            set(year, month, day, 12, 0, 0)
        }
    }

    @Test
    fun `hafta anahtari cihaz dilinden bagimsiz`() {
        val locales = listOf(
            Locale.US,                     // hafta Pazar, minimalDays = 1
            Locale.JAPAN,                  // hafta Pazar, minimalDays = 1
            Locale.FRANCE,                 // hafta Pazartesi, minimalDays = 4
            Locale("tr", "TR"),            // hafta Pazartesi, minimalDays = 1
            Locale("ar", "SA")             // hafta Cumartesi, minimalDays = 1
        )
        val keys = locales.map {
            it to AdaptiveReminderManager.getWeekYearKey(calendarIn(it, 2026, Calendar.SEPTEMBER, 13))
        }
        assertEquals(
            "Ayni gun icin farkli cihaz dilleri farkli hafta anahtari uretti: $keys",
            1,
            keys.map { it.second }.toSet().size
        )
        assertEquals("2026-W37", keys.first().second)
    }

    @Test
    fun `hafta pazartesi gunu devrilir`() {
        val sunday = AdaptiveReminderManager.getWeekYearKey(calendarIn(Locale.US, 2026, Calendar.SEPTEMBER, 13))
        val monday = AdaptiveReminderManager.getWeekYearKey(calendarIn(Locale.US, 2026, Calendar.SEPTEMBER, 14))
        assertEquals("2026-W37", sunday)
        assertEquals("2026-W38", monday)
        assertNotEquals("Pazar ve Pazartesi ayni hafta anahtarini verdi", sunday, monday)
    }

    @Test
    fun `yil sinirinda hafta ISO yilina yazilir`() {
        // 28 Aralik 2026 (Pazartesi) ve 1 Ocak 2027 (Cuma) ayni ISO haftasidir: 2026-W53.
        val dec28 = AdaptiveReminderManager.getWeekYearKey(calendarIn(Locale.US, 2026, Calendar.DECEMBER, 28))
        val jan01 = AdaptiveReminderManager.getWeekYearKey(calendarIn(Locale.US, 2027, Calendar.JANUARY, 1))
        val jan04 = AdaptiveReminderManager.getWeekYearKey(calendarIn(Locale.US, 2027, Calendar.JANUARY, 4))
        assertEquals("2026-W53", dec28)
        assertEquals("Ocak 1, ISO'ya gore hala 2026-W53 olmali", "2026-W53", jan01)
        assertEquals("2027-W01", jan04)
    }

    @Test
    fun `bicim her zaman dort hane yil ve iki hane hafta`() {
        val pattern = Regex("^\\d{4}-W\\d{2}$")
        for (day in 1..28) {
            val key = AdaptiveReminderManager.getWeekYearKey(calendarIn(Locale.US, 2026, Calendar.MARCH, day))
            assertTrue("Beklenmeyen bicim: $key", pattern.matches(key))
        }
    }
}
