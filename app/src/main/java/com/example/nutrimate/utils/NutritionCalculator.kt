package com.example.nutrimate.utils

import com.example.nutrimate.data.User

object NutritionCalculator {

    data class NutritionResult(
        val dailyCalories: Int,
        val carbsTarget: Float,
        val proteinTarget: Float,
        val fatTarget: Float,
        val sugarLimit: Float,
        val sodiumLimit: Float,
        val fiberTarget: Float
    )

    fun calculateNutrition(
        weight: Float,
        height: Float,
        age: Int,
        gender: String,
        activityLevel: String,
        goal: String,
        conditions: List<String>
    ): NutritionResult {

        // 1. Calculate BMR (Mifflin-St Jeor)
        var bmr = (10 * weight) + (6.25 * height) - (5 * age)
        if (gender.equals("Male", ignoreCase = true)) {
            bmr += 5
        } else {
            bmr -= 161
        }

        // 2. Activity Multiplier
        val activityMultiplier = when {
            activityLevel.startsWith("Sedenter", ignoreCase = true) || activityLevel.startsWith("Sedentary", ignoreCase = true) -> 1.2
            activityLevel.startsWith("Sedikit", ignoreCase = true) || activityLevel.startsWith("Light", ignoreCase = true) -> 1.375
            activityLevel.startsWith("Cukup", ignoreCase = true) || activityLevel.startsWith("Moderate", ignoreCase = true) -> 1.55
            activityLevel.startsWith("Aktif", ignoreCase = true) || activityLevel.startsWith("Active", ignoreCase = true) -> 1.725
            activityLevel.startsWith("Sangat", ignoreCase = true) || activityLevel.startsWith("Very", ignoreCase = true) -> 1.9
            else -> 1.2
        }

        val tdee = bmr * activityMultiplier

        // 3. Determine Health Logic Category
        // BMI for check
        val heightM = height / 100
        val bmi = if (heightM > 0) weight / (heightM * heightM) else 0f

        val isDiabetes = conditions.any { it.contains("Diabetes", ignoreCase = true) }
        val isObesity = conditions.any { it.contains("Obesitas", ignoreCase = true) } || bmi >= 25

        // Priority: Diabetes rules override Obesity rules if both present (based on safety for blood sugar)
        // Or we can combine? The plan treats them as distinct columns. 
        // Let's prioritize Diabetes for carb control, but Obesity for calorie deficit if overlapping.
        // For simplicity and safety, let's categorize strictness: Diabetes > Obesity > Normal.
        
        val category = when {
            isDiabetes -> "Diabetes"
            isObesity -> "Obesity"
            else -> "Normal"
        }

        // 4. Calculate Calorie Target
        var targetCalories = tdee

        when (goal) {
            "Lose Weight" -> {
                val deficit = when (category) {
                    "Obesity" -> 750.0 // Max deficit for obesity
                    else -> 500.0 // Standard deficit
                }
                targetCalories -= deficit
            }
            "Gain Weight" -> targetCalories += 500
            // Maintain: no change
        }

        // 5. Apply Safety Limits (Min Calories)
        val minCalories = if (gender.equals("Male", ignoreCase = true)) 1500.0 else 1200.0
        if (targetCalories < minCalories) targetCalories = minCalories

        // Round to nearest integer
        val finalCalories = targetCalories.toInt()

        // 6. Calculate Macros & Limits based on Category
        // Grams = (Calories * Percentage) / CaloriesPerGram
        // Carbs: 4 cal/g, Protein: 4 cal/g, Fat: 9 cal/g

        val (carbsPct, proteinPct, fatPct, sugarMax, fiberMin, satFatMaxPct) = when (category) {
            "Diabetes" -> {
                // Low Carb, Moderate Protein, Mod Fat
                // Carbs 45%, Protein 20%, Fat 30% (Total 95%? Let's balance to 100%)
                // Plan: Carbs 45%, Protein 15-20%, Fat 30%. Let's do 45/25/30.
                NutritionConfig(0.45f, 0.25f, 0.30f, 25f, 30f, 0.10f)
            }
            "Obesity" -> {
                // Balanced but lower fat
                // Plan: Carbs 45-50% (use 50), Protein 20-25% (use 25), Fat 25%.
                // 50+25+25 = 100%.
                NutritionConfig(0.50f, 0.25f, 0.25f, 25f, 30f, 0.07f)
            }
            else -> { // Normal
                // Plan: Carbs 55-60% (use 55), Protein 15%, Fat 30%.
                // 55+15+30 = 100%.
                NutritionConfig(0.55f, 0.15f, 0.30f, 50f, 25f, 0.10f)
            }
        }

        val carbsGrams = (finalCalories * carbsPct / 4).toFloat()
        val proteinGrams = (finalCalories * proteinPct / 4).toFloat()
        val fatGrams = (finalCalories * fatPct / 9).toFloat()
        
        // Sodium limit (Standard: 2300mg, Obesity/Diabetes: 2000mg)
        val sodiumMax = if (category == "Normal") 2300f else 2000f

        return NutritionResult(
            dailyCalories = finalCalories,
            carbsTarget = carbsGrams,
            proteinTarget = proteinGrams,
            fatTarget = fatGrams,
            sugarLimit = sugarMax,
            sodiumLimit = sodiumMax,
            fiberTarget = fiberMin
        )
    }

    private data class NutritionConfig(
        val carbsPct: Float,
        val proteinPct: Float,
        val fatPct: Float,
        val sugarMax: Float,
        val fiberMin: Float,
        val satFatMaxPct: Float
    )
}
