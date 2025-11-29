package com.example.nutrimate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_intakes")
data class WaterIntake(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val amount: Int, // in ml
    val date: String, // yyyy-MM-dd format
    val time: String = "" // HH:mm format (optional)
)
