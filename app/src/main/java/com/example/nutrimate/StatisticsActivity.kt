package com.example.nutrimate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.ui.main.NavItem
import com.example.nutrimate.ui.statistics.DailyCalorieData
import com.example.nutrimate.ui.statistics.MacroData
import com.example.nutrimate.ui.statistics.StatisticsScreen
import com.example.nutrimate.ui.statistics.StatisticsScreenState
import com.example.nutrimate.ui.theme.GreenPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class StatisticsActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private var currentUsername: String = ""
    private val localeID = Locale.forLanguageTag("id-ID")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", localeID)

    // Compose state
    private var screenState by mutableStateOf(StatisticsScreenState())

    // Colors for trend
    private val TrendIncreasing = Color(0xFFFF9800) // Orange
    private val TrendDecreasing = Color(0xFF4CAF50) // Green
    private val TrendStable = Color(0xFF757575)     // Gray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(this)

        val username = intent.getStringExtra("USERNAME")
        if (username.isNullOrEmpty()) {
            finish()
            return
        }
        currentUsername = username

        // Load initial statistics
        loadStatistics("7")

        setContent {
            StatisticsScreen(
                state = screenState,
                selectedNavItem = NavItem.STATS,
                onTimeRangeChange = { timeRange ->
                    val days = when (timeRange) {
                        "7 Hari Terakhir" -> "7"
                        "14 Hari Terakhir" -> "14"
                        "30 Hari Terakhir" -> "30"
                        else -> "all"
                    }
                    screenState = screenState.copy(selectedTimeRange = timeRange)
                    loadStatistics(days)
                },
                onNavItemClick = { navItem ->
                    when (navItem) {
                        NavItem.HOME -> {
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                        }
                        NavItem.FOOD_LOG -> {
                            startActivity(Intent(this, FoodLogActivity::class.java).putExtra("USERNAME", currentUsername))
                            finish()
                        }
                        NavItem.STATS -> { /* Already on Stats */ }
                        NavItem.SETTINGS -> {
                            startActivity(Intent(this, SettingsActivity::class.java).putExtra("USERNAME", currentUsername))
                            finish()
                        }
                    }
                }
            )
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
                Date(0)
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
                screenState = screenState.copy(hasData = false)
                return@launch
            }

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

            // Goal achievement
            val daysOnTarget = dailyCalories.values.count {
                it >= targetCalories * 0.9 && it <= targetCalories * 1.1
            }
            val achievementPercent = if (totalDays > 0) (daysOnTarget.toFloat() / totalDays * 100).roundToInt() else 0

            // Best and worst days
            val bestDay = dailyCalories.maxByOrNull { it.value }
            val worstDay = dailyCalories.minByOrNull { it.value }

            // Calorie trend
            val sortedDates = dailyCalories.keys.sorted()
            val trendPercent = if (sortedDates.size < 2) {
                0
            } else {
                val midPoint = sortedDates.size / 2
                val firstHalfAvg = sortedDates.take(midPoint).mapNotNull { dailyCalories[it] }.average()
                val secondHalfAvg = sortedDates.takeLast(midPoint).mapNotNull { dailyCalories[it] }.average()

                if (firstHalfAvg.isNaN() || secondHalfAvg.isNaN() || firstHalfAvg == 0.0) {
                    0
                } else {
                    ((secondHalfAvg - firstHalfAvg) / firstHalfAvg * 100).roundToInt()
                }
            }

            // Prepare chart data (last 7 days)
            val chartData = sortedDates.takeLast(7).map { date ->
                DailyCalorieData(
                    label = formatDate(date, "EEE"),
                    calories = dailyCalories[date] ?: 0f,
                    isOverBudget = (dailyCalories[date] ?: 0f) > targetCalories
                )
            }

            // Trend text and color
            val (trendText, trendColor) = when {
                trendPercent > 5 -> "↑ Meningkat ($trendPercent%)" to TrendIncreasing
                trendPercent < -5 -> "↓ Menurun ($trendPercent%)" to TrendDecreasing
                else -> "→ Stabil" to TrendStable
            }

            // Update state
            screenState = StatisticsScreenState(
                selectedTimeRange = screenState.selectedTimeRange,
                hasData = true,
                dailyCaloriesData = chartData,
                targetCalories = targetCalories,
                macroData = MacroData(avgCarbs, avgProtein, avgFat),
                totalDays = totalDays,
                avgCalories = avgCalories.roundToInt(),
                avgCarbs = avgCarbs.roundToInt(),
                avgProtein = avgProtein.roundToInt(),
                avgFat = avgFat.roundToInt(),
                goalAchievementPercent = achievementPercent,
                bestDay = bestDay?.key?.let { formatDate(it) } ?: "---",
                bestDayCalories = bestDay?.value?.roundToInt() ?: 0,
                worstDay = worstDay?.key?.let { formatDate(it) } ?: "---",
                worstDayCalories = worstDay?.value?.roundToInt() ?: 0,
                trendText = trendText,
                trendColor = trendColor
            )
        }
    }

    private fun formatDate(dateStr: String, pattern: String = "MMM dd"): String {
        return try {
            val date = dateFormat.parse(dateStr)
            val displayFormat = SimpleDateFormat(pattern, localeID)
            if (date != null) displayFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }
}