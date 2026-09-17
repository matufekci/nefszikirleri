package com.example

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * GUVENLIK / VERI BUTUNLUGU kaynak kodu bekcisi.
 *
 * Kaynak dosyalari dogrudan okur (Robolectric gerektirmez) ve su
 * regresyonlarin sessizce geri gelmesini engeller:
 *
 *  1. REPLACE + CASCADE: ZikirDao'ya REPLACE stratejisinin geri gelmesi ya da
 *     repository katmaninda mevcut Zikir satirinin `zikirDao.insert(...)` ile
 *     "guncellenmesi". REPLACE, zikir_history'nin CASCADE ile silinmesine yol
 *     acar (bkz. HistoryPreservationOnZikirUpdateTest.canary_*).
 *  2. App Check: release yolunda DebugAppCheckProviderFactory kurulmasi.
 *     Debug provider yalnizca `BuildConfig.DEBUG` dalinda olmali;
 *     Play Integrity else dalinda.
 *  3. Loglama: production'da BuildConfig.DEBUG korumasi olmayan Log.* /
 *     println cagrilari (R8 yalnizca d/v/i'yi siler; w/e release'te kalir).
 *  4. Hassas veri: token / uid / password / key gibi degerlerin log
 *     satirlarina interpolasyonla yazilmasi.
 *  5. Migration zinciri: 1->2 ... 7->8 ve dogrudan yollar kayitli kalmali.
 */
class SecurityHygieneSourceGuardTest {

    private val mainDir: File by lazy {
        listOf(File("src/main/java"), File("app/src/main/java")).firstOrNull { it.isDirectory }
            ?: throw IllegalStateException("app/src/main/java bulunamadi (cwd=${File(".").absolutePath})")
    }

    private fun kotlinFiles(): List<File> =
        mainDir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    private fun read(relative: String): String = File(mainDir, relative).readText()

    // ------------------------------------------------------- 1. REPLACE+CASCADE

    @Test
    fun `repository mevcut zikir satirini REPLACE ile guncellemez`() {
        val repo = read("com/example/data/repository/ZikirRepository.kt")
        val lines = repo.lines()
        // Tek satirlik insert cagrisi: zikirDao.insert(x). Yalnizca
        // insertAll (ilk kurulum, bos tablo) ve replaceSnapshot serbesttir.
        val offenders = lines.withIndex()
            .filter { (_, l) -> Regex("""\bzikirDao\.insert\(""").containsMatchIn(l) }
            .map { (i, l) -> "${i + 1}: ${l.trim()}" }
        assertTrue(
            "ZikirRepository icinde zikirDao.insert(...) kullanimi yasak (mevcut satir icin ya REPLACE ile " +
                "CASCADE kaybi ya da ABORT ile crash demektir). update/updateTarget/markStarted kullan. Bulunan:\n" +
                offenders.joinToString("\n"),
            offenders.isEmpty()
        )
        // insertAll yalnizca bos-tablo baglamlarinda: ensureInitialized (getCount()==0) icinde.
        val insertAllLines = lines.withIndex().filter { (_, l) -> l.contains("zikirDao.insertAll(") }
        assertEquals("zikirDao.insertAll yalnizca ensureInitialized'da (ilk kurulum) olmali", 1, insertAllLines.size)
        val idx = insertAllLines.first().index
        val context = lines.subList(maxOf(0, idx - 6), idx).joinToString("\n")
        assertTrue("insertAll, getCount() == 0 kosulunun altinda olmali", context.contains("getCount() == 0"))
    }

    @Test
    fun `ZikirDao guvenli guncelleme yollarini sunar ve FK CASCADE korunur`() {
        val dao = read("com/example/data/local/ZikirDao.kt")
        assertFalse(
            "ZikirDao'da OnConflictStrategy.REPLACE OLMAMALI: zikirs ust tablodur, REPLACE = sil+ekle = " +
                "zikir_history CASCADE kaybi. Yeni satir icin ABORT, mevcut satir icin UPDATE kullan.",
            dao.contains("OnConflictStrategy.REPLACE")
        )
        assertTrue(dao.contains("@Update"))
        assertTrue(dao.contains("UPDATE zikirs SET target = :target, completedAt = :completedAt WHERE id = :id"))
        assertTrue(dao.contains("UPDATE zikirs SET startedAt = :now WHERE id = :id AND (startedAt IS NULL OR startedAt = 0)"))
        val history = read("com/example/data/model/ZikirHistory.kt")
        assertTrue("zikir_history FK ON DELETE CASCADE degismemeli (migration/sema uyumu)", history.contains("onDelete = ForeignKey.CASCADE"))
        assertTrue(history.contains("Index(value = [\"eventId\"], unique = true)"))
    }

    // ------------------------------------------------------------- 2. App Check

    @Test
    fun `release App Check yolunda Debug provider yok, Play Integrity var`() {
        val src = read("com/example/NefsApplication.kt")
        val start = src.indexOf("private fun initializeAppCheck()")
        assertTrue(start >= 0)
        val body = src.substring(start)

        val debugIdx = body.indexOf("DebugAppCheckProviderFactory.getInstance()")
        val playIdx = body.indexOf("PlayIntegrityAppCheckProviderFactory.getInstance()")
        assertTrue("Debug provider kurulumu bulunmali (debug gelistirici deneyimi)", debugIdx >= 0)
        assertTrue("Play Integrity provider kurulumu bulunmali (release)", playIdx >= 0)

        // Debug provider, ondan onceki en yakin `if (` kosulunda BuildConfig.DEBUG ile kapili olmali
        val beforeDebug = body.substring(0, debugIdx)
        val lastIf = beforeDebug.lastIndexOf("if (")
        assertTrue(lastIf >= 0)
        val condition = beforeDebug.substring(lastIf, minOf(beforeDebug.length, lastIf + 60))
        assertTrue(
            "DebugAppCheckProviderFactory yalnizca `if (BuildConfig.DEBUG)` dalinda kurulmali; bulunan kosul: $condition",
            condition.startsWith("if (BuildConfig.DEBUG)") || condition.startsWith("if (com.example.BuildConfig.DEBUG)")
        )
        // Play Integrity, debug dalindan SONRA (else) gelmeli ve debug'a bagli olmamali
        assertTrue("Play Integrity else dalinda olmali", playIdx > debugIdx)
        val between = body.substring(debugIdx, playIdx)
        assertTrue("debug ve Play Integrity kurulumlari arasinda else olmali", between.contains("} else {"))
        assertFalse(
            "Play Integrity kurulumu BuildConfig.DEBUG kosuluna bagli OLMAMALI",
            between.substringAfterLast("} else {").contains("BuildConfig.DEBUG")
        )
    }

    // -------------------------------------------------------------- 3. Loglama

    @Test
    fun `tum Log cagrilari BuildConfig DEBUG ile korunur ve println yok`() {
        val logCall = Regex("""\bLog\.(d|i|w|e|v|wtf)\(""")
        val offenders = mutableListOf<String>()
        for (f in kotlinFiles()) {
            val lines = f.readLines()
            for ((i, line) in lines.withIndex()) {
                val t = line.trim()
                if (t.startsWith("//") || t.startsWith("*") || t.startsWith("/*")) continue
                if (Regex("""\b(println|System\.out|System\.err|printStackTrace)\b""").containsMatchIn(t)) {
                    offenders.add("${f.name}:${i + 1}: $t")
                    continue
                }
                if (logCall.containsMatchIn(t)) {
                    val ctx = lines.subList(maxOf(0, i - 4), i + 1).joinToString("\n")
                    if (!ctx.contains("BuildConfig.DEBUG")) offenders.add("${f.name}:${i + 1}: $t")
                }
            }
        }
        assertTrue(
            "Production loglari BuildConfig.DEBUG ile korunmali (R8 yalnizca d/v/i'yi siler). Bulunan:\n" +
                offenders.joinToString("\n"),
            offenders.isEmpty()
        )
    }

    @Test
    fun `log satirlarina token uid parola anahtar veya ham yedek icerigi yazilmaz`() {
        val logCall = Regex("""\bLog\.(d|i|w|e|v|wtf)\(""")
        val sensitive = Regex(
            """\$\{?\s*[\w.]*(idToken|accessToken|authToken|token|uid|userId|password|passphrase|secretKey|secret|apiKey|\.email|jsonContent|jsonString|decryptedBytes|plaintext|ciphertext)\b""",
            RegexOption.IGNORE_CASE
        )
        val offenders = mutableListOf<String>()
        for (f in kotlinFiles()) {
            f.readLines().forEachIndexed { i, line ->
                if (logCall.containsMatchIn(line) && sensitive.containsMatchIn(line)) {
                    offenders.add("${f.name}:${i + 1}: ${line.trim()}")
                }
            }
        }
        assertTrue("Hassas deger log satirina interpolasyonla yazilmis:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }

    // ------------------------------------------------------------ 5. Migration

    @Test
    fun `migration zinciri ve dogrudan yollar kayitli kalir`() {
        val db = read("com/example/data/local/AppDatabase.kt")
        assertTrue(db.contains("version = 8"))
        val registration = db.substring(db.indexOf(".addMigrations("))
            .substringBefore(")")
        for (m in listOf(
            "MIGRATION_1_2", "MIGRATION_2_3", "MIGRATION_3_4", "MIGRATION_4_5", "MIGRATION_5_6",
            "MIGRATION_6_7", "MIGRATION_7_8", "MIGRATION_1_4", "MIGRATION_2_4", "MIGRATION_1_6"
        )) {
            assertTrue("$m addMigrations'a kayitli olmali", registration.contains(m))
        }
        assertFalse("Yikici fallback (fallbackToDestructiveMigration) KULLANILMAMALI", db.contains("fallbackToDestructiveMigration"))
        // 7->8 eventId doldurma mantigi korunmali
        assertTrue(db.contains("ADD COLUMN `eventId` TEXT NOT NULL DEFAULT ''"))
        assertTrue(db.contains("CREATE UNIQUE INDEX IF NOT EXISTS `index_zikir_history_eventId`"))
        assertTrue(db.contains("migrated_\${id}_\${zikirId}_\${timestamp}_"))
    }
}
