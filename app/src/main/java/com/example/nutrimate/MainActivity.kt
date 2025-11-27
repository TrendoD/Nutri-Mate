package com.example.nutrimate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var btnProfile: MaterialButton
    private lateinit var btnFoodLog: MaterialButton
    
    // Dashboard Views
    private lateinit var tvCalorieProgress: TextView
    private lateinit var tvCalorieTarget: TextView
    private lateinit var tvCaloriePercentage: TextView
    private lateinit var pbCalories: ProgressBar
    private lateinit var tvCarbs: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvFat: TextView
    private lateinit var tvCarbsTarget: TextView
    private lateinit var tvProteinTarget: TextView
    private lateinit var tvFatTarget: TextView
    private lateinit var cvAlert: MaterialCardView
    private lateinit var tvAlertTitle: TextView
    private lateinit var tvAlertBody: TextView
    private lateinit var ivAlertIcon: ImageView
    private lateinit var cvMedicalConditions: MaterialCardView
    private lateinit var tvMedicalConditions: TextView

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
        tvCaloriePercentage = findViewById(R.id.tvCaloriePercentage)
        pbCalories = findViewById(R.id.pbCalories)
        tvCarbs = findViewById(R.id.tvCarbs)
        tvProtein = findViewById(R.id.tvProtein)
        tvFat = findViewById(R.id.tvFat)
        tvCarbsTarget = findViewById(R.id.tvCarbsTarget)
        tvProteinTarget = findViewById(R.id.tvProteinTarget)
        tvFatTarget = findViewById(R.id.tvFatTarget)
        
        cvAlert = findViewById(R.id.cvAlert)
        tvAlertTitle = findViewById(R.id.tvAlertTitle)
        tvAlertBody = findViewById(R.id.tvAlertBody)
        ivAlertIcon = findViewById(R.id.ivAlertIcon)
        
        cvMedicalConditions = findViewById(R.id.cvMedicalConditions)
        tvMedicalConditions = findViewById(R.id.tvMedicalConditions)
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

                tvWelcome.text = user.fullName
                val target = user.dailyCalorieTarget
                tvCalorieTarget.text = "/ $target kcal"

                // Calculate macro targets based on calorie target
                // Standard macro split: 50% carbs, 20% protein, 30% fat
                val carbsTarget = (target * 0.5 / 4).toInt() // 4 cal per gram
                val proteinTarget = (target * 0.2 / 4).toInt() // 4 cal per gram
                val fatTarget = (target * 0.3 / 9).toInt() // 9 cal per gram
                
                tvCarbsTarget.text = "/ ${carbsTarget}g"
                tvProteinTarget.text = "/ ${proteinTarget}g"
                tvFatTarget.text = "/ ${fatTarget}g"

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
                pbCalories.progress = totalCals.toInt().coerceAtMost(target)
                
                val percentage = if (target > 0) (totalCals / target * 100).toInt() else 0
                val percentageText = when {
                    percentage > 100 -> "${percentage}% (exceeded daily goal)"
                    percentage >= 90 -> "$percentage% - almost there!"
                    else -> "$percentage% of daily goal"
                }
                tvCaloriePercentage.text = percentageText
                
                tvCarbs.text = "${totalCarbs.toInt()}g"
                tvProtein.text = "${totalProtein.toInt()}g"
                tvFat.text = "${totalFat.toInt()}g"

                // Show medical conditions if any
                if (user.medicalConditions.isNotEmpty()) {
                    cvMedicalConditions.visibility = View.VISIBLE
                    tvMedicalConditions.text = user.medicalConditions.replace(",", " • ")
                } else {
                    cvMedicalConditions.visibility = View.GONE
                }

                // Generate Insights/Warnings
                generateInsights(user.medicalConditions, totalCals, target, totalCarbs, totalProtein, totalFat, carbsTarget, proteinTarget, fatTarget)
            }
        }
    }

    private fun generateInsights(
        conditions: String, 
        currentCals: Float, 
        targetCals: Int, 
        carbs: Float,
        protein: Float,
        fat: Float,
        carbsTarget: Int,
        proteinTarget: Int,
        fatTarget: Int
    ) {
        val conditionList = conditions.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        var title = "Daily Tip"
        var body = "Stay hydrated and eat balanced meals! Track your food to get personalized recommendations."
        var alertType = AlertType.INFO
        
        // Check for various conditions and provide specific insights
        when {
            currentCals > targetCals -> {
                title = "Calorie Limit Exceeded"
                body = "You've consumed ${currentCals.toInt()} kcal, which is ${(currentCals - targetCals).toInt()} kcal over your daily target. Consider lighter meals for the rest of the day."
                alertType = AlertType.WARNING
            }
            currentCals > targetCals * 0.9 && currentCals <= targetCals -> {
                title = "Almost There!"
                body = "You're at ${(currentCals / targetCals * 100).toInt()}% of your daily calorie goal. Only ${(targetCals - currentCals).toInt()} kcal remaining."
                alertType = AlertType.SUCCESS
            }
            conditionList.contains("Diabetes") && carbs > carbsTarget -> {
                title = "Diabetes Alert - High Carbs"
                body = "Your carb intake (${carbs.toInt()}g) exceeds the recommended ${carbsTarget}g. High carb intake can affect blood sugar levels. Consider low-carb alternatives."
                alertType = AlertType.DANGER
            }
            conditionList.contains("Diabetes") && carbs > carbsTarget * 0.8 -> {
                title = "Diabetes Reminder"
                body = "You've consumed ${(carbs / carbsTarget * 100).toInt()}% of your daily carb limit. Monitor your intake to maintain stable blood sugar levels."
                alertType = AlertType.WARNING
            }
            (conditionList.contains("Hypertension") || conditionList.contains("Cholesterol")) && fat > fatTarget -> {
                title = "Heart Health Alert"
                body = "Fat intake (${fat.toInt()}g) is above the recommended ${fatTarget}g. For heart health, consider reducing fried and fatty foods."
                alertType = AlertType.DANGER
            }
            (conditionList.contains("Hypertension") || conditionList.contains("Cholesterol")) && fat > fatTarget * 0.8 -> {
                title = "Heart Health Tip"
                body = "You're approaching your daily fat limit. Choose lean proteins and avoid excessive oil for better heart health."
                alertType = AlertType.WARNING
            }
            conditionList.contains("Gastritis") && currentCals < targetCals * 0.3 -> {
                title = "Gastritis Reminder"
                body = "You haven't eaten much today. For gastritis management, avoid long gaps between meals. Consider small, frequent meals."
                alertType = AlertType.WARNING
            }
            protein < proteinTarget * 0.5 && currentCals > targetCals * 0.5 -> {
                title = "Protein Intake Low"
                body = "Your protein intake (${protein.toInt()}g) is below the recommended level. Include lean meats, eggs, or legumes in your next meal."
                alertType = AlertType.INFO
            }
            currentCals == 0f -> {
                title = "Start Tracking!"
                body = "You haven't logged any food today. Tap 'Track Food' to start monitoring your nutrition."
                alertType = AlertType.INFO
            }
            conditionList.isNotEmpty() -> {
                title = "Personalized for You"
                val conditionAdvice = buildConditionAdvice(conditionList)
                body = conditionAdvice
                alertType = AlertType.INFO
            }
        }

        tvAlertTitle.text = title
        tvAlertBody.text = body
        
        // Update card appearance based on alert type
        updateAlertAppearance(alertType)
    }
    
    private fun buildConditionAdvice(conditions: List<String>): String {
        val advices = mutableListOf<String>()
        
        if (conditions.contains("Diabetes")) {
            advices.add("Monitor carbs for diabetes management")
        }
        if (conditions.contains("Hypertension")) {
            advices.add("Watch sodium and fat intake for blood pressure")
        }
        if (conditions.contains("Cholesterol")) {
            advices.add("Limit saturated fats for cholesterol control")
        }
        if (conditions.contains("Gastritis")) {
            advices.add("Eat small, frequent meals for gastritis")
        }
        
        return if (advices.isNotEmpty()) {
            "Based on your conditions: ${advices.joinToString(". ")}."
        } else {
            "Track your meals regularly for better health insights!"
        }
    }
    
    private enum class AlertType {
        INFO, SUCCESS, WARNING, DANGER
    }
    
    private fun updateAlertAppearance(type: AlertType) {
        val (backgroundColor, textColor, iconRes) = when (type) {
            AlertType.INFO -> Triple(
                getColor(R.color.alert_info),
                getColor(R.color.alert_info_text),
                android.R.drawable.ic_dialog_info
            )
            AlertType.SUCCESS -> Triple(
                getColor(R.color.alert_success),
                getColor(R.color.alert_success_text),
                android.R.drawable.checkbox_on_background
            )
            AlertType.WARNING -> Triple(
                getColor(R.color.alert_warning),
                getColor(R.color.alert_warning_text),
                android.R.drawable.ic_dialog_alert
            )
            AlertType.DANGER -> Triple(
                getColor(R.color.alert_danger),
                getColor(R.color.alert_danger_text),
                android.R.drawable.ic_dialog_alert
            )
        }
        
        cvAlert.setCardBackgroundColor(backgroundColor)
        cvAlert.strokeColor = textColor
        cvAlert.strokeWidth = resources.getDimensionPixelSize(R.dimen.card_stroke_width)
        tvAlertTitle.setTextColor(textColor)
        tvAlertBody.setTextColor(textColor)
        ivAlertIcon.setImageResource(iconRes)
        ivAlertIcon.setColorFilter(textColor)
    }
}
