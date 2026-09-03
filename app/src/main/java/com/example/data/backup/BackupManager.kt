package com.example.data.backup

import android.content.Context
import com.example.data.model.AppSettings
import com.example.data.model.ReminderSlot
import com.example.data.model.Zikir
import com.example.data.model.ZikirHistory
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dışa ve içe aktarımda kullanılacak katı şemaya sahip JSON yedekleme veri modeli.
 */
@JsonClass(generateAdapter = true)
data class NefsZikirBackupPayload(
    @param:Json(name = "schemaVersion") val schemaVersion: Int = 1,
    @param:Json(name = "appName") val appName: String = "NefsZikir",
    @param:Json(name = "timestamp") val timestamp: Long = 0L,
    @param:Json(name = "zikirs") val zikirs: List<ZikirBackupItem> = emptyList(),
    @param:Json(name = "history") val history: List<ZikirHistoryBackupItem> = emptyList(),
    @param:Json(name = "reminderSlots") val reminderSlots: List<ReminderSlotBackupItem> = emptyList(),
    @param:Json(name = "settings") val settings: AppSettingsBackupItem = AppSettingsBackupItem()
)

@JsonClass(generateAdapter = true)
data class ZikirBackupItem(
    @param:Json(name = "id") val id: Int = 1,
    @param:Json(name = "target") val target: Long = 70000L,
    @param:Json(name = "count") val count: Long = 0L,
    @param:Json(name = "startedAt") val startedAt: Long? = null,
    @param:Json(name = "completedAt") val completedAt: Long? = null
)

@JsonClass(generateAdapter = true)
data class ZikirHistoryBackupItem(
    @param:Json(name = "id") val id: Long = 0,
    @param:Json(name = "zikirId") val zikirId: Int = 1,
    @param:Json(name = "amount") val amount: Long = 1L,
    @param:Json(name = "type") val type: String = "add",
    @param:Json(name = "timestamp") val timestamp: Long = 0L,
    @param:Json(name = "dateKey") val dateKey: String = ""
)

@JsonClass(generateAdapter = true)
data class ReminderSlotBackupItem(
    @param:Json(name = "id") val id: Long = 0,
    @param:Json(name = "hour") val hour: Int = 9,
    @param:Json(name = "minute") val minute: Int = 0,
    @param:Json(name = "isEnabled") val isEnabled: Boolean = true
)

@JsonClass(generateAdapter = true)
data class AppSettingsBackupItem(
    @param:Json(name = "dailyTarget") val dailyTarget: Long = 10000L,
    @param:Json(name = "themeName") val themeName: String = "emerald",
    @param:Json(name = "lang") val lang: String = "tr",
    @param:Json(name = "hapticEnabled") val hapticEnabled: Boolean = true,
    @param:Json(name = "hapticTapMode") val hapticTapMode: String = "light",
    @param:Json(name = "hapticMilestoneMode") val hapticMilestoneMode: String = "double",
    @param:Json(name = "countdownMode") val countdownMode: Boolean = false,
    @param:Json(name = "fullScreenTap") val fullScreenTap: Boolean = false,
    @param:Json(name = "reminderEnabled") val reminderEnabled: Boolean = false,
    @param:Json(name = "inactivityAlertEnabled") val inactivityAlertEnabled: Boolean = false,
    @param:Json(name = "completedRounds") val completedRounds: Int = 0,
    @param:Json(name = "fontScale") val fontScale: Float = 1.15f,
    @param:Json(name = "keepAwakeEnabled") val keepAwakeEnabled: Boolean = true,
    @param:Json(name = "selectedZikirId") val selectedZikirId: Int = 1,
    @param:Json(name = "counterTexture") val counterTexture: String = "geometric",
    @param:Json(name = "targetReminderEnabled") val targetReminderEnabled: Boolean = false,
    @param:Json(name = "acknowledgedBadges") val acknowledgedBadges: String = "",
    @param:Json(name = "autoReorderSettings") val autoReorderSettings: Boolean = false,
    @param:Json(name = "settingsUsageStats") val settingsUsageStats: String = "{}"
)

/**
 * İçe aktarılan doğrulanmış verilerin ViewModel/Repository katmanına iletilmesini sağlayan kapsayıcı sınıf.
 */
data class ValidatedBackupData(
    val zikirs: List<Zikir>,
    val history: List<ZikirHistory>,
    val reminderSlots: List<ReminderSlot>,
    val settings: AppSettings
)

class BackupManager(private val context: Context) {

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
        const val APP_SIGNATURE = "NefsZikir"
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val jsonAdapter = moshi.adapter(NefsZikirBackupPayload::class.java)

    /**
     * Mevcut verileri katı şemaya uygun JSON formatına dönüştürerek OutputStream'e yazar.
     */
    suspend fun exportBackup(
        outputStream: OutputStream,
        zikirs: List<Zikir>,
        history: List<ZikirHistory>,
        reminderSlots: List<ReminderSlot>,
        settings: AppSettings
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = NefsZikirBackupPayload(
                schemaVersion = CURRENT_SCHEMA_VERSION,
                appName = APP_SIGNATURE,
                timestamp = System.currentTimeMillis(),
                zikirs = zikirs.map {
                    ZikirBackupItem(
                        id = it.id,
                        target = it.target,
                        count = it.count,
                        startedAt = it.startedAt,
                        completedAt = it.completedAt
                    )
                },
                history = history.map {
                    ZikirHistoryBackupItem(
                        id = it.id,
                        zikirId = it.zikirId,
                        amount = it.amount,
                        type = it.type,
                        timestamp = it.timestamp,
                        dateKey = it.dateKey
                    )
                },
                reminderSlots = reminderSlots.map {
                    ReminderSlotBackupItem(
                        id = it.id,
                        hour = it.hour,
                        minute = it.minute,
                        isEnabled = it.isEnabled
                    )
                },
                settings = AppSettingsBackupItem(
                    dailyTarget = settings.dailyTarget,
                    themeName = settings.themeName,
                    lang = settings.lang,
                    hapticEnabled = settings.hapticEnabled,
                    hapticTapMode = settings.hapticTapMode,
                    hapticMilestoneMode = settings.hapticMilestoneMode,
                    countdownMode = settings.countdownMode,
                    fullScreenTap = settings.fullScreenTap,
                    reminderEnabled = settings.reminderEnabled,
                    inactivityAlertEnabled = settings.inactivityAlertEnabled,
                    completedRounds = settings.completedRounds,
                    fontScale = settings.fontScale,
                    keepAwakeEnabled = settings.keepAwakeEnabled,
                    selectedZikirId = settings.selectedZikirId,
                    counterTexture = settings.counterTexture,
                    targetReminderEnabled = settings.targetReminderEnabled,
                    acknowledgedBadges = settings.acknowledgedBadges,
                    autoReorderSettings = settings.autoReorderSettings,
                    settingsUsageStats = settings.settingsUsageStats
                )
            )

            val jsonString = jsonAdapter.indent("  ").toJson(payload)
            outputStream.use { out ->
                out.write(jsonString.toByteArray(Charsets.UTF_8))
                out.flush()
            }

            Result.success("Yedekleme başarıyla oluşturuldu (${payload.zikirs.size} zikir, ${payload.history.size} geçmiş kaydı).")
        } catch (e: Exception) {
            Result.failure(Exception("Yedek oluşturulurken hata meydana geldi: ${e.localizedMessage}", e))
        }
    }

    /**
     * Gelen JSON InputStream akışını okur, katı şema doğrulaması uygular ve doğrulanmış veri modellerini döner.
     */
    suspend fun importBackup(
        inputStream: InputStream
    ): Result<ValidatedBackupData> = withContext(Dispatchers.IO) {
        try {
            val jsonContent = inputStream.use { input ->
                input.bufferedReader(Charsets.UTF_8).use { it.readText() }
            }

            if (jsonContent.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Yedek dosyası boş."))
            }

            // Katı şema parse işlemi
            val payload = jsonAdapter.fromJson(jsonContent)
                ?: return@withContext Result.failure(IllegalArgumentException("Geçersiz JSON formatı."))

            // Katı domain ve sürüm doğrulama kuralları
            if (payload.appName != APP_SIGNATURE) {
                return@withContext Result.failure(
                    IllegalArgumentException("Bu dosya Nefs Zikir uygulamasına ait geçerli bir yedek formatı içermiyor.")
                )
            }

            if (payload.schemaVersion < 1) {
                return@withContext Result.failure(
                    IllegalArgumentException("Geçersiz yedekleme şeması sürümü.")
                )
            }

            if (payload.schemaVersion > CURRENT_SCHEMA_VERSION) {
                return@withContext Result.failure(
                    IllegalArgumentException("Bu yedekleme dosyası daha yeni bir uygulama sürümünde oluşturulmuş. Lütfen uygulamanızı güncelleyin.")
                )
            }

            // Zikir veri bütünlüğü kontrolü
            if (payload.zikirs.isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Yedekleme dosyası içerisinde zikir verisi bulunamadı.")
                )
            }

            // Modelleri güvenle Room entity yapılarına dönüştürme
            val validatedZikirs = payload.zikirs
                .filter { it.id in 1..15 }
                .map {
                    val count = it.count.coerceAtLeast(0L)
                    val target = it.target.coerceIn(100L, 5000000L)
                    val startedAt = if (it.startedAt != null && it.startedAt > 0L) it.startedAt else if (count > 0) System.currentTimeMillis() else null
                    val completedAt = if (it.completedAt != null && it.completedAt > 0L) it.completedAt else if (count >= target) System.currentTimeMillis() else null
                    Zikir(
                        id = it.id,
                        target = target,
                        count = count,
                        startedAt = startedAt,
                        completedAt = completedAt
                    )
                }

            if (validatedZikirs.isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Yedekleme dosyasında geçerli zikir verisi bulunamadı.")
                )
            }

            val validatedHistory = payload.history
                .filter { it.zikirId in 1..15 }
                .map {
                    ZikirHistory(
                        id = 0, // Çakışmaları önlemek için yeni auto-increment ID
                        zikirId = it.zikirId,
                        amount = it.amount.coerceIn(1L, 5000000L),
                        type = if (it.type == "remove") "remove" else "add",
                        timestamp = if (it.timestamp > 0) it.timestamp else System.currentTimeMillis(),
                        dateKey = it.dateKey.ifBlank {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        }
                    )
                }

            val validatedReminderSlots = payload.reminderSlots.map {
                ReminderSlot(
                    id = 0,
                    hour = it.hour.coerceIn(0, 23),
                    minute = it.minute.coerceIn(0, 59),
                    isEnabled = it.isEnabled
                )
            }

            val s = payload.settings
            val validatedSettings = AppSettings(
                id = 1,
                dailyTarget = s.dailyTarget.coerceAtLeast(1L),
                themeName = s.themeName,
                lang = s.lang,
                hapticEnabled = s.hapticEnabled,
                hapticTapMode = s.hapticTapMode,
                hapticMilestoneMode = s.hapticMilestoneMode,
                countdownMode = s.countdownMode,
                fullScreenTap = s.fullScreenTap,
                reminderEnabled = s.reminderEnabled,
                inactivityAlertEnabled = s.inactivityAlertEnabled,
                completedRounds = s.completedRounds.coerceAtLeast(0),
                fontScale = s.fontScale.coerceIn(0.7f, 1.5f),
                keepAwakeEnabled = s.keepAwakeEnabled,
                selectedZikirId = s.selectedZikirId,
                counterTexture = s.counterTexture,
                targetReminderEnabled = s.targetReminderEnabled,
                acknowledgedBadges = s.acknowledgedBadges,
                autoReorderSettings = s.autoReorderSettings,
                settingsUsageStats = s.settingsUsageStats
            )

            val validatedData = ValidatedBackupData(
                zikirs = validatedZikirs,
                history = validatedHistory,
                reminderSlots = validatedReminderSlots,
                settings = validatedSettings
            )

            Result.success(validatedData)
        } catch (e: com.squareup.moshi.JsonDataException) {
            Result.failure(IllegalArgumentException("Şema Doğrulama Hatası: JSON verisi beklenen modelle uyuşmuyor veya eksik alanlar içeriyor.", e))
        } catch (e: Exception) {
            Result.failure(Exception("Yedek içe aktarılırken hata oluştu: ${e.localizedMessage}", e))
        }
    }

    /**
     * Paylaşım için geçici önbellekte (cacheDir/backups) `.json` yedek dosyası oluşturur.
     */
    fun createTemporaryBackupFile(): File {
        val backupDir = File(context.cacheDir, "backups").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "nefs_zikir_backup_$timeStamp.json"
        return File(backupDir, fileName)
    }
}
