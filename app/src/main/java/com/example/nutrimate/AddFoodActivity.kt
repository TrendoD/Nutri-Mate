package com.example.nutrimate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.data.FavoriteFood
import com.example.nutrimate.data.Food
import com.example.nutrimate.data.FoodLog
import com.example.nutrimate.ui.addfood.AddFoodDialog
import com.example.nutrimate.ui.addfood.AddFoodScreen
import com.example.nutrimate.ui.addfood.AddFoodScreenState
import com.example.nutrimate.ui.addfood.CreateCustomFoodDialog
import com.example.nutrimate.ui.addfood.FoodItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddFoodActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private var username: String = ""
    private var mealType: String = ""
    private var selectedDate: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // State holders
    private var screenState by mutableStateOf(AddFoodScreenState())
    private var favoriteIds by mutableStateOf<Set<String>>(emptySet())
    private var showAddDialog by mutableStateOf(false)
    private var selectedFood by mutableStateOf<Food?>(null)
    private var showCreateCustomDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(this)
        username = intent.getStringExtra("USERNAME") ?: ""
        mealType = intent.getStringExtra("MEAL_TYPE") ?: "Breakfast"
        selectedDate = intent.getStringExtra("DATE") ?: dateFormat.format(Date())

        if (username.isEmpty()) {
            finish()
            return
        }

        screenState = screenState.copy(mealType = mealType)

        loadFavorites()
        loadFilteredFoods()

        setContent {
            AddFoodScreen(
                    state = screenState,
                    onSearchQueryChange = { query ->
                        screenState = screenState.copy(searchQuery = query)
                        if (query.length >= 3) {
                            searchFood(query)
                        } else if (query.isEmpty()) {
                            loadFilteredFoods()
                        }
                    },
                    onFilterChange = { filter ->
                        screenState = screenState.copy(
                            currentFilter = filter,
                            searchQuery = ""
                        )
                        loadFilteredFoods()
                    },
                    onFoodClick = { foodItem ->
                        // Find the original Food object
                        lifecycleScope.launch {
                            val food = database.foodDao().getFoodById(foodItem.id)
                            food?.let {
                                selectedFood = it
                                showAddDialog = true
                            }
                        }
                    },
                    onFavoriteClick = { foodItem ->
                        toggleFavorite(foodItem)
                    },
                    onCreateCustomFoodClick = {
                        showCreateCustomDialog = true
                    },
                    onBackClick = {
                        finish()
                    }
                )

                // Add Food Dialog
                if (showAddDialog && selectedFood != null) {
                    val food = selectedFood!!
                    AddFoodDialog(
                        foodName = food.name,
                        servingUnit = food.servingUnit,
                        caloriesPerServing = food.calories,
                        carbsPerServing = food.carbs,
                        proteinPerServing = food.protein,
                        fatPerServing = food.fat,
                        sugarPerServing = food.sugar,
                        fiberPerServing = food.fiber,
                        sodiumPerServing = food.sodium,
                        onDismiss = {
                            showAddDialog = false
                            selectedFood = null
                        },
                        onAdd = { qty ->
                            saveFoodLog(food, qty)
                            showAddDialog = false
                            selectedFood = null
                        }
                    )
                }

                // Create Custom Food Dialog
                if (showCreateCustomDialog) {
                    CreateCustomFoodDialog(
                        onDismiss = {
                            showCreateCustomDialog = false
                        },
                        onSave = { name, servingSize, servingUnit, calories, carbs, protein, fat, sugar, fiber, sodium, displayCategory ->
                            val internalCategory = mapCategoryToInternal(displayCategory)
                            val customFood = Food(
                                id = "custom_${UUID.randomUUID()}",
                                name = name,
                                calories = calories,
                                carbs = carbs,
                                protein = protein,
                                fat = fat,
                                sugar = sugar,
                                fiber = fiber,
                                sodium = sodium,
                                servingSize = servingSize,
                                servingUnit = servingUnit,
                                category = internalCategory,
                                isCustom = true,
                                createdBy = username
                            )

                            lifecycleScope.launch {
                                database.foodDao().insertFood(customFood)
                                Toast.makeText(this@AddFoodActivity, "Makanan kustom dibuat!", Toast.LENGTH_SHORT).show()
                                loadFilteredFoods()
                                showCreateCustomDialog = false
                            }
                        }
                    )
                }
        }
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            val favorites = database.foodDao().getFavoriteFoods(username)
            favoriteIds = favorites.map { it.id }.toSet()
            updateFoodsWithFavorites()
        }
    }

    private fun loadFilteredFoods() {
        lifecycleScope.launch {
            screenState = screenState.copy(isLoading = true)
            
            val foods = when (screenState.currentFilter) {
                "Recent" -> database.foodDao().getRecentFoods(username, 20)
                "Favorites" -> database.foodDao().getFavoriteFoods(username)
                "Fruit", "Vegetable", "Protein", "Grain", "Dairy" ->
                    database.foodDao().getFoodsByCategory(screenState.currentFilter)
                else -> database.foodDao().searchFoods("")
            }

            val foodItems = foods.map { food ->
                FoodItem(
                    id = food.id,
                    name = food.name,
                    servingSize = food.servingSize,
                    servingUnit = food.servingUnit,
                    calories = food.calories,
                    carbs = food.carbs,
                    protein = food.protein,
                    fat = food.fat,
                    sugar = food.sugar,
                    fiber = food.fiber,
                    sodium = food.sodium,
                    category = food.category,
                    isFavorite = favoriteIds.contains(food.id)
                )
            }

            screenState = screenState.copy(
                foods = foodItems,
                isLoading = false
            )
        }
    }

    private fun searchFood(query: String) {
        lifecycleScope.launch {
            val foods = if (screenState.currentFilter == "All" || 
                           screenState.currentFilter == "Recent" || 
                           screenState.currentFilter == "Favorites") {
                database.foodDao().searchFoods(query)
            } else {
                database.foodDao().searchFoodsByCategory(query, screenState.currentFilter)
            }

            val foodItems = foods.map { food ->
                FoodItem(
                    id = food.id,
                    name = food.name,
                    servingSize = food.servingSize,
                    servingUnit = food.servingUnit,
                    calories = food.calories,
                    carbs = food.carbs,
                    protein = food.protein,
                    fat = food.fat,
                    sugar = food.sugar,
                    fiber = food.fiber,
                    sodium = food.sodium,
                    category = food.category,
                    isFavorite = favoriteIds.contains(food.id)
                )
            }

            screenState = screenState.copy(foods = foodItems)
        }
    }

    private fun toggleFavorite(foodItem: FoodItem) {
        lifecycleScope.launch {
            if (favoriteIds.contains(foodItem.id)) {
                // Remove from favorites
                database.foodDao().deleteFavorite(username, foodItem.id)
                favoriteIds = favoriteIds - foodItem.id
                Toast.makeText(this@AddFoodActivity, "Dihapus dari favorit", Toast.LENGTH_SHORT).show()
            } else {
                // Add to favorites
                val favorite = FavoriteFood(username = username, foodId = foodItem.id)
                database.foodDao().insertFavorite(favorite)
                favoriteIds = favoriteIds + foodItem.id
                Toast.makeText(this@AddFoodActivity, "Ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
            }

            updateFoodsWithFavorites()

            // Refresh list if in favorites view
            if (screenState.currentFilter == "Favorites") {
                loadFilteredFoods()
            }
        }
    }

    private fun updateFoodsWithFavorites() {
        screenState = screenState.copy(
            foods = screenState.foods.map { food ->
                food.copy(isFavorite = favoriteIds.contains(food.id))
            }
        )
    }

    private fun mapCategoryToInternal(displayCategory: String): String {
        return when (displayCategory) {
            "Buah" -> "Fruit"
            "Sayur" -> "Vegetable"
            "Protein" -> "Protein"
            "Biji-bijian" -> "Grain"
            "Produk Susu" -> "Dairy"
            else -> "Other"
        }
    }

    private fun saveFoodLog(food: Food, qty: Float) {
        lifecycleScope.launch {
            val log = FoodLog(
                username = username,
                foodId = food.id,
                servingQty = qty,
                mealType = mealType,
                date = selectedDate
            )
            database.foodDao().insertFoodLog(log)
            Toast.makeText(this@AddFoodActivity, "Ditambahkan ${food.name}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}