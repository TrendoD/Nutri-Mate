package com.example.nutrimate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class Food(
    @PrimaryKey val id: String,
    val name: String,
    val calories: Float,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val servingSize: Float,
    val servingUnit: String,
    val category: String = "Other", // Category: Fruit, Vegetable, Protein, Grain, Dairy, Other
    val sugar: Float = 0f,
    val fiber: Float = 0f,
    val sodium: Float = 0f,
    val isCustom: Boolean = false,
    val createdBy: String? = null
)
