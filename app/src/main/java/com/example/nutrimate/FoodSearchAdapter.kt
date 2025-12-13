package com.example.nutrimate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrimate.data.Food

class FoodSearchAdapter(
    private var foods: List<Food> = emptyList(),
    private var favorites: Set<String> = emptySet(),
    private val onFoodClick: (Food) -> Unit,
    private val onFavoriteClick: (Food) -> Unit
) : RecyclerView.Adapter<FoodSearchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDetails: TextView = view.findViewById(R.id.tvDetails)
        val tvNutrition: TextView = view.findViewById(R.id.tvNutrition)
        val tvNutritionExtra: TextView = view.findViewById(R.id.tvNutritionExtra)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val food = foods[position]
        holder.tvName.text = food.name
        holder.tvDetails.text = "${food.servingSize} ${food.servingUnit} | ${food.calories.toInt()} kkal"
        holder.tvNutrition.text = "K: ${food.carbs.toInt()}g | P: ${food.protein.toInt()}g | L: ${food.fat.toInt()}g"
        holder.tvNutritionExtra.text = "Gula: ${food.sugar.toInt()}g | Srt: ${food.fiber.toInt()}g | Na: ${food.sodium.toInt()}mg"
        
        // Translate category for display
        holder.tvCategory.text = when(food.category) {
            "Fruit" -> "Buah"
            "Vegetable" -> "Sayur"
            "Protein" -> "Protein"
            "Grain" -> "Biji-bijian"
            "Dairy" -> "Produk Susu"
            else -> "Lainnya"
        }
        
        // Set favorite icon
        val isFav = favorites.contains(food.id)
        holder.ivFavorite.setImageResource(
            if (isFav) android.R.drawable.star_big_on else android.R.drawable.star_big_off
        )
        
        // Set category color
        val categoryColor = when(food.category.lowercase()) {
            "fruit" -> android.graphics.Color.parseColor("#E8F5E9")
            "vegetable" -> android.graphics.Color.parseColor("#E8F5E9")
            "protein" -> android.graphics.Color.parseColor("#FFEBEE")
            "grain" -> android.graphics.Color.parseColor("#FFF3E0")
            "dairy" -> android.graphics.Color.parseColor("#E3F2FD")
            else -> android.graphics.Color.parseColor("#F5F5F5")
        }
        holder.tvCategory.setBackgroundColor(categoryColor)
        
        holder.itemView.setOnClickListener {
            onFoodClick(food)
        }
        
        holder.ivFavorite.setOnClickListener {
            onFavoriteClick(food)
        }
    }

    override fun getItemCount() = foods.size

    fun submitList(newFoods: List<Food>) {
        foods = newFoods
        notifyDataSetChanged()
    }
    
    fun updateFavorites(newFavorites: Set<String>) {
        favorites = newFavorites
        notifyDataSetChanged()
    }
}