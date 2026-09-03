package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zikir_history")
data class ZikirHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val zikirId: Int,
    val amount: Long,
    val type: String, // "add" or "remove"
    val timestamp: Long = System.currentTimeMillis(),
    val dateKey: String // "YYYY-MM-DD"
)
