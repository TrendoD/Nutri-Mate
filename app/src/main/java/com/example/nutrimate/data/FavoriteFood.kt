package com.example.nutrimate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_foods")
data class FavoriteFood(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val foodId: String
)
