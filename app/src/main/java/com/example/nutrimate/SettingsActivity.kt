package com.example.nutrimate

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingsActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    
    // Notification Settings
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var llBreakfastReminder: LinearLayout
    private lateinit var llLunchReminder: LinearLayout
    private lateinit var llDinnerReminder: LinearLayout
    private lateinit var tvBreakfastTime: TextView
    private lateinit var tvLunchTime: TextView
    private lateinit var tvDinnerTime: TextView
    
    // Preferences
    private lateinit var llUnitPreferences: LinearLayout
    private lateinit var llLanguage: LinearLayout
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var tvUnitSystem: TextView
    private lateinit var tvLanguage: TextView
    
    // Data Management
    private lateinit var llBackupData: LinearLayout
    private lateinit var llRestoreData: LinearLayout
    private lateinit var llClearFoodLog: LinearLayout
    private lateinit var llDeleteAccount: LinearLayout
    
    // About
    private lateinit var llAboutApp: LinearLayout
    private lateinit var llPrivacyPolicy: LinearLayout
    private lateinit var llHelpFaq: LinearLayout
    private lateinit var tvAppVersion: TextView
    
    private lateinit var database: AppDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private var username: String = ""
    
    companion object {
        const val PREFS_NAME = "NutriMateSettings"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_BREAKFAST_TIME = "breakfast_time"
        const val KEY_LUNCH_TIME = "lunch_time"
        const val KEY_DINNER_TIME = "dinner_time"
        const val KEY_UNIT_SYSTEM = "unit_system"
        const val KEY_LANGUAGE = "language"
        const val KEY_DARK_MODE = "dark_mode"
        
        // Login session
        private const val LOGIN_PREF_NAME = "NutriMatePrefs"
        private const val KEY_LOGGED_IN_USER = "logged_in_user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        database = AppDatabase.getDatabase(this)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        username = intent.getStringExtra("USERNAME") ?: ""
        
        if (username.isEmpty()) {
            Toast.makeText(this, "Error: User not identified", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        initViews()
        loadSettings()
        setupListeners()
    }
    
    private fun initViews() {
        ivBack = findViewById(R.id.ivBack)
        
        // Notifications
        switchNotifications = findViewById(R.id.switchNotifications)
        llBreakfastReminder = findViewById(R.id.llBreakfastReminder)
        llLunchReminder = findViewById(R.id.llLunchReminder)
        llDinnerReminder = findViewById(R.id.llDinnerReminder)
        tvBreakfastTime = findViewById(R.id.tvBreakfastTime)
        tvLunchTime = findViewById(R.id.tvLunchTime)
        tvDinnerTime = findViewById(R.id.tvDinnerTime)
        
        // Preferences
        llUnitPreferences = findViewById(R.id.llUnitPreferences)
        llLanguage = findViewById(R.id.llLanguage)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        tvUnitSystem = findViewById(R.id.tvUnitSystem)
        tvLanguage = findViewById(R.id.tvLanguage)
        
        // Data Management
        llBackupData = findViewById(R.id.llBackupData)
        llRestoreData = findViewById(R.id.llRestoreData)
        llClearFoodLog = findViewById(R.id.llClearFoodLog)
        llDeleteAccount = findViewById(R.id.llDeleteAccount)
        
        // About
        llAboutApp = findViewById(R.id.llAboutApp)
        llPrivacyPolicy = findViewById(R.id.llPrivacyPolicy)
        llHelpFaq = findViewById(R.id.llHelpFaq)
        tvAppVersion = findViewById(R.id.tvAppVersion)
        
        // Set app version
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            tvAppVersion.text = "Version ${pInfo.versionName}"
        } catch (e: Exception) {
            tvAppVersion.text = "Version 1.0.0"
        }
    }
    
    private fun loadSettings() {
        // Load notification settings
        switchNotifications.isChecked = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
        tvBreakfastTime.text = sharedPreferences.getString(KEY_BREAKFAST_TIME, "08:00 AM")
        tvLunchTime.text = sharedPreferences.getString(KEY_LUNCH_TIME, "12:00 PM")
        tvDinnerTime.text = sharedPreferences.getString(KEY_DINNER_TIME, "07:00 PM")
        
        // Load preferences
        val unitSystem = sharedPreferences.getString(KEY_UNIT_SYSTEM, "metric")
        tvUnitSystem.text = if (unitSystem == "metric") "Metric (kg, cm)" else "Imperial (lbs, inch)"
        
        val language = sharedPreferences.getString(KEY_LANGUAGE, "en")
        tvLanguage.text = if (language == "en") "English" else "Bahasa Indonesia"
        
        switchDarkMode.isChecked = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        
        // Update reminder items enabled state
        updateReminderItemsEnabled()
    }
    
    private fun updateReminderItemsEnabled() {
        val enabled = switchNotifications.isChecked
        llBreakfastReminder.alpha = if (enabled) 1.0f else 0.5f
        llLunchReminder.alpha = if (enabled) 1.0f else 0.5f
        llDinnerReminder.alpha = if (enabled) 1.0f else 0.5f
        llBreakfastReminder.isClickable = enabled
        llLunchReminder.isClickable = enabled
        llDinnerReminder.isClickable = enabled
    }
    
    private fun setupListeners() {
        ivBack.setOnClickListener {
            finish()
        }
        
        // Notification toggle
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked).apply()
            updateReminderItemsEnabled()
            if (isChecked) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Meal reminders
        llBreakfastReminder.setOnClickListener {
            if (switchNotifications.isChecked) {
                showTimePickerDialog("Breakfast", KEY_BREAKFAST_TIME, tvBreakfastTime)
            }
        }
        
        llLunchReminder.setOnClickListener {
            if (switchNotifications.isChecked) {
                showTimePickerDialog("Lunch", KEY_LUNCH_TIME, tvLunchTime)
            }
        }
        
        llDinnerReminder.setOnClickListener {
            if (switchNotifications.isChecked) {
                showTimePickerDialog("Dinner", KEY_DINNER_TIME, tvDinnerTime)
            }
        }
        
        // Unit preferences
        llUnitPreferences.setOnClickListener {
            showUnitSystemDialog()
        }
        
        // Language
        llLanguage.setOnClickListener {
            showLanguageDialog()
        }
        
        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        
        // Data management
        llBackupData.setOnClickListener {
            showBackupDataDialog()
        }
        
        llRestoreData.setOnClickListener {
            showRestoreDataDialog()
        }
        
        llClearFoodLog.setOnClickListener {
            showClearFoodLogDialog()
        }
        
        llDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
        
        // About
        llAboutApp.setOnClickListener {
            showAboutDialog()
        }
        
        llPrivacyPolicy.setOnClickListener {
            showPrivacyPolicyDialog()
        }
        
        llHelpFaq.setOnClickListener {
            showHelpFaqDialog()
        }
    }
    
    private fun showTimePickerDialog(mealType: String, prefKey: String, textView: TextView) {
        val calendar = Calendar.getInstance()
        val currentTime = textView.text.toString()
        
        // Parse current time
        try {
            val parts = currentTime.replace(" AM", "").replace(" PM", "").split(":")
            var hour = parts[0].toInt()
            val minute = parts[1].toInt()
            
            if (currentTime.contains("PM") && hour != 12) hour += 12
            if (currentTime.contains("AM") && hour == 12) hour = 0
            
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
        } catch (e: Exception) {
            // Use default time
        }
        
        TimePickerDialog(this, { _, hourOfDay, minute ->
            val amPm = if (hourOfDay < 12) "AM" else "PM"
            val hour12 = if (hourOfDay == 0) 12 else if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
            val timeString = String.format("%02d:%02d %s", hour12, minute, amPm)
            
            textView.text = timeString
            sharedPreferences.edit().putString(prefKey, timeString).apply()
            
            Toast.makeText(this, "$mealType reminder set to $timeString", Toast.LENGTH_SHORT).show()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }
    
    private fun showUnitSystemDialog() {
        val options = arrayOf("Metric (kg, cm)", "Imperial (lbs, inch)")
        val currentUnit = sharedPreferences.getString(KEY_UNIT_SYSTEM, "metric")
        val selectedIndex = if (currentUnit == "metric") 0 else 1
        
        AlertDialog.Builder(this)
            .setTitle("Select Unit System")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val selectedUnit = if (which == 0) "metric" else "imperial"
                sharedPreferences.edit().putString(KEY_UNIT_SYSTEM, selectedUnit).apply()
                tvUnitSystem.text = options[which]
                Toast.makeText(this, "Unit system changed to ${options[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showLanguageDialog() {
        val options = arrayOf("English", "Bahasa Indonesia")
        val currentLang = sharedPreferences.getString(KEY_LANGUAGE, "en")
        val selectedIndex = if (currentLang == "en") 0 else 1
        
        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val selectedLang = if (which == 0) "en" else "id"
                sharedPreferences.edit().putString(KEY_LANGUAGE, selectedLang).apply()
                tvLanguage.text = options[which]
                Toast.makeText(this, "Language changed to ${options[which]}. Please restart the app for full effect.", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showBackupDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Backup Data")
            .setMessage("This feature will export your data to a local file.\n\nNote: This feature is coming soon in a future update.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showRestoreDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Restore Data")
            .setMessage("This feature will import data from a backup file.\n\nNote: This feature is coming soon in a future update.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showClearFoodLogDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Food Logs")
            .setMessage("Are you sure you want to delete all your food log history? This action cannot be undone.")
            .setPositiveButton("Delete All") { _, _ ->
                lifecycleScope.launch {
                    try {
                        database.foodDao().deleteAllFoodLogsByUser(username)
                        database.waterIntakeDao().deleteAllWaterIntakeByUser(username)
                        Toast.makeText(this@SettingsActivity, "All food logs have been cleared", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@SettingsActivity, "Error clearing data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account and all associated data? This action cannot be undone.")
            .setPositiveButton("Delete Account") { _, _ ->
                lifecycleScope.launch {
                    try {
                        // Delete all user data
                        database.foodDao().deleteAllFoodLogsByUser(username)
                        database.foodDao().deleteAllCustomFoodsByUser(username)
                        database.foodDao().deleteAllFavoriteFoodsByUser(username)
                        database.waterIntakeDao().deleteAllWaterIntakeByUser(username)
                        database.userDao().deleteUserByUsername(username)
                        
                        // Clear login session
                        val loginPrefs = getSharedPreferences(LOGIN_PREF_NAME, Context.MODE_PRIVATE)
                        loginPrefs.edit().remove(KEY_LOGGED_IN_USER).apply()
                        
                        Toast.makeText(this@SettingsActivity, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                        
                        // Navigate to login screen
                        val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@SettingsActivity, "Error deleting account: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About NutriMate")
            .setMessage("""
                NutriMate - Healthy Food, Healthy Life
                
                Version: 1.0.0
                
                NutriMate is a comprehensive nutrition tracking app designed to help you manage your diet and achieve your health goals.
                
                Features:
                • Track daily food intake
                • Monitor calories and macronutrients
                • Set personalized nutrition targets
                • Get diet recommendations based on your health profile
                • View detailed statistics and progress
                
                Made with ❤️ for a healthier you!
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showPrivacyPolicyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Privacy Policy")
            .setMessage("""
                Privacy Policy for NutriMate
                
                Your privacy is important to us.
                
                Data Storage:
                • All your data is stored locally on your device
                • We do not collect or transmit any personal information to external servers
                • Your health data remains private and under your control
                
                Data Security:
                • Your data is protected by your device's security measures
                • We recommend using device-level security (PIN, fingerprint, etc.)
                
                Data Deletion:
                • You can delete all your data at any time through the Settings menu
                • Deleting the app will remove all associated data
                
                By using NutriMate, you agree to this privacy policy.
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showHelpFaqDialog() {
        AlertDialog.Builder(this)
            .setTitle("Help & FAQ")
            .setMessage("""
                Frequently Asked Questions
                
                Q: How do I track my meals?
                A: Go to Food Log from the bottom navigation and tap the + button to add food to your meals.
                
                Q: How are my calorie targets calculated?
                A: Your targets are calculated based on your profile information (age, weight, height, activity level, and goals) using the Mifflin-St Jeor equation.
                
                Q: Can I create custom foods?
                A: Yes! In the Add Food screen, tap "Create Custom Food" to add your own foods with custom nutrition values.
                
                Q: How do I change my nutrition targets?
                A: Go to Profile > Nutrition Targets to customize your daily goals.
                
                Q: Is my data backed up?
                A: Currently, data is stored locally on your device. Cloud backup feature is coming soon.
                
                Q: How do I reset my password?
                A: Since NutriMate uses local storage only, there's no password reset feature. Please remember your credentials.
                
                Need more help? Contact us at support@nutrimate.app
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
}
