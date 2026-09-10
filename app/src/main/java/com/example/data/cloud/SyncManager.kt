package com.example.data.cloud

import android.util.Log
import com.example.data.model.AppSettings
import com.example.data.model.ReminderSlot
import com.example.data.model.Zikir
import com.example.data.model.ZikirHistory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class CloudBackupData(
    val zikirs: List<Zikir>,
    val settings: AppSettings,
    val reminderSlots: List<ReminderSlot>,
    val history: List<ZikirHistory>,
    val syncMetadata: SyncMetadata?,
    val lastSyncedAt: Long
)

class SyncManager {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun backupToCloud(
        userId: String,
        zikirs: List<Zikir>,
        history: List<ZikirHistory>,
        slots: List<ReminderSlot>,
        settings: AppSettings,
        localRevision: Long,
        deviceId: String
    ): Result<Long> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Kullanıcı kimliği (UID) geçersiz veya boş."))
        }

        return try {
            val now = System.currentTimeMillis()
            val userDoc = firestore.collection("users").document(userId)

            // Conflict Check
            val currentSnapshot = userDoc.get().await()
            if (currentSnapshot.exists()) {
                val remoteMetadata = currentSnapshot.get("syncMetadata") as? Map<*, *>
                val remoteRevision = parseNumber(remoteMetadata?.get("revision"))?.toLong() ?: 0L
                if (remoteRevision > localRevision) {
                    throw SyncConflictException(remoteRevision)
                }
            }

            val newRevision = localRevision + 1

            // 1. snapshotId oluştur
            val snapshotId = "snap_${now}_${java.util.UUID.randomUUID().toString().replace("-", "").take(8)}"

            val zikirList = zikirs.map { z ->
                mapOf(
                    "id" to z.id,
                    "target" to z.target,
                    "count" to z.count,
                    "startedAt" to (z.startedAt ?: 0L),
                    "completedAt" to (z.completedAt ?: 0L)
                )
            }

            val slotList = slots.map { s ->
                mapOf(
                    "id" to s.id,
                    "hour" to s.hour,
                    "minute" to s.minute,
                    "isEnabled" to s.isEnabled
                )
            }

            val settingsMap = mapOf(
                "selectedZikirId" to settings.selectedZikirId,
                "countdownMode" to settings.countdownMode,
                "dailyTarget" to settings.dailyTarget,
                "hapticEnabled" to settings.hapticEnabled,
                "fullScreenTap" to settings.fullScreenTap,
                "keepAwakeEnabled" to settings.keepAwakeEnabled,
                "reminderEnabled" to settings.reminderEnabled,
                "inactivityAlertEnabled" to settings.inactivityAlertEnabled,
                "lang" to settings.lang,
                "themeName" to settings.themeName,
                "fontScale" to settings.fontScale.toDouble(),
                "counterTexture" to settings.counterTexture,
                "completedRounds" to settings.completedRounds,
                "hapticTapMode" to settings.hapticTapMode,
                "hapticMilestoneMode" to settings.hapticMilestoneMode,
                "targetReminderEnabled" to settings.targetReminderEnabled,
                "acknowledgedBadges" to settings.acknowledgedBadges,
                "autoReorderSettings" to settings.autoReorderSettings,
                "settingsUsageStats" to settings.settingsUsageStats,
                "lastActiveTimestamp" to settings.lastActiveTimestamp,
                "lastSyncedAt" to now
            )

            val snapshotRef = userDoc.collection("snapshots").document(snapshotId)
            val historyRef = snapshotRef.collection("history")

            // 2. Tüm history chunk'larını snapshotId altında yaz
            val historyChunks = history.chunked(500)
            if (historyChunks.isEmpty()) {
                historyRef.document("meta").set(
                    mapOf(
                        "snapshotId" to snapshotId,
                        "chunkCount" to 0,
                        "updatedAt" to now
                    )
                ).await()
            } else {
                historyChunks.forEachIndexed { index, chunk ->
                    val chunkMap = chunk.map { h ->
                        mapOf(
                            "eventId" to h.eventId,
                            "zikirId" to h.zikirId,
                            "amount" to h.amount,
                            "type" to h.type,
                            "timestamp" to h.timestamp,
                            "dateKey" to h.dateKey
                        )
                    }
                    historyRef.document("chunk_$index").set(
                        mapOf(
                            "snapshotId" to snapshotId,
                            "chunkIndex" to index,
                            "entries" to chunkMap,
                            "updatedAt" to now
                        )
                    ).await()
                }
                historyRef.document("meta").set(
                    mapOf(
                        "snapshotId" to snapshotId,
                        "chunkCount" to historyChunks.size,
                        "updatedAt" to now
                    )
                ).await()
            }

            // 3. Tüm zikir/settings/slots/meta verisini aynı snapshotId ile yaz
            val snapshotPayload = mapOf(
                "snapshotId" to snapshotId,
                "userId" to userId,
                "zikirs" to zikirList,
                "slots" to slotList,
                "settings" to settingsMap,
                "chunkCount" to historyChunks.size,
                "syncMetadata" to mapOf(
                    "revision" to newRevision,
                    "updatedAt" to now,
                    "deviceId" to deviceId
                ),
                "updatedAt" to now
            )
            snapshotRef.set(snapshotPayload).await()

            // 4. Bütün parçaların başarıyla yazıldığını doğrula
            val verifySnapshotDoc = snapshotRef.get().await()
            if (!verifySnapshotDoc.exists()) {
                throw IllegalStateException("Snapshot write verification failed: snapshot document does not exist.")
            }
            val verifyMetaDoc = historyRef.document("meta").get().await()
            if (!verifyMetaDoc.exists()) {
                throw IllegalStateException("Snapshot write verification failed: history meta document does not exist.")
            }
            if (historyChunks.isNotEmpty()) {
                for (index in historyChunks.indices) {
                    val verifyChunk = historyRef.document("chunk_$index").get().await()
                    if (!verifyChunk.exists()) {
                        throw IllegalStateException("Snapshot write verification failed: chunk_$index does not exist.")
                    }
                }
            }

            // 5. En son parent user document üzerindeki currentSnapshotId/currentRevision pointer'ını atomik olarak güncelle
            val pointerPayload = mapOf(
                "currentSnapshotId" to snapshotId,
                "currentRevision" to newRevision,
                "updatedAt" to now,
                "lastSyncedAt" to now,
                "syncMetadata" to mapOf(
                    "revision" to newRevision,
                    "updatedAt" to now,
                    "deviceId" to deviceId
                ),
                // Geriye dönük uyumluluk için kök alanları da senkronize tut
                "zikirs" to zikirList,
                "slots" to slotList,
                "settings" to settingsMap
            )
            userDoc.set(pointerPayload, SetOptions.merge()).await()

            // 6. Eski snapshot'lar başarıyla yeni snapshot pointer'ı aktif olduktan sonra garbage collection ile temizlenir
            cleanupOldSnapshots(userDoc, snapshotId)

            Result.success(now)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (com.example.BuildConfig.DEBUG) {
                Log.e("SyncManager", "backupToCloud failed", e)
            }
            Result.failure(e)
        }
    }

    private suspend fun cleanupOldSnapshots(userDocRef: com.google.firebase.firestore.DocumentReference, currentSnapshotId: String) {
        try {
            val snapshotsList = userDocRef.collection("snapshots").get().await()
            for (snap in snapshotsList.documents) {
                if (snap.id != currentSnapshotId) {
                    val oldHistoryChunks = snap.reference.collection("history").get().await()
                    for (c in oldHistoryChunks.documents) {
                        try {
                            c.reference.delete().await()
                        } catch (_: Exception) {}
                    }
                    try {
                        snap.reference.delete().await()
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {
            // Garbage collection best-effort, snapshot isolation is preserved
        }
    }

    suspend fun restoreFromCloud(userId: String): Result<CloudBackupData> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Kullanıcı kimliği (UID) geçersiz veya boş."))
        }

        return try {
            val userDocRef = firestore.collection("users").document(userId)
            val userDoc = userDocRef.get().await()
            if (!userDoc.exists()) {
                return Result.failure(Exception("Bulutta henüz kayıtlı bir zikir yedeği bulunamadı."))
            }

            // Versioned snapshot pointer kontrolü
            val currentSnapshotId = parseString(userDoc.get("currentSnapshotId"), "")
            val sourceDoc = if (currentSnapshotId.isNotBlank()) {
                val snapDoc = userDocRef.collection("snapshots").document(currentSnapshotId).get().await()
                if (!snapDoc.exists()) {
                    return Result.failure(CloudDataCorruptionException("Snapshot inconsistency: Active snapshot $currentSnapshotId not found."))
                }
                snapDoc
            } else {
                userDoc
            }

            val rawZikirs = (sourceDoc.get("zikirs") as? List<*>)?.filterIsInstance<Map<*, *>>() ?: emptyList()

            val zikirs = rawZikirs.map { m ->
                val id = parseIntStrict(m["id"], "zikirId")
                val target = parseLongStrict(m["target"], "target")
                val count = parseLongStrict(m["count"], "count")
                val startedAt = if (m["startedAt"] != null && m["startedAt"] != "null") parseLongStrict(m["startedAt"], "startedAt") else null
                val completedAt = if (m["completedAt"] != null && m["completedAt"] != "null") parseLongStrict(m["completedAt"], "completedAt") else null
                try {
                    com.example.data.model.DhikrDataValidator.validateZikirStrict(
                        Zikir(id = id, target = target, count = count, startedAt = startedAt, completedAt = completedAt)
                    )
                } catch (e: IllegalArgumentException) {
                    throw CloudDataCorruptionException(e.message ?: "Invalid zikir data in cloud")
                }
            }

            val rawSlots = (sourceDoc.get("slots") as? List<*>)?.filterIsInstance<Map<*, *>>() ?: emptyList()
            val slots = rawSlots.map { m ->
                val id = parseLongStrict(m["id"], "slot id")
                val hour = parseIntStrict(m["hour"], "slot hour")
                val minute = parseIntStrict(m["minute"], "slot minute")
                val isEnabled = parseBoolean(m["isEnabled"], default = true)
                try {
                    com.example.data.model.DhikrDataValidator.validateReminderSlotStrict(
                        ReminderSlot(id = id, hour = hour, minute = minute, isEnabled = isEnabled)
                    )
                } catch (e: IllegalArgumentException) {
                    throw CloudDataCorruptionException(e.message ?: "Invalid reminder slot data in cloud")
                }
            }

            val rawSettings = sourceDoc.get("settings") as? Map<*, *>
            val settings = if (rawSettings != null) {
                try {
                    com.example.data.model.DhikrDataValidator.validateSettingsStrict(
                        AppSettings(
                            id = 1,
                            lang = parseString(rawSettings["lang"], "tr"),
                            themeName = parseString(rawSettings["themeName"], "emerald"),
                            countdownMode = parseBoolean(rawSettings["countdownMode"], false),
                            dailyTarget = if (rawSettings.containsKey("dailyTarget") && rawSettings["dailyTarget"] != null) parseLongStrict(rawSettings["dailyTarget"], "dailyTarget") else 10000L,
                            hapticEnabled = parseBoolean(rawSettings["hapticEnabled"], true),
                            fullScreenTap = parseBoolean(rawSettings["fullScreenTap"], false),
                            keepAwakeEnabled = parseBoolean(rawSettings["keepAwakeEnabled"], true),
                            completedRounds = if (rawSettings.containsKey("completedRounds") && rawSettings["completedRounds"] != null) parseIntStrict(rawSettings["completedRounds"], "completedRounds") else 0,
                            reminderEnabled = parseBoolean(rawSettings["reminderEnabled"], false),
                            inactivityAlertEnabled = parseBoolean(rawSettings["inactivityAlertEnabled"], false),
                            selectedZikirId = if (rawSettings.containsKey("selectedZikirId") && rawSettings["selectedZikirId"] != null) parseIntStrict(rawSettings["selectedZikirId"], "selectedZikirId") else 1,
                            lastActiveTimestamp = if (rawSettings.containsKey("lastActiveTimestamp") && rawSettings["lastActiveTimestamp"] != null) parseLongStrict(rawSettings["lastActiveTimestamp"], "lastActiveTimestamp") else System.currentTimeMillis(),
                            counterTexture = parseString(rawSettings["counterTexture"], "geometric"),
                            fontScale = parseNumber(rawSettings["fontScale"])?.toFloat() ?: 1.15f,
                            hapticTapMode = parseString(rawSettings["hapticTapMode"], "light"),
                            hapticMilestoneMode = parseString(rawSettings["hapticMilestoneMode"], "double"),
                            targetReminderEnabled = parseBoolean(rawSettings["targetReminderEnabled"], false),
                            acknowledgedBadges = parseString(rawSettings["acknowledgedBadges"], ""),
                            autoReorderSettings = parseBoolean(rawSettings["autoReorderSettings"], false),
                            settingsUsageStats = parseString(rawSettings["settingsUsageStats"], "{}")
                        )
                    )
                } catch (e: IllegalArgumentException) {
                    throw CloudDataCorruptionException(e.message ?: "Invalid settings data in cloud")
                }
            } else {
                AppSettings()
            }
            val lastSyncedAt = parseLongStrict(sourceDoc.get("updatedAt"), "updatedAt")
            
            val remoteMetadata = sourceDoc.get("syncMetadata") as? Map<*, *>
            val syncMetadata = if (remoteMetadata != null) {
                SyncMetadata(
                    revision = parseLongStrict(remoteMetadata["revision"], "revision"),
                    updatedAt = parseLongStrict(remoteMetadata["updatedAt"], "updatedAt"),
                    deviceId = parseString(remoteMetadata["deviceId"], "")
                )
            } else null

            val restoredHistory = mutableListOf<ZikirHistory>()
            val historyCollection = if (currentSnapshotId.isNotBlank()) {
                userDocRef.collection("snapshots").document(currentSnapshotId).collection("history")
            } else {
                userDocRef.collection("history")
            }
            val historyMetaDoc = historyCollection.document("meta").get().await()
            
            if (historyMetaDoc.exists()) {
                val metaUpdatedAt = parseLongStrict(historyMetaDoc.get("updatedAt"), "history meta updatedAt")
                val expectedChunkCount = parseIntStrict(historyMetaDoc.get("chunkCount"), "chunkCount")
                
                if (lastSyncedAt != metaUpdatedAt) {
                    return Result.failure(CloudDataCorruptionException("Snapshot inconsistency: User document and history meta timestamps do not match."))
                }

                if (expectedChunkCount < 0) {
                    return Result.failure(CloudDataCorruptionException("Snapshot inconsistency: Invalid negative chunkCount $expectedChunkCount."))
                }

                if (expectedChunkCount > 0) {
                    val historyDocs = historyCollection.get().await()
                    val docsMap = historyDocs.documents.associateBy { it.id }

                    // Check for extra chunks with matching updatedAt (inconsistent chunkCount vs actual documents)
                    for (doc in historyDocs.documents) {
                        if (doc.id == "meta") continue
                        if (doc.id.startsWith("chunk_")) {
                            val chunkIndexStr = doc.id.removePrefix("chunk_")
                            val chunkIndex = chunkIndexStr.toIntOrNull()
                            if (chunkIndex == null || chunkIndex !in 0 until expectedChunkCount) {
                                val chunkUpdatedAt = (doc.get("updatedAt") as? Number)?.toLong()
                                if (chunkUpdatedAt == lastSyncedAt) {
                                    return Result.failure(CloudDataCorruptionException("Snapshot inconsistency: Unexpected chunk ${doc.id} with current snapshot timestamp found."))
                                }
                            }
                        }
                    }

                    // Enforce exact contiguous chunks: chunk_0, chunk_1, ..., chunk_(N-1) without holes
                    for (index in 0 until expectedChunkCount) {
                        val chunkId = "chunk_$index"
                        val doc = docsMap[chunkId]
                            ?: return Result.failure(CloudDataCorruptionException("Snapshot inconsistency: Missing $chunkId (hole detected). Expected exactly $expectedChunkCount chunks (chunk_0..chunk_${expectedChunkCount - 1})."))

                        val chunkUpdatedAt = parseLongStrict(doc.get("updatedAt"), "chunk updatedAt")
                        if (chunkUpdatedAt != lastSyncedAt) {
                            return Result.failure(CloudDataCorruptionException("Snapshot inconsistency: Chunk $chunkId timestamp ($chunkUpdatedAt) does not match snapshot timestamp ($lastSyncedAt)."))
                        }
                        
                        val entries = (doc.get("entries") as? List<*>)?.filterIsInstance<Map<*, *>>()
                            ?: return Result.failure(CloudDataCorruptionException("Snapshot inconsistency: Chunk $chunkId entries are missing or malformed."))

                        val chunkHistory = entries.map { m ->
                            val eventId = parseString(m["eventId"], "").ifBlank { java.util.UUID.randomUUID().toString() }
                            val zikirId = parseIntStrict(m["zikirId"], "zikirId")
                            val amount = parseLongStrict(m["amount"], "amount")
                            val type = parseString(m["type"], "add")
                            val timestamp = parseLongStrict(m["timestamp"], "timestamp")
                            val dateKey = parseString(m["dateKey"], "")
                            try {
                                com.example.data.model.DhikrDataValidator.validateHistoryStrict(
                                    ZikirHistory(eventId = eventId, zikirId = zikirId, amount = amount, type = type, timestamp = timestamp, dateKey = dateKey)
                                )
                            } catch (e: IllegalArgumentException) {
                                throw CloudDataCorruptionException(e.message ?: "Invalid history entry in cloud")
                            }
                        }
                        restoredHistory.addAll(chunkHistory)
                    }
                }
            } else {
                return Result.failure(CloudDataCorruptionException("Snapshot inconsistency: History meta document missing."))
            }

            try {
                com.example.data.model.DhikrDataValidator.validateFullSnapshotStrict(
                    zikirs = zikirs,
                    history = restoredHistory,
                    slots = slots,
                    settings = settings
                )
            } catch (e: IllegalArgumentException) {
                throw CloudDataCorruptionException(e.message ?: "Invalid snapshot structure in cloud")
            }

            Result.success(
                CloudBackupData(
                    zikirs = zikirs,
                    settings = settings,
                    reminderSlots = slots,
                    history = restoredHistory,
                    syncMetadata = syncMetadata,
                    lastSyncedAt = lastSyncedAt
                )
            )
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (com.example.BuildConfig.DEBUG) {
                Log.e("SyncManager", "restoreFromCloud failed", e)
            }
            Result.failure(e)
        }
    }

    private fun parseLongStrict(value: Any?, fieldName: String): Long {
        if (value == null) {
            throw CloudDataCorruptionException("Corrupted cloud data: Missing $fieldName")
        }
        val doubleVal = when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: throw CloudDataCorruptionException("Corrupted cloud data: Invalid numeric format for $fieldName ($value)")
            else -> throw CloudDataCorruptionException("Corrupted cloud data: Invalid type for $fieldName (${value::class.java.simpleName})")
        }

        if (doubleVal.isNaN() || doubleVal.isInfinite()) {
            throw CloudDataCorruptionException("Corrupted cloud data: Invalid numeric value for $fieldName ($doubleVal)")
        }

        val longVal = doubleVal.toLong()
        if (doubleVal != longVal.toDouble()) {
            throw CloudDataCorruptionException("Corrupted cloud data: $fieldName must be an integer without fractional part, got $value")
        }
        return longVal
    }

    private fun parseIntStrict(value: Any?, fieldName: String): Int {
        val longVal = parseLongStrict(value, fieldName)
        if (longVal < Int.MIN_VALUE || longVal > Int.MAX_VALUE) {
            throw CloudDataCorruptionException("Corrupted cloud data: $fieldName out of Int range ($longVal)")
        }
        return longVal.toInt()
    }

    private fun parseNumber(value: Any?): Number? {
        return when (value) {
            is Number -> value
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }

    private fun parseBoolean(value: Any?, default: Boolean): Boolean {
        return when (value) {
            is Boolean -> value
            is String -> value.toBooleanStrictOrNull() ?: default
            is Number -> value.toInt() != 0
            else -> default
        }
    }

    private fun parseString(value: Any?, default: String): String {
        return when (value) {
            is String -> value
            null -> default
            else -> value.toString()
        }
    }
}
