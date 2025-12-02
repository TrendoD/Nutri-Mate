package com.example.nutrimate.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nutrimate.R
import com.google.android.material.slider.Slider

class WeightFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_weight, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        
        val tvWeightValue = view.findViewById<TextView>(R.id.tvWeightValue)
        val sliderWeight = view.findViewById<Slider>(R.id.sliderWeight)
        
        sliderWeight.addOnChangeListener { _, value, _ ->
            tvWeightValue.text = String.format("%.1f kg", value)
        }
        
        view.findViewById<Button>(R.id.btnNext).setOnClickListener {
            viewModel.setWeight(sliderWeight.value)
            (activity as? OnboardingActivity)?.nextPage()
        }
    }
}