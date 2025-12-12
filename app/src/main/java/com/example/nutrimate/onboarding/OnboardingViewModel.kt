package com.example.nutrimate.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.nutrimate.data.AppDatabase
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    // Data holders
    val gender = MutableLiveData<String>()
    val age = MutableLiveData<Int>()
    val height = MutableLiveData<Int>() // in cm
    val weight = MutableLiveData<Float>() // in kg
    val activityLevel = MutableLiveData<String>()
    val dietGoal = MutableLiveData<String>()
    val medicalConditions = MutableLiveData<MutableSet<String>>(mutableSetOf())
    val allergies = MutableLiveData<String>("")
    
    // Calculated result
    private val _dailyCalories = MutableLiveData<Int>()
    val dailyCalories: LiveData<Int> = _dailyCalories

    fun setGender(value: String) { gender.value = value }
    fun setAge(value: Int) { age.value = value }
    fun setHeight(value: Int) { height.value = value }
    fun setWeight(value: Float) { weight.value = value }
    fun setActivityLevel(value: String) { activityLevel.value = value }
    fun setDietGoal(value: String) { dietGoal.value = value }
    
    fun toggleCondition(condition: String, isChecked: Boolean) {
        val current = medicalConditions.value ?: mutableSetOf()
        if (isChecked) {
            current.add(condition)
        } else {
            current.remove(condition)
        }
        medicalConditions.value = current
    }
    
    fun setAllergies(value: String) { allergies.value = value }

    fun calculateTDEE() {
        val w = weight.value ?: 60f
        val h = height.value?.toFloat() ?: 170f
        val a = age.value ?: 25
        val g = gender.value ?: "Male"
        val act = activityLevel.value ?: "Sedentary"
        val goal = dietGoal.value ?: "Maintain"
        val conds = medicalConditions.value?.toList() ?: emptyList()

        val result = com.example.nutrimate.utils.NutritionCalculator.calculateNutrition(
            weight = w,
            height = h,
            age = a,
            gender = g,
            activityLevel = act,
            goal = goal,
            conditions = conds
        )
        
        _dailyCalories.value = result.dailyCalories
    }

    fun saveUserData(username: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val currentConditions = medicalConditions.value?.joinToString(",") ?: ""
            
            val w = weight.value ?: 60f
            val h = height.value?.toFloat() ?: 170f
            val a = age.value ?: 25
            val g = gender.value ?: "Male"
            val act = activityLevel.value ?: "Sedentary"
            val goal = dietGoal.value ?: "Maintain"
            val conds = medicalConditions.value?.toList() ?: emptyList()

            // Recalculate to ensure we have the full result object for macros
            val result = com.example.nutrimate.utils.NutritionCalculator.calculateNutrition(
                weight = w,
                height = h,
                age = a,
                gender = g,
                activityLevel = act,
                goal = goal,
                conditions = conds
            )
            
            val user = userDao.getUserByUsername(username)
            if (user != null) {
                userDao.updateFullProfile(
                    username = username,
                    age = a,
                    weight = w,
                    height = h.toInt().toFloat(), // Ensure float format
                    gender = g,
                    conditions = currentConditions,
                    dailyCalories = result.dailyCalories,
                    activityLevel = act.split(" ")[0], // Store key
                    dietGoal = goal,
                    targetWeight = user.targetWeight,
                    allergies = allergies.value ?: "",
                    profilePicture = user.profilePicture,
                    fullName = user.fullName,
                    email = user.email,
                    carbs = result.carbsTarget,
                    protein = result.proteinTarget,
                    fat = result.fatTarget,
                    sugar = result.sugarLimit,
                    sodium = result.sodiumLimit,
                    fiber = result.fiberTarget
                )
            }
            
            onComplete()
        }
    }
}