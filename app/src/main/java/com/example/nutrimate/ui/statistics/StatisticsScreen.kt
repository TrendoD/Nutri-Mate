package com.example.nutrimate.ui.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrimate.ui.main.NavItem
import com.example.nutrimate.ui.theme.GrayText
import com.example.nutrimate.ui.theme.GreenDark
import com.example.nutrimate.ui.theme.GreenPrimary

// Colors specific to this screen
private val CardBackgroundColor = Color.White
private val TextPrimary = Color(0xFF212121)
private val TextSecondary = Color(0xFF757575)

// Chart colors
private val CarbsColor = Color(0xFFFF9800)    // Orange
private val ProteinColor = Color(0xFFE91E63)  // Pink
private val FatColor = Color(0xFF9C27B0)      // Purple
private val OverBudgetColor = Color(0xFFFF9800) // Orange for over budget

// Best/Worst day colors
private val BestDayBg = Color(0xFFE8F5E9)
private val BestDayLabelColor = Color(0xFF2E7D32)
private val BestDayValueColor = Color(0xFF1B5E20)
private val BestDayCalorieColor = Color(0xFF2E7D32)

private val WorstDayBg = Color(0xFFFFF3E0)
private val WorstDayLabelColor = Color(0xFFE65100)
private val WorstDayValueColor = Color(0xFFBF360C)
private val WorstDayCalorieColor = Color(0xFFE65100)

data class DailyCalorieData(
    val label: String,
    val calories: Float,
    val isOverBudget: Boolean = false
)

data class MacroData(
    val carbsGrams: Float,
    val proteinGrams: Float,
    val fatGrams: Float
)

data class StatisticsScreenState(
    val selectedTimeRange: String = "7 Hari Terakhir",
    val timeRangeOptions: List<String> = listOf(
        "7 Hari Terakhir",
        "14 Hari Terakhir",
        "30 Hari Terakhir",
        "Semua Waktu"
    ),
    val hasData: Boolean = true,
    // Weekly chart data
    val dailyCaloriesData: List<DailyCalorieData> = emptyList(),
    val targetCalories: Int = 2000,
    // Macro distribution
    val macroData: MacroData = MacroData(0f, 0f, 0f),
    // Average stats
    val totalDays: Int = 0,
    val avgCalories: Int = 0,
    val avgCarbs: Int = 0,
    val avgProtein: Int = 0,
    val avgFat: Int = 0,
    // Goal achievement
    val goalAchievementPercent: Int = 0,
    // Best/Worst days
    val bestDay: String = "---",
    val bestDayCalories: Int = 0,
    val worstDay: String = "---",
    val worstDayCalories: Int = 0,
    // Trend
    val trendText: String = "→ Stabil",
    val trendColor: Color = GrayText
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    state: StatisticsScreenState,
    selectedNavItem: NavItem = NavItem.STATS,
    onTimeRangeChange: (String) -> Unit = {},
    onNavItemClick: (NavItem) -> Unit = {}
) {
    var currentNavItem by remember { mutableStateOf(selectedNavItem) }
    var expanded by remember { mutableStateOf(false) }
    
    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = currentNavItem == item,
                        onClick = {
                            currentNavItem = item
                            onNavItemClick(item)
                        },
                        icon = {
                            if (item.useCustomIcon) {
                                val iconColor = if (currentNavItem == item) GreenDark else GrayText
                                Canvas(modifier = Modifier.size(24.dp)) {
                                    val barWidth = size.width / 5
                                    val gap = barWidth / 2
                                    val heights = listOf(0.5f, 0.75f, 1f)
                                    heights.forEachIndexed { index, heightFraction ->
                                        val barHeight = size.height * heightFraction
                                        val left = gap + index * (barWidth + gap)
                                        drawRoundRect(
                                            color = iconColor,
                                            topLeft = Offset(left, size.height - barHeight),
                                            size = Size(barWidth, barHeight),
                                            cornerRadius = CornerRadius(4f, 4f)
                                        )
                                    }
                                }
                            } else {
                                val icon = if (currentNavItem == item) item.selectedIcon else item.unselectedIcon
                                icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        label = { Text(text = item.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GreenDark,
                            selectedTextColor = GreenDark,
                            unselectedIconColor = GrayText,
                            unselectedTextColor = GrayText,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Statistik & Riwayat",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Lacak kemajuan Anda dari waktu ke waktu",
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Time Range Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rentang Waktu:",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = state.selectedTimeRange,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            state.timeRangeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        onTimeRangeChange(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!state.hasData) {
                // No Data Message
                Text(
                    text = "Tidak ada data untuk periode yang dipilih.\nMulai catat makanan Anda untuk melihat statistik!",
                    fontSize = 16.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            } else {
                // Weekly Chart Card
                WeeklyCalorieChartCard(
                    data = state.dailyCaloriesData,
                    targetCalories = state.targetCalories
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Macro Distribution Card
                MacroDistributionCard(
                    macroData = state.macroData
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Average Stats Card
                AverageStatsCard(
                    totalDays = state.totalDays,
                    avgCalories = state.avgCalories,
                    avgCarbs = state.avgCarbs,
                    avgProtein = state.avgProtein,
                    avgFat = state.avgFat
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Goal Achievement Card
                GoalAchievementCard(
                    achievementPercent = state.goalAchievementPercent
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Best & Worst Days Card
                BestWorstDaysCard(
                    bestDay = state.bestDay,
                    bestDayCalories = state.bestDayCalories,
                    worstDay = state.worstDay,
                    worstDayCalories = state.worstDayCalories
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Calorie Trend Card
                CalorieTrendCard(
                    trendText = state.trendText,
                    trendColor = state.trendColor
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun WeeklyCalorieChartCard(
    data: List<DailyCalorieData>,
    targetCalories: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Grafik Kalori Harian",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bar Chart
            BarChart(
                data = data,
                targetCalories = targetCalories,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Target Harian (Garis Putus-putus)",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun BarChart(
    data: List<DailyCalorieData>,
    targetCalories: Int,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    
    // Animate bar heights
    var animationProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "bar_animation"
    )
    
    LaunchedEffect(data) {
        animationProgress = 0f
        animationProgress = 1f
    }
    
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val maxValue = maxOf(data.maxOfOrNull { it.calories } ?: 0f, targetCalories.toFloat())
        val paddingLeft = 50f
        val paddingRight = 16f
        val paddingTop = 24f
        val paddingBottom = 40f
        
        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom
        
        val barWidth = (chartWidth / data.size) * 0.6f
        val barSpacing = (chartWidth / data.size)
        
        // Draw target line (dashed)
        val targetY = paddingTop + chartHeight * (1 - targetCalories / maxValue)
        drawLine(
            color = GrayText,
            start = Offset(paddingLeft, targetY),
            end = Offset(size.width - paddingRight, targetY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        
        // Draw bars
        data.forEachIndexed { index, item ->
            val barHeight = (item.calories / maxValue) * chartHeight * animatedProgress
            val x = paddingLeft + index * barSpacing + (barSpacing - barWidth) / 2
            val y = paddingTop + chartHeight - barHeight
            
            val barColor = if (item.isOverBudget) OverBudgetColor else GreenPrimary
            
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )
            
            // Draw calorie value above bar
            val valueText = "${item.calories.toInt()}"
            val textStyle = TextStyle(fontSize = 10.sp, color = GrayText)
            val textLayoutResult = textMeasurer.measure(valueText, textStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = valueText,
                style = textStyle,
                topLeft = Offset(
                    x + barWidth / 2 - textLayoutResult.size.width / 2,
                    y - textLayoutResult.size.height - 4
                )
            )
            
            // Draw day label below bar
            val labelStyle = TextStyle(fontSize = 10.sp, color = GrayText)
            val labelLayoutResult = textMeasurer.measure(item.label, labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = item.label,
                style = labelStyle,
                topLeft = Offset(
                    x + barWidth / 2 - labelLayoutResult.size.width / 2,
                    paddingTop + chartHeight + 8
                )
            )
        }
    }
}

@Composable
private fun MacroDistributionCard(
    macroData: MacroData
) {
    val total = macroData.carbsGrams + macroData.proteinGrams + macroData.fatGrams
    val carbsPct = if (total > 0) ((macroData.carbsGrams / total) * 100).toInt() else 0
    val proteinPct = if (total > 0) ((macroData.proteinGrams / total) * 100).toInt() else 0
    val fatPct = if (total > 0) ((macroData.fatGrams / total) * 100).toInt() else 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🍰 Distribusi Makronutrisi",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Pie Chart
            PieChart(
                carbsGrams = macroData.carbsGrams,
                proteinGrams = macroData.proteinGrams,
                fatGrams = macroData.fatGrams,
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroLegendItem(
                    label = "Karbo",
                    color = CarbsColor,
                    value = "${macroData.carbsGrams.toInt()}g",
                    percentage = "($carbsPct%)"
                )
                MacroLegendItem(
                    label = "Protein",
                    color = ProteinColor,
                    value = "${macroData.proteinGrams.toInt()}g",
                    percentage = "($proteinPct%)"
                )
                MacroLegendItem(
                    label = "Lemak",
                    color = FatColor,
                    value = "${macroData.fatGrams.toInt()}g",
                    percentage = "($fatPct%)"
                )
            }
        }
    }
}

@Composable
private fun PieChart(
    carbsGrams: Float,
    proteinGrams: Float,
    fatGrams: Float,
    modifier: Modifier = Modifier
) {
    val total = carbsGrams + proteinGrams + fatGrams
    
    // Animate the chart
    var animationProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 1400),
        label = "pie_animation"
    )
    
    LaunchedEffect(carbsGrams, proteinGrams, fatGrams) {
        animationProgress = 0f
        animationProgress = 1f
    }
    
    Canvas(modifier = modifier) {
        if (total <= 0) return@Canvas
        
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f
        val holeRadius = radius * 0.5f
        
        val carbsAngle = (carbsGrams / total) * 360f
        val proteinAngle = (proteinGrams / total) * 360f
        val fatAngle = (fatGrams / total) * 360f
        
        var startAngle = -90f
        
        // Draw Carbs arc
        drawArc(
            color = CarbsColor,
            startAngle = startAngle,
            sweepAngle = carbsAngle * animatedProgress,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        startAngle += carbsAngle
        
        // Draw Protein arc
        drawArc(
            color = ProteinColor,
            startAngle = startAngle,
            sweepAngle = proteinAngle * animatedProgress,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        startAngle += proteinAngle
        
        // Draw Fat arc
        drawArc(
            color = FatColor,
            startAngle = startAngle,
            sweepAngle = fatAngle * animatedProgress,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        
        // Draw hole in the center
        drawCircle(
            color = Color.White,
            radius = holeRadius,
            center = center
        )
    }
}

@Composable
private fun MacroLegendItem(
    label: String,
    color: Color,
    value: String,
    percentage: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "● $label",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = percentage,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun AverageStatsCard(
    totalDays: Int,
    avgCalories: Int,
    avgCarbs: Int,
    avgProtein: Int,
    avgFat: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 Rata-rata Asupan Harian",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Top row: Total Days & Avg Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Hari Tercatat",
                    value = "$totalDays",
                    valueColor = GreenPrimary
                )
                StatItem(
                    label = "Rata-rata Kalori",
                    value = "$avgCalories kkal",
                    valueColor = GreenPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE0E0E0))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bottom row: Macros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Karbo",
                    value = "${avgCarbs}g",
                    valueColor = TextPrimary
                )
                StatItem(
                    label = "Protein",
                    value = "${avgProtein}g",
                    valueColor = TextPrimary
                )
                StatItem(
                    label = "Lemak",
                    value = "${avgFat}g",
                    valueColor = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = if (value.contains("kkal")) 20.sp else 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun GoalAchievementCard(
    achievementPercent: Int
) {
    // Animate progress
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "goal_progress"
    )
    
    LaunchedEffect(achievementPercent) {
        animatedProgress = achievementPercent / 100f
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 Pencapaian Tujuan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Hari dalam target kalori (90-110%)",
                fontSize = 12.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { animatedValue },
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = GreenPrimary,
                    trackColor = Color(0xFFE0E0E0)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "$achievementPercent%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
            }
        }
    }
}

@Composable
private fun BestWorstDaysCard(
    bestDay: String,
    bestDayCalories: Int,
    worstDay: String,
    worstDayCalories: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🏆 Hari Terbaik & Terendah",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Best Day
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BestDayBg)
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tertinggi",
                            fontSize = 12.sp,
                            color = BestDayLabelColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bestDay,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BestDayValueColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$bestDayCalories kkal",
                            fontSize = 12.sp,
                            color = BestDayCalorieColor
                        )
                    }
                }
                
                // Worst Day
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(WorstDayBg)
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Terendah",
                            fontSize = 12.sp,
                            color = WorstDayLabelColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = worstDay,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorstDayValueColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$worstDayCalories kkal",
                            fontSize = 12.sp,
                            color = WorstDayCalorieColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalorieTrendCard(
    trendText: String,
    trendColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📉 Tren Kalori",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Membandingkan paruh pertama vs paruh kedua periode",
                fontSize = 12.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = trendText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = trendColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    StatisticsScreen(
        state = StatisticsScreenState(
            hasData = true,
            dailyCaloriesData = listOf(
                DailyCalorieData("Sen", 1800f, false),
                DailyCalorieData("Sel", 2100f, true),
                DailyCalorieData("Rab", 1950f, false),
                DailyCalorieData("Kam", 1750f, false),
                DailyCalorieData("Jum", 2200f, true),
                DailyCalorieData("Sab", 1900f, false),
                DailyCalorieData("Min", 1850f, false)
            ),
            targetCalories = 2000,
            macroData = MacroData(220f, 85f, 65f),
            totalDays = 7,
            avgCalories = 1936,
            avgCarbs = 220,
            avgProtein = 85,
            avgFat = 65,
            goalAchievementPercent = 71,
            bestDay = "Jum, 20 Des",
            bestDayCalories = 2200,
            worstDay = "Kam, 19 Des",
            worstDayCalories = 1750,
            trendText = "↓ Menurun (-5%)",
            trendColor = GreenPrimary
        )
    )
}
