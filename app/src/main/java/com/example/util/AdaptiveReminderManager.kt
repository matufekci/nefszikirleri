package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
    private const val KEY_CURRENT_WEEK_OF_YEAR = "current_week_of_year"

    data class SpiritualVerse(
        val surah: String,
        val verseText: String,
        val type: String // "warning" or "glad_tidings"
    )

    // Kullanıcının talep ettiği ayet-i kerimeler ve zikredenlere müjdeleyici ayetler
    val VERSES = listOf(
        // İkaz / Uyarı Ayetleri
        SpiritualVerse(
            surah = "Tâhâ Suresi, 124. Ayet",
            verseText = "Kim Benim zikrimden (Kur'an'dan ve Beni anmaktan) yüz çevirirse, şüphesiz onun sıkıntılı (dar) bir geçimi olur ve kıyamet günü onu kör olarak haşrederiz.",
            type = "warning"
        ),
        SpiritualVerse(
            surah = "Zuhruf Suresi, 36. Ayet",
            verseText = "Kim Rahman'ın zikrinden (Kur'an'dan ve ilahi hatırlatmadan) göz yumarsa (yüz çevirirse), Biz ona bir şeytan musallat ederiz; artık o, onun kesintisiz arkadaşıdır.",
            type = "warning"
        ),
        SpiritualVerse(
            surah = "Cinn Suresi, 17. Ayet",
            verseText = "O'nun zikrinden (Kur'an'dan) yüz çevirenleri Allah, sarp ve şiddetli bir azaba sürükler.",
            type = "warning"
        ),
        SpiritualVerse(
            surah = "En'âm Suresi, 44. Ayet",
            verseText = "Kendilerine hatırlatılanı (zikri) unuttuklarında, üzerlerine her şeyin kapılarını açtık. Nihayet kendilerine verilenler yüzünden şımardıkları an, onları ansızın yakaladık; birdenbire hepsi ümitsizliğe kapıldılar.",
            type = "warning"
        ),
        SpiritualVerse(
            surah = "Münâfikûn Suresi, 9. Ayet",
            verseText = "Ey iman edenler! Mallarınız da çocuklarınız da sizi Allah'ı zikretmekten alıkoymasın. Kim bunu yaparsa, işte onlar ziyana uğrayanların ta kendileridir.",
            type = "warning"
        ),
        // Müjdeleyici Ayetler
        SpiritualVerse(
            surah = "Bakara Suresi, 152. Ayet",
            verseText = "Öyleyse yalnız Beni anın ki Ben de sizi anayım. Bana şükredin, nankörlük etmeyin.",
            type = "glad_tidings"
        ),
        SpiritualVerse(
            surah = "Ahzâb Suresi, 41-42. Ayetler",
            verseText = "Ey iman edenler! Allah'ı çokça zikredin ve O'nu sabah akşam tesbih edip yüceltin.",
            type = "glad_tidings"
        ),
        SpiritualVerse(
            surah = "Rad Suresi, 28. Ayet",
            verseText = "Onlar, iman edenler ve kalpleri Allah'ın zikriyle huzura kavuşanlardır. Dikkat edin! Kalpler ancak Allah'ın zikriyle mutmain olur.",
            type = "glad_tidings"
        ),
        SpiritualVerse(
            surah = "Ahzâb Suresi, 35. Ayet",
            verseText = "Allah'ı çok zikreden erkekler ve çok zikreden kadınlar var ya; işte Allah onlar için bir mağfiret ve büyük bir mükâfat hazırlamıştır.",
            type = "glad_tidings"
        ),
        SpiritualVerse(
            surah = "Ankebût Suresi, 45. Ayet",
            verseText = "Şüphesiz Allah'ı zikretmek en büyük (ibadet)tir. Allah ne yaptığınızı çok iyi bilir.",
            type = "glad_tidings"
        )
    )

    fun getRandomSpiritualVerse(): SpiritualVerse {
        val index = Random().nextInt(VERSES.size)
        return VERSES[index]
    }

    /**
     * Akıllı kontrol periyodunu planlar. Her gün saat 20:30 civarında
     * sessiz bir kontrol tetikler.
     */
    fun schedulePeriodicEvaluation(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_TYPE, ReminderAlarmReceiver.TYPE_ADAPTIVE_CHECK)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ReminderAlarmReceiver.NOTIFICATION_ID_ADAPTIVE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    /**
     * Günlük ve haftalık bildirim sınırlandırma denetimi.
     * Kullanıcıyı bildirimle boğmamak, fakat zikirden uzaklaştığında ikaz etmek için:
     * - Aynı gün içinde en fazla 1 bildirim (nadiren haftada birkaç kez 2 defa)
     * - Haftada en fazla 3-4 defa bildirim gönderilmesini garanti eder.
     */
    fun canSendNotificationToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayKey = NumberFormatter.getDateKey()
        val lastDate = prefs.getString(KEY_LAST_SENT_DATE, "") ?: ""

        // Aynı gün içinde daha önce gönderildiyse tekrar gönderme
        if (todayKey == lastDate) {
            return false
        }

        // Haftalık sayacı kontrol et ve sıfırla
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val storedWeek = prefs.getInt(KEY_CURRENT_WEEK_OF_YEAR, -1)
        var weeklyCount = prefs.getInt(KEY_WEEKLY_SENT_COUNT, 0)

        if (currentWeek != storedWeek) {
            weeklyCount = 0
            prefs.edit()
                .putInt(KEY_CURRENT_WEEK_OF_YEAR, currentWeek)
                .putInt(KEY_WEEKLY_SENT_COUNT, 0)
                .apply()
        }

        // Haftada en fazla 4 defa bildirim gönder
        if (weeklyCount >= 4) {
            return false
        }

        return true
    }

    /**
     * Bildirim gönderildiğinde kaydeder
     */
    fun recordNotificationSent(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayKey = NumberFormatter.getDateKey()
        val weeklyCount = prefs.getInt(KEY_WEEKLY_SENT_COUNT, 0)

        prefs.edit()
            .putString(KEY_LAST_SENT_DATE, todayKey)
            .putInt(KEY_WEEKLY_SENT_COUNT, weeklyCount + 1)
            .apply()
    }
}
