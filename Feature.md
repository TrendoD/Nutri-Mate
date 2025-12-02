# Rencana Implementasi: Onboarding "NutriStart Wizard"

## 1. Tujuan Utama
Memperbaiki pengalaman pengguna baru (UX) setelah registrasi/login dengan mengganti `ProfileActivity` yang bersifat form-based menjadi alur orientasi (onboarding) yang interaktif, terpandu, dan langkah-demi-langkah.

## 2. Konsep: `OnboardingActivity`
Sebuah `Activity` baru bernama `OnboardingActivity` akan dibuat. Activity ini akan bertindak sebagai container untuk `ViewPager2`. Setiap langkah dalam proses orientasi akan menjadi sebuah `Fragment` yang ditampilkan di dalam `ViewPager2`.

**Teknologi Utama:**
- **`OnboardingActivity.kt`**: Host untuk ViewPager.
- **`ViewPager2`**: Untuk navigasi geser (swipe) antar halaman/fragment.
- **`Fragment`**: Setiap layar (Usia, Berat, Tujuan, dll.) akan menjadi `Fragment` terpisah.
- **`Shared ViewModel`**: Untuk menyimpan data sementara dari setiap fragment saat pengguna melalui alur wizard.
- **Material Design 3 Components**: Untuk UI yang modern dan interaktif (`CardView`, `Slider`, `Chip`, `NumberPicker`).

## 3. Alur Pengguna & Desain Interaksi per Halaman

Navigasi akan terjadi dari `LoginActivity`/`RegisterActivity` -> `OnboardingActivity` -> `MainActivity`.

1.  **Halaman 1: Selamat Datang & Jenis Kelamin (`GenderFragment`)**
    -   **UI:** Judul sambutan, sub-judul penjelasan, dan dua `CardView` besar untuk "Pria" dan "Wanita" dengan ikon.
    -   **Interaksi:** Mengetuk salah satu kartu akan memilih opsi tersebut, menyimpannya di `Shared ViewModel`, dan secara otomatis mengarahkan ke halaman berikutnya.

2.  **Halaman 2: Usia (`AgeFragment`)**
    -   **UI:** Pertanyaan jelas ("Berapa usia Anda?"), `NumberPicker` yang besar dan mudah digunakan.
    -   **Interaksi:** Pengguna menggeser pemilih angka. Tombol "Lanjut" akan aktif setelah nilai dipilih.

3.  **Halaman 3: Tinggi Badan (`HeightFragment`)**
    -   **UI:** Pertanyaan jelas, `Slider` horizontal yang didesain seperti penggaris, dan `TextView` besar yang menampilkan nilai cm secara real-time.
    -   **Interaksi:** Pengguna menggeser `Slider`.

4.  **Halaman 4: Berat Badan (`WeightFragment`)**
    -   **UI & Interaksi:** Sama seperti `HeightFragment`, tetapi untuk berat badan (kg).

5.  **Halaman 5: Tingkat Aktivitas (`ActivityLevelFragment`)**
    -   **UI:** Pertanyaan jelas, dan daftar `CardView` yang bisa dipilih (menggantikan `Spinner`). Setiap kartu berisi level aktivitas dan deskripsi singkat.
    -   **Interaksi:** Pengguna mengetuk satu kartu untuk memilih.

6.  **Halaman 6: Tujuan Diet (`GoalFragment`)**
    -   **UI:** Tiga `CardView` dengan ikon untuk "Turunkan Berat Badan", "Jaga Berat Badan", dan "Naikkan Berat Badan".
    -   **Interaksi:** Sama seperti `GenderFragment`, memilih satu kartu akan melanjutkan ke halaman berikutnya.

7.  **Halaman 7: Kondisi Kesehatan (`HealthFragment`)**
    -   **UI:** Judul (menekankan ini opsional), beberapa `Chip` yang bisa dipilih untuk kondisi umum, dan `EditText` untuk alergi.
    -   **Interaksi:** Pengguna mengetuk `Chip` untuk memilih/membatalkan pilihan. Ada tombol "Lewati" dan "Lanjut".

8.  **Halaman 8: Ringkasan & Selesai (`SummaryFragment`)**
    -   **UI:** Animasi, pesan konfirmasi, dan yang terpenting: Tampilan target kalori harian (`TDEE`) yang dihitung.
    -   **Interaksi:** Tombol "Mulai" akan memicu penyimpanan data dan navigasi ke `MainActivity`.

## 4. Perubahan Logika & Implementasi Teknis

1.  **Modifikasi `LoginActivity` dan `RegisterActivity`**:
    -   Setelah proses login/register berhasil, tambahkan logika untuk memeriksa profil pengguna di database.
    -   **Kondisi:** `IF user.age == 0 OR user.weight == 0 THEN`
        -   `Intent ke OnboardingActivity`
    -   **ELSE:**
        -   `Intent ke MainActivity` (perilaku untuk pengguna lama)

2.  **Buat `OnboardingActivity.kt`**:
    -   Inisialisasi `ViewPager2` dan `FragmentStateAdapter`.
    -   Sediakan `Shared ViewModel` untuk semua `Fragment` turunannya.

3.  **Buat XML Layouts**:
    -   `activity_onboarding.xml` (berisi `ViewPager2`).
    -   Layout untuk setiap `Fragment`: `fragment_gender.xml`, `fragment_age.xml`, dst.

4.  **Buat Class `Fragment`**:
    -   Buat 8 file Kotlin `Fragment` baru (mis: `GenderFragment.kt`, `AgeFragment.kt`, dll.).
    -   Setiap `Fragment` akan meng-handle UI dan interaksi untuk layarnya, dan berkomunikasi dengan `Shared ViewModel`.

5.  **Refactor Logika Kalkulasi**:
    -   Pindahkan/buat ulang fungsi kalkulasi BMR dan TDEE dari `ProfileActivity` ke dalam `Shared ViewModel` atau sebuah kelas `Utility`. Ini akan dipanggil di `SummaryFragment` sebelum menyimpan data.

6.  **Update Database**:
    -   Di `SummaryFragment`, setelah pengguna menekan "Mulai", `ViewModel` akan memanggil `UserDao` untuk meng-update data pengguna di database dengan semua informasi yang telah dikumpulkan.

7.  **Pastikan `ProfileActivity` Tetap Relevan**:
    -   `ProfileActivity` tidak dihapus, tetapi perannya berubah menjadi halaman **"Edit Profil"** yang bisa diakses dari menu navigasi oleh semua pengguna.

Dengan rencana ini, kita akan membangun alur orientasi yang solid, interaktif, dan secara signifikan meningkatkan pengalaman pengguna pertama kali.
