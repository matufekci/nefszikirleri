package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "pending_operations",
    indices = [
        Index(value = ["status"])
    ]
)
data class PendingOperation(
    @PrimaryKey
    val operationId: String,
    val zikirId: Int,
    val amount: Long,
    val timestamp: Long,
    val dateKey: String,
    val status: String
)
