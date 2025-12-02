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

class HeightFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_height, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        
        val tvHeightValue = view.findViewById<TextView>(R.id.tvHeightValue)
        val sliderHeight = view.findViewById<Slider>(R.id.sliderHeight)
        
        sliderHeight.addOnChangeListener { _, value, _ ->
            tvHeightValue.text = "${value.toInt()} cm"
        }
        
        view.findViewById<Button>(R.id.btnNext).setOnClickListener {
            viewModel.setHeight(sliderHeight.value.toInt())
            (activity as? OnboardingActivity)?.nextPage()
        }
    }
}