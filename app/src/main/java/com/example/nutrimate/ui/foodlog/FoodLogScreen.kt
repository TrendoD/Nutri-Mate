package com.example.nutrimate.ui.foodlog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrimate.ui.main.NavItem
import com.example.nutrimate.ui.theme.GrayText
import com.example.nutrimate.ui.theme.GreenDark
import com.example.nutrimate.ui.theme.GreenPrimary

// Colors specific to this screen - matching the XML layout
private val CarbsColor = Color(0xFFFF9800)    // Orange
private val ProteinColor = Color(0xFFE91E63) // Pink
private val FatColor = Color(0xFF9C27B0)      // Purple
private val SugarColor = Color(0xFF03A9F4)    // Light Blue
private val SodiumColor = Color(0xFF607D8B)   // Blue Gray
private val FiberColor = Color(0xFF4CAF50)    // Green

private val CardBackgroundColor = Color(0xFFFFFFFF)
private val ChipBorderColor = Color(0xFFE0E0E0)

// Meal section colors
private val BreakfastBg = Color(0xFFE8F5E9)
private val LunchBg = Color(0xFFE3F2FD)
private val DinnerBg = Color(0xFFFFF3E0)
private val SnackBg = Color(0xFFFCE4EC)

// Health state colors
private val ColorStateSafe = Color(0xFF4CAF50)
private val ColorStateWarning = Color(0xFFFF9800)
private val ColorStateDanger = Color(0xFFF44336)

data class FoodLogScreenItem(
    val id: Int,
    val name: String,
    val totalCalories: Float,
    val servingQty: Float,
    val unit: String,
    val foodId: String = "",
    val carbs: Float = 0f,
    val protein: Float = 0f,
    val fat: Float = 0f,
    val caloriesPerServing: Float = 0f
)

data class FoodLogScreenState(
    val currentDate: String = "Hari Ini",
    val isToday: Boolean = true,
    val calorieProgress: Int = 0,
    val calorieTarget: Int = 2000,
    val totalCarbs: Int = 0,
    val totalProtein: Int = 0,
    val totalFat: Int = 0,
    val totalSugar: Int = 0,
    val totalSodium: Int = 0,
    val totalFiber: Int = 0,
    val carbsLimit: Float = 300f,
    val fatLimit: Float = 65f,
    val sugarLimit: Float = 50f,
    val sodiumLimit: Float = 2300f,
    val breakfastItems: List<FoodLogScreenItem> = emptyList(),
    val lunchItems: List<FoodLogScreenItem> = emptyList(),
    val dinnerItems: List<FoodLogScreenItem> = emptyList(),
    val snackItems: List<FoodLogScreenItem> = emptyList()
)

@Composable
fun FoodLogScreen(
    state: FoodLogScreenState,
    selectedNavItem: NavItem = NavItem.FOOD_LOG,
    onPrevDayClick: () -> Unit = {},
    onNextDayClick: () -> Unit = {},
    onDateClick: () -> Unit = {},
    onCopyPreviousDayClick: () -> Unit = {},
    onAddFood: (mealType: String) -> Unit = {},
    onEditFood: (FoodLogScreenItem) -> Unit = {},
    onDeleteFood: (FoodLogScreenItem) -> Unit = {},
    onNavItemClick: (NavItem) -> Unit = {}
) {
    var currentNavItem by remember { mutableStateOf(selectedNavItem) }
    
    // Animation state for progress bar
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    
    val animatedCalorie by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "calorie_progress"
    )
    
    LaunchedEffect(state.calorieProgress, state.calorieTarget) {
        animatedProgress = if (state.calorieTarget > 0) {
            (state.calorieProgress.toFloat() / state.calorieTarget).coerceIn(0f, 1f)
        } else 0f
    }
    
    Scaffold(
        containerColor = Color.White,
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
                                // Custom bar chart icon for Stats
                                val iconColor = if (currentNavItem == item) GreenDark else GrayText
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
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Date Navigation Section
            DateNavigationSection(
                currentDate = state.currentDate,
                isToday = state.isToday,
                onPrevClick = onPrevDayClick,
                onNextClick = onNextDayClick,
                onDateClick = onDateClick
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calorie Card
            CalorieSummaryCard(
                calorieProgress = state.calorieProgress,
                calorieTarget = state.calorieTarget,
                progress = animatedCalorie
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macros Row
            MacrosRow(
                carbsGrams = state.totalCarbs,
                proteinGrams = state.totalProtein,
                fatGrams = state.totalFat,
                carbsLimit = state.carbsLimit,
                fatLimit = state.fatLimit
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Micros Row
            MicrosRow(
                sugarGrams = state.totalSugar,
                sodiumMg = state.totalSodium,
                fiberGrams = state.totalFiber,
                sugarLimit = state.sugarLimit,
                sodiumLimit = state.sodiumLimit
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Copy Previous Day Button
            OutlinedButton(
                onClick = onCopyPreviousDayClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "📋 Salin dari Hari Sebelumnya",
                    color = GreenDark
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Meal Sections
            MealSection(
                title = "🍳 Sarapan",
                backgroundColor = BreakfastBg,
                items = state.breakfastItems,
                onAddClick = { onAddFood("Breakfast") },
                onEditClick = onEditFood,
                onDeleteClick = onDeleteFood
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MealSection(
                title = "🍲 Makan Siang",
                backgroundColor = LunchBg,
                items = state.lunchItems,
                onAddClick = { onAddFood("Lunch") },
                onEditClick = onEditFood,
                onDeleteClick = onDeleteFood
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MealSection(
                title = "🍽️ Makan Malam",
                backgroundColor = DinnerBg,
                items = state.dinnerItems,
                onAddClick = { onAddFood("Dinner") },
                onEditClick = onEditFood,
                onDeleteClick = onDeleteFood
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MealSection(
                title = "🍎 Camilan",
                backgroundColor = SnackBg,
                items = state.snackItems,
                onAddClick = { onAddFood("Snack") },
                onEditClick = onEditFood,
                onDeleteClick = onDeleteFood
            )
            
            Spacer(modifier = Modifier.height(80.dp)) // Bottom padding
        }
    }
}

@Composable
private fun DateNavigationSection(
    currentDate: String,
    isToday: Boolean,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onDateClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Hari sebelumnya",
                modifier = Modifier.size(32.dp)
            )
        }
        
        Text(
            text = currentDate,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .clickable { onDateClick() }
                .padding(8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        IconButton(
            onClick = onNextClick,
            enabled = !isToday
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Hari berikutnya",
                modifier = Modifier.size(32.dp),
                tint = if (isToday) GrayText.copy(alpha = 0.3f) else Color.Black
            )
        }
    }
}

@Composable
private fun CalorieSummaryCard(
    calorieProgress: Int,
    calorieTarget: Int,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Ringkasan Harian",
                fontSize = 16.sp,
                color = GrayText
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                val calorieColor = getHealthColor(calorieProgress.toFloat(), calorieTarget.toFloat())
                
                Text(
                    text = "$calorieProgress",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = calorieColor
                )
                
                Text(
                    text = "/ $calorieTarget kkal",
                    fontSize = 18.sp,
                    color = GrayText,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(GreenPrimary, GreenDark)
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun MacrosRow(
    carbsGrams: Int,
    proteinGrams: Int,
    fatGrams: Int,
    carbsLimit: Float,
    fatLimit: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NutrientChip(
            label = "Karbo",
            value = "${carbsGrams}g",
            labelColor = CarbsColor,
            valueColor = getHealthColor(carbsGrams.toFloat(), carbsLimit),
            modifier = Modifier.weight(1f)
        )
        
        NutrientChip(
            label = "Protein",
            value = "${proteinGrams}g",
            labelColor = ProteinColor,
            valueColor = GrayText,
            modifier = Modifier.weight(1f)
        )
        
        NutrientChip(
            label = "Lemak",
            value = "${fatGrams}g",
            labelColor = FatColor,
            valueColor = getHealthColor(fatGrams.toFloat(), fatLimit),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MicrosRow(
    sugarGrams: Int,
    sodiumMg: Int,
    fiberGrams: Int,
    sugarLimit: Float,
    sodiumLimit: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NutrientChip(
            label = "Gula",
            value = "${sugarGrams}g",
            labelColor = SugarColor,
            valueColor = getHealthColor(sugarGrams.toFloat(), sugarLimit),
            modifier = Modifier.weight(1f)
        )
        
        NutrientChip(
            label = "Natrium",
            value = "${sodiumMg}mg",
            labelColor = SodiumColor,
            valueColor = getHealthColor(sodiumMg.toFloat(), sodiumLimit),
            modifier = Modifier.weight(1f)
        )
        
        NutrientChip(
            label = "Serat",
            value = "${fiberGrams}g",
            labelColor = FiberColor,
            valueColor = GrayText,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NutrientChip(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = labelColor
            )
            Text(
                text = value,
                fontSize = 18.sp,
                color = valueColor
            )
        }
    }
}

@Composable
private fun MealSection(
    title: String,
    backgroundColor: Color,
    items: List<FoodLogScreenItem>,
    onAddClick: () -> Unit,
    onEditClick: (FoodLogScreenItem) -> Unit,
    onDeleteClick: (FoodLogScreenItem) -> Unit
) {
    Column {
        // Section Header
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(8.dp)
        )
        
        // Food Items
        items.forEach { item ->
            FoodLogItemRow(
                item = item,
                onEditClick = { onEditClick(item) },
                onDeleteClick = { onDeleteClick(item) }
            )
        }
        
        // Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onAddClick) {
                Text(
                    text = "+ Tambah Makanan",
                    color = GreenDark
                )
            }
        }
    }
}

@Composable
private fun FoodLogItemRow(
    item: FoodLogScreenItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${item.servingQty} ${item.unit}",
                fontSize = 12.sp,
                color = GrayText
            )
        }
        
        Text(
            text = "${item.totalCalories.toInt()} kkal",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier.size(20.dp)
            )
        }
        
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Hapus",
                modifier = Modifier.size(20.dp),
                tint = Color(0xFFF44336)
            )
        }
    }
}

@Composable
fun EditFoodDialog(
    item: FoodLogScreenItem,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    var quantity by remember { mutableStateOf(item.servingQty.toString()) }
    val previewCalories = (quantity.toFloatOrNull() ?: 0f) * item.caloriesPerServing
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Edit Jumlah Makanan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    color = GrayText
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Jumlah Porsi:",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(text = item.unit)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Kalori: ${previewCalories.toInt()} kkal",
                    color = GreenPrimary,
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toFloatOrNull()
                    if (qty != null && qty > 0) {
                        onSave(qty)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    item: FoodLogScreenItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Hapus Makanan",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Apakah Anda yakin ingin menghapus '${item.name}' dari log Anda?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Text("Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

private fun getHealthColor(current: Float, max: Float): Color {
    return when {
        current > max -> ColorStateDanger
        current >= (max * 0.8f) -> ColorStateWarning
        else -> ColorStateSafe
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun FoodLogScreenPreview() {
    FoodLogScreen(
        state = FoodLogScreenState(
            currentDate = "Hari Ini, Rabu, 25 Desember 2024",
            isToday = true,
            calorieProgress = 1500,
            calorieTarget = 2000,
            totalCarbs = 180,
            totalProtein = 60,
            totalFat = 50,
            totalSugar = 30,
            totalSodium = 1500,
            totalFiber = 20,
            breakfastItems = listOf(
                FoodLogScreenItem(
                    id = 1,
                    name = "Nasi Goreng",
                    totalCalories = 450f,
                    servingQty = 1f,
                    unit = "porsi",
                    caloriesPerServing = 450f
                )
            ),
            lunchItems = listOf(
                FoodLogScreenItem(
                    id = 2,
                    name = "Ayam Bakar",
                    totalCalories = 350f,
                    servingQty = 1f,
                    unit = "porsi",
                    caloriesPerServing = 350f
                )
            )
        )
    )
}
