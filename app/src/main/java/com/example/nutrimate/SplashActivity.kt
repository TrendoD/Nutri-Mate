package com.example.nutrimate

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrimate.ui.theme.GradientCenter
import com.example.nutrimate.ui.theme.GradientEnd
import com.example.nutrimate.ui.theme.GradientStart
import kotlinx.coroutines.delay

// Ini Activity untuk Splash Screen
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    companion object {
        // Durasi tampilan splash screen
        private const val SPLASH_DELAY_MS = 2500L
        private const val LOGO_FADE_DURATION_MS = 800
        private const val TEXT_FADE_DELAY_MS = 200L
        private const val TEXT_FADE_DURATION_MS = 600
        private const val SCREEN_FADE_OUT_DURATION_MS = 500

        // Key SharedPreferences
        private const val PREF_NAME = "NutriMatePrefs"
        private const val KEY_LOGGED_IN_USER = "logged_in_user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen(onSplashFinished = { navigateToNextScreen() })
        }
    }

    // Navigasi ke layar berikutnya (Main atau Login)
    private fun navigateToNextScreen() {
        val sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val loggedInUser = sharedPreferences.getString(KEY_LOGGED_IN_USER, null)

        val intent = if (loggedInUser != null) {
            Intent(this, MainActivity::class.java).apply {
                putExtra("USERNAME", loggedInUser)
            }
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
        applyFadeTransition()
    }

    // Animasi transisi antar activity
    private fun applyFadeTransition() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}

// Ini Tampilan UI Splash Screen
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    // State animasi
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }

    // Jalankan animasi secara berurutan
    LaunchedEffect(Unit) {
        // Fade in logo
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    LaunchedEffect(Unit) {
        // Fade in teks setelah delay
        delay(200)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    LaunchedEffect(Unit) {
        // Tunggu splash selesai, lalu fade out layar
        delay(2500)
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500)
        )
        onSplashFinished()
    }

    // Layout Splash Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GradientStart, GradientCenter, GradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ini Logo Aplikasi
            Image(
                painter = painterResource(id = R.drawable.logo_aplikasi),
                contentDescription = "Logo NutriMate",
                modifier = Modifier
                    .size(150.dp)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ini Judul Aplikasi
            Text(
                text = "Nutri-Mate",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ini Tagline Aplikasi
            Text(
                text = "Makanan Sehat, Hidup Sehat",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }

        // Ini Loading Indicator
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .alpha(textAlpha.value)
                .size(36.dp),
            color = Color.White,
            strokeWidth = 3.dp
        )
    }
}

// Ini Preview Splash Screen
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}
