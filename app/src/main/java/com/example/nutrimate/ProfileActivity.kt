package com.example.nutrimate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfilePicture: ImageView
    private lateinit var btnChangePhoto: Button
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAge: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var tvBMI: TextView
    private lateinit var tvBMIStatus: TextView
    private lateinit var etTargetWeight: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var spActivityLevel: Spinner
    private lateinit var rgDietGoal: RadioGroup
    private lateinit var cbDiabetes: CheckBox
    private lateinit var cbHypertension: CheckBox
    private lateinit var cbCholesterol: CheckBox
    private lateinit var cbGastritis: CheckBox
    private lateinit var etAllergies: EditText
    private lateinit var btnNutritionTargets: Button
    private lateinit var btnSaveProfile: Button
    
    private lateinit var database: AppDatabase
    private var username: String = ""
    private var profilePictureUri: String = ""

    private val activityLevels = arrayOf(
        "Sedentary (little or no exercise)",
        "Lightly active (light exercise 1-3 days/week)",
        "Moderately active (moderate exercise 3-5 days/week)",
        "Active (hard exercise 6-7 days/week)",
        "Very active (very hard exercise & physical job)"
    )

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            profilePictureUri = it.toString()
            ivProfilePicture.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        database = AppDatabase.getDatabase(this)
        username = intent.getStringExtra("USERNAME") ?: ""

        if (username.isEmpty()) {
            Toast.makeText(this, "Error: User not identified", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupListeners()
        loadUserData()
    }

    private fun initViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etAge = findViewById(R.id.etAge)
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        tvBMI = findViewById(R.id.tvBMI)
        tvBMIStatus = findViewById(R.id.tvBMIStatus)
        etTargetWeight = findViewById(R.id.etTargetWeight)
        rgGender = findViewById(R.id.rgGender)
        spActivityLevel = findViewById(R.id.spActivityLevel)
        rgDietGoal = findViewById(R.id.rgDietGoal)
        cbDiabetes = findViewById(R.id.cbDiabetes)
        cbHypertension = findViewById(R.id.cbHypertension)
        cbCholesterol = findViewById(R.id.cbCholesterol)
        cbGastritis = findViewById(R.id.cbGastritis)
        etAllergies = findViewById(R.id.etAllergies)
        btnNutritionTargets = findViewById(R.id.btnNutritionTargets)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)

        // Setup Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spActivityLevel.adapter = adapter
    }

    private fun setupListeners() {
        btnChangePhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        btnNutritionTargets.setOnClickListener {
            val intent = Intent(this, NutritionTargetActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateBMI()
            }
        }

        etWeight.addTextChangedListener(textWatcher)
        etHeight.addTextChangedListener(textWatcher)
    }

    private fun calculateBMI() {
        val weightStr = etWeight.text.toString()
        val heightStr = etHeight.text.toString()

        if (weightStr.isNotEmpty() && heightStr.isNotEmpty()) {
            val weight = weightStr.toFloatOrNull()
            val heightCm = heightStr.toFloatOrNull()

            if (weight != null && heightCm != null && heightCm > 0) {
                val heightM = heightCm / 100
                val bmi = weight / (heightM * heightM)
                tvBMI.text = String.format("BMI: %.1f", bmi)

                val status = when {
                    bmi < 18.5 -> "Underweight"
                    bmi < 25 -> "Normal Weight"
                    bmi < 30 -> "Overweight"
                    else -> "Obese"
                }
                tvBMIStatus.text = "($status)"
                
                // Set color based on status (optional, simple logic)
                // tvBMIStatus.setTextColor(...) 
            }
        } else {
            tvBMI.text = "BMI: --"
            tvBMIStatus.text = "(Normal)"
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val user = database.userDao().getUserByUsername(username)
            if (user != null) {
                etFullName.setText(user.fullName)
                etEmail.setText(user.email)
                if (user.age > 0) etAge.setText(user.age.toString())
                if (user.weight > 0) etWeight.setText(user.weight.toString())
                if (user.height > 0) etHeight.setText(user.height.toString())
                if (user.targetWeight > 0) etTargetWeight.setText(user.targetWeight.toString())
                
                // Gender
                if (user.gender == "Male") {
                    rgGender.check(R.id.rbMale)
                } else if (user.gender == "Female") {
                    rgGender.check(R.id.rbFemale)
                }

                // Activity Level
                val levelIndex = activityLevels.indexOfFirst { it.startsWith(user.activityLevel) }
                if (levelIndex >= 0) {
                    spActivityLevel.setSelection(levelIndex)
                }

                // Diet Goal
                when (user.dietGoal) {
                    "Lose Weight" -> rgDietGoal.check(R.id.rbLoseWeight)
                    "Maintain" -> rgDietGoal.check(R.id.rbMaintain)
                    "Gain Weight" -> rgDietGoal.check(R.id.rbGainWeight)
                    else -> rgDietGoal.check(R.id.rbMaintain) // Default
                }

                // Conditions
                val conditions = user.medicalConditions.split(",")
                cbDiabetes.isChecked = conditions.contains("Diabetes")
                cbHypertension.isChecked = conditions.contains("Hypertension")
                cbCholesterol.isChecked = conditions.contains("Cholesterol")
                cbGastritis.isChecked = conditions.contains("Gastritis")

                // Allergies
                etAllergies.setText(user.allergies)

                // Profile Picture
                if (user.profilePicture.isNotEmpty()) {
                    profilePictureUri = user.profilePicture
                    try {
                        ivProfilePicture.setImageURI(Uri.parse(user.profilePicture))
                    } catch (e: Exception) {
                        // Handle error loading image (e.g. deleted)
                        ivProfilePicture.setImageResource(R.drawable.ic_launcher_foreground)
                    }
                }
                
                // Trigger BMI calc
                calculateBMI()

                btnSaveProfile.text = "Update Profile"
            }
        }
    }

    private fun saveProfile() {
        val fullName = etFullName.text.toString()
        val email = etEmail.text.toString()
        val ageStr = etAge.text.toString()
        val weightStr = etWeight.text.toString()
        val heightStr = etHeight.text.toString()
        val targetWeightStr = etTargetWeight.text.toString()
        
        if (fullName.isEmpty() || email.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageStr.toIntOrNull()
        val weight = weightStr.toFloatOrNull()
        val height = heightStr.toFloatOrNull()
        val targetWeight = targetWeightStr.toFloatOrNull() ?: 0f

        if (age == null || weight == null || height == null) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedGenderId = rgGender.checkedRadioButtonId
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
            return
        }
        val gender = if (selectedGenderId == R.id.rbMale) "Male" else "Female"

        // Activity Level
        val selectedActivity = activityLevels[spActivityLevel.selectedItemPosition]
        // Extract just the key part if needed, but saving full string is fine for display
        // Or better, save "Sedentary", "Light", etc.
        val activityLevelKey = selectedActivity.split(" ")[0] 

        // Diet Goal
        val selectedGoalId = rgDietGoal.checkedRadioButtonId
        val dietGoal = when (selectedGoalId) {
            R.id.rbLoseWeight -> "Lose Weight"
            R.id.rbGainWeight -> "Gain Weight"
            else -> "Maintain"
        }

        // Collect conditions
        val conditions = mutableListOf<String>()
        if (cbDiabetes.isChecked) conditions.add("Diabetes")
        if (cbHypertension.isChecked) conditions.add("Hypertension")
        if (cbCholesterol.isChecked) conditions.add("Cholesterol")
        if (cbGastritis.isChecked) conditions.add("Gastritis")
        val conditionsString = conditions.joinToString(",")

        // Allergies
        val allergies = etAllergies.text.toString()

        // Calculate BMR (Mifflin-St Jeor)
        var bmr = (10 * weight) + (6.25 * height) - (5 * age)
        if (gender == "Male") {
            bmr += 5
        } else {
            bmr -= 161
        }

        // Calculate TDEE based on Activity Level
        val activityMultiplier = when (spActivityLevel.selectedItemPosition) {
            0 -> 1.2   // Sedentary
            1 -> 1.375 // Light
            2 -> 1.55  // Moderate
            3 -> 1.725 // Active
            4 -> 1.9   // Very Active
            else -> 1.2
        }
        
        var tdee = (bmr * activityMultiplier).toInt()

        // Adjust TDEE based on Goal
        // Lose: -500, Gain: +500 (Standard rule of thumb)
        when (dietGoal) {
            "Lose Weight" -> tdee -= 500
            "Gain Weight" -> tdee += 500
        }
        
        // Safety check: Don't let calories go too low
        if (tdee < 1200) tdee = 1200

        lifecycleScope.launch {
            database.userDao().updateProfile(
                username,
                age,
                weight,
                height,
                gender,
                conditionsString,
                tdee,
                activityLevelKey,
                dietGoal,
                targetWeight,
                allergies,
                profilePictureUri,
                fullName,
                email
            )

            Toast.makeText(this@ProfileActivity, "Profile updated! Daily Target: $tdee kcal", Toast.LENGTH_LONG).show()
            
            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
            intent.putExtra("USERNAME", username)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}