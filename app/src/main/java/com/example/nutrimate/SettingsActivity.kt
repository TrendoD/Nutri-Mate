package com.example.nutrimate

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.ui.main.NavItem
import com.example.nutrimate.ui.settings.SettingsScreen
import com.example.nutrimate.ui.settings.SettingsScreenState
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private var username: String = ""
    
    // State for Compose UI
    private var screenState by mutableStateOf(SettingsScreenState())
    
    companion object {
        const val PREFS_NAME = "NutriMateSettings"
        
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
        
        setContent {
            SettingsScreen(
                state = screenState,
                selectedNavItem = NavItem.SETTINGS,
                onNavItemClick = { navItem -> handleNavigation(navItem) },
                onMyProfileClick = {
                    startActivity(Intent(this, ProfileActivity::class.java).putExtra("USERNAME", username))
                },
                onClearFoodLogClick = { showClearFoodLogDialog() },
                onDeleteAccountClick = { showDeleteAccountDialog() }
            )
        }
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
}