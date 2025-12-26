package com.example.nutrimate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.ui.profile.ProfileScreen
import com.example.nutrimate.ui.profile.ProfileScreenState
import com.example.nutrimate.ui.profile.activityLevels
import kotlinx.coroutines.launch

class ProfileActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private var username: String = ""
    
    // State variables
    private var fullName by mutableStateOf("")
    private var email by mutableStateOf("")
    private var age by mutableStateOf("")
    private var weight by mutableStateOf("")
    private var height by mutableStateOf("")
    private var targetWeight by mutableStateOf("")
    private var gender by mutableStateOf("")
    private var activityLevelIndex by mutableIntStateOf(0)
    private var dietGoal by mutableStateOf("Maintain")
    private var hasDiabetes by mutableStateOf(false)
    private var hasHypertension by mutableStateOf(false)
    private var hasCholesterol by mutableStateOf(false)
    private var hasGastritis by mutableStateOf(false)
    private var allergies by mutableStateOf("")
    private var profilePictureUri by mutableStateOf("")
    private var isUpdateMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(this)
        username = intent.getStringExtra("USERNAME") ?: ""

        if (username.isEmpty()) {
            Toast.makeText(this, "Kesalahan: Pengguna tidak teridentifikasi", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserData()

        setContent {
            ProfileScreen(
                state = ProfileScreenState(
                    fullName = fullName,
                    email = email,
                    age = age,
                    weight = weight,
                    height = height,
                    targetWeight = targetWeight,
                    gender = gender,
                    activityLevelIndex = activityLevelIndex,
                    dietGoal = dietGoal,
                    hasDiabetes = hasDiabetes,
                    hasHypertension = hasHypertension,
                    hasCholesterol = hasCholesterol,
                    hasGastritis = hasGastritis,
                    allergies = allergies,
                    profilePictureUri = profilePictureUri,
                    isUpdateMode = isUpdateMode
                ),
                onBackClick = { finish() },
                onFullNameChange = { fullName = it },
                onEmailChange = { email = it },
                onAgeChange = { age = it },
                onWeightChange = { weight = it },
                onHeightChange = { height = it },
                onTargetWeightChange = { targetWeight = it },
                onGenderChange = { gender = it },
                onActivityLevelChange = { activityLevelIndex = it },
                onDietGoalChange = { dietGoal = it },
                onDiabetesChange = { hasDiabetes = it },
                onHypertensionChange = { hasHypertension = it },
                onCholesterolChange = { hasCholesterol = it },
                onGastritisChange = { hasGastritis = it },
                onAllergiesChange = { allergies = it },
                onProfilePictureChange = { uri ->
                    profilePictureUri = uri.toString()
                },
                onNutritionTargetsClick = {
                    val intent = Intent(this, NutritionTargetActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                },
                onSaveClick = { saveProfile() }
            )
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val user = database.userDao().getUserByUsername(username)
            if (user != null) {
                fullName = user.fullName
                email = user.email
                if (user.age > 0) age = user.age.toString()
                if (user.weight > 0) weight = user.weight.toString()
                if (user.height > 0) height = user.height.toString()
                if (user.targetWeight > 0) targetWeight = user.targetWeight.toString()

                // Gender
                gender = user.gender

                // Activity Level
                val levelIndex = activityLevels.indexOfFirst { it.startsWith(user.activityLevel) }
                if (levelIndex >= 0) {
                    activityLevelIndex = levelIndex
                }

                // Diet Goal
                dietGoal = user.dietGoal.ifEmpty { "Maintain" }

                // Conditions
                val conditions = user.medicalConditions.split(",")
                hasDiabetes = conditions.contains("Diabetes")
                hasHypertension = conditions.contains("Hypertension")
                hasCholesterol = conditions.contains("Cholesterol")
                hasGastritis = conditions.contains("Gastritis")

                // Allergies
                allergies = user.allergies

                // Profile Picture
                if (user.profilePicture.isNotEmpty()) {
                    profilePictureUri = user.profilePicture
                }

                isUpdateMode = true
            }
        }
    }

    private fun saveProfile() {
        if (fullName.isEmpty() || email.isEmpty() || age.isEmpty() || weight.isEmpty() || height.isEmpty()) {
            Toast.makeText(this, "Harap isi semua kolom yang wajib", Toast.LENGTH_SHORT).show()
            return
        }

        val ageInt = age.toIntOrNull()
        val weightFloat = weight.toFloatOrNull()
        val heightFloat = height.toFloatOrNull()
        val targetWeightFloat = targetWeight.toFloatOrNull() ?: 0f

        if (ageInt == null || weightFloat == null || heightFloat == null) {
            Toast.makeText(this, "Format angka tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        if (gender.isEmpty()) {
            Toast.makeText(this, "Harap pilih jenis kelamin", Toast.LENGTH_SHORT).show()
            return
        }

        // Activity Level
        val selectedActivity = activityLevels[activityLevelIndex]
        val activityLevelKey = selectedActivity.split(" ")[0]

        // Collect conditions
        val conditions = mutableListOf<String>()
        if (hasDiabetes) conditions.add("Diabetes")
        if (hasHypertension) conditions.add("Hypertension")
        if (hasCholesterol) conditions.add("Cholesterol")
        if (hasGastritis) conditions.add("Gastritis")
        val conditionsString = conditions.joinToString(",")

        // Use centralized NutritionCalculator
        val result = com.example.nutrimate.utils.NutritionCalculator.calculateNutrition(
            weight = weightFloat,
            height = heightFloat,
            age = ageInt,
            gender = gender,
            activityLevel = selectedActivity,
            goal = dietGoal,
            conditions = conditions
        )

        val tdee = result.dailyCalories

        lifecycleScope.launch {
            database.userDao().updateFullProfile(
                username,
                ageInt,
                weightFloat,
                heightFloat,
                gender,
                conditionsString,
                tdee,
                activityLevelKey,
                dietGoal,
                targetWeightFloat,
                allergies,
                profilePictureUri,
                fullName,
                email,
                result.carbsTarget,
                result.proteinTarget,
                result.fatTarget,
                result.sugarLimit,
                result.sodiumLimit,
                result.fiberTarget
            )

            Toast.makeText(this@ProfileActivity, "Profil diperbarui! Target Harian: $tdee kkal", Toast.LENGTH_LONG).show()

            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
            intent.putExtra("USERNAME", username)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}