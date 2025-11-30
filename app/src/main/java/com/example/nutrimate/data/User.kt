package com.example.nutrimate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val email: String,
    val username: String,
    val password: String,
    val age: Int = 0,
    val weight: Float = 0f,
    val height: Float = 0f,
    val gender: String = "",
    val medicalConditions: String = "", // Stored as comma-separated string or JSON
    val dailyCalorieTarget: Int = 0,
    val activityLevel: String = "Sedentary",
    val dietGoal: String = "Maintain",
    val targetWeight: Float = 0f,
    val allergies: String = "",
    val profilePicture: String = "",
    // Nutrition Targets
    val carbsTarget: Float = 0f,      // Target carbs in grams
    val proteinTarget: Float = 0f,    // Target protein in grams
    val fatTarget: Float = 0f,        // Target fat in grams
    val sugarLimit: Float = 0f,       // Sugar limit in grams
    val sodiumLimit: Float = 0f,      // Sodium limit in mg
    val fiberTarget: Float = 0f,      // Fiber target in grams
    val waterTarget: Int = 2000       // Water target in ml
)
