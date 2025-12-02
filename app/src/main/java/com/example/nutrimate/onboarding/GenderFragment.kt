package com.example.nutrimate.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nutrimate.R
import com.google.android.material.card.MaterialCardView

class GenderFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gender, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        
        view.findViewById<MaterialCardView>(R.id.cardMale).setOnClickListener {
            viewModel.setGender("Male")
            (activity as? OnboardingActivity)?.nextPage()
        }
        
        view.findViewById<MaterialCardView>(R.id.cardFemale).setOnClickListener {
            viewModel.setGender("Female")
            (activity as? OnboardingActivity)?.nextPage()
        }
    }
}