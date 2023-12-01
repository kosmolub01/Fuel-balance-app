package com.example.fuelbalanceapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fuelbalanceapp.detectedactivity.DetectedActivityService

class TripRecordingService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 2
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        Log.d("TripRecordingService", "Service created")
        // Additional setup if needed
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TripRecordingService", "Service started")
        // Perform background tasks here
        return START_NOT_STICKY // so it will not be restarted
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TripRecordingService", "Service destroyed")
        stopForeground(true)
        // Cleanup or additional tasks on service destroy - tutaj chyba powinno być obliczenie przebytego dystansu
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


}

