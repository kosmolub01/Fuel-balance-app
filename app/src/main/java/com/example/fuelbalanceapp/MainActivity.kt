package com.example.fuelbalanceapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.fuelbalanceapp.detectedactivity.DetectedActivityService
import com.example.fuelbalanceapp.fuelpurchase.AddFuelPurchase
import com.example.fuelbalanceapp.fuelpurchase.FuelPurchase
import com.example.fuelbalanceapp.transitions.TRANSITIONS_RECEIVER_ACTION
import com.example.fuelbalanceapp.transitions.TransitionsReceiver
import com.example.fuelbalanceapp.transitions.removeActivityTransitionUpdates
import com.example.fuelbalanceapp.transitions.requestActivityTransitionUpdates
import com.example.fuelbalanceapp.tripshistory.Trip
import com.example.fuelbalanceapp.tripshistory.ViewTripsHistory
import kotlinx.android.synthetic.main.activity_main.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.math.BigDecimal
import java.math.RoundingMode

const val SHARED_PREFERENCES_FILE = "MyPrefs"
const val TRIPS_RECORDING_KEY = "tripsRecording"
//const val BALANCE_KEY = "balance"

class MainActivity : AppCompatActivity() {

    private val transitionBroadcastReceiver: TransitionsReceiver = TransitionsReceiver().apply {
        action = { setDetectedActivity(it) }
    }

    private lateinit var dbHelper: DatabaseHelper

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra(SUPPORTED_ACTIVITY_KEY)) {
            val supportedActivity = intent.getSerializableExtra(
                SUPPORTED_ACTIVITY_KEY) as SupportedActivity
            setDetectedActivity(supportedActivity)
            Log.d("onNewIntent", "onNewIntent")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        // Load tripsRecording from SharedPreferences and set the switch accordingly, start service,
        // request activity transition updates.
        val sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val tripsRecording: Boolean = sharedPreferences.getBoolean(TRIPS_RECORDING_KEY, false)
        switchTripsRecording.isChecked = tripsRecording

        // According to the switch saved state, start tracking (start service,
        // request activity transition updates) right away after starting the app.
        if(switchTripsRecording.isChecked) {
            if(arePermissionsGranted()) {
                startTracking()
            }
            else {
                switchTripsRecording.isChecked = false
                saveTripsRecordingToSharedPreferences(false)
            }
        }

        switchTripsRecording.setOnCheckedChangeListener { _, isChecked ->
            // Handle the switch state change.
            if (isChecked) {
                if(arePermissionsGranted()) {
                    switchTripsRecording.isChecked = true
                    saveTripsRecordingToSharedPreferences(true)
                    startTracking()
                    Toast.makeText(this@MainActivity, "Activity tracking has started",
                        Toast.LENGTH_SHORT).show()
                }
                else {
                    requestPermission()
                    switchTripsRecording.isChecked = false
                    saveTripsRecordingToSharedPreferences(false)
                }

            } else {
                switchTripsRecording.isChecked = false
                Toast.makeText(this, "Activity tracking has stopped", Toast.LENGTH_SHORT).show()
                saveTripsRecordingToSharedPreferences(false)
                resetTracking()
            }
        }

        // Calculate the balance and update the textView.
        //val balance: Float = sharedPreferences.getFloat(BALANCE_KEY, 0.0F)
        textViewBalanceValue.text = calculateTheBalance().toString()

        buttonSetFuelConsumption.setOnClickListener {
            val intent = Intent(this@MainActivity, SetFuelConsumption::class.java)
            startActivity(intent)
        }

        buttonViewTripsHistory.setOnClickListener {
            val intent = Intent(this@MainActivity, ViewTripsHistory::class.java)
            startActivity(intent)
        }

        buttonAddFuelPurchase.setOnClickListener {
            val intent = Intent(this@MainActivity, AddFuelPurchase::class.java)
            startActivity(intent)
        }
    }

    private fun resetTracking() {
        setDetectedActivity(SupportedActivity.NOT_ENABLED)
        removeActivityTransitionUpdates()
        stopService(Intent(this, DetectedActivityService::class.java))

    }

    private fun startTracking() {
        startService(Intent(this, DetectedActivityService::class.java))
        requestActivityTransitionUpdates()
        setDetectedActivity(SupportedActivity.UPDATE_IN_PROGRESS)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(transitionBroadcastReceiver, IntentFilter(TRANSITIONS_RECEIVER_ACTION))
        textViewBalanceValue.text = calculateTheBalance().toString()
    }

    override fun onPause() {
        unregisterReceiver(transitionBroadcastReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        Log.d("onDestroy", "onDestroy")
        removeActivityTransitionUpdates()
        super.onDestroy()
    }

    private fun setDetectedActivity(supportedActivity: SupportedActivity) {
        activityTitle.text = getString(supportedActivity.activityText)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d("onRequestPermissionsRes", "grantResults.size = ${grantResults.size}")
        Log.d("onRequestPermissionsRes", "permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) = ${permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)}")
        Log.d("onRequestPermissionsRes", "permissions.contains(Manifest.permission.ACTIVITY_RECOGNITION) = ${permissions.contains(Manifest.permission.ACTIVITY_RECOGNITION)}")

        for(result in grantResults) {
            Log.d("onRequestPermissionsRes", "result = $result")
        }

        if (requestCode == PERMISSION_REQUEST &&
            permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) &&
            permissions.contains(Manifest.permission.ACTIVITY_RECOGNITION) &&
            grantResults.size == 2) {

            // Not all permissions granted.
            if(
                grantResults[0] == PackageManager.PERMISSION_DENIED ||
                grantResults[1] == PackageManager.PERMISSION_DENIED) {
                showSettingsDialog(this)
                Log.d("onRequestPermissionsRes", "Not granted")
            }
            // All permissions granted. Inform the user to also allow the app to access location all the time if not yet allowed.
            else if (
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {

                Log.d("onRequestPermissionsRes", "Runtime permissions granted. Inform the user to also allow the app to access location all the time")

                if(PackageManager.PERMISSION_DENIED == ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    showSettingsDialog(this)
                }
            }
        }
    }

    private fun saveTripsRecordingToSharedPreferences(isChecked: Boolean) {
        // Save tripsRecording to SharedPreferences.
        val sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(TRIPS_RECORDING_KEY, isChecked)
        editor.apply()
    }

    private fun calculateTheBalance() : Double{
        var totalDistance = 0.0
        var totalFuelAmount = 0.0
        var usedFuel : Double

        // Calculate total distance covered and amount of purchased fuel.
        val trips = getAllTrips()

        for (trip in trips) {
            totalDistance += trip.distance
        }

        // Load fuelConsumption from SharedPreferences.
        val sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val fuelConsumption: Float = sharedPreferences.getFloat(FUEL_CONSUMPTION_KEY, 0.0F)

        // Calculate used fuel.
        usedFuel = fuelConsumption * totalDistance / 100

        val fuelPurchases = getAllFuelPurchases()

        for (fuelPurchase in fuelPurchases) {
            totalFuelAmount += fuelPurchase.amount
        }

        return BigDecimal(totalFuelAmount - usedFuel).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    private fun getAllTrips(): MutableList<Trip> {
        val trips = mutableListOf<Trip>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TRIPS_TABLE_NAME}", null)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID))
            val date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE))
            val distance = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_DISTANCE))

            // Parse the date string into a LocalDate object.
            val parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))

            trips.add(Trip(parsedDate, distance, id))
        }
        cursor.close()
        db.close()

        trips.sortByDescending { it.date }

        return trips
    }

    private fun getAllFuelPurchases(): MutableList<FuelPurchase> {
        val fuelPurchases = mutableListOf<FuelPurchase>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.FUEL_PURCHASES_TABLE_NAME}", null)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID))
            val date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE))
            val amount = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_FUEL_AMOUNT))

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