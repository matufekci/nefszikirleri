package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.HistoryDao
import com.example.data.local.ReminderDao
import com.example.data.local.SettingsDao
import com.example.data.local.ZikirDao
import com.example.data.model.AppSettings
import com.example.data.model.ReminderSlot
import com.example.data.model.Zikir
import com.example.data.model.ZikirContent
import com.example.data.model.ZikirHistory
import com.example.util.NumberFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ZikirRepository(
    private val database: AppDatabase,
    private val zikirDao: ZikirDao = database.zikirDao(),
    private val historyDao: HistoryDao = database.historyDao(),
    private val reminderDao: ReminderDao = database.reminderDao(),
    private val settingsDao: SettingsDao = database.settingsDao()
) {
    private val zikirMutex = Mutex()

    val allZikirs: Flow<List<Zikir>> = zikirDao.getAllZikirs()
    val allHistory: Flow<List<ZikirHistory>> = historyDao.getAllHistory()
    val allSlots: Flow<List<ReminderSlot>> = reminderDao.getAllSlots()
    val settings: Flow<AppSettings?> = settingsDao.getSettings()

    suspend fun ensureInitialized() = database.withTransaction {
        if (zikirDao.getCount() == 0) {
            val initialZikirs = ZikirContent.INITIAL_DEFINITIONS.map { def ->
                Zikir(id = def.id, target = def.defaultTarget, count = 0L)
            }
            zikirDao.insertAll(initialZikirs)
        }
        if (reminderDao.getCount() == 0) {
            val defaultSlots = listOf(
                ReminderSlot(hour = 9, minute = 0, isEnabled = true),
                ReminderSlot(hour = 20, minute = 30, isEnabled = true)
            )
            reminderDao.insertAll(defaultSlots)
        }
        val currentSettings = settingsDao.getSettingsDirect()
        if (currentSettings == null) {
            settingsDao.insertOrUpdate(AppSettings())
        } else if (currentSettings.fontScale <= 1.05f) {
            settingsDao.insertOrUpdate(currentSettings.copy(fontScale = 1.15f))
        }
    }

    suspend fun addDhikrCount(zikirId: Int, amount: Long): Pair<Long, Boolean> = zikirMutex.withLock {
        database.withTransaction {
            val now = System.currentTimeMillis()
            
            // 1. SQLite sayım artışı
            zikirDao.incrementZikirCount(zikirId, amount, now)
            
            // 2. Güncel durumu oku
            val zikir = zikirDao.getZikirById(zikirId) ?: return@withTransaction Pair(0L, false)
            val reachedTarget = zikir.completedAt == now || (zikir.count >= zikir.target)

            val historyEntry = ZikirHistory(
                zikirId = zikirId,
                amount = amount,
                type = "add",
                timestamp = now,
                dateKey = NumberFormatter.getDateKey()
            )
            historyDao.insert(historyEntry)

            // Update settings last active timestamp
            val currentSettings = settingsDao.getSettingsDirect() ?: AppSettings()
            settingsDao.insertOrUpdate(currentSettings.copy(lastActiveTimestamp = now))

            Pair(zikir.count, reachedTarget)
        }
    }

    suspend fun removeDhikrCount(zikirId: Int, amount: Long): Long = zikirMutex.withLock {
        database.withTransaction {
            val now = System.currentTimeMillis()
            
            // 1. SQLite sayım azalışı
            zikirDao.decrementZikirCount(zikirId, amount)
            
            // 2. Güncel durumu oku
            val zikir = zikirDao.getZikirById(zikirId) ?: return@withTransaction 0L

            val historyEntry = ZikirHistory(
                zikirId = zikirId,
                amount = amount,
                type = "remove",
                timestamp = now,
                dateKey = NumberFormatter.getDateKey()
            )
            historyDao.insert(historyEntry)

            zikir.count
        }
    }
    
    suspend fun undoLastAction(zikirId: Int): Long? = zikirMutex.withLock {
        database.withTransaction {
            val recentHistory = historyDao.getMostRecentForZikir(zikirId)
            if (recentHistory != null) {
                historyDao.deleteById(recentHistory.id)
                if (recentHistory.type == "add") {
                    zikirDao.decrementZikirCount(zikirId, recentHistory.amount)
                } else if (recentHistory.type == "remove") {
                    zikirDao.incrementZikirCount(zikirId, recentHistory.amount, System.currentTimeMillis())
                }
                return@withTransaction zikirDao.getZikirById(zikirId)?.count
            }
            null
        }
    }

    suspend fun resetSingleZikir(zikirId: Int) = zikirMutex.withLock {
        database.withTransaction {
            zikirDao.resetZikir(zikirId)
            historyDao.deleteForZikir(zikirId)
        }
    }

    suspend fun resetAllZikirs() = zikirMutex.withLock {
        database.withTransaction {
            zikirDao.resetAllZikirs()
            historyDao.deleteAll()
        }
    }

    suspend fun fastJumpToZikir(targetZikirId: Int) = zikirMutex.withLock {
        database.withTransaction {
            val now = System.currentTimeMillis()
            val clampedTargetId = targetZikirId.coerceIn(1, 15)
            for (id in 1 until clampedTargetId) {
                val zikir = zikirDao.getZikirById(id)
                if (zikir != null && zikir.count < zikir.target) {
                    val amountToAdd = zikir.target - zikir.count
                    val updated = zikir.copy(
                        count = zikir.target,
                        startedAt = if (zikir.startedAt == null || zikir.startedAt == 0L) now else zikir.startedAt,
                        completedAt = zikir.completedAt ?: now
                    )
                    zikirDao.insert(updated)

                    val historyEntry = ZikirHistory(
                        zikirId = id,
                        amount = amountToAdd,
                        type = "add",
                        timestamp = now,
                        dateKey = NumberFormatter.getDateKey()
                    )
                    historyDao.insert(historyEntry)
                }
            }

            // Ensure the target zikir is marked as started if not already
            val targetZikir = zikirDao.getZikirById(clampedTargetId)
            if (targetZikir != null && (targetZikir.startedAt == null || targetZikir.startedAt == 0L)) {
                zikirDao.insert(targetZikir.copy(startedAt = now))
            }

            val currentSettings = settingsDao.getSettingsDirect() ?: AppSettings()
            settingsDao.insertOrUpdate(currentSettings.copy(
                selectedZikirId = clampedTargetId,
                lastActiveTimestamp = now
            ))
        }
    }

    suspend fun startNewRound() = zikirMutex.withLock {
        database.withTransaction {
            val currentSettings = settingsDao.getSettingsDirect() ?: AppSettings()
            settingsDao.insertOrUpdate(currentSettings.copy(
                completedRounds = currentSettings.completedRounds + 1,
                selectedZikirId = 1
            ))
            zikirDao.resetAllZikirs()
            historyDao.deleteAll()
        }
    }

    suspend fun updateZikirTarget(zikirId: Int, newTarget: Long) = zikirMutex.withLock {
        database.withTransaction {
            val zikir = zikirDao.getZikirById(zikirId) ?: return@withTransaction
            val clampedTarget = newTarget.coerceIn(100L, 5000000L)
            val updated = zikir.copy(
                target = clampedTarget,
                completedAt = if (zikir.count >= clampedTarget) zikir.completedAt ?: System.currentTimeMillis() else null
            )
            zikirDao.insert(updated)
        }
    }

    suspend fun updateSettings(settings: AppSettings) {
        settingsDao.insertOrUpdate(settings)
    }
    
    suspend fun getSettingsDirect(): AppSettings? {
        return settingsDao.getSettingsDirect()
    }

    suspend fun restoreBackup(zikirs: List<Zikir>, completedRounds: Int?) = zikirMutex.withLock {
        database.withTransaction {
            zikirDao.insertAll(zikirs)
            if (completedRounds != null) {
                val currentSettings = settingsDao.getSettingsDirect() ?: AppSettings()
                settingsDao.insertOrUpdate(currentSettings.copy(
                    completedRounds = completedRounds.coerceAtLeast(0)
                ))
            }
        }
    }

    suspend fun restoreFullCloudBackup(zikirs: List<Zikir>, settings: AppSettings, slots: List<ReminderSlot>) = zikirMutex.withLock {
        database.withTransaction {
            if (zikirs.isNotEmpty()) {
                zikirDao.insertAll(zikirs)
            }
            if (slots.isNotEmpty()) {
                reminderDao.deleteAll()
                reminderDao.insertAll(slots)
            }
            settingsDao.insertOrUpdate(settings)
        }
    }

    suspend fun restoreFullLocalBackup(
        zikirs: List<Zikir>,
        history: List<ZikirHistory>,
        slots: List<ReminderSlot>,
        settings: AppSettings,
        selectedZikirId: Int
    ) = zikirMutex.withLock {
        database.withTransaction {
            if (zikirs.isNotEmpty()) {
                zikirDao.insertAll(zikirs)
            }
            if (history.isNotEmpty()) {
                historyDao.deleteAll()
                historyDao.insertAll(history)
            }
            if (slots.isNotEmpty()) {
                reminderDao.deleteAll()
                reminderDao.insertAll(slots)
            }
            val finalSettings = settings.copy(
                selectedZikirId = selectedZikirId.coerceIn(1, 15),
                lastActiveTimestamp = System.currentTimeMillis()
            )
            settingsDao.insertOrUpdate(finalSettings)
        }
    }

    suspend fun getAllSlotsList(): List<ReminderSlot> {
        return reminderDao.getAllSlotsList()
    }

    suspend fun addReminderSlot(hour: Int, minute: Int) {
        reminderDao.insert(ReminderSlot(hour = hour, minute = minute, isEnabled = true))
    }

    suspend fun updateReminderSlot(slot: ReminderSlot) {
        reminderDao.update(slot)
    }

    suspend fun removeReminderSlot(id: Long) {
        reminderDao.deleteById(id)
    }
}
