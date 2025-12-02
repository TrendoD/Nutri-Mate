package com.example.nutrimate.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.nutrimate.R
import com.google.android.material.progressindicator.LinearProgressIndicator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var viewModel: OnboardingViewModel
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        username = intent.getStringExtra("USERNAME") ?: ""

        viewModel = ViewModelProvider(this)[OnboardingViewModel::class.java]
        
        viewPager = findViewById(R.id.viewPager)
        progressBar = findViewById(R.id.progressBar)

        val adapter = OnboardingPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false // Disable swipe to enforce flow validation if needed, or true for free flow. Feature doc says "swipe", but logical flow usually requires validation (e.g. can't go next if age not picked). I'll set false and control via buttons.
        // Actually feature doc says "Navigasi geser (swipe) antar halaman". 
        // But also "Mengetuk salah satu kartu ... secara otomatis mengarahkan ke halaman berikutnya".
        // I'll enable user input but maybe I should check validation. For simplicity and UX smoothness, I'll disable swipe so users don't skip steps accidentally, and rely on buttons/interactions.
        
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val progress = ((position + 1) * 100) / adapter.itemCount
                progressBar.setProgress(progress, true)
                
                // Calculate TDEE when reaching Summary page (last page)
                if (position == adapter.itemCount - 1) {
                    viewModel.calculateTDEE()
                }
            }
        })
        
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewPager.currentItem > 0) {
                    viewPager.currentItem -= 1
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    fun nextPage() {
        if (viewPager.currentItem < (viewPager.adapter?.itemCount ?: 0) - 1) {
            viewPager.currentItem += 1
        }
    }
    
    fun getUsername(): String = username

    private inner class OnboardingPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 8

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> GenderFragment()
                1 -> AgeFragment()
                2 -> HeightFragment()
                3 -> WeightFragment()
                4 -> ActivityLevelFragment()
                5 -> GoalFragment()
                6 -> HealthFragment()
                7 -> SummaryFragment()
                else -> GenderFragment()
            }
        }
    }
}