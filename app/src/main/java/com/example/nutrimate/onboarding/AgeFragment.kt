package com.example.nutrimate.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nutrimate.R

class AgeFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_age, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        
        val npAge = view.findViewById<NumberPicker>(R.id.npAge)
        npAge.minValue = 10
        npAge.maxValue = 100
        npAge.value = 25
        npAge.wrapSelectorWheel = false
        
        view.findViewById<Button>(R.id.btnNext).setOnClickListener {
            viewModel.setAge(npAge.value)
            (activity as? OnboardingActivity)?.nextPage()
        }
    }
}