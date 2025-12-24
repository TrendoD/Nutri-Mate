package com.example.nutrimate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.nutrimate.data.User
import com.example.nutrimate.ui.components.NutriMatePrimaryButton
import com.example.nutrimate.ui.components.NutriMateTextField
import com.example.nutrimate.ui.theme.GradientCenter
import com.example.nutrimate.ui.theme.GradientEnd
import com.example.nutrimate.ui.theme.GradientStart
import com.example.nutrimate.ui.theme.GrayLight
import com.example.nutrimate.ui.theme.GrayText
import com.example.nutrimate.ui.theme.GreenDark
import com.example.nutrimate.ui.theme.StrengthMedium
import com.example.nutrimate.ui.theme.StrengthStrong
import com.example.nutrimate.ui.theme.StrengthWeak
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)

        setContent {
            RegisterScreen(
                onRegisterClick = { email, fullName, username, password, confirmPassword ->
                    performRegistration(email, fullName, username, password, confirmPassword, database)
                },
                onLoginClick = { finish() }
            )
        }
    }

    private fun performRegistration(
        email: String,
        fullName: String,
        username: String,
        password: String,
        confirmPassword: String,
        database: AppDatabase
    ) {
        // Validasi dasar
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()
        ) {
            Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Kata sandi tidak cocok", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Kata sandi harus minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        MainScope().launch {
            try {
                // Cek username sudah ada
                val existingUser = database.userDao().getUserByUsername(username)
                if (existingUser != null) {
                    Toast.makeText(this@RegisterActivity, "Nama pengguna sudah ada", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Cek email sudah terdaftar
                val existingEmail = database.userDao().getUserByEmail(email)
                if (existingEmail != null) {
                    Toast.makeText(this@RegisterActivity, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Buat user baru
                val hashedPassword = com.example.nutrimate.utils.SecurityUtils.hashPassword(password)
                val newUser = User(
                    fullName = fullName,
                    email = email,
                    username = username,
                    password = hashedPassword
                )

                database.userDao().insertUser(newUser)

                // Verifikasi user tersimpan
                val verifyUser = database.userDao().getUserByUsername(username)
                if (verifyUser != null) {
                    Toast.makeText(this@RegisterActivity, "Pendaftaran berhasil! Selamat datang $fullName", Toast.LENGTH_SHORT).show()

                    // Simpan sesi login
                    val sharedPreferences = getSharedPreferences("NutriMatePrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putString("logged_in_user", username).apply()

                    // Navigasi ke Onboarding
                    val intent = Intent(this@RegisterActivity, com.example.nutrimate.onboarding.OnboardingActivity::class.java)
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
}

/**
 * Enum untuk kekuatan password
 */
enum class PasswordStrength {
    NONE, WEAK, MEDIUM, STRONG
}

/**
 * Fungsi untuk menghitung kekuatan password
 */
fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.NONE

    var score = 0

    // Cek panjang
    if (password.length >= 6) score++
    if (password.length >= 8) score++
    if (password.length >= 12) score++

    // Mengandung huruf kecil
    if (password.any { it.isLowerCase() }) score++

    // Mengandung huruf besar
    if (password.any { it.isUpperCase() }) score++

    // Mengandung angka
    if (password.any { it.isDigit() }) score++

    // Mengandung karakter spesial
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        score <= 2 -> PasswordStrength.WEAK
        score <= 4 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.STRONG
    }
}

@Composable
fun RegisterScreen(
    onRegisterClick: (email: String, fullName: String, username: String, password: String, confirmPassword: String) -> Unit = { _, _, _, _, _ -> },
    onLoginClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val passwordStrength = remember(password) { calculatePasswordStrength(password) }

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
            // Tombol login di pojok kanan atas
            Text(
                text = "Sudah punya akun? Masuk",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clickable { onLoginClick() }
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
                text = "Mulai gratis.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Gratis selamanya. Tidak perlu kartu kredit.",
                fontSize = 14.sp,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email field
            NutriMateTextField(
                value = email,
                onValueChange = { email = it },
                label = "Alamat Email",
                placeholder = "alucard@gmail.com",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Full name field
            NutriMateTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Nama Anda",
                placeholder = "Muhammad Alucard"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Username field
            NutriMateTextField(
                value = username,
                onValueChange = { username = it },
                label = "Nama Pengguna",
                placeholder = "Alucard"
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

            // Password strength indicator
            PasswordStrengthIndicator(strength = passwordStrength)

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm password field
            NutriMateTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Konfirmasi Kata Sandi",
                placeholder = "••••••••••••••••",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol register
            NutriMatePrimaryButton(
                text = "Daftar",
                onClick = { onRegisterClick(email, fullName, username, password, confirmPassword) }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * Komponen indikator kekuatan password
 */
@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    if (strength == PasswordStrength.NONE) return

    val bar1Color by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.WEAK -> StrengthWeak
            PasswordStrength.MEDIUM -> StrengthMedium
            PasswordStrength.STRONG -> StrengthStrong
            else -> GrayLight
        },
        label = "bar1"
    )

    val bar2Color by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.MEDIUM -> StrengthMedium
            PasswordStrength.STRONG -> StrengthStrong
            else -> GrayLight
        },
        label = "bar2"
    )

    val bar3Color by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.STRONG -> StrengthStrong
            else -> GrayLight
        },
        label = "bar3"
    )

    val strengthText = when (strength) {
        PasswordStrength.WEAK -> "Kata sandi lemah"
        PasswordStrength.MEDIUM -> "Kata sandi sedang"
        PasswordStrength.STRONG -> "Kata sandi kuat"
        else -> ""
    }

    val textColor = when (strength) {
        PasswordStrength.WEAK -> StrengthWeak
        PasswordStrength.MEDIUM -> StrengthMedium
        PasswordStrength.STRONG -> StrengthStrong
        else -> GrayText
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Bar indikator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(bar1Color, RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(bar2Color, RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(bar3Color, RoundedCornerShape(2.dp))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Teks kekuatan
        Text(
            text = strengthText,
            fontSize = 12.sp,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}