package com.example

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bildirim -> sekme derin baglantisi sozlesmesi.
 *
 * Gecmiste olan hata: `ReminderAlarmReceiver` ve `DailyTargetReminderReceiver`
 * bildirime `putExtra("open_tab", "zikir")` ekliyordu ama **ucuncu bildirim
 * kaynagi `DailyEvaluationWorker` (adaptif "manevi hareketsizlik" hatirlatmasi)
 * eklemiyordu**. Sonuc: o bildirime dokunan kullanici son acik sekmede
 * (ornegin Ayarlar) kaliyor, sayaca inmiyordu.
 *
 * Bu test su sozlesmeyi kilitler:
 *  1) `NotificationCompat.Builder` kuran HER dosya `open_tab` gonderir
 *     (yeni bir bildirim kaynagi eklenince de gecerli),
 *  2) gonderilen sekme kimligi `MainApp`'teki `when (targetTab)` dallarindan
 *     biridir (boyle bir dal yoksa kullanici bos ekran gorur),
 *  3) `MainActivity` extra'yi gercekten okur,
 *  4) bildirim `PendingIntent`'leri `FLAG_IMMUTABLE` ile kurulur
 *     (Android 12+ zorunlu; eksikse bildirim hic kurulmaz).
 */
class NotificationDeepLinkConsistencyTest {

    private val mainSource: File = findMainSourceRoot()
        ?: error("app/src/main/java/com/example dizini bulunamadi (user.dir=${System.getProperty("user.dir")})")

    private fun kotlinFiles(): List<File> = mainSource.walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .toList()

    private fun notificationSources(): List<File> = kotlinFiles()
        .filter { it.readText().contains("NotificationCompat.Builder") }
        .sortedBy { it.name }

    private fun openTabValues(): List<Pair<String, String>> = notificationSources()
        .flatMap { file ->
            OPEN_TAB_REGEX.findAll(file.readText())
                .map { file.name to it.groupValues[1] }
                .toList()
        }

    private fun tabIds(): Set<String> {
        val mainApp = File(mainSource, "ui/MainApp.kt")
        assertTrue("ui/MainApp.kt bulunamadi", mainApp.isFile)
        val text = mainApp.readText()
        val whenStart = text.indexOf(WHEN_TARGET_TAB)
        assertTrue("MainApp'te '$WHEN_TARGET_TAB' blogu bulunamadi", whenStart >= 0)
        val openBrace = text.indexOf('{', whenStart)
        assertTrue("'$WHEN_TARGET_TAB' blogunun acilis suslu parantezi yok", openBrace > whenStart)

        var depth = 0
        var end = -1
        for (i in openBrace until text.length) {
            when (text[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        end = i
                        break
                    }
                }
            }
        }
        assertTrue("'$WHEN_TARGET_TAB' blogu kapanmiyor", end > openBrace)

        val body = text.substring(openBrace, end)
        val ids = TAB_BRANCH_REGEX.findAll(body).map { it.groupValues[1] }.toSet()
        assertTrue("MainApp'te hic sekme dali bulunamadi", ids.isNotEmpty())
        return ids
    }

    @Test
    fun `bildirim ureten kaynaklar taranabiliyor`() {
        val sources = notificationSources()
        assertTrue(
            "Beklenen bildirim kaynaklari bulunamadi: ${sources.map { it.name }}",
            sources.size >= 3
        )
    }

    @Test
    fun `her bildirim kaynagi open_tab gonderir`() {
        val eksik = notificationSources()
            .filterNot { it.readText().contains("putExtra(\"open_tab\"") }
            .map { it.name }
        assertTrue(
            "Bu dosyalar bildirim kuruyor ama open_tab gondermiyor: $eksik",
            eksik.isEmpty()
        )
    }

    @Test
    fun `gonderilen sekme kimlikleri MainApp dallarinda var`() {
        val valid = tabIds()
        val sent = openTabValues()
        assertTrue("Hicbir kaynak open_tab gondermiyor", sent.isNotEmpty())
        for ((file, tab) in sent) {
            assertTrue(
                "$file tarafindan gonderilen '$tab' MainApp'te bir sekme dali degil (gecerli: $valid)",
                tab in valid
            )
        }
    }

    @Test
    fun `MainActivity open_tab extra sini okur`() {
        val activity = File(mainSource, "MainActivity.kt")
        assertTrue("MainActivity.kt bulunamadi", activity.isFile)
        val text = activity.readText()
        assertTrue(
            "MainActivity 'open_tab' extra'sini okumuyor",
            text.contains("getStringExtra(\"open_tab\")")
        )
        assertTrue(
            "MainActivity okudugu sekmeyi viewModel'e uygulamali",
            text.contains("viewModel.setTab(")
        )
    }

    @Test
    fun `bildirim PendingIntent leri FLAG_IMMUTABLE kullanir`() {
        val eksik = notificationSources()
            .filter { it.readText().contains("PendingIntent.getActivity(") }
            .filterNot { it.readText().contains("FLAG_IMMUTABLE") }
            .map { it.name }
        assertTrue("FLAG_IMMUTABLE kullanmayan bildirim kaynagi: $eksik", eksik.isEmpty())
    }

    @Test
    fun `tum bildirim kaynaklari ayni sekme kimligini kullanir`() {
        val distinct = openTabValues().map { it.second }.toSet()
        assertEquals(
            "Bildirim kaynaklari farkli hedef sekmeler gonderiyor: $distinct",
            1,
            distinct.size
        )
    }

    private companion object {
        val OPEN_TAB_REGEX = Regex("putExtra\\(\"open_tab\",\\s*\"([A-Za-z]+)\"\\)")
        val TAB_BRANCH_REGEX = Regex("\"([A-Za-z]+)\"\\s*->")
        const val WHEN_TARGET_TAB = "when (targetTab)"

        fun findMainSourceRoot(): File? {
            var dir: File? = File(System.getProperty("user.dir"))
            var hops = 0
            while (dir != null && hops < 6) {
                val candidate = File(dir, "app/src/main/java/com/example/MainActivity.kt")
                if (candidate.isFile) return File(dir, "app/src/main/java/com/example")
                dir = dir.parentFile
                hops++
            }
            return null
        }
    }
}
