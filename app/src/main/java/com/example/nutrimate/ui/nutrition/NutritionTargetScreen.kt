package com.example.nutrimate.ui.nutrition

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrimate.ui.theme.GrayText
import com.example.nutrimate.ui.theme.GreenDark
import com.example.nutrimate.ui.theme.GreenLight
import com.example.nutrimate.ui.theme.GreenPrimary
import com.example.nutrimate.ui.theme.PageBackground

// Colors specific to this screen
private val OrangeAccent = Color(0xFFFF5722)
private val CarbsColor = Color(0xFFFF9800)
private val ProteinColor = Color(0xFFF44336)
private val FatColor = Color(0xFF2196F3)
private val WarningColor = Color(0xFFFF9800)
private val TealColor = Color(0xFF009688)

data class NutritionTargetScreenState(
    val autoCalculate: Boolean = false,
    val calorieTarget: String = "",
    val calorieRecommendation: String = "Lengkapi profil Anda untuk mendapatkan rekomendasi yang dipersonalisasi",
    val carbsTarget: String = "",
    val proteinTarget: String = "",
    val fatTarget: String = "",
    val carbsPercentage: Int = 50,
    val proteinPercentage: Int = 20,
    val fatPercentage: Int = 30,
    val sugarLimit: String = "",
    val sodiumLimit: String = "",
    val sugarRecommendation: String = "💡 WHO merekomendasikan <25g untuk gula tambahan",
    val sodiumRecommendation: String = "💡 Disarankan: <2300mg (atau <1500mg untuk hipertensi)",
    val fiberTarget: String = "",
    val waterTarget: String = "",
    val waterGlasses: Int = 8
)

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
fun NutritionTargetScreen(
    state: NutritionTargetScreenState,
    onAutoCalculateChange: (Boolean) -> Unit = {},
    onCalorieTargetChange: (String) -> Unit = {},
    onCarbsTargetChange: (String) -> Unit = {},
    onProteinTargetChange: (String) -> Unit = {},
    onFatTargetChange: (String) -> Unit = {},
    onSugarLimitChange: (String) -> Unit = {},
    onSodiumLimitChange: (String) -> Unit = {},
    onFiberTargetChange: (String) -> Unit = {},
    onWaterTargetChange: (String) -> Unit = {},
    onResetDefaults: () -> Unit = {},
    onSaveTargets: () -> Unit = {},
    onNavItemClick: (NavItem) -> Unit = {}
) {
    var selectedNavItem by remember { mutableStateOf<NavItem?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Atur Ulang ke Default") },
            text = { Text("Ini akan mengatur ulang semua target nutrisi ke nilai default yang disarankan berdasarkan profil Anda. Lanjutkan?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetDefaults()
                    }
                ) {
                    Text("Atur Ulang", color = GreenPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal", color = GrayText)
                }
            }
        )
    }
    
    Scaffold(
        containerColor = PageBackground,
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
                                val iconColor = if (selectedNavItem == item) GreenDark else GrayText
                                Canvas(modifier = Modifier.size(24.dp)) {
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
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Target Nutrisi",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tetapkan tujuan nutrisi harian Anda",
                fontSize = 14.sp,
                color = GrayText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auto Calculate Toggle Card
            AutoCalculateCard(
                isChecked = state.autoCalculate,
                onCheckedChange = onAutoCalculateChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Calorie Target Card
            CalorieTargetCard(
                calorieTarget = state.calorieTarget,
                recommendation = state.calorieRecommendation,
                onCalorieChange = onCalorieTargetChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macronutrients Card
            MacronutrientsCard(
                carbsTarget = state.carbsTarget,
                proteinTarget = state.proteinTarget,
                fatTarget = state.fatTarget,
                carbsPercentage = state.carbsPercentage,
                proteinPercentage = state.proteinPercentage,
                fatPercentage = state.fatPercentage,
                enabled = !state.autoCalculate,
                onCarbsChange = onCarbsTargetChange,
                onProteinChange = onProteinTargetChange,
                onFatChange = onFatTargetChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Daily Limits Card
            DailyLimitsCard(
                sugarLimit = state.sugarLimit,
                sodiumLimit = state.sodiumLimit,
                sugarRecommendation = state.sugarRecommendation,
                sodiumRecommendation = state.sodiumRecommendation,
                onSugarChange = onSugarLimitChange,
                onSodiumChange = onSodiumLimitChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Other Targets Card
            OtherTargetsCard(
                fiberTarget = state.fiberTarget,
                waterTarget = state.waterTarget,
                waterGlasses = state.waterGlasses,
                onFiberChange = onFiberTargetChange,
                onWaterChange = onWaterTargetChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = GrayText
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Atur Ulang ke Default")
                }
                
                Button(
                    onClick = onSaveTargets,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Simpan Target")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AutoCalculateCard(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GreenLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🧮 Hitung Otomatis",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenDark
                )
                Text(
                    text = "Berdasarkan profil dan tujuan Anda",
                    fontSize = 12.sp,
                    color = GrayText
                )
            }
            
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = GreenPrimary,
                    checkedTrackColor = GreenLight,
                    uncheckedThumbColor = GrayText,
                    uncheckedTrackColor = Color(0xFFE0E0E0)
                )
            )
        }
    }
}

@Composable
private fun CalorieTargetCard(
    calorieTarget: String,
    recommendation: String,
    onCalorieChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔥 Target Kalori Harian",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = calorieTarget,
                    onValueChange = onCalorieChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g. 2000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "kkal",
                    fontSize = 14.sp,
                    color = GrayText
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = recommendation,
                fontSize = 12.sp,
                color = GreenPrimary
            )
        }
    }
}

@Composable
private fun MacronutrientsCard(
    carbsTarget: String,
    proteinTarget: String,
    fatTarget: String,
    carbsPercentage: Int,
    proteinPercentage: Int,
    fatPercentage: Int,
    enabled: Boolean,
    onCarbsChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onFatChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🥗 Makronutrisi",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Carbs
            MacroInputField(
                label = "Karbohidrat",
                value = carbsTarget,
                percentage = carbsPercentage,
                unit = "gram",
                enabled = enabled,
                onValueChange = onCarbsChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Protein
            MacroInputField(
                label = "Protein",
                value = proteinTarget,
                percentage = proteinPercentage,
                unit = "gram",
                enabled = enabled,
                onValueChange = onProteinChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fat
            MacroInputField(
                label = "Lemak",
                value = fatTarget,
                percentage = fatPercentage,
                unit = "gram",
                enabled = enabled,
                onValueChange = onFatChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macro Distribution Bar
            MacroDistributionBar(
                carbsPercentage = carbsPercentage,
                proteinPercentage = proteinPercentage,
                fatPercentage = fatPercentage
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroLegendItem(color = CarbsColor, label = "Karbo")
                MacroLegendItem(color = ProteinColor, label = "Protein")
                MacroLegendItem(color = FatColor, label = "Lemak")
            }
        }
    }
}

@Composable
private fun MacroInputField(
    label: String,
    value: String,
    percentage: Int,
    unit: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("e.g. 250") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                enabled = enabled,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledTextColor = GrayText
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = unit,
                fontSize = 14.sp,
                color = GrayText
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "($percentage%)",
                fontSize = 12.sp,
                color = GreenPrimary
            )
        }
    }
}

@Composable
private fun MacroDistributionBar(
    carbsPercentage: Int,
    proteinPercentage: Int,
    fatPercentage: Int
) {
    val total = carbsPercentage + proteinPercentage + fatPercentage
    val carbsWeight = if (total > 0) carbsPercentage.toFloat() / total else 0.33f
    val proteinWeight = if (total > 0) proteinPercentage.toFloat() / total else 0.33f
    val fatWeight = if (total > 0) fatPercentage.toFloat() / total else 0.33f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .animateContentSize()
    ) {
        Box(
            modifier = Modifier
                .weight(carbsWeight.coerceAtLeast(0.01f))
                .fillMaxSize()
                .background(CarbsColor)
        )
        Box(
            modifier = Modifier
                .weight(proteinWeight.coerceAtLeast(0.01f))
                .fillMaxSize()
                .background(ProteinColor)
        )
        Box(
            modifier = Modifier
                .weight(fatWeight.coerceAtLeast(0.01f))
                .fillMaxSize()
                .background(FatColor)
        )
    }
}

@Composable
private fun MacroLegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun DailyLimitsCard(
    sugarLimit: String,
    sodiumLimit: String,
    sugarRecommendation: String,
    sodiumRecommendation: String,
    onSugarChange: (String) -> Unit,
    onSodiumChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⚠️ Batas Harian",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent
            )
            
            Text(
                text = "Penting untuk kondisi medis seperti Diabetes dan Hipertensi",
                fontSize = 12.sp,
                color = GrayText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sugar Limit
            Text(
                text = "🍬 Batas Gula",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = sugarLimit,
                    onValueChange = onSugarChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g. 25") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "gram",
                    fontSize = 14.sp,
                    color = GrayText
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = sugarRecommendation,
                fontSize = 12.sp,
                color = WarningColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sodium Limit
            Text(
                text = "🧂 Batas Natrium",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = sodiumLimit,
                    onValueChange = onSodiumChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g. 2300") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "mg",
                    fontSize = 14.sp,
                    color = GrayText
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = sodiumRecommendation,
                fontSize = 12.sp,
                color = WarningColor
            )
        }
    }
}

@Composable
private fun OtherTargetsCard(
    fiberTarget: String,
    waterTarget: String,
    waterGlasses: Int,
    onFiberChange: (String) -> Unit,
    onWaterChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🌿 Target Lainnya",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fiber Target
            Text(
                text = "🌾 Target Serat",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = fiberTarget,
                    onValueChange = onFiberChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g. 25") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "gram",
                    fontSize = 14.sp,
                    color = GrayText
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Water Target
            Text(
                text = "💧 Target Air",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = waterTarget,
                    onValueChange = onWaterChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g. 2000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "ml",
                    fontSize = 14.sp,
                    color = GrayText
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "($waterGlasses gelas)",
                    fontSize = 12.sp,
                    color = TealColor
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NutritionTargetScreenPreview() {
    NutritionTargetScreen(
        state = NutritionTargetScreenState(
            calorieTarget = "2000",
            calorieRecommendation = "Berdasarkan TDEE Anda: 2000 kkal",
            carbsTarget = "250",
            proteinTarget = "100",
            fatTarget = "65",
            carbsPercentage = 50,
            proteinPercentage = 20,
            fatPercentage = 30,
            sugarLimit = "25",
            sodiumLimit = "2300",
            fiberTarget = "25",
            waterTarget = "2000",
            waterGlasses = 8
        )
    )
}
