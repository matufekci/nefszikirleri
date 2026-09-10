package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailyAggregate
import com.example.data.model.ZikirHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ZikirHistoryDao {
    @Query("SELECT * FROM zikir_history ORDER BY timestamp DESC, id DESC")
    fun getAllHistory(): Flow<List<ZikirHistory>>

    @Query("""
        SELECT * FROM zikir_history 
        WHERE timestamp >= :fromTimestamp AND type = 'add' AND amount <= :maxAmount 
        ORDER BY timestamp DESC, id DESC
    """)
    suspend fun getRecentManualHistoryDirect(fromTimestamp: Long, maxAmount: Long = 10000L): List<ZikirHistory>

    @Query("SELECT * FROM zikir_history ORDER BY timestamp DESC, id DESC LIMIT :limit OFFSET :offset")
    suspend fun getHistoryPagedDirect(limit: Int, offset: Int): List<ZikirHistory>

    @Query("SELECT COUNT(*) FROM zikir_history")
    suspend fun getHistoryCountDirect(): Int

    @Query("SELECT * FROM zikir_history ORDER BY timestamp DESC, id DESC")
    suspend fun getAllHistoryDirect(): List<ZikirHistory>

    // Hamle 13: Memory-hardened pagination for large histories (10k+ entries)
    // Use this for UI pagination instead of loading all history into RAM
    @Query("SELECT * FROM zikir_history ORDER BY timestamp DESC, id DESC LIMIT :limit OFFSET :offset")
    fun observeHistoryPaged(limit: Int, offset: Int): Flow<List<ZikirHistory>>

    @Query("SELECT * FROM zikir_history WHERE dateKey = :dateKey ORDER BY timestamp DESC, id DESC LIMIT :limit OFFSET :offset")
    suspend fun getHistoryByDatePagedDirect(dateKey: String, limit: Int, offset: Int): List<ZikirHistory>

    @Query("SELECT COUNT(*) FROM zikir_history WHERE dateKey = :dateKey")
    suspend fun getHistoryCountByDateDirect(dateKey: String): Int

    // For statistics: get total count without loading all entries
    @Query("SELECT COUNT(*) FROM zikir_history WHERE zikirId = :zikirId")
    suspend fun getHistoryCountForZikirDirect(zikirId: Int): Int

    @Query("SELECT * FROM zikir_history WHERE zikirId = :zikirId ORDER BY timestamp DESC, id DESC")
    fun getHistoryForZikir(zikirId: Int): Flow<List<ZikirHistory>>

    @Query("SELECT * FROM zikir_history ORDER BY timestamp DESC, id DESC LIMIT :limit")
    fun observeRecentHistory(limit: Int): Flow<List<ZikirHistory>>

    @Query("SELECT * FROM zikir_history ORDER BY timestamp DESC, id DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ZikirHistory>

    @Query("SELECT * FROM zikir_history WHERE zikirId = :zikirId ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun getMostRecentForZikir(zikirId: Int): ZikirHistory?

    // a) Günlük istatistik (grafik ve özet için)
    @Query("""
        SELECT dateKey, 
               SUM(CASE WHEN type = 'add' THEN amount ELSE -amount END) AS total
        FROM zikir_history 
        WHERE timestamp >= :fromTimestamp
        GROUP BY dateKey 
        ORDER BY dateKey
    """)
    fun observeDailyStats(fromTimestamp: Long): Flow<List<DailyAggregate>>

    @Query("""
        SELECT dateKey, 
               SUM(CASE WHEN type = 'add' THEN amount ELSE -amount END) AS total
        FROM zikir_history 
        WHERE timestamp >= :fromTimestamp
        GROUP BY dateKey 
        ORDER BY dateKey
    """)
    suspend fun getDailyStatsDirect(fromTimestamp: Long): List<DailyAggregate>

    // b) Belirli bir zikir için günlük istatistik
    @Query("""
        SELECT dateKey, 
               SUM(CASE WHEN type = 'add' THEN amount ELSE -amount END) AS total
        FROM zikir_history 
        WHERE zikirId = :zikirId AND timestamp >= :fromTimestamp
        GROUP BY dateKey 
        ORDER BY dateKey
    """)
    fun observeDailyStatsForZikir(zikirId: Int, fromTimestamp: Long): Flow<List<DailyAggregate>>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'add' THEN amount ELSE -amount END), 0)
        FROM zikir_history 
        WHERE zikirId = :zikirId AND dateKey = :dateKey
    """)
    fun observeTodayRecitedForZikir(zikirId: Int, dateKey: String): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'add' THEN amount ELSE -amount END), 0)
        FROM zikir_history 
        WHERE zikirId = :zikirId AND dateKey = :dateKey
    """)
    suspend fun getTodayRecitedForZikirDirect(zikirId: Int, dateKey: String): Long

    // c) Streak hesaplama (ardışık gün sayısı için distinct dateKey listesi)
    @Query("""
        SELECT DISTINCT dateKey 
        FROM zikir_history 
        WHERE type = 'add' 
        ORDER BY dateKey DESC
    """)
    fun observeDistinctActiveDates(): Flow<List<String>>

    @Query("""
        SELECT DISTINCT dateKey 
        FROM zikir_history 
        WHERE type = 'add' 
        ORDER BY dateKey DESC
    """)
    suspend fun getDistinctActiveDatesDirect(): List<String>

    // d) Toplam zikir sayısı (tüm zamanlar, RAM'e tüm kayıtları çekmeden)
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'add' THEN amount ELSE -amount END), 0) 
        FROM zikir_history
    """)
    fun observeTotalRecited(): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'add' THEN amount ELSE -amount END), 0) 
        FROM zikir_history
    """)
    suspend fun getTotalRecitedDirect(): Long

    // e) Son 30 gün ortalaması (aggregate)
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'add' THEN amount ELSE -amount END) / 30, 0)
        FROM zikir_history 
        WHERE timestamp >= :fromTimestamp
    """)
    fun observe30DayAverage(fromTimestamp: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'add' THEN amount ELSE -amount END) / 30, 0)
        FROM zikir_history 
        WHERE timestamp >= :fromTimestamp
    """)
    suspend fun get30DayAverageDirect(fromTimestamp: Long): Long

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
}

typealias HistoryDao = ZikirHistoryDao
