package com.example.fuelbalanceapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.fuelbalanceapp.tripshistory.Trip
import com.google.android.gms.location.*
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.math.BigDecimal
import java.math.RoundingMode

class TripRecordingService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 2
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var previousLocation: LocationResult? = null
    private var distance: Double = 0.0
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        dbHelper = DatabaseHelper(this)

        Log.d("TripRecordingService", "onCreate- service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TripRecordingService", "onStartCommand - service started")

        previousLocation = null
        distance = 0.0
        AndroidThreeTen.init(this)

        // Check for location permissions.
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("TripRecordingService", "onStartCommand - insufficient location permissions")
            stopSelf() // Stop the service if permissions are not granted.
            return START_NOT_STICKY
        }

        // Request location updates
        val locationRequest = LocationRequest.create().apply {
            interval = 1000 // Update interval in milliseconds.
            fastestInterval = 500 // Fastest update interval in milliseconds.
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

        return START_NOT_STICKY // so it will not be restarted.
    }

    // Location callback to handle location updates.
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.lastLocation?.let { location ->
                // Handle the received location update
                Log.d(
                    "TripRecordingService",
                    "Location Update - Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                )

                // Save covered distance in km.
                if( previousLocation != null ) {
                    distance +=  (location.distanceTo(previousLocation!!.lastLocation) / 1000)
                    distance = BigDecimal(distance).setScale(2, RoundingMode.HALF_UP).toDouble()
                }

                previousLocation = locationResult

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TripRecordingService", "Service destroyed")
        Log.d("TripRecordingService", "distance = $distance")
        stopForeground(true)

        // Save trip to the DB if distance is greater than 5 m (5 m because of rounding of distance).
        if(distance > 0.0) {
            //distance *= 100000
            val currentDate: LocalDate = LocalDate.now()
            val newTrip = Trip(currentDate, distance)
            insertTrip(newTrip)
        }

        // Cleanup or additional tasks on service destroy - tutaj chyba powinno być obliczenie przebytego dystansu
        // Remove location updates when the service is destroyed
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {

        val notificationChannelId = "foreground_trip_recording_channel_id"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId,
                "Fuel Balance App",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Trip recording started")
            //.setContentText("Tap to open the app")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.drawable.ic_stat_name)
            .build()
    }

    private fun insertTrip(trip: Trip) {
        val db = dbHelper.writableDatabase

        // Format the LocalDate to a String.
        val formattedDate: String = trip.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_DATE, formattedDate)
            put(DatabaseHelper.COLUMN_DISTANCE, trip.distance)
        }

        db.insert(DatabaseHelper.TRIPS_TABLE_NAME, null, values)
        db.close()
    }
}

