package com.example.fuelbalanceapp.detectedactivity

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.ActivityRecognitionClient
import com.example.fuelbalanceapp.*
import com.google.android.gms.location.DetectedActivity

const val ACTIVITY_UPDATES_INTERVAL = 0L

class DetectedActivityService : Service() {

    inner class LocalBinder : Binder() {

        val serverInstance: DetectedActivityService
            get() = this@DetectedActivityService
    }

    override fun onBind(p0: Intent?): IBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        requestActivityUpdates()
    }

    private fun requestActivityUpdates() {
        val task = ActivityRecognitionClient(this).requestActivityUpdates(ACTIVITY_UPDATES_INTERVAL,
            DetectedActivityReceiver.getPendingIntent(this))

        task.run {
            addOnSuccessListener {
                Log.d("ActivityUpdate", getString(R.string.activity_update_request_success))
            }
            addOnFailureListener {
                Log.d("ActivityUpdate", getString(R.string.activity_update_request_failed))
            }
        }
    }

    private fun removeActivityUpdates() {
        val task = ActivityRecognitionClient(this).removeActivityUpdates(
            DetectedActivityReceiver.getPendingIntent(this))

        task.run {
            addOnSuccessListener {
                Log.d("ActivityUpdate", getString(R.string.activity_update_remove_success))
            }
            addOnFailureListener {
                Log.d("ActivityUpdate", getString(R.string.activity_update_remove_failed))
            }
        }
    }

    private fun createNotification(): Notification {

        val notificationChannelId = "foreground_activity_detection_channel_id"
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
            .setContentTitle("Trips recording is ON")
            .setContentText("Tap to open the app")
            .setContentIntent(getPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.drawable.ic_stat_name)
            .build()
    }

    private fun getPendingIntent(): PendingIntent {
        // Add an intent to open your app when the notification is tapped.
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(this, 0, intent, 0)
    }

    override fun onDestroy() {
        super.onDestroy()

        removeActivityUpdates()
        stopForeground(true)

        // Stop tripRecordingService.
        val tripRecordingServiceIntent = Intent(this, TripRecordingService::class.java)
        this.stopService(tripRecordingServiceIntent)

        NotificationManagerCompat.from(this).cancel(DETECTED_ACTIVITY_NOTIFICATION_ID)
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}