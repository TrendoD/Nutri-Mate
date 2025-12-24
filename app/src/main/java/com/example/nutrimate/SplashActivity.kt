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
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    companion object {
        private const val SPLASH_DELAY = 2500L // 2.5 seconds before fade out
        private const val PREF_NAME = "NutriMatePrefs"
        private const val KEY_LOGGED_IN_USER = "logged_in_user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SplashScreen(
                onSplashFinished = {
                    navigateToNextScreen()
                }
            )
        }
    }

    private fun navigateToNextScreen() {
        // Check if user is already logged in
        val sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val loggedInUser = sharedPreferences.getString(KEY_LOGGED_IN_USER, null)

        val intent = if (loggedInUser != null) {
            // User is logged in, go to MainActivity
            Intent(this, MainActivity::class.java).apply {
                putExtra("USERNAME", loggedInUser)
            }
        } else {
            // User is not logged in, go to LoginActivity
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()

        // Add transition animation
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    // Animation states
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }

    // Launch animations
    LaunchedEffect(Unit) {
        // Fade in logo
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    LaunchedEffect(Unit) {
        // Slight delay for text animation (slide up effect simulated with fade)
        delay(200)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    LaunchedEffect(Unit) {
        // Wait for splash delay then fade out
        delay(2500L)
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500)
        )
        onSplashFinished()
    }

    // Gradient background colors (from splash_background.xml)
    val gradientColors = listOf(
        Color(0xFF4CAF50), // startColor
        Color(0xFF66BB6A), // centerColor
        Color(0xFF81C784)  // endColor
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_aplikasi),
                contentDescription = "Logo NutriMate",
                modifier = Modifier
                    .size(150.dp)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "Nutri-Mate",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Makanan Sehat, Hidup Sehat",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }

        // Progress indicator at the bottom
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(textAlpha.value)
                .size(36.dp),
            color = Color.White,
            strokeWidth = 3.dp
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}
