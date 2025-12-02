package com.example.nutrimate.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nutrimate.MainActivity
import com.example.nutrimate.R

class SummaryFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        
        val tvCalories = view.findViewById<TextView>(R.id.tvCalories)
        
        viewModel.dailyCalories.observe(viewLifecycleOwner) { calories ->
            tvCalories.text = String.format("%,d", calories)
        }
        
        view.findViewById<Button>(R.id.btnFinish).setOnClickListener {
            val activity = activity as? OnboardingActivity
            val username = activity?.getUsername() ?: ""
            
            if (username.isNotEmpty()) {
                viewModel.saveUserData(username) {
                    Toast.makeText(context, "Profil berhasil dibuat!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    // Also pass USER_NAME (Full name) if possible, but database fetch in MainActivity handles it usually.
                    // MainActivity expects USERNAME usually.
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    activity?.finish()
                }
            } else {
                Toast.makeText(context, "Error: Username not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}