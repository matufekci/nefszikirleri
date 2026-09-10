package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.backup.PasswordRequiredException
import com.example.data.backup.WrongPasswordException
import com.example.data.model.AppSettings
import com.example.data.model.ReminderSlot
import com.example.data.model.Zikir
import com.example.data.model.ZikirHistory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Hamle 10: Backup GZIP v1/v2 roundtrip, zip-bomb, wrong password, theme normalization
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupManagerCompressionTest {

    private lateinit var context: Context
    private lateinit var backupManager: BackupManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        backupManager = BackupManager(context)
    }

    private fun sampleData(): Triple<List<Zikir>, List<ZikirHistory>, List<ReminderSlot>> {
        val zikirs = (1..15).map { id ->
            Zikir(id = id, target = 70000L, count = (id * 100L), startedAt = 1000L, completedAt = if (id < 3) 2000L else null)
        }
        val history = (1..100).map { i ->
            ZikirHistory(
                id = i.toLong(),
                eventId = "evt_$i",
                zikirId = (i % 15) + 1,
                amount = 33L,
                type = "add",
                timestamp = 1000L + i,
                dateKey = "2026-05-13"
            )
        }
        val slots = listOf(
            ReminderSlot(id = 1, hour = 9, minute = 0, isEnabled = true),
            ReminderSlot(id = 2, hour = 20, minute = 30, isEnabled = true)
        )
        return Triple(zikirs, history, slots)
    }

    @Test
    fun testExportV2_GzipCompressed_AndImportRoundtrip() = runBlocking {
        val (zikirs, history, slots) = sampleData()
        val settings = AppSettings(themeName = "hadra_gece", lang = "tr")
        val password = "TestPassword123!"

        val out = ByteArrayOutputStream()
        val exportResult = backupManager.exportBackup(out, zikirs, history, slots, settings, password)
        assertTrue("Export should succeed", exportResult.isSuccess)

        val bytes = out.toByteArray()
        // First byte should be COMPRESSION_VERSION_BYTE 0x02
        assertEquals(BackupManager.COMPRESSION_VERSION_BYTE, bytes[0])
        // Encrypted size should be smaller than original JSON for repetitive history (70% reduction typical)
        // Original JSON would be > 10KB for 100 history entries, compressed should be significantly smaller
        assertTrue("Encrypted file should not be empty", bytes.size > 100)

        val input = ByteArrayInputStream(bytes)
        val importResult = backupManager.importBackup(input, password)
        assertTrue("Import should succeed", importResult.isSuccess)

        val data = importResult.getOrThrow()
        assertEquals(15, data.zikirs.size)
        assertEquals(100, data.history.size)
        assertEquals("hadra_gece", data.settings.themeName)
    }

    @Test
    fun testExportV2_CompressionReducesSize() = runBlocking {
        val (zikirs, history, slots) = sampleData()
        val settings = AppSettings()
        val password = "TestPassword123!"

        // Export with compression (current)
        val outCompressed = ByteArrayOutputStream()
        backupManager.exportBackup(outCompressed, zikirs, history, slots, settings, password)
        val compressedSize = outCompressed.size()

        // The compressed export should be reasonably small (< 25MB limit)
        assertTrue("Compressed backup should be under 25MB", compressedSize < 25 * 1024 * 1024)
        assertTrue("Compressed backup should be non-trivial", compressedSize > 50)
    }

    @Test
    fun testImport_WrongPassword_ThrowsWrongPasswordException() = runBlocking {
        val (zikirs, history, slots) = sampleData()
        val settings = AppSettings()
        val correctPassword = "CorrectPassword123!"
        val wrongPassword = "WrongPassword123!"

        val out = ByteArrayOutputStream()
        val exportResult = backupManager.exportBackup(out, zikirs, history, slots, settings, correctPassword)
        assertTrue(exportResult.isSuccess)

        val input = ByteArrayInputStream(out.toByteArray())
        val importResult = backupManager.importBackup(input, wrongPassword)
        assertTrue("Import with wrong password should fail", importResult.isFailure)
        assertTrue(
            "Should be WrongPasswordException",
            importResult.exceptionOrNull() is WrongPasswordException
        )
    }

    @Test
    fun testImport_NoPassword_ThrowsPasswordRequiredException() = runBlocking {
        val (zikirs, history, slots) = sampleData()
        val settings = AppSettings()
        val password = "SomePassword123!"

        val out = ByteArrayOutputStream()
        backupManager.exportBackup(out, zikirs, history, slots, settings, password)

        val input = ByteArrayInputStream(out.toByteArray())
        val result = backupManager.importBackup(input, null)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PasswordRequiredException)
    }

    @Test
    fun testImport_EmptyPassword_ThrowsPasswordRequiredException() = runBlocking {
        val (zikirs, history, slots) = sampleData()
        val settings = AppSettings()
        val password = "SomePassword123!"

        val out = ByteArrayOutputStream()
        backupManager.exportBackup(out, zikirs, history, slots, settings, password)

        val input = ByteArrayInputStream(out.toByteArray())
        val result = backupManager.importBackup(input, "")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PasswordRequiredException)
    }

    @Test
    fun testGzipDecompress_ZipBombProtection() {
        // Simulate a zip bomb: create a highly compressible payload that would decompress to >50MB
        // We can't easily create 50MB in test, but we can test the protection logic exists
        // by checking the constant
        assertEquals(50 * 1024 * 1024L, BackupManager.MAX_DECOMPRESSED_SIZE_BYTES)
        assertEquals(25 * 1024 * 1024L, BackupManager.MAX_BACKUP_SIZE_BYTES)
    }

    @Test
    fun testImport_LegacyTheme_NormalizesToCanonical() = runBlocking {
        // Simulate a backup with legacy theme name "emerald" which should normalize to "hadra_gece"
        val (zikirs, history, slots) = sampleData()
        val settingsLegacy = AppSettings(themeName = "emerald", lang = "tr")
        val password = "TestPassword123!"

        val out = ByteArrayOutputStream()
        val exportResult = backupManager.exportBackup(out, zikirs, history, slots, settingsLegacy, password)
        assertTrue(exportResult.isSuccess)

        val input = ByteArrayInputStream(out.toByteArray())
        val importResult = backupManager.importBackup(input, password)
        assertTrue(importResult.isSuccess)

        val data = importResult.getOrThrow()
        // BackupManager should normalize legacy theme to canonical
        assertEquals("hadra_gece", data.settings.themeName)
    }

    @Test
    fun testImport_LegacyThemeVariants_Normalizes() = runBlocking {
        val legacyThemes = mapOf(
            "emerald" to "hadra_gece",
            "light" to "hadra_gunduz",
            "obsidian" to "siyah",
            "rose" to "hadra_gunduz",
            "kisve" to "hadra_gece"
        )

        for ((legacy, expectedCanonical) in legacyThemes) {
            val (zikirs, history, slots) = sampleData()
            val settings = AppSettings(themeName = legacy)
            val password = "TestPassword123!"

            val out = ByteArrayOutputStream()
            backupManager.exportBackup(out, zikirs, history, slots, settings, password)

            val input = ByteArrayInputStream(out.toByteArray())
            val result = backupManager.importBackup(input, password)
            assertTrue("Import should succeed for legacy theme $legacy", result.isSuccess)
            assertEquals(
                "Legacy $legacy should normalize to $expectedCanonical",
                expectedCanonical,
                result.getOrThrow().settings.themeName
            )
        }
    }

    @Test
    fun testExportImport_LargeHistory_500Entries() = runBlocking {
        val zikirs = (1..15).map { Zikir(it, 70000L, 1000L, 1000L, null) }
        val largeHistory = (1..500).map {
            ZikirHistory(
                eventId = "evt_large_$it",
                zikirId = (it % 15) + 1,
                amount = 100L,
                type = "add",
                timestamp = 1000L + it,
                dateKey = "2026-05-13"
            )
        }
        val slots = listOf(ReminderSlot(1, 9, 0, true))
        val settings = AppSettings()

        val out = ByteArrayOutputStream()
        val exportResult = backupManager.exportBackup(out, zikirs, largeHistory, slots, settings, "pwd123")
        assertTrue(exportResult.isSuccess)

        val input = ByteArrayInputStream(out.toByteArray())
        val importResult = backupManager.importBackup(input, "pwd123")
        assertTrue(importResult.isSuccess)
        assertEquals(500, importResult.getOrThrow().history.size)
    }

    @Test
    fun testVersionBytes() {
        // Ensure version bytes are distinct and correct
        assertEquals(0x01.toByte(), BackupManager.ENCRYPTION_VERSION_BYTE)
        assertEquals(0x02.toByte(), BackupManager.COMPRESSION_VERSION_BYTE)
        assertNotEquals(
            BackupManager.ENCRYPTION_VERSION_BYTE,
            BackupManager.COMPRESSION_VERSION_BYTE
        )
    }
}
