package com.example.util

import android.content.Context
import androidx.core.content.edit
import com.example.data.model.AppStrings
import com.example.data.model.SpiritualVerse
import java.util.Calendar
import java.util.Random

/**
 * Akıllı Manevi Hatırlatıcı Yöneticisi (Adaptive Spiritual Reminder Scheduler)
 *
 * ## Tempo Matematiği (Nefs Terbiyesi Programı)
 *
 * Terkib-i Şerif'in varsayılan toplamı 1.140.000 zikirdir ve programın
 * 6 ayda (182 gün) bitirilmesi esastır. En kötü ihtimalle bir turun
 * 1 yılda (365 gün) tamamlanabilmesi gerekir. Buna göre:
 *
 * - Günlük ihtiyaç = kalan zikir / ideal takvimde kalan gün, [MIN_DAILY, MAX_DAILY]
 *   aralığına kıstırılır. Kullanıcının önündeki günlük hedef daima 3-5 bin bandındadır.
 * - MIN_DAILY = ceil(1.140.000 / 365) = 3.124 → bu tempoyla tur en geç 365 günde biter.
 * - MAX_DAILY = 5.000 → kullanıcı hiçbir zaman günde 5 binden fazlasına zorlanmaz.
 *
 * Bildirim yoğunluğu kullanıcının son 7 günlük gerçek temposuna (avg7) göre
 * her gün yeniden hesaplanır; ayar gerektirmez:
 *
 * - ON_TRACK  (avg7 >= need):        haftada en fazla 1 müjdeli teşvik
 * - MILD      (%60 <= avg7 < need):  haftada en fazla 2
 * - BEHIND    (%30 <= avg7 < %60):   haftada en fazla 3
 * - CRITICAL  (avg7 < %30):          haftada en fazla 4 (günde 1)
 */
object AdaptiveReminderManager {

    /** Kullanıcı bu kadar gün hiç zikir çekmediyse hareketsiz sayılır. */
    const val INACTIVITY_THRESHOLD_DAYS = 2

    /** Alarm, son zikirden bu kadar gün sonrasına kurulur (2-4 gün aralığının ortası). */
    const val INACTIVITY_TRIGGER_DAYS = 3

    /** Programın ideal bitiş süresi: 6 ay. */
    const val IDEAL_DAYS = 182

    /**
     * Günlük alt sınır: ceil(1.140.000 / 365) = 3.124.
     * Bu tempoyla bir tur en kötü ihtimalle 1 yılda biter.
     */
    const val MIN_DAILY = 3124L

    /** Günlük üst sınır: kullanıcı günde 5 binden fazlasına zorlanmaz. */
    const val MAX_DAILY = 5000L

    /** Tur başındaki hoşgörü süresi: ilk günlerde tempo bildirimi gönderilmez. */
    const val GRACE_DAYS = 3

    /** Bildirim temposu bantları. */
    enum class PaceBand { ON_TRACK, MILD, BEHIND, CRITICAL }

    private const val PREFS_NAME = "adaptive_spiritual_reminder_prefs"
    private const val KEY_LAST_SENT_DATE = "last_notification_sent_date"
    private const val KEY_WEEKLY_SENT_COUNT = "weekly_notification_sent_count"
    private const val KEY_CURRENT_WEEK_KEY = "current_week_key"

    /**
     * Kalan zikre ve turun ideal takviminde geçen süreye göre günlük ihtiyacı hesaplar.
     * Sonuç daima [MIN_DAILY, MAX_DAILY] = [3.124, 5.000] aralığındadır.
     */
    fun computeNeedDaily(remaining: Long, elapsedDays: Int): Long {
        if (remaining <= 0L) return MIN_DAILY
        val daysLeft = (IDEAL_DAYS - elapsedDays).coerceAtLeast(1)
        val raw = (remaining + daysLeft - 1) / daysLeft // yukarı yuvarla
        return raw.coerceIn(MIN_DAILY, MAX_DAILY)
    }

    /** Son 7 günün günlük ortalamasını ihtiyaçla kıyaslayıp tempo bandını verir. */
    fun paceBand(avg7: Double, needDaily: Long): PaceBand {
        if (needDaily <= 0L) return PaceBand.ON_TRACK
        val ratio = avg7 / needDaily
        return when {
            ratio >= 1.0 -> PaceBand.ON_TRACK
            ratio >= 0.6 -> PaceBand.MILD
            ratio >= 0.3 -> PaceBand.BEHIND
            else -> PaceBand.CRITICAL
        }
    }

    /** Bant başına haftalık bildirim kotası. Günlük sınır daima 1'dir. */
    fun weeklyQuotaFor(band: PaceBand): Int = when (band) {
        PaceBand.ON_TRACK -> 1
        PaceBand.MILD -> 2
        PaceBand.BEHIND -> 3
        PaceBand.CRITICAL -> 4
    }

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

    /** Rastgele MÜJDE ayeti (tempo yerindeyken teşvik için). */
    fun getRandomGladTidings(context: Context): SpiritualVerse {
        val lang = getAppLanguage(context)
        val pool = AppStrings.get(lang).spiritualVerses
            .ifEmpty { AppStrings.get("tr").spiritualVerses }
            .filter { it.type == "glad_tidings" }
        if (pool.isEmpty()) return getRandomSpiritualVerse(lang)
        return pool[Random().nextInt(pool.size)]
    }

    /** Rastgele UYARI ayeti (tempo düştüğünde ikaz için). */
    fun getRandomWarning(context: Context): SpiritualVerse {
        val lang = getAppLanguage(context)
        val pool = AppStrings.get(lang).spiritualVerses
            .ifEmpty { AppStrings.get("tr").spiritualVerses }
            .filter { it.type == "warning" }
        if (pool.isEmpty()) return getRandomSpiritualVerse(lang)
        return pool[Random().nextInt(pool.size)]
    }

    /**
     * Hareketsizlik hatırlatıcısı için sırayla ayet döndürür: önce 5 uyarı ayeti,
     * ardından 5 müjde ayeti; liste bitince başa sarar. Böylece kullanıcı aynı
     * ayeti üst üste görmez ve uyarı/müjde dengesi korunur.
     */
    fun getInactivityVerse(context: Context): SpiritualVerse {
        val lang = getAppLanguage(context)
        val verses = AppStrings.get(lang).spiritualVerses
            .ifEmpty { AppStrings.get("tr").spiritualVerses }

        val warnings = verses.filter { it.type == "warning" }
        val gladTidings = verses.filter { it.type == "glad_tidings" }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val total = (warnings.size + gladTidings.size).coerceAtLeast(1)
        val index = prefs.getInt("inactivity_verse_index", 0).mod(total)

        val verse = when {
            warnings.isNotEmpty() && index < warnings.size -> warnings[index]
            gladTidings.isNotEmpty() -> gladTidings[(index - warnings.size).mod(gladTidings.size)]
            verses.isNotEmpty() -> verses[index.mod(verses.size)]
            else -> getRandomSpiritualVerse(lang)
        }

        prefs.edit { putInt("inactivity_verse_index", (index + 1).mod(total)) }
        return verse
    }

    /**
     * Günlük ve haftalık bildirim sınırlandırma denetimi.
     * - Aynı gün içinde en fazla 1 bildirim
     * - Haftalık üst sınır tempo bandına göre tryReserveQuota'da uygulanır
     */
    @Synchronized
    fun canSendNotificationToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayKey = NumberFormatter.getDateKey()
        val lastDate = prefs.getString(KEY_LAST_SENT_DATE, "") ?: ""
        return todayKey != lastDate
    }

    data class QuotaReservation(
        val reservedDate: String,
        val previousLastDate: String,
        val previousWeeklyCount: Int,
        val previousWeekKey: String
    )

    /**
     * Atomik kota rezervasyon işlemi. Haftalık üst sınır tempo bandından gelir.
     * Kota uygun değilse null döner.
     */
    @Synchronized
    fun tryReserveQuota(
        context: Context,
        weeklyLimit: Int,
        calendar: Calendar = Calendar.getInstance()
    ): QuotaReservation? {
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

        if (weeklyCount >= weeklyLimit.coerceAtLeast(0)) {
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

    /** Bu hafta kaç bildirim gönderildiğini döndürür (bant kotası denetimi için). */
    @Synchronized
    fun getWeeklySentCount(context: Context, calendar: Calendar = Calendar.getInstance()): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentWeekKey = getWeekYearKey(calendar)
        val storedWeekKey = prefs.getString(KEY_CURRENT_WEEK_KEY, "") ?: ""
        if (currentWeekKey != storedWeekKey) return 0
        return prefs.getInt(KEY_WEEKLY_SENT_COUNT, 0)
    }
}
