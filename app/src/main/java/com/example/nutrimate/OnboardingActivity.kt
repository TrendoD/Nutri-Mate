package com.example.nutrimate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // 1. Inisialisasi Tombol dari Layout
        // Ada dua tombol Get Started di layout kamu (Card & Bottom)
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val btnGetStartedCard = findViewById<Button>(R.id.btnGetStartedCard)

        // 2. Aksi jika tombol Bawah diklik
        btnGetStarted.setOnClickListener {
            navigateToLogin()
        }

        // 3. Aksi jika tombol di Card diklik
        btnGetStartedCard.setOnClickListener {
            navigateToLogin()
        }
    }

    // Fungsi untuk pindah ke LoginActivity
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        // finish() agar kalau ditekan Back, tidak balik lagi ke Onboarding
        finish()
    }
}