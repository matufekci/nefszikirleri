package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val lang: String = "tr", // "tr", "ar", "en", "de", "fr"
    val themeName: String = "emerald", // "emerald", "night", "rose", "olive", "light", "obsidian", "kisve", "turq", "amethyst", "sahara"
    val countdownMode: Boolean = false,
    val dailyTarget: Long = 10000L,
    val hapticEnabled: Boolean = true,
    val fullScreenTap: Boolean = false,
    val keepAwakeEnabled: Boolean = true,
    val completedRounds: Int = 0,
    val reminderEnabled: Boolean = false,
    val inactivityAlertEnabled: Boolean = false,
    val selectedZikirId: Int = 1,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val counterTexture: String = "geometric", // "none", "geometric", "kaaba", "floral", "tasbih", "stars"
    val fontScale: Float = 1.15f, // 1.0f (Küçük), 1.15f (Normal - Varsayılan), 1.30f (Büyük), 1.45f (Çok Büyük)
    val hapticTapMode: String = "light", // "light", "medium", "strong"
    val hapticMilestoneMode: String = "double", // "double", "long", "triple"
    val targetReminderEnabled: Boolean = false, // Hedef hatırlatıcısı
    val acknowledgedBadges: String = "", // Comma-separated acknowledged badge IDs
    /**
     * Bu alanlar (`autoReorderSettings` ve `settingsUsageStats`) artık hiçbir UI tarafından kullanılmamaktadır.
     * Geçmişte denenip iptal edilen "ayarları kullanım sıklığına göre otomatik sıralama" özelliğinin kalıntısıdır.
     * Room veritabanı şema ve migration uyumluluğunun bozulmaması için bilerek silinmemiştir ve muhafaza edilmektedir.
     */
    val autoReorderSettings: Boolean = false,
    val settingsUsageStats: String = "{}" // JSON representation of category usage
)

