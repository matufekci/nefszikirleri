package com.example.util

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Yalnızca hareketsizlik emniyet ağı kaldı: slotlu/hedefli hatırlatıcılar
 * kaldırıldı, tempo bildirimlerini DailyEvaluationWorker yürütüyor.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationSchedulerTest {

    @Test
    fun testInactivityAlert_UcGunSonrasinaKurulurVeKapatilincaIptalOlur() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scheduler = NotificationScheduler(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadow = shadowOf(alarmManager)

        val before = System.currentTimeMillis()
        scheduler.scheduleInactivityAlert(true)

        val alarms = shadow.scheduledAlarms
        assertEquals(1, alarms.size)
        val delta = alarms[0].triggerAtTime - before
        val expected = AdaptiveReminderManager.INACTIVITY_TRIGGER_DAYS * 24L * 60 * 60 * 1000L
        assertTrue(
            "hareketsizlik alarmı ~3 gün sonrasına kurulmalı, delta=$delta ms",
            delta in (expected - 60_000L)..(expected + 60_000L)
        )

        // Kapatılınca alarm iptal olmalı
        scheduler.scheduleInactivityAlert(false)
        assertTrue(shadow.scheduledAlarms.isEmpty())
    }

    @Test
    fun testCancelAllScheduledAlarms_HareketsizlikAlarminiTemizler() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scheduler = NotificationScheduler(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadow = shadowOf(alarmManager)

        scheduler.scheduleInactivityAlert(true)
        assertEquals(1, shadow.scheduledAlarms.size)

        scheduler.cancelAllScheduledAlarms()
        assertTrue(shadow.scheduledAlarms.isEmpty())
    }

    @Test
    fun testHareketsizlikEsikleri_Katilasti() {
        // Sınırlar katılaştırıldı: 2 gün eşik / 3 gün tetik (eskiden 3/4).
        assertEquals(2, AdaptiveReminderManager.INACTIVITY_THRESHOLD_DAYS)
        assertEquals(3, AdaptiveReminderManager.INACTIVITY_TRIGGER_DAYS)
        assertTrue(AdaptiveReminderManager.INACTIVITY_THRESHOLD_DAYS < AdaptiveReminderManager.INACTIVITY_TRIGGER_DAYS)
    }
}
