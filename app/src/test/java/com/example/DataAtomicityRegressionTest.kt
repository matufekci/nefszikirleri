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
}
