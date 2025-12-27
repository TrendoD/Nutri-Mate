package com.example.nutrimate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.ui.components.NutriMatePrimaryButton
import com.example.nutrimate.ui.components.NutriMateTextField
import com.example.nutrimate.ui.theme.GradientCenter
import com.example.nutrimate.ui.theme.GradientEnd
import com.example.nutrimate.ui.theme.GradientStart
import com.example.nutrimate.ui.theme.GrayText
import com.example.nutrimate.ui.theme.GreenDark
import com.example.nutrimate.ui.theme.GreenPrimary
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    companion object {
        private const val PREF_NAME = "NutriMatePrefs"
        private const val KEY_LOGGED_IN_USER = "logged_in_user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)

        setContent {
            LoginScreen(
                onLoginClick = { username, password ->
                    performLogin(username, password, database)
                },
                onRegisterClick = {
                    startActivity(Intent(this, RegisterActivity::class.java))
                },
                onForgotPasswordClick = {
                    // TODO: Implementasi lupa password
                    Toast.makeText(this, "Fitur lupa password belum tersedia", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun performLogin(username: String, password: String, database: AppDatabase) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        kotlinx.coroutines.MainScope().launch {
            try {
                Log.d("LoginActivity", "Attempting login with identifier: '$username'")

                val hashedPassword = com.example.nutrimate.utils.SecurityUtils.hashPassword(password)
                val user = database.userDao().login(username, hashedPassword)

                if (user != null) {
                    Log.d("LoginActivity", "Login successful for user: ${user.username}")

                    val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    sharedPreferences.edit().putString(KEY_LOGGED_IN_USER, user.username).apply()

                    Toast.makeText(this@LoginActivity, "Berhasil masuk! Selamat datang ${user.fullName}", Toast.LENGTH_SHORT).show()

                    val intent = if (user.age == 0) {
                        Intent(this@LoginActivity, com.example.nutrimate.onboarding.OnboardingActivity::class.java)
                    } else {
                        Intent(this@LoginActivity, MainActivity::class.java)
                    }

                    intent.putExtra("USER_NAME", user.fullName)
                    intent.putExtra("USERNAME", user.username)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Email/Username atau kata sandi salah.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("LoginActivity", "Login error: ${e.message}", e)
                Toast.makeText(this@LoginActivity, "Gagal masuk: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Gradient header background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientCenter, GradientEnd)
                    )
                )
        ) {
            // Tombol register di pojok kanan atas
            Text(
                text = "Belum punya akun? Mulai",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clickable { onRegisterClick() }
                    .padding(8.dp)
            )

            // App title
            Text(
                text = "NutriMate",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 40.dp)
            )
        }

        // Form container dengan scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(32.dp)
        ) {
            // Header teks
            Text(
                text = "Selamat Datang Kembali",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Masukkan detail Anda di bawah",
                fontSize = 14.sp,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username/Email field
            NutriMateTextField(
                value = username,
                onValueChange = { username = it },
                label = "Email atau Username",
                placeholder = "Email atau Username",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password field
            NutriMateTextField(
                value = password,
                onValueChange = { password = it },
                label = "Kata Sandi",
                placeholder = "••••••••••••••••",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lupa password
            Text(
                text = "Lupa kata sandi?",
                fontSize = 13.sp,
                color = GreenPrimary,
                modifier = Modifier
                    .clickable { onForgotPasswordClick() }
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol login
            NutriMatePrimaryButton(
                text = "Masuk",
                onClick = { onLoginClick(username, password) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}