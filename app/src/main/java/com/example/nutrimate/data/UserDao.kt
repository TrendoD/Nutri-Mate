package com.example.nutrimate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    
    @Insert
    suspend fun insertUser(user: User)
    
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?
    
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE users SET age = :age, weight = :weight, height = :height, gender = :gender, medicalConditions = :conditions, dailyCalorieTarget = :calories WHERE username = :username")
    suspend fun updateProfile(username: String, age: Int, weight: Float, height: Float, gender: String, conditions: String, calories: Int)
}
