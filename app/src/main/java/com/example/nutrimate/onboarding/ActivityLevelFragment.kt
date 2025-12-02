package com.example.nutrimate.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nutrimate.R
import com.google.android.material.card.MaterialCardView

class ActivityLevelFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_activity_level, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        
        setupOption(view.findViewById(R.id.optSedentary), "Sedenter", "Sedikit atau tidak ada olahraga")
        setupOption(view.findViewById(R.id.optLight), "Sedikit Aktif", "Olahraga ringan 1-3 hari/minggu")
        setupOption(view.findViewById(R.id.optModerate), "Cukup Aktif", "Olahraga sedang 3-5 hari/minggu")
        setupOption(view.findViewById(R.id.optActive), "Aktif", "Olahraga berat 6-7 hari/minggu")
        setupOption(view.findViewById(R.id.optVeryActive), "Sangat Aktif", "Olahraga sangat berat & pekerjaan fisik")
    }
    
    private fun setupOption(card: View, title: String, desc: String) {
        card.findViewById<TextView>(R.id.tvOptionTitle).text = title
        card.findViewById<TextView>(R.id.tvOptionDesc).text = desc
        
        card.setOnClickListener {
            viewModel.setActivityLevel(title)
            
            // Reset all cards visual state (optional, but good for feedback if not auto-navigating)
            // Since we auto-navigate:
            (activity as? OnboardingActivity)?.nextPage()
        }
    }
}