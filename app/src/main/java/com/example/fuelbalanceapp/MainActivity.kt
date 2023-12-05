package com.example.fuelbalanceapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fuelbalanceapp.detectedactivity.DetectedActivityService
import com.example.fuelbalanceapp.fuelpurchase.AddFuelPurchase
import com.example.fuelbalanceapp.transitions.TRANSITIONS_RECEIVER_ACTION
import com.example.fuelbalanceapp.transitions.TransitionsReceiver
import com.example.fuelbalanceapp.transitions.removeActivityTransitionUpdates
import com.example.fuelbalanceapp.transitions.requestActivityTransitionUpdates
import kotlinx.android.synthetic.main.activity_main.*

const val SHARED_PREFERENCES_FILE = "MyPrefs"
const val TRIPS_RECORDING_KEY = "tripsRecording"

class MainActivity : AppCompatActivity() {

    private val transitionBroadcastReceiver: TransitionsReceiver = TransitionsReceiver().apply {
        action = { setDetectedActivity(it) }
    }

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

        buttonSetFuelConsumption.setOnClickListener {
            val intent = Intent(this@MainActivity, SetFuelConsumption::class.java)
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

        if (requestCode == PERMISSION_REQUEST &&
            permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) &&
            permissions.contains(Manifest.permission.ACTIVITY_RECOGNITION) &&
            grantResults.size == 2) {

            // Not all permissions granted.
            if(grantResults[0] == PackageManager.PERMISSION_DENIED ||
                grantResults[1] == PackageManager.PERMISSION_DENIED) {
                showSettingsDialog(this)
                Log.d("onRequestPermissionsRes", "Not granted")
            }
            // All permissions granted.
            else if (
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                Log.d("onRequestPermissionsRes", "Granted")

                switchTripsRecording.isChecked = true
                saveTripsRecordingToSharedPreferences(true)
                startTracking()
                Toast.makeText(this@MainActivity, "Activity tracking has started",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTripsRecordingToSharedPreferences(isChecked: Boolean) {
        // Save tripsRecording to SharedPreferences
        val sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(TRIPS_RECORDING_KEY, isChecked)
        editor.apply()
    }
}