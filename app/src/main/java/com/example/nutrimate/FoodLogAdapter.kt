package com.example.nutrimate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class FoodLogItem(
    val id: Int,
    val name: String,
    val totalCalories: Float,
    val servingQty: Float,
    val unit: String,
    val foodId: String = "",
    val carbs: Float = 0f,
    val protein: Float = 0f,
    val fat: Float = 0f,
    val caloriesPerServing: Float = 0f
)

interface FoodLogItemListener {
    fun onEditClick(item: FoodLogItem)
    fun onDeleteClick(item: FoodLogItem)
}

class FoodLogAdapter(
    private var items: List<FoodLogItem> = emptyList(),
    private var listener: FoodLogItemListener? = null
) : RecyclerView.Adapter<FoodLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFoodName: TextView = view.findViewById(R.id.tvFoodName)
        val tvServing: TextView = view.findViewById(R.id.tvServing)
        val tvCalories: TextView = view.findViewById(R.id.tvCalories)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
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
        holder.tvCalories.text = "${item.totalCalories.toInt()} kkal"
        
        holder.btnEdit.setOnClickListener {
            listener?.onEditClick(item)
        }
        
        holder.btnDelete.setOnClickListener {
            listener?.onDeleteClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<FoodLogItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    fun setListener(listener: FoodLogItemListener) {
        this.listener = listener
    }
}