package com.example.nutrimate.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrimate.ui.components.NutriMatePrimaryButton
import com.example.nutrimate.ui.theme.GrayDark
import com.example.nutrimate.ui.theme.GrayLight
import com.example.nutrimate.ui.theme.GreenPrimary
import com.example.nutrimate.ui.theme.PageBackground

/**
 * Halaman 1: Pilih Gender - dengan icon dan tombol Lanjut
 */
@Composable
fun GenderScreen(
    onGenderSelected: (String) -> Unit
) {
    var selectedGender by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(24.dp)
    ) {
        OnboardingTitle()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OnboardingSubtitle(text = "Apa jenis kelamin Anda?")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OnboardingGenderCard(
                text = "Pria",
                icon = "♂",
                isSelected = selectedGender == "Male",
                onClick = { selectedGender = "Male" }
            )
            
            OnboardingGenderCard(
                text = "Wanita",
                icon = "♀",
                isSelected = selectedGender == "Female",
                onClick = { selectedGender = "Female" }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        NutriMatePrimaryButton(
            text = "Lanjut",
            onClick = { selectedGender?.let { onGenderSelected(it) } },
            enabled = selectedGender != null
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Halaman 2: Pilih Usia - Scrollable wheel picker
 */
@Composable
fun AgeScreen(
    onAgeSelected: (Int) -> Unit
) {
    val ageRange = 10..100
    val initialAge = 20
    val itemHeightPx = 80  // Increased from 60 to accommodate 72sp font
    val itemHeight = itemHeightPx.dp
    val visibleItems = 5
    
    // Initial position: we want 'initialAge' to be at center
    // With contentPadding, firstVisibleItemIndex = the item at the TOP of visible area
    // But contentPadding pushes items down so center aligns with firstVisibleItemIndex
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialAge - ageRange.first)
    
    // With contentPadding of 2 items, the center item = firstVisibleItemIndex 
    // (because padding creates space so first visible item appears at center)
    val selectedAge by remember {
        derivedStateOf {
            // firstVisibleItemIndex directly maps to the centered item when using proper contentPadding
            val index = listState.firstVisibleItemIndex
            (index + ageRange.first).coerceIn(ageRange)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(24.dp)
    ) {
        OnboardingTitle()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OnboardingSubtitle(text = "Berapa usia Anda?")
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Scrollable wheel picker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val totalHeight = itemHeight * visibleItems
            
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                flingBehavior = rememberSnapFlingBehavior(listState),
                // ContentPadding creates 2 item heights of padding at top and bottom
                // This means when firstVisibleItemIndex = 0, that item appears at the CENTER
                contentPadding = PaddingValues(vertical = itemHeight * 2)
            ) {
                items(ageRange.count()) { index ->
                    val age = ageRange.first + index
                    val distanceFromCenter = kotlin.math.abs(age - selectedAge)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        WheelPickerItem(
                            value = age,
                            isSelected = distanceFromCenter == 0,
                            distanceFromCenter = distanceFromCenter.coerceAtMost(2)
                        )
                    }
                }
            }
        }
        
        NutriMatePrimaryButton(
            text = "Lanjut",
            onClick = { onAgeSelected(selectedAge) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}




/**
 * Halaman 3: Pilih Tinggi Badan - Vertical slider dengan card
 */
@Composable
fun HeightScreen(
    onHeightSelected: (Int) -> Unit
) {
    var height by remember { mutableFloatStateOf(163f) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(24.dp)
    ) {
        OnboardingTitle()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Note: Using same question as mockup shows "Berapa berat badan Anda?" but this is height screen
        OnboardingSubtitle(text = "Berapa tinggi badan Anda?")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Height display with vertical slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Value card on the left
            ValueDisplayCard(
                value = height.toInt().toString(),
                unit = "cm"
            )
            
            Spacer(modifier = Modifier.width(32.dp))
            
            // Vertical slider on the right
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "250",
                    fontSize = 14.sp,
                    color = GrayDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Custom vertical slider
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .width(40.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                // Drag up = increase height, drag down = decrease height
                                val sensitivity = 0.5f
                                val delta = -dragAmount.y * sensitivity
                                height = (height + delta).coerceIn(100f, 250f)
                            }
                        }
                ) {
                    // Track background
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(8.dp)
                            .height(200.dp)
                            .background(GrayLight, RoundedCornerShape(4.dp))
                    )
                    
                    // Active track
                    val progress = (height - 100f) / 150f
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .width(8.dp)
                            .height((200 * progress).dp)
                            .background(GreenPrimary, RoundedCornerShape(4.dp))
                    )
                    
                    // Thumb
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = ((200 * progress) - 12).coerceAtLeast(0f).dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .height(24.dp)
                            .width(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "100",
                    fontSize = 14.sp,
                    color = GrayDark
                )
            }
        }
        
        NutriMatePrimaryButton(
            text = "Lanjut",
            onClick = { onHeightSelected(height.toInt()) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Halaman 4: Pilih Berat Badan - Horizontal slider dengan card
 */
@Composable
fun WeightScreen(
    onWeightSelected: (Float) -> Unit
) {
    var weight by remember { mutableFloatStateOf(60f) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(24.dp)
    ) {
        OnboardingTitle()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OnboardingSubtitle(text = "Berapa berat badan Anda?")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Value card centered
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ValueDisplayCard(
                value = weight.toInt().toString(),
                unit = "kg"
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Horizontal slider with labels
        OnboardingHorizontalSlider(
            value = weight,
            onValueChange = { weight = it },
            valueFrom = 30f,
            valueTo = 150f
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        NutriMatePrimaryButton(
            text = "Lanjut",
            onClick = { onWeightSelected(weight) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Halaman 5: Pilih Activity Level
 */
@Composable
fun ActivityLevelScreen(
    onActivityLevelSelected: (String) -> Unit
) {
    val options = listOf(
        "Sedenter" to "Sedikit atau tidak ada olahraga",
        "Sedikit Aktif" to "Olahraga ringan 1-3 hari/minggu",
        "Cukup Aktif" to "Olahraga sedang 3-5 hari/minggu",
        "Aktif" to "Olahraga berat 6-7 hari/minggu",
        "Sangat Aktif" to "Olahraga sangat berat & pekerjaan fisik"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        OnboardingTitle()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OnboardingSubtitle(text = "Seberapa aktif Anda?")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        options.forEach { (title, description) ->
            OnboardingOptionCard(
                title = title,
                description = description,
                onClick = { onActivityLevelSelected(title) }
            )
        }
    }
}

/**
 * Halaman 6: Pilih Goal
 */
@Composable
fun GoalScreen(
    onGoalSelected: (String) -> Unit
) {
    val options = listOf(
        Triple("Turunkan Berat Badan", "Defisit kalori untuk membakar lemak", "Lose Weight"),
        Triple("Jaga Berat Badan", "Kalori seimbang untuk stabilitas", "Maintain"),
        Triple("Naikkan Berat Badan", "Surplus kalori untuk membangun otot", "Gain Weight")
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        OnboardingTitle()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OnboardingSubtitle(text = "Apa tujuan Anda?")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        options.forEach { (title, description, goalKey) ->
            OnboardingOptionCard(
                title = title,
                description = description,
                onClick = { onGoalSelected(goalKey) }
            )
        }
    }
}

/**
 * Halaman 7: Pilih Kondisi Kesehatan
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthScreen(
    onHealthSubmitted: (Set<String>, String) -> Unit
) {
    val conditions = listOf("Diabetes", "Hypertension", "Cholesterol", "Gastritis")
    val conditionLabels = mapOf(
        "Diabetes" to "Diabetes",
        "Hypertension" to "Hipertensi",
        "Cholesterol" to "Kolesterol",
        "Gastritis" to "Maag/Gastritis"
    )
    
    val selectedConditions = remember { mutableStateListOf<String>() }
    var allergies by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        OnboardingTitle()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OnboardingSubtitle(text = "Kondisi Kesehatan")
        
        Text(
            text = "Pilih jika ada (Opsional)",
            fontSize = 14.sp,
            color = GrayDark
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            conditions.forEach { condition ->
                val isSelected = condition in selectedConditions
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedConditions.remove(condition)
                        } else {
                            selectedConditions.add(condition)
                        }
                    },
                    label = { Text(conditionLabels[condition] ?: condition) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = allergies,
            onValueChange = { allergies = it },
            label = { Text("Alergi Makanan (Contoh: Kacang, Susu)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                focusedLabelColor = GreenPrimary,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        NutriMatePrimaryButton(
            text = "Lanjut",
            onClick = { onHealthSubmitted(selectedConditions.toSet(), allergies) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Halaman 8: Summary dengan kalori harian
 */
@Composable
fun SummaryScreen(
    dailyCalories: Int,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Siap Memulai!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = GreenPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Berdasarkan profil Anda, target kalori harian Anda adalah:",
            fontSize = 16.sp,
            color = GrayDark,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%,d", dailyCalories),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = "kkal / hari",
                    fontSize = 20.sp,
                    color = GrayDark
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        NutriMatePrimaryButton(
            text = "Mulai NutriMate",
            onClick = onFinish
        )
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}
