package com.example.nutrimate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class FoodLogItem(
    val id: Int,
    val name: String,
    val totalCalories: Float,
    val servingQty: Float,
    val unit: String
)

class FoodLogAdapter(
    private var items: List<FoodLogItem> = emptyList()
) : RecyclerView.Adapter<FoodLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFoodName: TextView = view.findViewById(R.id.tvFoodName)
        val tvServing: TextView = view.findViewById(R.id.tvServing)
        val tvCalories: TextView = view.findViewById(R.id.tvCalories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvFoodName.text = item.name
        holder.tvServing.text = "${item.servingQty} ${item.unit}"
        holder.tvCalories.text = "${item.totalCalories.toInt()} kcal"
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<FoodLogItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
