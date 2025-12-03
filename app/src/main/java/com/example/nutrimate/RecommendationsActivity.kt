package com.example.nutrimate

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.data.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RecommendationsActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var currentUsername: String = ""
    private val localeID = Locale.forLanguageTag("id-ID")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", localeID)

    // Views
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tvSubtitle: TextView
    
    // Calorie Budget
    private lateinit var tvConsumedCalories: TextView
    private lateinit var tvTargetCalories: TextView
    private lateinit var tvRemainingCalories: TextView
    private lateinit var pbCalorieBudget: ProgressBar

    // Condition Advice
    private lateinit var cvConditionAdvice: CardView
    private lateinit var tvConditionAdvice: TextView

    // Nutritionist Tips
    private lateinit var tvNutritionistTip: TextView
    private lateinit var btnNextTip: Button
    private var currentTipIndex = 0

    // Foods to Avoid/Consume
    private lateinit var tvFoodsToAvoid: TextView
    private lateinit var tvFoodsToConsume: TextView

    // Personalized Meals
    private lateinit var tvBreakfastSuggestion: TextView
    private lateinit var tvLunchSuggestion: TextView
    private lateinit var tvDinnerSuggestion: TextView
    private lateinit var tvSnackSuggestion: TextView

    // Meal Plan
    private lateinit var llMealPlanDays: LinearLayout

    // Recipes
    private lateinit var llRecipes: LinearLayout

    // Data
    private var userConditions: List<String> = emptyList()
    private var userAllergies: List<String> = emptyList()
    private var dietGoal: String = "Maintain"
    private var remainingCalories: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendations)

        database = AppDatabase.getDatabase(this)

        val username = intent.getStringExtra("USERNAME")
        if (username.isNullOrEmpty()) {
            finish()
            return
        }
        currentUsername = username

        initViews()
        setupListeners()
        loadRecommendations()
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        tvSubtitle = findViewById(R.id.tvSubtitle)

        // Calorie Budget
        tvConsumedCalories = findViewById(R.id.tvConsumedCalories)
        tvTargetCalories = findViewById(R.id.tvTargetCalories)
        tvRemainingCalories = findViewById(R.id.tvRemainingCalories)
        pbCalorieBudget = findViewById(R.id.pbCalorieBudget)

        // Condition Advice
        cvConditionAdvice = findViewById(R.id.cvConditionAdvice)
        tvConditionAdvice = findViewById(R.id.tvConditionAdvice)

        // Nutritionist Tips
        tvNutritionistTip = findViewById(R.id.tvNutritionistTip)
        btnNextTip = findViewById(R.id.btnNextTip)

        // Foods
        tvFoodsToAvoid = findViewById(R.id.tvFoodsToAvoid)
        tvFoodsToConsume = findViewById(R.id.tvFoodsToConsume)

        // Meals
        tvBreakfastSuggestion = findViewById(R.id.tvBreakfastSuggestion)
        tvLunchSuggestion = findViewById(R.id.tvLunchSuggestion)
        tvDinnerSuggestion = findViewById(R.id.tvDinnerSuggestion)
        tvSnackSuggestion = findViewById(R.id.tvSnackSuggestion)

        // Meal Plan & Recipes
        llMealPlanDays = findViewById(R.id.llMealPlanDays)
        llRecipes = findViewById(R.id.llRecipes)
    }

    private fun setupListeners() {
        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    finish()
                    true
                }
                R.id.nav_food_log -> {
                    val intent = Intent(this, FoodLogActivity::class.java)
                    intent.putExtra("USERNAME", currentUsername)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_stats -> {
                    val intent = Intent(this, StatisticsActivity::class.java)
                    intent.putExtra("USERNAME", currentUsername)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        // Next Tip Button
        btnNextTip.setOnClickListener {
            currentTipIndex = (currentTipIndex + 1) % getNutritionistTips().size
            tvNutritionistTip.text = getNutritionistTips()[currentTipIndex]
        }
    }

    private fun loadRecommendations() {
        lifecycleScope.launch {
            val user = database.userDao().getUserByUsername(currentUsername)
            if (user == null) {
                finish()
                return@launch
            }

            // Parse user data
            userConditions = user.medicalConditions.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            userAllergies = user.allergies.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            dietGoal = user.dietGoal

            // Calculate calorie budget
            val targetCalories = user.dailyCalorieTarget
            val dateStr = dateFormat.format(Date())
            val logs = database.foodDao().getFoodLogsByDate(currentUsername, dateStr)

            var consumedCalories = 0f
            for (log in logs) {
                val food = database.foodDao().getFoodById(log.foodId)
                if (food != null) {
                    consumedCalories += food.calories * log.servingQty
                }
            }

            remainingCalories = (targetCalories - consumedCalories).toInt().coerceAtLeast(0)

            // Update UI on main thread
            runOnUiThread {
                updateCalorieBudget(consumedCalories.toInt(), targetCalories, remainingCalories)
                updateConditionAdvice(user)
                updateNutritionistTips()
                updateFoodsToAvoid()
                updateFoodsToConsume()
                updatePersonalizedMeals()
                updateMealPlan()
                updateRecipeSuggestions()
            }
        }
    }

    private fun updateCalorieBudget(consumed: Int, target: Int, remaining: Int) {
        tvConsumedCalories.text = consumed.toString()
        tvTargetCalories.text = target.toString()
        tvRemainingCalories.text = remaining.toString()

        val percentage = if (target > 0) (consumed * 100 / target) else 0
        pbCalorieBudget.progress = percentage.coerceAtMost(100)

        // Color based on remaining
        when {
            remaining <= 0 -> {
                tvRemainingCalories.setTextColor(Color.parseColor("#C62828")) // Red - over budget
            }
            remaining < target * 0.2 -> {
                tvRemainingCalories.setTextColor(Color.parseColor("#FF8F00")) // Orange - getting close
            }
            else -> {
                tvRemainingCalories.setTextColor(Color.parseColor("#4CAF50")) // Green - good
            }
        }
    }

    private fun updateConditionAdvice(user: User) {
        val adviceList = mutableListOf<String>()

        if (userConditions.contains("Diabetes")) {
            adviceList.add("🩺 Diabetes: Pantau asupan karbohidrat. Targetkan karbohidrat kompleks dengan indeks glikemik rendah. Batasi gula hingga di bawah 25g/hari. Makan teratur dan seimbang untuk menjaga gula darah stabil.")
        }

        if (userConditions.contains("Hypertension")) {
            adviceList.add("💓 Hipertensi: Kurangi asupan natrium hingga kurang dari 2.300mg/hari. Fokus pada makanan kaya kalium seperti pisang, bayam, dan ubi jalar. Ikuti prinsip diet DASH.")
        }

        if (userConditions.contains("Cholesterol")) {
            adviceList.add("🫀 Kolesterol Tinggi: Batasi lemak jenuh hingga kurang dari 13g/hari. Tingkatkan asupan serat. Pilih protein tanpa lemak dan hindari makanan gorengan. Sertakan asam lemak omega-3 dari ikan.")
        }

        if (userConditions.contains("Gastritis")) {
            adviceList.add("🍃 Maag: Hindari makanan pedas, asam, dan gorengan. Makan dalam porsi kecil namun sering. Jangan makan larut malam. Hindari alkohol dan kafein. Pertimbangkan makanan kaya probiotik.")
        }

        // Diet goal specific advice
        when (dietGoal) {
            "Lose Weight" -> adviceList.add("⚖️ Penurunan Berat Badan: Buat defisit kalori 500-750 kkal/hari untuk penurunan berat badan yang sehat. Fokus pada protein untuk mempertahankan massa otot. Tetap terhidrasi dan hindari kalori cair.")
            "Gain Weight" -> adviceList.add("💪 Peningkatan Berat Badan: Targetkan surplus kalori 300-500 kkal/hari. Sertakan makanan kaya protein dan lemak sehat. Makan lebih sering dan sertakan makanan bergizi padat kalori.")
        }

        // Allergies
        if (userAllergies.isNotEmpty()) {
            adviceList.add("⚠️ Alergi: Ingatlah untuk menghindari makanan yang mengandung: ${userAllergies.joinToString(", ")}")
        }

        if (adviceList.isEmpty()) {
            tvConditionAdvice.text = "Anda dalam keadaan sehat! Tetap pertahankan pola makan seimbang dengan banyak sayuran, protein tanpa lemak, biji-bijian utuh, dan lemak sehat."
        } else {
            tvConditionAdvice.text = adviceList.joinToString("\n\n")
        }
    }

    private fun getNutritionistTips(): List<String> {
        val generalTips = listOf(
            "💧 Hidrasi adalah kunci! Minum setidaknya 8 gelas (2L) air setiap hari. Awali hari Anda dengan segelas air sebelum sarapan.",
            "🥗 Isi setengah piring Anda dengan sayuran berwarna setiap kali makan. Warna berbeda berarti nutrisi berbeda!",
            "🍳 Jangan lewatkan sarapan! Sarapan kaya protein membantu mengendalikan rasa lapar dan menjaga tingkat energi sepanjang hari.",
            "🕐 Praktikkan makan dengan sadar. Makan perlahan, kunyah dengan saksama, dan hindari gangguan seperti TV atau ponsel saat makan.",
            "🌾 Pilih biji-bijian utuh daripada biji-bijian olahan. Mereka menyediakan lebih banyak serat, vitamin, dan membuat Anda kenyang lebih lama.",
            "🥜 Sertakan lemak sehat dalam diet Anda dari sumber seperti alpukat, kacang-kacangan, minyak zaitun, dan ikan berlemak.",
            "🍎 Makan buah-buahan alih-alih minum jus buah. Buah utuh mengandung serat yang memperlambat penyerapan gula.",
            "🧂 Baca label nutrisi! Perhatikan ukuran porsi, kandungan natrium, dan gula tambahan.",
            "🍖 Pilih protein tanpa lemak seperti dada ayam, ikan, kacang-kacangan, dan tahu. Batasi daging merah hingga 2-3 kali per minggu.",
            "🥬 Makanlah berbagai warna! Sayuran berwarna berbeda menyediakan antioksidan dan fitonutrien yang berbeda.",
            "⏰ Cobalah makan malam setidaknya 2-3 jam sebelum tidur untuk meningkatkan pencernaan dan kualitas tidur.",
            "🍫 Boleh sesekali makan camilan! Targetkan aturan 80/20 - makan makanan bergizi 80% dari waktu."
        )

        val conditionSpecificTips = mutableListOf<String>()

        if (userConditions.contains("Diabetes")) {
            conditionSpecificTips.addAll(listOf(
                "🩺 Pasangkan karbohidrat dengan protein atau lemak untuk memperlambat penyerapan glukosa dan mencegah lonjakan gula darah.",
                "🩺 Pilih makanan tinggi serat seperti oatmeal, kacang-kacangan, dan sayuran untuk membantu mengelola kadar gula darah.",
                "🩺 Pantau ukuran porsi makanan bertepung. Seporsi nasi atau pasta harus seukuran kepalan tangan Anda."
            ))
        }

        if (userConditions.contains("Hypertension")) {
            conditionSpecificTips.addAll(listOf(
                "💓 Gunakan rempah-rempah alih-alih garam untuk membumbui makanan Anda. Coba bawang putih, lemon, atau rempah segar.",
                "💓 Sertakan makanan kaya kalium seperti pisang, jeruk, dan sayuran hijau untuk membantu menurunkan tekanan darah.",
                "💓 Batasi makanan olahan dan kemasan yang biasanya tinggi natrium."
            ))
        }

        return generalTips + conditionSpecificTips
    }

    private fun updateNutritionistTips() {
        val tips = getNutritionistTips()
        tvNutritionistTip.text = tips[currentTipIndex % tips.size]
    }

    private fun updateFoodsToAvoid() {
        val avoidList = mutableListOf<String>()

        // Based on conditions
        if (userConditions.contains("Diabetes")) {
            avoidList.addAll(listOf(
                "• Minuman manis (soda, teh manis, minuman energi)",
                "• Roti putih, nasi putih, pasta olahan",
                "• Permen, kue kering, dan makanan penutup dengan gula tambahan",
                "• Jus buah dan buah kering (tinggi gula)"
            ))
        }

        if (userConditions.contains("Hypertension")) {
            avoidList.addAll(listOf(
                "• Makanan tinggi natrium (acar, kecap asin, daging olahan)",
                "• Sup kalengan dan makanan olahan",
                "• Makanan cepat saji dan restoran",
                "• Camilan asin (keripik, pretzel, kacang asin)"
            ))
        }

        if (userConditions.contains("Cholesterol")) {
            avoidList.addAll(listOf(
                "• Makanan gorengan dan lemak trans",
                "• Produk susu penuh lemak",
                "• Potongan daging merah berlemak",
                "• Kue kering dengan mentega/shortening"
            ))
        }

        if (userConditions.contains("Gastritis")) {
            avoidList.addAll(listOf(
                "• Makanan pedas (cabai, lada, kari)",
                "• Buah jeruk dan makanan asam",
                "• Kopi dan minuman berkafein",
                "• Alkohol dan minuman berkarbonasi",
                "• Makanan gorengan dan berlemak"
            ))
        }

        // Based on allergies
        userAllergies.forEach { allergy ->
            when (allergy.lowercase()) {
                "gluten" -> avoidList.add("• Gandum, barley, gandum hitam, dan produk mengandung gluten")
                "dairy", "lactose" -> avoidList.add("• Susu, keju, yogurt, dan produk susu")
                "nuts" -> avoidList.add("• Semua kacang pohon dan produk mengandung kacang")
                "peanuts" -> avoidList.add("• Kacang tanah dan produk mengandung kacang tanah")
                "eggs" -> avoidList.add("• Telur dan produk mengandung telur")
                "shellfish" -> avoidList.add("• Udang, kepiting, lobster, dan kerang lainnya")
                "soy" -> avoidList.add("• Produk kedelai termasuk tahu, kecap asin, edamame")
            }
        }

        // Diet goal specific
        when (dietGoal) {
            "Lose Weight" -> avoidList.addAll(listOf(
                "• Minuman berkalori tinggi dan alkohol",
                "• Makanan gorengan dan minyak berlebih",
                "• Porsi besar makanan bertepung"
            ))
        }

        if (avoidList.isEmpty()) {
            tvFoodsToAvoid.text = "Tidak ada makanan khusus yang harus dihindari berdasarkan profil Anda.\n\nSaran umum:\n• Batasi makanan olahan\n• Kurangi asupan gula tambahan\n• Hindari alkohol berlebihan"
        } else {
            tvFoodsToAvoid.text = avoidList.distinct().joinToString("\n")
        }
    }

    private fun updateFoodsToConsume() {
        val consumeList = mutableListOf<String>()

        // General healthy foods
        consumeList.addAll(listOf(
            "• Sayuran hijau (bayam, kale, selada)",
            "• Sayuran berwarna (brokoli, wortel, paprika)",
            "• Buah segar (beri, apel, pisang)"
        ))

        // Based on conditions
        if (userConditions.contains("Diabetes")) {
            consumeList.addAll(listOf(
                "• Makanan glikemik rendah (oatmeal, ubi jalar)",
                "• Sayuran tinggi serat (brokoli, buncis)",
                "• Protein tanpa lemak (ayam, ikan, tahu)"
            ))
        }

        if (userConditions.contains("Hypertension")) {
            consumeList.addAll(listOf(
                "• Makanan kaya kalium (pisang, bayam, alpukat)",
                "• Bawang putih dan rempah untuk perasa",
                "• Produk susu rendah lemak"
            ))
        }

        if (userConditions.contains("Cholesterol")) {
            consumeList.addAll(listOf(
                "• Ikan berminyak (salmon, makarel) - omega 3",
                "• Oat dan barley (serat beta-glukan)",
                "• Kacang-kacangan (almond, kenari) dalam jumlah sedang"
            ))
        }

        if (userConditions.contains("Gastritis")) {
            consumeList.addAll(listOf(
                "• Makanan probiotik (yogurt, kefir)",
                "• Protein tanpa lemak (ayam, ikan)",
                "• Sayuran matang (tidak asam)"
            ))
        }

        // Diet goal specific
        when (dietGoal) {
            "Lose Weight" -> consumeList.addAll(listOf(
                "• Makanan tinggi protein untuk rasa kenyang",
                "• Sayuran kaya air (mentimun, seledri)",
                "• Biji-bijian utuh dalam porsi sedang"
            ))
            "Gain Weight" -> consumeList.addAll(listOf(
                "• Makanan sehat padat kalori (kacang-kacangan, alpukat)",
                "• Protein shake dan smoothie",
                "• Pasta gandum utuh dan nasi"
            ))
        }

        tvFoodsToConsume.text = consumeList.distinct().joinToString("\n")
    }

    private fun updatePersonalizedMeals() {
        // Calculate suggested calories per meal based on remaining
        val breakfastCal = (remainingCalories * 0.30).toInt()
        val lunchCal = (remainingCalories * 0.35).toInt()
        val dinnerCal = (remainingCalories * 0.25).toInt()
        val snackCal = (remainingCalories * 0.10).toInt()

        // Breakfast suggestions based on conditions and goal
        val breakfastOptions = getBreakfastSuggestions()
        tvBreakfastSuggestion.text = "$breakfastOptions\n\n💡 Disarankan: ~$breakfastCal kkal"

        // Lunch suggestions
        val lunchOptions = getLunchSuggestions()
        tvLunchSuggestion.text = "$lunchOptions\n\n💡 Disarankan: ~$lunchCal kkal"

        // Dinner suggestions
        val dinnerOptions = getDinnerSuggestions()
        tvDinnerSuggestion.text = "$dinnerOptions\n\n💡 Disarankan: ~$dinnerCal kkal"

        // Snack suggestions
        val snackOptions = getSnackSuggestions()
        tvSnackSuggestion.text = "$snackOptions\n\n💡 Disarankan: ~$snackCal kkal"
    }

    private fun getBreakfastSuggestions(): String {
        val suggestions = mutableListOf<String>()

        when {
            userConditions.contains("Diabetes") -> {
                suggestions.addAll(listOf(
                    "• Oatmeal dengan kayu manis dan almond",
                    "• Telur orak-arik dengan sayuran",
                    "• Yogurt Yunani dengan beri (tanpa gula)"
                ))
            }
            userConditions.contains("Gastritis") -> {
                suggestions.addAll(listOf(
                    "• Oatmeal dengan pisang",
                    "• Roti gandum utuh dengan alpukat",
                    "• Smoothie dengan buah tidak asam"
                ))
            }
            dietGoal == "Lose Weight" -> {
                suggestions.addAll(listOf(
                    "• Omelet putih telur dengan bayam",
                    "• Yogurt Yunani dengan sedikit beri",
                    "• Smoothie protein dengan sayuran hijau"
                ))
            }
            dietGoal == "Gain Weight" -> {
                suggestions.addAll(listOf(
                    "• Telur utuh dengan roti bakar alpukat",
                    "• Oatmeal dengan selai kacang dan pisang",
                    "• Pancake protein dengan kacang-kacangan"
                ))
            }
            else -> {
                suggestions.addAll(listOf(
                    "• Roti gandum utuh dengan telur",
                    "• Oatmeal dengan buah dan kacang",
                    "• Parfait yogurt dengan granola"
                ))
            }
        }

        return suggestions.joinToString("\n")
    }

    private fun getLunchSuggestions(): String {
        val suggestions = mutableListOf<String>()

        when {
            userConditions.contains("Hypertension") -> {
                suggestions.addAll(listOf(
                    "• Salad ayam panggang (dressing tanpa garam)",
                    "• Tumis sayuran dengan nasi merah",
                    "• Sup lentil dengan rempah-rempah"
                ))
            }
            userConditions.contains("Cholesterol") -> {
                suggestions.addAll(listOf(
                    "• Salmon panggang dengan sayuran",
                    "• Salad quinoa dengan buncis",
                    "• Wrap dada kalkun dengan sayuran hijau"
                ))
            }
            dietGoal == "Lose Weight" -> {
                suggestions.addAll(listOf(
                    "• Salad besar dengan ayam panggang",
                    "• Sup sayuran dengan protein tanpa lemak",
                    "• Ikan panggang dengan sayuran kukus"
                ))
            }
            dietGoal == "Gain Weight" -> {
                suggestions.addAll(listOf(
                    "• Dada ayam dengan nasi dan alpukat",
                    "• Pasta dengan saus daging tanpa lemak",
                    "• Rice bowl dengan salmon dan sayuran"
                ))
            }
            else -> {
                suggestions.addAll(listOf(
                    "• Ayam panggang dengan sayuran campur",
                    "• Rice bowl merah dengan kacang-kacangan",
                    "• Sandwich gandum utuh dengan daging tanpa lemak"
                ))
            }
        }

        return suggestions.joinToString("\n")
    }

    private fun getDinnerSuggestions(): String {
        val suggestions = mutableListOf<String>()

        when {
            userConditions.contains("Gastritis") -> {
                suggestions.addAll(listOf(
                    "• Ayam panggang dengan kentang tumbuk",
                    "• Ikan kukus dengan nasi",
                    "• Sup sayuran ringan dengan roti"
                ))
            }
            dietGoal == "Lose Weight" -> {
                suggestions.addAll(listOf(
                    "• Ikan panggang dengan sayuran panggang",
                    "• Tumis ayam dengan sedikit minyak",
                    "• Kari sayuran dengan porsi nasi kecil"
                ))
            }
            dietGoal == "Gain Weight" -> {
                suggestions.addAll(listOf(
                    "• Steak dengan ubi jalar",
                    "• Ayam dengan pasta dan sayuran",
                    "• Nasi dengan ikan panggang dan salad"
                ))
            }
            else -> {
                suggestions.addAll(listOf(
                    "• Salmon panggang dengan asparagus",
                    "• Dada ayam dengan quinoa",
                    "• Tumis sayuran dengan tahu"
                ))
            }
        }

        return suggestions.joinToString("\n")
    }

    private fun getSnackSuggestions(): String {
        val suggestions = mutableListOf<String>()

        when {
            userConditions.contains("Diabetes") -> {
                suggestions.addAll(listOf(
                    "• Segenggam almond (10-15)",
                    "• Seledri dengan selai kacang",
                    "• Telur rebus"
                ))
            }
            dietGoal == "Lose Weight" -> {
                suggestions.addAll(listOf(
                    "• Sayuran segar dengan hummus",
                    "• Irisan apel",
                    "• Yogurt Yunani (tawar)"
                ))
            }
            dietGoal == "Gain Weight" -> {
                suggestions.addAll(listOf(
                    "• Trail mix dengan buah kering",
                    "• Smoothie pisang selai kacang",
                    "• Keju dengan biskuit gandum utuh"
                ))
            }
            else -> {
                suggestions.addAll(listOf(
                    "• Buah segar",
                    "• Kacang campur (segenggam kecil)",
                    "• Yogurt dengan madu"
                ))
            }
        }

        return suggestions.joinToString("\n")
    }

    private fun updateMealPlan() {
        llMealPlanDays.removeAllViews()

        val daysOfWeek = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
        val mealPlans = generateWeeklyMealPlan()

        daysOfWeek.forEachIndexed { index, day ->
            val dayLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 12, 12, 12)
                background = resources.getDrawable(R.drawable.rounded_background, null)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8)
                }
            }

            // Day header
            val dayTitle = TextView(this).apply {
                text = "📅 $day"
                setTypeface(null, Typeface.BOLD)
                textSize = 14f
                setTextColor(Color.parseColor("#7B1FA2"))
            }
            dayLayout.addView(dayTitle)

            // Meals for this day
            val mealsText = TextView(this).apply {
                text = mealPlans[index]
                textSize = 12f
                setTextColor(Color.parseColor("#424242"))
                setPadding(0, 8, 0, 0)
            }
            dayLayout.addView(mealsText)

            llMealPlanDays.addView(dayLayout)
        }
    }

    private fun generateWeeklyMealPlan(): List<String> {
        val basePlans = listOf(
            "🌅 Oatmeal + beri | 🌤️ Salad ayam panggang | 🌙 Salmon + sayuran",
            "🌅 Telur + roti gandum | 🌤️ Quinoa bowl + buncis | 🌙 Tumis tahu + nasi",
            "🌅 Yogurt Yunani + granola | 🌤️ Wrap kalkun + salad | 🌙 Ikan panggang + kentang",
            "🌅 Smoothie bowl | 🌤️ Sup lentil + roti | 🌙 Ayam panggang + sayuran",
            "🌅 Roti bakar alpukat + telur | 🌤️ Rice bowl + kacang | 🌙 Pasta + daging tanpa lemak",
            "🌅 Pancake + buah | 🌤️ Sandwich ayam | 🌙 Pizza buatan sendiri (sayur)",
            "🌅 Sarapan lengkap | 🌤️ Ikan panggang + salad | 🌙 Sup ringan + roti"
        )

        // Customize based on conditions (simplified)
        return if (userConditions.contains("Diabetes")) {
            listOf(
                "🌅 Oatmeal + kacang | 🌤️ Ayam panggang + sayuran | 🌙 Ikan + buncis",
                "🌅 Telur + alpukat | 🌤️ Salad + buncis | 🌙 Tumis tahu",
                "🌅 Yogurt Yunani + biji-bijian | 🌤️ Sup lentil | 🌙 Ayam panggang + brokoli",
                "🌅 Omelet sayur | 🌤️ Kalkun + salad | 🌙 Salmon panggang + asparagus",
                "🌅 Puding chia | 🌤️ Quinoa + sayuran | 🌙 Daging sapi tanpa lemak + jamur",
                "🌅 Keju cottage + beri | 🌤️ Wrap ayam (rendah karbo) | 🌙 Ikan + kembang kol",
                "🌅 Smoothie (tanpa gula) | 🌤️ Salad kacang | 🌙 Protein ringan + sayuran"
            )
        } else {
            basePlans
        }
    }

    private fun updateRecipeSuggestions() {
        llRecipes.removeAllViews()

        val recipes = getRecipeList()

        recipes.forEach { recipe ->
            val recipeCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 12, 16, 12)
                background = resources.getDrawable(R.drawable.rounded_background, null)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 12)
                }
            }

            val titleText = TextView(this).apply {
                text = recipe.first
                setTypeface(null, Typeface.BOLD)
                textSize = 14f
                setTextColor(Color.parseColor("#E65100"))
            }
            recipeCard.addView(titleText)

            val descText = TextView(this).apply {
                text = recipe.second
                textSize = 12f
                setTextColor(Color.parseColor("#5D4037"))
                setPadding(0, 4, 0, 0)
            }
            recipeCard.addView(descText)

            llRecipes.addView(recipeCard)
        }
    }

    private fun getRecipeList(): List<Pair<String, String>> {
        val recipes = mutableListOf<Pair<String, String>>()

        // Base healthy recipes
        recipes.add(Pair(
            "🥗 Salad Mediterania",
            "Sayuran hijau campur, tomat ceri, mentimun, zaitun, keju feta dengan dressing minyak zaitun. ~250 kkal"
        ))

        recipes.add(Pair(
            "🍳 Omelet Sayuran",
            "3 telur dengan bayam, jamur, dan paprika. Sajikan dengan roti gandum utuh. ~300 kkal"
        ))

        // Condition specific
        if (userConditions.contains("Diabetes")) {
            recipes.add(Pair(
                "🥣 Overnight Oats (Ramah Diabetes)",
                "Oat gulung, biji chia, susu almond tanpa pemanis, kayu manis, dan sedikit beri. Sarapan GI rendah. ~280 kkal"
            ))
        }

        if (userConditions.contains("Hypertension")) {
            recipes.add(Pair(
                "🐟 Salmon Berbalut Rempah",
                "Fillet salmon dengan rempah segar, lemon, dan bawang putih (tanpa garam). Sajikan dengan sayuran kukus. ~350 kkal"
            ))
        }

        if (userConditions.contains("Cholesterol")) {
            recipes.add(Pair(
                "🥑 Roti Bakar Alpukat dengan Biji-bijian",
                "Roti gandum utuh dengan alpukat tumbuk, biji rami, dan telur rebus. Lemak sehat jantung. ~320 kkal"
            ))
        }

        if (userConditions.contains("Gastritis")) {
            recipes.add(Pair(
                "🍲 Sup Ayam Lembut",
                "Ayam empuk, wortel, kentang dalam kaldu ringan. Mudah dicerna. ~200 kkal"
            ))
        }

        // Diet goal specific
        if (dietGoal == "Lose Weight") {
            recipes.add(Pair(
                "🥒 Mie Zucchini dengan Pesto",
                "Zucchini spiral dengan pesto kemangi buatan sendiri dan tomat ceri. Alternatif rendah karbohidrat. ~180 kkal"
            ))
        }

        if (dietGoal == "Gain Weight") {
            recipes.add(Pair(
                "🍌 Smoothie Bowl Protein",
                "Pisang, selai kacang, bubuk protein, oat, dan susu almond. Taburi dengan granola dan kacang. ~500 kkal"
            ))
        }

        // General healthy recipes
        recipes.add(Pair(
            "🍗 Ayam Panggang & Quinoa",
            "Dada ayam berbumbu dengan quinoa empuk dan sayuran panggang. Seimbang dan bergizi. ~400 kkal"
        ))

        recipes.add(Pair(
            "🥙 Wrap Sayuran Hummus",
            "Wrap gandum utuh dengan hummus, sayuran hijau campur, mentimun, tomat, dan sayuran panggang. ~350 kkal"
        ))

        return recipes
    }
}