package com.example.nutrimate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val foodId: String,
    val servingQty: Float,
    val mealType: String,
    val date: String
)
