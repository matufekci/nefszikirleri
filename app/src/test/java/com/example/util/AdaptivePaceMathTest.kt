package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tempo matematiğinin saf birim testleri (Robolectric gerektirmez).
 *
 * Program gerçekleri: 15 zikir, toplam 1.140.000 (100k×3 + 70k×7 + 60k + 50k + 40k + 100k×2),
 * hedef 6 ayda (182 gün) bitirmek; en kötü ihtimalle 1 yılda 1 tur.
 */
class AdaptivePaceMathTest {

    private val roundTotal = 1_140_000L

    @Test
    fun turBasindaIhtiyacUstSinirda() {
        // Taze tur: 1.140.000 / 182 ≈ 6.264 → kullanıcı 5.000 üstüne zorlanmaz
        assertEquals(5000L, AdaptiveReminderManager.computeNeedDaily(roundTotal, 0))
    }

    @Test
    fun ihtiyacAslaBandinDisinaCikmaz() {
        for (elapsed in 0..400) {
            for (remaining in longArrayOf(0L, 1L, 100_000L, 570_000L, 1_140_000L, 5_000_000L)) {
                val need = AdaptiveReminderManager.computeNeedDaily(remaining, elapsed)
                assertTrue(
                    "need=$need (remaining=$remaining, elapsed=$elapsed) [3124,5000] dışında",
                    need in 3124L..5000L
                )
            }
        }
    }

    @Test
    fun enKotuIhtimalleYildaBirTurBiter() {
        // Alt sınır 3.124 = ceil(1.140.000/365): bu tempoyla tur ≤ 365 günde biter.
        val min = AdaptiveReminderManager.MIN_DAILY
        val days = Math.ceil(roundTotal.toDouble() / min).toLong()
        assertTrue("3.124/gün ile tur $days gün sürer, 365'i aşmamalı", days <= 365L)
        // Bir eksik tempoda (3.123) garanti bozulurdu — sınırın neden 3.124 olduğu buradan gelir.
        val daysBelow = Math.ceil(roundTotal.toDouble() / (min - 1)).toLong()
        assertTrue(daysBelow > 365L)
    }

    @Test
    fun idealTakvimdeIhtiyacZamanlaDuser() {
        // 91 günde turun yarısı bittiyse: 570.000 / 91 gün ≈ 6.264 → yine 5.000'e kırpılır
        assertEquals(5000L, AdaptiveReminderManager.computeNeedDaily(570_000, 91))
        // 152 günde 114.000 kaldıysa: 114.000 / 30 ≈ 3.800 → bant içinde gerçek değer
        val need = AdaptiveReminderManager.computeNeedDaily(114_000, 152)
        assertEquals(3800L, need)
    }

    @Test
    fun idealSureAsilincaIhtiyacUstSiniraYapisir() {
        // 182+ gün geçti, hâlâ 400.000 kaldı → daysLeft=1 → 5.000
        assertEquals(5000L, AdaptiveReminderManager.computeNeedDaily(400_000, 250))
    }

    @Test
    fun tempoBantlariDogruSinirlanir() {
        val need = 4000L
        assertEquals(AdaptiveReminderManager.PaceBand.ON_TRACK, AdaptiveReminderManager.paceBand(4000.0, need))
        assertEquals(AdaptiveReminderManager.PaceBand.ON_TRACK, AdaptiveReminderManager.paceBand(9000.0, need))
        assertEquals(AdaptiveReminderManager.PaceBand.MILD, AdaptiveReminderManager.paceBand(3999.0, need))
        assertEquals(AdaptiveReminderManager.PaceBand.MILD, AdaptiveReminderManager.paceBand(2400.0, need)) // %60
        assertEquals(AdaptiveReminderManager.PaceBand.BEHIND, AdaptiveReminderManager.paceBand(2399.0, need))
        assertEquals(AdaptiveReminderManager.PaceBand.BEHIND, AdaptiveReminderManager.paceBand(1200.0, need)) // %30
        assertEquals(AdaptiveReminderManager.PaceBand.CRITICAL, AdaptiveReminderManager.paceBand(1199.0, need))
        assertEquals(AdaptiveReminderManager.PaceBand.CRITICAL, AdaptiveReminderManager.paceBand(0.0, need))
    }

    @Test
    fun kotaBantlaBirlikteArtarVeSikmaz() {
        assertEquals(1, AdaptiveReminderManager.weeklyQuotaFor(AdaptiveReminderManager.PaceBand.ON_TRACK))
        assertEquals(2, AdaptiveReminderManager.weeklyQuotaFor(AdaptiveReminderManager.PaceBand.MILD))
        assertEquals(3, AdaptiveReminderManager.weeklyQuotaFor(AdaptiveReminderManager.PaceBand.BEHIND))
        assertEquals(4, AdaptiveReminderManager.weeklyQuotaFor(AdaptiveReminderManager.PaceBand.CRITICAL))
        // "Fazla sıkmadan": mutlak üst sınır haftada 4'ü geçemez
        assertTrue(AdaptiveReminderManager.PaceBand.values().all { AdaptiveReminderManager.weeklyQuotaFor(it) <= 4 })
    }

    @Test
    fun bosalanTurdaZorlamaYok() {
        // Tur bittiğinde (kalan 0) ihtiyaç alt sınıra iner; worker bu durumda zaten göndermez.
        assertEquals(AdaptiveReminderManager.MIN_DAILY, AdaptiveReminderManager.computeNeedDaily(0L, 10))
    }
}
