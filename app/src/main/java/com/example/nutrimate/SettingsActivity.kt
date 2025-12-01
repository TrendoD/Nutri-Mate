package com.example.nutrimate

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingsActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    
    // Notification Settings
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var llBreakfastReminder: LinearLayout
    private lateinit var llLunchReminder: LinearLayout
    private lateinit var llDinnerReminder: LinearLayout
    private lateinit var tvBreakfastTime: TextView
    private lateinit var tvLunchTime: TextView
    private lateinit var tvDinnerTime: TextView
    
    // Preferences
    private lateinit var llUnitPreferences: LinearLayout
    private lateinit var switchDarkMode: SwitchCompat
    private lateinit var tvUnitSystem: TextView
    
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
    
    private lateinit var bottomNavigation: BottomNavigationView
    
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
            Toast.makeText(this, "Kesalahan: Pengguna tidak teridentifikasi", Toast.LENGTH_SHORT).show()
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
        switchDarkMode = findViewById(R.id.switchDarkMode)
        tvUnitSystem = findViewById(R.id.tvUnitSystem)
        
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
        
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        // Set app version
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            tvAppVersion.text = "Versi ${pInfo.versionName}"
        } catch (e: Exception) {
            tvAppVersion.text = "Versi 1.0.0"
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
        tvUnitSystem.text = if (unitSystem == "metric") "Metrik (kg, cm)" else "Imperial (lbs, inci)"
        
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
        
        setupBottomNavigation()
        
        // Notification toggle
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked).apply()
            updateReminderItemsEnabled()
            if (isChecked) {
                Toast.makeText(this, "Notifikasi diaktifkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifikasi dinonaktifkan", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Meal reminders
        llBreakfastReminder.setOnClickListener {
            if (switchNotifications.isChecked) {
                showTimePickerDialog("Sarapan", KEY_BREAKFAST_TIME, tvBreakfastTime)
            }
        }
        
        llLunchReminder.setOnClickListener {
            if (switchNotifications.isChecked) {
                showTimePickerDialog("Makan Siang", KEY_LUNCH_TIME, tvLunchTime)
            }
        }
        
        llDinnerReminder.setOnClickListener {
            if (switchNotifications.isChecked) {
                showTimePickerDialog("Makan Malam", KEY_DINNER_TIME, tvDinnerTime)
            }
        }
        
        // Unit preferences
        llUnitPreferences.setOnClickListener {
            showUnitSystemDialog()
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
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_settings
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    true
                }
                R.id.nav_food_log -> {
                    startActivity(Intent(this, FoodLogActivity::class.java).putExtra("USERNAME", username))
                    finish()
                    true
                }
                R.id.nav_stats -> {
                    startActivity(Intent(this, StatisticsActivity::class.java).putExtra("USERNAME", username))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java).putExtra("USERNAME", username))
                    finish()
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
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
            
            Toast.makeText(this, "Pengingat $mealType diatur ke $timeString", Toast.LENGTH_SHORT).show()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }
    
    private fun showUnitSystemDialog() {
        val options = arrayOf("Metrik (kg, cm)", "Imperial (lbs, inci)")
        val currentUnit = sharedPreferences.getString(KEY_UNIT_SYSTEM, "metric")
        val selectedIndex = if (currentUnit == "metric") 0 else 1
        
        AlertDialog.Builder(this)
            .setTitle("Pilih Sistem Satuan")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val selectedUnit = if (which == 0) "metric" else "imperial"
                sharedPreferences.edit().putString(KEY_UNIT_SYSTEM, selectedUnit).apply()
                tvUnitSystem.text = options[which]
                Toast.makeText(this, "Sistem satuan diubah ke ${options[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun showBackupDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cadangkan Data")
            .setMessage("Fitur ini akan mengekspor data Anda ke file lokal.\n\nCatatan: Fitur ini akan segera hadir dalam pembaruan mendatang.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showRestoreDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Pulihkan Data")
            .setMessage("Fitur ini akan mengimpor data dari file cadangan.\n\nCatatan: Fitur ini akan segera hadir dalam pembaruan mendatang.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showClearFoodLogDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Semua Catatan Makanan")
            .setMessage("Apakah Anda yakin ingin menghapus semua riwayat catatan makanan Anda? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus Semua") { _, _ ->
                lifecycleScope.launch {
                    try {
                        database.foodDao().deleteAllFoodLogsByUser(username)
                        database.waterIntakeDao().deleteAllWaterIntakeByUser(username)
                        Toast.makeText(this@SettingsActivity, "Semua catatan makanan telah dihapus", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@SettingsActivity, "Gagal menghapus data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Akun")
            .setMessage("Apakah Anda yakin ingin menghapus akun dan semua data terkait secara permanen? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus Akun") { _, _ ->
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
                        
                        Toast.makeText(this@SettingsActivity, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                        
                        // Navigate to login screen
                        val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@SettingsActivity, "Gagal menghapus akun: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Tentang NutriMate")
            .setMessage("""
                NutriMate - Makanan Sehat, Hidup Sehat
                
                Versi: 1.0.0
                
                NutriMate adalah aplikasi pelacakan nutrisi komprehensif yang dirancang untuk membantu Anda mengelola diet dan mencapai tujuan kesehatan Anda.
                
                Fitur:
                • Lacak asupan makanan harian
                • Pantau kalori dan makronutrisi
                • Tetapkan target nutrisi yang dipersonalisasi
                • Dapatkan rekomendasi diet berdasarkan profil kesehatan Anda
                • Lihat statistik dan kemajuan terperinci
                
                Dibuat dengan ❤️ untuk Anda yang lebih sehat!
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showPrivacyPolicyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Kebijakan Privasi")
            .setMessage("""
                Kebijakan Privasi untuk NutriMate
                
                Privasi Anda penting bagi kami.
                
                Penyimpanan Data:
                • Semua data Anda disimpan secara lokal di perangkat Anda
                • Kami tidak mengumpulkan atau mengirimkan informasi pribadi apa pun ke server eksternal
                • Data kesehatan Anda tetap pribadi dan di bawah kendali Anda
                
                Keamanan Data:
                • Data Anda dilindungi oleh langkah-langkah keamanan perangkat Anda
                • Kami menyarankan menggunakan keamanan tingkat perangkat (PIN, sidik jari, dll.)
                
                Penghapusan Data:
                • Anda dapat menghapus semua data Anda kapan saja melalui menu Pengaturan
                • Menghapus aplikasi akan menghapus semua data terkait
                
                Dengan menggunakan NutriMate, Anda menyetujui kebijakan privasi ini.
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showHelpFaqDialog() {
        AlertDialog.Builder(this)
            .setTitle("Bantuan & FAQ")
            .setMessage("""
                Pertanyaan yang Sering Diajukan
                
                T: Bagaimana cara melacak makanan saya?
                J: Buka Catatan Makanan dari navigasi bawah dan ketuk tombol + untuk menambahkan makanan.
                
                T: Bagaimana target kalori saya dihitung?
                J: Target Anda dihitung berdasarkan informasi profil Anda (usia, berat badan, tinggi badan, tingkat aktivitas, dan tujuan) menggunakan persamaan Mifflin-St Jeor.
                
                T: Bisakah saya membuat makanan khusus?
                J: Ya! Di layar Tambah Makanan, ketuk "Buat Makanan Kustom" untuk menambahkan makanan Anda sendiri dengan nilai nutrisi kustom.
                
                T: Bagaimana cara mengubah target nutrisi saya?
                J: Buka Profil > Target Nutrisi untuk menyesuaikan tujuan harian Anda.
                
                T: Apakah data saya dicadangkan?
                J: Saat ini, data disimpan secara lokal di perangkat Anda. Fitur cadangan cloud akan segera hadir.
                
                T: Bagaimana cara mengatur ulang kata sandi saya?
                J: Karena NutriMate hanya menggunakan penyimpanan lokal, tidak ada fitur pengaturan ulang kata sandi. Harap ingat kredensial Anda.
                
                Butuh bantuan lebih lanjut? Hubungi kami di support@nutrimate.app
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
}