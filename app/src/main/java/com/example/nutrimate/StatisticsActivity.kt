package com.example.nutrimate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class StatisticsActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var currentUsername: String = ""
    private val localeID = Locale.forLanguageTag("id-ID")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", localeID)
    
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
    private lateinit var chartWeeklyCalories: BarChart
    private lateinit var cvMacroChart: CardView
    private lateinit var chartMacros: PieChart
    private lateinit var tvNoData: TextView
    
    // Macro Legend Views
    private lateinit var tvLegendCarbsVal: TextView
    private lateinit var tvLegendCarbsPct: TextView
    private lateinit var tvLegendProteinVal: TextView
    private lateinit var tvLegendProteinPct: TextView
    private lateinit var tvLegendFatVal: TextView
    private lateinit var tvLegendFatPct: TextView

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
        setupCharts()
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
        chartWeeklyCalories = findViewById(R.id.chartWeeklyCalories)
        cvMacroChart = findViewById(R.id.cvMacroChart)
        chartMacros = findViewById(R.id.chartMacros)
        
        tvNoData = findViewById(R.id.tvNoData)
        
        tvLegendCarbsVal = findViewById(R.id.tvLegendCarbsVal)
        tvLegendCarbsPct = findViewById(R.id.tvLegendCarbsPct)
        tvLegendProteinVal = findViewById(R.id.tvLegendProteinVal)
        tvLegendProteinPct = findViewById(R.id.tvLegendProteinPct)
        tvLegendFatVal = findViewById(R.id.tvLegendFatVal)
        tvLegendFatPct = findViewById(R.id.tvLegendFatPct)
        
        // Setup spinner
        val timeRanges = arrayOf("7 Hari Terakhir", "14 Hari Terakhir", "30 Hari Terakhir", "Semua Waktu")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeRanges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimeRange.adapter = adapter
    }

    private fun setupCharts() {
        // Setup Bar Chart
        chartWeeklyCalories.description.isEnabled = false
        chartWeeklyCalories.setDrawGridBackground(false)
        chartWeeklyCalories.setDrawBarShadow(false)
        chartWeeklyCalories.setDrawBorders(false)
        chartWeeklyCalories.legend.isEnabled = false
        
        val xAxis = chartWeeklyCalories.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textColor = ContextCompat.getColor(this, R.color.gray_text)
        
        val leftAxis = chartWeeklyCalories.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.textColor = ContextCompat.getColor(this, R.color.gray_text)
        leftAxis.axisMinimum = 0f // Start at 0
        
        chartWeeklyCalories.axisRight.isEnabled = false
        chartWeeklyCalories.animateY(1000)

        // Setup Pie Chart
        chartMacros.description.isEnabled = false
        chartMacros.isDrawHoleEnabled = true
        chartMacros.holeRadius = 40f
        chartMacros.transparentCircleRadius = 45f
        chartMacros.setHoleColor(Color.WHITE)
        chartMacros.setEntryLabelColor(Color.TRANSPARENT) // Hide labels
        chartMacros.setEntryLabelTextSize(0f) // Hide labels
        chartMacros.legend.isEnabled = false // Disable default legend as we have custom one
        chartMacros.setTouchEnabled(false) // Disable touch interaction
        chartMacros.animateY(1400)
    }

    private fun setupListeners() {
        // Bottom Navigation
        bottomNavigation.selectedItemId = R.id.nav_stats
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    true
                }
                R.id.nav_food_log -> {
                    startActivity(Intent(this, FoodLogActivity::class.java).putExtra("USERNAME", currentUsername))
                    finish()
                    true
                }
                R.id.nav_stats -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java).putExtra("USERNAME", currentUsername))
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

            // Update UI Text
            updateUIText(
                totalDays,
                avgCalories,
                avgCarbs,
                avgProtein,
                avgFat,
                achievementPercent,
                bestDay,
                worstDay,
                trendPercent
            )

            // Update Charts
            updateBarChart(dailyCalories, targetCalories)
            updatePieChart(avgCarbs, avgProtein, avgFat)
        }
    }

    private fun updateUIText(
        totalDays: Int,
        avgCalories: Float,
        avgCarbs: Float,
        avgProtein: Float,
        avgFat: Float,
        achievementPercent: Int,
        bestDay: Map.Entry<String, Float>?,
        worstDay: Map.Entry<String, Float>?,
        trendPercent: Int
    ) {
        tvTotalDays.text = "$totalDays"
        tvAvgCalories.text = "${avgCalories.roundToInt()} kkal"
        tvAvgCarbs.text = "${avgCarbs.roundToInt()}g"
        tvAvgProtein.text = "${avgProtein.roundToInt()}g"
        tvAvgFat.text = "${avgFat.roundToInt()}g"

        tvGoalAchievement.text = "$achievementPercent%"
        pbGoalAchievement.progress = achievementPercent

        // Best day
        if (bestDay != null) {
            tvBestDay.text = formatDate(bestDay.key)
            tvBestDayCalories.text = "${bestDay.value.roundToInt()} kkal"
        }

        // Worst day
        if (worstDay != null) {
            tvWorstDay.text = formatDate(worstDay.key)
            tvWorstDayCalories.text = "${worstDay.value.roundToInt()} kkal"
        }

        // Trend
        val trendText = when {
            trendPercent > 5 -> "↑ Meningkat ($trendPercent%)"
            trendPercent < -5 -> "↓ Menurun ($trendPercent%)"
            else -> "→ Stabil"
        }
        tvCalorieTrend.text = trendText
        tvCalorieTrend.setTextColor(
            when {
                trendPercent > 5 -> ContextCompat.getColor(this, R.color.bmi_overweight) // Orange/Red
                trendPercent < -5 -> ContextCompat.getColor(this, R.color.green_primary)
                else -> ContextCompat.getColor(this, R.color.gray_text)
            }
        )
    }

    private fun updateBarChart(dailyCalories: Map<String, Float>, targetCalories: Int) {
        val sortedDates = dailyCalories.keys.sorted().takeLast(7) // Show last 7 days relevant for chart
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        sortedDates.forEachIndexed { index, date ->
            entries.add(BarEntry(index.toFloat(), dailyCalories[date] ?: 0f))
            labels.add(formatDate(date, "EEE")) // Mon, Tue, etc.
        }

        val set = BarDataSet(entries, "Kalori Harian")
        set.color = ContextCompat.getColor(this, R.color.green_primary)
        set.valueTextColor = ContextCompat.getColor(this, R.color.gray_text)
        set.valueTextSize = 10f
        set.setDrawValues(true)
        
        // Highlight over-budget days
        val colors = ArrayList<Int>()
        for (entry in entries) {
            if (entry.y > targetCalories) {
                colors.add(ContextCompat.getColor(this, R.color.bmi_overweight)) // Orange/Red
            } else {
                colors.add(ContextCompat.getColor(this, R.color.green_primary))
            }
        }
        set.colors = colors

        val data = BarData(set)
        data.barWidth = 0.6f
        
        chartWeeklyCalories.data = data
        chartWeeklyCalories.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        
        // Add LimitLine for Target
        val leftAxis = chartWeeklyCalories.axisLeft
        leftAxis.removeAllLimitLines()
        val ll = LimitLine(targetCalories.toFloat(), "Target")
        ll.lineColor = ContextCompat.getColor(this, R.color.gray_text)
        ll.lineWidth = 1f
        ll.enableDashedLine(10f, 10f, 0f)
        ll.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        ll.textSize = 10f
        leftAxis.addLimitLine(ll)
        
        chartWeeklyCalories.invalidate() // refresh
    }

    private fun updatePieChart(avgCarbs: Float, avgProtein: Float, avgFat: Float) {
        val entries = ArrayList<PieEntry>()
        if (avgCarbs > 0) entries.add(PieEntry(avgCarbs, "Karbo"))
        if (avgProtein > 0) entries.add(PieEntry(avgProtein, "Protein"))
        if (avgFat > 0) entries.add(PieEntry(avgFat, "Lemak"))

        val set = PieDataSet(entries, "")
        set.colors = listOf(
            Color.parseColor("#FF9800"), // Carbs - Orange
            Color.parseColor("#E91E63"), // Protein - Pink/Red
            Color.parseColor("#9C27B0")  // Fat - Purple
        )
        set.sliceSpace = 3f
        set.setDrawValues(false) // Disable drawing values on slices

        val data = PieData(set)
        chartMacros.data = data
        chartMacros.invalidate() // refresh

        // Update Legend Views
        val total = avgCarbs + avgProtein + avgFat
        if (total > 0) {
            val carbsPct = (avgCarbs / total * 100).roundToInt()
            val proteinPct = (avgProtein / total * 100).roundToInt()
            val fatPct = (avgFat / total * 100).roundToInt()

            tvLegendCarbsVal.text = "${avgCarbs.roundToInt()}g"
            tvLegendCarbsPct.text = "($carbsPct%)"

            tvLegendProteinVal.text = "${avgProtein.roundToInt()}g"
            tvLegendProteinPct.text = "($proteinPct%)"

            tvLegendFatVal.text = "${avgFat.roundToInt()}g"
            tvLegendFatPct.text = "($fatPct%)"
        } else {
            tvLegendCarbsVal.text = "0g"
            tvLegendCarbsPct.text = "(0%)"
            tvLegendProteinVal.text = "0g"
            tvLegendProteinPct.text = "(0%)"
            tvLegendFatVal.text = "0g"
            tvLegendFatPct.text = "(0%)"
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

    private fun showNoData() {
        tvNoData.visibility = View.VISIBLE
        cvWeeklyChart.visibility = View.GONE
        cvMacroChart.visibility = View.GONE
        findViewById<CardView>(R.id.cvAverageStats).visibility = View.GONE
        findViewById<CardView>(R.id.cvGoalAchievement).visibility = View.GONE
        findViewById<CardView>(R.id.cvBestWorst).visibility = View.GONE
        findViewById<CardView>(R.id.cvTrend).visibility = View.GONE
    }

    private fun hideNoData() {
        tvNoData.visibility = View.GONE
        cvWeeklyChart.visibility = View.VISIBLE
        cvMacroChart.visibility = View.VISIBLE
        findViewById<CardView>(R.id.cvAverageStats).visibility = View.VISIBLE
        findViewById<CardView>(R.id.cvGoalAchievement).visibility = View.VISIBLE
        findViewById<CardView>(R.id.cvBestWorst).visibility = View.VISIBLE
        findViewById<CardView>(R.id.cvTrend).visibility = View.VISIBLE
    }
}