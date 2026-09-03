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
    val lastSyncedAt: Long
)

class SyncManager {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun backupToCloud(
        userId: String,
        zikirs: List<Zikir>,
        history: List<ZikirHistory>,
        slots: List<ReminderSlot>,
        settings: AppSettings
    ): Result<Long> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Kullanıcı kimliği (UID) geçersiz veya boş."))
        }

        return try {
            val now = System.currentTimeMillis()
            val userDoc = firestore.collection("users").document(userId)

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

            val payload = mapOf(
                "userId" to userId,
                "zikirs" to zikirList,
                "slots" to slotList,
                "settings" to settingsMap,
                "updatedAt" to now
            )

            userDoc.set(payload, SetOptions.merge()).await()

            // Backup recent history (last 100 entries)
            val recentHistory = history.takeLast(100).map { h ->
                mapOf(
                    "zikirId" to h.zikirId,
                    "amount" to h.amount,
                    "type" to h.type,
                    "timestamp" to h.timestamp,
                    "dateKey" to h.dateKey
                )
            }
            if (recentHistory.isNotEmpty()) {
                userDoc.collection("history").document("recent").set(
                    mapOf("entries" to recentHistory, "updatedAt" to now)
                ).await()
            }

            Result.success(now)
        } catch (e: Exception) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("SyncManager", "backupToCloud failed", e)
            }
            Result.failure(e)
        }
    }

    suspend fun restoreFromCloud(userId: String): Result<CloudBackupData> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Kullanıcı kimliği (UID) geçersiz veya boş."))
        }

        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            if (!userDoc.exists()) {
                return Result.failure(Exception("Bulutta henüz kayıtlı bir zikir yedeği bulunamadı."))
            }

            val rawZikirs = (userDoc.get("zikirs") as? List<*>)?.filterIsInstance<Map<*, *>>() ?: emptyList()
            val zikirs = rawZikirs.mapNotNull { m ->
                val id = parseNumber(m["id"])?.toInt() ?: return@mapNotNull null
                val target = parseNumber(m["target"])?.toLong() ?: 100000L
                val count = parseNumber(m["count"])?.toLong() ?: 0L
                val startedAtRaw = parseNumber(m["startedAt"])?.toLong()
                val completedAtRaw = parseNumber(m["completedAt"])?.toLong()
                val startedAt = if (startedAtRaw == null || startedAtRaw == 0L) null else startedAtRaw
                val completedAt = if (completedAtRaw == null || completedAtRaw == 0L) null else completedAtRaw
                Zikir(id = id, target = target, count = count, startedAt = startedAt, completedAt = completedAt)
            }

            val rawSlots = (userDoc.get("slots") as? List<*>)?.filterIsInstance<Map<*, *>>() ?: emptyList()
            val slots = rawSlots.mapNotNull { m ->
                val id = parseNumber(m["id"])?.toLong() ?: 0L
                val hour = parseNumber(m["hour"])?.toInt() ?: 9
                val minute = parseNumber(m["minute"])?.toInt() ?: 0
                val isEnabled = parseBoolean(m["isEnabled"], default = true)
                ReminderSlot(id = id, hour = hour, minute = minute, isEnabled = isEnabled)
            }

            val rawSettings = userDoc.get("settings") as? Map<*, *>
            val settings = if (rawSettings != null) {
                AppSettings(
                    id = 1,
                    lang = parseString(rawSettings["lang"], "tr"),
                    themeName = parseString(rawSettings["themeName"], "emerald"),
                    countdownMode = parseBoolean(rawSettings["countdownMode"], false),
                    dailyTarget = parseNumber(rawSettings["dailyTarget"])?.toLong() ?: 10000L,
                    hapticEnabled = parseBoolean(rawSettings["hapticEnabled"], true),
                    fullScreenTap = parseBoolean(rawSettings["fullScreenTap"], false),
                    keepAwakeEnabled = parseBoolean(rawSettings["keepAwakeEnabled"], true),
                    completedRounds = parseNumber(rawSettings["completedRounds"])?.toInt() ?: 0,
                    reminderEnabled = parseBoolean(rawSettings["reminderEnabled"], false),
                    inactivityAlertEnabled = parseBoolean(rawSettings["inactivityAlertEnabled"], false),
                    selectedZikirId = parseNumber(rawSettings["selectedZikirId"])?.toInt() ?: 1,
                    lastActiveTimestamp = parseNumber(rawSettings["lastActiveTimestamp"])?.toLong() ?: System.currentTimeMillis(),
                    counterTexture = parseString(rawSettings["counterTexture"], "geometric"),
                    fontScale = parseNumber(rawSettings["fontScale"])?.toFloat() ?: 1.15f,
                    hapticTapMode = parseString(rawSettings["hapticTapMode"], "light"),
                    hapticMilestoneMode = parseString(rawSettings["hapticMilestoneMode"], "double"),
                    targetReminderEnabled = parseBoolean(rawSettings["targetReminderEnabled"], false),
                    acknowledgedBadges = parseString(rawSettings["acknowledgedBadges"], ""),
                    autoReorderSettings = parseBoolean(rawSettings["autoReorderSettings"], false),
                    settingsUsageStats = parseString(rawSettings["settingsUsageStats"], "{}")
                )
            } else {
                AppSettings()
            }

            val lastSyncedAt = parseNumber(userDoc.get("updatedAt"))?.toLong() ?: System.currentTimeMillis()

            Result.success(
                CloudBackupData(
                    zikirs = zikirs,
                    settings = settings,
                    reminderSlots = slots,
                    lastSyncedAt = lastSyncedAt
                )
            )
        } catch (e: Exception) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("SyncManager", "restoreFromCloud failed", e)
            }
            Result.failure(e)
        }
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
