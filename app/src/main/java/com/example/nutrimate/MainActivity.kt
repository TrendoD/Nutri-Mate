package com.example.nutrimate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnProfile: Button
    private lateinit var btnFoodLog: Button
    
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

    private lateinit var database: AppDatabase
    private var currentUsername: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
    }

    override fun onResume() {
        super.onResume()
        if (currentUsername.isNotEmpty()) {
            loadDashboardData(currentUsername)
        }
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnLogout = findViewById(R.id.btnLogout)
        btnProfile = findViewById(R.id.btnProfile)
        btnFoodLog = findViewById(R.id.btnFoodLog)
        
        tvCalorieProgress = findViewById(R.id.tvCalorieProgress)
        tvCalorieTarget = findViewById(R.id.tvCalorieTarget)
        pbCalories = findViewById(R.id.pbCalories)
        tvCarbs = findViewById(R.id.tvCarbs)
        tvProtein = findViewById(R.id.tvProtein)
        tvFat = findViewById(R.id.tvFat)
        
        cvAlert = findViewById(R.id.cvAlert)
        tvAlertTitle = findViewById(R.id.tvAlertTitle)
        tvAlertBody = findViewById(R.id.tvAlertBody)
    }

    private fun setupListeners() {
        btnFoodLog.setOnClickListener {
            val intent = Intent(this, FoodLogActivity::class.java)
            intent.putExtra("USERNAME", currentUsername)
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USERNAME", currentUsername)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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

                tvWelcome.text = "Hello, ${user.fullName}!"
                val target = user.dailyCalorieTarget
                tvCalorieTarget.text = "/ $target kcal"

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
                generateInsights(user.medicalConditions, totalCals, target, totalCarbs, totalFat)
            }
        }
    }

    private fun generateInsights(conditions: String, currentCals: Float, targetCals: Int, carbs: Float, fat: Float) {
        val conditionList = conditions.split(",").map { it.trim() }
        var title = "Daily Tip"
        var body = "Stay hydrated and eat balanced meals!"
        var isWarning = false

        if (currentCals > targetCals) {
            title = "Calorie Alert"
            body = "You have exceeded your daily calorie limit. Try to eat lighter for the rest of the day."
            isWarning = true
        } else if (conditionList.contains("Diabetes") && carbs > 250) {
            // Simple threshold for example
            title = "Diabetes Alert"
            body = "Your carb intake is high ($carbs g). Monitor your blood sugar closely."
            isWarning = true
        } else if (conditionList.contains("Hypertension") || conditionList.contains("Cholesterol")) {
            if (fat > 70) {
                 title = "Heart Health Alert"
                 body = "Fat intake is high ($fat g). Reduce fried foods to manage your condition."
                 isWarning = true
            }
        }

        tvAlertTitle.text = title
        tvAlertBody.text = body
        
        if (isWarning) {
            cvAlert.setCardBackgroundColor(android.graphics.Color.parseColor("#FFEBEE")) // Red tint
            tvAlertTitle.setTextColor(android.graphics.Color.parseColor("#C62828"))
            tvAlertBody.setTextColor(android.graphics.Color.parseColor("#B71C1C"))
        } else {
            cvAlert.setCardBackgroundColor(android.graphics.Color.parseColor("#E3F2FD")) // Blue tint
            tvAlertTitle.setTextColor(android.graphics.Color.parseColor("#1565C0"))
            tvAlertBody.setTextColor(android.graphics.Color.parseColor("#0D47A1"))
        }
    }
}
