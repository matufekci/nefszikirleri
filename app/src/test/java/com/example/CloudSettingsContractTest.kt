package com.example

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bulut yedekleme ayar sozlesmesi (yaz <-> oku).
 *
 * Risk: `SyncManager.backupToCloud` ayarlari `settingsMap` icinde elle yaziyor,
 * `restoreFromCloud` ise `rawSettings["..."]` ile elle okuyor. Bu iki taraf
 * derleyici tarafindan BAGLANMIYOR. Bir alan `AppSettings`'e eklenip yazma
 * tarafina konur ama okuma tarafi unutulursa (veya tam tersi) uygulama
 * calismaya devam eder; kullanici yedegi geri yuklediginde o ayar sessizce
 * varsayilana duser. Bug bug bildirimi gelmeden fark edilmez.
 *
 * Bu test uc tarafi da (model, yazma, okuma) birbirine kilitler.
 */
class CloudSettingsContractTest {

    private val mainSource: File = findMainSourceRoot()
        ?: error(
            "app/src/main/java/com/example bulunamadi " +
                "(user.dir=${System.getProperty("user.dir")})"
        )

    /** `data/model/AppSettings.kt` icindeki tum `val alan:` bildirimleri. */
    private fun appSettingsFields(): Set<String> {
        val file = File(mainSource, "data/model/AppSettings.kt")
        assertTrue("AppSettings.kt bulunamadi", file.isFile)
        val text = file.readText()
        val classStart = text.indexOf("class AppSettings")
        assertTrue("AppSettings sinif bildirimi bulunamadi", classStart >= 0)
        val openParen = text.indexOf('(', classStart)
        assertTrue("AppSettings baslik parantezi bulunamadi", openParen > classStart)
        val body = balancedBlock(text, openParen, '(', ')')
        val fields = FIELD_REGEX.findAll(body).map { it.groupValues[1] }.toSet()
        assertTrue("AppSettings'ten hic alan ayiklanamadi", fields.isNotEmpty())
        return fields
    }

    /** `backupToCloud` icindeki `settingsMap = mapOf(...)` blogunun anahtarlari. */
    private fun writtenKeys(): Set<String> {
        val text = syncManagerText()
        val start = text.indexOf("val settingsMap = mapOf(")
        assertTrue("backupToCloud'ta settingsMap bulunamadi", start >= 0)
        val openParen = text.indexOf('(', start)
        assertTrue("settingsMap parantezi bulunamadi", openParen > start)
        val body = balancedBlock(text, openParen, '(', ')')
        val keys = KEY_TO_REGEX.findAll(body).map { it.groupValues[1] }.toSet()
        assertTrue("settingsMap'ten hic anahtar ayiklanamadi", keys.isNotEmpty())
        return keys
    }

    /** `restoreFromCloud` icindeki `rawSettings["..."]` okumalari. */
    private fun readKeys(): Set<String> = RAW_SETTINGS_REGEX
        .findAll(syncManagerText())
        .map { it.groupValues[1] }
        .toSet()

    private fun syncManagerText(): String {
        val file = File(mainSource, "data/cloud/SyncManager.kt")
        assertTrue("SyncManager.kt bulunamadi", file.isFile)
        return file.readText()
    }

    /** `open` indeksindeki parantezin esini bulur (string literallerini atlar). */
    private fun balancedBlock(text: String, open: Int, openChar: Char, closeChar: Char): String {
        var depth = 0
        var inString = false
        var i = open
        while (i < text.length) {
            val c = text[i]
            if (inString) {
                if (c == '\\') {
                    // Kacis dizisi: hem ters boluyu hem sonraki karakteri atla.
                    i += 2
                    continue
                }
                if (c == '"') inString = false
            } else {
                if (c == '"') {
                    inString = true
                } else if (c == openChar) {
                    depth++
                } else if (c == closeChar) {
                    depth--
                    if (depth == 0) return text.substring(open, i)
                }
            }
            i++
        }
        throw AssertionError("Parantez dengesi bulunamadi (index=$open)")
    }

    @Test
    fun `her AppSettings alani buluta yazilir`() {
        val missing = appSettingsFields() - IGNORED_FIELDS - writtenKeys()
        assertTrue(
            "Bu AppSettings alanlari buluta HIC yazilmiyor (geri yuklemede kaybolur): $missing",
            missing.isEmpty()
        )
    }

    @Test
    fun `her AppSettings alani buluttan geri okunur`() {
        val missing = appSettingsFields() - IGNORED_FIELDS - readKeys()
        assertTrue(
            "Bu AppSettings alanlari buluttan okunmuyor (geri yuklemede varsayilana duser): $missing",
            missing.isEmpty()
        )
    }

    @Test
    fun `yazilan ve okunan alan kumeleri birebir ayni`() {
        // lastSyncedAt ayarlara degil ayri bir alana okunur (sourceDoc.updatedAt),
        // bu yuzden karsilastirmadan muaf.
        val written = writtenKeys() - META_ONLY_KEYS
        val read = readKeys()
        assertEquals(
            "yazilip okunmayan: ${written - read} | okunup yazilmayan: ${read - written}",
            written,
            read
        )
    }

    @Test
    fun `ayrilan alanlar gerekceli ve sinirli`() {
        val fields = appSettingsFields()
        for (ignored in IGNORED_FIELDS) {
            assertTrue(
                "IGNORED_FIELDS icindeki '$ignored' artik AppSettings'te yok; listeden kaldir",
                ignored in fields
            )
        }
        for (meta in META_ONLY_KEYS) {
            assertTrue(
                "META_ONLY_KEYS icindeki '$meta' artik settingsMap'te yazilmiyor; listeden kaldir",
                meta in writtenKeys()
            )
        }
    }

    private companion object {
        val FIELD_REGEX = Regex("""\bval\s+(\w+)\s*:""")
        val KEY_TO_REGEX = Regex("\"(\\w+)\"\\s+to\\s")
        val RAW_SETTINGS_REGEX = Regex("""rawSettings\["(\w+)"\]""")

        /** Room birincil anahtari; buluta tasinmaz, geri yuklemede 1 atanir. */
        val IGNORED_FIELDS = setOf("id")

        /** Ayarlarin parcasi olmayan, meta amacli yazilan anahtarlar. */
        val META_ONLY_KEYS = setOf("lastSyncedAt")

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
