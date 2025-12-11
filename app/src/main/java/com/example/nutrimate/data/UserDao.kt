package com.example.nutrimate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    
    @Insert
    suspend fun insertUser(user: User)
    
    @Query("SELECT * FROM users WHERE (LOWER(username) = LOWER(:username) OR LOWER(email) = LOWER(:username)) AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?
    
    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("UPDATE users SET age = :age, weight = :weight, height = :height, gender = :gender, medicalConditions = :conditions, dailyCalorieTarget = :calories, activityLevel = :activityLevel, dietGoal = :dietGoal, targetWeight = :targetWeight, allergies = :allergies, profilePicture = :profilePicture, fullName = :fullName, email = :email WHERE username = :username")
    suspend fun updateProfile(username: String, age: Int, weight: Float, height: Float, gender: String, conditions: String, calories: Int, activityLevel: String, dietGoal: String, targetWeight: Float, allergies: String, profilePicture: String, fullName: String, email: String)

    @Query("UPDATE users SET carbsTarget = :carbsTarget, proteinTarget = :proteinTarget, fatTarget = :fatTarget, sugarLimit = :sugarLimit, sodiumLimit = :sodiumLimit, fiberTarget = :fiberTarget, waterTarget = :waterTarget WHERE username = :username")
    suspend fun updateNutritionTargets(username: String, carbsTarget: Float, proteinTarget: Float, fatTarget: Float, sugarLimit: Float, sodiumLimit: Float, fiberTarget: Float, waterTarget: Int)

    @Query("UPDATE users SET dailyCalorieTarget = :calorieTarget, carbsTarget = :carbsTarget, proteinTarget = :proteinTarget, fatTarget = :fatTarget, sugarLimit = :sugarLimit, sodiumLimit = :sodiumLimit, fiberTarget = :fiberTarget, waterTarget = :waterTarget WHERE username = :username")
    suspend fun updateAllNutritionTargets(username: String, calorieTarget: Int, carbsTarget: Float, proteinTarget: Float, fatTarget: Float, sugarLimit: Float, sodiumLimit: Float, fiberTarget: Float, waterTarget: Int)
    
    // Delete user account
    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUserByUsername(username: String)
}
