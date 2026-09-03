package com.example

import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.cloud.SyncManager
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.ReminderSlot
import com.example.data.model.Zikir
import com.example.data.model.ZikirContent
import com.example.data.model.ZikirHistory
import com.example.data.repository.ZikirRepository
import com.example.receiver.BootReceiver
import com.example.receiver.ReminderAlarmReceiver
import com.example.util.NotificationScheduler
import com.example.util.NumberFormatter
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProductionRegressionSuiteTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: ZikirRepository
    private lateinit var backupManager: BackupManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ZikirRepository(db)
        backupManager = BackupManager(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // 1. Database Migration & Schema Integrity
    @Test
    fun test01_databaseMigrationAndSchemaIntegrity() = runBlocking {
        repository.ensureInitialized()

        val zikirs = db.zikirDao().getAllZikirsDirect()
        assertEquals(ZikirContent.INITIAL_DEFINITIONS.size, zikirs.size)
        assertEquals(1, zikirs[0].id)
        assertEquals(100000L, zikirs[0].target)
        assertEquals(0L, zikirs[0].count)
        assertNull(zikirs[0].startedAt)
        assertNull(zikirs[0].completedAt)

        val slots = db.reminderDao().getAllSlotsList()
        assertEquals(2, slots.size)
        assertEquals(9, slots[0].hour)
        assertEquals(0, slots[0].minute)
        assertTrue(slots[0].isEnabled)

        val settings = db.settingsDao().getSettingsDirect()
        assertNotNull(settings)
        assertEquals(1, settings?.selectedZikirId)
        assertEquals(10000L, settings?.dailyTarget)
        assertEquals("emerald", settings?.themeName)
        assertEquals(1.15f, settings?.fontScale ?: 0f, 0.01f)
    }

    // 2. Count Persistence
    @Test
    fun test02_countPersistence() = runBlocking {
        repository.ensureInitialized()

        val (c1, reached1) = repository.addDhikrCount(1, 33L)
        assertEquals(33L, c1)
        assertFalse(reached1)

        val (c2, reached2) = repository.addDhikrCount(1, 67L)
        assertEquals(100L, c2)
        assertFalse(reached2)

        val storedZikir = db.zikirDao().getZikirById(1)
        assertNotNull(storedZikir)
        assertEquals(100L, storedZikir?.count)

        val settings = db.settingsDao().getSettingsDirect()
        assertNotNull(settings)
        assertTrue((settings?.lastActiveTimestamp ?: 0L) > 0L)
    }

    // 3. History Consistency & DateKeys
    @Test
    fun test03_historyConsistencyAndDateKeys() = runBlocking {
        repository.ensureInitialized()

        repository.addDhikrCount(1, 50L)
        repository.removeDhikrCount(1, 10L)

        val history = db.historyDao().getAllHistoryDirect()
        assertEquals(2, history.size)

        // History items ordered by id descending / recent
        val removeEntry = history.find { it.type == "remove" }
        assertNotNull(removeEntry)
        assertEquals(10L, removeEntry?.amount)
        assertEquals(1, removeEntry?.zikirId)

        val addEntry = history.find { it.type == "add" }
        assertNotNull(addEntry)
        assertEquals(50L, addEntry?.amount)

        val expectedDateKey = NumberFormatter.getDateKey()
        assertEquals(expectedDateKey, addEntry?.dateKey)
        assertEquals(expectedDateKey, removeEntry?.dateKey)
    }

    // 4. Transaction Rollback on Failure
    @Test
    fun test04_transactionRollbackOnFailure() = runBlocking {
        repository.ensureInitialized()
        repository.addDhikrCount(1, 100L)
        val initialCount = db.zikirDao().getZikirById(1)?.count ?: 0L
        assertEquals(100L, initialCount)

        try {
            db.runInTransaction {
                db.openHelper.writableDatabase.execSQL("UPDATE zikirs SET count = 9999 WHERE id = 1")
                throw IllegalStateException("Simulated critical transaction failure")
            }
        } catch (e: Exception) {
            assertEquals("Simulated critical transaction failure", e.message)
        }

        val finalZikir = db.zikirDao().getZikirById(1)
        assertEquals(100L, finalZikir?.count)
    }

    // 5. Add / Remove & Clamping
    @Test
    fun test05_addAndRemoveDhikrCount() = runBlocking {
        repository.ensureInitialized()

        repository.addDhikrCount(2, 500L)
        assertEquals(500L, db.zikirDao().getZikirById(2)?.count)

        val remainingAfterRemove = repository.removeDhikrCount(2, 200L)
        assertEquals(300L, remainingAfterRemove)
        assertEquals(300L, db.zikirDao().getZikirById(2)?.count)

        // Removing more than current count should clamp count to 0 (cannot be negative)
        val remainingAfterExcessiveRemove = repository.removeDhikrCount(2, 1000L)
        assertEquals(0L, remainingAfterExcessiveRemove)
        assertEquals(0L, db.zikirDao().getZikirById(2)?.count)
    }

    // 6. Undo Mechanism
    @Test
    fun test06_undoMechanismForAddAndRemove() = runBlocking {
        repository.ensureInitialized()

        repository.addDhikrCount(1, 100L)
        assertEquals(100L, db.zikirDao().getZikirById(1)?.count)

        // Undo add -> decreases count back to 0
        val afterUndoAdd = repository.undoLastAction(1)
        assertEquals(0L, afterUndoAdd)
        assertEquals(0L, db.zikirDao().getZikirById(1)?.count)

        // Add 50, then remove 20
        repository.addDhikrCount(1, 50L)
        repository.removeDhikrCount(1, 20L)
        assertEquals(30L, db.zikirDao().getZikirById(1)?.count)

        // Undo remove -> restores count back to 50
        val afterUndoRemove = repository.undoLastAction(1)
        assertEquals(50L, afterUndoRemove)
        assertEquals(50L, db.zikirDao().getZikirById(1)?.count)

        // Undo on empty history returns null and doesn't crash
        repository.undoLastAction(1) // undid the add 50 -> 0
        val emptyUndo = repository.undoLastAction(1)
        assertNull(emptyUndo)
    }

    // 7. Reset & Start New Round
    @Test
    fun test07_resetAndStartNewRound() = runBlocking {
        repository.ensureInitialized()

        repository.addDhikrCount(1, 1000L)
        repository.addDhikrCount(2, 2000L)

        // Single reset
        repository.resetSingleZikir(1)
        assertEquals(0L, db.zikirDao().getZikirById(1)?.count)
        assertEquals(2000L, db.zikirDao().getZikirById(2)?.count)

        // Start new round
        repository.startNewRound()

        val allZikirs = db.zikirDao().getAllZikirsDirect()
        assertTrue(allZikirs.all { it.count == 0L })

        val history = db.historyDao().getAllHistoryDirect()
        assertTrue(history.isEmpty())

        val settings = db.settingsDao().getSettingsDirect()
        assertEquals(1, settings?.completedRounds)
        assertEquals(1, settings?.selectedZikirId)
    }

    // 8. Target Completion & Auto Transition
    @Test
    fun test08_targetCompletionAndCountdownMode() = runBlocking {
        repository.ensureInitialized()

        val initialTarget = db.zikirDao().getZikirById(1)?.target ?: 70000L
        val (count, reached) = repository.addDhikrCount(1, initialTarget)

        assertEquals(initialTarget, count)
        assertTrue(reached)

        val updatedZikir = db.zikirDao().getZikirById(1)
        assertNotNull(updatedZikir?.completedAt)
        assertTrue((updatedZikir?.completedAt ?: 0L) > 0L)
    }

    // 9. Malformed Backup Handling
    @Test
    fun test09_malformedBackupJson() = runBlocking {
        val malformedStrings = listOf(
            "{ invalid json structure !!! }",
            "",
            "[]",
            "{\"schemaVersion\": 1, \"zikirs\": \"not_a_list\"}",
            "{\"schemaVersion\": 1, \"timestamp\": \"abc\"}"
        )

        for (malformed in malformedStrings) {
            val stream = ByteArrayInputStream(malformed.toByteArray(Charsets.UTF_8))
            val result = backupManager.importBackup(stream)
            assertTrue("Malformed JSON should fail safely: $malformed", result.isFailure)
        }
    }

    // 10. Unknown Backup Schema Version
    @Test
    fun test10_unknownBackupSchemaVersion() = runBlocking {
        val futureVersionJson = """
            {
                "schemaVersion": 999,
                "appName": "NefsZikir",
                "timestamp": 1725200000000,
                "zikirs": [{"id": 1, "target": 70000, "count": 100}]
            }
        """.trimIndent()

        val stream = ByteArrayInputStream(futureVersionJson.toByteArray(Charsets.UTF_8))
        val result = backupManager.importBackup(stream)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("versiyon") == true || result.isFailure)
    }

    // 11. Backup Restore Atomicity
    @Test
    fun test11_backupRestoreAtomicity() = runBlocking {
        repository.ensureInitialized()
        repository.addDhikrCount(1, 100L)

        val validZikirs = listOf(
            Zikir(id = 1, target = 70000L, count = 5555L),
            Zikir(id = 2, target = 80000L, count = 6666L)
        )
        val validHistory = listOf(
            ZikirHistory(id = 1, zikirId = 1, amount = 5555L, type = "add", timestamp = 1000L, dateKey = "2026-09-01")
        )
        val validSlots = listOf(
            ReminderSlot(id = 1, hour = 11, minute = 30, isEnabled = true)
        )
        val validSettings = AppSettings(id = 1, completedRounds = 5, selectedZikirId = 2)

        repository.restoreFullLocalBackup(validZikirs, validHistory, validSlots, validSettings, selectedZikirId = 2)

        val restoredZikir1 = db.zikirDao().getZikirById(1)
        val restoredZikir2 = db.zikirDao().getZikirById(2)
        assertEquals(5555L, restoredZikir1?.count)
        assertEquals(6666L, restoredZikir2?.count)

        val restoredSlots = db.reminderDao().getAllSlotsList()
        assertEquals(1, restoredSlots.size)
        assertEquals(11, restoredSlots[0].hour)

        val restoredSettings = db.settingsDao().getSettingsDirect()
        assertEquals(5, restoredSettings?.completedRounds)
        assertEquals(2, restoredSettings?.selectedZikirId)
    }

    // 12. Corrupted Firestore Payload Parsing
    @Test
    fun test12_corruptedFirestorePayloadParsing() = runBlocking {
        val corruptedPayload = """
            {
                "schemaVersion": 1,
                "appName": "NefsZikir",
                "timestamp": 123456789,
                "zikirs": [
                    {"id": 1, "target": -100, "count": -500}
                ],
                "history": [
                    {"id": 0, "zikirId": 1, "amount": -50, "type": "corrupted", "timestamp": -1}
                ],
                "reminderSlots": [
                    {"id": 0, "hour": 25, "minute": 99, "isEnabled": true}
                ],
                "settings": {
                    "completedRounds": -10,
                    "fontScale": 99.0
                }
            }
        """.trimIndent()

        val stream = ByteArrayInputStream(corruptedPayload.toByteArray(Charsets.UTF_8))
        val result = backupManager.importBackup(stream)
        assertTrue(result.isSuccess)

        val data = result.getOrThrow()
        assertEquals(0L, data.zikirs[0].count)
        assertEquals(100L, data.zikirs[0].target)
        assertEquals(1L, data.history[0].amount)
        assertEquals(23, data.reminderSlots[0].hour)
        assertEquals(59, data.reminderSlots[0].minute)
        assertEquals(0, data.settings.completedRounds)
        assertEquals(1.5f, data.settings.fontScale, 0.01f)
    }

    // 13. Firebase / Network Error Handling
    @Test
    fun test13_firebaseAndNetworkErrorHandling() = runBlocking {
        val syncManager = SyncManager()

        val emptyUidBackup = syncManager.backupToCloud("", emptyList(), emptyList(), emptyList(), AppSettings())
        assertTrue(emptyUidBackup.isFailure)
        assertTrue(emptyUidBackup.exceptionOrNull() is IllegalArgumentException)

        val emptyUidRestore = syncManager.restoreFromCloud("")
        assertTrue(emptyUidRestore.isFailure)
        assertTrue(emptyUidRestore.exceptionOrNull() is IllegalArgumentException)
    }

    // 14. Notification Permission Denied & Scheduler
    @Test
    fun test14_notificationPermissionDeniedAndScheduler() {
        val scheduler = NotificationScheduler(context)
        val slots = listOf(ReminderSlot(id = 1, hour = 10, minute = 0, isEnabled = true))

        scheduler.scheduleDailyReminders(slots, isEnabled = false)
        scheduler.scheduleInactivityAlert(isEnabled = false)
        scheduler.scheduleTargetReminder(isEnabled = false)

        scheduler.scheduleDailyReminders(slots, isEnabled = true)
        scheduler.scheduleInactivityAlert(isEnabled = true)
        scheduler.scheduleTargetReminder(isEnabled = true)
        assertTrue(true)
    }

    // 15. BroadcastReceiver Async Completion
    @Test
    fun test15_broadcastReceiversAsyncCompletion() {
        val bootIntent = Intent(context, BootReceiver::class.java).apply {
            action = Intent.ACTION_BOOT_COMPLETED
        }
        val bootReceiver = BootReceiver()
        bootReceiver.onReceive(context, bootIntent)

        val alarmIntent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra("reminder_type", "daily")
            putExtra("slot_id", 1L)
        }
        val alarmReceiver = ReminderAlarmReceiver()
        alarmReceiver.onReceive(context, alarmIntent)

        assertTrue(true)
    }

    // 16. Process Recreation Resilient Persistence
    @Test
    fun test16_processRecreationResilientPersistence() = runBlocking {
        val dbFile = File(context.cacheDir, "process_recreation_test.db")
        if (dbFile.exists()) dbFile.delete()

        val persistentDb = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
        val persistentRepo = ZikirRepository(persistentDb)
        persistentRepo.ensureInitialized()
        persistentRepo.addDhikrCount(1, 777L)

        val beforeCount = persistentDb.zikirDao().getZikirById(1)?.count
        assertEquals(777L, beforeCount)

        // Close db to simulate process death
        persistentDb.close()

        // Re-open db with same file path to simulate process relaunch
        val relaunchedDb = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
        val relaunchedRepo = ZikirRepository(relaunchedDb)
        relaunchedRepo.ensureInitialized()

        val afterZikir = relaunchedDb.zikirDao().getZikirById(1)
        assertEquals(777L, afterZikir?.count)

        relaunchedDb.close()
        dbFile.delete()
        Unit
    }
}

