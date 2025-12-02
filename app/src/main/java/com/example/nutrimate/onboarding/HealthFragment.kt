package com.example.nutrimate.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nutrimate.R
import com.google.android.material.chip.Chip

class HealthFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_health, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        
        setupChip(view.findViewById(R.id.chipDiabetes), "Diabetes")
        setupChip(view.findViewById(R.id.chipHypertension), "Hypertension")
        setupChip(view.findViewById(R.id.chipCholesterol), "Cholesterol")
        setupChip(view.findViewById(R.id.chipGastritis), "Gastritis")
        
        val etAllergies = view.findViewById<EditText>(R.id.etAllergies)
        
        view.findViewById<Button>(R.id.btnNext).setOnClickListener {
            viewModel.setAllergies(etAllergies.text.toString())
            (activity as? OnboardingActivity)?.nextPage()
        }
    }
    
    private fun setupChip(chip: Chip, condition: String) {
        chip.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleCondition(condition, isChecked)
        }
    }
}