package com.example.nutrimate.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrimate.ui.theme.GrayText
import com.example.nutrimate.ui.theme.GreenDark
import com.example.nutrimate.ui.theme.GreenPrimary
import kotlinx.coroutines.delay

// Colors specific to this screen
private val WaterCardBackground = Color(0xFFE3F2FD)
private val WaterTextPrimary = Color(0xFF0288D1)
private val WaterTextSecondary = Color(0xFF0277BD)
private val WaterButtonOutline = Color(0xFF81D4FA)
private val WaterProgress = Color(0xFF29B6F6)
private val WaterProgressBg = Color(0xFFE0E0E0)

private val AlertGreenBg = Color(0xFFE8F5E9)
private val AlertGreenText = Color(0xFF2E7D32)
private val AlertGreenTextDark = Color(0xFF1B5E20)
private val AlertRedBg = Color(0xFFFFEBEE)
private val AlertRedText = Color(0xFFC62828)
private val AlertRedTextDark = Color(0xFFB71C1C)
private val AlertOrangeBg = Color(0xFFFFF3E0)
private val AlertOrangeText = Color(0xFFE65100)
private val AlertOrangeTextDark = Color(0xFFBF360C)
private val ChipBorderColor = Color(0xFFE0E0E0)

data class MainScreenState(
    val userName: String = "",
    val currentDate: String = "",
    val calorieProgress: Int = 0,
    val calorieTarget: Int = 2000,
    val carbsGrams: Int = 0,
    val proteinGrams: Int = 0,
    val fatGrams: Int = 0,
    val waterIntake: Int = 0,
    val waterTarget: Int = 2000,
    val alertTitle: String = "Tips Harian!",
    val alertBody: String = "Tetap terhidrasi dan makan makanan seimbang!",
    val alertType: AlertType = AlertType.TIP
)

enum class AlertType {
    TIP,
    WARNING,
    GASTRITIS_WARNING
}

enum class NavItem(
    val title: String,
    val selectedIcon: ImageVector?,
    val unselectedIcon: ImageVector?,
    val useCustomIcon: Boolean = false
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    FOOD_LOG("Food Log", Icons.Filled.Create, Icons.Outlined.Create),
    STATS("Stats", null, null, useCustomIcon = true),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainScreen(
    state: MainScreenState,
    onLogoutClick: () -> Unit = {},
    onFabClick: () -> Unit = {},
    onWaterAdd: (Int) -> Unit = {},
    onNavItemClick: (NavItem) -> Unit = {}
) {
    var selectedNavItem by remember { mutableStateOf(NavItem.HOME) }
    
    // Animation states for progress bars
    var animatedCalorieProgress by remember { mutableFloatStateOf(0f) }
    var animatedWaterProgress by remember { mutableFloatStateOf(0f) }
    
    val animatedCalorie by animateFloatAsState(
        targetValue = animatedCalorieProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "calorie_progress"
    )
    
    val animatedWater by animateFloatAsState(
        targetValue = animatedWaterProgress,
        animationSpec = tween(durationMillis = 500),
        label = "water_progress"
    )
    
    LaunchedEffect(state.calorieProgress, state.calorieTarget) {
        animatedCalorieProgress = if (state.calorieTarget > 0) {
            (state.calorieProgress.toFloat() / state.calorieTarget).coerceIn(0f, 1f)
        } else 0f
    }
    
    LaunchedEffect(state.waterIntake, state.waterTarget) {
        animatedWaterProgress = if (state.waterTarget > 0) {
            (state.waterIntake.toFloat() / state.waterTarget).coerceIn(0f, 1f)
        } else 0f
    }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = GreenDark,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah makanan cepat",
                    tint = Color.White
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = selectedNavItem == item,
                        onClick = {
                            selectedNavItem = item
                            onNavItemClick(item)
                        },
                        icon = {
                            if (item.useCustomIcon) {
                                // Custom bar chart icon for Stats
                                val iconColor = if (selectedNavItem == item) GreenDark else GrayText
                                Canvas(
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    val barWidth = size.width / 5
                                    val gap = barWidth / 2
                                    val heights = listOf(0.5f, 0.75f, 1f)
                                    
                                    heights.forEachIndexed { index, heightFraction ->
                                        val barHeight = size.height * heightFraction
                                        val left = gap + index * (barWidth + gap)
                                        drawRoundRect(
                                            color = iconColor,
                                            topLeft = androidx.compose.ui.geometry.Offset(left, size.height - barHeight),
                                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                        )
                                    }
                                }
                            } else {
                                val icon = if (selectedNavItem == item) item.selectedIcon else item.unselectedIcon
                                icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        label = { 
                            Text(
                                text = item.title,
                                fontSize = 11.sp
                            ) 
                        },
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Section
            HeaderSection(
                userName = state.userName,
                currentDate = state.currentDate
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Calorie Card
            CalorieCard(
                calorieProgress = state.calorieProgress,
                calorieTarget = state.calorieTarget,
                progress = animatedCalorie
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macros Row
            MacrosRow(
                carbsGrams = state.carbsGrams,
                proteinGrams = state.proteinGrams,
                fatGrams = state.fatGrams
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Water Intake Card
            WaterIntakeCard(
                waterIntake = state.waterIntake,
                waterTarget = state.waterTarget,
                progress = animatedWater,
                onWaterAdd = onWaterAdd
            )
            

            Spacer(modifier = Modifier.height(20.dp))
            
            // Insights Section
            InsightsSection(
                title = state.alertTitle,
                body = state.alertBody,
                alertType = state.alertType
            )
            
            Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for FAB
        }
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    currentDate: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Halo, $userName!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = currentDate,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = GreenPrimary
        )
    }
}

@Composable
private fun CalorieCard(
    calorieProgress: Int,
    calorieTarget: Int,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Kalori Hari Ini",
                fontSize = 14.sp,
                color = GrayText
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$calorieProgress",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
                
                Text(
                    text = "/$calorieTarget kkal",
                    fontSize = 18.sp,
                    color = GrayText,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Custom progress bar with gradient look
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFE8E8E8))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    GreenPrimary,
                                    GreenDark
                                )
                            )
                        )
                )
            }
        }
    }
}

// Macro colors from StatisticsActivity
private val CarbsColor = Color(0xFFFF9800)   // Orange
private val ProteinColor = Color(0xFFE91E63) // Pink
private val FatColor = Color(0xFF9C27B0)     // Purple

@Composable
private fun MacrosRow(
    carbsGrams: Int,
    proteinGrams: Int,
    fatGrams: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MacroChip(
            label = "Karbo",
            value = "${carbsGrams}g",
            labelColor = CarbsColor,
            modifier = Modifier.weight(1f)
        )
        
        MacroChip(
            label = "Protein",
            value = "${proteinGrams}g",
            labelColor = ProteinColor,
            modifier = Modifier.weight(1f)
        )
        
        MacroChip(
            label = "Lemak",
            value = "${fatGrams}g",
            labelColor = FatColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MacroChip(
    label: String,
    value: String,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, ChipBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = labelColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                color = GrayText
            )
        }
    }
}

@Composable
private fun WaterIntakeCard(
    waterIntake: Int,
    waterTarget: Int,
    progress: Float,
    onWaterAdd: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WaterCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hidrasi Tubuh",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WaterTextPrimary
                )
                
                Text(
                    text = "$waterIntake/$waterTarget ml",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = WaterTextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Water progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(WaterProgressBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(WaterProgress)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Tambah Cepat",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF546E7A)
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WaterButton(
                    text = "+100",
                    onClick = { onWaterAdd(100) },
                    modifier = Modifier.weight(1f)
                )
                
                WaterButton(
                    text = "+250",
                    onClick = { onWaterAdd(250) },
                    modifier = Modifier.weight(1f)
                )
                
                WaterButton(
                    text = "+500",
                    onClick = { onWaterAdd(500) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WaterButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 100),
        label = "button_scale"
    )
    
    OutlinedButton(
        onClick = {
            scale = 0.9f
            onClick()
        },
        modifier = modifier
            .height(44.dp)
            .scale(animatedScale),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = WaterTextSecondary
        ),
        border = BorderStroke(1.dp, WaterButtonOutline)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
    
    LaunchedEffect(scale) {
        if (scale < 1f) {
            delay(100)
            scale = 1f
        }
    }
}


@Composable
private fun InsightsSection(
    title: String,
    body: String,
    alertType: AlertType
) {
    val (bgColor, titleColor, bodyColor) = when (alertType) {
        AlertType.TIP -> Triple(AlertGreenBg, AlertGreenText, AlertGreenTextDark)
        AlertType.WARNING -> Triple(AlertRedBg, AlertRedText, AlertRedTextDark)
        AlertType.GASTRITIS_WARNING -> Triple(AlertOrangeBg, AlertOrangeText, AlertOrangeTextDark)
    }
    
    Text(
        text = "Wawasan",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = body,
                fontSize = 13.sp,
                color = bodyColor,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun MainScreenPreview() {
    MainScreen(
        state = MainScreenState(
            userName = "Trendo",
            currentDate = "Jumat, 15 Desember 2025",
            calorieProgress = 1454,
            calorieTarget = 2184,
            carbsGrams = 150,
            proteinGrams = 85,
            fatGrams = 45,
            waterIntake = 1250,
            waterTarget = 2000,
            alertTitle = "Tips Harian!",
            alertBody = "Tidur yang cukup penting untuk menjaga metabolisme yang sehat."
        )
    )
}
