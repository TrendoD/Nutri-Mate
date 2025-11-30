package com.example.nutrimate

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.data.FavoriteFood
import com.example.nutrimate.data.Food
import com.example.nutrimate.data.FoodLog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddFoodActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var etSearch: EditText
    private lateinit var rvFoodSearch: RecyclerView
    private lateinit var tvMealType: TextView
    private lateinit var adapter: FoodSearchAdapter
    
    // Filter buttons
    private lateinit var btnFilterAll: Button
    private lateinit var btnFilterRecent: Button
    private lateinit var btnFilterFavorites: Button
    private lateinit var btnCategoryFruit: Button
    private lateinit var btnCategoryVegetable: Button
    private lateinit var btnCategoryProtein: Button
    private lateinit var btnCategoryGrain: Button
    private lateinit var btnCategoryDairy: Button
    private lateinit var btnCreateCustomFood: Button
    
    private var username: String = ""
    private var mealType: String = ""
    private var selectedDate: String = ""
    private var currentFilter: String = "All"
    private var favoriteIds: MutableSet<String> = mutableSetOf()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        database = AppDatabase.getDatabase(this)
        username = intent.getStringExtra("USERNAME") ?: ""
        mealType = intent.getStringExtra("MEAL_TYPE") ?: "Breakfast"
        selectedDate = intent.getStringExtra("DATE") ?: dateFormat.format(Date())
        
        if (username.isEmpty()) {
            finish()
            return
        }

        initViews()
        loadFavorites()
        loadFilteredFoods()
    }

    private fun initViews() {
        tvMealType = findViewById(R.id.tvMealType)
        tvMealType.text = "Add to $mealType"

        etSearch = findViewById(R.id.etSearch)
        rvFoodSearch = findViewById(R.id.rvFoodSearch)
        
        // Filter buttons
        btnFilterAll = findViewById(R.id.btnFilterAll)
        btnFilterRecent = findViewById(R.id.btnFilterRecent)
        btnFilterFavorites = findViewById(R.id.btnFilterFavorites)
        btnCategoryFruit = findViewById(R.id.btnCategoryFruit)
        btnCategoryVegetable = findViewById(R.id.btnCategoryVegetable)
        btnCategoryProtein = findViewById(R.id.btnCategoryProtein)
        btnCategoryGrain = findViewById(R.id.btnCategoryGrain)
        btnCategoryDairy = findViewById(R.id.btnCategoryDairy)
        btnCreateCustomFood = findViewById(R.id.btnCreateCustomFood)
        
        rvFoodSearch.layoutManager = LinearLayoutManager(this)
        adapter = FoodSearchAdapter(
            onFoodClick = { food -> showAddDialog(food) },
            onFavoriteClick = { food -> toggleFavorite(food) }
        )
        rvFoodSearch.adapter = adapter
        
        // Search functionality
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.length >= 3) {
                    searchFood(query)
                } else if (query.isEmpty()) {
                    loadFilteredFoods()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        // Filter button listeners
        btnFilterAll.setOnClickListener { setFilter("All") }
        btnFilterRecent.setOnClickListener { setFilter("Recent") }
        btnFilterFavorites.setOnClickListener { setFilter("Favorites") }
        btnCategoryFruit.setOnClickListener { setFilter("Fruit") }
        btnCategoryVegetable.setOnClickListener { setFilter("Vegetable") }
        btnCategoryProtein.setOnClickListener { setFilter("Protein") }
        btnCategoryGrain.setOnClickListener { setFilter("Grain") }
        btnCategoryDairy.setOnClickListener { setFilter("Dairy") }
        
        btnCreateCustomFood.setOnClickListener { showCreateCustomFoodDialog() }
        
        updateFilterButtons()
    }
    
    private fun setFilter(filter: String) {
        currentFilter = filter
        updateFilterButtons()
        loadFilteredFoods()
        etSearch.setText("") // Clear search when changing filter
    }
    
    private fun updateFilterButtons() {
        // Reset all buttons
        val buttons = listOf(
            btnFilterAll, btnFilterRecent, btnFilterFavorites,
            btnCategoryFruit, btnCategoryVegetable, btnCategoryProtein,
            btnCategoryGrain, btnCategoryDairy
        )
        
        buttons.forEach { btn ->
            btn.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
            btn.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }
        
        // Highlight selected button
        val selectedBtn = when(currentFilter) {
            "All" -> btnFilterAll
            "Recent" -> btnFilterRecent
            "Favorites" -> btnFilterFavorites
            "Fruit" -> btnCategoryFruit
            "Vegetable" -> btnCategoryVegetable
            "Protein" -> btnCategoryProtein
            "Grain" -> btnCategoryGrain
            "Dairy" -> btnCategoryDairy
            else -> btnFilterAll
        }
        
        selectedBtn.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
        selectedBtn.setTextColor(resources.getColor(android.R.color.white, null))
    }
    
    private fun loadFavorites() {
        lifecycleScope.launch {
            val favorites = database.foodDao().getFavoriteFoods(username)
            favoriteIds = favorites.map { it.id }.toMutableSet()
            adapter.updateFavorites(favoriteIds)
        }
    }
    
    private fun loadFilteredFoods() {
        lifecycleScope.launch {
            val foods = when(currentFilter) {
                "Recent" -> database.foodDao().getRecentFoods(username, 20)
                "Favorites" -> database.foodDao().getFavoriteFoods(username)
                "Fruit", "Vegetable", "Protein", "Grain", "Dairy" -> 
                    database.foodDao().getFoodsByCategory(currentFilter)
                else -> database.foodDao().searchFoods("") // All foods
            }
            adapter.submitList(foods)
        }
    }

    private fun searchFood(query: String) {
        lifecycleScope.launch {
            val foods = if (currentFilter == "All" || currentFilter == "Recent" || currentFilter == "Favorites") {
                database.foodDao().searchFoods(query)
            } else {
                database.foodDao().searchFoodsByCategory(query, currentFilter)
            }
            adapter.submitList(foods)
        }
    }
    
    private fun toggleFavorite(food: Food) {
        lifecycleScope.launch {
            if (favoriteIds.contains(food.id)) {
                // Remove from favorites
                database.foodDao().deleteFavorite(username, food.id)
                favoriteIds.remove(food.id)
                Toast.makeText(this@AddFoodActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
            } else {
                // Add to favorites
                val favorite = FavoriteFood(username = username, foodId = food.id)
                database.foodDao().insertFavorite(favorite)
                favoriteIds.add(food.id)
                Toast.makeText(this@AddFoodActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
            }
            adapter.updateFavorites(favoriteIds)
            
            // Refresh list if in favorites view
            if (currentFilter == "Favorites") {
                loadFilteredFoods()
            }
        }
    }

    private fun showAddDialog(food: Food) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_food)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val etQty = dialog.findViewById<EditText>(R.id.etQuantity)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val tvUnit = dialog.findViewById<TextView>(R.id.tvUnit)
        val tvNutritionPreview = dialog.findViewById<TextView>(R.id.tvNutritionPreview)
        
        tvTitle.text = "Add ${food.name}"
        tvUnit.text = food.servingUnit
        
        // Update nutrition preview when quantity changes
        fun updateNutritionPreview() {
            val qty = etQty.text.toString().toFloatOrNull() ?: 1.0f
            val calories = (food.calories * qty).toInt()
            val carbs = (food.carbs * qty).toInt()
            val protein = (food.protein * qty).toInt()
            val fat = (food.fat * qty).toInt()
            
            tvNutritionPreview.text = "Calories: $calories kcal\nCarbs: ${carbs}g | Protein: ${protein}g | Fat: ${fat}g"
        }
        
        updateNutritionPreview() // Initial update
        
        etQty.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateNutritionPreview() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        btnAdd.setOnClickListener {
            val qtyStr = etQty.text.toString()
            val qty = qtyStr.toFloatOrNull() ?: 1.0f
            
            if (qty <= 0) {
                Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            saveFoodLog(food, qty)
            dialog.dismiss()
        }
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showCreateCustomFoodDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_create_food)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        val etFoodName = dialog.findViewById<EditText>(R.id.etFoodName)
        val etServingSize = dialog.findViewById<EditText>(R.id.etServingSize)
        val etServingUnit = dialog.findViewById<EditText>(R.id.etServingUnit)
        val etCalories = dialog.findViewById<EditText>(R.id.etCalories)
        val etCarbs = dialog.findViewById<EditText>(R.id.etCarbs)
        val etProtein = dialog.findViewById<EditText>(R.id.etProtein)
        val etFat = dialog.findViewById<EditText>(R.id.etFat)
        val spinnerCategory = dialog.findViewById<Spinner>(R.id.spinnerCategory)
        val btnSave = dialog.findViewById<Button>(R.id.btnSaveCustom)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelCustom)
        
        // Setup category spinner
        val categories = arrayOf("Fruit", "Vegetable", "Protein", "Grain", "Dairy", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        
        btnSave.setOnClickListener {
            val name = etFoodName.text.toString().trim()
            val servingSize = etServingSize.text.toString().toFloatOrNull() ?: 0f
            val servingUnit = etServingUnit.text.toString().trim()
            val calories = etCalories.text.toString().toFloatOrNull() ?: 0f
            val carbs = etCarbs.text.toString().toFloatOrNull() ?: 0f
            val protein = etProtein.text.toString().toFloatOrNull() ?: 0f
            val fat = etFat.text.toString().toFloatOrNull() ?: 0f
            val category = spinnerCategory.selectedItem.toString()
            
            // Validation
            if (name.isEmpty() || servingUnit.isEmpty() || servingSize <= 0 || calories <= 0) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Create custom food
            val customFood = Food(
                id = "custom_${UUID.randomUUID()}",
                name = name,
                calories = calories,
                carbs = carbs,
                protein = protein,
                fat = fat,
                servingSize = servingSize,
                servingUnit = servingUnit,
                category = category,
                isCustom = true,
                createdBy = username
            )
            
            lifecycleScope.launch {
                database.foodDao().insertFood(customFood)
                Toast.makeText(this@AddFoodActivity, "Custom food created!", Toast.LENGTH_SHORT).show()
                loadFilteredFoods() // Refresh list
                dialog.dismiss()
            }
        }
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
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
            Toast.makeText(this@AddFoodActivity, "Added ${food.name}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
