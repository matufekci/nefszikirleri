package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ZikirDao
import com.example.data.local.ZikirHistoryDao
import com.example.data.model.Zikir
import com.example.data.model.ZikirHistory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeterministicHistoryOrderTest {

    private lateinit var db: AppDatabase
    private lateinit var zikirDao: ZikirDao
    private lateinit var historyDao: ZikirHistoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        zikirDao = db.zikirDao()
        historyDao = db.historyDao()

        runBlocking {
            zikirDao.insert(
                Zikir(
                    id = 1,
                    target = 33L,
                    count = 0L
                )
            )
        }
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testSameTimestamp_DeterministicIdDescOrdering() = runBlocking {
        val sharedTimestamp = 1700000000000L
        val dateKey = "2023-11-14"

        // Insert 5 entries with EXACTLY the same timestamp
        val id1 = historyDao.insert(ZikirHistory(zikirId = 1, amount = 1, type = "add", timestamp = sharedTimestamp, dateKey = dateKey))
        val id2 = historyDao.insert(ZikirHistory(zikirId = 1, amount = 2, type = "add", timestamp = sharedTimestamp, dateKey = dateKey))
        val id3 = historyDao.insert(ZikirHistory(zikirId = 1, amount = 3, type = "add", timestamp = sharedTimestamp, dateKey = dateKey))
        val id4 = historyDao.insert(ZikirHistory(zikirId = 1, amount = 4, type = "add", timestamp = sharedTimestamp, dateKey = dateKey))
        val id5 = historyDao.insert(ZikirHistory(zikirId = 1, amount = 5, type = "add", timestamp = sharedTimestamp, dateKey = dateKey))

        // 1. getAllHistory
        val allHistoryFlow = historyDao.getAllHistory().first()
        assertEquals(listOf(id5, id4, id3, id2, id1), allHistoryFlow.map { it.id })

        // 2. getAllHistoryDirect
        val allHistoryDirect = historyDao.getAllHistoryDirect()
        assertEquals(listOf(id5, id4, id3, id2, id1), allHistoryDirect.map { it.id })

        // 3. getMostRecentForZikir (Deterministic Undo anchor)
        val mostRecent = historyDao.getMostRecentForZikir(1)
        assertEquals(id5, mostRecent?.id)
        assertEquals(5L, mostRecent?.amount)

        // 4. getHistoryPagedDirect (Deterministic pagination without skipping or duplicating)
        val page1 = historyDao.getHistoryPagedDirect(limit = 2, offset = 0)
        val page2 = historyDao.getHistoryPagedDirect(limit = 2, offset = 2)
        val page3 = historyDao.getHistoryPagedDirect(limit = 2, offset = 4)

        assertEquals(listOf(id5, id4), page1.map { it.id })
        assertEquals(listOf(id3, id2), page2.map { it.id })
        assertEquals(listOf(id1), page3.map { it.id })

        // 5. getRecentManualHistoryDirect
        val recentManual = historyDao.getRecentManualHistoryDirect(fromTimestamp = sharedTimestamp)
        assertEquals(listOf(id5, id4, id3, id2, id1), recentManual.map { it.id })

        // 6. getHistoryForZikir
        val forZikirFlow = historyDao.getHistoryForZikir(1).first()
        assertEquals(listOf(id5, id4, id3, id2, id1), forZikirFlow.map { it.id })

        // 7. observeRecentHistory
        val recentHistoryFlow = historyDao.observeRecentHistory(3).first()
        assertEquals(listOf(id5, id4, id3), recentHistoryFlow.map { it.id })

        // 8. getRecent
        val recentDirect = historyDao.getRecent(3)
        assertEquals(listOf(id5, id4, id3), recentDirect.map { it.id })
    }
}
