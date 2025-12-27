/**
 * SplashActivity.kt
 *
 * File ini berisi splash screen untuk aplikasi Nutri-Mate.
 * Splash screen menampilkan logo dan tagline aplikasi dengan animasi fade-in,
 * kemudian secara otomatis berpindah ke MainActivity (jika user sudah login)
 * atau LoginActivity (jika user belum login).
 *
 * Dibangun sepenuhnya dengan Jetpack Compose.
 */

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

/**
 * SplashActivity - Titik masuk awal aplikasi.
 *
 * Menampilkan splash screen dengan animasi, kemudian berpindah ke:
 * - MainActivity: Jika user sudah login
 * - LoginActivity: Jika user belum login
 *
 * Menggunakan @SuppressLint("CustomSplashScreen") karena kita menggunakan
 * desain splash screen kustom, bukan Android 12+ SplashScreen API.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    companion object {
        // --------------- KONSTANTA WAKTU ---------------
        /** Durasi tampilan splash screen sebelum fade-out (dalam milidetik) */
        private const val SPLASH_DELAY_MS = 2500L

        /** Durasi animasi fade-in untuk logo */
        private const val LOGO_FADE_DURATION_MS = 800

        /** Jeda sebelum teks mulai fade-in (setelah logo mulai) */
        private const val TEXT_FADE_DELAY_MS = 200L

        /** Durasi animasi fade-in untuk teks */
        private const val TEXT_FADE_DURATION_MS = 600

        /** Durasi animasi fade-out untuk seluruh layar */
        private const val SCREEN_FADE_OUT_DURATION_MS = 500

        // --------------- KUNCI SHARED PREFERENCES ---------------
        /** Nama file SharedPreferences untuk pengaturan aplikasi */
        private const val PREF_NAME = "NutriMatePrefs"

        /** Kunci untuk menyimpan/mengambil username yang sedang login */
        private const val KEY_LOGGED_IN_USER = "logged_in_user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set konten Composable untuk activity ini
        setContent {
            SplashScreen(
                splashDelayMs = SPLASH_DELAY_MS,
                logoFadeDurationMs = LOGO_FADE_DURATION_MS,
                textFadeDelayMs = TEXT_FADE_DELAY_MS,
                textFadeDurationMs = TEXT_FADE_DURATION_MS,
                screenFadeOutDurationMs = SCREEN_FADE_OUT_DURATION_MS,
                onSplashFinished = { navigateToNextScreen() }
            )
        }
    }

    /**
     * Menentukan halaman tujuan setelah splash screen dan melakukan navigasi.
     *
     * Logika navigasi:
     * - Jika user sudah login (username ada di SharedPreferences) -> MainActivity
     * - Jika user belum login -> LoginActivity
     */
    private fun navigateToNextScreen() {
        // Cek apakah user sudah login dengan membaca SharedPreferences
        val sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val loggedInUser = sharedPreferences.getString(KEY_LOGGED_IN_USER, null)

        // Buat intent yang sesuai berdasarkan status login
        val intent = if (loggedInUser != null) {
            // User sudah login -> pergi ke halaman utama dengan username
            Intent(this, MainActivity::class.java).apply {
                putExtra("USERNAME", loggedInUser)
            }
        } else {
            // User belum login -> pergi ke halaman login
            Intent(this, LoginActivity::class.java)
        }

        // Mulai activity berikutnya dan tutup activity ini (cegah navigasi kembali)
        startActivity(intent)
        finish()

        // Terapkan animasi transisi fade untuk perpindahan yang mulus
        applyFadeTransition()
    }

    /**
     * Menerapkan animasi transisi fade-in/fade-out antar activity.
     *
     * Menangani perbedaan versi API:
     * - API 34+ (UPSIDE_DOWN_CAKE): Menggunakan overrideActivityTransition()
     * - API 33 ke bawah: Menggunakan overridePendingTransition() yang deprecated
     */
    private fun applyFadeTransition() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // API modern untuk Android 14+
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        } else {
            // API lama untuk versi Android sebelumnya
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}

// =============================================================================
// FUNGSI COMPOSABLE
// =============================================================================

/**
 * SplashScreen - Composable utama untuk tampilan splash screen.
 *
 * Menampilkan background gradient dengan:
 * - Logo aplikasi (fade-in pertama)
 * - Nama aplikasi "Nutri-Mate" (fade-in dengan sedikit jeda)
 * - Tagline (fade-in bersamaan dengan nama aplikasi)
 * - Spinner loading di bagian bawah
 *
 * Setelah durasi splash selesai, seluruh layar fade-out dan memanggil [onSplashFinished].
 *
 * @param splashDelayMs           Waktu tampil splash sebelum fade-out (default: 2500ms)
 * @param logoFadeDurationMs      Durasi animasi fade-in logo (default: 800ms)
 * @param textFadeDelayMs         Jeda sebelum teks mulai fade-in (default: 200ms)
 * @param textFadeDurationMs      Durasi animasi fade-in teks (default: 600ms)
 * @param screenFadeOutDurationMs Durasi animasi fade-out layar (default: 500ms)
 * @param onSplashFinished        Callback yang dipanggil saat animasi splash selesai
 */
@Composable
fun SplashScreen(
    splashDelayMs: Long = 2500L,
    logoFadeDurationMs: Int = 800,
    textFadeDelayMs: Long = 200L,
    textFadeDurationMs: Int = 600,
    screenFadeOutDurationMs: Int = 500,
    onSplashFinished: () -> Unit = {}
) {
    // --------------- STATE ANIMASI ---------------
    // Nilai Animatable untuk mengontrol efek fade-in/fade-out
    val logoAlpha = remember { Animatable(0f) }     // Logo mulai tidak terlihat
    val textAlpha = remember { Animatable(0f) }     // Teks mulai tidak terlihat
    val screenAlpha = remember { Animatable(1f) }   // Layar mulai terlihat penuh

    // --------------- URUTAN ANIMASI ---------------
    // LaunchedEffect terpisah untuk menangani animasi secara paralel
    LaunchedEffect(Unit) {
        // Fase 1: Fade-in logo
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = logoFadeDurationMs)
        )
    }

    LaunchedEffect(Unit) {
        // Fase 2: Setelah jeda singkat, fade-in teks (berjalan paralel dengan logo)
        delay(textFadeDelayMs)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = textFadeDurationMs)
        )
    }

    LaunchedEffect(Unit) {
        // Fase 3: Tunggu durasi splash, lalu fade-out seluruh layar
        delay(splashDelayMs)
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = screenFadeOutDurationMs)
        )

        // Fase 4: Beritahu bahwa splash selesai -> picu navigasi
        onSplashFinished()
    }

    // --------------- WARNA BACKGROUND GRADIENT ---------------
    // Menggunakan warna tema untuk konsistensi di seluruh aplikasi
    val gradientColors = listOf(GradientStart, GradientCenter, GradientEnd)

    // --------------- TATA LETAK UI ---------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)  // Terapkan efek fade-out layar
            .background(brush = Brush.linearGradient(colors = gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        // ----- KONTEN TENGAH: Logo + Nama Aplikasi + Tagline -----
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Aplikasi
            Image(
                painter = painterResource(id = R.drawable.logo_aplikasi),
                contentDescription = "Logo NutriMate",
                modifier = Modifier
                    .size(150.dp)
                    .alpha(logoAlpha.value)  // Terapkan efek fade-in logo
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nama Aplikasi
            Text(
                text = "Nutri-Mate",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(textAlpha.value)  // Terapkan efek fade-in teks
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Makanan Sehat, Hidup Sehat",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                modifier = Modifier.alpha(textAlpha.value)  // Terapkan efek fade-in teks
            )
        }

        // ----- KONTEN BAWAH: Spinner Loading -----
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)  // Tambah padding dari tepi bawah
                .alpha(textAlpha.value)   // Fade-in bersamaan dengan teks
                .size(36.dp),
            color = Color.White,
            strokeWidth = 3.dp
        )
    }
}

// =============================================================================
// PREVIEW
// =============================================================================

/**
 * Preview untuk composable SplashScreen.
 * Menampilkan tampilan splash screen di panel Preview Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}
