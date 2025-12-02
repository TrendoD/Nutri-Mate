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

        // Mifflin-St Jeor Equation
        var bmr = (10 * w) + (6.25 * h) - (5 * a)
        if (g == "Male") {
            bmr += 5
        } else {
            bmr -= 161
        }

        val activityMultiplier = when {
            act.startsWith("Sedenter") -> 1.2
            act.startsWith("Sedikit") -> 1.375
            act.startsWith("Cukup") -> 1.55
            act.startsWith("Aktif") -> 1.725
            act.startsWith("Sangat") -> 1.9
            else -> 1.2
        }

        var tdee = (bmr * activityMultiplier).toInt()

        when (goal) {
            "Lose Weight" -> tdee -= 500
            "Gain Weight" -> tdee += 500
        }

        if (tdee < 1200) tdee = 1200
        
        _dailyCalories.value = tdee
    }

    fun saveUserData(username: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val currentConditions = medicalConditions.value?.joinToString(",") ?: ""
            
            val user = userDao.getUserByUsername(username)
            if (user != null) {
                userDao.updateProfile(
                    username = username,
                    age = age.value ?: 0,
                    weight = weight.value ?: 0f,
                    height = (height.value ?: 0).toFloat(),
                    gender = gender.value ?: "",
                    conditions = currentConditions,
                    calories = _dailyCalories.value ?: 2000,
                    activityLevel = activityLevel.value?.split(" ")?.get(0) ?: "Sedentary",
                    dietGoal = dietGoal.value ?: "Maintain",
                    targetWeight = user.targetWeight,
                    allergies = allergies.value ?: "",
                    profilePicture = user.profilePicture,
                    fullName = user.fullName,
                    email = user.email
                )
            }
            
            onComplete()
        }
    }
}