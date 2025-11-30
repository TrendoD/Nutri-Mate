package com.example.nutrimate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize database
        database = AppDatabase.getDatabase(this)

        // Initialize views
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        // Login button click listener
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString() // Don't trim password

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check credentials from database
            lifecycleScope.launch {
                try {
                    // Debug: Log the credentials being used
                    Log.d("LoginActivity", "Attempting login with username: '$username', password length: ${password.length}")
                    
                    // First check if user exists
                    val userExists = database.userDao().getUserByUsername(username)
                    
                    if (userExists == null) {
                        Log.d("LoginActivity", "User '$username' not found in database")
                        
                        // Debug: Show how many users exist
                        val userCount = database.userDao().getUserCount()
                        Log.d("LoginActivity", "Total users in database: $userCount")
                        
                        Toast.makeText(this@LoginActivity, "Username not found. Please register first.", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    
                    // Debug: Log found user info (password length only for security)
                    Log.d("LoginActivity", "User found: ${userExists.username}, stored password length: ${userExists.password.length}")
                    
                    // Now try to login with password
                    val user = database.userDao().login(username, password)

                    if (user != null) {
                        Log.d("LoginActivity", "Login successful for user: ${user.username}")
                        Toast.makeText(this@LoginActivity, "Login successful! Welcome ${user.fullName}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("USER_NAME", user.fullName)
                        intent.putExtra("USERNAME", user.username)
                        startActivity(intent)
                        finish()
                    } else {
                        // User exists but password is wrong
                        Log.d("LoginActivity", "Password mismatch. Expected length: ${userExists.password.length}, Provided length: ${password.length}")
                        Toast.makeText(this@LoginActivity, "Invalid password. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("LoginActivity", "Login error: ${e.message}", e)
                    Toast.makeText(this@LoginActivity, "Login error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Register text click listener
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}