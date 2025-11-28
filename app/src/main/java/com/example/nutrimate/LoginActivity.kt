package com.example.nutrimate

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        private const val KEY_LOGGED_IN_USER_FULLNAME = "logged_in_user_fullname"
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
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check credentials from database
            lifecycleScope.launch {
                val user = database.userDao().login(username, password)
                
                if (user != null) {
                    // Save login session
                    saveLoginSession(user.username, user.fullName)
                    
                    Toast.makeText(this@LoginActivity, "Login successful! Welcome ${user.fullName}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("USER_NAME", user.fullName)
                    intent.putExtra("USERNAME", user.username)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Register text click listener
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveLoginSession(username: String, fullName: String) {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString(KEY_LOGGED_IN_USER, username)
            putString(KEY_LOGGED_IN_USER_FULLNAME, fullName)
            apply()
        }
    }
}
