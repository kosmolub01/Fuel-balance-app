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
import androidx.core.app.ActivityCompat
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

    private var isTrackingStarted = false

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

        // According to the switch saved state, start tracking right away after starting the app.
        if(switchTripsRecording.isChecked) {
            startTracking()
        }

        switchTripsRecording.setOnCheckedChangeListener { _, isChecked ->
            // Save tripsRecording to SharedPreferences when it changes.
            saveTripsRecordingToSharedPreferences(isChecked)
            // Handle the switch state change.
            if (isChecked) {
                startTracking()
                Toast.makeText(this@MainActivity, "Activity tracking has started",
                    Toast.LENGTH_SHORT).show()
            } else {
                resetTracking()
            }
        }

        buttonSetFuelConsumption.setOnClickListener {
            val intent = Intent(this@MainActivity, SetFuelConsumption::class.java)
            //intent.putExtra("key", value) //Optional parameters
            startActivity(intent)
        }

        buttonAddFuelPurchase.setOnClickListener {
            val intent = Intent(this@MainActivity, AddFuelPurchase::class.java)
            startActivity(intent)
        }


    }

    private fun resetTracking() {
        isTrackingStarted = false
        setDetectedActivity(SupportedActivity.NOT_ENABLED)
        removeActivityTransitionUpdates()
        stopService(Intent(this, DetectedActivityService::class.java))
        Toast.makeText(this, "Activity tracking has stopped", Toast.LENGTH_SHORT).show()
    }

    private fun startTracking() {
        if (isPermissionGranted()) {
            startService(Intent(this, DetectedActivityService::class.java))
            requestActivityTransitionUpdates()
            setDetectedActivity(SupportedActivity.UPDATE_IN_PROGRESS)
            isTrackingStarted = true
        } else {
            requestPermission()
        }
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
        //stopService(Intent(this, DetectedActivityService::class.java))
        super.onDestroy()
    }

    private fun setDetectedActivity(supportedActivity: SupportedActivity) {
        //activityImage.setImageDrawable(ContextCompat.getDrawable(this, supportedActivity.activityImage))
        activityTitle.text = getString(supportedActivity.activityText)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACTIVITY_RECOGNITION).not() &&
            grantResults.size == 1 &&
            grantResults[0] == PackageManager.PERMISSION_DENIED) {
            showSettingsDialog(this)
        } else if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION &&
            permissions.contains(Manifest.permission.ACTIVITY_RECOGNITION) &&
            grantResults.size == 1 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("permission_result", "permission granted")
            startService(Intent(this, DetectedActivityService::class.java))
            requestActivityTransitionUpdates()
            isTrackingStarted = true
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