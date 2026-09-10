package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zikirs")
data class Zikir(
    @PrimaryKey val id: Int,
    val target: Long,
    val count: Long = 0L,
    val startedAt: Long? = null,
    val completedAt: Long? = null
)
