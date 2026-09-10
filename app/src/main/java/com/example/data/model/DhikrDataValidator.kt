package com.example.data.model

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DhikrDataValidator {
    private const val TAG = "DhikrDataValidator"

    fun validateZikir(zikir: Zikir): Zikir {
        val count = zikir.count.coerceAtLeast(0L)
        val target = zikir.target.coerceIn(100L, 5000000L)
        
        var startedAt = zikir.startedAt
        if (startedAt != null && startedAt <= 0L) startedAt = null
        if (startedAt == null && count > 0) startedAt = System.currentTimeMillis()

        var completedAt = zikir.completedAt
        if (completedAt != null && completedAt <= 0L) completedAt = null
        if (completedAt == null && count >= target) completedAt = System.currentTimeMillis()

        if (zikir.count != count || zikir.target != target) {
            if (com.example.BuildConfig.DEBUG) {
                Log.d(TAG, "Zikir (ID=${zikir.id}) alanları clamp edildi: count=$count, target=$target")
            }
        }

        return Zikir(
            id = zikir.id.coerceIn(1, 15),
            target = target,
            count = count,
            startedAt = startedAt,
            completedAt = completedAt
        )
    }

    fun validateSettings(settings: AppSettings): AppSettings {
        val dailyTarget = settings.dailyTarget.coerceAtLeast(1L)
        val completedRounds = settings.completedRounds.coerceAtLeast(0)
        val fontScale = settings.fontScale.coerceIn(0.7f, 1.5f)
        val selectedZikirId = settings.selectedZikirId.coerceIn(1, 15)

        if (settings.dailyTarget != dailyTarget || settings.completedRounds != completedRounds || settings.fontScale != fontScale || settings.selectedZikirId != selectedZikirId) {
            if (com.example.BuildConfig.DEBUG) {
                Log.d(TAG, "Settings alanları clamp edildi.")
            }
        }

        return settings.copy(
            dailyTarget = dailyTarget,
            completedRounds = completedRounds,
            fontScale = fontScale,
            selectedZikirId = selectedZikirId
        )
    }

    fun validateReminderSlot(slot: ReminderSlot): ReminderSlot {
        val hour = slot.hour.coerceIn(0, 23)
        val minute = slot.minute.coerceIn(0, 59)
        
        if (slot.hour != hour || slot.minute != minute) {
            if (com.example.BuildConfig.DEBUG) {
                Log.d(TAG, "ReminderSlot alanları clamp edildi: hour=$hour, minute=$minute")
            }
        }

        return slot.copy(
            hour = hour,
            minute = minute
        )
    }

    fun validateHistory(history: ZikirHistory): ZikirHistory {
        val eventId = history.eventId.ifBlank { java.util.UUID.randomUUID().toString() }
        val zikirId = history.zikirId.coerceIn(1, 15)
        val amount = history.amount.coerceIn(1L, 5000000L)
        val type = if (history.type == "remove") "remove" else "add"
        val timestamp = if (history.timestamp > 0) history.timestamp else System.currentTimeMillis()
        val dateKey = history.dateKey.ifBlank {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
        }

        if (history.zikirId != zikirId || history.amount != amount || history.type != type || history.timestamp != timestamp) {
            if (com.example.BuildConfig.DEBUG) {
                Log.d(TAG, "ZikirHistory alanları clamp edildi.")
            }
        }

        return history.copy(
            id = 0, // ID should be auto-generated in DB
            eventId = eventId,
            zikirId = zikirId,
            amount = amount,
            type = type,
            timestamp = timestamp,
            dateKey = dateKey
        )
    }

    // STRICT 15-ZIKIR SNAPSHOT VALIDATION (FOR CLOUD & LOCAL RESTORE)
    fun validateFullSnapshotStrict(
        zikirs: List<Zikir>,
        history: List<ZikirHistory> = emptyList(),
        slots: List<ReminderSlot> = emptyList(),
        settings: AppSettings? = null
    ) {
        if (zikirs.size != 15) {
            throw IllegalArgumentException("Strict Snapshot Error: Snapshot must contain exactly 15 zikirs, found ${zikirs.size}")
        }

        val zikirIds = zikirs.map { it.id }
        val idSet = zikirIds.toSet()

        if (idSet.size != 15) {
            throw IllegalArgumentException("Strict Snapshot Error: Duplicate zikir IDs found in snapshot")
        }

        val expectedIds = (1..15).toSet()
        if (idSet != expectedIds) {
            throw IllegalArgumentException("Strict Snapshot Error: Zikir ID set must be exactly 1..15, found $idSet")
        }

        for (zikir in zikirs) {
            validateZikirStrict(zikir)
        }

        for (item in history) {
            validateHistoryStrict(item)
            if (item.zikirId !in idSet) {
                throw IllegalArgumentException("Strict Snapshot Error: History references invalid or non-snapshot zikirId: ${item.zikirId}")
            }
        }

        for (slot in slots) {
            validateReminderSlotStrict(slot)
        }

        if (settings != null) {
            validateSettingsStrict(settings)
        }
    }

    // STRICT RESTORE VALIDATION FUNCTIONS FOR BACKUP AND CLOUD RESTORE
    fun validateZikirStrict(zikir: Zikir): Zikir {
        if (zikir.id !in 1..15) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid Zikir ID (${zikir.id})")
        }
        if (zikir.target !in 100L..5000000L) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid Zikir target (${zikir.target})")
        }
        if (zikir.count < 0L || zikir.count > zikir.target) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid Zikir count (${zikir.count}) for target (${zikir.target})")
        }
        
        // invalid startedAt: if present and <= 0
        if (zikir.startedAt != null && zikir.startedAt <= 0L) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid startedAt (${zikir.startedAt})")
        }
        // missing startedAt for count > 0
        if (zikir.startedAt == null && zikir.count > 0L) {
            throw IllegalArgumentException("Strict Restore Validation Error: Missing startedAt when count > 0")
        }

        // invalid completedAt: if present and <= 0 or before startedAt
        if (zikir.completedAt != null && zikir.completedAt <= 0L) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid completedAt (${zikir.completedAt})")
        }
        if (zikir.startedAt != null && zikir.completedAt != null && zikir.completedAt < zikir.startedAt) {
            throw IllegalArgumentException("Strict Restore Validation Error: completedAt is earlier than startedAt")
        }
        // missing completedAt for count >= target
        if (zikir.completedAt == null && zikir.count >= zikir.target) {
            throw IllegalArgumentException("Strict Restore Validation Error: Missing completedAt when count >= target")
        }

        return zikir
    }

    fun validateHistoryStrict(history: ZikirHistory): ZikirHistory {
        if (history.eventId.isBlank()) {
            throw IllegalArgumentException("Strict Restore Validation Error: Empty eventId in ZikirHistory")
        }
        if (history.zikirId !in 1..15) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid ZikirHistory zikirId (${history.zikirId})")
        }
        if (history.amount !in 1L..5000000L) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid ZikirHistory amount (${history.amount})")
        }
        if (history.type != "add" && history.type != "remove") {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid ZikirHistory type (${history.type})")
        }
        // bozuk timestamp -> throw exception
        if (history.timestamp <= 0L) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid ZikirHistory timestamp (${history.timestamp})")
        }
        // boş dateKey -> throw exception
        if (history.dateKey.isBlank()) {
            throw IllegalArgumentException("Strict Restore Validation Error: Empty dateKey in ZikirHistory")
        }

        return history
    }

    fun validateSettingsStrict(settings: AppSettings): AppSettings {
        val ALLOWED_LANGS = setOf("tr", "ar", "en", "de", "fr")
        if (settings.lang !in ALLOWED_LANGS) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid lang (${settings.lang})")
        }

        // Canonical 3 + legacy 10 + aliases for backward compat (must stay in sync with Color.kt + firestore.rules)
        val ALLOWED_THEMES = setOf(
            // Canonical (new)
            "hadra_gunduz", "hadra_gece", "siyah",
            "beyaz", "yesil", "black",
            // Legacy (old 10)
            "emerald", "night", "rose", "olive", "light", "obsidian", "kisve", "turq", "amethyst", "sahara",
            // Additional legacy aliases
            "hadra_light", "hadra_dark", "inci", "white", "green", "oniks", "oled", "pure_black",
            "hadra_white", "hadra", "leyl", "kudus", "iznik", "gul", "amber", "kandil"
        )
        if (settings.themeName !in ALLOWED_THEMES) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid themeName (${settings.themeName})")
        }

        val ALLOWED_TEXTURES = setOf("none", "geometric", "kaaba", "floral", "tasbih", "stars")
        if (settings.counterTexture !in ALLOWED_TEXTURES) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid counterTexture (${settings.counterTexture})")
        }

        val ALLOWED_HAPTIC_TAP_MODES = setOf("light", "medium", "strong")
        if (settings.hapticTapMode !in ALLOWED_HAPTIC_TAP_MODES) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid hapticTapMode (${settings.hapticTapMode})")
        }

        val ALLOWED_HAPTIC_MILESTONE_MODES = setOf("double", "long", "triple")
        if (settings.hapticMilestoneMode !in ALLOWED_HAPTIC_MILESTONE_MODES) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid hapticMilestoneMode (${settings.hapticMilestoneMode})")
        }

        if (settings.dailyTarget !in 1L..5000000L) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid dailyTarget (${settings.dailyTarget})")
        }

        if (settings.completedRounds < 0) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid completedRounds (${settings.completedRounds})")
        }

        if (settings.fontScale !in 0.7f..1.5f) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid fontScale (${settings.fontScale})")
        }

        if (settings.selectedZikirId !in 1..15) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid selectedZikirId (${settings.selectedZikirId})")
        }

        if (settings.acknowledgedBadges.isNotEmpty()) {
            val ALLOWED_BADGE_IDS = setOf(
                "zikir_1", "zikir_3", "one_third", "half_way", "zikir_10",
                "terkip_hatmi", "streak_7", "streak_21", "streak_40"
            )
            val tokens = settings.acknowledgedBadges.split(",")
            val seen = mutableSetOf<String>()
            for (token in tokens) {
                if (token.isBlank() || token !in ALLOWED_BADGE_IDS || !seen.add(token)) {
                    throw IllegalArgumentException("Strict Restore Validation Error: Malformed acknowledgedBadges (${settings.acknowledgedBadges})")
                }
            }
        }

        if (settings.settingsUsageStats.isBlank()) {
            throw IllegalArgumentException("Strict Restore Validation Error: Malformed JSON in settingsUsageStats (blank)")
        }
        try {
            org.json.JSONObject(settings.settingsUsageStats)
        } catch (e: Exception) {
            throw IllegalArgumentException("Strict Restore Validation Error: Malformed JSON in settingsUsageStats (${settings.settingsUsageStats})")
        }

        return settings
    }

    fun validateReminderSlotStrict(slot: ReminderSlot): ReminderSlot {
        if (slot.hour !in 0..23) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid hour (${slot.hour})")
        }
        if (slot.minute !in 0..59) {
            throw IllegalArgumentException("Strict Restore Validation Error: Invalid minute (${slot.minute})")
        }
        return slot
    }
}
