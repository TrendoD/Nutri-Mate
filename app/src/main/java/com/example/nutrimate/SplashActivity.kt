package com.example.nutrimate

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DELAY = 2500L // 2.5 seconds before fade out
        private const val PREF_NAME = "NutriMatePrefs"
        private const val KEY_LOGGED_IN_USER = "logged_in_user"
    }

    private lateinit var rootLayout: ConstraintLayout
    private var isNavigating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide action bar for splash screen
        supportActionBar?.hide()

        // Initialize views
        rootLayout = findViewById(R.id.rootLayout)
        val logoImage = findViewById<ImageView>(R.id.ivSplashLogo)
        val appName = findViewById<TextView>(R.id.tvAppName)
        val tagline = findViewById<TextView>(R.id.tvTagline)
        val tapHint = findViewById<TextView>(R.id.tvTapHint)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Show progress bar, hide tap hint (auto mode)
        progressBar.visibility = View.VISIBLE
        tapHint.visibility = View.GONE

        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Start animations
        logoImage.startAnimation(fadeIn)
        appName.startAnimation(slideUp)
        tagline.startAnimation(slideUp)

        // Auto navigate after delay with fade out animation
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isNavigating) {
                isNavigating = true
                startFadeOutAndNavigate()
            }
        }, SPLASH_DELAY)
    }

    private fun startFadeOutAndNavigate() {
        // Load fade out animation
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            
            override fun onAnimationEnd(animation: Animation?) {
                navigateToNextScreen()
            }
            
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        
        // Start fade out animation on root layout
        rootLayout.startAnimation(fadeOut)
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
