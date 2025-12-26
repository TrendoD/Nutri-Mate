package com.example.nutrimate.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutrimate.ui.theme.GreenDark
import com.example.nutrimate.ui.theme.GreenPrimary
import com.example.nutrimate.ui.theme.GrayText
import androidx.compose.foundation.text.KeyboardOptions

// Colors specific to this screen
private val BackgroundColor = Color(0xFFF8F9FA)
private val CardBackgroundColor = Color.White
private val HeaderGradientStart = GreenPrimary
private val HeaderGradientEnd = GreenDark
private val BMIUnderweight = Color(0xFF2196F3)  // Blue
private val BMINormal = Color(0xFF4CAF50)        // Green
private val BMIOverweight = Color(0xFFFF9800)    // Orange
private val BMIObesity = Color(0xFFF44336)       // Red

data class ProfileScreenState(
    val fullName: String = "",
    val email: String = "",
    val age: String = "",
    val weight: String = "",
    val height: String = "",
    val targetWeight: String = "",
    val gender: String = "",  // "Male" or "Female"
    val activityLevelIndex: Int = 0,
    val dietGoal: String = "Maintain",  // "Lose Weight", "Maintain", "Gain Weight"
    val hasDiabetes: Boolean = false,
    val hasHypertension: Boolean = false,
    val hasCholesterol: Boolean = false,
    val hasGastritis: Boolean = false,
    val allergies: String = "",
    val profilePictureUri: String = "",
    val isUpdateMode: Boolean = false
)

val activityLevels = listOf(
    "Sedenter (sedikit atau tidak ada olahraga)",
    "Sedikit aktif (olahraga ringan 1-3 hari/minggu)",
    "Cukup aktif (olahraga sedang 3-5 hari/minggu)",
    "Aktif (olahraga berat 6-7 hari/minggu)",
    "Sangat aktif (olahraga sangat berat & pekerjaan fisik)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileScreenState,
    onBackClick: () -> Unit = {},
    onFullNameChange: (String) -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onAgeChange: (String) -> Unit = {},
    onWeightChange: (String) -> Unit = {},
    onHeightChange: (String) -> Unit = {},
    onTargetWeightChange: (String) -> Unit = {},
    onGenderChange: (String) -> Unit = {},
    onActivityLevelChange: (Int) -> Unit = {},
    onDietGoalChange: (String) -> Unit = {},
    onDiabetesChange: (Boolean) -> Unit = {},
    onHypertensionChange: (Boolean) -> Unit = {},
    onCholesterolChange: (Boolean) -> Unit = {},
    onGastritisChange: (Boolean) -> Unit = {},
    onAllergiesChange: (String) -> Unit = {},
    onProfilePictureChange: (Uri) -> Unit = {},
    onNutritionTargetsClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onProfilePictureChange(it) }
    }
    
    // Calculate BMI
    val bmi = remember(state.weight, state.height) {
        val weight = state.weight.toFloatOrNull()
        val heightCm = state.height.toFloatOrNull()
        if (weight != null && heightCm != null && heightCm > 0) {
            val heightM = heightCm / 100
            weight / (heightM * heightM)
        } else null
    }
    
    val bmiStatus = remember(bmi) {
        when {
            bmi == null -> "Normal"
            bmi < 18.5 -> "Berat Badan Kurang"
            bmi < 25 -> "Berat Badan Normal"
            bmi < 30 -> "Berat Badan Lebih"
            else -> "Obesitas"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(HeaderGradientStart, HeaderGradientEnd)
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
            ) {
                // Back button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Title
                Text(
                    text = "Profil Saya",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp)
                )
            }
            
            // Profile Picture (overlapping header)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-60).dp),
                contentAlignment = Alignment.Center
            ) {
                Box {
                    // Profile image
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(8.dp, CircleShape),
                        shape = CircleShape,
                        color = Color(0xFFEEEEEE)
                    ) {
                        if (state.profilePictureUri.isNotEmpty()) {
                            AsyncImage(
                                model = Uri.parse(state.profilePictureUri),
                                contentDescription = "Foto Profil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.fullName.take(1).uppercase(),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrayText
                                )
                            }
                        }
                    }
                    
                    // Camera FAB
                    FloatingActionButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomEnd),
                        containerColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ganti Foto",
                            tint = GreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // Cards section with negative margin to account for profile pic offset
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-36).dp)
                    .padding(horizontal = 16.dp)
            ) {
                // Card 1: Personal Info
                ProfileCard(title = "Info Pribadi") {
                    ProfileTextField(
                        value = state.fullName,
                        onValueChange = onFullNameChange,
                        label = "Nama Lengkap"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ProfileTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        label = "Email",
                        keyboardType = KeyboardType.Email
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Card 2: Body Stats
                ProfileCard(title = "Statistik Tubuh") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileTextField(
                            value = state.age,
                            onValueChange = onAgeChange,
                            label = "Usia",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        
                        ProfileTextField(
                            value = state.weight,
                            onValueChange = onWeightChange,
                            label = "Berat (kg)",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                        
                        ProfileTextField(
                            value = state.height,
                            onValueChange = onHeightChange,
                            label = "Tinggi (cm)",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // BMI Section
                    BMISection(bmi = bmi, bmiStatus = bmiStatus)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Card 3: Goals & Activity
                ProfileCard(title = "Target & Aktivitas") {
                    // Gender selection
                    Text(
                        text = "Jenis Kelamin",
                        fontSize = 12.sp,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    GenderSelector(
                        selectedGender = state.gender,
                        onGenderSelected = onGenderChange
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Diet Goal
                    Text(
                        text = "Tujuan",
                        fontSize = 12.sp,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    DietGoalSelector(
                        selectedGoal = state.dietGoal,
                        onGoalSelected = onDietGoalChange
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ProfileTextField(
                        value = state.targetWeight,
                        onValueChange = onTargetWeightChange,
                        label = "Target Berat (kg)",
                        keyboardType = KeyboardType.Decimal
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Tingkat Aktivitas",
                        fontSize = 12.sp,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    ActivityLevelDropdown(
                        selectedIndex = state.activityLevelIndex,
                        onActivitySelected = onActivityLevelChange
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Card 4: Medical Conditions
                ProfileCard(title = "Kesehatan") {
                    MedicalConditionCheckbox(
                        text = "Diabetes",
                        checked = state.hasDiabetes,
                        onCheckedChange = onDiabetesChange
                    )
                    
                    MedicalConditionCheckbox(
                        text = "Hipertensi",
                        checked = state.hasHypertension,
                        onCheckedChange = onHypertensionChange
                    )
                    
                    MedicalConditionCheckbox(
                        text = "Kolesterol Tinggi",
                        checked = state.hasCholesterol,
                        onCheckedChange = onCholesterolChange
                    )
                    
                    MedicalConditionCheckbox(
                        text = "Maag/GERD",
                        checked = state.hasGastritis,
                        onCheckedChange = onGastritisChange
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ProfileTextField(
                        value = state.allergies,
                        onValueChange = onAllergiesChange,
                        label = "Alergi Makanan",
                        singleLine = false,
                        minLines = 2
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Nutrition Targets Button
                OutlinedButton(
                    onClick = onNutritionTargetsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = GreenPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, GreenPrimary)
                ) {
                    Text(
                        text = "🎯 Cek Target Nutrisi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Save Button
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (state.isUpdateMode) "Perbarui Profil" else "Simpan Profil",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProfileCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenPrimary,
            focusedLabelColor = GreenPrimary,
            cursorColor = GreenPrimary
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines
    )
}

@Composable
private fun BMISection(
    bmi: Float?,
    bmiStatus: String
) {
    var containerWidth by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    
    // Calculate marker position
    val markerPosition = remember(bmi, containerWidth) {
        if (bmi != null && containerWidth > 0f) {
            val minBMI = 15f
            val maxBMI = 40f
            val percentage = ((bmi - minBMI).coerceIn(0f, maxBMI - minBMI)) / (maxBMI - minBMI)
            (percentage * containerWidth)
        } else {
            // Default to middle (BMI 22) when no data
            val defaultBMI = 22f
            val percentage = ((defaultBMI - 15f) / 25f)
            (percentage * containerWidth)
        }
    }
    
    val animatedMarkerPosition by animateDpAsState(
        targetValue = with(density) { markerPosition.toDp() },
        animationSpec = tween(durationMillis = 300),
        label = "bmi_marker"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BMI",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = if (bmi != null) String.format("%.1f", bmi) else "--",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Text(
                text = "($bmiStatus)",
                fontSize = 12.sp,
                color = GrayText,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // BMI Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .onGloballyPositioned { coordinates ->
                        containerWidth = coordinates.size.width.toFloat()
                    }
            ) {
                // Gradient bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    BMIUnderweight,
                                    BMINormal,
                                    BMIOverweight,
                                    BMIObesity
                                )
                            )
                        )
                )
                
                // Marker
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = animatedMarkerPosition - 12.dp)
                        .align(Alignment.CenterStart)
                        .background(Color.Black, CircleShape)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White, CircleShape)
                    )
                }
            }
            
            // Scale labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "15", fontSize = 10.sp, color = GrayText)
                Text(text = "40", fontSize = 10.sp, color = GrayText)
            }
        }
    }
}

@Composable
private fun GenderSelector(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ChipOption(
            text = "Pria",
            selected = selectedGender == "Male",
            onClick = { onGenderSelected("Male") },
            modifier = Modifier.weight(1f)
        )
        
        ChipOption(
            text = "Wanita",
            selected = selectedGender == "Female",
            onClick = { onGenderSelected("Female") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DietGoalSelector(
    selectedGoal: String,
    onGoalSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChipOption(
            text = "Turunkan Berat Badan",
            selected = selectedGoal == "Lose Weight",
            onClick = { onGoalSelected("Lose Weight") }
        )
        
        ChipOption(
            text = "Pertahankan Berat",
            selected = selectedGoal == "Maintain",
            onClick = { onGoalSelected("Maintain") }
        )
        
        ChipOption(
            text = "Naikkan Berat Badan",
            selected = selectedGoal == "Gain Weight",
            onClick = { onGoalSelected("Gain Weight") }
        )
    }
}

@Composable
private fun ChipOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) GreenPrimary else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) GreenPrimary else Color(0xFFE0E0E0)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (selected) Color.White else Color.Black,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityLevelDropdown(
    selectedIndex: Int,
    onActivitySelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF5F5F5),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activityLevels.getOrElse(selectedIndex) { activityLevels[0] },
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            activityLevels.forEachIndexed { index, level ->
                DropdownMenuItem(
                    text = { Text(level) },
                    onClick = {
                        onActivitySelected(index)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun MedicalConditionCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = GreenPrimary,
                checkmarkColor = Color.White
            )
        )
        
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        state = ProfileScreenState(
            fullName = "Trendo",
            email = "trendo@example.com",
            age = "25",
            weight = "70",
            height = "175",
            targetWeight = "68",
            gender = "Male",
            activityLevelIndex = 2,
            dietGoal = "Lose Weight",
            hasDiabetes = false,
            hasHypertension = true,
            hasCholesterol = false,
            hasGastritis = false,
            allergies = "Seafood",
            isUpdateMode = true
        )
    )
}
