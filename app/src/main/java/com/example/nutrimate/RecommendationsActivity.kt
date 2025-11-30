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
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
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
            adviceList.add("🩺 Diabetes: Monitor carbohydrate intake. Aim for complex carbs with low glycemic index. Limit sugar to under 25g/day. Eat regular, balanced meals to maintain stable blood sugar.")
        }

        if (userConditions.contains("Hypertension")) {
            adviceList.add("💓 Hypertension: Reduce sodium intake to less than 2,300mg/day. Focus on potassium-rich foods like bananas, spinach, and sweet potatoes. Follow the DASH diet principles.")
        }

        if (userConditions.contains("Cholesterol")) {
            adviceList.add("🫀 High Cholesterol: Limit saturated fats to less than 13g/day. Increase fiber intake. Choose lean proteins and avoid fried foods. Include omega-3 fatty acids from fish.")
        }

        if (userConditions.contains("Gastritis")) {
            adviceList.add("🍃 Gastritis: Avoid spicy, acidic, and fried foods. Eat smaller, more frequent meals. Don't eat late at night. Avoid alcohol and caffeine. Consider probiotic-rich foods.")
        }

        // Diet goal specific advice
        when (dietGoal) {
            "Lose Weight" -> adviceList.add("⚖️ Weight Loss: Create a calorie deficit of 500-750 kcal/day for healthy weight loss. Focus on protein to maintain muscle mass. Stay hydrated and avoid liquid calories.")
            "Gain Weight" -> adviceList.add("💪 Weight Gain: Aim for a calorie surplus of 300-500 kcal/day. Include protein-rich foods and healthy fats. Eat more frequently and include calorie-dense nutritious foods.")
        }

        // Allergies
        if (userAllergies.isNotEmpty()) {
            adviceList.add("⚠️ Allergies: Remember to avoid foods containing: ${userAllergies.joinToString(", ")}")
        }

        if (adviceList.isEmpty()) {
            tvConditionAdvice.text = "You're in good health! Keep maintaining a balanced diet with plenty of vegetables, lean proteins, whole grains, and healthy fats."
        } else {
            tvConditionAdvice.text = adviceList.joinToString("\n\n")
        }
    }

    private fun getNutritionistTips(): List<String> {
        val generalTips = listOf(
            "💧 Hydration is key! Drink at least 8 glasses (2L) of water daily. Start your day with a glass of water before breakfast.",
            "🥗 Fill half your plate with colorful vegetables at each meal. Different colors mean different nutrients!",
            "🍳 Don't skip breakfast! A protein-rich breakfast helps control hunger and maintains energy levels throughout the day.",
            "🕐 Practice mindful eating. Eat slowly, chew thoroughly, and avoid distractions like TV or phones during meals.",
            "🌾 Choose whole grains over refined grains. They provide more fiber, vitamins, and keep you fuller longer.",
            "🥜 Include healthy fats in your diet from sources like avocados, nuts, olive oil, and fatty fish.",
            "🍎 Eat fruits instead of drinking fruit juice. Whole fruits contain fiber that slows down sugar absorption.",
            "🧂 Read nutrition labels! Pay attention to serving sizes, sodium content, and added sugars.",
            "🍖 Choose lean proteins like chicken breast, fish, legumes, and tofu. Limit red meat to 2-3 times per week.",
            "🥬 Eat the rainbow! Different colored vegetables provide different antioxidants and phytonutrients.",
            "⏰ Try to eat dinner at least 2-3 hours before bedtime to improve digestion and sleep quality.",
            "🍫 It's okay to have treats occasionally! Aim for the 80/20 rule - eat nutritious foods 80% of the time."
        )

        val conditionSpecificTips = mutableListOf<String>()

        if (userConditions.contains("Diabetes")) {
            conditionSpecificTips.addAll(listOf(
                "🩺 Pair carbohydrates with protein or fat to slow glucose absorption and prevent blood sugar spikes.",
                "🩺 Choose high-fiber foods like oatmeal, beans, and vegetables to help manage blood sugar levels.",
                "🩺 Monitor portion sizes of starchy foods. A portion of rice or pasta should be about the size of your fist."
            ))
        }

        if (userConditions.contains("Hypertension")) {
            conditionSpecificTips.addAll(listOf(
                "💓 Use herbs and spices instead of salt to flavor your food. Try garlic, lemon, or fresh herbs.",
                "💓 Include potassium-rich foods like bananas, oranges, and leafy greens to help lower blood pressure.",
                "💓 Limit processed and packaged foods which are typically high in sodium."
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
                "• Sugary drinks (soda, sweetened tea, energy drinks)",
                "• White bread, white rice, refined pasta",
                "• Candy, pastries, and desserts with added sugar",
                "• Fruit juices and dried fruits (high sugar)"
            ))
        }

        if (userConditions.contains("Hypertension")) {
            avoidList.addAll(listOf(
                "• High-sodium foods (pickles, soy sauce, processed meats)",
                "• Canned soups and processed foods",
                "• Fast food and restaurant meals",
                "• Salty snacks (chips, pretzels, salted nuts)"
            ))
        }

        if (userConditions.contains("Cholesterol")) {
            avoidList.addAll(listOf(
                "• Fried foods and trans fats",
                "• Full-fat dairy products",
                "• Fatty cuts of red meat",
                "• Baked goods with butter/shortening"
            ))
        }

        if (userConditions.contains("Gastritis")) {
            avoidList.addAll(listOf(
                "• Spicy foods (chili, hot peppers, curry)",
                "• Citrus fruits and acidic foods",
                "• Coffee and caffeinated beverages",
                "• Alcohol and carbonated drinks",
                "• Fried and fatty foods"
            ))
        }

        // Based on allergies
        userAllergies.forEach { allergy ->
            when (allergy.lowercase()) {
                "gluten" -> avoidList.add("• Wheat, barley, rye, and products containing gluten")
                "dairy", "lactose" -> avoidList.add("• Milk, cheese, yogurt, and dairy products")
                "nuts" -> avoidList.add("• All tree nuts and nut-containing products")
                "peanuts" -> avoidList.add("• Peanuts and peanut-containing products")
                "eggs" -> avoidList.add("• Eggs and egg-containing products")
                "shellfish" -> avoidList.add("• Shrimp, crab, lobster, and other shellfish")
                "soy" -> avoidList.add("• Soy products including tofu, soy sauce, edamame")
            }
        }

        // Diet goal specific
        when (dietGoal) {
            "Lose Weight" -> avoidList.addAll(listOf(
                "• High-calorie beverages and alcohol",
                "• Fried foods and excessive oil",
                "• Large portions of starchy foods"
            ))
        }

        if (avoidList.isEmpty()) {
            tvFoodsToAvoid.text = "No specific foods to avoid based on your profile.\n\nGeneral advice:\n• Limit processed foods\n• Reduce added sugar intake\n• Avoid excessive alcohol"
        } else {
            tvFoodsToAvoid.text = avoidList.distinct().joinToString("\n")
        }
    }

    private fun updateFoodsToConsume() {
        val consumeList = mutableListOf<String>()

        // General healthy foods
        consumeList.addAll(listOf(
            "• Leafy greens (spinach, kale, lettuce)",
            "• Colorful vegetables (broccoli, carrots, bell peppers)",
            "• Fresh fruits (berries, apples, bananas)"
        ))

        // Based on conditions
        if (userConditions.contains("Diabetes")) {
            consumeList.addAll(listOf(
                "• Low glycemic foods (oatmeal, sweet potatoes)",
                "• High-fiber vegetables (broccoli, green beans)",
                "• Lean proteins (chicken, fish, tofu)"
            ))
        }

        if (userConditions.contains("Hypertension")) {
            consumeList.addAll(listOf(
                "• Potassium-rich foods (bananas, spinach, avocado)",
                "• Garlic and herbs for flavoring",
                "• Low-fat dairy products"
            ))
        }

        if (userConditions.contains("Cholesterol")) {
            consumeList.addAll(listOf(
                "• Oily fish (salmon, mackerel) - omega 3",
                "• Oats and barley (beta-glucan fiber)",
                "• Nuts (almonds, walnuts) in moderation"
            ))
        }

        if (userConditions.contains("Gastritis")) {
            consumeList.addAll(listOf(
                "• Probiotic foods (yogurt, kefir)",
                "• Lean proteins (chicken, fish)",
                "• Cooked vegetables (non-acidic)"
            ))
        }

        // Diet goal specific
        when (dietGoal) {
            "Lose Weight" -> consumeList.addAll(listOf(
                "• High-protein foods for satiety",
                "• Water-rich vegetables (cucumber, celery)",
                "• Whole grains in moderate portions"
            ))
            "Gain Weight" -> consumeList.addAll(listOf(
                "• Calorie-dense healthy foods (nuts, avocado)",
                "• Protein shakes and smoothies",
                "• Whole grain pasta and rice"
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
        tvBreakfastSuggestion.text = "$breakfastOptions\n\n💡 Suggested: ~$breakfastCal kcal"

        // Lunch suggestions
        val lunchOptions = getLunchSuggestions()
        tvLunchSuggestion.text = "$lunchOptions\n\n💡 Suggested: ~$lunchCal kcal"

        // Dinner suggestions
        val dinnerOptions = getDinnerSuggestions()
        tvDinnerSuggestion.text = "$dinnerOptions\n\n💡 Suggested: ~$dinnerCal kcal"

        // Snack suggestions
        val snackOptions = getSnackSuggestions()
        tvSnackSuggestion.text = "$snackOptions\n\n💡 Suggested: ~$snackCal kcal"
    }

    private fun getBreakfastSuggestions(): String {
        val suggestions = mutableListOf<String>()

        when {
            userConditions.contains("Diabetes") -> {
                suggestions.addAll(listOf(
                    "• Oatmeal with cinnamon and almonds",
                    "• Scrambled eggs with vegetables",
                    "• Greek yogurt with berries (no sugar)"
                ))
            }
            userConditions.contains("Gastritis") -> {
                suggestions.addAll(listOf(
                    "• Oatmeal with banana",
                    "• Whole grain toast with avocado",
                    "• Smoothie with non-acidic fruits"
                ))
            }
            dietGoal == "Lose Weight" -> {
                suggestions.addAll(listOf(
                    "• Egg white omelet with spinach",
                    "• Greek yogurt with a few berries",
                    "• Protein smoothie with greens"
                ))
            }
            dietGoal == "Gain Weight" -> {
                suggestions.addAll(listOf(
                    "• Whole eggs with avocado toast",
                    "• Oatmeal with peanut butter and banana",
                    "• Protein pancakes with nuts"
                ))
            }
            else -> {
                suggestions.addAll(listOf(
                    "• Whole grain toast with eggs",
                    "• Oatmeal with fruits and nuts",
                    "• Yogurt parfait with granola"
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
                    "• Grilled chicken salad (no salt dressing)",
                    "• Vegetable stir-fry with brown rice",
                    "• Lentil soup with herbs"
                ))
            }
            userConditions.contains("Cholesterol") -> {
                suggestions.addAll(listOf(
                    "• Grilled salmon with vegetables",
                    "• Quinoa salad with chickpeas",
                    "• Turkey breast wrap with greens"
                ))
            }
            dietGoal == "Lose Weight" -> {
                suggestions.addAll(listOf(
                    "• Large salad with grilled chicken",
                    "• Vegetable soup with lean protein",
                    "• Grilled fish with steamed veggies"
                ))
            }
            dietGoal == "Gain Weight" -> {
                suggestions.addAll(listOf(
                    "• Chicken breast with rice and avocado",
                    "• Pasta with lean meat sauce",
                    "• Rice bowl with salmon and vegetables"
                ))
            }
            else -> {
                suggestions.addAll(listOf(
                    "• Grilled chicken with mixed vegetables",
                    "• Brown rice bowl with beans",
                    "• Whole wheat sandwich with lean meat"
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
                    "• Baked chicken with mashed potatoes",
                    "• Steamed fish with rice",
                    "• Light vegetable soup with bread"
                ))
            }
            dietGoal == "Lose Weight" -> {
                suggestions.addAll(listOf(
                    "• Grilled fish with roasted vegetables",
                    "• Chicken stir-fry with minimal oil",
                    "• Vegetable curry with small rice portion"
                ))
            }
            dietGoal == "Gain Weight" -> {
                suggestions.addAll(listOf(
                    "• Steak with sweet potato",
                    "• Chicken with pasta and vegetables",
                    "• Rice with grilled fish and salad"
                ))
            }
            else -> {
                suggestions.addAll(listOf(
                    "• Baked salmon with asparagus",
                    "• Chicken breast with quinoa",
                    "• Vegetable stir-fry with tofu"
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
                    "• Handful of almonds (10-15)",
                    "• Celery with peanut butter",
                    "• Hard-boiled egg"
                ))
            }
            dietGoal == "Lose Weight" -> {
                suggestions.addAll(listOf(
                    "• Fresh vegetables with hummus",
                    "• Apple slices",
                    "• Greek yogurt (plain)"
                ))
            }
            dietGoal == "Gain Weight" -> {
                suggestions.addAll(listOf(
                    "• Trail mix with dried fruits",
                    "• Peanut butter banana smoothie",
                    "• Cheese with whole grain crackers"
                ))
            }
            else -> {
                suggestions.addAll(listOf(
                    "• Fresh fruits",
                    "• Mixed nuts (small handful)",
                    "• Yogurt with honey"
                ))
            }
        }

        return suggestions.joinToString("\n")
    }

    private fun updateMealPlan() {
        llMealPlanDays.removeAllViews()

        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
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
            "🌅 Oatmeal + berries | 🌤️ Grilled chicken salad | 🌙 Salmon + vegetables",
            "🌅 Eggs + whole toast | 🌤️ Quinoa bowl + chickpeas | 🌙 Stir-fry tofu + rice",
            "🌅 Greek yogurt + granola | 🌤️ Turkey wrap + salad | 🌙 Baked fish + potatoes",
            "🌅 Smoothie bowl | 🌤️ Lentil soup + bread | 🌙 Grilled chicken + veggies",
            "🌅 Avocado toast + eggs | 🌤️ Rice bowl + beans | 🌙 Pasta + lean meat",
            "🌅 Pancakes + fruit | 🌤️ Chicken sandwich | 🌙 Homemade pizza (veggie)",
            "🌅 Full breakfast | 🌤️ Grilled fish + salad | 🌙 Light soup + bread"
        )

        // Customize based on conditions (simplified)
        return if (userConditions.contains("Diabetes")) {
            listOf(
                "🌅 Oatmeal + nuts | 🌤️ Grilled chicken + veggies | 🌙 Fish + green beans",
                "🌅 Eggs + avocado | 🌤️ Salad + chickpeas | 🌙 Tofu stir-fry",
                "🌅 Greek yogurt + seeds | 🌤️ Lentil soup | 🌙 Baked chicken + broccoli",
                "🌅 Veggie omelet | 🌤️ Turkey + salad | 🌙 Grilled salmon + asparagus",
                "🌅 Chia pudding | 🌤️ Quinoa + vegetables | 🌙 Lean beef + mushrooms",
                "🌅 Cottage cheese + berries | 🌤️ Chicken wrap (low carb) | 🌙 Fish + cauliflower",
                "🌅 Smoothie (no sugar) | 🌤️ Bean salad | 🌙 Light protein + vegetables"
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
            "🥗 Mediterranean Salad",
            "Mixed greens, cherry tomatoes, cucumber, olives, feta cheese with olive oil dressing. ~250 kcal"
        ))

        recipes.add(Pair(
            "🍳 Veggie Omelet",
            "3 eggs with spinach, mushrooms, and bell peppers. Serve with whole wheat toast. ~300 kcal"
        ))

        // Condition specific
        if (userConditions.contains("Diabetes")) {
            recipes.add(Pair(
                "🥣 Overnight Oats (Diabetic-Friendly)",
                "Rolled oats, chia seeds, unsweetened almond milk, cinnamon, and a few berries. Low GI breakfast. ~280 kcal"
            ))
        }

        if (userConditions.contains("Hypertension")) {
            recipes.add(Pair(
                "🐟 Herb-Crusted Salmon",
                "Salmon fillet with fresh herbs, lemon, and garlic (no salt). Serve with steamed vegetables. ~350 kcal"
            ))
        }

        if (userConditions.contains("Cholesterol")) {
            recipes.add(Pair(
                "🥑 Avocado Toast with Seeds",
                "Whole grain bread with mashed avocado, hemp seeds, and a poached egg. Heart-healthy fats. ~320 kcal"
            ))
        }

        if (userConditions.contains("Gastritis")) {
            recipes.add(Pair(
                "🍲 Gentle Chicken Soup",
                "Tender chicken, carrots, potatoes in a mild broth. Easy on the stomach. ~200 kcal"
            ))
        }

        // Diet goal specific
        if (dietGoal == "Lose Weight") {
            recipes.add(Pair(
                "🥒 Zucchini Noodles with Pesto",
                "Spiralized zucchini with homemade basil pesto and cherry tomatoes. Low-carb alternative. ~180 kcal"
            ))
        }

        if (dietGoal == "Gain Weight") {
            recipes.add(Pair(
                "🍌 Protein Smoothie Bowl",
                "Banana, peanut butter, protein powder, oats, and almond milk. Topped with granola and nuts. ~500 kcal"
            ))
        }

        // General healthy recipes
        recipes.add(Pair(
            "🍗 Grilled Chicken & Quinoa",
            "Seasoned chicken breast with fluffy quinoa and roasted vegetables. Balanced and nutritious. ~400 kcal"
        ))

        recipes.add(Pair(
            "🥙 Hummus Veggie Wrap",
            "Whole wheat wrap with hummus, mixed greens, cucumber, tomatoes, and grilled vegetables. ~350 kcal"
        ))

        return recipes
    }
}
