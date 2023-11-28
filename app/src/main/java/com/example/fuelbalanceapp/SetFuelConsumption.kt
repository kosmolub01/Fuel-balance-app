package com.example.fuelbalanceapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.NavUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_set_fuel_consumption.*

const val FUEL_CONSUMPTION_KEY = "fuelConsumption"

class SetFuelConsumption : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_fuel_consumption)

        // Load fuelConsumption from SharedPreferences.
        val sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val fuelConsumption: Float = sharedPreferences.getFloat(FUEL_CONSUMPTION_KEY, 0.0F)

        // Set the text of the EditText to the loaded fuelConsumption
        decimalFuelConsumption.setText(fuelConsumption.toString())

        buttonSaveCarFuelConsumption.setOnClickListener {
            // Save fuelConsumption to SharedPreferences.
            val fuelConsumption: Float = decimalFuelConsumption.text.toString().toFloatOrNull() ?: 0.0F
            saveFuelConsumptionToSharedPreferences(fuelConsumption)
            Toast.makeText(this, "Car fuel consumption has been saved", Toast.LENGTH_SHORT).show()
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    private fun saveFuelConsumptionToSharedPreferences(fuelConsumption: Float) {
        val sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putFloat(FUEL_CONSUMPTION_KEY, fuelConsumption)
        editor.apply()
    }
}