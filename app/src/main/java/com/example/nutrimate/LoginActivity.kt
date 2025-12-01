package com.example.nutrimate

import android.content.Context
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

    companion object {
        private const val PREF_NAME = "NutriMatePrefs"
        private const val KEY_LOGGED_IN_USER = "logged_in_user"
    }

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
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
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
                        
                        Toast.makeText(this@LoginActivity, "Nama pengguna tidak ditemukan. Harap daftar terlebih dahulu.", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    
                    // Debug: Log found user info (password length only for security)
                    Log.d("LoginActivity", "User found: ${userExists.username}, stored password length: ${userExists.password.length}")
                    
                    // Now try to login with password
                    val user = database.userDao().login(username, password)

                    if (user != null) {
                        Log.d("LoginActivity", "Login successful for user: ${user.username}")
                        
                        // Save login session to SharedPreferences
                        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                        sharedPreferences.edit().putString(KEY_LOGGED_IN_USER, user.username).apply()
                        
                        Toast.makeText(this@LoginActivity, "Berhasil masuk! Selamat datang ${user.fullName}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("USER_NAME", user.fullName)
                        intent.putExtra("USERNAME", user.username)
                        startActivity(intent)
                        finish()
                    } else {
                        // User exists but password is wrong
                        Log.d("LoginActivity", "Password mismatch. Expected length: ${userExists.password.length}, Provided length: ${password.length}")
                        Toast.makeText(this@LoginActivity, "Kata sandi salah. Silakan coba lagi.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("LoginActivity", "Login error: ${e.message}", e)
                    Toast.makeText(this@LoginActivity, "Gagal masuk: ${e.message}", Toast.LENGTH_LONG).show()
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