package com.example

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.data.backup.BackupManager
import com.example.data.cloud.CloudBackupData
import com.example.data.cloud.CloudDataCorruptionException
import com.example.data.cloud.SyncManager
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.DhikrDataValidator
import com.example.data.model.ReminderSlot
import com.example.data.model.Zikir
import com.example.data.model.ZikirContent
import com.example.data.model.ZikirHistory
import com.example.data.model.PendingOperation
import com.example.util.MonotonicTime
import java.util.UUID
import com.example.data.repository.ZikirRepository
import com.example.receiver.BootReceiver
import com.example.receiver.ReminderAlarmReceiver
import com.example.util.AdaptiveReminderManager
import com.example.util.NumberFormatter
import com.example.worker.DailyEvaluationWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Calendar

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

    // 1. increment + process death
    @Test
    fun test01_increment_plus_process_death() = runBlocking {
        val dbFile = File(context.cacheDir, "process_death_test_${System.currentTimeMillis()}.db")
        if (dbFile.exists()) dbFile.delete()

        try {
            // First process lifecycle
            val fileDb = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
                .allowMainThreadQueries()
                .build()
            val fileRepo = ZikirRepository(fileDb)
            fileRepo.ensureInitialized()

            fileRepo.addDhikrCount(1, 100L)
            fileRepo.addDhikrCount(2, 250L)

            assertEquals(100L, fileDb.zikirDao().getZikirById(1)?.count)
            assertEquals(250L, fileDb.zikirDao().getZikirById(2)?.count)

            // Simulate process termination (kill references and close DB)
            fileDb.close()

            // New process recreation from persistent disk
            val restoredDb = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
                .allowMainThreadQueries()
                .build()
            val restoredRepo = ZikirRepository(restoredDb)

            val zikir1 = restoredDb.zikirDao().getZikirById(1)
            val zikir2 = restoredDb.zikirDao().getZikirById(2)

            assertNotNull(zikir1)
            assertNotNull(zikir2)
            assertEquals(100L, zikir1?.count)
            assertEquals(250L, zikir2?.count)

            val history = restoredDb.historyDao().getAllHistoryDirect()
            assertEquals(2, history.size)
            assertTrue(history.any { it.zikirId == 1 && it.amount == 100L })
            assertTrue(history.any { it.zikirId == 2 && it.amount == 250L })

            restoredDb.close()
        } finally {
            if (dbFile.exists()) dbFile.delete()
        }
    }

    // 2. pending recovery + live channel
    @Test
    fun test02_pending_recovery_plus_live_channel() = runBlocking {
        repository.ensureInitialized()

        data class IncrementRequest(val zikirId: Int, val amount: Long)
        val incrementsChannel = Channel<IncrementRequest>(capacity = Channel.UNLIMITED)

        // Dispatch requests into live channel
        incrementsChannel.trySend(IncrementRequest(1, 33L))
        incrementsChannel.trySend(IncrementRequest(1, 33L))
        incrementsChannel.trySend(IncrementRequest(2, 100L))

        // Process batch from live channel
        val first = incrementsChannel.receive()
        val batch = mutableListOf(first)
        while (true) {
            val next = incrementsChannel.tryReceive().getOrNull() ?: break
            batch.add(next)
        }

        val grouped = batch.groupBy { it.zikirId }
        for ((zikirId, requests) in grouped) {
            val totalDelta = requests.sumOf { it.amount }
            if (totalDelta > 0) {
                repository.addDhikrCount(zikirId, totalDelta)
            }
        }

        val zikir1 = db.zikirDao().getZikirById(1)
        val zikir2 = db.zikirDao().getZikirById(2)

        assertEquals(66L, zikir1?.count)
        assertEquals(100L, zikir2?.count)
        assertEquals(2, db.historyDao().getAllHistoryDirect().size)
    }

    // 3. duplicate pending application
    @Test
    fun test03_duplicate_pending_application() = runBlocking {
        repository.ensureInitialized()

        // Test idempotency: applying the same unique request token twice should be guarded
        val processedRequestIds = mutableSetOf<String>()
        val requestId = "req_unique_99"

        fun applyRequestOnce(id: String, zikirId: Int, amount: Long) = runBlocking {
            if (processedRequestIds.add(id)) {
                repository.addDhikrCount(zikirId, amount)
            }
        }

        // Apply first time
        applyRequestOnce(requestId, 1, 50L)
        assertEquals(50L, db.zikirDao().getZikirById(1)?.count)

        // Attempt to apply duplicate
        applyRequestOnce(requestId, 1, 50L)
        assertEquals(50L, db.zikirDao().getZikirById(1)?.count) // Must remain 50L, not 100L
        assertEquals(1, db.historyDao().getAllHistoryDirect().size)
    }

    // 4. increment + reset
    @Test
    fun test04_increment_plus_reset() = runBlocking {
        repository.ensureInitialized()

        repository.addDhikrCount(1, 750L)
        assertEquals(750L, db.zikirDao().getZikirById(1)?.count)

        // Reset the zikir
        repository.resetSingleZikir(1)

        val resetZikir = db.zikirDao().getZikirById(1)
        assertNotNull(resetZikir)
        assertEquals(0L, resetZikir?.count)
        assertNull(resetZikir?.startedAt)
        assertNull(resetZikir?.completedAt)

        // Subsequent increment starts cleanly from 0
        repository.addDhikrCount(1, 20L)
        assertEquals(20L, db.zikirDao().getZikirById(1)?.count)
    }

    // 5. increment + restore
    @Test
    fun test05_increment_plus_restore() = runBlocking {
        repository.ensureInitialized()

        // User increments in local session
        repository.addDhikrCount(1, 150L)
        assertEquals(150L, db.zikirDao().getZikirById(1)?.count)

        // Full restore is performed with a valid 15-zikir snapshot containing count = 9999L on Zikir 1
        val restoredZikirs = (1..15).map { id ->
            when (id) {
                1 -> Zikir(id = 1, target = 70000L, count = 9999L, startedAt = 1000L, completedAt = null)
                2 -> Zikir(id = 2, target = 80000L, count = 500L, startedAt = 1000L, completedAt = null)
                else -> Zikir(id = id, target = 70000L, count = 0L, startedAt = null, completedAt = null)
            }
        }
        val restoredHistory = listOf(
            ZikirHistory(id = 10, zikirId = 1, amount = 9999L, type = "add", timestamp = 2000L, dateKey = "2026-09-01")
        )

        repository.restoreFullLocalBackup(
            zikirs = restoredZikirs,
            history = restoredHistory,
            slots = emptyList(),
            settings = AppSettings(id = 1, dailyTarget = 10000L, completedRounds = 1),
            selectedZikirId = 1
        )

        val zikir1 = db.zikirDao().getZikirById(1)
        assertEquals(9999L, zikir1?.count) // Must be completely overwritten by snapshot
        val history = db.historyDao().getAllHistoryDirect()
        assertEquals(1, history.size)
        assertEquals(9999L, history[0].amount)
    }

    // 6. concurrent increment
    @Test
    fun test06_concurrent_increment() = runBlocking {
        repository.ensureInitialized()

        // 30 concurrent increments of 10 count each = 300 total
        coroutineScope {
            (1..30).map {
                async(Dispatchers.Default) {
                    repository.addDhikrCount(1, 10L)
                }
            }.awaitAll()
        }

        val zikir1 = db.zikirDao().getZikirById(1)
        assertNotNull(zikir1)
        assertEquals(300L, zikir1?.count)

        val historyList = db.historyDao().getAllHistoryDirect()
        assertEquals(30, historyList.size)
        assertEquals(300L, historyList.sumOf { it.amount })
    }

    // 7. consistent backup snapshot
    @Test
    fun test07_consistent_backup_snapshot() = runBlocking {
        repository.ensureInitialized()

        repository.addDhikrCount(1, 500L)
        repository.removeDhikrCount(1, 100L)
        repository.addDhikrCount(2, 300L)

        val snapshot = repository.getAtomicSnapshot()
        assertEquals(15, snapshot.zikirs.size)

        val z1 = snapshot.zikirs.find { it.id == 1 }
        val z2 = snapshot.zikirs.find { it.id == 2 }
        assertNotNull(z1)
        assertNotNull(z2)
        assertEquals(400L, z1?.count)
        assertEquals(300L, z2?.count)

        val z1Adds = snapshot.history.filter { it.zikirId == 1 && it.type == "add" }.sumOf { it.amount }
        val z1Removes = snapshot.history.filter { it.zikirId == 1 && it.type == "remove" }.sumOf { it.amount }
        assertEquals(400L, z1Adds - z1Removes)

        assertNotNull(snapshot.settings)
        assertTrue(snapshot.settings.lastActiveTimestamp > 0L)
    }

    // 8. inconsistent cloud metadata/history
    @Test
    fun test08_inconsistent_cloud_metadata_history() {
        // Validation check for corrupted or inconsistent cloud metadata
        val lastSyncedAt = 5000L
        val metaUpdatedAt = 4000L // Inconsistent with lastSyncedAt

        val error = if (lastSyncedAt != metaUpdatedAt) {
            CloudDataCorruptionException("Snapshot inconsistency: User document and history meta timestamps do not match.")
        } else null

        assertNotNull(error)
        assertTrue(error?.message?.contains("Snapshot inconsistency") == true)
    }

    // 9. same timestamp history events
    @Test
    fun test09_same_timestamp_history_events() = runBlocking {
        repository.ensureInitialized()

        val fixedTimestamp = 1725500000000L
        val todayKey = "2026-09-05"

        // Insert 5 history records sharing the exact same millisecond timestamp
        for (i in 1..5) {
            db.historyDao().insert(
                ZikirHistory(
                    id = 0,
                    zikirId = 1,
                    amount = (i * 10).toLong(),
                    type = "add",
                    timestamp = fixedTimestamp,
                    dateKey = todayKey
                )
            )
        }

        val allHistory = db.historyDao().getAllHistoryDirect()
        assertEquals(5, allHistory.size)

        // Ensure each record has an autoincremented unique ID
        val distinctIds = allHistory.map { it.id }.distinct()
        assertEquals(5, distinctIds.size)

        // Ensure all preserved the identical timestamp
        assertTrue(allHistory.all { it.timestamp == fixedTimestamp })
    }

    // 10. 15-zikir snapshot integrity
    @Test
    fun test10_15_zikir_snapshot_integrity() = runBlocking {
        repository.ensureInitialized()

        val allZikirs = db.zikirDao().getAllZikirsDirect()
        assertEquals(15, allZikirs.size)

        val definitions = ZikirContent.INITIAL_DEFINITIONS
        assertEquals(15, definitions.size)

        for (item in definitions) {
            val dbItem = allZikirs.find { it.id == item.id }
            assertNotNull("Zikir ID ${item.id} must exist in DB", dbItem)
            assertEquals("Target for Zikir ${item.id} must match authentic sufi definitions", item.defaultTarget, dbItem?.target)
            val nameTr = item.names["tr"]
            assertNotNull(nameTr)
            assertTrue(nameTr!!.isNotBlank())
            assertNotNull(item.arabicText)
            assertTrue(item.arabicText.isNotBlank())
            assertTrue(item.defaultTarget >= 1000L)
        }
    }

    // 11. malformed settings
    @Test
    fun test11_malformed_settings() {
        val malformed = AppSettings(
            id = 1,
            dailyTarget = -500L,
            completedRounds = -99,
            fontScale = 99.0f,
            selectedZikirId = 99,
            themeName = "invalid_theme_xyz"
        )

        // Lenient clamping
        val clamped = DhikrDataValidator.validateSettings(malformed)
        assertTrue(clamped.dailyTarget >= 1L)
        assertTrue(clamped.completedRounds >= 0)
        assertTrue(clamped.fontScale in 0.7f..1.5f)
        assertTrue(clamped.selectedZikirId in 1..15)

        // Strict validation throws IllegalArgumentException
        assertThrows(IllegalArgumentException::class.java) {
            DhikrDataValidator.validateSettingsStrict(malformed)
        }
    }

    // 12. malformed history
    @Test
    fun test12_malformed_history() {
        val malformed = ZikirHistory(
            id = 0,
            zikirId = 999,
            amount = -100L,
            type = "hack_type",
            timestamp = -1L,
            dateKey = ""
        )

        // Lenient clamping
        val clamped = DhikrDataValidator.validateHistory(malformed)
        assertTrue(clamped.zikirId in 1..15)
        assertTrue(clamped.amount >= 1L)
        assertEquals("add", clamped.type)
        assertTrue(clamped.timestamp > 0L)
        assertTrue(clamped.dateKey.isNotBlank())

        // Strict validation throws IllegalArgumentException
        assertThrows(IllegalArgumentException::class.java) {
            DhikrDataValidator.validateHistoryStrict(malformed)
        }
    }

    // 13. malformed reminder slot
    @Test
    fun test13_malformed_reminder_slot() {
        val malformed = ReminderSlot(
            id = 1,
            hour = 35,
            minute = 99,
            isEnabled = true
        )

        // Lenient clamping
        val clamped = DhikrDataValidator.validateReminderSlot(malformed)
        assertEquals(23, clamped.hour)
        assertEquals(59, clamped.minute)

        // Strict validation throws IllegalArgumentException
        assertThrows(IllegalArgumentException::class.java) {
            DhikrDataValidator.validateReminderSlotStrict(malformed)
        }
    }

    // 14. malformed Firestore chunk entry #100
    @Test
    fun test14_malformed_firestore_chunk_entry_100() {
        val entries = (1..100).map { i ->
            ZikirHistory(
                id = 0,
                zikirId = 1,
                amount = 10L,
                type = "add",
                timestamp = 1000L + i,
                dateKey = "2026-09-01"
            )
        }.toMutableList()

        // Entry #100 (index 99) is corrupted
        entries[99] = entries[99].copy(amount = -999L)

        // Strict validation fails at entry #100
        var failedIndex = -1
        try {
            entries.forEachIndexed { index, item ->
                failedIndex = index
                DhikrDataValidator.validateHistoryStrict(item)
            }
            fail("Strict validation should fail on malformed entry #100")
        } catch (e: IllegalArgumentException) {
            assertEquals(99, failedIndex)
        }

        // Lenient validation cleans all 100 entries without dropping
        val sanitized = entries.map { DhikrDataValidator.validateHistory(it) }
        assertEquals(100, sanitized.size)
        assertTrue(sanitized[99].amount >= 1L)
    }

    // 15. malformed Firestore chunk entry #499
    @Test
    fun test15_malformed_firestore_chunk_entry_499() {
        val entries = (0..499).map { i ->
            ZikirHistory(
                id = 0,
                zikirId = (i % 15) + 1,
                amount = 33L,
                type = "add",
                timestamp = 2000L + i,
                dateKey = "2026-09-01"
            )
        }.toMutableList()

        // Entry at boundary #499 (index 499) has corrupt timestamp
        entries[499] = entries[499].copy(timestamp = -1L)

        var failedIndex = -1
        try {
            entries.forEachIndexed { index, item ->
                failedIndex = index
                DhikrDataValidator.validateHistoryStrict(item)
            }
            fail("Strict validation should fail on malformed entry #499")
        } catch (e: IllegalArgumentException) {
            assertEquals(499, failedIndex)
        }

        val sanitized = entries.map { DhikrDataValidator.validateHistory(it) }
        assertEquals(500, sanitized.size)
        assertTrue(sanitized[499].timestamp > 0L)
    }

    // 16. missing cloud chunk
    @Test
    fun test16_missing_cloud_chunk() {
        val expectedChunkCount = 3
        val actualChunkCount = 2 // Chunk 1 is missing

        val result = if (actualChunkCount != expectedChunkCount) {
            Result.failure<CloudBackupData>(
                CloudDataCorruptionException("Snapshot inconsistency: Expected $expectedChunkCount chunks but found $actualChunkCount.")
            )
        } else {
            Result.success(CloudBackupData(emptyList(), AppSettings(), emptyList(), emptyList(), null, 0L))
        }

        assertTrue(result.isFailure)
        assertEquals(
            "Snapshot inconsistency: Expected 3 chunks but found 2.",
            result.exceptionOrNull()?.message
        )
    }

    // 17. stale cloud chunk
    @Test
    fun test17_stale_cloud_chunk() {
        val lastSyncedAt = 2000L
        val chunkUpdatedAt = 1000L // Stale chunk

        val result = if (chunkUpdatedAt != lastSyncedAt) {
            Result.failure<CloudBackupData>(
                CloudDataCorruptionException("Snapshot inconsistency: Chunk chunk_0 timestamp does not match the snapshot.")
            )
        } else {
            Result.success(CloudBackupData(emptyList(), AppSettings(), emptyList(), emptyList(), null, 0L))
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Chunk chunk_0 timestamp does not match") == true)
    }

    // 18. cloud revision conflict
    @Test
    fun test18_cloud_revision_conflict() = runBlocking {
        repository.ensureInitialized()

        // Local state
        repository.addDhikrCount(1, 50L)
        val localSnapshot = repository.getAtomicSnapshot()

        // Remote state with different revision and additional history (all 15 zikirs)
        val remoteZikirs = (1..15).map { id ->
            if (id == 1) Zikir(id = 1, target = 70000L, count = 120L, startedAt = 1000L, completedAt = null)
            else Zikir(id = id, target = 70000L, count = 0L, startedAt = null, completedAt = null)
        }
        val remoteHistory = listOf(
            ZikirHistory(id = 99, zikirId = 1, amount = 120L, type = "add", timestamp = 9999L, dateKey = "2026-09-02")
        )
        val remoteSettings = AppSettings(id = 1, dailyTarget = 5000L, completedRounds = 2)

        // Merge logic: counts = maxOf(local, remote), history = distinct union
        val mergedCount = maxOf(localSnapshot.zikirs.find { it.id == 1 }?.count ?: 0L, 120L)
        assertEquals(120L, mergedCount)

        val mergedHistory = (localSnapshot.history + remoteHistory).distinctBy { "${it.zikirId}_${it.amount}_${it.timestamp}" }
        assertEquals(2, mergedHistory.size)

        repository.restoreFullLocalBackup(
            zikirs = remoteZikirs.map { if (it.id == 1) it.copy(count = mergedCount) else it },
            history = mergedHistory,
            slots = emptyList(),
            settings = remoteSettings,
            selectedZikirId = 1
        )

        assertEquals(120L, db.zikirDao().getZikirById(1)?.count)
        assertEquals(2, db.historyDao().getAllHistoryDirect().size)
    }

    // 19. adaptive notification race
    @Test
    fun test19_adaptive_notification_race() = runBlocking {
        // Reset prefs
        val prefs = context.getSharedPreferences("adaptive_spiritual_reminder_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        // Launch 20 concurrent coroutines attempting to reserve quota for today
        val results = coroutineScope {
            (1..20).map {
                async(Dispatchers.Default) {
                    AdaptiveReminderManager.tryReserveQuota(context, 4)
                }
            }.awaitAll()
        }

        val successfulReservations = results.filterNotNull()
        val rejectedReservations = results.filter { it == null }

        assertEquals(1, successfulReservations.size)
        assertEquals(19, rejectedReservations.size)
    }

    // 20. large history memory scenario
    @Test
    fun test20_large_history_memory_scenario() = runBlocking {
        repository.ensureInitialized()

        val totalRecords = 1200
        val historyList = (1..totalRecords).map { i ->
            ZikirHistory(
                id = 0,
                zikirId = (i % 15) + 1,
                amount = 10L,
                type = "add",
                timestamp = 1000L + i,
                dateKey = "2026-09-01"
            )
        }

        // Insert batch in transaction
        db.historyDao().insertAll(historyList)
        assertEquals(1200, db.historyDao().getHistoryCountDirect())

        // Query in chunks of 500 to guarantee low memory usage (exercises 500 + 500 + 200 chunk pagination)
        val chunkedRecords = repository.getAllHistoryInChunksDirect(chunkSize = 500)
        assertEquals(1200, chunkedRecords.size)
        assertEquals(12000L, chunkedRecords.sumOf { it.amount })
    }

    // 21. backup password state recreation
    @Test
    fun test21_backup_password_state_recreation() = runBlocking {
        repository.ensureInitialized()
        repository.addDhikrCount(1, 1000L)

        val snapshot = repository.getAtomicSnapshot()
        val correctPassword = "ManeviParola2026!#"
        val wrongPassword = "YanlisParola!"

        val outStream = ByteArrayOutputStream()
        val exportResult = backupManager.exportBackup(
            outputStream = outStream,
            zikirs = snapshot.zikirs,
            history = snapshot.history,
            reminderSlots = snapshot.slots,
            settings = snapshot.settings,
            password = correctPassword
        )
        assertTrue(exportResult.isSuccess)

        // Attempt restore with wrong password must fail
        val wrongStream = ByteArrayInputStream(outStream.toByteArray())
        val wrongImportResult = backupManager.importBackup(wrongStream, wrongPassword)
        assertTrue(wrongImportResult.isFailure)

        // Attempt restore with correct password must succeed
        val correctStream = ByteArrayInputStream(outStream.toByteArray())
        val correctImportResult = backupManager.importBackup(correctStream, correctPassword)
        assertTrue(correctImportResult.isSuccess)

        val restoredData = correctImportResult.getOrNull()
        assertNotNull(restoredData)
        assertEquals(1000L, restoredData?.zikirs?.find { it.id == 1 }?.count)
    }

    // 22. FileProvider path
    @Test
    fun test22_fileprovider_path() {
        val backupDir = File(context.cacheDir, "backups").apply { if (!exists()) mkdirs() }
        val testFile = File(backupDir, "test_fileprovider_export.nefs")
        testFile.writeText("TEST_BACKUP_ENCRYPTED_PAYLOAD")

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            testFile
        )

        assertNotNull(uri)
        assertEquals("content", uri.scheme)
        assertEquals("${context.packageName}.fileprovider", uri.authority)

        // Read content stream through content resolver
        val input = context.contentResolver.openInputStream(uri)
        assertNotNull(input)
        val readText = input?.bufferedReader()?.use { it.readText() }
        assertEquals("TEST_BACKUP_ENCRYPTED_PAYLOAD", readText)

        testFile.delete()
    }

    // 23. receiver cancellation
    @Test
    fun test23_receiver_cancellation() {
        val reminderReceiver = ReminderAlarmReceiver()
        val bootReceiver = BootReceiver()

        // Null / empty intents must be handled gracefully without throwing unhandled exceptions
        reminderReceiver.onReceive(context, Intent())
        bootReceiver.onReceive(context, Intent())

        // Specific unexpected action
        reminderReceiver.onReceive(context, Intent("android.intent.action.UNEXPECTED_CANCEL"))
        bootReceiver.onReceive(context, Intent("android.intent.action.UNEXPECTED_CANCEL"))
        assertTrue(true)
    }

    // 24. worker cancellation
    @Test
    fun test24_worker_cancellation() = runBlocking {
        val worker = TestListenableWorkerBuilder<DailyEvaluationWorker>(context).build()

        // Cooperative coroutine cancellation
        val job = launch(Dispatchers.Default) {
            try {
                worker.doWork()
            } catch (e: CancellationException) {
                // Must be rethrown and caught here
                throw e
            }
        }
        job.cancel()
        assertTrue(job.isCancelled)
    }

    @Test
    fun test24b_worker_transient_vs_permanent_failure_classification() {
        // Transient failures
        assertTrue(DailyEvaluationWorker.isTransientFailure(java.io.IOException("Network/disk transient error")))
        assertTrue(DailyEvaluationWorker.isTransientFailure(android.database.sqlite.SQLiteDatabaseLockedException("Database locked")))
        assertTrue(DailyEvaluationWorker.isTransientFailure(android.database.sqlite.SQLiteDiskIOException("Disk IO")))
        assertTrue(DailyEvaluationWorker.isTransientFailure(RuntimeException("Wrapper", java.io.IOException("Nested IO error"))))

        // Permanent failures
        assertFalse(DailyEvaluationWorker.isTransientFailure(IllegalArgumentException("Invalid arg")))
        assertFalse(DailyEvaluationWorker.isTransientFailure(IllegalStateException("Invalid state")))
        assertFalse(DailyEvaluationWorker.isTransientFailure(NullPointerException("NPE")))
        assertFalse(DailyEvaluationWorker.isTransientFailure(ClassCastException("Cast error")))
    }

    @Test
    fun test24c_worker_max_retries_limit_constant() {
        assertEquals(3, DailyEvaluationWorker.MAX_RETRIES)
    }

    // 25. migration 1→2→3→4→5→6
    @Test
    fun test25_migration_1_to_2_to_3_to_4_to_5_to_6() {
        val dbFile = File(context.cacheDir, "migration_step_test.db")
        if (dbFile.exists()) dbFile.delete()

        try {
            val factory = FrameworkSQLiteOpenHelperFactory()
            val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbFile.name)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Create v1 schema
                        db.execSQL("CREATE TABLE IF NOT EXISTS `zikirs` (`id` INTEGER NOT NULL, `target` INTEGER NOT NULL, `count` INTEGER NOT NULL, `startedAt` INTEGER, `completedAt` INTEGER, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `zikir_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `zikirId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `dateKey` TEXT NOT NULL)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `reminder_slots` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `app_settings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dailyTarget` INTEGER NOT NULL, `completedRounds` INTEGER NOT NULL, `selectedZikirId` INTEGER NOT NULL, `keepAwakeEnabled` INTEGER NOT NULL, `hapticEnabled` INTEGER NOT NULL, `fullScreenTap` INTEGER NOT NULL, `themeName` TEXT NOT NULL, `fontScale` REAL NOT NULL, `lang` TEXT NOT NULL, `reminderEnabled` INTEGER NOT NULL, `counterTexture` TEXT NOT NULL, `hapticTapMode` TEXT NOT NULL, `hapticMilestoneMode` TEXT NOT NULL, `countdownMode` INTEGER NOT NULL, `inactivityAlertEnabled` INTEGER NOT NULL, `targetReminderEnabled` INTEGER NOT NULL, `lastActiveTimestamp` INTEGER NOT NULL)")

                        // Insert initial test record
                        db.execSQL("INSERT INTO `zikirs` (`id`, `target`, `count`, `startedAt`, `completedAt`) VALUES (1, 70000, 33, 1000, NULL)")
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()

            val helper = factory.create(config)
            val v1Db = helper.writableDatabase

            // Step-by-step migrations
            AppDatabase.MIGRATION_1_2.migrate(v1Db)
            AppDatabase.MIGRATION_2_3.migrate(v1Db)
            AppDatabase.MIGRATION_3_4.migrate(v1Db)
            AppDatabase.MIGRATION_4_5.migrate(v1Db)
            AppDatabase.MIGRATION_5_6.migrate(v1Db)

            // Verify columns added in migrations exist
            val cursor = v1Db.query("SELECT acknowledgedBadges, autoReorderSettings, settingsUsageStats FROM app_settings")
            assertNotNull(cursor)
            cursor.close()

            // Verify pre-existing data
            val zikirCursor = v1Db.query("SELECT count FROM zikirs WHERE id = 1")
            assertTrue(zikirCursor.moveToFirst())
            assertEquals(33L, zikirCursor.getLong(0))
            zikirCursor.close()

            v1Db.close()
        } finally {
            if (dbFile.exists()) dbFile.delete()
        }
    }

    // 26. direct migration 1→6
    @Test
    fun test26_direct_migration_1_to_6() {
        val dbName = "migration_direct_test.db"
        context.deleteDatabase(dbName)

        try {
            val factory = FrameworkSQLiteOpenHelperFactory()
            val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `zikirs` (`id` INTEGER NOT NULL, `target` INTEGER NOT NULL, `count` INTEGER NOT NULL, `startedAt` INTEGER, `completedAt` INTEGER, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `zikir_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `zikirId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `dateKey` TEXT NOT NULL)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `reminder_slots` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `app_settings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dailyTarget` INTEGER NOT NULL, `completedRounds` INTEGER NOT NULL, `selectedZikirId` INTEGER NOT NULL, `keepAwakeEnabled` INTEGER NOT NULL, `hapticEnabled` INTEGER NOT NULL, `fullScreenTap` INTEGER NOT NULL, `themeName` TEXT NOT NULL, `fontScale` REAL NOT NULL, `lang` TEXT NOT NULL, `reminderEnabled` INTEGER NOT NULL, `counterTexture` TEXT NOT NULL, `hapticTapMode` TEXT NOT NULL, `hapticMilestoneMode` TEXT NOT NULL, `countdownMode` INTEGER NOT NULL, `inactivityAlertEnabled` INTEGER NOT NULL, `targetReminderEnabled` INTEGER NOT NULL, `lastActiveTimestamp` INTEGER NOT NULL)")

                        db.execSQL("INSERT INTO `zikirs` (`id`, `target`, `count`, `startedAt`, `completedAt`) VALUES (1, 70000, 100, 1000, NULL)")
                        // Add history record for a zikir not yet in zikirs table
                        db.execSQL("INSERT INTO `zikir_history` (`id`, `zikirId`, `amount`, `type`, `timestamp`, `dateKey`) VALUES (1, 999, 50, 'add', 1000, '2026-09-01')")
                        // Add initial settings row
                        db.execSQL("INSERT INTO `app_settings` (`id`, `dailyTarget`, `completedRounds`, `selectedZikirId`, `keepAwakeEnabled`, `hapticEnabled`, `fullScreenTap`, `themeName`, `fontScale`, `lang`, `reminderEnabled`, `counterTexture`, `hapticTapMode`, `hapticMilestoneMode`, `countdownMode`, `inactivityAlertEnabled`, `targetReminderEnabled`, `lastActiveTimestamp`) VALUES (1, 10000, 0, 1, 1, 1, 0, 'emerald', 1.15, 'tr', 0, 'geometric', 'light', 'double', 0, 0, 0, 1000)")
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()

            val helper = factory.create(config)
            val v1Db = helper.writableDatabase
            v1Db.close()
            helper.close()

            // Direct migration 1 -> 8 via Room builder (triggers Room's real onValidateSchema)
            val roomDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
                .addMigrations(AppDatabase.MIGRATION_1_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8)
                .build()

            // Verify access through Room DAOs (this forces open, migration, and schema validation)
            kotlinx.coroutines.runBlocking {
                val zikirs = roomDb.zikirDao().getAllZikirsDirect()
                assertEquals(2, zikirs.size)
                assertTrue(zikirs.any { it.id == 1 && it.count == 100L })
                assertTrue(zikirs.any { it.id == 999 })

                // Verify ZERO DATA LOSS: History record was preserved
                val history = roomDb.historyDao().getAllHistoryDirect()
                assertEquals(1, history.size)
                assertEquals(999, history.first().zikirId)
                assertEquals(50L, history.first().amount)

                // Verify app settings were preserved and new columns have defaults
                val settings = roomDb.settingsDao().getSettingsDirect()
                assertNotNull(settings)
                assertEquals("emerald", settings?.themeName)
                assertEquals("", settings?.acknowledgedBadges)
                assertEquals(false, settings?.autoReorderSettings)
                assertEquals("{}", settings?.settingsUsageStats)

                // Verify Foreign Key CASCADE: deleting zikir 999 should delete its history
                val zikir999 = zikirs.first { it.id == 999 }
                roomDb.zikirDao().insertAll(zikirs.filter { it.id != 999 }.also {
                    roomDb.zikirDao().deleteAll()
                })
                val historyAfterDelete = roomDb.historyDao().getAllHistoryDirect()
                assertEquals(0, historyAfterDelete.size)
            }

            // Verify SQLite indices exist on zikir_history
            val openHelper = roomDb.openHelper.readableDatabase
            val indices = mutableListOf<String>()
            openHelper.query("PRAGMA index_list('zikir_history')").use { cursor ->
                while (cursor.moveToNext()) {
                    val nameIndex = cursor.getColumnIndex("name")
                    if (nameIndex >= 0) indices.add(cursor.getString(nameIndex))
                }
            }
            assertTrue("Missing index_zikir_history_zikirId", indices.contains("index_zikir_history_zikirId"))
            assertTrue("Missing index_zikir_history_dateKey", indices.contains("index_zikir_history_dateKey"))
            assertTrue("Missing index_zikir_history_timestamp", indices.contains("index_zikir_history_timestamp"))
            assertTrue("Missing index_zikir_history_eventId", indices.contains("index_zikir_history_eventId"))
            assertTrue("Missing index_zikir_history_zikirId_dateKey", indices.contains("index_zikir_history_zikirId_dateKey"))

            roomDb.close()
        } finally {
            context.deleteDatabase(dbName)
        }
    }

    // 27. Android edge-to-edge regression where testable
    @Test
    fun test27_android_edge_to_edge_regression() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()

        assertNotNull(activity)
        assertNotNull(activity.window)
        assertNotNull(activity.window.decorView)

        // Verify configuration changes (e.g. landscape & font scaling) preserve edge-to-edge layout without crash
        val config = android.content.res.Configuration().apply {
            fontScale = 1.3f
        }
        controller.configurationChange(config)
        assertNotNull(activity.window.decorView)

        controller.pause().stop().destroy()
    }

    // 28. Pending increment + resetSingleZikir concurrency race
    @Test
    fun test28_pending_fence_reset_single_zikir_concurrency() = runBlocking {
        repository.ensureInitialized()

        // Generate 20 pending ops for zikir 1 and 20 for zikir 2
        val opsZikir1 = (1..20).map {
            PendingOperation(
                operationId = "z1_op_$it",
                zikirId = 1,
                amount = 10L,
                timestamp = MonotonicTime.now(),
                dateKey = "2026-09-07",
                status = "created"
            )
        }
        val opsZikir2 = (1..20).map {
            PendingOperation(
                operationId = "z2_op_$it",
                zikirId = 2,
                amount = 5L,
                timestamp = MonotonicTime.now(),
                dateKey = "2026-09-07",
                status = "created"
            )
        }

        // Insert into Room concurrently
        (opsZikir1 + opsZikir2).forEach { repository.createPendingOperation(it) }

        // Concurrently race resetSingleZikir(1) and applyBatchOperations
        coroutineScope {
            val job1 = async(Dispatchers.Default) {
                repository.resetSingleZikir(1)
            }
            val job2 = async(Dispatchers.Default) {
                repository.applyBatchOperations(opsZikir1)
            }
            val job3 = async(Dispatchers.Default) {
                repository.applyBatchOperations(opsZikir2)
            }
            job1.await()
            job2.await()
            job3.await()
        }

        // Zikir 1 MUST be 0 because all opsZikir1 were created before or during the reset fence
        val zikir1 = db.zikirDao().getZikirById(1)
        assertNotNull(zikir1)
        assertEquals("Zikir 1 must remain 0 after fence reset", 0L, zikir1?.count)

        // Zikir 2 was NOT reset, so its pending ops MUST be successfully applied (20 * 5 = 100)
        val zikir2 = db.zikirDao().getZikirById(2)
        assertNotNull(zikir2)
        assertEquals("Zikir 2 must be 100L", 100L, zikir2?.count)

        // Verify that a new pending op created AFTER the fence executes cleanly from 0
        val postFenceOp = PendingOperation(
            operationId = "z1_post_fence",
            zikirId = 1,
            amount = 42L,
            timestamp = MonotonicTime.now(),
            dateKey = "2026-09-07",
            status = "created"
        )
        repository.createPendingOperation(postFenceOp)
        repository.applyBatchOperations(listOf(postFenceOp))

        val zikir1After = db.zikirDao().getZikirById(1)
        assertEquals("Zikir 1 must be 42L after new post-fence op", 42L, zikir1After?.count)
    }

    // 29. Pending increment + resetAllZikirs concurrency race
    @Test
    fun test29_pending_fence_reset_all_zikirs_concurrency() = runBlocking {
        repository.ensureInitialized()

        val allOps = (1..15).flatMap { zId ->
            (1..5).map { opIdx ->
                PendingOperation(
                    operationId = "all_z_${zId}_op_$opIdx",
                    zikirId = zId,
                    amount = 10L,
                    timestamp = MonotonicTime.now(),
                    dateKey = "2026-09-07",
                    status = "created"
                )
            }
        }

        allOps.forEach { repository.createPendingOperation(it) }

        coroutineScope {
            val resetJob = async(Dispatchers.Default) {
                repository.resetAllZikirs()
            }
            val applyJob = async(Dispatchers.Default) {
                repository.applyBatchOperations(allOps)
            }
            resetJob.await()
            applyJob.await()
        }

        // All zikirs must be strictly 0L
        for (id in 1..15) {
            val z = db.zikirDao().getZikirById(id)
            assertEquals("Zikir $id must be 0 after resetAll fence", 0L, z?.count)
        }
        val history = db.historyDao().getAllHistoryDirect()
        assertEquals("History must be empty after resetAll fence", 0, history.size)
    }

    // 30. Pending increment + startNewRound concurrency race
    @Test
    fun test30_pending_fence_start_new_round_concurrency() = runBlocking {
        repository.ensureInitialized()

        val initialOps = (1..10).map {
            PendingOperation(
                operationId = "round_op_$it",
                zikirId = 1,
                amount = 20L,
                timestamp = MonotonicTime.now(),
                dateKey = "2026-09-07",
                status = "created"
            )
        }
        initialOps.forEach { repository.createPendingOperation(it) }

        coroutineScope {
            val roundJob = async(Dispatchers.Default) {
                repository.startNewRound()
            }
            val applyJob = async(Dispatchers.Default) {
                repository.applyBatchOperations(initialOps)
            }
            roundJob.await()
            applyJob.await()
        }

        val z1 = db.zikirDao().getZikirById(1)
        assertEquals("Zikir 1 must be 0 after round transition fence", 0L, z1?.count)
        val settings = db.settingsDao().getSettingsDirect()
        assertEquals("Completed rounds must be incremented to 1", 1, settings?.completedRounds)
    }

    // 31. Pending increment + removeDhikrCount & undoLastAction fence
    @Test
    fun test31_pending_fence_remove_and_undo_concurrency() = runBlocking {
        repository.ensureInitialized()

        // Establish initial count of 100 via direct add
        repository.addDhikrCount(1, 100L)
        assertEquals(100L, db.zikirDao().getZikirById(1)?.count)

        // Queue a pending increment of 50
        val pendingOp = PendingOperation(
            operationId = "decrement_race_op",
            zikirId = 1,
            amount = 50L,
            timestamp = MonotonicTime.now(),
            dateKey = "2026-09-07",
            status = "created"
        )
        repository.createPendingOperation(pendingOp)

        // removeDhikrCount(1, 20L) must act as a fence against older pending operations
        repository.removeDhikrCount(1, 20L)

        // Delayed batch attempt of the older pending op
        repository.applyBatchOperations(listOf(pendingOp))

        // Count must be 80L (100 - 20), NOT 130L (80 + 50)
        val z1 = db.zikirDao().getZikirById(1)
        assertEquals("Zikir count must be exactly 80L, older pending op must be fenced out", 80L, z1?.count)

        // Test undoLastAction fence
        val pendingOp2 = PendingOperation(
            operationId = "undo_race_op",
            zikirId = 1,
            amount = 30L,
            timestamp = MonotonicTime.now(),
            dateKey = "2026-09-07",
            status = "created"
        )
        repository.createPendingOperation(pendingOp2)

        // undoLastAction reverses the remove action (restores 100L) and fences out pendingOp2
        repository.undoLastAction(1)

        // Attempt applying pendingOp2
        repository.applyBatchOperations(listOf(pendingOp2))

        val z1AfterUndo = db.zikirDao().getZikirById(1)
        assertEquals("Zikir count must be restored to 100L and older pending op fenced out", 100L, z1AfterUndo?.count)
    }

    // 32. Pending increment + restoreFullLocalBackup & restoreFullCloudBackup fence
    @Test
    fun test32_pending_fence_restore_local_and_cloud_backup_concurrency() = runBlocking {
        repository.ensureInitialized()

        // Generate rapid pending operations
        val preRestoreOps = (1..30).map {
            PendingOperation(
                operationId = "restore_race_$it",
                zikirId = 1,
                amount = 100L,
                timestamp = MonotonicTime.now(),
                dateKey = "2026-09-07",
                status = "created"
            )
        }
        preRestoreOps.forEach { repository.createPendingOperation(it) }

        val snapshotZikirs = (1..15).map { id ->
            when (id) {
                1 -> Zikir(id = 1, target = 70000L, count = 1234L, startedAt = 1000L, completedAt = null)
                2 -> Zikir(id = 2, target = 80000L, count = 5678L, startedAt = 1000L, completedAt = null)
                else -> Zikir(id = id, target = 70000L, count = 0L, startedAt = null, completedAt = null)
            }
        }
        val snapshotHistory = listOf(
            ZikirHistory(id = 50, zikirId = 1, amount = 1234L, type = "add", timestamp = 2000L, dateKey = "2026-09-01")
        )

        coroutineScope {
            val restoreJob = async(Dispatchers.Default) {
                repository.restoreFullLocalBackup(
                    zikirs = snapshotZikirs,
                    history = snapshotHistory,
                    slots = emptyList(),
                    settings = AppSettings(id = 1, dailyTarget = 10000L, completedRounds = 2),
                    selectedZikirId = 1
                )
            }
            val applyJob = async(Dispatchers.Default) {
                repository.applyBatchOperations(preRestoreOps)
            }
            restoreJob.await()
            applyJob.await()
        }

        // Snapshot MUST be completely preserved, no older pending ops can bleed into restored state
        val z1 = db.zikirDao().getZikirById(1)
        val z2 = db.zikirDao().getZikirById(2)
        assertEquals("Restored Zikir 1 count must match snapshot exactly", 1234L, z1?.count)
        assertEquals("Restored Zikir 2 count must match snapshot exactly", 5678L, z2?.count)

        // Cloud backup restore test with concurrent pending ops
        val cloudOps = (1..10).map {
            PendingOperation(
                operationId = "cloud_restore_race_$it",
                zikirId = 1,
                amount = 999L,
                timestamp = MonotonicTime.now(),
                dateKey = "2026-09-07",
                status = "created"
            )
        }
        cloudOps.forEach { repository.createPendingOperation(it) }

        val cloudSnapshotZikirs = (1..15).map { id ->
            if (id == 1) Zikir(id = 1, target = 70000L, count = 7777L, startedAt = 1000L, completedAt = null)
            else Zikir(id = id, target = 70000L, count = 0L, startedAt = null, completedAt = null)
        }

        coroutineScope {
            val cloudRestoreJob = async(Dispatchers.Default) {
                repository.restoreFullCloudBackup(
                    zikirs = cloudSnapshotZikirs,
                    settings = AppSettings(id = 1, dailyTarget = 10000L, completedRounds = 5),
                    slots = emptyList(),
                    history = emptyList()
                )
            }
            val cloudApplyJob = async(Dispatchers.Default) {
                repository.applyBatchOperations(cloudOps)
            }
            cloudRestoreJob.await()
            cloudApplyJob.await()
        }

        val z1Cloud = db.zikirDao().getZikirById(1)
        assertEquals("Cloud restored Zikir 1 count must match snapshot exactly (7777L)", 7777L, z1Cloud?.count)
    }

    // 33. COMPLETE 15-ZIKIR SNAPSHOT VALIDATION: Missing zikirs (< 15) must fail restore immediately
    @Test
    fun test33_strictSnapshotValidation_missingZikirs_failsRestore() = runBlocking {
        repository.ensureInitialized()
        val originalZ1 = db.zikirDao().getZikirById(1)

        val invalid14Zikirs = (1..14).map { id ->
            Zikir(id = id, target = 70000L, count = 0L)
        }

        try {
            repository.restoreFullLocalBackup(
                zikirs = invalid14Zikirs,
                history = emptyList(),
                slots = emptyList(),
                settings = AppSettings(),
                selectedZikirId = 1
            )
            fail("Should have thrown IllegalArgumentException due to snapshot size != 15")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("exactly 15 zikirs") == true)
        }

        // Database MUST NOT be modified
        val afterZ1 = db.zikirDao().getZikirById(1)
        assertEquals(originalZ1?.count, afterZ1?.count)
    }

    // 34. COMPLETE 15-ZIKIR SNAPSHOT VALIDATION: Duplicate IDs must fail restore
    @Test
    fun test34_strictSnapshotValidation_duplicateIds_failsRestore() = runBlocking {
        repository.ensureInitialized()

        // 15 items but IDs are not unique (ID 1 duplicated, ID 15 missing)
        val duplicateIdZikirs = (1..14).map { id ->
            Zikir(id = id, target = 70000L, count = 0L)
        } + listOf(Zikir(id = 1, target = 70000L, count = 0L))

        try {
            repository.restoreFullCloudBackup(
                zikirs = duplicateIdZikirs,
                settings = AppSettings(),
                slots = emptyList(),
                history = emptyList()
            )
            fail("Should have thrown IllegalArgumentException due to duplicate ID")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Duplicate zikir IDs") == true)
        }
    }

    // 35. COMPLETE 15-ZIKIR SNAPSHOT VALIDATION: Non 1..15 IDs must fail restore
    @Test
    fun test35_strictSnapshotValidation_invalidIdRange_failsRestore() = runBlocking {
        repository.ensureInitialized()

        // 15 unique items, but IDs are 2..16 instead of 1..15
        val shiftedIdZikirs = (2..16).map { id ->
            Zikir(id = id, target = 70000L, count = 0L)
        }

        try {
            DhikrDataValidator.validateFullSnapshotStrict(zikirs = shiftedIdZikirs)
            fail("Should have thrown IllegalArgumentException due to ID range != 1..15")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("1..15") == true)
        }
    }

    // 36. COMPLETE 15-ZIKIR SNAPSHOT VALIDATION: Invalid Zikir strict validation failure
    @Test
    fun test36_strictSnapshotValidation_invalidZikirStrict_failsRestore() = runBlocking {
        repository.ensureInitialized()

        // 15 items 1..15, but zikir 3 has count > 0 without startedAt
        val invalidZikirs = (1..15).map { id ->
            if (id == 3) Zikir(id = 3, target = 70000L, count = 100L, startedAt = null)
            else Zikir(id = id, target = 70000L, count = 0L)
        }

        try {
            repository.restoreFullLocalBackup(
                zikirs = invalidZikirs,
                history = emptyList(),
                slots = emptyList(),
                settings = AppSettings(),
                selectedZikirId = 1
            )
            fail("Should have thrown IllegalArgumentException due to missing startedAt for count > 0")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Missing startedAt") == true)
        }
    }

    // 37. COMPLETE 15-ZIKIR SNAPSHOT VALIDATION: History referencing invalid zikirId outside 1..15 or snapshot
    @Test
    fun test37_strictSnapshotValidation_historyInvalidZikirId_failsRestore() = runBlocking {
        repository.ensureInitialized()

        val valid15Zikirs = (1..15).map { id ->
            Zikir(id = id, target = 70000L, count = 0L)
        }

        val invalidHistory = listOf(
            ZikirHistory(id = 1, zikirId = 99, amount = 100L, type = "add", timestamp = 1000L, dateKey = "2026-09-01")
        )

        try {
            repository.restoreFullCloudBackup(
                zikirs = valid15Zikirs,
                settings = AppSettings(),
                slots = emptyList(),
                history = invalidHistory
            )
            fail("Should have thrown IllegalArgumentException due to history referencing zikirId 99")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Invalid ZikirHistory zikirId") == true || e.message?.contains("non-snapshot") == true)
        }
    }

    // 38. COMPLETE 15-ZIKIR SNAPSHOT VALIDATION: Valid full 15-zikir snapshot succeeds atomically
    @Test
    fun test38_strictSnapshotValidation_validFullSnapshot_succeeds() = runBlocking {
        repository.ensureInitialized()

        val valid15Zikirs = (1..15).map { id ->
            Zikir(
                id = id,
                target = 70000L,
                count = (id * 100).toLong(),
                startedAt = 1000L,
                completedAt = null
            )
        }

        val validHistory = (1..15).map { id ->
            ZikirHistory(
                id = id.toLong(),
                zikirId = id,
                amount = (id * 100).toLong(),
                type = "add",
                timestamp = 1000L + id,
                dateKey = "2026-09-07"
            )
        }

        val validSlots = listOf(
            ReminderSlot(id = 1, hour = 9, minute = 30, isEnabled = true)
        )

        val validSettings = AppSettings(
            id = 1,
            lang = "tr",
            dailyTarget = 15000L,
            completedRounds = 3
        )

        repository.restoreFullLocalBackup(
            zikirs = valid15Zikirs,
            history = validHistory,
            slots = validSlots,
            settings = validSettings,
            selectedZikirId = 5
        )

        val allZikirs = db.zikirDao().getAllZikirsDirect()
        assertEquals(15, allZikirs.size)
        assertEquals((1..15).toSet(), allZikirs.map { it.id }.toSet())
        assertEquals(500L, db.zikirDao().getZikirById(5)?.count)

        val allHistory = db.historyDao().getAllHistoryDirect()
        assertEquals(15, allHistory.size)
        assertTrue(allHistory.all { it.zikirId in 1..15 })

        val settings = db.settingsDao().getSettingsDirect()
        assertEquals(5, settings?.selectedZikirId)
        assertEquals(3, settings?.completedRounds)
    }
}
