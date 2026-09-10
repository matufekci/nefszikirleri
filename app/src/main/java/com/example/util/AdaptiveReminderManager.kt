package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import com.example.data.model.AppStrings
import com.example.data.model.SpiritualVerse
import com.example.receiver.ReminderAlarmReceiver
import java.util.Calendar
import java.util.Random

/**
 * Akıllı Manevi Hatırlatıcı Yöneticisi (Adaptive Spiritual Reminder Scheduler)
 * Kullanıcının zikir temposunu ve önceki günlerdeki zikir çekim alışkanlıklarını
 * sessizce arka planda analiz eder. Kullanıcı zikirden uzaklaştığı veya
 * önceki günlere göre zikri azalttığı anda Kur'an-ı Kerim'den ikaz ve müjde
 * ayetleriyle kişiyi gafletten uyandırır.
 */
object AdaptiveReminderManager {

    private const val PREFS_NAME = "adaptive_spiritual_reminder_prefs"
    private const val KEY_LAST_SENT_DATE = "last_notification_sent_date"
    private const val KEY_WEEKLY_SENT_COUNT = "weekly_notification_sent_count"
    private const val KEY_CURRENT_WEEK_KEY = "current_week_key"

    /**
     * Deterministik yıl ve hafta anahtarı üretir (Örn: "2026-W36", "2027-W01").
     * Yılbaşı geçişlerinde haftanın ait olduğu yılla senkron çalışır.
     */
    fun getWeekYearKey(calendar: Calendar = Calendar.getInstance()): String {
        val year = calendar.get(Calendar.YEAR)
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
        val month = calendar.get(Calendar.MONTH)

        val adjustedYear = if (weekOfYear == 1 && month == Calendar.DECEMBER) {
            year + 1
        } else if (weekOfYear >= 52 && month == Calendar.JANUARY) {
            year - 1
        } else {
            year
        }

        return String.format(java.util.Locale.US, "%04d-W%02d", adjustedYear, weekOfYear)
    }

    /**
     * Uygulamanın güncel dil ayarını SharedPreferences'tan okur.
     */
    fun getAppLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val spLang = prefs.getString("app_language", null)
        if (!spLang.isNullOrBlank()) return spLang

        val rootPrefs = context.getSharedPreferences("nefs_app_prefs", Context.MODE_PRIVATE)
        val rootLang = rootPrefs.getString("lang", null)
        if (!rootLang.isNullOrBlank()) return rootLang

        return "tr"
    }

    /**
     * Rastgele manevi ayeti kullanıcının seçtiği dilde döndürür.
     */
    fun getRandomSpiritualVerse(lang: String): SpiritualVerse {
        val verses = AppStrings.get(lang).spiritualVerses
        if (verses.isEmpty()) {
            val fallback = AppStrings.get("tr").spiritualVerses
            if (fallback.isNotEmpty()) {
                val index = Random().nextInt(fallback.size)
                return fallback[index]
            }
            return SpiritualVerse(
                surah = "Bakara Suresi, 152. Ayet",
                verseText = "Öyleyse yalnız Beni anın ki Ben de sizi anayım. Bana şükredin, nankörlük etmeyin.",
                type = "glad_tidings"
            )
        }
        val index = Random().nextInt(verses.size)
        return verses[index]
    }

    /**
     * Context üzerinden dili SharedPreferences'tan tespit edip rastgele manevi ayet döndürür.
     */
    fun getRandomSpiritualVerse(context: Context): SpiritualVerse {
        val lang = getAppLanguage(context)
        return getRandomSpiritualVerse(lang)
    }

    /**
     * Akıllı kontrol periyodunu planlar. (Artık WorkManager [DailyEvaluationWorker] tarafından yürütülüyor.)
     * Geriye dönük uyumluluk için NefsApplication scheduler'ına yönlendirir.
     */
    fun schedulePeriodicEvaluation(context: Context) {
        try {
            com.example.NefsApplication.scheduleDailyEvaluation(context)
        } catch (e: Exception) {
            // Best-effort, ignore
        }
    }

    /**
     * Günlük ve haftalık bildirim sınırlandırma denetimi.
     * Kullanıcıyı bildirimle boğmamak, fakat zikirden uzaklaştığında ikaz etmek için:
     * - Aynı gün içinde en fazla 1 bildirim (nadiren haftada birkaç kez 2 defa)
     * - Haftada en fazla 3-4 defa bildirim gönderilmesini garanti eder.
     */
    @Synchronized
    fun canSendNotificationToday(context: Context, calendar: Calendar = Calendar.getInstance()): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayKey = NumberFormatter.getDateKey()
        val lastDate = prefs.getString(KEY_LAST_SENT_DATE, "") ?: ""

        // Aynı gün içinde daha önce gönderildiyse tekrar gönderme
        if (todayKey == lastDate) {
            return false
        }

        // Yıl + Hafta bazlı sayacı kontrol et ve sıfırla (Örn: "2026-W36")
        val currentWeekKey = getWeekYearKey(calendar)
        val storedWeekKey = prefs.getString(KEY_CURRENT_WEEK_KEY, "") ?: ""
        var weeklyCount = prefs.getInt(KEY_WEEKLY_SENT_COUNT, 0)

        if (currentWeekKey != storedWeekKey) {
            weeklyCount = 0
        }

        // Haftada en fazla 4 defa bildirim gönder
        if (weeklyCount >= 4) {
            return false
        }

        return true
    }

    data class QuotaReservation(
        val reservedDate: String,
        val previousLastDate: String,
        val previousWeeklyCount: Int,
        val previousWeekKey: String
    )

    /**
     * Atomik kota rezervasyon işlemi.
     * Bildirim gönderimi öncesinde kotayı rezerv eder.
     * Kota uygun değilse null döner.
     */
    @Synchronized
    fun tryReserveQuota(context: Context, calendar: Calendar = Calendar.getInstance()): QuotaReservation? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayKey = NumberFormatter.getDateKey()
        val lastDate = prefs.getString(KEY_LAST_SENT_DATE, "") ?: ""

        if (todayKey == lastDate) {
            return null
        }

        val currentWeekKey = getWeekYearKey(calendar)
        val storedWeekKey = prefs.getString(KEY_CURRENT_WEEK_KEY, "") ?: ""
        var weeklyCount = prefs.getInt(KEY_WEEKLY_SENT_COUNT, 0)

        val previousWeeklyCount = weeklyCount
        val previousWeekKey = storedWeekKey

        if (currentWeekKey != storedWeekKey) {
            weeklyCount = 0
        }

        if (weeklyCount >= 4) {
            return null
        }

        val newWeeklyCount = weeklyCount + 1

        prefs.edit {
            putString(KEY_LAST_SENT_DATE, todayKey)
            putString(KEY_CURRENT_WEEK_KEY, currentWeekKey)
            putInt(KEY_WEEKLY_SENT_COUNT, newWeeklyCount)
        }

        return QuotaReservation(
            reservedDate = todayKey,
            previousLastDate = lastDate,
            previousWeeklyCount = previousWeeklyCount,
            previousWeekKey = previousWeekKey
        )
    }

    /**
     * Bildirim gönderimi başarısız olduğunda rezervasyonu geri alır (Rollback).
     */
    @Synchronized
    fun rollbackQuotaReservation(context: Context, reservation: QuotaReservation) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_LAST_SENT_DATE, reservation.previousLastDate)
            putString(KEY_CURRENT_WEEK_KEY, reservation.previousWeekKey)
            putInt(KEY_WEEKLY_SENT_COUNT, reservation.previousWeeklyCount)
        }
    }

    /**
     * Bildirim gönderildiğinde kaydeder
     */
    @Synchronized
    fun recordNotificationSent(context: Context, calendar: Calendar = Calendar.getInstance()) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayKey = NumberFormatter.getDateKey()
        val currentWeekKey = getWeekYearKey(calendar)
        val storedWeekKey = prefs.getString(KEY_CURRENT_WEEK_KEY, "") ?: ""
        var weeklyCount = prefs.getInt(KEY_WEEKLY_SENT_COUNT, 0)

        if (currentWeekKey != storedWeekKey) {
            weeklyCount = 0
        }

        prefs.edit {
            putString(KEY_LAST_SENT_DATE, todayKey)
            putString(KEY_CURRENT_WEEK_KEY, currentWeekKey)
            putInt(KEY_WEEKLY_SENT_COUNT, weeklyCount + 1)
        }
    }
}
