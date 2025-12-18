package com.habittracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val reminderTime: String? = null, // Format: "HH:mm"
    val reminderEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)
