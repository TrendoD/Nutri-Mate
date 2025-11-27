package com.example.nutrimate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var etAge: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var cbDiabetes: CheckBox
    private lateinit var cbHypertension: CheckBox
    private lateinit var cbCholesterol: CheckBox
    private lateinit var cbGastritis: CheckBox
    private lateinit var btnSaveProfile: Button
    private lateinit var database: AppDatabase

    private var username: String = ""

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

        // Load existing data if available
        loadUserData()

        btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun initViews() {
        etAge = findViewById(R.id.etAge)
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        rgGender = findViewById(R.id.rgGender)
        cbDiabetes = findViewById(R.id.cbDiabetes)
        cbHypertension = findViewById(R.id.cbHypertension)
        cbCholesterol = findViewById(R.id.cbCholesterol)
        cbGastritis = findViewById(R.id.cbGastritis)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val user = database.userDao().getUserByUsername(username)
            if (user != null && user.age > 0) {
                etAge.setText(user.age.toString())
                etWeight.setText(user.weight.toString())
                etHeight.setText(user.height.toString())
                
                if (user.gender == "Male") {
                    rgGender.check(R.id.rbMale)
                } else if (user.gender == "Female") {
                    rgGender.check(R.id.rbFemale)
                }

                val conditions = user.medicalConditions.split(",")
                cbDiabetes.isChecked = conditions.contains("Diabetes")
                cbHypertension.isChecked = conditions.contains("Hypertension")
                cbCholesterol.isChecked = conditions.contains("Cholesterol")
                cbGastritis.isChecked = conditions.contains("Gastritis")
                
                btnSaveProfile.text = "Update Profile"
            }
        }
    }

    private fun saveProfile() {
        val ageStr = etAge.text.toString()
        val weightStr = etWeight.text.toString()
        val heightStr = etHeight.text.toString()
        
        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all numeric fields", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageStr.toIntOrNull()
        val weight = weightStr.toFloatOrNull()
        val height = heightStr.toFloatOrNull()

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

        // Collect conditions
        val conditions = mutableListOf<String>()
        if (cbDiabetes.isChecked) conditions.add("Diabetes")
        if (cbHypertension.isChecked) conditions.add("Hypertension")
        if (cbCholesterol.isChecked) conditions.add("Cholesterol")
        if (cbGastritis.isChecked) conditions.add("Gastritis")
        
        val conditionsString = conditions.joinToString(",")

        // Calculate Calories (Mifflin-St Jeor)
        // Men: 10W + 6.25H - 5A + 5
        // Women: 10W + 6.25H - 5A - 161
        var bmr = (10 * weight) + (6.25 * height) - (5 * age)
        if (gender == "Male") {
            bmr += 5
        } else {
            bmr -= 161
        }

        // TDEE - Default to Sedentary (1.2) for now
        val tdee = (bmr * 1.2).toInt()

        lifecycleScope.launch {
            database.userDao().updateProfile(
                username,
                age,
                weight,
                height,
                gender,
                conditionsString,
                tdee
            )

            Toast.makeText(this@ProfileActivity, "Profile updated! Daily Target: $tdee kcal", Toast.LENGTH_LONG).show()
            
            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
            intent.putExtra("USERNAME", username)
            // Clear back stack so user can't go back to profile setup easily without intent
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
