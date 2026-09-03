package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ReminderSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder_slots ORDER BY id ASC")
    fun getAllSlots(): Flow<List<ReminderSlot>>

    @Query("SELECT * FROM reminder_slots ORDER BY id ASC")
    suspend fun getAllSlotsList(): List<ReminderSlot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(slot: ReminderSlot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slots: List<ReminderSlot>)

    @Update
    suspend fun update(slot: ReminderSlot)

    @Query("DELETE FROM reminder_slots WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM reminder_slots")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM reminder_slots")
    suspend fun getCount(): Int
}
