package com.example.nutrimate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrimate.data.Food

class FoodSearchAdapter(
    private var foods: List<Food> = emptyList(),
    private val onFoodClick: (Food) -> Unit
) : RecyclerView.Adapter<FoodSearchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDetails: TextView = view.findViewById(R.id.tvDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val food = foods[position]
        holder.tvName.text = food.name
        holder.tvDetails.text = "${food.servingSize} ${food.servingUnit} | ${food.calories.toInt()} kcal"
        
        holder.itemView.setOnClickListener {
            onFoodClick(food)
        }
    }

    override fun getItemCount() = foods.size

    fun submitList(newFoods: List<Food>) {
        foods = newFoods
        notifyDataSetChanged()
    }
}
