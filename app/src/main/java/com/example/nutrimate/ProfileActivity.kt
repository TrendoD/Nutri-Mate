package com.example.nutrimate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfilePicture: ImageView
    private lateinit var btnChangePhoto: FloatingActionButton
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAge: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var tvBMI: TextView
    private lateinit var tvBMIStatus: TextView
    private lateinit var flBMIContainer: FrameLayout
    private lateinit var ivBMIMarker: ImageView
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
    private lateinit var ivBack: ImageView
    
    private lateinit var database: AppDatabase
    private var username: String = ""
    private var profilePictureUri: String = ""

    private val activityLevels = arrayOf(
        "Sedenter (sedikit atau tidak ada olahraga)",
        "Sedikit aktif (olahraga ringan 1-3 hari/minggu)",
        "Cukup aktif (olahraga sedang 3-5 hari/minggu)",
        "Aktif (olahraga berat 6-7 hari/minggu)",
        "Sangat aktif (olahraga sangat berat & pekerjaan fisik)"
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
            Toast.makeText(this, "Kesalahan: Pengguna tidak teridentifikasi", Toast.LENGTH_SHORT).show()
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
        flBMIContainer = findViewById(R.id.flBMIContainer)
        ivBMIMarker = findViewById(R.id.ivBMIMarker)
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
        ivBack = findViewById(R.id.ivBack)

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
        
        ivBack.setOnClickListener {
            finish()
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
                    bmi < 18.5 -> "Berat Badan Kurang"
                    bmi < 25 -> "Berat Badan Normal"
                    bmi < 30 -> "Berat Badan Lebih"
                    else -> "Obesitas"
                }
                tvBMIStatus.text = "($status)"

                // Update Marker Position
                updateBMIMarker(bmi)
            }
        } else {
            tvBMI.text = "BMI: --"
            tvBMIStatus.text = "(Normal)"
            updateBMIMarker(22.0f) // Default to middle/normal
        }
    }

    private fun updateBMIMarker(bmi: Float) {
        flBMIContainer.post {
            val minBMI = 15f
            val maxBMI = 40f
            val width = flBMIContainer.width.toFloat()
            
            // Calculate percentage (0.0 to 1.0)
            val percentage = (bmi - minBMI).coerceIn(0f, maxBMI - minBMI) / (maxBMI - minBMI)
            
            // Calculate X position
            // Subtract half of marker width to center it on the value
            val markerWidth = ivBMIMarker.width.toFloat()
            val xPos = (percentage * width) - (markerWidth / 2)
            
            ivBMIMarker.animate()
                .translationX(xPos)
                .setDuration(300)
                .start()
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

                btnSaveProfile.text = "Perbarui Profil"
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
            Toast.makeText(this, "Harap isi semua kolom yang wajib", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageStr.toIntOrNull()
        val weight = weightStr.toFloatOrNull()
        val height = heightStr.toFloatOrNull()
        val targetWeight = targetWeightStr.toFloatOrNull() ?: 0f

        if (age == null || weight == null || height == null) {
            Toast.makeText(this, "Format angka tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedGenderId = rgGender.checkedRadioButtonId
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Harap pilih jenis kelamin", Toast.LENGTH_SHORT).show()
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

        // Use centralized NutritionCalculator
        val result = com.example.nutrimate.utils.NutritionCalculator.calculateNutrition(
            weight = weight,
            height = height,
            age = age,
            gender = gender,
            activityLevel = selectedActivity,
            goal = dietGoal,
            conditions = conditions
        )
        
        val tdee = result.dailyCalories

        lifecycleScope.launch {
            database.userDao().updateFullProfile(
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