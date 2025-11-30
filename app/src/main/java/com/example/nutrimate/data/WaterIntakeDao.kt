package com.example.nutrimate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WaterIntakeDao {
    
    @Insert
    suspend fun insertWaterIntake(waterIntake: WaterIntake)
    
    @Query("SELECT * FROM water_intakes WHERE username = :username AND date = :date")
    suspend fun getWaterIntakesByDate(username: String, date: String): List<WaterIntake>
    
    @Query("SELECT SUM(amount) FROM water_intakes WHERE username = :username AND date = :date")
    suspend fun getTotalWaterIntakeByDate(username: String, date: String): Int?
    
    @Query("DELETE FROM water_intakes WHERE id = :id")
    suspend fun deleteWaterIntake(id: Int)
    
    @Query("DELETE FROM water_intakes WHERE username = :username AND date = :date")
    suspend fun clearWaterIntakeForDate(username: String, date: String)
    
    // Delete all water intake for a user (for Settings)
    @Query("DELETE FROM water_intakes WHERE username = :username")
    suspend fun deleteAllWaterIntakeByUser(username: String)
}
