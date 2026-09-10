package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "zikir_history",
    foreignKeys = [
        ForeignKey(
            entity = Zikir::class,
            parentColumns = ["id"],
            childColumns = ["zikirId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["zikirId"]),
        Index(value = ["dateKey"]),
        Index(value = ["timestamp"]),
        Index(value = ["eventId"], unique = true),
        Index(value = ["zikirId", "dateKey"])
    ]
)
data class ZikirHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String = java.util.UUID.randomUUID().toString(),
    val zikirId: Int,
    val amount: Long,
    val type: String, // "add" or "remove"
    val timestamp: Long = System.currentTimeMillis(),
    val dateKey: String // "YYYY-MM-DD"
)
