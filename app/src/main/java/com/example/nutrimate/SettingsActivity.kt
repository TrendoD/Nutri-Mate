package com.example.nutrimate

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.ui.main.NavItem
import com.example.nutrimate.ui.settings.SettingsScreen
import com.example.nutrimate.ui.settings.SettingsScreenState
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingsActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private var username: String = ""
    
    // State for Compose UI
    private var screenState by mutableStateOf(SettingsScreenState())
    
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
        
        database = AppDatabase.getDatabase(this)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        username = intent.getStringExtra("USERNAME") ?: ""
        
        if (username.isEmpty()) {
            Toast.makeText(this, "Kesalahan: Pengguna tidak teridentifikasi", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        loadSettings()
        
        setContent {
            SettingsScreen(
                state = screenState,
                selectedNavItem = NavItem.SETTINGS,
                onNavItemClick = { navItem -> handleNavigation(navItem) },
                onMyProfileClick = {
                    startActivity(Intent(this, ProfileActivity::class.java).putExtra("USERNAME", username))
                },
                onNotificationsToggle = { enabled -> handleNotificationToggle(enabled) },
                onBreakfastReminderClick = { 
                    if (screenState.notificationsEnabled) {
                        showTimePickerDialog("Sarapan", KEY_BREAKFAST_TIME) { time ->
                            screenState = screenState.copy(breakfastTime = time)
                        }
                    }
                },
                onLunchReminderClick = { 
                    if (screenState.notificationsEnabled) {
                        showTimePickerDialog("Makan Siang", KEY_LUNCH_TIME) { time ->
                            screenState = screenState.copy(lunchTime = time)
                        }
                    }
                },
                onDinnerReminderClick = { 
                    if (screenState.notificationsEnabled) {
                        showTimePickerDialog("Makan Malam", KEY_DINNER_TIME) { time ->
                            screenState = screenState.copy(dinnerTime = time)
                        }
                    }
                },
                onUnitPreferencesClick = { showUnitSystemDialog() },
                onDarkModeToggle = { enabled -> handleDarkModeToggle(enabled) },
                onBackupDataClick = { showBackupDataDialog() },
                onRestoreDataClick = { showRestoreDataDialog() },
                onClearFoodLogClick = { showClearFoodLogDialog() },
                onDeleteAccountClick = { showDeleteAccountDialog() },
                onAboutAppClick = { showAboutDialog() },
                onPrivacyPolicyClick = { showPrivacyPolicyDialog() },
                onHelpFaqClick = { showHelpFaqDialog() }
            )
        }
    }

    
    private fun loadSettings() {
        val notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
        val breakfastTime = sharedPreferences.getString(KEY_BREAKFAST_TIME, "08:00 AM") ?: "08:00 AM"
        val lunchTime = sharedPreferences.getString(KEY_LUNCH_TIME, "12:00 PM") ?: "12:00 PM"
        val dinnerTime = sharedPreferences.getString(KEY_DINNER_TIME, "07:00 PM") ?: "07:00 PM"
        val unitSystem = sharedPreferences.getString(KEY_UNIT_SYSTEM, "metric")
        val unitSystemDisplay = if (unitSystem == "metric") "Metrik (kg, cm)" else "Imperial (lbs, inci)"
        val darkModeEnabled = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        
        val appVersion = try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            "Versi ${pInfo.versionName}"
        } catch (e: Exception) {
            "Versi 1.0.0"
        }
        
        screenState = SettingsScreenState(
            notificationsEnabled = notificationsEnabled,
            breakfastTime = breakfastTime,
            lunchTime = lunchTime,
            dinnerTime = dinnerTime,
            unitSystem = unitSystemDisplay,
            darkModeEnabled = darkModeEnabled,
            appVersion = appVersion
        )
    }
    
    private fun handleNavigation(navItem: NavItem) {
        when (navItem) {
            NavItem.HOME -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            }
            NavItem.FOOD_LOG -> {
                startActivity(Intent(this, FoodLogActivity::class.java).putExtra("USERNAME", username))
                finish()
            }
            NavItem.STATS -> {
                startActivity(Intent(this, StatisticsActivity::class.java).putExtra("USERNAME", username))
                finish()
            }
            NavItem.SETTINGS -> { /* Already on settings */ }
        }
    }
    
    private fun handleNotificationToggle(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        screenState = screenState.copy(notificationsEnabled = enabled)
        if (enabled) {
            Toast.makeText(this, "Notifikasi diaktifkan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifikasi dinonaktifkan", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun handleDarkModeToggle(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        screenState = screenState.copy(darkModeEnabled = enabled)
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun showTimePickerDialog(mealType: String, prefKey: String, onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentTime = when (prefKey) {
            KEY_BREAKFAST_TIME -> screenState.breakfastTime
            KEY_LUNCH_TIME -> screenState.lunchTime
            KEY_DINNER_TIME -> screenState.dinnerTime
            else -> "08:00 AM"
        }
        
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
            
            sharedPreferences.edit().putString(prefKey, timeString).apply()
            onTimeSelected(timeString)
            
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
                screenState = screenState.copy(unitSystem = options[which])
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