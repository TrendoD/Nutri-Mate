package com.example.nutrimate.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nutrimate.R

class GoalFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_goal, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        
        setupOption(view.findViewById(R.id.optLoseWeight), "Turunkan Berat Badan", "Defisit kalori untuk membakar lemak")
        setupOption(view.findViewById(R.id.optMaintainWeight), "Jaga Berat Badan", "Kalori seimbang untuk stabilitas")
        setupOption(view.findViewById(R.id.optGainWeight), "Naikkan Berat Badan", "Surplus kalori untuk membangun otot")
    }

    private fun setupOption(card: View, title: String, desc: String) {
        card.findViewById<TextView>(R.id.tvOptionTitle).text = title
        card.findViewById<TextView>(R.id.tvOptionDesc).text = desc
        
        val goalKey = when {
            title.contains("Turunkan") -> "Lose Weight"
            title.contains("Naikkan") -> "Gain Weight"
            else -> "Maintain"
        }

        card.setOnClickListener {
            viewModel.setDietGoal(goalKey)
            (activity as? OnboardingActivity)?.nextPage()
        }
    }
}