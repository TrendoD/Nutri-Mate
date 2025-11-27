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

@Database(entities = [User::class, Food::class, FoodLog::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    
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
                .fallbackToDestructiveMigration()
                .addCallback(FoodDatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class FoodDatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(context, database.foodDao())
                    }
                }
            }
            
            suspend fun populateDatabase(context: Context, foodDao: FoodDao) {
                if (foodDao.getFoodCount() == 0) {
                    try {
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
                                servingUnit = item.getString("servingUnit")
                            ))
                        }
                        
                        foodDao.insertAll(foods)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}