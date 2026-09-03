package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Zikir
import kotlinx.coroutines.flow.Flow

@Dao
interface ZikirDao {
    @Query("SELECT * FROM zikirs ORDER BY id ASC")
    fun getAllZikirs(): Flow<List<Zikir>>

    @Query("SELECT * FROM zikirs ORDER BY id ASC")
    suspend fun getAllZikirsDirect(): List<Zikir>

    @Query("SELECT * FROM zikirs WHERE id = :id")
    suspend fun getZikirById(id: Int): Zikir?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(zikirs: List<Zikir>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(zikir: Zikir)

    @Update
    suspend fun update(zikir: Zikir)

    @Query("UPDATE zikirs SET count = count + :amount, startedAt = CASE WHEN startedAt IS NULL OR startedAt = 0 THEN :now ELSE startedAt END, completedAt = CASE WHEN count + :amount >= target THEN COALESCE(completedAt, :now) ELSE NULL END WHERE id = :id")
    suspend fun incrementZikirCount(id: Int, amount: Long, now: Long)

    @Query("UPDATE zikirs SET count = MAX(0, count - :amount), completedAt = CASE WHEN MAX(0, count - :amount) < target THEN NULL ELSE completedAt END WHERE id = :id")
    suspend fun decrementZikirCount(id: Int, amount: Long)

    @Query("UPDATE zikirs SET count = 0, startedAt = NULL, completedAt = NULL WHERE id = :id")
    suspend fun resetZikir(id: Int)

    @Query("UPDATE zikirs SET count = 0, startedAt = NULL, completedAt = NULL")
    suspend fun resetAllZikirs()

    @Query("SELECT COUNT(*) FROM zikirs")
    suspend fun getCount(): Int
}
