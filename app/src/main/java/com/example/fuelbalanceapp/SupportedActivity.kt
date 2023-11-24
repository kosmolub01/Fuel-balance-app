package com.example.fuelbalanceapp

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.gms.location.DetectedActivity
import java.lang.IllegalArgumentException

const val SUPPORTED_ACTIVITY_KEY = "activity_key"

enum class SupportedActivity(
    @DrawableRes val activityImage: Int,
    @StringRes val activityText: Int
) {

    NOT_ENABLED(R.drawable.time_to_start, R.string.enable_trips_recording),
    STILL(R.drawable.dog_standing, R.string.still_text),
    WALKING(R.drawable.dog_walking, R.string.walking_text),
    RUNNING(R.drawable.dog_running, R.string.running_text),
    IN_VEHICLE(R.drawable.dog_standing, R.string.in_vehicle_text);


    companion object {

        fun fromActivityType(type: Int): SupportedActivity = when (type) {
            DetectedActivity.STILL -> STILL
            DetectedActivity.WALKING -> WALKING
            DetectedActivity.RUNNING -> RUNNING
            DetectedActivity.IN_VEHICLE -> IN_VEHICLE
            else -> throw IllegalArgumentException("activity $type not supported")
        }
    }
}