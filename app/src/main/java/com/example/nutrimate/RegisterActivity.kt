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
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Register user in database
            lifecycleScope.launch {
                try {
                    // Check if username already exists
                    val existingUser = database.userDao().getUserByUsername(username)
                    if (existingUser != null) {
                        Toast.makeText(this@RegisterActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Check if email already exists
                    val existingEmail = database.userDao().getUserByEmail(email)
                    if (existingEmail != null) {
                        Toast.makeText(this@RegisterActivity, "Email already registered", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Create new user
                    val newUser = User(
                        fullName = fullName,
                        email = email,
                        username = username,
                        password = password // Store password as-is
                    )
                    
                    database.userDao().insertUser(newUser)
                    
                    // Verify the user was actually saved
                    val verifyUser = database.userDao().getUserByUsername(username)
                    if (verifyUser != null) {
                        Toast.makeText(this@RegisterActivity, "Registration successful! Please login with:\nUsername: $username", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Error: User not saved properly", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@RegisterActivity, "Registration error: ${e.message}", Toast.LENGTH_LONG).show()
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
                tvPasswordStrength.text = "Weak password"
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_weak))
            }
            PasswordStrength.MEDIUM -> {
                strengthBar1.setBackgroundColor(ContextCompat.getColor(this, R.color.strength_medium))
                strengthBar2.setBackgroundColor(ContextCompat.getColor(this, R.color.strength_medium))
                strengthBar3.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))
                tvPasswordStrength.text = "Medium password"
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_medium))
            }
            PasswordStrength.STRONG -> {
                strengthBar1.setBackgroundColor(ContextCompat.getColor(this, R.color.strength_strong))
                strengthBar2.setBackgroundColor(ContextCompat.getColor(this, R.color.strength_strong))
                strengthBar3.setBackgroundColor(ContextCompat.getColor(this, R.color.strength_strong))
                tvPasswordStrength.text = "Strong password"
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
