package com.example.fuelbalanceapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_add_fuel_purchase.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class AddFuelPurchase : AppCompatActivity() {

    private lateinit var fuelPurchaseAdapter: FuelPurchaseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_fuel_purchase)

        AndroidThreeTen.init(this)

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

        buttonAddFuelPurchaseToTheList.setOnClickListener {
            addFuelPurchase()
        }
    }

    private fun addFuelPurchase() {
        val fuelAmount = decimalFuelPurchase.text.toString()
        val currentDate: LocalDate = LocalDate.now()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val formattedCurrentDate: String = currentDate.format(formatter).toString()

            if (fuelAmount.isNotEmpty()) {
                val newFuelPurchase = FuelPurchase(fuelAmount, formattedCurrentDate)
                fuelPurchaseAdapter.addFuelPurchase(newFuelPurchase)
                decimalFuelPurchase.text.clear() // Clear the EditText after adding
                updateEmptyViewVisibility()
                RecyclerViewFuelPurchases.smoothScrollToPosition(0)
            } else {
                // Handle case where fuel amount is empty
                Toast.makeText(this, "Please enter a valid fuel amount", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateEmptyViewVisibility() {
        val emptyTextView: TextView = findViewById(R.id.textViewEmpty)
        emptyTextView.visibility = if (fuelPurchaseAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }
}
