package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.AdaptiveReminderManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdaptiveReminderQuotaTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val prefs = context.getSharedPreferences("adaptive_spiritual_reminder_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun testTryReserveQuota_ConcurrentFirstReservationSucceeds_SecondFailsSameDay() {
        assertTrue(AdaptiveReminderManager.canSendNotificationToday(context))

        val firstReservation = AdaptiveReminderManager.tryReserveQuota(context)
        assertNotNull(firstReservation)

        // Second reservation on the same day must be rejected (null)
        val secondReservation = AdaptiveReminderManager.tryReserveQuota(context)
        assertNull(secondReservation)
    }

    @Test
    fun testRollbackQuotaReservation_RestoresQuotaOnFailure() {
        val reservation = AdaptiveReminderManager.tryReserveQuota(context)
        assertNotNull(reservation)

        // Simulating notification dispatch failure -> Rollback reservation
        AdaptiveReminderManager.rollbackQuotaReservation(context, reservation!!)

        // Quota should be available again
        assertTrue(AdaptiveReminderManager.canSendNotificationToday(context))
        val retryReservation = AdaptiveReminderManager.tryReserveQuota(context)
        assertNotNull(retryReservation)
    }

    @Test
    fun testGetWeekYearKey_DeterministicFormatAndCrossYearIsolation() {
        val cal2026W1 = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, 2026)
            set(java.util.Calendar.WEEK_OF_YEAR, 1)
            set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
        }
        val key2026W1 = AdaptiveReminderManager.getWeekYearKey(cal2026W1)
        assertEquals("2026-W01", key2026W1)

        val cal2027W1 = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, 2027)
            set(java.util.Calendar.WEEK_OF_YEAR, 1)
            set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
        }
        val key2027W1 = AdaptiveReminderManager.getWeekYearKey(cal2027W1)
        assertEquals("2027-W01", key2027W1)

        org.junit.Assert.assertNotEquals(key2026W1, key2027W1)
    }
}
