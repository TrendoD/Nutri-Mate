package com.example.nutrimate

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.data.FoodLog
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FoodLogActivity : AppCompatActivity(), FoodLogItemListener {

    private lateinit var database: AppDatabase
    private lateinit var rvBreakfast: RecyclerView
    private lateinit var rvLunch: RecyclerView
    private lateinit var rvDinner: RecyclerView
    private lateinit var rvSnack: RecyclerView
    
    private lateinit var btnAddBreakfast: Button
    private lateinit var btnAddLunch: Button
    private lateinit var btnAddDinner: Button
    private lateinit var btnAddSnack: Button
    
    private lateinit var tvDate: TextView
    private lateinit var btnPrevDay: ImageButton
    private lateinit var btnNextDay: ImageButton
    private lateinit var btnCopyPreviousDay: Button
    
    private lateinit var bottomNavigation: BottomNavigationView

    // Daily Summary TextViews
    private lateinit var tvTotalCalories: TextView
    private lateinit var tvTotalCarbs: TextView
    private lateinit var tvTotalProtein: TextView
    private lateinit var tvTotalFat: TextView
    
    private var username: String = ""
    private val localeID = Locale.forLanguageTag("id-ID")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", localeID)
    private val displayDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", localeID)
    
    private var currentCalendar: Calendar = Calendar.getInstance()
    private val todayCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_log)

        database = AppDatabase.getDatabase(this)
        username = intent.getStringExtra("USERNAME") ?: ""
        
        if (username.isEmpty()) {
            Toast.makeText(this, "Kesalahan pengguna", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupListeners()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadFoodLogs()
    }

    private fun initViews() {
        tvDate = findViewById(R.id.tvDate)
        btnPrevDay = findViewById(R.id.btnPrevDay)
        btnNextDay = findViewById(R.id.btnNextDay)
        btnCopyPreviousDay = findViewById(R.id.btnCopyPreviousDay)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        // Daily Summary
        tvTotalCalories = findViewById(R.id.tvTotalCalories)
        tvTotalCarbs = findViewById(R.id.tvTotalCarbs)
        tvTotalProtein = findViewById(R.id.tvTotalProtein)
        tvTotalFat = findViewById(R.id.tvTotalFat)
        
        updateDateDisplay()

        rvBreakfast = findViewById(R.id.rvBreakfast)
        rvLunch = findViewById(R.id.rvLunch)
        rvDinner = findViewById(R.id.rvDinner)
        rvSnack = findViewById(R.id.rvSnack)

        rvBreakfast.layoutManager = LinearLayoutManager(this)
        rvLunch.layoutManager = LinearLayoutManager(this)
        rvDinner.layoutManager = LinearLayoutManager(this)
        rvSnack.layoutManager = LinearLayoutManager(this)
        
        val breakfastAdapter = FoodLogAdapter()
        val lunchAdapter = FoodLogAdapter()
        val dinnerAdapter = FoodLogAdapter()
        val snackAdapter = FoodLogAdapter()
        
        breakfastAdapter.setListener(this)
        lunchAdapter.setListener(this)
        dinnerAdapter.setListener(this)
        snackAdapter.setListener(this)
        
        rvBreakfast.adapter = breakfastAdapter
        rvLunch.adapter = lunchAdapter
        rvDinner.adapter = dinnerAdapter
        rvSnack.adapter = snackAdapter
        
        btnAddBreakfast = findViewById(R.id.btnAddBreakfast)
        btnAddLunch = findViewById(R.id.btnAddLunch)
        btnAddDinner = findViewById(R.id.btnAddDinner)
        btnAddSnack = findViewById(R.id.btnAddSnack)
    }
    
    private fun updateDateDisplay() {
        val isToday = isSameDay(currentCalendar, todayCalendar)
        val displayText = if (isToday) {
            "Hari Ini, ${displayDateFormat.format(currentCalendar.time)}"
        } else {
            displayDateFormat.format(currentCalendar.time)
        }
        tvDate.text = displayText
        
        // Disable next day button if current date is today
        btnNextDay.isEnabled = !isToday
        btnNextDay.alpha = if (isToday) 0.3f else 1.0f
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun setupListeners() {
        val addListener = { mealType: String ->
            val intent = Intent(this, AddFoodActivity::class.java)
            intent.putExtra("MEAL_TYPE", mealType)
            intent.putExtra("USERNAME", username)
            intent.putExtra("DATE", dateFormat.format(currentCalendar.time))
            startActivity(intent)
        }

        btnAddBreakfast.setOnClickListener { addListener("Breakfast") }
        btnAddLunch.setOnClickListener { addListener("Lunch") }
        btnAddDinner.setOnClickListener { addListener("Dinner") }
        btnAddSnack.setOnClickListener { addListener("Snack") }
        
        // Date Navigation
        btnPrevDay.setOnClickListener {
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1)
            updateDateDisplay()
            loadFoodLogs()
        }
        
        btnNextDay.setOnClickListener {
            if (!isSameDay(currentCalendar, todayCalendar)) {
                currentCalendar.add(Calendar.DAY_OF_MONTH, 1)
                updateDateDisplay()
                loadFoodLogs()
            }
        }
        
        // Calendar Picker - Click on date text
        tvDate.setOnClickListener {
            showDatePicker()
        }
        
        // Copy Previous Day
        btnCopyPreviousDay.setOnClickListener {
            copyFromPreviousDay()
        }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_food_log
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_food_log -> {
                    true
                }
                R.id.nav_stats -> {
                    val intent = Intent(this, StatisticsActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun showDatePicker() {
        val year = currentCalendar.get(Calendar.YEAR)
        val month = currentCalendar.get(Calendar.MONTH)
        val day = currentCalendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                currentCalendar.set(selectedYear, selectedMonth, selectedDay)
                
                // Don't allow future dates
                if (currentCalendar.after(todayCalendar)) {
                    currentCalendar.time = todayCalendar.time
                }
                
                updateDateDisplay()
                loadFoodLogs()
            },
            year, month, day
        )
        
        // Set max date to today
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun copyFromPreviousDay() {
        lifecycleScope.launch {
            val previousCalendar = currentCalendar.clone() as Calendar
            previousCalendar.add(Calendar.DAY_OF_MONTH, -1)
            val previousDateStr = dateFormat.format(previousCalendar.time)
            val currentDateStr = dateFormat.format(currentCalendar.time)
            
            val previousLogs = database.foodDao().getFoodLogsByDate(username, previousDateStr)
            
            if (previousLogs.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(this@FoodLogActivity, "Tidak ada catatan makanan dari hari sebelumnya", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            
            // Copy each log to current date
            for (log in previousLogs) {
                val newLog = FoodLog(
                    username = username,
                    foodId = log.foodId,
                    servingQty = log.servingQty,
                    mealType = log.mealType,
                    date = currentDateStr
                )
                database.foodDao().insertFoodLog(newLog)
            }
            
            runOnUiThread {
                Toast.makeText(this@FoodLogActivity, "Disalin ${previousLogs.size} item dari hari sebelumnya", Toast.LENGTH_SHORT).show()
                loadFoodLogs()
            }
        }
    }

    private fun loadFoodLogs() {
        lifecycleScope.launch {
            val dateStr = dateFormat.format(currentCalendar.time)
            val logs = database.foodDao().getFoodLogsByDate(username, dateStr)
            
            val breakfastList = mutableListOf<FoodLogItem>()
            val lunchList = mutableListOf<FoodLogItem>()
            val dinnerList = mutableListOf<FoodLogItem>()
            val snackList = mutableListOf<FoodLogItem>()
            
            var totalCalories = 0f
            var totalCarbs = 0f
            var totalProtein = 0f
            var totalFat = 0f
            
            for (log in logs) {
                val food = database.foodDao().getFoodById(log.foodId)
                if (food != null) {
                    val totalCals = food.calories * log.servingQty
                    val itemCarbs = food.carbs * log.servingQty
                    val itemProtein = food.protein * log.servingQty
                    val itemFat = food.fat * log.servingQty
                    
                    totalCalories += totalCals
                    totalCarbs += itemCarbs
                    totalProtein += itemProtein
                    totalFat += itemFat
                    
                    val item = FoodLogItem(
                        id = log.id,
                        name = food.name,
                        totalCalories = totalCals,
                        servingQty = log.servingQty,
                        unit = food.servingUnit,
                        foodId = food.id,
                        carbs = food.carbs,
                        protein = food.protein,
                        fat = food.fat,
                        caloriesPerServing = food.calories
                    )
                    
                    when (log.mealType) {
                        "Breakfast" -> breakfastList.add(item)
                        "Lunch" -> lunchList.add(item)
                        "Dinner" -> dinnerList.add(item)
                        "Snack" -> snackList.add(item)
                    }
                }
            }
            
            runOnUiThread {
                // Update daily summary
                tvTotalCalories.text = totalCalories.toInt().toString()
                tvTotalCarbs.text = "${totalCarbs.toInt()}g"
                tvTotalProtein.text = "${totalProtein.toInt()}g"
                tvTotalFat.text = "${totalFat.toInt()}g"
                
                // Update lists
                (rvBreakfast.adapter as FoodLogAdapter).submitList(breakfastList)
                (rvLunch.adapter as FoodLogAdapter).submitList(lunchList)
                (rvDinner.adapter as FoodLogAdapter).submitList(dinnerList)
                (rvSnack.adapter as FoodLogAdapter).submitList(snackList)
            }
        }
    }
    
    // FoodLogItemListener implementation
    override fun onEditClick(item: FoodLogItem) {
        showEditDialog(item)
    }
    
    override fun onDeleteClick(item: FoodLogItem) {
        showDeleteConfirmation(item)
    }
    
    private fun showEditDialog(item: FoodLogItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_food, null)
        val tvFoodName = dialogView.findViewById<TextView>(R.id.tvFoodName)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
        val tvUnit = dialogView.findViewById<TextView>(R.id.tvUnit)
        val tvNutritionPreview = dialogView.findViewById<TextView>(R.id.tvNutritionPreview)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        
        tvFoodName.text = item.name
        etQuantity.setText(item.servingQty.toString())
        tvUnit.text = item.unit
        
        // Update preview based on quantity
        fun updatePreview() {
            val qty = etQuantity.text.toString().toFloatOrNull() ?: 0f
            val calories = (item.caloriesPerServing * qty).toInt()
            tvNutritionPreview.text = "Kalori: $calories kkal"
        }
        
        updatePreview()
        
        etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePreview()
            }
        })
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnSave.setOnClickListener {
            val newQty = etQuantity.text.toString().toFloatOrNull()
            if (newQty == null || newQty <= 0) {
                Toast.makeText(this, "Harap masukkan jumlah yang valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            lifecycleScope.launch {
                database.foodDao().updateFoodLogQuantity(item.id, newQty)
                runOnUiThread {
                    Toast.makeText(this@FoodLogActivity, "Berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadFoodLogs()
                }
            }
        }
        
        dialog.show()
    }
    
    private fun showDeleteConfirmation(item: FoodLogItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Makanan")
            .setMessage("Apakah Anda yakin ingin menghapus '${item.name}' dari log Anda?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    database.foodDao().deleteFoodLog(item.id)
                    runOnUiThread {
                        Toast.makeText(this@FoodLogActivity, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadFoodLogs()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}