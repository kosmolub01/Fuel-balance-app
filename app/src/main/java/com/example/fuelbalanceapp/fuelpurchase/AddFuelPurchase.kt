package com.example.fuelbalanceapp.fuelpurchase

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fuelbalanceapp.DatabaseHelper
import com.example.fuelbalanceapp.DatabaseHelper.Companion.FUEL_PURCHASES_TABLE_NAME
import com.example.fuelbalanceapp.DatabaseHelper.Companion.COLUMN_DATE
import com.example.fuelbalanceapp.DatabaseHelper.Companion.COLUMN_FUEL_AMOUNT
import com.example.fuelbalanceapp.DatabaseHelper.Companion.COLUMN_ID
import com.example.fuelbalanceapp.R
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_add_fuel_purchase.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class AddFuelPurchase : AppCompatActivity() {

    private lateinit var fuelPurchaseAdapter: FuelPurchaseAdapter
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_fuel_purchase)

        AndroidThreeTen.init(this)

        dbHelper = DatabaseHelper(this)
        Log.d("AddFuelPurchase", "onCreate")

        val recyclerView: RecyclerView = RecyclerViewFuelPurchases
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fuelPurchases = getAllFuelPurchases()

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

        if (isFuelAmountValid(fuelAmount)) {
            val newFuelPurchase = FuelPurchase(currentDate, fuelAmount.toDouble())
            val id : Long = insertFuelPurchase(newFuelPurchase)
            newFuelPurchase.id = id
            fuelPurchaseAdapter.addFuelPurchase(newFuelPurchase)
            decimalFuelPurchase.text.clear() // Clear the EditText after adding.
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

    private fun isFuelAmountValid(fuelAmount: String): Boolean {
        if (fuelAmount.isEmpty()) {
            return false
        }

        if ('.' !in fuelAmount) {
            return true
        }

        // Check if there are any digits after the decimal separator.
        val parts = fuelAmount.split('.')
        val digitsAfterSeparator = parts[1]
        return digitsAfterSeparator.isNotEmpty()
    }

    private fun insertFuelPurchase(fuelPurchase: FuelPurchase) : Long {
        val db = dbHelper.writableDatabase

        // Format the LocalDate to a String.
        val formattedDate: String = fuelPurchase.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))

        val values = ContentValues().apply {
            put(COLUMN_DATE, formattedDate)
            put(COLUMN_FUEL_AMOUNT, fuelPurchase.amount)
        }
        val id = db.insert(FUEL_PURCHASES_TABLE_NAME, null, values)
        db.close()

        return id
    }

    fun deleteFuelPurchaseFromDb(id: Long) {
        val db = dbHelper.writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())

        // Attempt to delete the record from the database.
        db.delete(FUEL_PURCHASES_TABLE_NAME, whereClause, whereArgs)

        db.close()
    }

    private fun getAllFuelPurchases(): MutableList<FuelPurchase> {
        val fuelPurchases = mutableListOf<FuelPurchase>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $FUEL_PURCHASES_TABLE_NAME", null)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
            val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
            val amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_FUEL_AMOUNT))

            // Parse the date string into a LocalDate object.
            val parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))

            fuelPurchases.add(FuelPurchase(parsedDate, amount, id))
        }

        cursor.close()
        db.close()

        fuelPurchases.sortByDescending { it.date }

        return fuelPurchases
    }
}
