package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.ReminderSlot
import com.example.data.model.Zikir
import com.example.data.model.ZikirHistory
import com.example.data.repository.ZikirRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * REPLACE + ON DELETE CASCADE regresyon testleri.
 *
 * Kok neden: `zikir_history.zikirId -> zikirs.id` iliskisi ON DELETE CASCADE.
 * `@Insert(onConflict = REPLACE)` SQLite'ta ayni PK'li satiri SILIP yeniden
 * ekler; silme CASCADE'i tetikler ve o zikrin TUM gecmisi sessizce yok olur.
 * `updateZikirTarget()` ve `fastJumpToZikir()` eskiden bu yolu kullaniyordu:
 * kullanici hedefi degistirdiginde veya hizli intikal yaptiginda istatistik/
 * seri verisi kayboluyordu.
 *
 * Bu sinif, mevcut Zikir satirini degistiren HER repository islemi icin
 * history satir sayisinin, eventId'lerin, timestamp/dateKey degerlerinin
 * birebir korundugunu dogrular; ayrica REPLACE'in gercekten tehlikeli
 * oldugunu kanitlayan bir "canary" testi icerir (DAO uyarisi silinirse veya
 * FK semantigi degisirse bu test onu haber verir).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HistoryPreservationOnZikirUpdateTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ZikirRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ZikirRepository(db)
        runBlocking { repository.ensureInitialized() }
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ------------------------------------------------------------------ altyapi

    /** Gecmisin kimligini olusturan alanlar: bunlar degismemeli. */
    private data class HistoryFingerprint(
        val id: Long,
        val eventId: String,
        val zikirId: Int,
        val amount: Long,
        val type: String,
        val timestamp: Long,
        val dateKey: String
    )

    private fun ZikirHistory.fingerprint() =
        HistoryFingerprint(id, eventId, zikirId, amount, type, timestamp, dateKey)

    private suspend fun seedHistory(zikirId: Int, count: Int, baseTs: Long = 1_700_000_000_000L): List<ZikirHistory> {
        repeat(count) { i ->
            db.historyDao().insert(
                ZikirHistory(
                    eventId = "seed-$zikirId-$i",
                    zikirId = zikirId,
                    amount = 10L + i,
                    type = if (i % 4 == 3) "remove" else "add",
                    timestamp = baseTs + i * 1000L,
                    dateKey = "2026-09-%02d".format(1 + (i % 28))
                )
            )
        }
        return db.historyDao().getAllHistoryDirect().filter { it.zikirId == zikirId }
    }

    private suspend fun snapshotAll(): Set<HistoryFingerprint> =
        db.historyDao().getAllHistoryDirect().map { it.fingerprint() }.toSet()

    private fun assertHistoryUnchanged(before: Set<HistoryFingerprint>, after: Set<HistoryFingerprint>, op: String) {
        assertEquals("$op sonrasi history SAYISI degismemeli", before.size, after.size)
        assertEquals("$op sonrasi history eventId/timestamp/dateKey birebir korunmali", before, after)
        assertEquals(
            "$op sonrasi eventId'ler benzersiz kalmali",
            after.size,
            after.map { it.eventId }.toSet().size
        )
    }

    // ------------------------------------------------------------ 1. canary

    @Test
    fun canary_rawInsertOrReplaceOnExistingZikir_cascadesHistory() = runBlocking {
        // Bu test REPLACE'in neden yasakli oldugunu KANITLAR (ham SQL ile):
        // history 0'a dusuyorsa FK+REPLACE semantigi hala tehlikelidir ve
        // ZikirDao'da REPLACE'e geri donulmemelidir.
        seedHistory(zikirId = 2, count = 5)
        assertEquals(5, db.historyDao().getAllHistoryDirect().count { it.zikirId == 2 })

        val z2 = db.zikirDao().getZikirById(2)!!
        db.openHelper.writableDatabase.execSQL(
            "INSERT OR REPLACE INTO zikirs (id, target, count, startedAt, completedAt) VALUES (?, ?, ?, ?, ?)",
            arrayOf<Any?>(z2.id, z2.target + 1000L, z2.count, z2.startedAt, z2.completedAt)
        )

        val remaining = db.historyDao().getAllHistoryDirect().count { it.zikirId == 2 }
        assertEquals(
            "CANARY: INSERT OR REPLACE mevcut zikir satirini silip yeniden ekler ve " +
                "CASCADE ile gecmisi yok eder. Bu yuzden repository'de mevcut satir icin " +
                "ASLA REPLACE tabanli insert kullanilmamalidir.",
            0,
            remaining
        )
    }

    @Test
    fun zikirDaoInsert_onExistingRow_failsLoudlyInsteadOfSilentlyDeletingHistory() = runBlocking {
        // DAO artik ABORT: mevcut satir icin insert() sessizce degistirmek yerine hata verir
        // ve gecmis oldugu gibi kalir (yanlislikla eski davranisa donus = bu test kirmizi).
        seedHistory(zikirId = 3, count = 4)
        val before = snapshotAll()
        val z3 = db.zikirDao().getZikirById(3)!!
        try {
            db.zikirDao().insert(z3.copy(target = z3.target + 1L))
            fail("Mevcut id icin zikirDao.insert() SQLiteConstraintException firlatmaliydi")
        } catch (_: android.database.sqlite.SQLiteConstraintException) {
            // beklenen
        }
        assertHistoryUnchanged(before, snapshotAll(), "basarisiz insert()")
        assertEquals("hedef degismemis olmali", z3.target, db.zikirDao().getZikirById(3)!!.target)
    }

    // ------------------------------------------------ 2. updateZikirTarget

    @Test
    fun updateZikirTarget_preservesHistoryRows_eventIds_timestamps() = runBlocking {
        val seeded = seedHistory(zikirId = 1, count = 7)
        assertEquals(7, seeded.size)
        repository.addDhikrCount(1, 120L) // gercek akistan da bir kayit
        val before = snapshotAll()
        assertEquals(8, before.size)

        repository.updateZikirTarget(1, 5000L)

        val after = snapshotAll()
        assertHistoryUnchanged(before, after, "updateZikirTarget")

        // Hesaplama mantigi birebir korunmus olmali
        val z1 = db.zikirDao().getZikirById(1)!!
        assertEquals(5000L, z1.target)
        assertEquals(120L, z1.count)
        assertNotNull("startedAt korunmali", z1.startedAt)
        assertEquals("count < target iken completedAt null olmali", null, z1.completedAt)
    }

    @Test
    fun updateZikirTarget_belowCount_setsCompletedAt_keepsHistory() = runBlocking {
        repository.addDhikrCount(3, 900L)
        seedHistory(zikirId = 3, count = 3)
        val before = snapshotAll()

        repository.updateZikirTarget(3, 500L) // 900 >= 500 -> tamamlanmis sayilir

        assertHistoryUnchanged(before, snapshotAll(), "updateZikirTarget(count>=target)")
        val z3 = db.zikirDao().getZikirById(3)!!
        assertEquals(500L, z3.target)
        assertEquals(900L, z3.count)
        assertNotNull("count >= target oldugunda completedAt dolmali", z3.completedAt)

        // Tekrar yukari cekince completedAt sifirlanmali ama history yine korunmali
        val before2 = snapshotAll()
        repository.updateZikirTarget(3, 70000L)
        assertHistoryUnchanged(before2, snapshotAll(), "updateZikirTarget(geri yukari)")
        assertEquals(null, db.zikirDao().getZikirById(3)!!.completedAt)
    }

    @Test
    fun updateZikirTarget_clampsRange_keepsHistory() = runBlocking {
        seedHistory(zikirId = 4, count = 2)
        val before = snapshotAll()
        repository.updateZikirTarget(4, 1L) // alt sinir 100
        repository.updateZikirTarget(4, 99_999_999L) // ust sinir 5.000.000
        assertHistoryUnchanged(before, snapshotAll(), "updateZikirTarget(clamp)")
        assertEquals(5_000_000L, db.zikirDao().getZikirById(4)!!.target)
    }

    // -------------------------------------------------- 3. fastJumpToZikir

    @Test
    fun fastJumpToZikir_preservesExistingHistory_ofJumpedAndTargetZikirs() = runBlocking {
        // Atlanacak zikirler (1,2) ve hedef zikir (3) icin onceden gecmis olsun
        seedHistory(zikirId = 1, count = 4)
        seedHistory(zikirId = 2, count = 3)
        seedHistory(zikirId = 3, count = 2)
        repository.addDhikrCount(1, 50L)
        val before = snapshotAll()
        assertEquals(10, before.size)

        repository.fastJumpToZikir(3)

        val after = snapshotAll()
        // Eski satirlar birebir durmali (alt kume)
        val missing = before - after
        assertTrue("fastJumpToZikir mevcut history satirlarini silmemeli; kaybolan: $missing", missing.isEmpty())
        // Atlama, 1 ve 2 icin birer "add" satiri EKLER (mevcut davranis)
        val added = after - before
        assertEquals("fastJumpToZikir yalnizca atlanan 2 zikir icin yeni kayit eklemeli", 2, added.size)
        assertTrue(added.all { it.type == "add" && it.zikirId in setOf(1, 2) })
        assertEquals(after.size, after.map { it.eventId }.toSet().size)

        // Sayac/hedef mantigi degismemis olmali
        val z1 = db.zikirDao().getZikirById(1)!!
        val z2 = db.zikirDao().getZikirById(2)!!
        val z3 = db.zikirDao().getZikirById(3)!!
        assertEquals(z1.target, z1.count)
        assertEquals(z2.target, z2.count)
        assertNotNull(z1.completedAt)
        assertNotNull(z2.completedAt)
        assertEquals(0L, z3.count)
        assertNotNull("hedef zikir baslamis isaretlenmeli", z3.startedAt)
        assertEquals(3, db.settingsDao().getSettingsDirect()?.selectedZikirId)
    }

    @Test
    fun fastJumpToZikir_targetAlreadyStarted_keepsStartedAtAndHistory() = runBlocking {
        repository.addDhikrCount(2, 10L) // 2 zaten baslamis
        val startedAtBefore = db.zikirDao().getZikirById(2)!!.startedAt
        seedHistory(zikirId = 2, count = 3)
        val before = snapshotAll()

        repository.fastJumpToZikir(2)

        val after = snapshotAll()
        assertTrue((before - after).isEmpty())
        assertEquals("hedef zikir icin yeni kayit eklenmemeli", 1, (after - before).size) // sadece zikir 1 icin
        assertEquals("startedAt doluysa dokunulmamali", startedAtBefore, db.zikirDao().getZikirById(2)!!.startedAt)
    }

    @Test
    fun fastJumpToZikir_calledTwice_isIdempotentForHistory() = runBlocking {
        seedHistory(zikirId = 1, count = 2)
        repository.fastJumpToZikir(4)
        val afterFirst = snapshotAll()
        repository.fastJumpToZikir(4)
        assertHistoryUnchanged(afterFirst, snapshotAll(), "ikinci fastJumpToZikir")
    }

    // ----------------------------------- 4. diger mevcut-satir guncellemeleri

    @Test
    fun addRemoveUndo_doNotTouchUnrelatedHistory() = runBlocking {
        val seeded = seedHistory(zikirId = 5, count = 6).map { it.fingerprint() }.toSet()

        repository.addDhikrCount(5, 33L)
        repository.removeDhikrCount(5, 3L)
        repository.undoLastAction(5) // remove'u geri alir

        val after = snapshotAll()
        assertTrue("seed satirlari korunmali", after.containsAll(seeded))
        assertEquals(seeded.size + 1, after.size) // yalnizca 'add' kaldi
        assertEquals(33L, db.zikirDao().getZikirById(5)!!.count)
    }

    @Test
    fun updateTarget_thenFastJump_thenUpdateTarget_chainKeepsHistory() = runBlocking {
        seedHistory(zikirId = 1, count = 3)
        seedHistory(zikirId = 2, count = 3)
        val seeded = snapshotAll()

        repository.updateZikirTarget(1, 1000L)
        repository.updateZikirTarget(2, 2000L)
        repository.fastJumpToZikir(3)
        repository.updateZikirTarget(1, 3000L) // tamamlanmis zikrin hedefi yukari
        repository.updateZikirTarget(2, 150L) // asagi

        val after = snapshotAll()
        assertTrue("zincir islemler seed history'yi silmemeli: ${seeded - after}", after.containsAll(seeded))
        assertEquals(after.size, after.map { it.eventId }.toSet().size)
        val z1 = db.zikirDao().getZikirById(1)!!
        assertEquals(3000L, z1.target)
        assertEquals(1000L, z1.count) // fastJump 1000'e tamamladi, hedef sonradan 3000 oldu
        assertEquals(null, z1.completedAt) // 1000 < 3000
    }

    // ------------------------------------------------- 5. restore butunlugu

    private fun fullSnapshot(): Triple<List<Zikir>, List<ZikirHistory>, List<ReminderSlot>> {
        val zikirs = (1..15).map { id ->
            when (id) {
                1 -> Zikir(id = 1, target = 70000L, count = 70000L, startedAt = 1000L, completedAt = 5000L)
                2 -> Zikir(id = 2, target = 80000L, count = 1234L, startedAt = 2000L, completedAt = null)
                else -> Zikir(id = id, target = 70000L, count = 0L, startedAt = null, completedAt = null)
            }
        }
        val history = listOf(
            ZikirHistory(eventId = "ev-a", zikirId = 1, amount = 70000L, type = "add", timestamp = 3000L, dateKey = "2026-08-01"),
            ZikirHistory(eventId = "ev-b", zikirId = 2, amount = 1300L, type = "add", timestamp = 4000L, dateKey = "2026-08-02"),
            ZikirHistory(eventId = "ev-c", zikirId = 2, amount = 66L, type = "remove", timestamp = 4500L, dateKey = "2026-08-02"),
            ZikirHistory(eventId = "migrated_9_2_1_abcd1234", zikirId = 2, amount = 1L, type = "add", timestamp = 4600L, dateKey = "2026-08-03")
        )
        val slots = listOf(ReminderSlot(hour = 7, minute = 15, isEnabled = true), ReminderSlot(hour = 21, minute = 0, isEnabled = false))
        return Triple(zikirs, history, slots)
    }

    private suspend fun assertRestoredStateMatches(zikirs: List<Zikir>, history: List<ZikirHistory>) {
        val dbZikirs = db.zikirDao().getAllZikirsDirect()
        assertEquals(15, dbZikirs.size)
        for (expected in zikirs) {
            val actual = dbZikirs.first { it.id == expected.id }
            assertEquals("target id=${expected.id}", expected.target, actual.target)
            assertEquals("count id=${expected.id}", expected.count, actual.count)
            assertEquals("startedAt id=${expected.id}", expected.startedAt, actual.startedAt)
            assertEquals("completedAt id=${expected.id}", expected.completedAt, actual.completedAt)
        }
        val dbHistory = db.historyDao().getAllHistoryDirect()
        assertEquals("history sayisi", history.size, dbHistory.size)
        val expectedKeys = history.map { listOf(it.eventId, it.zikirId, it.amount, it.type, it.timestamp, it.dateKey) }.toSet()
        val actualKeys = dbHistory.map { listOf(it.eventId, it.zikirId, it.amount, it.type, it.timestamp, it.dateKey) }.toSet()
        assertEquals("eventId/timestamp/dateKey/amount/type birebir", expectedKeys, actualKeys)
        assertEquals("eventId benzersiz", dbHistory.size, dbHistory.map { it.eventId }.toSet().size)
        assertTrue("eventId bos olamaz", dbHistory.none { it.eventId.isBlank() })
    }

    @Test
    fun restoreFullLocalBackup_replacesEverything_andKeepsHistoryIdentity() = runBlocking {
        // Cihazda onceden veri olsun (restore bunu tamamen degistirmeli)
        repository.addDhikrCount(7, 500L)
        seedHistory(zikirId = 7, count = 4)

        val (zikirs, history, slots) = fullSnapshot()
        repository.restoreFullLocalBackup(
            zikirs = zikirs,
            history = history,
            slots = slots,
            settings = AppSettings(id = 1, completedRounds = 2, themeName = "emerald"),
            selectedZikirId = 2
        )

        assertRestoredStateMatches(zikirs, history)
        assertEquals("eski cihaz gecmisi kalmamali", 0, db.historyDao().getAllHistoryDirect().count { it.zikirId == 7 })
        assertEquals(2, db.reminderDao().getAllSlotsList().size)
        val s = db.settingsDao().getSettingsDirect()!!
        assertEquals(2, s.completedRounds)
        assertEquals(2, s.selectedZikirId)
        assertEquals("legacy tema canonical'e normalize edilmeli", "hadra_gece", s.themeName)
    }

    @Test
    fun restoreFullCloudBackup_replacesEverything_andKeepsHistoryIdentity() = runBlocking {
        repository.addDhikrCount(9, 250L)
        val (zikirs, history, slots) = fullSnapshot()

        repository.restoreFullCloudBackup(
            zikirs = zikirs,
            settings = AppSettings(id = 1, completedRounds = 4, selectedZikirId = 5),
            slots = slots,
            history = history
        )

        assertRestoredStateMatches(zikirs, history)
        assertEquals(0, db.historyDao().getAllHistoryDirect().count { it.zikirId == 9 })
        assertEquals(4, db.settingsDao().getSettingsDirect()!!.completedRounds)
    }

    @Test
    fun restoreBackup_legacy_replacesZikirs_andClearsHistoryDeliberately() = runBlocking {
        // Eski formatta history YOK; snapshot semantigi geregi gecmis temizlenir.
        repository.addDhikrCount(1, 100L)
        seedHistory(zikirId = 1, count = 2)
        val (zikirs, _, _) = fullSnapshot()

        repository.restoreBackup(zikirs = zikirs, completedRounds = 3)

        assertRestoredStateMatches(zikirs, emptyList())
        assertEquals(3, db.settingsDao().getSettingsDirect()!!.completedRounds)
    }

    @Test
    fun restore_restoreAgain_isIdempotent_andEventIdsStayUnique() = runBlocking {
        val (zikirs, history, slots) = fullSnapshot()
        repeat(2) {
            repository.restoreFullLocalBackup(zikirs, history, slots, AppSettings(id = 1), selectedZikirId = 1)
        }
        assertRestoredStateMatches(zikirs, history) // ikinci restore duplicate uretmemeli
    }

    @Test
    fun restoreAfterUpdates_thenUpdateTarget_keepsRestoredHistory() = runBlocking {
        val (zikirs, history, slots) = fullSnapshot()
        repository.restoreFullLocalBackup(zikirs, history, slots, AppSettings(id = 1), selectedZikirId = 2)
        val restored = snapshotAll()

        repository.updateZikirTarget(2, 90000L)
        repository.fastJumpToZikir(4)

        val after = snapshotAll()
        assertTrue("restore edilen history sonraki guncellemelerde korunmali: ${restored - after}", after.containsAll(restored))
    }

    // ------------------------------------------- 6. restore atomiklik (kesinti)

    /**
     * Restore'un ORTASINDA kesinti simulasyonu: dogrulamadan gecen bir payload
     * ile, yazim sirasinin ilerleyen bir adiminda (zikirs ve history yazildiktan
     * SONRA) SQLite seviyesinde hata uretilir. Bunu bir trigger ile yapiyoruz:
     * RAISE(ABORT) -> SQLiteConstraintException -> withTransaction rollback.
     * Beklenen: zikirs/history/slots/settings tamamen ESKI haliyle kalir
     * (yarim yazilmis veritabani yok).
     */
    private fun installCrashTrigger(table: String, event: String) {
        db.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER IF NOT EXISTS test_crash_$table BEFORE $event ON $table " +
                "BEGIN SELECT RAISE(ABORT, 'simulated crash during restore'); END"
        )
    }

    private fun removeCrashTrigger(table: String) {
        db.openHelper.writableDatabase.execSQL("DROP TRIGGER IF EXISTS test_crash_$table")
    }

    @Test
    fun restoreFullLocalBackup_failsMidway_leavesDatabaseUntouched() = runBlocking {
        repository.addDhikrCount(1, 77L)
        seedHistory(zikirId = 1, count = 3)
        val zikirsBefore = db.zikirDao().getAllZikirsDirect()
        val historyBefore = snapshotAll()
        val slotsBefore = db.reminderDao().getAllSlotsList()
        val settingsBefore = db.settingsDao().getSettingsDirect()

        val (zikirs, history, slots) = fullSnapshot()
        // Yazim sirasi: replaceSnapshot(zikirs) -> history.insertAll -> reminder.insertAll -> settings
        // Kesinti: reminder_slots insert'inde (zikirs + history ZATEN yazilmisken).
        installCrashTrigger("reminder_slots", "INSERT")
        try {
            repository.restoreFullLocalBackup(zikirs, history, slots, AppSettings(id = 1, completedRounds = 9), selectedZikirId = 1)
            fail("simule edilen kesintide restore basarili olmamaliydi")
        } catch (e: Exception) {
            // beklenen: SQLiteConstraintException (RAISE ABORT)
        } finally {
            removeCrashTrigger("reminder_slots")
        }

        // Transaction geri alinmis olmali: hicbir tablo yarim kalmamali
        assertEquals("zikirs eski haliyle kalmali", zikirsBefore, db.zikirDao().getAllZikirsDirect())
        assertEquals("history eski haliyle kalmali (snapshot'taki ev-* satirlari YOK)", historyBefore, snapshotAll())
        assertEquals("slots eski haliyle kalmali", slotsBefore, db.reminderDao().getAllSlotsList())
        assertEquals("settings eski haliyle kalmali", settingsBefore, db.settingsDao().getSettingsDirect())

        // Kesinti kalkinca ayni payload ile restore sorunsuz tamamlanmali
        repository.restoreFullLocalBackup(zikirs, history, slots, AppSettings(id = 1, completedRounds = 9), selectedZikirId = 1)
        assertRestoredStateMatches(zikirs, history)
    }

    @Test
    fun restoreFullCloudBackup_failsMidway_leavesDatabaseUntouched() = runBlocking {
        repository.addDhikrCount(2, 55L)
        seedHistory(zikirId = 2, count = 2)
        val zikirsBefore = db.zikirDao().getAllZikirsDirect()
        val historyBefore = snapshotAll()
        val settingsBefore = db.settingsDao().getSettingsDirect()

        val (zikirs, history, slots) = fullSnapshot()
        // Kesinti EN SON adimda (app_settings yazimi): zikirs+history+slots yazilmisken.
        installCrashTrigger("app_settings", "INSERT")
        try {
            repository.restoreFullCloudBackup(zikirs, AppSettings(id = 1, completedRounds = 4), slots, history)
            fail("simule edilen kesintide restore basarili olmamaliydi")
        } catch (e: Exception) {
            // beklenen
        } finally {
            removeCrashTrigger("app_settings")
        }
        assertEquals(zikirsBefore, db.zikirDao().getAllZikirsDirect())
        assertEquals(historyBefore, snapshotAll())
        assertEquals(settingsBefore, db.settingsDao().getSettingsDirect())
    }

    @Test
    fun restore_duplicateEventIdInPayload_isDeduplicatedNotDoubled() = runBlocking {
        // HistoryDao.insert/insertAll REPLACE'tir ve zikir_history'nin BAGIMLI
        // tablosu yoktur: ayni eventId payload'da iki kez gelirse crash olmaz,
        // tek satir kalir (idempotent restore). Bu, sync tekrarlarina karsi
        // bilincli davranistir; sayi ASLA ikiye katlanmamali.
        val (zikirs, history, slots) = fullSnapshot()
        val duplicated = history + history.first().copy(amount = history.first().amount)
        repository.restoreFullLocalBackup(zikirs, duplicated, slots, AppSettings(id = 1), selectedZikirId = 1)

        val dbHistory = db.historyDao().getAllHistoryDirect()
        assertEquals("tekrar eden eventId tek satira inmeli", history.size, dbHistory.size)
        assertEquals(dbHistory.size, dbHistory.map { it.eventId }.toSet().size)
    }

    @Test
    fun restore_invalidSnapshot_isRejectedBeforeAnyWrite() = runBlocking {
        repository.addDhikrCount(1, 10L)
        val before = snapshotAll()
        val (zikirs, history, slots) = fullSnapshot()
        val broken = zikirs.map { if (it.id == 1) it.copy(count = it.target + 1) else it } // count > target
        try {
            repository.restoreFullLocalBackup(broken, history, slots, AppSettings(id = 1), selectedZikirId = 1)
            fail("gecersiz snapshot reddedilmeliydi")
        } catch (e: IllegalArgumentException) {
            // beklenen (fail-fast dogrulama)
        }
        assertEquals(before, snapshotAll())
        assertEquals(10L, db.zikirDao().getZikirById(1)!!.count)
    }
}
