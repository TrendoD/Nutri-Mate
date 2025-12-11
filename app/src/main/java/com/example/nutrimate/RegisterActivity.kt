package com.example.nutrimate

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.data.User
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var database: AppDatabase
    
    // Password Strength Views
    private lateinit var strengthBar1: View
    private lateinit var strengthBar2: View
    private lateinit var strengthBar3: View
    private lateinit var tvPasswordStrength: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize database
        database = AppDatabase.getDatabase(this)

        // Initialize views
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        
        // Initialize Password Strength Views
        strengthBar1 = findViewById(R.id.strengthBar1)
        strengthBar2 = findViewById(R.id.strengthBar2)
        strengthBar3 = findViewById(R.id.strengthBar3)
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength)
        
        // Setup Password Strength Checker
        setupPasswordStrengthChecker()

        // Register button click listener
        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString() // Don't trim password - user might want spaces
            val confirmPassword = etConfirmPassword.text.toString()

            // Basic validation
            if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || 
                password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Kata sandi tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Kata sandi harus minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Register user in database
            lifecycleScope.launch {
                try {
                    // Check if username already exists
                    val existingUser = database.userDao().getUserByUsername(username)
                    if (existingUser != null) {
                        Toast.makeText(this@RegisterActivity, "Nama pengguna sudah ada", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Check if email already exists
                    val existingEmail = database.userDao().getUserByEmail(email)
                    if (existingEmail != null) {
                        Toast.makeText(this@RegisterActivity, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Create new user
                    val hashedPassword = com.example.nutrimate.utils.SecurityUtils.hashPassword(password)
                    val newUser = User(
                        fullName = fullName,
                        email = email,
                        username = username,
                        password = hashedPassword
                    )
                    
                    database.userDao().insertUser(newUser)
                    
                    // Verify the user was actually saved
                    val verifyUser = database.userDao().getUserByUsername(username)
                    if (verifyUser != null) {
                        Toast.makeText(this@RegisterActivity, "Pendaftaran berhasil! Selamat datang $fullName", Toast.LENGTH_SHORT).show()
                        
                        // Save login session
                        val sharedPreferences = getSharedPreferences("NutriMatePrefs", android.content.Context.MODE_PRIVATE)
                        sharedPreferences.edit().putString("logged_in_user", username).apply()
                        
                        // Navigate to Onboarding (Setup Profile)
                        val intent = android.content.Intent(this@RegisterActivity, com.example.nutrimate.onboarding.OnboardingActivity::class.java)
                        intent.putExtra("USER_NAME", fullName)
                        intent.putExtra("USERNAME", username)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Kesalahan: Pengguna tidak tersimpan dengan benar", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@RegisterActivity, "Gagal mendaftar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Login text click listener
        tvLogin.setOnClickListener {
            finish()
        }
    }
    
    private fun setupPasswordStrengthChecker() {
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                updatePasswordStrength(password)
            }
        })
    }
    
    private fun updatePasswordStrength(password: String) {
        if (password.isEmpty()) {
            // Hide strength indicator when password is empty
            tvPasswordStrength.visibility = View.GONE
            resetStrengthBars()
            return
        }
        
        tvPasswordStrength.visibility = View.VISIBLE
        val strength = calculatePasswordStrength(password)
        
        when (strength) {
            PasswordStrength.WEAK -> {
                strengthBar1.setBackgroundColor(ContextCompat.getColor(this, R.color.strength_weak))
                strengthBar2.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))
                strengthBar3.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))
                tvPasswordStrength.text = "Kata sandi lemah"
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_weak))
            }
            PasswordStrength.MEDIUM -> {
                strengthBar1.setBackgroundColor(ContextCompat.getColor(this, R.color.strength_medium))
                strengthBar2.setBackgroundColor(ContextCompat.getColor(this, R.color.strength_medium))
                strengthBar3.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))
                tvPasswordStrength.text = "Kata sandi sedang"
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_medium))
            }
            PasswordStrength.STRONG -> {
                strengthBar1.setBackgroundColor(ContextCompat.getColor(this, R.color.strength_strong))
                strengthBar2.setBackgroundColor(ContextCompat.getColor(this, R.color.strength_strong))
                strengthBar3.setBackgroundColor(ContextCompat.getColor(this, R.color.strength_strong))
                tvPasswordStrength.text = "Kata sandi kuat"
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_strong))
            }
        }
    }
    
    private fun resetStrengthBars() {
        val grayColor = ContextCompat.getColor(this, R.color.gray_light)
        strengthBar1.setBackgroundColor(grayColor)
        strengthBar2.setBackgroundColor(grayColor)
        strengthBar3.setBackgroundColor(grayColor)
    }
    
    private fun calculatePasswordStrength(password: String): PasswordStrength {
        var score = 0
        
        // Length checks
        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        
        // Contains lowercase
        if (password.any { it.isLowerCase() }) score++
        
        // Contains uppercase
        if (password.any { it.isUpperCase() }) score++
        
        // Contains digit
        if (password.any { it.isDigit() }) score++
        
        // Contains special character
        if (password.any { !it.isLetterOrDigit() }) score++
        
        return when {
            score <= 2 -> PasswordStrength.WEAK
            score <= 4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
    
    private enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}