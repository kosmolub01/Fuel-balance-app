package com.example.fuelbalanceapp.detectedactivity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.example.fuelbalanceapp.MainActivity
import com.example.fuelbalanceapp.R
import com.example.fuelbalanceapp.SUPPORTED_ACTIVITY_KEY
import com.example.fuelbalanceapp.SupportedActivity

private const val DETECTED_PENDING_INTENT_REQUEST_CODE = 100
private const val RELIABLE_CONFIDENCE = 75

const val DETECTED_ACTIVITY_CHANNEL_ID = "detected_activity_channel_id"
const val DETECTED_ACTIVITY_NOTIFICATION_ID = 10

class DetectedActivityReceiver : BroadcastReceiver() {

    companion object {

        fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, DetectedActivityReceiver::class.java)
            return PendingIntent.getBroadcast(context, DETECTED_PENDING_INTENT_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            Log.d("DetectedActivityRec", "onReceive")
            val result = ActivityRecognitionResult.extractResult(intent)
            result?.let { handleDetectedActivities(it.probableActivities, context) }
        }
    }

    private fun handleDetectedActivities(detectedActivities: List<DetectedActivity>,
                                         context: Context) {
        detectedActivities
            .filter {
                it.type == DetectedActivity.STILL ||
                        it.type == DetectedActivity.WALKING ||
                        it.type == DetectedActivity.RUNNING ||
                        it.type == DetectedActivity.IN_VEHICLE
            }
            .filter { it.confidence > RELIABLE_CONFIDENCE }
            .run {
                if (isNotEmpty()) {
                    Log.d("DetectedActReceiver", "handleDetectedActivities()")
                    showNotification(this[0], context)
                }
            }
    }

    private fun showNotification(detectedActivity: DetectedActivity, context: Context) {
        createNotificationChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(SUPPORTED_ACTIVITY_KEY, SupportedActivity.fromActivityType(detectedActivity.type))
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        val activity = SupportedActivity.fromActivityType(detectedActivity.type)

        val builder = NotificationCompat.Builder(context, DETECTED_ACTIVITY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(activity.activityText))
            .setContentText("With ${detectedActivity.confidence}% confidence")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(DETECTED_ACTIVITY_NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "detected_activity_channel_name"
            val descriptionText = "detected_activity_channel_description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(DETECTED_ACTIVITY_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(false)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}