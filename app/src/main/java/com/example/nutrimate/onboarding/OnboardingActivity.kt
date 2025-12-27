package com.example.nutrimate.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.nutrimate.MainActivity
import com.example.nutrimate.ui.theme.PageBackground


class OnboardingActivity : ComponentActivity() {

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        username = intent.getStringExtra("USERNAME") ?: ""

        viewModel = ViewModelProvider(this)[OnboardingViewModel::class.java]

        setContent {
            OnboardingScreen(
                viewModel = viewModel,
                onFinish = { finishOnboarding() }
            )
        }
    }

    private fun finishOnboarding() {
        if (username.isNotEmpty()) {
            viewModel.saveUserData(username) {
                Toast.makeText(this, "Profil berhasil dibuat!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USERNAME", username)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        } else {
            Toast.makeText(this, "Error: Username not found", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    val totalPages = 8
    var currentPage by remember { mutableIntStateOf(0) }
    val dailyCalories by viewModel.dailyCalories.observeAsState(0)

    // Handle back button
    BackHandler(enabled = currentPage > 0) {
        currentPage--
    }

    fun nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++
        }
        // Calculate TDEE when reaching Summary page
        if (currentPage == totalPages - 1) {
            viewModel.calculateTDEE()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        // Segmented progress bar
        SegmentedProgressIndicator(
            currentPage = currentPage,
            totalPages = totalPages,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        // Page content with animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } togetherWith
                                slideOutHorizontally { width -> -width }
                    } else {
                        slideInHorizontally { width -> -width } togetherWith
                                slideOutHorizontally { width -> width }
                    }
                },
                label = "page_transition"
            ) { page ->
                when (page) {
                    0 -> GenderScreen(
                        onGenderSelected = { gender ->
                            viewModel.setGender(gender)
                            nextPage()
                        }
                    )
                    1 -> AgeScreen(
                        onAgeSelected = { age ->
                            viewModel.setAge(age)
                            nextPage()
                        }
                    )
                    2 -> HeightScreen(
                        onHeightSelected = { height ->
                            viewModel.setHeight(height)
                            nextPage()
                        }
                    )
                    3 -> WeightScreen(
                        onWeightSelected = { weight ->
                            viewModel.setWeight(weight)
                            nextPage()
                        }
                    )
                    4 -> ActivityLevelScreen(
                        onActivityLevelSelected = { level ->
                            viewModel.setActivityLevel(level)
                            nextPage()
                        }
                    )
                    5 -> GoalScreen(
                        onGoalSelected = { goal ->
                            viewModel.setDietGoal(goal)
                            nextPage()
                        }
                    )
                    6 -> HealthScreen(
                        onHealthSubmitted = { conditions, allergies ->
                            conditions.forEach { viewModel.toggleCondition(it, true) }
                            viewModel.setAllergies(allergies)
                            nextPage()
                        }
                    )
                    7 -> SummaryScreen(
                        dailyCalories = dailyCalories,
                        onFinish = onFinish
                    )
                }
            }
        }
    }
}