package com.example.nutrimate

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class StatisticsActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var currentUsername: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // Views
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var spinnerTimeRange: Spinner
    private lateinit var tvTotalDays: TextView
    private lateinit var tvAvgCalories: TextView
    private lateinit var tvAvgCarbs: TextView
    private lateinit var tvAvgProtein: TextView
    private lateinit var tvAvgFat: TextView
    private lateinit var tvGoalAchievement: TextView
    private lateinit var pbGoalAchievement: ProgressBar
    private lateinit var tvBestDay: TextView
    private lateinit var tvBestDayCalories: TextView
    private lateinit var tvWorstDay: TextView
    private lateinit var tvWorstDayCalories: TextView
    private lateinit var tvCalorieTrend: TextView
    private lateinit var cvWeeklyChart: CardView
    private lateinit var llWeeklyBars: LinearLayout
    private lateinit var tvNoData: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        database = AppDatabase.getDatabase(this)
        
        val username = intent.getStringExtra("USERNAME")
        if (username.isNullOrEmpty()) {
            finish()
            return
        }
        currentUsername = username

        initViews()
        setupListeners()
        loadStatistics("7") // Default to 7 days
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        spinnerTimeRange = findViewById(R.id.spinnerTimeRange)
        tvTotalDays = findViewById(R.id.tvTotalDays)
        tvAvgCalories = findViewById(R.id.tvAvgCalories)
        tvAvgCarbs = findViewById(R.id.tvAvgCarbs)
        tvAvgProtein = findViewById(R.id.tvAvgProtein)
        tvAvgFat = findViewById(R.id.tvAvgFat)
        tvGoalAchievement = findViewById(R.id.tvGoalAchievement)
        pbGoalAchievement = findViewById(R.id.pbGoalAchievement)
        tvBestDay = findViewById(R.id.tvBestDay)
        tvBestDayCalories = findViewById(R.id.tvBestDayCalories)
        tvWorstDay = findViewById(R.id.tvWorstDay)
        tvWorstDayCalories = findViewById(R.id.tvWorstDayCalories)
        tvCalorieTrend = findViewById(R.id.tvCalorieTrend)
        cvWeeklyChart = findViewById(R.id.cvWeeklyChart)
        llWeeklyBars = findViewById(R.id.llWeeklyBars)
        tvNoData = findViewById(R.id.tvNoData)
        
        // Setup spinner
        val timeRanges = arrayOf("Last 7 Days", "Last 14 Days", "Last 30 Days", "All Time")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeRanges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimeRange.adapter = adapter
    }

    private fun setupListeners() {
        // Bottom Navigation
        bottomNavigation.selectedItemId = R.id.nav_stats
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    finish()
                    true
                }
                R.id.nav_food_log -> {
                    finish()
                    true
                }
                R.id.nav_stats -> true
                R.id.nav_profile -> {
                    finish()
                    true
                }
                else -> false
            }
        }
        
        // Spinner listener
        spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val days = when (position) {
                    0 -> "7"
                    1 -> "14"
                    2 -> "30"
                    else -> "all"
                }
                loadStatistics(days)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadStatistics(daysStr: String) {
        lifecycleScope.launch {
            val user = database.userDao().getUserByUsername(currentUsername)
            if (user == null) {
                finish()
                return@launch
            }

            val targetCalories = user.dailyCalorieTarget
            
            // Calculate date range
            val endDate = Date()
            val startDate = if (daysStr == "all") {
                // Get first log date
                Date(0) // Very old date
            } else {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -daysStr.toInt())
                calendar.time
            }

            // Get all logs in range
            val allLogs = database.foodDao().getAllFoodLogs(currentUsername)
            val filteredLogs = allLogs.filter { log ->
                try {
                    val logDate = dateFormat.parse(log.date)
                    logDate != null && !logDate.before(startDate) && !logDate.after(endDate)
                } catch (e: Exception) {
                    false
                }
            }

            if (filteredLogs.isEmpty()) {
                showNoData()
                return@launch
            }

            hideNoData()

            // Group by date
            val logsByDate = filteredLogs.groupBy { it.date }
            val dailyCalories = mutableMapOf<String, Float>()
            val dailyCarbs = mutableMapOf<String, Float>()
            val dailyProtein = mutableMapOf<String, Float>()
            val dailyFat = mutableMapOf<String, Float>()

            for ((date, logs) in logsByDate) {
                var cals = 0f
                var carbs = 0f
                var protein = 0f
                var fat = 0f

                for (log in logs) {
                    val food = database.foodDao().getFoodById(log.foodId)
                    if (food != null) {
                        cals += food.calories * log.servingQty
                        carbs += food.carbs * log.servingQty
                        protein += food.protein * log.servingQty
                        fat += food.fat * log.servingQty
                    }
                }

                dailyCalories[date] = cals
                dailyCarbs[date] = carbs
                dailyProtein[date] = protein
                dailyFat[date] = fat
            }

            // Calculate statistics
            val totalDays = dailyCalories.size
            val avgCalories = dailyCalories.values.average().toFloat()
            val avgCarbs = dailyCarbs.values.average().toFloat()
            val avgProtein = dailyProtein.values.average().toFloat()
            val avgFat = dailyFat.values.average().toFloat()

            // Goal achievement (days within 90-110% of target)
            val daysOnTarget = dailyCalories.values.count { 
                it >= targetCalories * 0.9 && it <= targetCalories * 1.1 
            }
            val achievementPercent = (daysOnTarget.toFloat() / totalDays * 100).roundToInt()

            // Best and worst days
            val bestDay = dailyCalories.maxByOrNull { it.value }
            val worstDay = dailyCalories.minByOrNull { it.value }

            // Calorie trend (compare first half vs second half)
            val sortedDates = dailyCalories.keys.sorted()
            val midPoint = sortedDates.size / 2
            val firstHalfAvg = sortedDates.take(midPoint).mapNotNull { dailyCalories[it] }.average()
            val secondHalfAvg = sortedDates.takeLast(midPoint).mapNotNull { dailyCalories[it] }.average()
            val trendPercent = ((secondHalfAvg - firstHalfAvg) / firstHalfAvg * 100).roundToInt()

            // Update UI
            updateUI(
                totalDays,
                avgCalories,
                avgCarbs,
                avgProtein,
                avgFat,
                achievementPercent,
                bestDay,
                worstDay,
                trendPercent,
                dailyCalories,
                targetCalories
            )

            // Draw weekly chart
            if (daysStr == "7" || daysStr == "14") {
                drawWeeklyChart(dailyCalories, targetCalories)
            }
        }
    }

    private fun updateUI(
        totalDays: Int,
        avgCalories: Float,
        avgCarbs: Float,
        avgProtein: Float,
        avgFat: Float,
        achievementPercent: Int,
        bestDay: Map.Entry<String, Float>?,
        worstDay: Map.Entry<String, Float>?,
        trendPercent: Int,
        dailyCalories: Map<String, Float>,
        targetCalories: Int
    ) {
        tvTotalDays.text = "$totalDays"
        tvAvgCalories.text = "${avgCalories.roundToInt()} kcal"
        tvAvgCarbs.text = "${avgCarbs.roundToInt()}g"
        tvAvgProtein.text = "${avgProtein.roundToInt()}g"
        tvAvgFat.text = "${avgFat.roundToInt()}g"

        tvGoalAchievement.text = "$achievementPercent%"
        pbGoalAchievement.progress = achievementPercent

        // Best day
        if (bestDay != null) {
            val displayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            try {
                val date = dateFormat.parse(bestDay.key)
                tvBestDay.text = if (date != null) displayFormat.format(date) else bestDay.key
            } catch (e: Exception) {
                tvBestDay.text = bestDay.key
            }
            tvBestDayCalories.text = "${bestDay.value.roundToInt()} kcal"
        }

        // Worst day
        if (worstDay != null) {
            val displayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            try {
                val date = dateFormat.parse(worstDay.key)
                tvWorstDay.text = if (date != null) displayFormat.format(date) else worstDay.key
            } catch (e: Exception) {
                tvWorstDay.text = worstDay.key
            }
            tvWorstDayCalories.text = "${worstDay.value.roundToInt()} kcal"
        }

        // Trend
        val trendText = when {
            trendPercent > 5 -> "↑ Increasing ($trendPercent%)"
            trendPercent < -5 -> "↓ Decreasing ($trendPercent%)"
            else -> "→ Stable"
        }
        tvCalorieTrend.text = trendText
        tvCalorieTrend.setTextColor(
            when {
                trendPercent > 5 -> getColor(android.R.color.holo_red_dark)
                trendPercent < -5 -> getColor(android.R.color.holo_green_dark)
                else -> getColor(android.R.color.darker_gray)
            }
        )
    }

    private fun drawWeeklyChart(dailyCalories: Map<String, Float>, targetCalories: Int) {
        llWeeklyBars.removeAllViews()
        
        val sortedDates = dailyCalories.keys.sorted().takeLast(7)
        if (sortedDates.isEmpty()) return

        val maxCalories = dailyCalories.values.maxOrNull() ?: targetCalories.toFloat()
        val chartMax = maxOf(maxCalories, targetCalories.toFloat()) * 1.1f

        val displayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        for (dateStr in sortedDates) {
            val calories = dailyCalories[dateStr] ?: 0f
            val barContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                ).apply {
                    marginEnd = 8
                }
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            }

            // Calorie amount text
            val tvAmount = TextView(this).apply {
                text = calories.roundToInt().toString()
                textSize = 10f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Bar view
            val barHeight = (calories / chartMax * 150).coerceAtLeast(10f).toInt()
            val bar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    barHeight
                ).apply {
                    topMargin = 4
                }
                setBackgroundColor(
                    if (calories >= targetCalories * 0.9 && calories <= targetCalories * 1.1) {
                        getColor(R.color.green_primary)
                    } else if (calories > targetCalories * 1.1) {
                        getColor(android.R.color.holo_orange_dark)
                    } else {
                        getColor(android.R.color.darker_gray)
                    }
                )
            }

            // Day label
            val tvDay = TextView(this).apply {
                try {
                    val date = dateFormat.parse(dateStr)
                    text = if (date != null) displayFormat.format(date) else dateStr
                } catch (e: Exception) {
                    text = dateStr
                }
                textSize = 10f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 4
                }
            }

            barContainer.addView(tvAmount)
            barContainer.addView(bar)
            barContainer.addView(tvDay)
            llWeeklyBars.addView(barContainer)
        }

        cvWeeklyChart.visibility = View.VISIBLE
    }

    private fun showNoData() {
        tvNoData.visibility = View.VISIBLE
        cvWeeklyChart.visibility = View.GONE
        // Hide all stat cards
        findViewById<CardView>(R.id.cvAverageStats).visibility = View.GONE
        findViewById<CardView>(R.id.cvGoalAchievement).visibility = View.GONE
        findViewById<CardView>(R.id.cvBestWorst).visibility = View.GONE
        findViewById<CardView>(R.id.cvTrend).visibility = View.GONE
    }

    private fun hideNoData() {
        tvNoData.visibility = View.GONE
        findViewById<CardView>(R.id.cvAverageStats).visibility = View.VISIBLE
        findViewById<CardView>(R.id.cvGoalAchievement).visibility = View.VISIBLE
        findViewById<CardView>(R.id.cvBestWorst).visibility = View.VISIBLE
        findViewById<CardView>(R.id.cvTrend).visibility = View.VISIBLE
    }
}
