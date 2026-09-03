package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ZikirHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM zikir_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ZikirHistory>>

    @Query("SELECT * FROM zikir_history ORDER BY timestamp DESC")
    suspend fun getAllHistoryDirect(): List<ZikirHistory>

    @Query("SELECT * FROM zikir_history WHERE zikirId = :zikirId ORDER BY timestamp DESC")
    fun getHistoryForZikir(zikirId: Int): Flow<List<ZikirHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ZikirHistory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ZikirHistory>)

    @Query("DELETE FROM zikir_history WHERE zikirId = :zikirId")
    suspend fun deleteForZikir(zikirId: Int)

    @Query("DELETE FROM zikir_history")
    suspend fun deleteAll()

    @Query("DELETE FROM zikir_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM zikir_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ZikirHistory>

    @Query("SELECT * FROM zikir_history WHERE zikirId = :zikirId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecentForZikir(zikirId: Int): ZikirHistory?
}
