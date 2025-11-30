package com.example.nutrimate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

@Database(entities = [User::class, Food::class, FoodLog::class, WaterIntake::class, FavoriteFood::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun waterIntakeDao(): WaterIntakeDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutrimate_database"
                )
                .fallbackToDestructiveMigration() // This will drop and recreate DB on version change
                .addCallback(FoodDatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class FoodDatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Populate database when it's created for the first time
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(context, database.foodDao())
                    }
                }
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Also check on open in case database was cleared
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            populateDatabase(context, database.foodDao())
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Log error but don't crash the app
                        }
                    }
                }
            }
            
            suspend fun populateDatabase(context: Context, foodDao: FoodDao) {
                try {
                    // Only populate if database is empty
                    if (foodDao.getFoodCount() == 0) {
                        val jsonString = context.assets.open("food_data.json").bufferedReader().use { it.readText() }
                        val jsonArray = JSONArray(jsonString)
                        val foods = mutableListOf<Food>()
                        
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            foods.add(Food(
                                id = item.getString("id"),
                                name = item.getString("name"),
                                calories = item.getDouble("calories").toFloat(),
                                carbs = item.getDouble("carbs").toFloat(),
                                protein = item.getDouble("protein").toFloat(),
                                fat = item.getDouble("fat").toFloat(),
                                servingSize = item.getDouble("servingSize").toFloat(),
                                servingUnit = item.getString("servingUnit"),
                                // Handle optional new fields with defaults
                                category = if (item.has("category")) item.getString("category") else "Other",
                                sugar = if (item.has("sugar")) item.getDouble("sugar").toFloat() else 0f,
                                fiber = if (item.has("fiber")) item.getDouble("fiber").toFloat() else 0f,
                                sodium = if (item.has("sodium")) item.getDouble("sodium").toFloat() else 0f,
                                isCustom = false,
                                createdBy = null
                            ))
                        }
                        
                        if (foods.isNotEmpty()) {
                            foodDao.insertAll(foods)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Log error but don't crash - app can still work without pre-populated foods
                }
            }
        }
    }
}