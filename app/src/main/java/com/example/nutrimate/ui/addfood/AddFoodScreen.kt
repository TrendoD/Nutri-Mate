package com.example.nutrimate.ui.addfood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrimate.ui.theme.GrayText
import com.example.nutrimate.ui.theme.GreenDark
import com.example.nutrimate.ui.theme.GreenPrimary
import com.example.nutrimate.ui.theme.PageBackground

// Color constants for this screen
private val CategoryFruitBg = Color(0xFFE8F5E9)
private val CategoryVegetableBg = Color(0xFFE8F5E9)
private val CategoryProteinBg = Color(0xFFFFEBEE)
private val CategoryGrainBg = Color(0xFFFFF3E0)
private val CategoryDairyBg = Color(0xFFE3F2FD)
private val CategoryOtherBg = Color(0xFFF5F5F5)

private val SelectedChipBg = Color(0xFF757575)
private val UnselectedChipBg = Color.Transparent

data class FoodItem(
    val id: String,
    val name: String,
    val servingSize: Float,
    val servingUnit: String,
    val calories: Float,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val sugar: Float,
    val fiber: Float,
    val sodium: Float,
    val category: String,
    val isFavorite: Boolean = false
)

data class AddFoodScreenState(
    val mealType: String = "Breakfast",
    val searchQuery: String = "",
    val currentFilter: String = "All",
    val foods: List<FoodItem> = emptyList(),
    val isLoading: Boolean = false
)

enum class FoodFilter(val displayName: String, val internalName: String) {
    ALL("Semua", "All"),
    RECENT("Baru Saja", "Recent"),
    FAVORITES("Favorit", "Favorites"),
    FRUIT("Buah", "Fruit"),
    VEGETABLE("Sayur", "Vegetable"),
    PROTEIN("Protein", "Protein"),
    GRAIN("Biji-bijian", "Grain"),
    DAIRY("Produk Susu", "Dairy")
}

@Composable
fun AddFoodScreen(
    state: AddFoodScreenState,
    onSearchQueryChange: (String) -> Unit = {},
    onFilterChange: (String) -> Unit = {},
    onFoodClick: (FoodItem) -> Unit = {},
    onFavoriteClick: (FoodItem) -> Unit = {},
    onCreateCustomFoodClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val displayMealType = when (state.mealType) {
        "Breakfast" -> "Sarapan"
        "Lunch" -> "Makan Siang"
        "Dinner" -> "Makan Malam"
        "Snack" -> "Camilan"
        else -> state.mealType
    }

    Scaffold(
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Tambah ke $displayMealType",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Cari makanan (mis. Nasi Goreng)",
                        color = GrayText
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GrayText
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FoodFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.currentFilter == filter.internalName,
                        onClick = { onFilterChange(filter.internalName) },
                        label = {
                            Text(
                                text = filter.displayName,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = UnselectedChipBg,
                            selectedContainerColor = SelectedChipBg,
                            labelColor = GrayText,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.height(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Create Custom Food Button
            TextButton(
                onClick = onCreateCustomFoodClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "+ Buat Makanan Kustom",
                    color = GreenPrimary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Food List
            state.foods.forEach { food ->
                FoodSearchItem(
                    food = food,
                    onClick = { onFoodClick(food) },
                    onFavoriteClick = { onFavoriteClick(food) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.foods.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada makanan ditemukan",
                        color = GrayText,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun FoodSearchItem(
    food: FoodItem,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val categoryBgColor = when (food.category.lowercase()) {
        "fruit" -> CategoryFruitBg
        "vegetable" -> CategoryVegetableBg
        "protein" -> CategoryProteinBg
        "grain" -> CategoryGrainBg
        "dairy" -> CategoryDairyBg
        else -> CategoryOtherBg
    }

    val categoryDisplayName = when (food.category) {
        "Fruit" -> "Buah"
        "Vegetable" -> "Sayur"
        "Protein" -> "Protein"
        "Grain" -> "Biji-bijian"
        "Dairy" -> "Produk Susu"
        else -> "Lainnya"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Food name
                Text(
                    text = food.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Serving and calories
                Text(
                    text = "${food.servingSize.toInt()} ${food.servingUnit} | ${food.calories.toInt()} kkal",
                    fontSize = 14.sp,
                    color = GrayText
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Macros
                Text(
                    text = "K: ${food.carbs.toInt()}g | P: ${food.protein.toInt()}g | L: ${food.fat.toInt()}g",
                    fontSize = 12.sp,
                    color = GrayText
                )

                // Extra nutrition
                Text(
                    text = "Gula: ${food.sugar.toInt()}g | Srt: ${food.fiber.toInt()}g | Na: ${food.sodium.toInt()}mg",
                    fontSize = 12.sp,
                    color = GrayText
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category chip
                Surface(
                    color = categoryBgColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = categoryDisplayName,
                        fontSize = 11.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Favorite icon
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (food.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (food.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (food.isFavorite) Color(0xFFFFD700) else GrayText
                )
            }
        }
    }
}

@Composable
fun AddFoodDialog(
    foodName: String,
    servingUnit: String,
    caloriesPerServing: Float,
    carbsPerServing: Float,
    proteinPerServing: Float,
    fatPerServing: Float,
    sugarPerServing: Float,
    fiberPerServing: Float,
    sodiumPerServing: Float,
    onDismiss: () -> Unit,
    onAdd: (Float) -> Unit
) {
    var quantity by remember { mutableStateOf("1.0") }
    val qty = quantity.toFloatOrNull() ?: 1.0f

    val previewCalories = (caloriesPerServing * qty).toInt()
    val previewCarbs = (carbsPerServing * qty).toInt()
    val previewProtein = (proteinPerServing * qty).toInt()
    val previewFat = (fatPerServing * qty).toInt()
    val previewSugar = (sugarPerServing * qty).toInt()
    val previewFiber = (fiberPerServing * qty).toInt()
    val previewSodium = (sodiumPerServing * qty).toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tambah $foodName",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
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

                    Text(text = servingUnit)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nutrition Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Pratinjau Nutrisi:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Kalori: $previewCalories kkal",
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Karbo: ${previewCarbs}g | Protein: ${previewProtein}g | Lemak: ${previewFat}g",
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Gula: ${previewSugar}g | Serat: ${previewFiber}g | Na: ${previewSodium}mg",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qtyValue = quantity.toFloatOrNull()
                    if (qtyValue != null && qtyValue > 0) {
                        onAdd(qtyValue)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
            ) {
                Text("Tambahkan ke Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    color = GrayText
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomFoodDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        servingSize: Float,
        servingUnit: String,
        calories: Float,
        carbs: Float,
        protein: Float,
        fat: Float,
        sugar: Float,
        fiber: Float,
        sodium: Float,
        category: String
    ) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }
    var servingUnit by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var sodium by remember { mutableStateOf("") }

    val categories = listOf("Buah", "Sayur", "Protein", "Biji-bijian", "Produk Susu", "Lainnya")
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Buat Makanan Kustom",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Food Name
                FormField(
                    label = "Nama Makanan *",
                    value = foodName,
                    onValueChange = { foodName = it },
                    placeholder = "mis. Salad Spesial Saya"
                )

                // Serving Size Row
                Text(
                    text = "Ukuran Porsi *",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = servingSize,
                        onValueChange = { servingSize = it },
                        placeholder = { Text("1.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = servingUnit,
                        onValueChange = { servingUnit = it },
                        placeholder = { Text("piring/cangkir/gram") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calories
                FormField(
                    label = "Kalori (kkal) *",
                    value = calories,
                    onValueChange = { calories = it },
                    placeholder = "300",
                    isNumeric = true
                )

                // Carbs
                FormField(
                    label = "Karbohidrat (g) *",
                    value = carbs,
                    onValueChange = { carbs = it },
                    placeholder = "45",
                    isNumeric = true
                )

                // Protein
                FormField(
                    label = "Protein (g) *",
                    value = protein,
                    onValueChange = { protein = it },
                    placeholder = "8",
                    isNumeric = true
                )

                // Fat
                FormField(
                    label = "Lemak (g) *",
                    value = fat,
                    onValueChange = { fat = it },
                    placeholder = "10",
                    isNumeric = true
                )

                // Sugar
                FormField(
                    label = "Gula (g)",
                    value = sugar,
                    onValueChange = { sugar = it },
                    placeholder = "0",
                    isNumeric = true
                )

                // Fiber
                FormField(
                    label = "Serat (g)",
                    value = fiber,
                    onValueChange = { fiber = it },
                    placeholder = "0",
                    isNumeric = true
                )

                // Sodium
                FormField(
                    label = "Natrium (mg)",
                    value = sodium,
                    onValueChange = { sodium = it },
                    placeholder = "0",
                    isNumeric = true
                )

                // Category Dropdown
                Text(
                    text = "Kategori",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = foodName.trim()
                    val size = servingSize.toFloatOrNull() ?: 0f
                    val unit = servingUnit.trim()
                    val cal = calories.toFloatOrNull() ?: 0f
                    val carb = carbs.toFloatOrNull() ?: 0f
                    val prot = protein.toFloatOrNull() ?: 0f
                    val fatVal = fat.toFloatOrNull() ?: 0f
                    val sug = sugar.toFloatOrNull() ?: 0f
                    val fib = fiber.toFloatOrNull() ?: 0f
                    val sod = sodium.toFloatOrNull() ?: 0f

                    if (name.isNotEmpty() && unit.isNotEmpty() && size > 0 && cal > 0) {
                        onSave(name, size, unit, cal, carb, prot, fatVal, sug, fib, sod, selectedCategory)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
            ) {
                Text("Simpan Makanan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    color = GrayText
                )
            }
        }
    )
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isNumeric: Boolean = false
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            keyboardOptions = if (isNumeric) {
                KeyboardOptions(keyboardType = KeyboardType.Decimal)
            } else {
                KeyboardOptions.Default
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddFoodScreenPreview() {
    AddFoodScreen(
        state = AddFoodScreenState(
            mealType = "Breakfast",
            searchQuery = "",
            currentFilter = "All",
            foods = listOf(
                FoodItem(
                    id = "1",
                    name = "Nasi Goreng",
                    servingSize = 1f,
                    servingUnit = "piring",
                    calories = 350f,
                    carbs = 50f,
                    protein = 10f,
                    fat = 12f,
                    sugar = 2f,
                    fiber = 1f,
                    sodium = 500f,
                    category = "Grain",
                    isFavorite = true
                ),
                FoodItem(
                    id = "2",
                    name = "Ayam Bakar",
                    servingSize = 1f,
                    servingUnit = "potong",
                    calories = 200f,
                    carbs = 5f,
                    protein = 30f,
                    fat = 8f,
                    sugar = 0f,
                    fiber = 0f,
                    sodium = 300f,
                    category = "Protein",
                    isFavorite = false
                )
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun AddFoodDialogPreview() {
    AddFoodDialog(
        foodName = "Nasi Goreng",
        servingUnit = "piring",
        caloriesPerServing = 350f,
        carbsPerServing = 50f,
        proteinPerServing = 10f,
        fatPerServing = 12f,
        sugarPerServing = 2f,
        fiberPerServing = 1f,
        sodiumPerServing = 500f,
        onDismiss = {},
        onAdd = {}
    )
}
