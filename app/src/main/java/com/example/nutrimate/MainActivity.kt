package com.example.nutrimate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.data.WaterIntake
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PREF_NAME = "NutriMatePrefs"
        private const val KEY_LOGGED_IN_USER = "logged_in_user"
    }

    private lateinit var tvWelcome: TextView
    private lateinit var tvDate: TextView
    private lateinit var btnLogout: Button
    
    // Dashboard Views
    private lateinit var tvCalorieProgress: TextView
    private lateinit var tvCalorieTarget: TextView
    private lateinit var pbCalories: ProgressBar
    private lateinit var tvCarbs: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvFat: TextView
    private lateinit var cvAlert: CardView
    private lateinit var tvAlertTitle: TextView
    private lateinit var tvAlertBody: TextView
    
    // Recommendations Card
    private lateinit var cvRecommendations: CardView
    
    // Water Intake Views
    private lateinit var tvWaterIntake: TextView
    private lateinit var pbWater: ProgressBar
    private lateinit var btnWater100: Button
    private lateinit var btnWater250: Button
    private lateinit var btnWater500: Button
    
    // Navigation
    private lateinit var fabQuickAdd: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var database: AppDatabase
    private var currentUsername: String = ""
    private val localeID = Locale.forLanguageTag("id-ID")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", localeID)
    private val displayDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", localeID)
    
    private val waterTarget = 2000 // 2000ml daily target

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)
        
        initViews()

        // Get user name from intent
        val username = intent.getStringExtra("USERNAME")
        
        if (username.isNullOrEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        currentUsername = username

        setupListeners()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        if (currentUsername.isNotEmpty()) {
            loadDashboardData(currentUsername)
            loadWaterIntake(currentUsername)
        }
        // Reset bottom navigation to home when returning (without triggering listener)
        if (::bottomNavigation.isInitialized) {
            bottomNavigation.menu.findItem(R.id.nav_home)?.isChecked = true
        }
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvDate = findViewById(R.id.tvDate)
        btnLogout = findViewById(R.id.btnLogout)
        
        tvCalorieProgress = findViewById(R.id.tvCalorieProgress)
        tvCalorieTarget = findViewById(R.id.tvCalorieTarget)
        pbCalories = findViewById(R.id.pbCalories)
        tvCarbs = findViewById(R.id.tvCarbs)
        tvProtein = findViewById(R.id.tvProtein)
        tvFat = findViewById(R.id.tvFat)
        
        cvAlert = findViewById(R.id.cvAlert)
        tvAlertTitle = findViewById(R.id.tvAlertTitle)
        tvAlertBody = findViewById(R.id.tvAlertBody)
        
        // Recommendations
        cvRecommendations = findViewById(R.id.cvRecommendations)
        
        // Water Intake
        tvWaterIntake = findViewById(R.id.tvWaterIntake)
        pbWater = findViewById(R.id.pbWater)
        btnWater100 = findViewById(R.id.btnWater100)
        btnWater250 = findViewById(R.id.btnWater250)
        btnWater500 = findViewById(R.id.btnWater500)
        
        // Navigation
        fabQuickAdd = findViewById(R.id.fabQuickAdd)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        // Set today's date
        tvDate.text = displayDateFormat.format(Date())
    }

    private fun setupListeners() {
        // FAB for quick add food (goes to breakfast by default)
        fabQuickAdd.setOnClickListener {
            val intent = Intent(this, AddFoodActivity::class.java)
            intent.putExtra("USERNAME", currentUsername)
            intent.putExtra("MEAL_TYPE", "Breakfast")
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            // Clear login session from SharedPreferences
            val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit().remove(KEY_LOGGED_IN_USER).apply()
            
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        
        // Recommendations card click
        cvRecommendations.setOnClickListener {
            val intent = Intent(this, RecommendationsActivity::class.java)
            intent.putExtra("USERNAME", currentUsername)
            startActivity(intent)
        }
        
        // Water intake buttons
        btnWater100.setOnClickListener { addWaterIntake(100) }
        btnWater250.setOnClickListener { addWaterIntake(250) }
        btnWater500.setOnClickListener { addWaterIntake(500) }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Refresh data when tapping home
                    loadDashboardData(currentUsername)
                    loadWaterIntake(currentUsername)
                    true
                }
                R.id.nav_food_log -> {
                    val intent = Intent(this, FoodLogActivity::class.java)
                    intent.putExtra("USERNAME", currentUsername)
                    startActivity(intent)
                    true
                }
                R.id.nav_stats -> {
                    val intent = Intent(this, StatisticsActivity::class.java)
                    intent.putExtra("USERNAME", currentUsername)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("USERNAME", currentUsername)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.putExtra("USERNAME", currentUsername)
                    startActivity(intent)
                    true
                }
                else -> false
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
            
            tvWaterIntake.text = "$totalWater / $waterTarget ml"
            pbWater.max = waterTarget
            pbWater.progress = totalWater.coerceAtMost(waterTarget)
        }
    }

    private fun loadDashboardData(username: String) {
        lifecycleScope.launch {
            val user = database.userDao().getUserByUsername(username)
            if (user != null) {
                if (user.age == 0) {
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    finish()
                    return@launch
                }

                tvWelcome.text = "Halo, ${user.fullName}!"
                val target = user.dailyCalorieTarget
                tvCalorieTarget.text = "/ $target kkal"

                // Calculate Consumed
                val dateStr = dateFormat.format(Date())
                val logs = database.foodDao().getFoodLogsByDate(username, dateStr)
                
                var totalCals = 0f
                var totalCarbs = 0f
                var totalProtein = 0f
                var totalFat = 0f

                for (log in logs) {
                    val food = database.foodDao().getFoodById(log.foodId)
                    if (food != null) {
                        totalCals += food.calories * log.servingQty
                        totalCarbs += food.carbs * log.servingQty
                        totalProtein += food.protein * log.servingQty
                        totalFat += food.fat * log.servingQty
                    }
                }

                // Update UI
                tvCalorieProgress.text = totalCals.toInt().toString()
                pbCalories.max = target
                pbCalories.progress = totalCals.toInt()
                
                tvCarbs.text = "${totalCarbs.toInt()}g"
                tvProtein.text = "${totalProtein.toInt()}g"
                tvFat.text = "${totalFat.toInt()}g"

                // Generate Insights/Warnings
                generateInsights(user.medicalConditions, totalCals, target, totalCarbs, totalFat, totalProtein)
            }
        }
    }

    private fun generateInsights(conditions: String, currentCals: Float, targetCals: Int, carbs: Float, fat: Float, protein: Float) {
        val conditionList = conditions.split(",").map { it.trim() }
        var title = "Tips Harian"
        var body = "Tetap terhidrasi dan makan makanan seimbang!"
        var isWarning = false

        if (currentCals > targetCals) {
            title = "⚠️ Peringatan Kalori"
            body = "Anda telah melebihi batas kalori harian Anda. Cobalah makan lebih ringan untuk sisa hari ini."
            isWarning = true
        } else if (conditionList.contains("Diabetes") && carbs > 250) {
            // Simple threshold for example
            title = "⚠️ Peringatan Diabetes"
            body = "Asupan karbohidrat Anda tinggi (${carbs.toInt()}g). Pantau gula darah Anda dengan cermat."
            isWarning = true
        } else if ((conditionList.contains("Hypertension") || conditionList.contains("Cholesterol")) && fat > 70) {
            title = "⚠️ Peringatan Kesehatan Jantung"
            body = "Asupan lemak tinggi (${fat.toInt()}g). Kurangi makanan gorengan untuk mengelola kondisi Anda."
            isWarning = true
        } else if (conditionList.contains("Gastritis")) {
            // Gastritis alert - always show advice for gastritis patients
            title = "🍃 Pengingat Maag"
            body = "Hindari makanan pedas, asam, dan gorengan. Makan dalam porsi kecil namun sering dan hindari makan larut malam."
            isWarning = true
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

        tvAlertTitle.text = title
        tvAlertBody.text = body
        
        if (isWarning) {
            if (conditionList.contains("Gastritis") && !title.contains("Calorie") && !title.contains("Diabetes") && !title.contains("Heart")) {
                // Gastritis is an informational warning, use orange
                cvAlert.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF3E0")) // Orange tint
                tvAlertTitle.setTextColor(android.graphics.Color.parseColor("#E65100"))
                tvAlertBody.setTextColor(android.graphics.Color.parseColor("#BF360C"))
            } else {
                cvAlert.setCardBackgroundColor(android.graphics.Color.parseColor("#FFEBEE")) // Red tint
                tvAlertTitle.setTextColor(android.graphics.Color.parseColor("#C62828"))
                tvAlertBody.setTextColor(android.graphics.Color.parseColor("#B71C1C"))
            }
        } else {
            cvAlert.setCardBackgroundColor(android.graphics.Color.parseColor("#E8F5E9")) // Green tint
            tvAlertTitle.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            tvAlertBody.setTextColor(android.graphics.Color.parseColor("#1B5E20"))
        }
    }
}