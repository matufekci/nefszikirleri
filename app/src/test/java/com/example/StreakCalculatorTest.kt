package com.example

import com.example.util.StreakCalculator
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * En uzun ardisik gun serisi (best streak) hesabi.
 *
 * Regresyon: `ZikirViewModel` bu hesabi "yyyy-MM-dd" -> yerel gece yarisi parse
 * edip farki 86.400.000 ms'e bolerek yapiyordu. Yaz saati uygulayan bolgelerde
 * (Europe/Berlin, Europe/Paris) bahar gecisinde iki gun arasi 23 saat oldugu
 * icin bolum 0 cikiyor ve seri YANLISLIKLA kopuyordu. Asagidaki son test bu
 * hatayi belgeleyip yeni hesabin etkilenmedigini kanitlar.
 */
class StreakCalculatorTest {

    private val originalTimeZone = TimeZone.getDefault()

    @After
    fun restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `gun sayisi bilinen tarihlerle eslesir`() {
        // Beklenen degerler bagimsiz olarak takvim aritmetigiyle hesaplandi.
        assertEquals(0L, StreakCalculator.daysFromCivil(1970, 1, 1))
        assertEquals(-1L, StreakCalculator.daysFromCivil(1969, 12, 31))
        assertEquals(10957L, StreakCalculator.daysFromCivil(2000, 1, 1))
        assertEquals(19782L, StreakCalculator.daysFromCivil(2024, 2, 29)) // artik yil
        assertEquals(19813L, StreakCalculator.daysFromCivil(2024, 3, 31))
    }

    @Test
    fun `en uzun seri bulunur`() {
        assertEquals(
            3,
            StreakCalculator.longestRunDays(
                listOf("2024-03-01", "2024-03-02", "2024-03-03", "2024-03-05")
            )
        )
        assertEquals(
            4,
            StreakCalculator.longestRunDays(
                listOf("2024-03-01", "2024-03-02", "2024-04-10", "2024-04-11", "2024-04-12", "2024-04-13")
            )
        )
    }

    @Test
    fun `ay ve yil siniri ile artik yil dogru islenir`() {
        assertEquals(2, StreakCalculator.longestRunDays(listOf("2023-12-31", "2024-01-01")))
        assertEquals(3, StreakCalculator.longestRunDays(listOf("2024-02-28", "2024-02-29", "2024-03-01")))
        // Artik yil OLMAYAN yilda 28 Subat -> 1 Mart ardisiktir.
        assertEquals(2, StreakCalculator.longestRunDays(listOf("2023-02-28", "2023-03-01")))
    }

    @Test
    fun `sirasiz tekrarli ve bozuk kayitlar`() {
        assertEquals(
            3,
            StreakCalculator.longestRunDays(
                listOf("2024-03-03", "2024-03-01", "bozuk", "2024-03-02", "2024-03-02", "")
            )
        )
    }

    @Test
    fun `tek gun 1 sayilir ve bos liste 0 doner`() {
        // Eski kod tek gunluk gecmiste 0 veriyordu (seri yalnizca komsu cift
        // bulunca artiyordu); dogru deger 1.
        assertEquals(1, StreakCalculator.longestRunDays(listOf("2024-05-05")))
        assertEquals(0, StreakCalculator.longestRunDays(emptyList()))
        assertEquals(0, StreakCalculator.longestRunDays(listOf("gecersiz")))
    }

    @Test
    fun `DST gecisinde eski yontem seriyi kopariyordu, yeni yontem koparmaz`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // Yaz saati 31 Mart 2024'te 02:00'de basliyor; o takvim gunu 23 saat.
        // Bu yuzden KIRILAN cift 31 Mart -> 1 Nisan (30->31 Mart degil: iki
        // gece yarisi da +01:00 oldugu icin arasi tam 24 saat).
        val springMs = sdf.parse("2024-04-01")!!.time - sdf.parse("2024-03-31")!!.time
        assertEquals(23L, springMs / 3_600_000L)
        // Eski kodun bolmesi bunu 0 gun sayiyordu -> seri kopuyordu.
        assertEquals(0L, springMs / 86_400_000L)

        // Sonbahar gecisi (25 saatlik gun) eski bolmede 1 cikiyordu; yani hata
        // yalnizca bahar gecisinde goruluyordu.
        val autumnMs = sdf.parse("2024-10-28")!!.time - sdf.parse("2024-10-27")!!.time
        assertEquals(25L, autumnMs / 3_600_000L)
        assertEquals(1L, autumnMs / 86_400_000L)

        // Yeni hesap takvim gunu kullandigi icin DST'den hic etkilenmez.
        assertEquals(2, StreakCalculator.longestRunDays(listOf("2024-03-31", "2024-04-01")))
        assertEquals(
            3,
            StreakCalculator.longestRunDays(listOf("2024-03-30", "2024-03-31", "2024-04-01"))
        )
        assertEquals(2, StreakCalculator.longestRunDays(listOf("2024-10-27", "2024-10-28")))
    }
}
