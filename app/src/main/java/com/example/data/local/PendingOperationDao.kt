package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PendingOperation

@Dao
interface PendingOperationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(operation: PendingOperation)

    @Query("SELECT * FROM pending_operations WHERE status NOT IN ('applied', 'cancelled') ORDER BY timestamp ASC")
    suspend fun getUnappliedOperations(): List<PendingOperation>

    @Query("SELECT * FROM pending_operations WHERE operationId IN (:ids)")
    suspend fun getOperationsByIds(ids: List<String>): List<PendingOperation>

    @Query("UPDATE pending_operations SET status = :status WHERE operationId IN (:ids)")
    suspend fun updateStatus(ids: List<String>, status: String)

    @Query("UPDATE pending_operations SET status = 'cancelled' WHERE zikirId = :zikirId AND status != 'applied'")
    suspend fun cancelPendingForZikir(zikirId: Int)

    @Query("UPDATE pending_operations SET status = 'cancelled' WHERE status != 'applied'")
    suspend fun cancelAllPending()

    @Query("DELETE FROM pending_operations WHERE status IN ('applied', 'cancelled') AND timestamp < :olderThan")
    suspend fun cleanupOldApplied(olderThan: Long)
}
