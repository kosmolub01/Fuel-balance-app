/*
 * This file is modified version of file from Kodeco tutorial from raywenderlich.com.
 *
 * Copyright (c) 2021 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.example.fuelbalanceapp.detectedactivity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fuelbalanceapp.*
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException

private const val DETECTED_PENDING_INTENT_REQUEST_CODE = 100
private const val RELIABLE_CONFIDENCE = 75

private const val PREVIOUS_ACTIVITY_KEY = "previousActivity"

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
                    val activity = when (this[0].type) {
                        DetectedActivity.STILL -> SupportedActivity.STILL
                        DetectedActivity.WALKING -> SupportedActivity.WALKING
                        DetectedActivity.RUNNING -> SupportedActivity.RUNNING
                        DetectedActivity.IN_VEHICLE -> SupportedActivity.IN_VEHICLE
                        else -> throw IllegalArgumentException("activity not supported")
                    }
                    Log.d("DetectedActReceiver", "DetectedActivity: $activity")

                    // Load previousActivity from SharedPreferences
                    // and if transition to or from IN_VEHICLE state is detected,
                    // then start or stop service responsible for recording a trip.
                    val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
                    val previousActivity: String? = sharedPreferences.getString(PREVIOUS_ACTIVITY_KEY, "NO_IN_VEHICLE")

                    val tripRecordingServiceIntent = Intent(context, TripRecordingService::class.java)

                    // NO_WALKING -> WALKING || NO_IN_VEHICLE -> IN_VEHICLE
                    if (this[0].type == DetectedActivity.IN_VEHICLE && previousActivity == "NO_IN_VEHICLE") {
                        savePreviousActivityToSharedPreferences(context, "IN_VEHICLE")
                        context.startService(tripRecordingServiceIntent)
                    }
                    // WALKING -> NO_WALKING || IN_VEHICLE -> NO_IN_VEHICLE
                    else if (this[0].type != DetectedActivity.IN_VEHICLE && previousActivity == "IN_VEHICLE") {
                        context.stopService(tripRecordingServiceIntent)
                        savePreviousActivityToSharedPreferences(context, "NO_IN_VEHICLE")
                    }

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
            .setSmallIcon(R.drawable.ic_stat_name)
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

 fun savePreviousActivityToSharedPreferences(context: Context, previousActivity: String) {
    // Save previousActivity to SharedPreferences.
    val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    editor.putString(PREVIOUS_ACTIVITY_KEY, previousActivity)
    editor.apply()
}