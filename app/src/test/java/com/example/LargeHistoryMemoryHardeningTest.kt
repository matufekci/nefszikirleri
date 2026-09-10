package com.example

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.Zikir
import com.example.data.model.ZikirHistory
import com.example.data.repository.ZikirRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LargeHistoryMemoryHardeningTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ZikirRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ZikirRepository(
            database = db,
            zikirDao = db.zikirDao(),
            settingsDao = db.settingsDao(),
            reminderDao = db.reminderDao(),
            historyDao = db.historyDao()
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testLargeHistory_50kEntries_ChunkedLoadingAndAggregateIntegrity() = runBlocking {
        repository.ensureInitialized()

        val totalRecords = 50_000
        val chunkSize = 2_500
        val baseTimestamp = 1700000000000L

        // Insert 50,000 history entries in batch chunks within transaction
        db.withTransaction {
            for (chunkStart in 0 until totalRecords step chunkSize) {
                val batch = (chunkStart until (chunkStart + chunkSize)).map { i ->
                    ZikirHistory(
                        id = (i + 1).toLong(),
                        zikirId = (i % 7) + 1,
                        amount = 10L,
                        type = "add",
                        timestamp = baseTimestamp + (i * 1000L),
                        dateKey = "2026-09-${((i % 28) + 1).toString().padStart(2, '0')}"
                    )
                }
                db.historyDao().insertAll(batch)
            }
        }

        val totalCount = db.historyDao().getHistoryCountDirect()
        assertEquals(50_000, totalCount)

        // 1. Test chunked loading
        val loadedHistory = repository.getAllHistoryInChunksDirect(chunkSize = 2_000)
        assertEquals(50_000, loadedHistory.size)

        // 2. Test aggregate total without loading all into heap
        val totalRecited = db.historyDao().getTotalRecitedDirect()
        assertEquals(500_000L, totalRecited)

        // 3. Test recent filtered history
        val recentHistory = db.historyDao().getRecentManualHistoryDirect(
            fromTimestamp = baseTimestamp + (49_900 * 1000L),
            maxAmount = 10000L
        )
        assertTrue(recentHistory.isNotEmpty())
        assertTrue(recentHistory.size <= 100)

        // 4. Test atomic snapshot with chunked history loading
        val snapshot = repository.getAtomicSnapshot()
        assertNotNull(snapshot)
        assertEquals(50_000, snapshot.history.size)
        assertEquals(15, snapshot.zikirs.size)
    }
}
