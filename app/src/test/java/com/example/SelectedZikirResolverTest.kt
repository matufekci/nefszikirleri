package com.example

import com.example.data.model.Zikir
import com.example.ui.viewmodel.SelectedZikirResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Hızlı intikal ("Öncekileri Tamamla ve Buradan Başla") sonrası kullanıcının seçtiği
 * basamakta kalıp kalmadığını doğrular.
 *
 * Regresyon: Room, `zikirs` ve `settings` sorgularını ayrı emission'lar halinde
 * teslim edebildiği için `combine` bloğu bir ara emission'da (ESKİ zikir listesi,
 * YENİ ayarlar) ikilisiyle çalışıyordu. Hedef basamak eski listede kilitli
 * göründüğü için seçim ilk eksik basamağa düşürülüp `savedStateHandle`'a geri
 * yazılıyordu; kullanıcı hızlı intikalle geçtiği basamakta değil, bir önceki
 * çektiği zikirde kalıyordu.
 */
class SelectedZikirResolverTest {

    private fun zikir(id: Int, target: Long = 100_000L, count: Long = 0L) =
        Zikir(id = id, target = target, count = count)

    /** 1..[completed] arası hedefi dolmuş, [inProgressId] yarım, kalanlar 0. */
    private fun progress(completed: Int, inProgressId: Int = completed + 1): List<Zikir> =
        (1..15).map { id ->
            when {
                id <= completed -> zikir(id, count = 100_000L)
                id == inProgressId -> zikir(id, count = 42_000L)
                else -> zikir(id, count = 0L)
            }
        }

    private val now = 1_000_000L

    @Test
    fun `hizli intikal - ayarlar yetisti ama liste eskiyse hedef korunur`() {
        // Kullanıcı 5. basamağa atladı. Transaction settings'i yazdı, zikir listesi
        // akışı henüz eski listeyi taşıyor (1 ve 2 tamam, 3 yarım).
        val staleList = progress(completed = 2, inProgressId = 3)

        val result = SelectedZikirResolver.resolve(
            savedId = 5,
            settingsSelectedId = 5,
            zikirs = staleList,
            pending = SelectedZikirResolver.Pending(zikirId = 5, requestedAtMs = now),
            nowMs = now + 10
        )

        // Eski davranış burada 3 döndürüyordu (bir önceki çekilen zikir).
        assertEquals(5, result.selectedId)
        // Liste henüz yetişmediği için bekleyen seçim korunmalı.
        assertFalse(result.clearPending)
    }

    @Test
    fun `hizli intikal - hicbir akis yetismediyse hedef korunur`() {
        val staleList = progress(completed = 2, inProgressId = 3)

        val result = SelectedZikirResolver.resolve(
            savedId = 5,
            settingsSelectedId = 3,
            zikirs = staleList,
            pending = SelectedZikirResolver.Pending(zikirId = 5, requestedAtMs = now),
            nowMs = now + 10
        )

        assertEquals(5, result.selectedId)
        assertFalse(result.clearPending)
    }

    @Test
    fun `hizli intikal - liste yetisince secim kesinlesir`() {
        // 1..4 tamamlandı, hedef (5) artık açık.
        val settledList = progress(completed = 4, inProgressId = 5)

        val result = SelectedZikirResolver.resolve(
            savedId = 5,
            settingsSelectedId = 5,
            zikirs = settledList,
            pending = SelectedZikirResolver.Pending(zikirId = 5, requestedAtMs = now),
            nowMs = now + 10
        )

        assertEquals(5, result.selectedId)
        assertTrue(result.clearPending)
    }

    @Test
    fun `bekleyen secim yokken kilitli basamak ilk eksige yonlendirilir`() {
        val list = progress(completed = 2, inProgressId = 3)

        val result = SelectedZikirResolver.resolve(
            savedId = 7,
            settingsSelectedId = 7,
            zikirs = list,
            pending = null,
            nowMs = now
        )

        assertEquals(3, result.selectedId)
        assertFalse(result.clearPending)
    }

    @Test
    fun `bekleyen secim yokken acik basamak korunur`() {
        val list = progress(completed = 4, inProgressId = 5)

        val result = SelectedZikirResolver.resolve(
            savedId = 5,
            settingsSelectedId = 5,
            zikirs = list,
            pending = null,
            nowMs = now
        )

        assertEquals(5, result.selectedId)
    }

    @Test
    fun `yazma dbye hic yansimazsa zaman asimiyla normal kurallara donulur`() {
        val list = progress(completed = 2, inProgressId = 3)

        val result = SelectedZikirResolver.resolve(
            savedId = 5,
            settingsSelectedId = 3,
            zikirs = list,
            pending = SelectedZikirResolver.Pending(zikirId = 5, requestedAtMs = now),
            nowMs = now + SelectedZikirResolver.PENDING_TIMEOUT_MS + 1
        )

        assertEquals(3, result.selectedId)
        assertTrue(result.clearPending)
    }

    @Test
    fun `kayitli id yoksa ayarlardaki secim kullanilir`() {
        val list = progress(completed = 4, inProgressId = 5)

        val result = SelectedZikirResolver.resolve(
            savedId = null,
            settingsSelectedId = 5,
            zikirs = list,
            pending = null,
            nowMs = now
        )

        assertEquals(5, result.selectedId)
    }

    @Test
    fun `sinir disindaki idler 1-15 arasina cekilir`() {
        val list = progress(completed = 14, inProgressId = 15)

        val result = SelectedZikirResolver.resolve(
            savedId = 99,
            settingsSelectedId = 99,
            zikirs = list,
            pending = null,
            nowMs = now
        )

        assertEquals(15, result.selectedId)
    }

    @Test
    fun `ilk basamak her zaman acik ve hepsi bittiyse ilk eksik 1`() {
        val allDone = (1..15).map { zikir(it, count = 100_000L) }

        assertTrue(SelectedZikirResolver.isUnlocked(1, emptyList()))
        assertTrue(SelectedZikirResolver.isUnlocked(15, allDone))
        assertEquals(1, SelectedZikirResolver.firstIncompleteId(allDone))
    }

    @Test
    fun `aradaki basamak eksikse sonraki basamak kilitli kalir`() {
        val list = progress(completed = 14, inProgressId = 15)

        assertTrue(SelectedZikirResolver.isUnlocked(15, list))
        assertFalse(SelectedZikirResolver.isUnlocked(15, progress(completed = 13, inProgressId = 14)))
    }
}
