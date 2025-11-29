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
    val profilePicture: String = ""
)
