package com.example.fuelbalanceapp.tripshistory

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
import com.example.fuelbalanceapp.DatabaseHelper.Companion.COLUMN_DATE
import com.example.fuelbalanceapp.DatabaseHelper.Companion.COLUMN_DISTANCE
import com.example.fuelbalanceapp.DatabaseHelper.Companion.COLUMN_FUEL_AMOUNT
import com.example.fuelbalanceapp.DatabaseHelper.Companion.COLUMN_ID
import com.example.fuelbalanceapp.DatabaseHelper.Companion.FUEL_PURCHASES_TABLE_NAME
import com.example.fuelbalanceapp.DatabaseHelper.Companion.TRIPS_TABLE_NAME
import com.example.fuelbalanceapp.R
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_view_trips_history.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class ViewTripsHistory : AppCompatActivity() {

    private lateinit var tripsHistoryAdapter: TripsHistoryAdapter
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_trips_history)

        AndroidThreeTen.init(this)

        dbHelper = DatabaseHelper(this)
        Log.d("ViewTripsHistory", "onCreate")

        val recyclerView: RecyclerView = RecyclerViewTripsHistory
        recyclerView.layoutManager = LinearLayoutManager(this)

        val tripsHistory = getAllTrips()

        tripsHistoryAdapter = TripsHistoryAdapter(tripsHistory, this)
        recyclerView.adapter = tripsHistoryAdapter

        updateEmptyViewVisibility()

        buttonAddTripToTheList.setOnClickListener {
            addTrip()
        }
    }

    private fun addTrip() {

        val distance = decimalDistance.text.toString()
        val currentDate: LocalDate = LocalDate.now()

        if (isDistanceValid(distance)) {
            val newTrip = Trip(currentDate, distance.toDouble())
            val id : Long = insertTrip(newTrip)
            newTrip.id = id
            tripsHistoryAdapter.addTrip(newTrip)
            decimalDistance.text.clear() // Clear the EditText after adding.
            updateEmptyViewVisibility()
            RecyclerViewTripsHistory.smoothScrollToPosition(0)
        } else {
            // Handle case where fuel amount is empty
            Toast.makeText(this, "Please enter a valid distance", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateEmptyViewVisibility() {
        val emptyTextView: TextView = findViewById(R.id.textViewEmpty)
        emptyTextView.visibility = if (tripsHistoryAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun isDistanceValid(distance: String): Boolean {
        if (distance.isEmpty()) {
            return false
        }

        if ('.' !in distance) {
            return true
        }

        // Check if there are any digits after the decimal separator.
        val parts = distance.split('.')
        val digitsAfterSeparator = parts[1]
        return digitsAfterSeparator.isNotEmpty()
    }

    private fun insertTrip(trip: Trip) : Long {
        val db = dbHelper.writableDatabase

        // Format the LocalDate to a String.
        val formattedDate: String = trip.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))

        val values = ContentValues().apply {
            put(COLUMN_DATE, formattedDate)
            put(COLUMN_DISTANCE, trip.distance)
        }
        val id = db.insert(TRIPS_TABLE_NAME, null, values)
        db.close()

        return id
    }

    fun deleteTripFromDb(id: Long) {
        val db = dbHelper.writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())

        // Attempt to delete the record from the database.
        db.delete(TRIPS_TABLE_NAME, whereClause, whereArgs)

        db.close()
    }

    private fun getAllTrips(): MutableList<Trip> {
        val trips = mutableListOf<Trip>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TRIPS_TABLE_NAME", null)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
            val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
            val distance = cursor.getDouble(cursor.getColumnIndex(COLUMN_DISTANCE))

            // Parse the date string into a LocalDate object.
            val parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))

            trips.add(Trip(parsedDate, distance, id))
        }

        cursor.close()
        db.close()

        trips.sortByDescending { it.date }

        return trips
    }
}
