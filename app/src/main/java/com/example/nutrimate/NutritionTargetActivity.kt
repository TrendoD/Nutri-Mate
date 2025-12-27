package com.example.nutrimate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.data.User
import com.example.nutrimate.ui.nutrition.NavItem
import com.example.nutrimate.ui.nutrition.NutritionTargetScreen
import com.example.nutrimate.ui.nutrition.NutritionTargetScreenState
import kotlinx.coroutines.launch

class NutritionTargetActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private var currentUsername: String = ""
    private var currentUser: User? = null
    
    // Compose state
    private var screenState by mutableStateOf(NutritionTargetScreenState())

    // Default values based on standard nutrition recommendations
    companion object {
        const val DEFAULT_CALORIE_TARGET = 2000
        const val DEFAULT_CARBS_PERCENTAGE = 50 // 50% of calories
        const val DEFAULT_PROTEIN_PERCENTAGE = 20 // 20% of calories
        const val DEFAULT_FAT_PERCENTAGE = 30 // 30% of calories
        const val DEFAULT_SUGAR_LIMIT = 25f // WHO recommendation
        const val DEFAULT_SODIUM_LIMIT = 2300f // mg
        const val DEFAULT_SODIUM_LIMIT_HYPERTENSION = 1500f // mg for hypertension
        const val DEFAULT_FIBER_TARGET = 25f // grams
        const val DEFAULT_WATER_TARGET = 2000 // ml
        const val DEFAULT_WATER_TARGET_GLASSES = 8
        
        // Calorie per gram
        const val CALORIES_PER_GRAM_CARBS = 4
        const val CALORIES_PER_GRAM_PROTEIN = 4
        const val CALORIES_PER_GRAM_FAT = 9
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(this)

        val username = intent.getStringExtra("USERNAME")
        if (username.isNullOrEmpty()) {
            finish()
            return
        }
        currentUsername = username

        loadUserData()

        setContent {
            NutritionTargetScreen(
                state = screenState,
                onAutoCalculateChange = { isChecked ->
                    screenState = screenState.copy(autoCalculate = isChecked)
                    if (isChecked) {
                        calculateAutoTargets()
                    }
                },
                onCalorieTargetChange = { value ->
                    screenState = screenState.copy(calorieTarget = value)
                    updateMacroPercentages()
                },
                onCarbsTargetChange = { value ->
                    screenState = screenState.copy(carbsTarget = value)
                    updateMacroPercentages()
                },
                onProteinTargetChange = { value ->
                    screenState = screenState.copy(proteinTarget = value)
                    updateMacroPercentages()
                },
                onFatTargetChange = { value ->
                    screenState = screenState.copy(fatTarget = value)
                    updateMacroPercentages()
                },
                onSugarLimitChange = { value ->
                    screenState = screenState.copy(sugarLimit = value)
                },
                onSodiumLimitChange = { value ->
                    screenState = screenState.copy(sodiumLimit = value)
                },
                onFiberTargetChange = { value ->
                    screenState = screenState.copy(fiberTarget = value)
                },
                onWaterTargetChange = { value ->
                    screenState = screenState.copy(waterTarget = value)
                    updateWaterGlasses()
                },
                onResetDefaults = {
                    resetToDefaults()
                },
                onSaveTargets = {
                    saveTargets()
                },
                onNavItemClick = { navItem ->
                    handleNavigation(navItem)
                }
            )
        }
    }

    private fun handleNavigation(navItem: NavItem) {
        when (navItem) {
            NavItem.HOME -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USERNAME", currentUsername)
                startActivity(intent)
                finish()
            }
            NavItem.FOOD_LOG -> {
                val intent = Intent(this, FoodLogActivity::class.java)
                intent.putExtra("USERNAME", currentUsername)
                startActivity(intent)
                finish()
            }
            NavItem.STATS -> {
                val intent = Intent(this, StatisticsActivity::class.java)
                intent.putExtra("USERNAME", currentUsername)
                startActivity(intent)
                finish()
            }
            NavItem.SETTINGS -> {
                val intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra("USERNAME", currentUsername)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val user = database.userDao().getUserByUsername(currentUsername)
            if (user == null) {
                finish()
                return@launch
            }
            currentUser = user
            populateFields(user)
            updateRecommendationsText(user)
        }
    }

    private fun populateFields(user: User) {
        var newState = screenState.copy()
        
        // Calorie Target
        if (user.dailyCalorieTarget > 0) {
            newState = newState.copy(calorieTarget = user.dailyCalorieTarget.toString())
        }

        // Macros
        if (user.carbsTarget > 0) {
            newState = newState.copy(carbsTarget = user.carbsTarget.toInt().toString())
        }
        if (user.proteinTarget > 0) {
            newState = newState.copy(proteinTarget = user.proteinTarget.toInt().toString())
        }
        if (user.fatTarget > 0) {
            newState = newState.copy(fatTarget = user.fatTarget.toInt().toString())
        }

        // Limits
        if (user.sugarLimit > 0) {
            newState = newState.copy(sugarLimit = user.sugarLimit.toInt().toString())
        }
        if (user.sodiumLimit > 0) {
            newState = newState.copy(sodiumLimit = user.sodiumLimit.toInt().toString())
        }

        // Other targets
        if (user.fiberTarget > 0) {
            newState = newState.copy(fiberTarget = user.fiberTarget.toInt().toString())
        }
        if (user.waterTarget > 0) {
            newState = newState.copy(waterTarget = user.waterTarget.toString())
        }

        screenState = newState

        // If no targets are set, calculate defaults
        if (user.carbsTarget == 0f && user.proteinTarget == 0f && user.fatTarget == 0f) {
            if (user.dailyCalorieTarget > 0) {
                screenState = screenState.copy(autoCalculate = true)
                calculateAutoTargets()
            }
        }

        updateMacroPercentages()
        updateWaterGlasses()
    }

    private fun updateRecommendationsText(user: User) {
        var newState = screenState
        
        // Update calorie recommendation
        if (user.dailyCalorieTarget > 0) {
            newState = newState.copy(calorieRecommendation = "Berdasarkan TDEE Anda: ${user.dailyCalorieTarget} kkal")
        } else {
            newState = newState.copy(calorieRecommendation = "Lengkapi profil Anda untuk mendapatkan rekomendasi yang dipersonalisasi")
        }

        // Update sodium recommendation based on medical conditions
        val conditions = user.medicalConditions.split(",").map { it.trim() }
        if (conditions.contains("Hypertension")) {
            newState = newState.copy(sodiumRecommendation = "⚠️ Hipertensi terdeteksi: Batasi hingga <1500mg setiap hari")
        }

        // Update sugar recommendation for diabetes
        if (conditions.contains("Diabetes")) {
            newState = newState.copy(sugarRecommendation = "⚠️ Diabetes terdeteksi: Batasi asupan gula secara ketat")
        }
        
        screenState = newState
    }

    private fun calculateAutoTargets() {
        val user = currentUser ?: return
        val calorieTarget = if (user.dailyCalorieTarget > 0) user.dailyCalorieTarget else DEFAULT_CALORIE_TARGET

        // Calculate macros based on standard distribution
        val carbsGrams = (calorieTarget * DEFAULT_CARBS_PERCENTAGE / 100) / CALORIES_PER_GRAM_CARBS
        val proteinGrams = (calorieTarget * DEFAULT_PROTEIN_PERCENTAGE / 100) / CALORIES_PER_GRAM_PROTEIN
        val fatGrams = (calorieTarget * DEFAULT_FAT_PERCENTAGE / 100) / CALORIES_PER_GRAM_FAT

        // Adjust based on diet goal
        val adjustedValues = when (user.dietGoal) {
            "Lose Weight" -> {
                // Higher protein for satiety, lower carbs
                Triple(
                    (calorieTarget * 40 / 100) / CALORIES_PER_GRAM_CARBS,
                    (calorieTarget * 30 / 100) / CALORIES_PER_GRAM_PROTEIN,
                    (calorieTarget * 30 / 100) / CALORIES_PER_GRAM_FAT
                )
            }
            "Gain Weight" -> {
                // Higher carbs for energy, higher protein for muscle
                Triple(
                    (calorieTarget * 50 / 100) / CALORIES_PER_GRAM_CARBS,
                    (calorieTarget * 25 / 100) / CALORIES_PER_GRAM_PROTEIN,
                    (calorieTarget * 25 / 100) / CALORIES_PER_GRAM_FAT
                )
            }
            else -> Triple(carbsGrams, proteinGrams, fatGrams)
        }

        // Determine sugar and sodium limits based on medical conditions
        val conditions = user.medicalConditions.split(",").map { it.trim() }
        
        val sugarLimit = if (conditions.contains("Diabetes")) {
            15f // More restrictive for diabetes
        } else {
            DEFAULT_SUGAR_LIMIT
        }

        val sodiumLimit = if (conditions.contains("Hypertension")) {
            DEFAULT_SODIUM_LIMIT_HYPERTENSION
        } else {
            DEFAULT_SODIUM_LIMIT
        }

        screenState = screenState.copy(
            calorieTarget = calorieTarget.toString(),
            carbsTarget = adjustedValues.first.toString(),
            proteinTarget = adjustedValues.second.toString(),
            fatTarget = adjustedValues.third.toString(),
            sugarLimit = sugarLimit.toInt().toString(),
            sodiumLimit = sodiumLimit.toInt().toString(),
            fiberTarget = DEFAULT_FIBER_TARGET.toInt().toString(),
            waterTarget = DEFAULT_WATER_TARGET.toString()
        )
        
        updateMacroPercentages()
        updateWaterGlasses()
    }

    private fun updateMacroPercentages() {
        val calorieTarget = screenState.calorieTarget.toIntOrNull() ?: 0
        val carbsGrams = screenState.carbsTarget.toFloatOrNull() ?: 0f
        val proteinGrams = screenState.proteinTarget.toFloatOrNull() ?: 0f
        val fatGrams = screenState.fatTarget.toFloatOrNull() ?: 0f

        if (calorieTarget > 0) {
            val carbsCalories = carbsGrams * CALORIES_PER_GRAM_CARBS
            val proteinCalories = proteinGrams * CALORIES_PER_GRAM_PROTEIN
            val fatCalories = fatGrams * CALORIES_PER_GRAM_FAT
            val totalMacroCalories = carbsCalories + proteinCalories + fatCalories

            if (totalMacroCalories > 0) {
                val carbsPercent = (carbsCalories / totalMacroCalories * 100).toInt()
                val proteinPercent = (proteinCalories / totalMacroCalories * 100).toInt()
                val fatPercent = (fatCalories / totalMacroCalories * 100).toInt()

                screenState = screenState.copy(
                    carbsPercentage = carbsPercent,
                    proteinPercentage = proteinPercent,
                    fatPercentage = fatPercent
                )
            }
        }
    }

    private fun updateWaterGlasses() {
        val waterMl = screenState.waterTarget.toIntOrNull() ?: 0
        val glasses = (waterMl / 250f).toInt() // 250ml per glass
        screenState = screenState.copy(waterGlasses = glasses)
    }

    private fun resetToDefaults() {
        screenState = screenState.copy(autoCalculate = true)
        calculateAutoTargets()
        Toast.makeText(this, "Target diatur ulang ke default", Toast.LENGTH_SHORT).show()
    }

    private fun saveTargets() {
        // Validate inputs
        val calorieTarget = screenState.calorieTarget.toIntOrNull()
        val carbsTarget = screenState.carbsTarget.toFloatOrNull()
        val proteinTarget = screenState.proteinTarget.toFloatOrNull()
        val fatTarget = screenState.fatTarget.toFloatOrNull()
        val sugarLimit = screenState.sugarLimit.toFloatOrNull()
        val sodiumLimit = screenState.sodiumLimit.toFloatOrNull()
        val fiberTarget = screenState.fiberTarget.toFloatOrNull()
        val waterTarget = screenState.waterTarget.toIntOrNull()

        if (calorieTarget == null || calorieTarget < 1000) {
            Toast.makeText(this, "Harap masukkan target kalori yang valid (minimal 1000)", Toast.LENGTH_SHORT).show()
            return
        }

        if (carbsTarget == null || proteinTarget == null || fatTarget == null) {
            Toast.makeText(this, "Harap masukkan target makronutrisi yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate macro percentages add up reasonably
        val carbsCalories = carbsTarget * CALORIES_PER_GRAM_CARBS
        val proteinCalories = proteinTarget * CALORIES_PER_GRAM_PROTEIN
        val fatCalories = fatTarget * CALORIES_PER_GRAM_FAT
        val totalMacroCalories = carbsCalories + proteinCalories + fatCalories

        // Allow some variance (80-120% of target)
        if (totalMacroCalories < calorieTarget * 0.8 || totalMacroCalories > calorieTarget * 1.2) {
            AlertDialog.Builder(this)
                .setTitle("Peringatan Makro")
                .setMessage("Target makronutrisi Anda (${totalMacroCalories.toInt()} kkal) tidak sesuai dengan target kalori Anda ($calorieTarget kkal). Apakah Anda ingin tetap menyimpan?")
                .setPositiveButton("Simpan Saja") { _, _ ->
                    performSave(calorieTarget, carbsTarget, proteinTarget, fatTarget, 
                               sugarLimit ?: DEFAULT_SUGAR_LIMIT, 
                               sodiumLimit ?: DEFAULT_SODIUM_LIMIT,
                               fiberTarget ?: DEFAULT_FIBER_TARGET,
                               waterTarget ?: DEFAULT_WATER_TARGET)
                }
                .setNegativeButton("Sesuaikan", null)
                .show()
            return
        }

        performSave(calorieTarget, carbsTarget, proteinTarget, fatTarget,
                   sugarLimit ?: DEFAULT_SUGAR_LIMIT,
                   sodiumLimit ?: DEFAULT_SODIUM_LIMIT,
                   fiberTarget ?: DEFAULT_FIBER_TARGET,
                   waterTarget ?: DEFAULT_WATER_TARGET)
    }

    private fun performSave(calorieTarget: Int, carbsTarget: Float, proteinTarget: Float, 
                           fatTarget: Float, sugarLimit: Float, sodiumLimit: Float,
                           fiberTarget: Float, waterTarget: Int) {
        lifecycleScope.launch {
            database.userDao().updateAllNutritionTargets(
                currentUsername,
                calorieTarget,
                carbsTarget,
                proteinTarget,
                fatTarget,
                sugarLimit,
                sodiumLimit,
                fiberTarget,
                waterTarget
            )

            runOnUiThread {
                Toast.makeText(this@NutritionTargetActivity, 
                    "Target nutrisi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                
                // Navigate back to main activity
                val intent = Intent(this@NutritionTargetActivity, MainActivity::class.java)
                intent.putExtra("USERNAME", currentUsername)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}