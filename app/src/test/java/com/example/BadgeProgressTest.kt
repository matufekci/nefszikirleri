package com.example

import com.example.ui.viewmodel.BadgeProgress
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Hızlı intikal ("Öncekileri Tamamla ve Buradan Başla") sonrası rozetlerin otomatik
 * tamamlanan basamaklar için tetiklenmemesini doğrular.
 *
 * Regresyon: 10. basamağa atlanınca 1-9 arası basamaklar otomatik tamamlanıyor,
 * `completedCount` 9'a fırlıyor ve "1/3/5/8 zikir tamamlandı" rozetleri üst üste
 * kutlanıyordu; kullanıcı her dokunuşta yeni bir rozet uyarısı görüyordu.
 */
class BadgeProgressTest {

    @Test
    fun atlamaIleTamamlananBasamaklarRozetKazandirmaz() {
        // Kullanıcı gerçekten 3 basamak bitirmişti, 10. basamağa atladı -> completedCount 9.
        // Atlama 6 basamak ekledi, offset 6.
        assertEquals(3, BadgeProgress.badgeCompletedCount(completedCount = 9, storedOffset = 6))
    }

    @Test
    fun atlamadanSonraSiraylaIlerlemeRozetKazandirmayaDevamEder() {
        // Offset sabit kalır; kullanıcı 10. ve 11. basamağı kendi çekince completedCount 11 olur.
        assertEquals(5, BadgeProgress.badgeCompletedCount(completedCount = 11, storedOffset = 6))
    }

    @Test
    fun atlamaYokkenRozetlerNormalIsler() {
        assertEquals(4, BadgeProgress.badgeCompletedCount(completedCount = 4, storedOffset = 0))
        assertEquals(300_000L, BadgeProgress.badgeTotalDone(totalDone = 300_000L, storedOffset = 0L))
    }

    @Test
    fun atlamaToplamZikirRozetleriniDeTetiklemez() {
        // 300k gerçek + 600k atlama ile eklenen = 900k; rozetler 300k üzerinden değerlendirilir.
        assertEquals(300_000L, BadgeProgress.badgeTotalDone(totalDone = 900_000L, storedOffset = 600_000L))
    }

    @Test
    fun sifirlamaSonrasiOffsetKendiniOnarir() {
        // Sıfırlama/yeni tur sonrası değerler 0'a döner; offset negatif sonuca yol açmamalı
        // ve sonraki gerçek ilerlemeler yeniden sayılmalı.
        assertEquals(0, BadgeProgress.badgeCompletedCount(completedCount = 0, storedOffset = 6))
        assertEquals(0L, BadgeProgress.badgeTotalDone(totalDone = 0L, storedOffset = 600_000L))
        // Kullanıcı yeniden 2 basamak bitirdiğinde offset clamp'lenmiş olur.
        assertEquals(0, BadgeProgress.badgeCompletedCount(completedCount = 2, storedOffset = 6))
    }

    @Test
    fun degerlerAslaNegatifOlmaz() {
        assertEquals(0, BadgeProgress.badgeCompletedCount(completedCount = 3, storedOffset = 99))
        assertEquals(0L, BadgeProgress.badgeTotalDone(totalDone = 100L, storedOffset = 99_999L))
        assertEquals(0, BadgeProgress.effectiveCompletedOffset(storedOffset = -5, completedCount = 10))
        assertEquals(0L, BadgeProgress.effectiveTotalOffset(storedOffset = -5L, totalDone = 10L))
    }

    @Test
    fun basamakSayisiOnBesiAsmaz() {
        assertEquals(15, BadgeProgress.badgeCompletedCount(completedCount = 15, storedOffset = 0))
    }
}
