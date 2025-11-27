package com.example.nutrimate

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrimate.data.AppDatabase
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodLogActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var rvBreakfast: RecyclerView
    private lateinit var rvLunch: RecyclerView
    private lateinit var rvDinner: RecyclerView
    private lateinit var rvSnack: RecyclerView
    
    private lateinit var btnAddBreakfast: MaterialButton
    private lateinit var btnAddLunch: MaterialButton
    private lateinit var btnAddDinner: MaterialButton
    private lateinit var btnAddSnack: MaterialButton
    
    private lateinit var tvDate: TextView
    private lateinit var tvBreakfastCals: TextView
    private lateinit var tvLunchCals: TextView
    private lateinit var tvDinnerCals: TextView
    private lateinit var tvSnackCals: TextView
    
    private var username: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    private val today = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_log)

        database = AppDatabase.getDatabase(this)
        username = intent.getStringExtra("USERNAME") ?: ""
        
        if (username.isEmpty()) {
            Toast.makeText(this, "User error", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadFoodLogs()
    }

    private fun initViews() {
        tvDate = findViewById(R.id.tvDate)
        tvDate.text = displayDateFormat.format(today)

        rvBreakfast = findViewById(R.id.rvBreakfast)
        rvLunch = findViewById(R.id.rvLunch)
        rvDinner = findViewById(R.id.rvDinner)
        rvSnack = findViewById(R.id.rvSnack)

        rvBreakfast.layoutManager = LinearLayoutManager(this)
        rvLunch.layoutManager = LinearLayoutManager(this)
        rvDinner.layoutManager = LinearLayoutManager(this)
        rvSnack.layoutManager = LinearLayoutManager(this)
        
        rvBreakfast.adapter = FoodLogAdapter()
        rvLunch.adapter = FoodLogAdapter()
        rvDinner.adapter = FoodLogAdapter()
        rvSnack.adapter = FoodLogAdapter()
        
        btnAddBreakfast = findViewById(R.id.btnAddBreakfast)
        btnAddLunch = findViewById(R.id.btnAddLunch)
        btnAddDinner = findViewById(R.id.btnAddDinner)
        btnAddSnack = findViewById(R.id.btnAddSnack)
        
        tvBreakfastCals = findViewById(R.id.tvBreakfastCals)
        tvLunchCals = findViewById(R.id.tvLunchCals)
        tvDinnerCals = findViewById(R.id.tvDinnerCals)
        tvSnackCals = findViewById(R.id.tvSnackCals)
    }

    private fun setupListeners() {
        val addListener = { mealType: String ->
            val intent = Intent(this, AddFoodActivity::class.java)
            intent.putExtra("MEAL_TYPE", mealType)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }

        btnAddBreakfast.setOnClickListener { addListener("Breakfast") }
        btnAddLunch.setOnClickListener { addListener("Lunch") }
        btnAddDinner.setOnClickListener { addListener("Dinner") }
        btnAddSnack.setOnClickListener { addListener("Snack") }
    }

    private fun loadFoodLogs() {
        lifecycleScope.launch {
            val dateStr = dateFormat.format(today)
            val logs = database.foodDao().getFoodLogsByDate(username, dateStr)
            
            val breakfastList = mutableListOf<FoodLogItem>()
            val lunchList = mutableListOf<FoodLogItem>()
            val dinnerList = mutableListOf<FoodLogItem>()
            val snackList = mutableListOf<FoodLogItem>()
            
            var breakfastCals = 0f
            var lunchCals = 0f
            var dinnerCals = 0f
            var snackCals = 0f
            
            for (log in logs) {
                val food = database.foodDao().getFoodById(log.foodId)
                if (food != null) {
                    val totalCals = food.calories * log.servingQty
                    val item = FoodLogItem(
                        log.id,
                        food.name,
                        totalCals,
                        log.servingQty,
                        food.servingUnit
                    )
                    
                    when (log.mealType) {
                        "Breakfast" -> {
                            breakfastList.add(item)
                            breakfastCals += totalCals
                        }
                        "Lunch" -> {
                            lunchList.add(item)
                            lunchCals += totalCals
                        }
                        "Dinner" -> {
                            dinnerList.add(item)
                            dinnerCals += totalCals
                        }
                        "Snack" -> {
                            snackList.add(item)
                            snackCals += totalCals
                        }
                    }
                }
            }
            
            (rvBreakfast.adapter as FoodLogAdapter).submitList(breakfastList)
            (rvLunch.adapter as FoodLogAdapter).submitList(lunchList)
            (rvDinner.adapter as FoodLogAdapter).submitList(dinnerList)
            (rvSnack.adapter as FoodLogAdapter).submitList(snackList)
            
            // Update calorie totals
            tvBreakfastCals.text = "${breakfastCals.toInt()} kcal"
            tvLunchCals.text = "${lunchCals.toInt()} kcal"
            tvDinnerCals.text = "${dinnerCals.toInt()} kcal"
            tvSnackCals.text = "${snackCals.toInt()} kcal"
        }
    }
}
