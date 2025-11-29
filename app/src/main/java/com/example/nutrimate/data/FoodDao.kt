package com.example.nutrimate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FoodDao {
    // Food Database Methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<Food>)

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%'")
    suspend fun searchFoods(query: String): List<Food>

    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: String): Food?
    
    // Food Log Methods
    @Insert
    suspend fun insertFoodLog(foodLog: FoodLog)
    
    @Query("SELECT * FROM food_logs WHERE username = :username AND date = :date")
    suspend fun getFoodLogsByDate(username: String, date: String): List<FoodLog>

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodLog(id: Int)
    
    @Query("UPDATE food_logs SET servingQty = :servingQty WHERE id = :id")
    suspend fun updateFoodLogQuantity(id: Int, servingQty: Float)
    
    @Query("SELECT * FROM food_logs WHERE username = :username AND date = :date")
    suspend fun getFoodLogsWithFoodByDate(username: String, date: String): List<FoodLog>
    
    // Helper to check if DB is empty
    @Query("SELECT COUNT(*) FROM foods")
    suspend fun getFoodCount(): Int
}
