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
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food)

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%'")
    suspend fun searchFoods(query: String): List<Food>
    
    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' AND category = :category")
    suspend fun searchFoodsByCategory(query: String, category: String): List<Food>
    
    @Query("SELECT DISTINCT category FROM foods ORDER BY category")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: String): Food?
    
    @Query("SELECT * FROM foods WHERE category = :category")
    suspend fun getFoodsByCategory(category: String): List<Food>
    
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
    
    // Recent Foods - Get most frequently logged foods
    @Query("""
        SELECT f.* FROM foods f 
        INNER JOIN food_logs fl ON f.id = fl.foodId 
        WHERE fl.username = :username 
        GROUP BY f.id 
        ORDER BY COUNT(*) DESC, MAX(fl.date) DESC 
        LIMIT :limit
    """)
    suspend fun getRecentFoods(username: String, limit: Int = 10): List<Food>
    
    // Favorite Foods Methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteFood)
    
    @Query("DELETE FROM favorite_foods WHERE username = :username AND foodId = :foodId")
    suspend fun deleteFavorite(username: String, foodId: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_foods WHERE username = :username AND foodId = :foodId)")
    suspend fun isFavorite(username: String, foodId: String): Boolean
    
    @Query("""
        SELECT f.* FROM foods f 
        INNER JOIN favorite_foods ff ON f.id = ff.foodId 
        WHERE ff.username = :username 
        ORDER BY f.name
    """)
    suspend fun getFavoriteFoods(username: String): List<Food>
    
    // Helper to check if DB is empty
    @Query("SELECT COUNT(*) FROM foods")
    suspend fun getFoodCount(): Int
}
