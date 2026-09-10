package com.example.data.backup

import android.content.Context
import com.example.data.model.AppSettings
import com.example.data.model.ReminderSlot
import com.example.data.model.Zikir
import com.example.data.model.ZikirHistory
import com.example.data.model.DhikrDataValidator
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class ZikirBackupItem(
    val id: Int,
    val target: Long,
    val count: Long,
    val startedAt: Long?,
    val completedAt: Long?
)

data class ZikirHistoryBackupItem(
    val id: Long = 0,
    val eventId: String? = null,
    val zikirId: Int,
    val amount: Long,
    val type: String,
    val timestamp: Long,
    val dateKey: String
)

data class ReminderSlotBackupItem(
    val id: Long,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean
)

data class AppSettingsBackupItem(
    val dailyTarget: Long,
    val themeName: String,
    val lang: String,
    val hapticEnabled: Boolean,
    val hapticTapMode: String,
    val hapticMilestoneMode: String,
    val countdownMode: Boolean,
    val fullScreenTap: Boolean,
    val reminderEnabled: Boolean,
    val inactivityAlertEnabled: Boolean,
    val completedRounds: Int,
    val fontScale: Float,
    val keepAwakeEnabled: Boolean,
    val selectedZikirId: Int,
    val counterTexture: String,
    val targetReminderEnabled: Boolean,
    val acknowledgedBadges: String,
    val autoReorderSettings: Boolean,
    val settingsUsageStats: String
)

data class BackupPayload(
    val appName: String,
    val schemaVersion: Int,
    val exportedAt: Long,
    val zikirs: List<ZikirBackupItem>,
    val history: List<ZikirHistoryBackupItem>,
    val reminderSlots: List<ReminderSlotBackupItem>,
    val settings: AppSettingsBackupItem
)

data class ValidatedBackupData(
    val zikirs: List<Zikir>,
    val history: List<ZikirHistory>,
    val reminderSlots: List<ReminderSlot>,
    val settings: AppSettings
)

class PasswordRequiredException : Exception("Şifreli yedek için parola gereklidir.")
class WrongPasswordException : Exception("Parola hatalı veya yedek dosyası bozuk.")

class BackupManager(private val context: Context) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
        const val APP_SIGNATURE = "com.example.nefs_zikir"
        const val ENCRYPTION_VERSION_BYTE: Byte = 0x01
        const val MAX_BACKUP_SIZE_BYTES = 25 * 1024 * 1024L // 25 MB
        
        private const val ITERATION_COUNT = 120000
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 32
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 128
    }

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(BackupPayload::class.java)
    
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    suspend fun exportBackup(
        outputStream: OutputStream,
        zikirs: List<Zikir>,
        history: List<ZikirHistory>,
        reminderSlots: List<ReminderSlot>,
        settings: AppSettings,
        password: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = BackupPayload(
                appName = APP_SIGNATURE,
                schemaVersion = CURRENT_SCHEMA_VERSION,
                exportedAt = System.currentTimeMillis(),
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
                        eventId = it.eventId,
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
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

            val secureRandom = SecureRandom()
            val salt = ByteArray(SALT_LENGTH)
            secureRandom.nextBytes(salt)
            val iv = ByteArray(IV_LENGTH)
            secureRandom.nextBytes(iv)

            val secretKey = deriveKey(password, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val parameterSpec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

            val ciphertext = cipher.doFinal(jsonBytes)

            outputStream.use { out ->
                out.write(byteArrayOf(ENCRYPTION_VERSION_BYTE))
                out.write(salt)
                out.write(iv)
                out.write(ciphertext)
                out.flush()
            }

            Result.success("Yedekleme başarıyla oluşturuldu ve şifrelendi (${payload.zikirs.size} zikir, ${payload.history.size} geçmiş kaydı).")
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception("Yedek oluşturulurken hata meydana geldi: ${e.localizedMessage}", e))
        }
    }

    suspend fun importBackup(
        inputStream: InputStream,
        password: String? = null
    ): Result<ValidatedBackupData> = withContext(Dispatchers.IO) {
        try {
            val buffer = java.io.ByteArrayOutputStream()
            val temp = ByteArray(8192)
            var totalBytes = 0L
            inputStream.use { input ->
                while (true) {
                    val count = input.read(temp)
                    if (count == -1) break
                    totalBytes += count
                    if (totalBytes > MAX_BACKUP_SIZE_BYTES) {
                        return@withContext Result.failure(IllegalArgumentException("Yedek dosyası izin verilen boyutu aşıyor (Maksimum 25 MB)."))
                    }
                    buffer.write(temp, 0, count)
                }
            }
            val bytes = buffer.toByteArray()
            if (bytes.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Yedek dosyası boş."))
            }

            val jsonContent: String
            
            if (bytes[0] == '{'.code.toByte()) {
                // Eski şifresiz JSON formatı
                jsonContent = String(bytes, Charsets.UTF_8)
            } else if (bytes[0] == ENCRYPTION_VERSION_BYTE) {
                // Yeni şifreli format
                if (password.isNullOrBlank()) {
                    return@withContext Result.failure(PasswordRequiredException())
                }
                
                if (bytes.size < 1 + SALT_LENGTH + IV_LENGTH) {
                    return@withContext Result.failure(IllegalArgumentException("Yedek dosyası bozuk veya eksik."))
                }
                
                val salt = bytes.copyOfRange(1, 1 + SALT_LENGTH)
                val iv = bytes.copyOfRange(1 + SALT_LENGTH, 1 + SALT_LENGTH + IV_LENGTH)
                val ciphertext = bytes.copyOfRange(1 + SALT_LENGTH + IV_LENGTH, bytes.size)
                
                try {
                    val secretKey = deriveKey(password, salt)
                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    val parameterSpec = GCMParameterSpec(TAG_LENGTH, iv)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)
                    
                    val decryptedBytes = cipher.doFinal(ciphertext)
                    jsonContent = String(decryptedBytes, Charsets.UTF_8)
                } catch (e: javax.crypto.AEADBadTagException) {
                    return@withContext Result.failure(WrongPasswordException())
                } catch (e: Exception) {
                    return@withContext Result.failure(Exception("Şifre çözme hatası: ${e.localizedMessage}", e))
                }
            } else {
                // Geçersiz veya eski metin tabanlı format
                val text = String(bytes, Charsets.UTF_8)
                val startTag = "--- NEFS_ZIKIR_BACKUP_DATA_START ---"
                val endTag = "--- NEFS_ZIKIR_BACKUP_DATA_END ---"
                
                if (text.contains(startTag) && text.contains(endTag)) {
                    val start = text.indexOf(startTag) + startTag.length
                    val end = text.indexOf(endTag)
                    if (start in 0..end) {
                        jsonContent = text.substring(start, end).trim()
                    } else {
                        return@withContext Result.failure(IllegalArgumentException("Yedekleme dosyasındaki veri blokları geçerli değil."))
                    }
                } else {
                    return@withContext Result.failure(IllegalArgumentException("Bilinmeyen veya desteklenmeyen yedek formatı."))
                }
            }

            if (jsonContent.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Dosyadan veri okunamadı."))
            }

            val payload = jsonAdapter.fromJson(jsonContent)
                ?: return@withContext Result.failure(IllegalArgumentException("Geçersiz JSON formatı."))

            if (payload.appName != APP_SIGNATURE) {
                return@withContext Result.failure(IllegalArgumentException("Bu dosya Nefs Zikir uygulamasına ait geçerli bir yedek formatı içermiyor."))
            }
            if (payload.schemaVersion < 1) {
                return@withContext Result.failure(IllegalArgumentException("Geçersiz yedekleme şeması sürümü."))
            }
            if (payload.schemaVersion > CURRENT_SCHEMA_VERSION) {
                return@withContext Result.failure(IllegalArgumentException("Bu yedekleme dosyası daha yeni bir uygulama sürümünde oluşturulmuş. Lütfen uygulamanızı güncelleyin."))
            }
            if (payload.zikirs.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Yedekleme dosyası içerisinde zikir verisi bulunamadı."))
            }

            val parsedZikirs = payload.zikirs.map {
                Zikir(
                    id = it.id,
                    target = it.target,
                    count = it.count,
                    startedAt = it.startedAt,
                    completedAt = it.completedAt
                )
            }

            val parsedHistory = payload.history.map {
                ZikirHistory(
                    id = 0L,
                    eventId = if (!it.eventId.isNullOrBlank()) it.eventId else java.util.UUID.randomUUID().toString(),
                    zikirId = it.zikirId,
                    amount = it.amount,
                    type = it.type,
                    timestamp = it.timestamp,
                    dateKey = it.dateKey
                )
            }

            val parsedReminderSlots = payload.reminderSlots.map {
                ReminderSlot(
                    id = 0,
                    hour = it.hour,
                    minute = it.minute,
                    isEnabled = it.isEnabled
                )
            }

            val s = payload.settings
            val parsedSettings = AppSettings(
                id = 1,
                dailyTarget = s.dailyTarget,
                themeName = s.themeName,
                lang = s.lang,
                hapticEnabled = s.hapticEnabled,
                hapticTapMode = s.hapticTapMode,
                hapticMilestoneMode = s.hapticMilestoneMode,
                countdownMode = s.countdownMode,
                fullScreenTap = s.fullScreenTap,
                reminderEnabled = s.reminderEnabled,
                inactivityAlertEnabled = s.inactivityAlertEnabled,
                completedRounds = s.completedRounds,
                fontScale = s.fontScale,
                keepAwakeEnabled = s.keepAwakeEnabled,
                selectedZikirId = s.selectedZikirId,
                counterTexture = s.counterTexture,
                targetReminderEnabled = s.targetReminderEnabled,
                acknowledgedBadges = s.acknowledgedBadges,
                autoReorderSettings = s.autoReorderSettings,
                settingsUsageStats = s.settingsUsageStats
            )

            // Validate entire snapshot strictly (15 zikirs, IDs 1..15, strict zikirs, valid history referencing snapshot zikirs, slots, settings)
            DhikrDataValidator.validateFullSnapshotStrict(
                zikirs = parsedZikirs,
                history = parsedHistory,
                slots = parsedReminderSlots,
                settings = parsedSettings
            )

            Result.success(
                ValidatedBackupData(
                    zikirs = parsedZikirs,
                    history = parsedHistory,
                    reminderSlots = parsedReminderSlots,
                    settings = parsedSettings
                )
            )
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception("Yedek dosyası okunamadı veya geçersiz format: ${e.localizedMessage}", e))
        }
    }

    /**
     * cacheDir/backups klasöründeki eski geçici dışa aktarım (export/share) dosyalarını temizler.
     * Sadece "nefs_zikir_backup_" ile başlayan geçici export dosyalarını hedefler.
     * Kullanıcının seçtiği veya dışarıdan gelen yedek dosyalarına dokunmaz.
     *
     * @param maxAgeMillis Belirtilen süreden eski geçici dosyalar silinir (varsayılan 24 saat).
     * @param maxRetainedFiles Saklanacak maksimum geçici dosya sayısı (en yeni dosyalar korunur).
     */
    fun cleanupTemporaryBackups(
        maxAgeMillis: Long = 24 * 60 * 60 * 1000L,
        maxRetainedFiles: Int = 3
    ) {
        try {
            val backupsDir = java.io.File(context.cacheDir, "backups")
            if (backupsDir.exists() && backupsDir.isDirectory) {
                val now = System.currentTimeMillis()
                val tempFiles = backupsDir.listFiles { file ->
                    file.isFile && file.name.startsWith("nefs_zikir_backup_") &&
                        (file.name.endsWith(".edb") || file.name.endsWith(".json") || file.name.endsWith(".tmp"))
                } ?: emptyArray()

                // 1. maxAgeMillis süresini aşan eski dosyaları temizle
                val remainingFiles = mutableListOf<java.io.File>()
                for (file in tempFiles) {
                    if (now - file.lastModified() > maxAgeMillis) {
                        file.delete()
                    } else {
                        remainingFiles.add(file)
                    }
                }

                // 2. Maksimum saklanacak dosya sayısını aşan en eski geçici dosyaları temizle
                if (remainingFiles.size > maxRetainedFiles) {
                    remainingFiles.sortBy { it.lastModified() }
                    val toDeleteCount = remainingFiles.size - maxRetainedFiles
                    for (i in 0 until toDeleteCount) {
                        remainingFiles[i].delete()
                    }
                }
            }

            // Kök cacheDir dizininde kalmış olabilecek eski geçici dosyaları da güvenle temizle
            val rootTempFiles = context.cacheDir.listFiles { file ->
                file.isFile && file.name.startsWith("nefs_zikir_backup_") &&
                    (file.name.endsWith(".edb") || file.name.endsWith(".json") || file.name.endsWith(".tmp"))
            }
            rootTempFiles?.forEach { it.delete() }
        } catch (e: Exception) {
            // Arka planda güvenli çalışma garantisi
        }
    }

    fun createTemporaryBackupFile(): java.io.File {
        val backupsDir = java.io.File(context.cacheDir, "backups")
        if (!backupsDir.exists()) {
            backupsDir.mkdirs()
        }
        cleanupTemporaryBackups()
        val fileName = "nefs_zikir_backup_${System.currentTimeMillis()}.edb"
        return java.io.File(backupsDir, fileName)
    }
}
