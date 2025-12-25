package com.example.nutrimate

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.data.FoodLog
import com.example.nutrimate.ui.foodlog.DeleteConfirmationDialog
import com.example.nutrimate.ui.foodlog.EditFoodDialog
import com.example.nutrimate.ui.foodlog.FoodLogScreen
import com.example.nutrimate.ui.foodlog.FoodLogScreenItem
import com.example.nutrimate.ui.foodlog.FoodLogScreenState
import com.example.nutrimate.ui.main.NavItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FoodLogActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private var username: String = ""
    private val localeID = Locale.forLanguageTag("id-ID")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", localeID)
    private val displayDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", localeID)
    
    private var currentCalendar: Calendar = Calendar.getInstance()
    private val todayCalendar: Calendar = Calendar.getInstance()
    
    // Compose state
    private var screenState by mutableStateOf(FoodLogScreenState())
    private var editingItem by mutableStateOf<FoodLogScreenItem?>(null)
    private var deletingItem by mutableStateOf<FoodLogScreenItem?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(this)
        username = intent.getStringExtra("USERNAME") ?: ""
        
        if (username.isEmpty()) {
            Toast.makeText(this, "Kesalahan pengguna", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        updateDateDisplay()
        
        setContent {
            FoodLogScreen(
                state = screenState,
                selectedNavItem = NavItem.FOOD_LOG,
                onPrevDayClick = { navigatePrevDay() },
                onNextDayClick = { navigateNextDay() },
                onDateClick = { showDatePicker() },
                onCopyPreviousDayClick = { copyFromPreviousDay() },
                onAddFood = { mealType -> navigateToAddFood(mealType) },
                onEditFood = { item -> editingItem = item },
                onDeleteFood = { item -> deletingItem = item },
                onNavItemClick = { navItem -> handleNavigation(navItem) }
            )
            
            // Edit Dialog
            editingItem?.let { item ->
                EditFoodDialog(
                    item = item,
                    onDismiss = { editingItem = null },
                    onSave = { newQty -> 
                        updateFoodQuantity(item.id, newQty)
                        editingItem = null
                    }
                )
            }
            
            // Delete Confirmation Dialog
            deletingItem?.let { item ->
                DeleteConfirmationDialog(
                    item = item,
                    onDismiss = { deletingItem = null },
                    onConfirm = {
                        deleteFood(item.id)
                        deletingItem = null
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadFoodLogs()
    }
    
    private fun updateDateDisplay() {
        val isToday = isSameDay(currentCalendar, todayCalendar)
        val displayText = if (isToday) {
            "Hari Ini, ${displayDateFormat.format(currentCalendar.time)}"
        } else {
            displayDateFormat.format(currentCalendar.time)
        }
        
        screenState = screenState.copy(
            currentDate = displayText,
            isToday = isToday
        )
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    private fun navigatePrevDay() {
        currentCalendar.add(Calendar.DAY_OF_MONTH, -1)
        updateDateDisplay()
        loadFoodLogs()
    }
    
    private fun navigateNextDay() {
        if (!isSameDay(currentCalendar, todayCalendar)) {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1)
            updateDateDisplay()
            loadFoodLogs()
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
    
    private fun navigateToAddFood(mealType: String) {
        val intent = Intent(this, AddFoodActivity::class.java)
        intent.putExtra("MEAL_TYPE", mealType)
        intent.putExtra("USERNAME", username)
        intent.putExtra("DATE", dateFormat.format(currentCalendar.time))
        startActivity(intent)
    }
    
    private fun handleNavigation(navItem: NavItem) {
        when (navItem) {
            NavItem.HOME -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USERNAME", username)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
            NavItem.FOOD_LOG -> {
                // Already here, refresh data
                loadFoodLogs()
            }
            NavItem.STATS -> {
                val intent = Intent(this, StatisticsActivity::class.java)
                intent.putExtra("USERNAME", username)
                startActivity(intent)
                finish()
            }
            NavItem.SETTINGS -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra("USERNAME", username)
                startActivity(intent)
                finish()
            }
        }
    }
    
    private fun updateFoodQuantity(itemId: Int, newQty: Float) {
        lifecycleScope.launch {
            database.foodDao().updateFoodLogQuantity(itemId, newQty)
            runOnUiThread {
                Toast.makeText(this@FoodLogActivity, "Berhasil diperbarui", Toast.LENGTH_SHORT).show()
                loadFoodLogs()
            }
        }
    }
    
    private fun deleteFood(itemId: Int) {
        lifecycleScope.launch {
            database.foodDao().deleteFoodLog(itemId)
            runOnUiThread {
                Toast.makeText(this@FoodLogActivity, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                loadFoodLogs()
            }
        }
    }

    private fun loadFoodLogs() {
        lifecycleScope.launch {
            val dateStr = dateFormat.format(currentCalendar.time)
            val logs = database.foodDao().getFoodLogsByDate(username, dateStr)
            
            // Get user data for limits
            val user = database.userDao().getUserByUsername(username)
            val sugarLimit = user?.sugarLimit ?: 50f
            val sodiumLimit = user?.sodiumLimit ?: 2300f
            val fiberTarget = user?.fiberTarget ?: 30f
            val calorieLimit = user?.dailyCalorieTarget?.toFloat() ?: 2000f
            val fatLimit = user?.fatTarget ?: 65f
            val carbsLimit = user?.carbsTarget ?: 300f
            
            val breakfastList = mutableListOf<FoodLogScreenItem>()
            val lunchList = mutableListOf<FoodLogScreenItem>()
            val dinnerList = mutableListOf<FoodLogScreenItem>()
            val snackList = mutableListOf<FoodLogScreenItem>()
            
            var totalCalories = 0f
            var totalCarbs = 0f
            var totalProtein = 0f
            var totalFat = 0f
            var totalSugar = 0f
            var totalSodium = 0f
            var totalFiber = 0f
            
            for (log in logs) {
                val food = database.foodDao().getFoodById(log.foodId)
                if (food != null) {
                    val totalCals = food.calories * log.servingQty
                    val itemCarbs = food.carbs * log.servingQty
                    val itemProtein = food.protein * log.servingQty
                    val itemFat = food.fat * log.servingQty
                    val itemSugar = food.sugar * log.servingQty
                    val itemSodium = food.sodium * log.servingQty
                    val itemFiber = food.fiber * log.servingQty
                    
                    totalCalories += totalCals
                    totalCarbs += itemCarbs
                    totalProtein += itemProtein
                    totalFat += itemFat
                    totalSugar += itemSugar
                    totalSodium += itemSodium
                    totalFiber += itemFiber
                    
                    val item = FoodLogScreenItem(
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
                screenState = screenState.copy(
                    calorieProgress = totalCalories.toInt(),
                    calorieTarget = calorieLimit.toInt(),
                    totalCarbs = totalCarbs.toInt(),
                    totalProtein = totalProtein.toInt(),
                    totalFat = totalFat.toInt(),
                    totalSugar = totalSugar.toInt(),
                    totalSodium = totalSodium.toInt(),
                    totalFiber = totalFiber.toInt(),
                    carbsLimit = carbsLimit,
                    fatLimit = fatLimit,
                    sugarLimit = sugarLimit,
                    sodiumLimit = sodiumLimit,
                    breakfastItems = breakfastList,
                    lunchItems = lunchList,
                    dinnerItems = dinnerList,
                    snackItems = snackList
                )
            }
        }
    }
}