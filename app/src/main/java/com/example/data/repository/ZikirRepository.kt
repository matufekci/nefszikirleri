package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.HistoryDao
import com.example.data.local.ReminderDao
import com.example.data.local.SettingsDao
import com.example.data.local.ZikirDao
import com.example.data.model.AppSettings
import com.example.data.model.DailyAggregate
import com.example.data.model.ReminderSlot
import com.example.data.model.Zikir
import com.example.data.model.ZikirContent
import com.example.data.model.ZikirHistory
import com.example.data.model.PendingOperation
import com.example.util.NumberFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ZikirRepository(
    private val database: AppDatabase,
    private val zikirDao: ZikirDao = database.zikirDao(),
    private val historyDao: HistoryDao = database.historyDao(),
    private val reminderDao: ReminderDao = database.reminderDao(),
    private val settingsDao: SettingsDao = database.settingsDao(),
    private val pendingOperationDao: com.example.data.local.PendingOperationDao = database.pendingOperationDao()
) {
    private val zikirMutex = Mutex()
    private val globalFence = java.util.concurrent.atomic.AtomicLong(0L)
    private val zikirFences = java.util.concurrent.ConcurrentHashMap<Int, Long>()

    fun getEffectiveFence(zikirId: Int): Long {
        val zFence = zikirFences[zikirId] ?: 0L
        val gFence = globalFence.get()
        return maxOf(zFence, gFence)
    }

    suspend fun createPendingOperation(op: PendingOperation) {
        val effectiveFence = getEffectiveFence(op.zikirId)
        if (op.timestamp <= effectiveFence) {
            // Fenced out: mark cancelled immediately so it can never be applied
            pendingOperationDao.insert(op.copy(status = "cancelled"))
        } else {
            pendingOperationDao.insert(op)
        }
    }

    suspend fun processUnappliedOperations() {
        val unapplied = pendingOperationDao.getUnappliedOperations()
        if (unapplied.isEmpty()) return
        applyBatchOperations(unapplied)
    }

    suspend fun applyBatchOperations(batch: List<PendingOperation>) = zikirMutex.withLock {
        database.withTransaction {
            val opIds = batch.map { it.operationId }
            val opsInDb = pendingOperationDao.getOperationsByIds(opIds).associateBy { it.operationId }
            
            val validOps = mutableListOf<PendingOperation>()
            val opsToCancel = mutableListOf<String>()

            for (op in batch) {
                val dbOp = opsInDb[op.operationId] ?: op
                if (dbOp.status == "applied" || dbOp.status == "cancelled") {
                    continue
                }

                val effectiveFence = getEffectiveFence(op.zikirId)
                if (op.timestamp <= effectiveFence) {
                    opsToCancel.add(op.operationId)
                } else {
                    validOps.add(op)
                }
            }

            if (opsToCancel.isNotEmpty()) {
                pendingOperationDao.updateStatus(opsToCancel, "cancelled")
            }
            
            if (validOps.isEmpty()) return@withTransaction
            
            val validIds = validOps.map { it.operationId }
            pendingOperationDao.updateStatus(validIds, "pending")
            
            val grouped = validOps.groupBy { it.zikirId }
            for ((zikirId, ops) in grouped) {
                val totalAmount = ops.sumOf { it.amount }
                if (totalAmount > 0) {
                    val lastOp = ops.last()
                    zikirDao.incrementZikirCount(zikirId, totalAmount, lastOp.timestamp)
                    
                    val historyEntry = ZikirHistory(
                        eventId = lastOp.operationId,
                        zikirId = zikirId,
                        amount = totalAmount,
                        type = "add",
                        timestamp = lastOp.timestamp,
                        dateKey = lastOp.dateKey
                    )
                    historyDao.insert(historyEntry)
                }
            }
            
            // update settings last active timestamp
            val now = com.example.util.MonotonicTime.now()
            val currentSettings = settingsDao.getSettingsDirect() ?: AppSettings()
            settingsDao.insertOrUpdate(currentSettings.copy(lastActiveTimestamp = now))
            
            pendingOperationDao.updateStatus(validIds, "applied")
        }
    }

    val allZikirs: Flow<List<Zikir>> = zikirDao.getAllZikirs()
    val allHistory: Flow<List<ZikirHistory>> = historyDao.getAllHistory()
    val recentHistory: Flow<List<ZikirHistory>> = historyDao.observeRecentHistory(50)
    val distinctActiveDates: Flow<List<String>> = historyDao.observeDistinctActiveDates()
    val totalRecited: Flow<Long> = historyDao.observeTotalRecited()
    val allSlots: Flow<List<ReminderSlot>> = reminderDao.getAllSlots()
    val settings: Flow<AppSettings?> = settingsDao.getSettings()

    fun observeDailyStats(fromTimestamp: Long): Flow<List<DailyAggregate>> =
        historyDao.observeDailyStats(fromTimestamp)

    fun observeDailyStatsForZikir(zikirId: Int, fromTimestamp: Long): Flow<List<DailyAggregate>> =
        historyDao.observeDailyStatsForZikir(zikirId, fromTimestamp)

    fun observeTodayRecitedForZikir(zikirId: Int, dateKey: String): Flow<Long> =
        historyDao.observeTodayRecitedForZikir(zikirId, dateKey)

    fun observe30DayAverage(fromTimestamp: Long): Flow<Long> =
        historyDao.observe30DayAverage(fromTimestamp)

    suspend fun getAllHistoryDirect(): List<ZikirHistory> =
        getAllHistoryInChunksDirect()

    suspend fun getAllHistoryInChunksDirect(chunkSize: Int = 2000): List<ZikirHistory> = database.withTransaction {
        val totalCount = historyDao.getHistoryCountDirect()
        if (totalCount <= chunkSize) {
            return@withTransaction historyDao.getAllHistoryDirect()
        }
        val result = ArrayList<ZikirHistory>(totalCount)
        var offset = 0
        while (offset < totalCount) {
            val chunk = historyDao.getHistoryPagedDirect(limit = chunkSize, offset = offset)
            if (chunk.isEmpty()) break
            result.addAll(chunk)
            offset += chunk.size
        }
        result
    }

    suspend fun getAllHistoryInChunksDirectInternal(chunkSize: Int = 2000): List<ZikirHistory> {
        // Internal non-transactional version for use inside existing transactions
        val totalCount = historyDao.getHistoryCountDirect()
        if (totalCount <= chunkSize) {
            return historyDao.getAllHistoryDirect()
        }
        val result = ArrayList<ZikirHistory>(totalCount)
        var offset = 0
        while (offset < totalCount) {
            val chunk = historyDao.getHistoryPagedDirect(limit = chunkSize, offset = offset)
            if (chunk.isEmpty()) break
            result.addAll(chunk)
            offset += chunk.size
        }
        return result
    }

    data class BackupSnapshot(
        val zikirs: List<Zikir>,
        val history: List<ZikirHistory>,
        val slots: List<ReminderSlot>,
        val settings: AppSettings
    )

    suspend fun getAtomicSnapshot(): BackupSnapshot {
        // Ensure pending increments are applied before snapshot to avoid count/history mismatch
        processUnappliedOperations()
        return database.withTransaction {
            val zikirs = zikirDao.getAllZikirsDirect()
            val history = getAllHistoryInChunksDirectInternal()
            val slots = reminderDao.getAllSlotsList()
            val settings = settingsDao.getSettingsDirect() ?: AppSettings()
            
            BackupSnapshot(
                zikirs = zikirs,
                history = history,
                slots = slots,
                settings = settings
            )
        }
    }

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
        } else {
            var needsUpdate = false
            var updated = currentSettings
            // FontScale migration: very old default 1.0 -> 1.15
            if (currentSettings.fontScale <= 1.05f) {
                updated = updated.copy(fontScale = 1.15f)
                needsUpdate = true
            }
            // Theme normalization: legacy -> canonical
            try {
                val normalized = com.example.ui.theme.AppPalettes.normalizeId(currentSettings.themeName)
                if (normalized != currentSettings.themeName) {
                    updated = updated.copy(themeName = normalized)
                    needsUpdate = true
                }
            } catch (_: Exception) {
                // If normalize fails, fallback to canonical default
                if (currentSettings.themeName != "hadra_gece") {
                    updated = updated.copy(themeName = "hadra_gece")
                    needsUpdate = true
                }
            }
            if (needsUpdate) {
                settingsDao.insertOrUpdate(updated)
            }
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
                dateKey = NumberFormatter.getDateKey(now)
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
            val fenceTime = com.example.util.MonotonicTime.now()
            zikirFences[zikirId] = fenceTime
            pendingOperationDao.cancelPendingForZikir(zikirId)

            // 1. SQLite sayım azalışı
            zikirDao.decrementZikirCount(zikirId, amount)
            
            // 2. Güncel durumu oku
            val zikir = zikirDao.getZikirById(zikirId) ?: return@withTransaction 0L

            val historyEntry = ZikirHistory(
                zikirId = zikirId,
                amount = amount,
                type = "remove",
                timestamp = fenceTime,
                dateKey = NumberFormatter.getDateKey(fenceTime)
            )
            historyDao.insert(historyEntry)

            zikir.count
        }
    }
    
    suspend fun undoLastAction(zikirId: Int): Long? = zikirMutex.withLock {
        database.withTransaction {
            val fenceTime = com.example.util.MonotonicTime.now()
            zikirFences[zikirId] = fenceTime
            pendingOperationDao.cancelPendingForZikir(zikirId)

            val recentHistory = historyDao.getMostRecentForZikir(zikirId)
            if (recentHistory != null) {
                historyDao.deleteById(recentHistory.id)
                if (recentHistory.type == "add") {
                    zikirDao.decrementZikirCount(zikirId, recentHistory.amount)
                } else if (recentHistory.type == "remove") {
                    zikirDao.incrementZikirCount(zikirId, recentHistory.amount, fenceTime)
                }
                return@withTransaction zikirDao.getZikirById(zikirId)?.count
            }
            null
        }
    }

    suspend fun resetSingleZikir(zikirId: Int) = zikirMutex.withLock {
        database.withTransaction {
            val fenceTime = com.example.util.MonotonicTime.now()
            zikirFences[zikirId] = fenceTime
            pendingOperationDao.cancelPendingForZikir(zikirId)

            zikirDao.resetZikir(zikirId)
            historyDao.deleteForZikir(zikirId)
        }
    }

    suspend fun resetAllZikirs() = zikirMutex.withLock {
        database.withTransaction {
            val fenceTime = com.example.util.MonotonicTime.now()
            globalFence.set(fenceTime)
            for (i in 1..15) { zikirFences[i] = fenceTime }
            pendingOperationDao.cancelAllPending()

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
                        dateKey = NumberFormatter.getDateKey(now)
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
            val fenceTime = com.example.util.MonotonicTime.now()
            globalFence.set(fenceTime)
            for (i in 1..15) { zikirFences[i] = fenceTime }
            pendingOperationDao.cancelAllPending()

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
        com.example.data.model.DhikrDataValidator.validateFullSnapshotStrict(
            zikirs = zikirs
        )
        database.withTransaction {
            val fenceTime = com.example.util.MonotonicTime.now()
            globalFence.set(fenceTime)
            for (i in 1..15) { zikirFences[i] = fenceTime }
            pendingOperationDao.cancelAllPending()

            zikirDao.replaceSnapshot(zikirs)
            if (completedRounds != null) {
                val currentSettings = settingsDao.getSettingsDirect() ?: AppSettings()
                settingsDao.insertOrUpdate(currentSettings.copy(
                    completedRounds = completedRounds.coerceAtLeast(0)
                ))
            }
        }
    }

    suspend fun restoreFullCloudBackup(zikirs: List<Zikir>, settings: AppSettings, slots: List<ReminderSlot>, history: List<ZikirHistory>) = zikirMutex.withLock {
        // Restore başlamadan önce tüm snapshot doğrulanmalı (fail-fast, no database modification)
        com.example.data.model.DhikrDataValidator.validateFullSnapshotStrict(
            zikirs = zikirs,
            history = history,
            slots = slots,
            settings = settings
        )

        database.withTransaction {
            val fenceTime = com.example.util.MonotonicTime.now()
            globalFence.set(fenceTime)
            for (i in 1..15) { zikirFences[i] = fenceTime }
            pendingOperationDao.cancelAllPending()

            // Önce tüm verileri temizle (snapshot restore semantiği)
            reminderDao.deleteAll()
            historyDao.deleteAll()
            
            // Sonra yeni verileri yaz - theme normalize
            val normalizedSettings = try {
                val nid = com.example.ui.theme.AppPalettes.normalizeId(settings.themeName)
                settings.copy(themeName = nid)
            } catch (_: Exception) { settings.copy(themeName = "hadra_gece") }
            zikirDao.replaceSnapshot(zikirs)
            historyDao.insertAll(history)
            reminderDao.insertAll(slots)
            settingsDao.insertOrUpdate(normalizedSettings)
        }
    }

    suspend fun restoreFullLocalBackup(
        zikirs: List<Zikir>,
        history: List<ZikirHistory>,
        slots: List<ReminderSlot>,
        settings: AppSettings,
        selectedZikirId: Int
    ) = zikirMutex.withLock {
        // Restore başlamadan önce tüm snapshot doğrulanmalı (fail-fast, no database modification)
        com.example.data.model.DhikrDataValidator.validateFullSnapshotStrict(
            zikirs = zikirs,
            history = history,
            slots = slots,
            settings = settings
        )

        database.withTransaction {
            val fenceTime = com.example.util.MonotonicTime.now()
            globalFence.set(fenceTime)
            for (i in 1..15) { zikirFences[i] = fenceTime }
            pendingOperationDao.cancelAllPending()

            // Önce tüm tabloları temizle (snapshot restore semantiği)
            historyDao.deleteAll()
            reminderDao.deleteAll()
            
            // Sonra verileri yaz - theme normalize
            val normalizedTheme = try {
                com.example.ui.theme.AppPalettes.normalizeId(settings.themeName)
            } catch (_: Exception) { "hadra_gece" }
            zikirDao.replaceSnapshot(zikirs)
            historyDao.insertAll(history)
            reminderDao.insertAll(slots)
            
            val finalSettings = settings.copy(
                themeName = normalizedTheme,
                selectedZikirId = selectedZikirId.coerceIn(1, 15),
                lastActiveTimestamp = fenceTime
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
