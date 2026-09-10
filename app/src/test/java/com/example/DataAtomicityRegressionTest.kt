package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.Zikir
import com.example.data.repository.ZikirRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataAtomicityRegressionTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ZikirRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ZikirRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testAddDhikrCount_AtomicExecution() = runBlocking {
        repository.ensureInitialized()

        val (newCount, reachedTarget) = repository.addDhikrCount(1, 33L)
        assertEquals(33L, newCount)
        assertEquals(false, reachedTarget)

        val zikir = db.zikirDao().getZikirById(1)
        assertNotNull(zikir)
        assertEquals(33L, zikir?.count)

        val history = db.historyDao().getAllHistoryDirect()
        assertEquals(1, history.size)
        assertEquals(1, history[0].zikirId)
        assertEquals(33L, history[0].amount)
        assertEquals("add", history[0].type)

        val settings = db.settingsDao().getSettingsDirect()
        assertNotNull(settings)
        assertTrue((settings?.lastActiveTimestamp ?: 0L) > 0L)
    }

    @Test
    fun testRemoveAndUndo_AtomicConsistency() = runBlocking {
        repository.ensureInitialized()
        repository.addDhikrCount(1, 100L)

        val afterRemove = repository.removeDhikrCount(1, 30L)
        assertEquals(70L, afterRemove)

        val historyAfterRemove = db.historyDao().getAllHistoryDirect()
        assertEquals(2, historyAfterRemove.size)
        assertEquals("remove", historyAfterRemove[0].type)

        val afterUndo = repository.undoLastAction(1)
        assertEquals(100L, afterUndo)

        val zikir = db.zikirDao().getZikirById(1)
        assertEquals(100L, zikir?.count)

        val historyAfterUndo = db.historyDao().getAllHistoryDirect()
        assertEquals(1, historyAfterUndo.size)
    }

    @Test
    fun testFastJumpToZikir_AtomicBatch() = runBlocking {
        repository.ensureInitialized()
        
        repository.fastJumpToZikir(3)

        val zikir1 = db.zikirDao().getZikirById(1)
        val zikir2 = db.zikirDao().getZikirById(2)
        val zikir3 = db.zikirDao().getZikirById(3)

        assertEquals(zikir1?.target, zikir1?.count)
        assertEquals(zikir2?.target, zikir2?.count)
        assertEquals(0L, zikir3?.count)

        val settings = db.settingsDao().getSettingsDirect()
        assertEquals(3, settings?.selectedZikirId)
    }

    @Test
    fun testStartNewRound_AtomicReset() = runBlocking {
        repository.ensureInitialized()
        repository.addDhikrCount(1, 500L)

        repository.startNewRound()

        val allZikirs = db.zikirDao().getAllZikirsDirect()
        assertTrue(allZikirs.all { it.count == 0L })

        val history = db.historyDao().getAllHistoryDirect()
        assertTrue(history.isEmpty())

        val settings = db.settingsDao().getSettingsDirect()
        assertEquals(1, settings?.completedRounds)
        assertEquals(1, settings?.selectedZikirId)
    }

    @Test
    fun testBackupSnapshot_ConsistentState() = runBlocking {
        repository.ensureInitialized()
        repository.addDhikrCount(1, 15L)

        // Simulate a scenario where a snapshot is taken
        val snapshot = repository.getAtomicSnapshot()

        // Assert that the snapshot is internally consistent
        val zikir = snapshot.zikirs.find { it.id == 1 }
        assertEquals(15L, zikir?.count)

        val history = snapshot.history
        assertEquals(1, history.size)
        assertEquals(15L, history[0].amount)
        assertEquals("add", history[0].type)

        assertNotNull(snapshot.settings)
        assertTrue(snapshot.slots.isNotEmpty())

        // Ensure that sum of history amounts for zikir 1 matches its count
        val historySum = history.filter { it.zikirId == 1 && it.type == "add" }.sumOf { it.amount }
        assertEquals(zikir?.count, historySum)
    }

    @Test
    fun testMergeHistory_SameTimestampDifferentEntries_BothPreserved() = runBlocking {
        repository.ensureInitialized()
        
        val sharedTimestamp = 1700000000000L
        val localHistory = listOf(
            com.example.data.model.ZikirHistory(id = 1, zikirId = 1, amount = 33L, type = "add", timestamp = sharedTimestamp, dateKey = "2026-09-01")
        )
        val remoteHistory = listOf(
            com.example.data.model.ZikirHistory(id = 2, zikirId = 2, amount = 100L, type = "add", timestamp = sharedTimestamp, dateKey = "2026-09-01")
        )

        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val savedStateHandle = androidx.lifecycle.SavedStateHandle()
        val zikirViewModel = com.example.ui.viewmodel.ZikirViewModel(app, savedStateHandle)

        // Inject in-memory repository into viewModel
        val repoField = com.example.ui.viewmodel.ZikirViewModel::class.java.getDeclaredField("repository")
        repoField.isAccessible = true
        repoField.set(zikirViewModel, repository)

        val backupData = com.example.data.cloud.CloudBackupData(
            zikirs = (1..15).map { id ->
                when (id) {
                    1 -> com.example.data.model.Zikir(id = 1, target = 70000L, count = 33L, startedAt = sharedTimestamp)
                    2 -> com.example.data.model.Zikir(id = 2, target = 80000L, count = 100L, startedAt = sharedTimestamp)
                    else -> com.example.data.model.Zikir(id = id, target = 70000L, count = 0L)
                }
            },
            settings = AppSettings(),
            reminderSlots = emptyList(),
            history = remoteHistory,
            syncMetadata = com.example.data.cloud.SyncMetadata(revision = 1L, updatedAt = sharedTimestamp, deviceId = "dev2"),
            lastSyncedAt = sharedTimestamp
        )

        // Set conflict state and call resolveConflictWithMerge
        val syncConflictField = com.example.ui.viewmodel.ZikirViewModel::class.java.getDeclaredField("_syncConflictState")
        syncConflictField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = syncConflictField.get(zikirViewModel) as kotlinx.coroutines.flow.MutableStateFlow<com.example.data.cloud.CloudBackupData?>
        stateFlow.value = backupData

        // Seed DB with local history
        db.historyDao().insertAll(localHistory)

        zikirViewModel.resolveConflictWithMerge()
        
        // Allow coroutine execution on Dispatchers.IO to finish
        var retries = 50
        var storedHistory = db.historyDao().getAllHistoryDirect()
        while (storedHistory.size < 2 && retries > 0) {
            Thread.sleep(50)
            storedHistory = db.historyDao().getAllHistoryDirect()
            retries--
        }

        assertEquals(2, storedHistory.size)
        assertTrue(storedHistory.any { it.zikirId == 1 && it.timestamp == sharedTimestamp })
        assertTrue(storedHistory.any { it.zikirId == 2 && it.timestamp == sharedTimestamp })
    }

    @Test
    fun testMergePreservesDistinctEventsWithIdenticalTimestampAndAttributes() = runBlocking {
        repository.ensureInitialized()

        val sharedTimestamp = 1700000000000L
        val event1 = com.example.data.model.ZikirHistory(
            id = 1,
            eventId = "event_local_001",
            zikirId = 1,
            amount = 33L,
            type = "add",
            timestamp = sharedTimestamp,
            dateKey = "2026-09-01"
        )
        val event2 = com.example.data.model.ZikirHistory(
            id = 2,
            eventId = "event_remote_002", // Different eventId, but same zikirId, amount, type, timestamp, dateKey
            zikirId = 1,
            amount = 33L,
            type = "add",
            timestamp = sharedTimestamp,
            dateKey = "2026-09-01"
        )
        val duplicateEvent1 = com.example.data.model.ZikirHistory(
            id = 3,
            eventId = "event_local_001", // Exact duplicate eventId
            zikirId = 1,
            amount = 33L,
            type = "add",
            timestamp = sharedTimestamp,
            dateKey = "2026-09-01"
        )

        val localHistory = listOf(event1)
        val remoteHistory = listOf(event2, duplicateEvent1)

        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val savedStateHandle = androidx.lifecycle.SavedStateHandle()
        val zikirViewModel = com.example.ui.viewmodel.ZikirViewModel(app, savedStateHandle)

        val repoField = com.example.ui.viewmodel.ZikirViewModel::class.java.getDeclaredField("repository")
        repoField.isAccessible = true
        repoField.set(zikirViewModel, repository)

        val backupData = com.example.data.cloud.CloudBackupData(
            zikirs = (1..15).map { id ->
                when (id) {
                    1 -> com.example.data.model.Zikir(id = 1, target = 70000L, count = 66L, startedAt = sharedTimestamp)
                    else -> com.example.data.model.Zikir(id = id, target = 70000L, count = 0L)
                }
            },
            settings = AppSettings(),
            reminderSlots = emptyList(),
            history = remoteHistory,
            syncMetadata = com.example.data.cloud.SyncMetadata(revision = 1L, updatedAt = sharedTimestamp, deviceId = "dev2"),
            lastSyncedAt = sharedTimestamp
        )

        val syncConflictField = com.example.ui.viewmodel.ZikirViewModel::class.java.getDeclaredField("_syncConflictState")
        syncConflictField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = syncConflictField.get(zikirViewModel) as kotlinx.coroutines.flow.MutableStateFlow<com.example.data.cloud.CloudBackupData?>
        stateFlow.value = backupData

        db.historyDao().insertAll(localHistory)

        zikirViewModel.resolveConflictWithMerge()

        var retries = 50
        var storedHistory = db.historyDao().getAllHistoryDirect()
        while (storedHistory.size < 2 && retries > 0) {
            Thread.sleep(50)
            storedHistory = db.historyDao().getAllHistoryDirect()
            retries--
        }

        // Exactly 2 events should be preserved (event_local_001 and event_remote_002), duplicate event_local_001 dropped
        assertEquals(2, storedHistory.size)
        val eventIds = storedHistory.map { it.eventId }.toSet()
        assertTrue(eventIds.contains("event_local_001"))
        assertTrue(eventIds.contains("event_remote_002"))
    }

    @Test
    fun testMergeCountDerivedFromHistoryAddRemoveAndConsistencyWithTargetCompletedAt(): Unit = runBlocking {
        repository.ensureInitialized()

        val t1 = 1700000001000L
        val t2 = 1700000002000L
        val t3 = 1700000003000L
        val t4 = 1700000004000L
        val t5 = 1700000005000L

        // Local history
        val localHistory = listOf(
            com.example.data.model.ZikirHistory(id = 1, eventId = "ev_loc_1", zikirId = 1, amount = 500L, type = "add", timestamp = t1, dateKey = "2026-09-01"),
            com.example.data.model.ZikirHistory(id = 2, eventId = "ev_loc_2", zikirId = 1, amount = 100L, type = "remove", timestamp = t2, dateKey = "2026-09-01"),
            com.example.data.model.ZikirHistory(id = 3, eventId = "ev_loc_3", zikirId = 2, amount = 300L, type = "add", timestamp = t3, dateKey = "2026-09-01")
        )

        // Remote history
        val remoteHistory = listOf(
            com.example.data.model.ZikirHistory(id = 4, eventId = "ev_rem_1", zikirId = 1, amount = 200L, type = "add", timestamp = t4, dateKey = "2026-09-01"),
            com.example.data.model.ZikirHistory(id = 5, eventId = "ev_rem_2", zikirId = 2, amount = 700L, type = "add", timestamp = t5, dateKey = "2026-09-01")
        )

        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val savedStateHandle = androidx.lifecycle.SavedStateHandle()
        val zikirViewModel = com.example.ui.viewmodel.ZikirViewModel(app, savedStateHandle)

        val repoField = com.example.ui.viewmodel.ZikirViewModel::class.java.getDeclaredField("repository")
        repoField.isAccessible = true
        repoField.set(zikirViewModel, repository)

        // Remote zikirs with target = 1000 for zikir 1 and 2
        val backupData = com.example.data.cloud.CloudBackupData(
            zikirs = (1..15).map { id ->
                when (id) {
                    1 -> com.example.data.model.Zikir(id = 1, target = 1000L, count = 9999L, startedAt = t1) // Unrelated count in snapshot
                    2 -> com.example.data.model.Zikir(id = 2, target = 1000L, count = 5000L, startedAt = t3) // Unrelated count in snapshot
                    else -> com.example.data.model.Zikir(id = id, target = 1000L, count = 0L)
                }
            },
            settings = AppSettings(),
            reminderSlots = emptyList(),
            history = remoteHistory,
            syncMetadata = com.example.data.cloud.SyncMetadata(revision = 1L, updatedAt = t5, deviceId = "dev2"),
            lastSyncedAt = t5
        )

        val syncConflictField = com.example.ui.viewmodel.ZikirViewModel::class.java.getDeclaredField("_syncConflictState")
        syncConflictField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = syncConflictField.get(zikirViewModel) as kotlinx.coroutines.flow.MutableStateFlow<com.example.data.cloud.CloudBackupData?>
        stateFlow.value = backupData

        db.historyDao().insertAll(localHistory)

        zikirViewModel.resolveConflictWithMerge()

        var retries = 50
        var storedHistory = db.historyDao().getAllHistoryDirect()
        while (storedHistory.size < 5 && retries > 0) {
            Thread.sleep(50)
            storedHistory = db.historyDao().getAllHistoryDirect()
            retries--
        }
        val storedZikirs = db.zikirDao().getAllZikirsDirect()

        assertEquals(5, storedHistory.size)

        // Zikir 1: +500, -100, +200 -> count = 600, target = 1000, completedAt = null, startedAt = t1
        val zikir1 = storedZikirs.first { it.id == 1 }
        assertEquals(600L, zikir1.count)
        assertEquals(1000L, zikir1.target)
        assertEquals(t1, zikir1.startedAt)
        assertNull(zikir1.completedAt)

        // Zikir 2: +300, +700 -> count = 1000, target = 1000, completedAt != null, startedAt = t3
        val zikir2 = storedZikirs.first { it.id == 2 }
        assertEquals(1000L, zikir2.count)
        assertEquals(1000L, zikir2.target)
        assertEquals(t3, zikir2.startedAt)
        assertNotNull(zikir2.completedAt)
        assertTrue(zikir2.completedAt!! >= zikir2.startedAt!!)

        // Zikir 3: 0 count, null startedAt and completedAt
        val zikir3 = storedZikirs.first { it.id == 3 }
        assertEquals(0L, zikir3.count)
        assertNull(zikir3.startedAt)
        assertNull(zikir3.completedAt)
    }

    @Test
    fun testDateKeyAndTimestampConsistency_MidnightBoundary() = runBlocking {
        repository.ensureInitialized()

        // 1. Check direct NumberFormatter timestamp-to-dateKey at 23:59:59.999
        val cal = java.util.Calendar.getInstance()
        cal.set(2026, java.util.Calendar.SEPTEMBER, 8, 23, 59, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        val endOfDayMillis = cal.timeInMillis

        val dateKeyEndOfDay = com.example.util.NumberFormatter.getDateKey(endOfDayMillis)
        val expectedDateKey = String.format(
            java.util.Locale.US,
            "%04d-%02d-%02d",
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
        assertEquals(expectedDateKey, dateKeyEndOfDay)

        // 2. Add count and ensure timestamp & dateKey in history are 100% synchronized
        repository.addDhikrCount(1, 33L)
        val history = db.historyDao().getAllHistoryDirect()
        assertEquals(1, history.size)
        val entry = history[0]
        assertEquals(com.example.util.NumberFormatter.getDateKey(entry.timestamp), entry.dateKey)

        // 3. Remove count and ensure single-source timestamp & dateKey consistency
        repository.removeDhikrCount(1, 10L)
        val historyAfterRemove = db.historyDao().getAllHistoryDirect()
        assertEquals(2, historyAfterRemove.size)
        val removeEntry = historyAfterRemove[0]
        assertEquals(com.example.util.NumberFormatter.getDateKey(removeEntry.timestamp), removeEntry.dateKey)
    }
}
