package com.example.fuelbalanceapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_add_fuel_purchase.*

class AddFuelPurchase : AppCompatActivity() {

    private lateinit var fuelPurchaseAdapter: FuelPurchaseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_fuel_purchase)

        val recyclerView: RecyclerView = RecyclerViewFuelPurchases
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fuelPurchases = mutableListOf(
            FuelPurchase("10 liters", "2023-11-28"),
            FuelPurchase("11 liters", "2023-11-25"),
            FuelPurchase("12 liters", "2023-11-28"),
            FuelPurchase("13 liters", "2023-11-25"),
            FuelPurchase("14 liters", "2023-11-28"),
            FuelPurchase("15 liters", "2023-11-25"),
            FuelPurchase("16 liters", "2023-11-28"),
            FuelPurchase("25 liters", "2023-11-25"),
            FuelPurchase("30 liters", "2023-11-28"),

        )

        fuelPurchaseAdapter = FuelPurchaseAdapter(fuelPurchases, this)
        recyclerView.adapter = fuelPurchaseAdapter

        updateEmptyViewVisibility()
    }

    fun updateEmptyViewVisibility() {
        val emptyTextView: TextView = findViewById(R.id.textViewEmpty)
        emptyTextView.visibility = if (fuelPurchaseAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }
}
