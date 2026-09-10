package com.example

import com.example.data.cloud.SyncManager
import com.example.data.model.AppSettings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncManagerTest {

    @Test
    fun testSyncManagerInitialization() {
        val syncManager = SyncManager()
        assertNotNull(syncManager)
    }

    @Test
    fun testSyncManager_EmptyOrBlankUidReturnsFailure() = runBlocking {
        val syncManager = SyncManager()

        val emptyBackup = syncManager.backupToCloud("", emptyList(), emptyList(), emptyList(), AppSettings(), 0L, "test_dev")
        assertTrue(emptyBackup.isFailure)

        val blankBackup = syncManager.backupToCloud("   ", emptyList(), emptyList(), emptyList(), AppSettings(), 0L, "test_dev")
        assertTrue(blankBackup.isFailure)

        val emptyRestore = syncManager.restoreFromCloud("")
        assertTrue(emptyRestore.isFailure)

        val blankRestore = syncManager.restoreFromCloud("   ")
        assertTrue(blankRestore.isFailure)
    }
}

