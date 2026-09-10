package com.example.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ReminderSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationSchedulerTest {

    @Test
    fun testGetSlotRequestCode_DeterministicAndBounded() {
        val code1 = NotificationScheduler.getSlotRequestCode(1L)
        val code2 = NotificationScheduler.getSlotRequestCode(2L)
        val code1Again = NotificationScheduler.getSlotRequestCode(1L)
        val code5001 = NotificationScheduler.getSlotRequestCode(5001L)

        // Deterministik
        assertEquals(code1, code1Again)
        assertEquals(10001, code1)
        assertEquals(10002, code2)
        assertEquals(15001, code5001)
        assertNotEquals(code1, code2)
        assertNotEquals(code1, code5001) // Modulo % 5000 çakışması olmamalı

        // Sabit sistem bildirim ID'leri ile asla çakışmaz (>= 10000)
        assertTrue(code1 >= NotificationScheduler.REQUEST_CODE_BASE_SLOT)
        assertTrue(code2 >= NotificationScheduler.REQUEST_CODE_BASE_SLOT)
        assertNotEquals(NotificationScheduler.REQUEST_CODE_TARGET, code1)
        assertNotEquals(NotificationScheduler.REQUEST_CODE_INACTIVITY, code1)
    }

    @Test
    fun testGetSlotRequestCode_ZeroCollisionsAcrossDistinctIds() {
        val testIds = (1L..5000L).map { it } + listOf(5001L, 10000L, 50000L, 1000000L)
        val codeSet = testIds.map { NotificationScheduler.getSlotRequestCode(it) }.toSet()
        assertEquals(testIds.size, codeSet.size) // Her ID için benzersiz ve çakışmasız kod üretilmeli
    }

    @Test
    fun testGetSlotRequestCode_OverflowAndEdgeCases() {
        val codeMax = NotificationScheduler.getSlotRequestCode(Long.MAX_VALUE)
        val codeMin = NotificationScheduler.getSlotRequestCode(Long.MIN_VALUE)
        val codeNeg = NotificationScheduler.getSlotRequestCode(-42L)
        val codeZero = NotificationScheduler.getSlotRequestCode(0L)

        assertEquals(10000, codeZero)
        assertEquals(10042, codeNeg)
        assertTrue(codeMax in NotificationScheduler.REQUEST_CODE_BASE_SLOT..Int.MAX_VALUE)
        assertTrue(codeMin in NotificationScheduler.REQUEST_CODE_BASE_SLOT..Int.MAX_VALUE)
        assertNotEquals(NotificationScheduler.REQUEST_CODE_TARGET, codeMax)
        assertNotEquals(NotificationScheduler.REQUEST_CODE_INACTIVITY, codeMax)
    }

    @Test
    fun testOrphanAlarmCleanup_CancelsDeletedSlotsAndMaintainsActiveSet() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scheduler = NotificationScheduler(context)

        val slot1 = ReminderSlot(id = 1L, hour = 8, minute = 0, isEnabled = true)
        val slot2 = ReminderSlot(id = 2L, hour = 13, minute = 30, isEnabled = true)
        val slot3 = ReminderSlot(id = 3L, hour = 20, minute = 15, isEnabled = true)

        // 1. 3 slot ile planla
        scheduler.scheduleDailyReminders(listOf(slot1, slot2, slot3), isEnabled = true)
        assertEquals(setOf(1L, 2L, 3L), scheduler.getActiveScheduledSlotIds())

        // 2. Slot 2 silindiğinde (yalnızca slot1 ve slot3 gönderildiğinde), slot 2 orphan olarak temizlenmeli
        scheduler.scheduleDailyReminders(listOf(slot1, slot3), isEnabled = true)
        val activeAfterDeletion = scheduler.getActiveScheduledSlotIds()
        assertEquals(setOf(1L, 3L), activeAfterDeletion)
        assertFalse(activeAfterDeletion.contains(2L))

        // 3. Slot 3 pasife alındığında (isEnabled = false), aktif alarm setinden düşmeli
        val slot3Disabled = slot3.copy(isEnabled = false)
        scheduler.scheduleDailyReminders(listOf(slot1, slot3Disabled), isEnabled = true)
        assertEquals(setOf(1L), scheduler.getActiveScheduledSlotIds())

        // 4. Genel hatırlatıcı kapatıldığında (isEnabled = false), tüm alarmlar temizlenmeli
        scheduler.scheduleDailyReminders(listOf(slot1, slot3Disabled), isEnabled = false)
        assertTrue(scheduler.getActiveScheduledSlotIds().isEmpty())
    }
}
