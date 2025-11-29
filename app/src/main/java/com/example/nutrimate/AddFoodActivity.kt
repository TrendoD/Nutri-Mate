package com.example.nutrimate

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrimate.data.AppDatabase
import com.example.nutrimate.data.Food
import com.example.nutrimate.data.FoodLog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddFoodActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var rvFoodSearch: RecyclerView
    private lateinit var tvMealType: TextView
    private lateinit var adapter: FoodSearchAdapter
    
    private var username: String = ""
    private var mealType: String = ""
    private var selectedDate: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        database = AppDatabase.getDatabase(this)
        username = intent.getStringExtra("USERNAME") ?: ""
        mealType = intent.getStringExtra("MEAL_TYPE") ?: "Breakfast"
        selectedDate = intent.getStringExtra("DATE") ?: dateFormat.format(Date())
        
        if (username.isEmpty()) {
            finish()
            return
        }

        initViews()
    }

    private fun initViews() {
        tvMealType = findViewById(R.id.tvMealType)
        tvMealType.text = "Add to $mealType"

        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        rvFoodSearch = findViewById(R.id.rvFoodSearch)
        
        rvFoodSearch.layoutManager = LinearLayoutManager(this)
        adapter = FoodSearchAdapter { food ->
            showAddDialog(food)
        }
        rvFoodSearch.adapter = adapter
        
        btnSearch.setOnClickListener {
            val query = etSearch.text.toString()
            searchFood(query)
        }
        
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if ((s?.length ?: 0) > 2) {
                    searchFood(s.toString())
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun searchFood(query: String) {
        lifecycleScope.launch {
            val foods = database.foodDao().searchFoods(query)
            adapter.submitList(foods)
        }
    }

    private fun showAddDialog(food: Food) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_food)
        // Make dialog width match parent (mostly)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val etQty = dialog.findViewById<EditText>(R.id.etQuantity)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)
        val tvUnit = dialog.findViewById<TextView>(R.id.tvUnit)
        
        tvTitle.text = "Add ${food.name}"
        tvUnit.text = food.servingUnit
        
        btnAdd.setOnClickListener {
            val qtyStr = etQty.text.toString()
            val qty = qtyStr.toFloatOrNull() ?: 1.0f
            
            saveFoodLog(food, qty)
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun saveFoodLog(food: Food, qty: Float) {
        lifecycleScope.launch {
            val log = FoodLog(
                username = username,
                foodId = food.id,
                servingQty = qty,
                mealType = mealType,
                date = selectedDate
            )
            database.foodDao().insertFoodLog(log)
            Toast.makeText(this@AddFoodActivity, "Added ${food.name}", Toast.LENGTH_SHORT).show()
            finish() // Go back to Log after save is complete
        }
    }
}
