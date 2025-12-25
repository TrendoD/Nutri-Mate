package com.example.nutrimate

import android.content.Context
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
import com.example.nutrimate.data.User
import com.example.nutrimate.data.WaterIntake
import com.example.nutrimate.ui.main.AlertType
import com.example.nutrimate.ui.main.MainScreen
import com.example.nutrimate.ui.main.MainScreenState
import com.example.nutrimate.ui.main.NavItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    companion object {
        private const val PREF_NAME = "NutriMatePrefs"
        private const val KEY_LOGGED_IN_USER = "logged_in_user"
    }

    private lateinit var database: AppDatabase
    private var currentUsername: String = ""
    private val localeID = Locale.forLanguageTag("id-ID")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", localeID)
    private val displayDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", localeID)
    
    private val waterTarget = 2000 // 2000ml daily target
    
    // Compose state
    private var screenState by mutableStateOf(MainScreenState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(this)

        // Get user name from intent
        val username = intent.getStringExtra("USERNAME")
        
        if (username.isNullOrEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        currentUsername = username

        // Set initial date
        screenState = screenState.copy(
            currentDate = displayDateFormat.format(Date())
        )

        setContent {
            MainScreen(
                state = screenState,
                onLogoutClick = { handleLogout() },
                onFabClick = { navigateToAddFood() },
                onRecommendationsClick = { navigateToRecommendations() },
                onWaterAdd = { amount -> addWaterIntake(amount) },
                onNavItemClick = { navItem -> handleNavigation(navItem) }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentUsername.isNotEmpty()) {
            loadDashboardData(currentUsername)
            loadWaterIntake(currentUsername)
        }
    }

    private fun handleLogout() {
        // Clear login session from SharedPreferences
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(KEY_LOGGED_IN_USER).apply()
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToAddFood() {
        val intent = Intent(this, AddFoodActivity::class.java)
        intent.putExtra("USERNAME", currentUsername)
        intent.putExtra("MEAL_TYPE", "Breakfast")
        startActivity(intent)
    }

    private fun navigateToRecommendations() {
        val intent = Intent(this, RecommendationsActivity::class.java)
        intent.putExtra("USERNAME", currentUsername)
        startActivity(intent)
    }

    private fun handleNavigation(navItem: NavItem) {
        when (navItem) {
            NavItem.HOME -> {
                // Refresh data when tapping home
                loadDashboardData(currentUsername)
                loadWaterIntake(currentUsername)
            }
            NavItem.FOOD_LOG -> {
                val intent = Intent(this, FoodLogActivity::class.java)
                intent.putExtra("USERNAME", currentUsername)
                startActivity(intent)
            }
            NavItem.STATS -> {
                val intent = Intent(this, StatisticsActivity::class.java)
                intent.putExtra("USERNAME", currentUsername)
                startActivity(intent)
            }
            NavItem.SETTINGS -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra("USERNAME", currentUsername)
                startActivity(intent)
            }
        }
    }
    
    private fun addWaterIntake(amount: Int) {
        lifecycleScope.launch {
            val dateStr = dateFormat.format(Date())
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            
            val waterIntake = WaterIntake(
                username = currentUsername,
                amount = amount,
                date = dateStr,
                time = timeStr
            )
            
            database.waterIntakeDao().insertWaterIntake(waterIntake)
            loadWaterIntake(currentUsername)
            
            Toast.makeText(this@MainActivity, "+${amount}ml air ditambahkan!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadWaterIntake(username: String) {
        lifecycleScope.launch {
            val dateStr = dateFormat.format(Date())
            val totalWater = database.waterIntakeDao().getTotalWaterIntakeByDate(username, dateStr) ?: 0
            
            screenState = screenState.copy(
                waterIntake = totalWater.coerceAtMost(waterTarget * 2), // Allow up to 2x target for display
                waterTarget = waterTarget
            )
        }
    }

    private fun loadDashboardData(username: String) {
        lifecycleScope.launch {
            val user = database.userDao().getUserByUsername(username)
            if (user != null) {
                if (user.age == 0) {
                    val intent = Intent(this@MainActivity, com.example.nutrimate.onboarding.OnboardingActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    finish()
                    return@launch
                }

                val target = user.dailyCalorieTarget

                // Calculate Consumed
                val dateStr = dateFormat.format(Date())
                val logs = database.foodDao().getFoodLogsByDate(username, dateStr)
                
                var totalCals = 0f
                var totalCarbs = 0f
                var totalProtein = 0f
                var totalFat = 0f
                var totalSugar = 0f
                var totalSodium = 0f

                for (log in logs) {
                    val food = database.foodDao().getFoodById(log.foodId)
                    if (food != null) {
                        totalCals += food.calories * log.servingQty
                        totalCarbs += food.carbs * log.servingQty
                        totalProtein += food.protein * log.servingQty
                        totalFat += food.fat * log.servingQty
                        totalSugar += food.sugar * log.servingQty
                        totalSodium += food.sodium * log.servingQty
                    }
                }

                // Generate insights
                val (alertTitle, alertBody, alertType) = generateInsights(
                    user, totalCals, target, totalCarbs, totalFat, totalProtein, totalSugar, totalSodium
                )

                // Update UI state
                screenState = screenState.copy(
                    userName = user.fullName,
                    calorieProgress = totalCals.toInt(),
                    calorieTarget = target,
                    carbsGrams = totalCarbs.toInt(),
                    proteinGrams = totalProtein.toInt(),
                    fatGrams = totalFat.toInt(),
                    alertTitle = alertTitle,
                    alertBody = alertBody,
                    alertType = alertType
                )
            }
        }
    }

    private fun generateInsights(
        user: User,
        currentCals: Float,
        targetCals: Int,
        carbs: Float,
        fat: Float,
        protein: Float,
        sugar: Float,
        sodium: Float
    ): Triple<String, String, AlertType> {
        val conditions = user.medicalConditions
        val conditionList = conditions.split(",").map { it.trim() }
        var title = "Tips Harian"
        var body = "Tetap terhidrasi dan makan makanan seimbang!"
        var alertType = AlertType.TIP

        if (currentCals > targetCals) {
            title = "⚠️ Peringatan Kalori"
            body = "Anda telah melebihi batas kalori harian Anda. Cobalah makan lebih ringan untuk sisa hari ini."
            alertType = AlertType.WARNING
        } else if (conditionList.contains("Diabetes")) {
            if (user.carbsTarget > 0 && carbs > user.carbsTarget) {
                title = "⚠️ Peringatan Diabetes"
                body = "Asupan karbohidrat (${carbs.toInt()}g) melebihi target (${user.carbsTarget.toInt()}g). Pantau gula darah Anda."
                alertType = AlertType.WARNING
            } else if (user.sugarLimit > 0 && sugar > user.sugarLimit) {
                title = "⚠️ Peringatan Gula"
                body = "Asupan gula (${sugar.toInt()}g) melebihi batas (${user.sugarLimit.toInt()}g). Mohon berhati-hati."
                alertType = AlertType.WARNING
            }
        } else if (conditionList.contains("Hypertension") && user.sodiumLimit > 0 && sodium > user.sodiumLimit) {
             title = "⚠️ Peringatan Hipertensi"
             body = "Asupan garam (${sodium.toInt()}mg) melebihi batas (${user.sodiumLimit.toInt()}mg). Kurangi makanan asin."
             alertType = AlertType.WARNING
        } else if ((conditionList.contains("Hypertension") || conditionList.contains("Cholesterol")) && user.fatTarget > 0 && fat > user.fatTarget) {
            title = "⚠️ Peringatan Kesehatan Jantung"
            body = "Asupan lemak (${fat.toInt()}g) tinggi. Kurangi makanan gorengan."
            alertType = AlertType.WARNING
        } else if (conditionList.contains("Gastritis")) {
            // Gastritis alert - always show advice for gastritis patients
            title = "🍃 Pengingat Maag"
            body = "Hindari makanan pedas, asam, dan gorengan. Makan dalam porsi kecil namun sering dan hindari makan larut malam."
            alertType = AlertType.GASTRITIS_WARNING
        } else {
            // Daily tips rotation based on current intake
            val tips = listOf(
                "💧 Tetap terhidrasi! Targetkan setidaknya 8 gelas air hari ini.",
                "🥗 Cobalah mengisi setengah piring Anda dengan sayuran setiap kali makan.",
                "🚶 Jalan kaki 30 menit setelah makan dapat membantu pencernaan.",
                "🍎 Sertakan makanan kaya serat agar Anda merasa kenyang lebih lama.",
                "😴 Tidur yang cukup penting untuk menjaga metabolisme yang sehat.",
                "🥦 Mengonsumsi berbagai sayuran berwarna memastikan nutrisi yang beragam."
            )
            val tipIndex = (System.currentTimeMillis() / 60000).toInt() % tips.size
            body = tips[tipIndex]
        }

        return Triple(title, body, alertType)
    }
}