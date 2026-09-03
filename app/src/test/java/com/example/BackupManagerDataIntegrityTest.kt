package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.model.AppSettings
import com.example.data.model.ReminderSlot
import com.example.data.model.Zikir
import com.example.data.model.ZikirHistory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupManagerDataIntegrityTest {

    private lateinit var context: Context
    private lateinit var backupManager: BackupManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        backupManager = BackupManager(context)
    }

    @Test
    fun testExportAndImport_ValidFullPayload() = runBlocking {
        val zikirs = listOf(
            Zikir(id = 1, target = 70000L, count = 3300L, startedAt = 1000L, completedAt = null),
            Zikir(id = 2, target = 80000L, count = 80000L, startedAt = 1000L, completedAt = 2000L)
        )
        val history = listOf(
            ZikirHistory(id = 1, zikirId = 1, amount = 33L, type = "add", timestamp = 1500L, dateKey = "2026-09-01")
        )
        val slots = listOf(
            ReminderSlot(id = 1, hour = 14, minute = 30, isEnabled = true)
        )
        val settings = AppSettings(
            id = 1,
            dailyTarget = 5000L,
            completedRounds = 2,
            selectedZikirId = 1
        )

        val out = ByteArrayOutputStream()
        val exportResult = backupManager.exportBackup(out, zikirs, history, slots, settings)
        assertTrue(exportResult.isSuccess)

        val inputStream = ByteArrayInputStream(out.toByteArray())
        val importResult = backupManager.importBackup(inputStream)
        assertTrue(importResult.isSuccess)

        val data = importResult.getOrThrow()
        assertEquals(2, data.zikirs.size)
        assertEquals(3300L, data.zikirs[0].count)
        assertEquals(1, data.history.size)
        assertEquals(33L, data.history[0].amount)
        assertEquals(1, data.reminderSlots.size)
        assertEquals(14, data.reminderSlots[0].hour)
        assertEquals(2, data.settings.completedRounds)
    }

    @Test
    fun testImport_CorruptedJson_ReturnsFailureWithoutCrashing() = runBlocking {
        val corruptedJson = "{ this is not a valid json content !!! }"
        val inputStream = ByteArrayInputStream(corruptedJson.toByteArray(Charsets.UTF_8))
        val result = backupManager.importBackup(inputStream)
        assertTrue(result.isFailure)
    }

    @Test
    fun testImport_InvalidSchemaVersion_ReturnsFailure() = runBlocking {
        val futureVersionJson = """
            {
                "schemaVersion": 999,
                "appName": "NefsZikir",
                "timestamp": 123456789,
                "zikirs": [
                    {"id": 1, "target": 70000, "count": 100}
                ]
            }
        """.trimIndent()
        val inputStream = ByteArrayInputStream(futureVersionJson.toByteArray(Charsets.UTF_8))
        val result = backupManager.importBackup(inputStream)
        assertTrue(result.isFailure)
    }

    @Test
    fun testImport_NegativeAndOverflowValues_ClampedSafely() = runBlocking {
        val weirdValuesJson = """
            {
                "schemaVersion": 1,
                "appName": "NefsZikir",
                "timestamp": 123456789,
                "zikirs": [
                    {"id": 1, "target": -500, "count": -9999}
                ],
                "history": [
                    {"id": 0, "zikirId": 1, "amount": -10, "type": "unknown", "timestamp": -5}
                ],
                "reminderSlots": [
                    {"id": 0, "hour": 99, "minute": -10, "isEnabled": true}
                ],
                "settings": {
                    "completedRounds": -5,
                    "fontScale": 10.0
                }
            }
        """.trimIndent()

        val inputStream = ByteArrayInputStream(weirdValuesJson.toByteArray(Charsets.UTF_8))
        val result = backupManager.importBackup(inputStream)
        assertTrue(result.isSuccess)

        val data = result.getOrThrow()
        assertEquals(0L, data.zikirs[0].count)
        assertEquals(100L, data.zikirs[0].target) // Coerced to minimum allowed target
        assertEquals(1L, data.history[0].amount) // Coerced to positive
        assertEquals(23, data.reminderSlots[0].hour) // Clamped to 23
        assertEquals(0, data.reminderSlots[0].minute) // Clamped to 0
        assertEquals(0, data.settings.completedRounds) // Coerced to 0
        assertEquals(1.5f, data.settings.fontScale, 0.01f) // Clamped to max 1.5f
    }
}
