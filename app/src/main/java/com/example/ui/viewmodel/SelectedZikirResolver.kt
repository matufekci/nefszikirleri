package com.example.ui.viewmodel

import com.example.data.model.Zikir

/**
 * Seçili zikrin (aktif basamak) hangi id olacağını saf olarak hesaplar.
 *
 * ## Çözdüğü problem
 *
 * Zikir listesinde kilitli bir basamağa dokunulup "Öncekileri Tamamla ve Buradan
 * Başla" (hızlı intikal) onaylandığında [ZikirViewModel.fastJumpToZikir] tek bir
 * transaction içinde hem önceki basamakları tamamlıyor hem de
 * `settings.selectedZikirId` değerini hedefe yazıyor. Room bu iki tabloyu ayrı
 * sorgularla izlediği için `allZikirs` ve `settings` akışları `combine` bloğuna
 * AYRI emission'lar halinde düşebiliyor.
 *
 * Ayarlar satırı önce gelirse blok şu ikiliyle çalışıyordu:
 * (eski zikir listesi, yeni ayarlar). Hedef basamak eski listede hâlâ kilitli
 * göründüğü için "kilitliyse ilk eksik basamağa yönlendir" kuralı devreye giriyor,
 * bulunan değer `savedStateHandle`'a GERİ YAZILIYORDU. Bir sonraki emission artık
 * bu bozulmuş değeri okuduğu için kullanıcı hızlı intikalle geçtiği basamakta
 * değil, bir önceki çektiği zikirde kalıyordu.
 *
 * ## Çözüm
 *
 * Kullanıcının açık seçimi "bekleyen seçim" (pending) olarak tutulur. Akışlar
 * hedefle tutarlı hale gelene kadar kilit yönlendirmesi uygulanmaz; hedef açıldığı
 * anda seçim kesinleşir. Yazma hiç gerçekleşmezse zaman aşımı devreye girer ve
 * normal kurallara dönülür.
 */
object SelectedZikirResolver {

    const val TOTAL_ZIKIRS = 15

    /** Yazma DB'ye hiç yansımazsa seçimi sonsuza kadar kilitlememek için üst sınır. */
    const val PENDING_TIMEOUT_MS = 5_000L

    /** Kullanıcının yaptığı açık seçim ve yapıldığı an. */
    data class Pending(val zikirId: Int, val requestedAtMs: Long)

    /**
     * @param selectedId bu emission için seçili basamak
     * @param clearPending bekleyen seçim kesinleştiyse (veya zaman aşımına uğradıysa) true
     */
    data class Result(val selectedId: Int, val clearPending: Boolean)

    /** Bir basamağın açık olması için 1..(id-1) arası tüm basamaklar hedefine ulaşmış olmalı. */
    fun isUnlocked(id: Int, zikirs: List<Zikir>): Boolean {
        if (id <= 1) return true
        if (zikirs.isEmpty()) return false
        for (prevId in 1 until id) {
            val prev = zikirs.find { it.id == prevId } ?: return false
            if (prev.count < prev.target) return false
        }
        return true
    }

    /** Hedefine ulaşmamış ilk basamak; hepsi bittiyse 1. */
    fun firstIncompleteId(zikirs: List<Zikir>): Int {
        for (id in 1..TOTAL_ZIKIRS) {
            val z = zikirs.find { it.id == id } ?: return id
            if (z.count < z.target) return id
        }
        return 1
    }

    fun resolve(
        savedId: Int?,
        settingsSelectedId: Int,
        zikirs: List<Zikir>,
        pending: Pending?,
        nowMs: Long
    ): Result {
        val fallback = (savedId ?: settingsSelectedId).coerceIn(1, TOTAL_ZIKIRS)

        if (pending == null) {
            return Result(applyLockRule(fallback, zikirs), clearPending = false)
        }

        val target = pending.zikirId.coerceIn(1, TOTAL_ZIKIRS)
        val targetUnlocked = isUnlocked(target, zikirs)
        val settingsCaughtUp = settingsSelectedId.coerceIn(1, TOTAL_ZIKIRS) == target
        val expired = nowMs - pending.requestedAtMs > PENDING_TIMEOUT_MS

        return when {
            // Zikir listesi yetişti: hedef artık açık, seçim kesinleşti.
            targetUnlocked -> Result(target, clearPending = true)
            // Ayarlar yetişti ama liste hâlâ atlama öncesi: hedefi koru, yönlendirme.
            settingsCaughtUp -> Result(target, clearPending = false)
            // Yazma DB'ye yansımadı: normal kurallara dön.
            expired -> Result(applyLockRule(fallback, zikirs), clearPending = true)
            // Hiçbir akış yetişmedi: hedefi koru.
            else -> Result(target, clearPending = false)
        }
    }

    private fun applyLockRule(id: Int, zikirs: List<Zikir>): Int =
        if (zikirs.isNotEmpty() && !isUnlocked(id, zikirs)) firstIncompleteId(zikirs) else id
}
