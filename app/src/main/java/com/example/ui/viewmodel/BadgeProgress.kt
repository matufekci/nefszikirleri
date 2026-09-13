package com.example.ui.viewmodel

/**
 * Hızlı intikal ("Öncekileri Tamamla ve Buradan Başla") ile otomatik tamamlanan
 * basamakların rozet kazandırmaması için kullanılan saf hesap.
 *
 * Kullanıcı 10. basamağa atladığında 1-9 arası basamaklar otomatik tamamlanıyor ve
 * `completedCount` bir anda 9'a çıkıyordu; bu da "1/3/5/8 zikir tamamlandı"
 * rozetlerinin hepsini aynı anda tetikliyor, kullanıcı her dokunuşta bir rozet
 * kutlaması görüyordu. Rozetler artık yalnızca gerçekten sırayla ilerlemeye göre
 * değerlendiriliyor: atlamayla eklenen basamak/zikir miktarı bir offset olarak
 * tutulup güncel değerden düşülüyor.
 */
object BadgeProgress {

    /**
     * Offset hiçbir zaman güncel değeri aşamaz; böylece sıfırlama veya yeni tur
     * sonrasında (değerler 0'a döndüğünde) offset kendini otomatik olarak sıfırlar
     * ve sonraki gerçek ilerlemeler yeniden rozet kazandırır.
     */
    fun effectiveCompletedOffset(storedOffset: Int, completedCount: Int): Int =
        storedOffset.coerceIn(0, completedCount.coerceAtLeast(0))

    fun effectiveTotalOffset(storedOffset: Long, totalDone: Long): Long =
        storedOffset.coerceIn(0L, totalDone.coerceAtLeast(0L))

    /** Rozet değerlendirmesinde kullanılacak, sırayla tamamlanmış basamak sayısı. */
    fun badgeCompletedCount(completedCount: Int, storedOffset: Int): Int =
        (completedCount - effectiveCompletedOffset(storedOffset, completedCount)).coerceIn(0, 15)

    /** Rozet değerlendirmesinde kullanılacak, sırayla çekilmiş toplam zikir. */
    fun badgeTotalDone(totalDone: Long, storedOffset: Long): Long =
        (totalDone - effectiveTotalOffset(storedOffset, totalDone)).coerceAtLeast(0L)
}
