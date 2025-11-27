# NutriMate - Development Plan

## 🎯 Project Overview

**Nama Aplikasi:** NutriMate  
**Tujuan:** Platform mobile untuk membantu pengguna mengelola pola makan dan nutrisi, khususnya bagi yang memiliki kondisi medis tertentu.  
**Timeline:** 1 Bulan | **Team:** Solo Developer

---

## 📊 Core Features (MVP)

| Feature | Deskripsi |
|---------|-----------|
| **Simple Login** | Login dengan username/password (local storage) |
| **User Profile** | Input data diri + pilih kondisi medis |
| **Food Logging** | Catat makanan harian per kategori meal |
| **Calorie Tracking** | Hitung & tampilkan kalori + makro harian |
| **Diet Recommendations** | Saran makanan berdasarkan kondisi medis |

---

## 🏗️ Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM |
| Local DB | Room + DataStore |
| Food Data | Local JSON |

---

## 📁 Project Structure

```
app/src/main/java/com/nutrimate/
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/ (UserDao, FoodDao, FoodLogDao)
│   │   └── entity/ (UserEntity, FoodEntity, FoodLogEntity)
│   └── repository/
│       ├── UserRepository.kt
│       └── FoodRepository.kt
├── ui/
│   ├── theme/
│   ├── components/
│   ├── navigation/NavGraph.kt
│   └── screens/
│       ├── login/
│       ├── profile/
│       ├── home/
│       └── foodlog/
└── util/
```

---

## 📅 4-Week Sprint Plan

### Week 1: Setup & User Management (Day 1-7)

| Day | Tasks |
|-----|-------|
| 1-2 | Setup project, Room database, Navigation |
| 3-4 | Login screen + Register screen (local auth) |
| 5-7 | Profile setup: input data diri, pilih kondisi medis, hitung kebutuhan kalori |

**Output:** User bisa login, register, dan setup profil

---

### Week 2: Food Database & Logging (Day 8-14)

| Day | Tasks |
|-----|-------|
| 8-9 | Buat database makanan Indonesia (JSON), pre-populate ke Room |
| 10-11 | Food log screen: tampilkan makanan per meal (Breakfast/Lunch/Dinner/Snack) |
| 12-14 | Add food: search makanan, pilih porsi, simpan ke log |

**Output:** User bisa mencari dan mencatat makanan

---

### Week 3: Dashboard & Recommendations (Day 15-21)

| Day | Tasks |
|-----|-------|
| 15-17 | Home dashboard: total kalori hari ini, progress bar, breakdown makro |
| 18-21 | Recommendations: tampilkan warning jika melebihi batas, saran makanan sesuai kondisi medis |

**Output:** User bisa melihat progress nutrisi dan rekomendasi diet

---

### Week 4: Polish & Testing (Day 22-30)

| Day | Tasks |
|-----|-------|
| 22-24 | Edit profile, UI improvements |
| 25-27 | Testing, bug fixes |
| 28-30 | Final polish, dokumentasi, prepare APK |

**Output:** Aplikasi siap demo

---

## 🗃️ Database Schema

### UserEntity
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 0,
    val username: String,
    val password: String,  // hashed
    val name: String,
    val age: Int,
    val weight: Float,
    val height: Float,
    val gender: String,
    val medicalConditions: String,  // JSON: ["diabetes"]
    val dailyCalorieTarget: Int
)
```

### FoodEntity
```kotlin
@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val calories: Float,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val servingSize: Float,
    val servingUnit: String
)
```

### FoodLogEntity
```kotlin
@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val odId: Int,
    val foodId: String,
    val servingQty: Float,
    val mealType: String,
    val date: String
)
```

---

## 📱 Screen Flow

```
LOGIN ──► REGISTER (if new user)
  │
  ▼
PROFILE SETUP (first time)
  │
  ▼
HOME (Dashboard) ◄──► FOOD LOG ◄──► ADD FOOD
  │
  ▼
PROFILE (view/edit)
```

---

## 🎯 Feature Checklist

### Week 1
- [x] Project setup dengan Compose
- [x] Room database configuration
- [x] Login screen
- [x] Register screen  
- [x] Profile setup screen
- [x] Calorie calculator (BMR/TDEE)

### Week 2
- [x] Indonesian food database (50+ items)
- [x] Food search
- [x] Food log by meal type
- [x] Add food to log
- [ ] Delete food from log

### Week 3
- [x] Home dashboard UI
- [x] Daily calorie progress
- [x] Macro breakdown display
- [x] Medical-based recommendations
- [x] Warning alerts

### Week 4
- [x] Profile edit
- [x] UI polish
- [x] Testing
- [x] Bug fixes
- [x] Documentation

---

## 📝 Notes

- Gunakan local storage (Room + DataStore) untuk semua data
- Tidak perlu backend/Firebase - semua offline
- Focus pada fitur utama, skip fitur tambahan
- Test di real device secara regular

---

**Target: Aplikasi fungsional dalam 30 hari! 🚀**
**Status: COMPLETED**