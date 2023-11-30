package com.example.fuelbalanceapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class TripRecordingService : Service() {

    override fun onCreate() {
        super.onCreate()
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
        // Cleanup or additional tasks on service destroy
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

