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
        val zikirs = (1..15).map { id ->
            when (id) {
                1 -> Zikir(id = 1, target = 70000L, count = 3300L, startedAt = 1000L, completedAt = null)
                2 -> Zikir(id = 2, target = 80000L, count = 80000L, startedAt = 1000L, completedAt = 2000L)
                else -> Zikir(id = id, target = 70000L, count = 0L, startedAt = null, completedAt = null)
            }
        }
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
        val testPassword = "StrongPassword123!"
        val exportResult = backupManager.exportBackup(out, zikirs, history, slots, settings, testPassword)
        assertTrue(exportResult.isSuccess)

        val inputStream = ByteArrayInputStream(out.toByteArray())
        val importResult = backupManager.importBackup(inputStream, testPassword)
        assertTrue(importResult.isSuccess)

        val data = importResult.getOrThrow()
        assertEquals(15, data.zikirs.size)
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
    fun testImport_NegativeAndOverflowValues_RejectedByStrictValidation() = runBlocking {
        val weirdValuesJson = """
            {
                "appName": "com.example.nefs_zikir",
                "schemaVersion": 1,
                "exportedAt": 123456789,
                "zikirs": [
                    {"id": 1, "target": -500, "count": -9999, "startedAt": null, "completedAt": null}
                ],
                "history": [
                    {"id": 0, "zikirId": 1, "amount": -10, "type": "unknown", "timestamp": -5, "dateKey": ""}
                ],
                "reminderSlots": [
                    {"id": 0, "hour": 99, "minute": -10, "isEnabled": true}
                ],
                "settings": {
                    "dailyTarget": 5000,
                    "themeName": "emerald",
                    "lang": "tr",
                    "hapticEnabled": true,
                    "hapticTapMode": "light",
                    "hapticMilestoneMode": "double",
                    "countdownMode": false,
                    "fullScreenTap": false,
                    "reminderEnabled": false,
                    "inactivityAlertEnabled": false,
                    "completedRounds": -5,
                    "fontScale": 10.0,
                    "keepAwakeEnabled": true,
                    "selectedZikirId": 1,
                    "counterTexture": "geometric",
                    "targetReminderEnabled": false,
                    "acknowledgedBadges": "",
                    "autoReorderSettings": false,
                    "settingsUsageStats": "{}"
                }
            }
        """.trimIndent()

        val inputStream = ByteArrayInputStream(weirdValuesJson.toByteArray(Charsets.UTF_8))
        val result = backupManager.importBackup(inputStream)
        assertTrue(result.isFailure)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateZikirStrict_InvalidStartedAt_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateZikirStrict(
            Zikir(id = 1, target = 1000L, count = 10L, startedAt = -100L, completedAt = null)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateZikirStrict_MissingStartedAtForCount_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateZikirStrict(
            Zikir(id = 1, target = 1000L, count = 10L, startedAt = null, completedAt = null)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateZikirStrict_MissingCompletedAtForTarget_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateZikirStrict(
            Zikir(id = 1, target = 1000L, count = 1000L, startedAt = 1000L, completedAt = null)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateHistoryStrict_EmptyDateKey_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateHistoryStrict(
            ZikirHistory(id = 1, zikirId = 1, amount = 10L, type = "add", timestamp = 1000L, dateKey = "")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateHistoryStrict_CorruptedTimestamp_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateHistoryStrict(
            ZikirHistory(id = 1, zikirId = 1, amount = 10L, type = "add", timestamp = 0L, dateKey = "2026-09-01")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateSettingsStrict_InvalidLang_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateSettingsStrict(
            com.example.data.model.AppSettings().copy(lang = "es")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateSettingsStrict_InvalidTheme_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateSettingsStrict(
            com.example.data.model.AppSettings().copy(themeName = "invalid_theme")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateSettingsStrict_InvalidTexture_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateSettingsStrict(
            com.example.data.model.AppSettings().copy(counterTexture = "invalid_texture")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateSettingsStrict_InvalidHapticTap_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateSettingsStrict(
            com.example.data.model.AppSettings().copy(hapticTapMode = "invalid_haptic")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateSettingsStrict_InvalidHapticMilestone_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateSettingsStrict(
            com.example.data.model.AppSettings().copy(hapticMilestoneMode = "invalid_milestone")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateSettingsStrict_MalformedBadges_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateSettingsStrict(
            com.example.data.model.AppSettings().copy(acknowledgedBadges = "unknown_badge")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateSettingsStrict_MalformedBadgesTrailingComma_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateSettingsStrict(
            com.example.data.model.AppSettings().copy(acknowledgedBadges = "zikir_1,")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidateSettingsStrict_MalformedUsageStatsJson_ThrowsException() {
        com.example.data.model.DhikrDataValidator.validateSettingsStrict(
            com.example.data.model.AppSettings().copy(settingsUsageStats = "not_a_json")
        )
    }

    @Test
    fun testValidateSettingsStrict_ValidSettings_Passes() {
        val valid = com.example.data.model.AppSettings(
            lang = "tr",
            themeName = "emerald",
            counterTexture = "geometric",
            hapticTapMode = "light",
            hapticMilestoneMode = "double",
            dailyTarget = 10000L,
            completedRounds = 1,
            fontScale = 1.15f,
            selectedZikirId = 1,
            acknowledgedBadges = "zikir_1,streak_7",
            settingsUsageStats = "{\"appearance\":1}"
        )
        val result = com.example.data.model.DhikrDataValidator.validateSettingsStrict(valid)
        assertEquals("tr", result.lang)
        assertEquals("emerald", result.themeName)
    }

    @Test(expected = com.example.data.cloud.CloudDataCorruptionException::class)
    fun testParseLongStrict_FractionalValue_ThrowsCloudDataCorruptionException() {
        val syncManager = com.example.data.cloud.SyncManager()
        val method = com.example.data.cloud.SyncManager::class.java.getDeclaredMethod("parseLongStrict", Any::class.java, String::class.java)
        method.isAccessible = true
        try {
            method.invoke(syncManager, 123.9, "testField")
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.cause ?: e
        }
    }

    @Test
    fun testParseLongStrict_IntegralDouble_Succeeds() {
        val syncManager = com.example.data.cloud.SyncManager()
        val method = com.example.data.cloud.SyncManager::class.java.getDeclaredMethod("parseLongStrict", Any::class.java, String::class.java)
        method.isAccessible = true
        val result = method.invoke(syncManager, 123.0, "testField") as Long
        assertEquals(123L, result)
    }

    @Test
    fun testParseLongStrict_IntegralString_Succeeds() {
        val syncManager = com.example.data.cloud.SyncManager()
        val method = com.example.data.cloud.SyncManager::class.java.getDeclaredMethod("parseLongStrict", Any::class.java, String::class.java)
        method.isAccessible = true
        val result = method.invoke(syncManager, "123.0", "testField") as Long
        assertEquals(123L, result)
    }

    @Test(expected = com.example.data.cloud.CloudDataCorruptionException::class)
    fun testParseLongStrict_FractionalString_ThrowsCloudDataCorruptionException() {
        val syncManager = com.example.data.cloud.SyncManager()
        val method = com.example.data.cloud.SyncManager::class.java.getDeclaredMethod("parseLongStrict", Any::class.java, String::class.java)
        method.isAccessible = true
        try {
            method.invoke(syncManager, "123.9", "testField")
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.cause ?: e
        }
    }

    @Test
    fun testCreateTemporaryBackupFile_CreatesInBackupsDirectoryAndCleansUpOldFiles() {
        val backupsDir = java.io.File(context.cacheDir, "backups").apply { mkdirs() }
        
        // 1. Oluşturulan dosyanın backups dizininde olduğunu doğrula
        val tempFile = backupManager.createTemporaryBackupFile()
        assertTrue(tempFile.parentFile!!.name == "backups")
        assertTrue(tempFile.name.startsWith("nefs_zikir_backup_"))
        assertTrue(tempFile.name.endsWith(".edb"))

        // 2. Kullanıcıya ait harici/farklı isimdeki bir dosyanın silinmediğini doğrula
        val userFile = java.io.File(backupsDir, "user_selected_backup.json").apply {
            writeText("{}")
        }

        // 3. Eski geçici dosyalar üret
        val oldTempFile = java.io.File(backupsDir, "nefs_zikir_backup_old1.edb").apply {
            writeText("dummy")
            setLastModified(System.currentTimeMillis() - (48 * 60 * 60 * 1000L)) // 48 saat önce
        }

        // Cleanup çalıştır
        backupManager.cleanupTemporaryBackups(maxAgeMillis = 24 * 60 * 60 * 1000L)

        // Eski geçici dosya silinmiş olmalı
        org.junit.Assert.assertFalse(oldTempFile.exists())
        // Kullanıcı dosyası korunmuş olmalı
        assertTrue(userFile.exists())

        // Temizlik
        userFile.delete()
    }
}
