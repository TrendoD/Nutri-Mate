package com.example.nutrimate

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.data.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class NutritionTargetActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var currentUsername: String = ""
    private var currentUser: User? = null

    // Views
    private lateinit var switchAutoCalculate: SwitchCompat
    private lateinit var etCalorieTarget: EditText
    private lateinit var tvCalorieRecommendation: TextView
    private lateinit var etCarbsTarget: EditText
    private lateinit var etProteinTarget: EditText
    private lateinit var etFatTarget: EditText
    private lateinit var tvCarbsPercentage: TextView
    private lateinit var tvProteinPercentage: TextView
    private lateinit var tvFatPercentage: TextView
    private lateinit var viewCarbsBar: View
    private lateinit var viewProteinBar: View
    private lateinit var viewFatBar: View
    private lateinit var etSugarLimit: EditText
    private lateinit var etSodiumLimit: EditText
    private lateinit var tvSugarRecommendation: TextView
    private lateinit var tvSodiumRecommendation: TextView
    private lateinit var etFiberTarget: EditText
    private lateinit var etWaterTarget: EditText
    private lateinit var tvWaterGlasses: TextView
    private lateinit var btnResetDefaults: Button
    private lateinit var btnSaveTargets: Button
    private lateinit var bottomNavigation: BottomNavigationView

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
        setContentView(R.layout.activity_nutrition_target)

        database = AppDatabase.getDatabase(this)

        val username = intent.getStringExtra("USERNAME")
        if (username.isNullOrEmpty()) {
            finish()
            return
        }
        currentUsername = username

        initViews()
        setupListeners()
        loadUserData()
    }

    private fun initViews() {
        switchAutoCalculate = findViewById(R.id.switchAutoCalculate)
        etCalorieTarget = findViewById(R.id.etCalorieTarget)
        tvCalorieRecommendation = findViewById(R.id.tvCalorieRecommendation)
        etCarbsTarget = findViewById(R.id.etCarbsTarget)
        etProteinTarget = findViewById(R.id.etProteinTarget)
        etFatTarget = findViewById(R.id.etFatTarget)
        tvCarbsPercentage = findViewById(R.id.tvCarbsPercentage)
        tvProteinPercentage = findViewById(R.id.tvProteinPercentage)
        tvFatPercentage = findViewById(R.id.tvFatPercentage)
        viewCarbsBar = findViewById(R.id.viewCarbsBar)
        viewProteinBar = findViewById(R.id.viewProteinBar)
        viewFatBar = findViewById(R.id.viewFatBar)
        etSugarLimit = findViewById(R.id.etSugarLimit)
        etSodiumLimit = findViewById(R.id.etSodiumLimit)
        tvSugarRecommendation = findViewById(R.id.tvSugarRecommendation)
        tvSodiumRecommendation = findViewById(R.id.tvSodiumRecommendation)
        etFiberTarget = findViewById(R.id.etFiberTarget)
        etWaterTarget = findViewById(R.id.etWaterTarget)
        tvWaterGlasses = findViewById(R.id.tvWaterGlasses)
        btnResetDefaults = findViewById(R.id.btnResetDefaults)
        btnSaveTargets = findViewById(R.id.btnSaveTargets)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupListeners() {
        // Auto Calculate Switch
        switchAutoCalculate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                calculateAutoTargets()
            }
            setFieldsEnabled(!isChecked)
        }

        // Text Watchers for macro calculations
        val macroTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateMacroPercentages()
            }
        }

        etCalorieTarget.addTextChangedListener(macroTextWatcher)
        etCarbsTarget.addTextChangedListener(macroTextWatcher)
        etProteinTarget.addTextChangedListener(macroTextWatcher)
        etFatTarget.addTextChangedListener(macroTextWatcher)

        // Water target text watcher
        etWaterTarget.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateWaterGlasses()
            }
        })

        // Reset Defaults Button
        btnResetDefaults.setOnClickListener {
            showResetConfirmationDialog()
        }

        // Save Targets Button
        btnSaveTargets.setOnClickListener {
            saveTargets()
        }

        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("USERNAME", currentUsername)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_food_log -> {
                    val intent = Intent(this, FoodLogActivity::class.java)
                    intent.putExtra("USERNAME", currentUsername)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_stats -> {
                    val intent = Intent(this, StatisticsActivity::class.java)
                    intent.putExtra("USERNAME", currentUsername)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
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

            runOnUiThread {
                populateFields(user)
                updateRecommendationsText(user)
            }
        }
    }

    private fun populateFields(user: User) {
        // Calorie Target
        if (user.dailyCalorieTarget > 0) {
            etCalorieTarget.setText(user.dailyCalorieTarget.toString())
        }

        // Macros
        if (user.carbsTarget > 0) {
            etCarbsTarget.setText(user.carbsTarget.toInt().toString())
        }
        if (user.proteinTarget > 0) {
            etProteinTarget.setText(user.proteinTarget.toInt().toString())
        }
        if (user.fatTarget > 0) {
            etFatTarget.setText(user.fatTarget.toInt().toString())
        }

        // Limits
        if (user.sugarLimit > 0) {
            etSugarLimit.setText(user.sugarLimit.toInt().toString())
        }
        if (user.sodiumLimit > 0) {
            etSodiumLimit.setText(user.sodiumLimit.toInt().toString())
        }

        // Other targets
        if (user.fiberTarget > 0) {
            etFiberTarget.setText(user.fiberTarget.toInt().toString())
        }
        if (user.waterTarget > 0) {
            etWaterTarget.setText(user.waterTarget.toString())
        }

        // If no targets are set, calculate defaults
        if (user.carbsTarget == 0f && user.proteinTarget == 0f && user.fatTarget == 0f) {
            if (user.dailyCalorieTarget > 0) {
                calculateAutoTargets()
                switchAutoCalculate.isChecked = true
            }
        }

        updateMacroPercentages()
        updateWaterGlasses()
    }

    private fun updateRecommendationsText(user: User) {
        // Update calorie recommendation
        if (user.dailyCalorieTarget > 0) {
            tvCalorieRecommendation.text = "Berdasarkan TDEE Anda: ${user.dailyCalorieTarget} kkal"
        } else {
            tvCalorieRecommendation.text = "Lengkapi profil Anda untuk mendapatkan rekomendasi yang dipersonalisasi"
        }

        // Update sodium recommendation based on medical conditions
        val conditions = user.medicalConditions.split(",").map { it.trim() }
        if (conditions.contains("Hypertension")) {
            tvSodiumRecommendation.text = "⚠️ Hipertensi terdeteksi: Batasi hingga <1500mg setiap hari"
        }

        // Update sugar recommendation for diabetes
        if (conditions.contains("Diabetes")) {
            tvSugarRecommendation.text = "⚠️ Diabetes terdeteksi: Batasi asupan gula secara ketat"
        }
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

        // Set values
        etCalorieTarget.setText(calorieTarget.toString())
        etCarbsTarget.setText(adjustedValues.first.toString())
        etProteinTarget.setText(adjustedValues.second.toString())
        etFatTarget.setText(adjustedValues.third.toString())

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

        etSugarLimit.setText(sugarLimit.toInt().toString())
        etSodiumLimit.setText(sodiumLimit.toInt().toString())
        etFiberTarget.setText(DEFAULT_FIBER_TARGET.toInt().toString())
        etWaterTarget.setText(DEFAULT_WATER_TARGET.toString())
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        etCarbsTarget.isEnabled = enabled
        etProteinTarget.isEnabled = enabled
        etFatTarget.isEnabled = enabled
        // Calorie target, limits, and other targets remain editable
    }

    private fun updateMacroPercentages() {
        val calorieTarget = etCalorieTarget.text.toString().toIntOrNull() ?: 0
        val carbsGrams = etCarbsTarget.text.toString().toFloatOrNull() ?: 0f
        val proteinGrams = etProteinTarget.text.toString().toFloatOrNull() ?: 0f
        val fatGrams = etFatTarget.text.toString().toFloatOrNull() ?: 0f

        if (calorieTarget > 0) {
            val carbsCalories = carbsGrams * CALORIES_PER_GRAM_CARBS
            val proteinCalories = proteinGrams * CALORIES_PER_GRAM_PROTEIN
            val fatCalories = fatGrams * CALORIES_PER_GRAM_FAT
            val totalMacroCalories = carbsCalories + proteinCalories + fatCalories

            if (totalMacroCalories > 0) {
                val carbsPercent = (carbsCalories / totalMacroCalories * 100).toInt()
                val proteinPercent = (proteinCalories / totalMacroCalories * 100).toInt()
                val fatPercent = (fatCalories / totalMacroCalories * 100).toInt()

                tvCarbsPercentage.text = "($carbsPercent%)"
                tvProteinPercentage.text = "($proteinPercent%)"
                tvFatPercentage.text = "($fatPercent%)"

                // Update visual bar weights
                updateMacroDistributionBar(carbsPercent, proteinPercent, fatPercent)
            }
        }
    }

    private fun updateMacroDistributionBar(carbs: Int, protein: Int, fat: Int) {
        val total = carbs + protein + fat
        if (total > 0) {
            val params1 = viewCarbsBar.layoutParams as LinearLayout.LayoutParams
            val params2 = viewProteinBar.layoutParams as LinearLayout.LayoutParams
            val params3 = viewFatBar.layoutParams as LinearLayout.LayoutParams

            params1.weight = carbs.toFloat()
            params2.weight = protein.toFloat()
            params3.weight = fat.toFloat()

            viewCarbsBar.layoutParams = params1
            viewProteinBar.layoutParams = params2
            viewFatBar.layoutParams = params3
        }
    }

    private fun updateWaterGlasses() {
        val waterMl = etWaterTarget.text.toString().toIntOrNull() ?: 0
        val glasses = (waterMl / 250f).toInt() // 250ml per glass
        tvWaterGlasses.text = "($glasses gelas)"
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Atur Ulang ke Default")
            .setMessage("Ini akan mengatur ulang semua target nutrisi ke nilai default yang disarankan berdasarkan profil Anda. Lanjutkan?")
            .setPositiveButton("Atur Ulang") { _, _ ->
                resetToDefaults()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun resetToDefaults() {
        switchAutoCalculate.isChecked = true
        calculateAutoTargets()
        Toast.makeText(this, "Target diatur ulang ke default", Toast.LENGTH_SHORT).show()
    }

    private fun saveTargets() {
        // Validate inputs
        val calorieTarget = etCalorieTarget.text.toString().toIntOrNull()
        val carbsTarget = etCarbsTarget.text.toString().toFloatOrNull()
        val proteinTarget = etProteinTarget.text.toString().toFloatOrNull()
        val fatTarget = etFatTarget.text.toString().toFloatOrNull()
        val sugarLimit = etSugarLimit.text.toString().toFloatOrNull()
        val sodiumLimit = etSodiumLimit.text.toString().toFloatOrNull()
        val fiberTarget = etFiberTarget.text.toString().toFloatOrNull()
        val waterTarget = etWaterTarget.text.toString().toIntOrNull()

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