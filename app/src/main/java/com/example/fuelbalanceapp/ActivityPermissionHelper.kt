package com.example.fuelbalanceapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat

const val PERMISSION_REQUEST = 1000

val permissions = arrayOf(
    Manifest.permission.ACTIVITY_RECOGNITION,
    Manifest.permission.ACCESS_FINE_LOCATION,
    //Manifest.permission.ACCESS_BACKGROUND_LOCATION
)

fun Activity.requestPermission() {
    var shouldShowRequestPermissionRationale : Boolean = false

    for (permission in permissions) {
        shouldShowRequestPermissionRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            permission).not()
    }

    if (shouldShowRequestPermissionRationale) {
        ActivityCompat.requestPermissions(this, permissions,
            PERMISSION_REQUEST)
        Log.d("requestPermission", "requestPermissions")
    } else {
        Log.d("requestPermission", "showRationalDialog")
        showRationalDialog(this)
    }
}

fun Activity.arePermissionsGranted(): Boolean {
    val isAndroidQOrLater: Boolean =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

     if (isAndroidQOrLater.not()) {
         return true
    } else {
        var permissionsGranted : BooleanArray = booleanArrayOf(false, false)

        for (i in permissions.indices) {
            permissionsGranted[i] = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                permissions[i])

            Log.d("arePermissionsGranted", "permission: ${permissions[i]}")
            Log.d("arePermissionsGranted", "permissionsGranted: ${permissionsGranted[i]}")

        }
         return permissionsGranted.all { it }
    }
}

private fun showRationalDialog(activity: Activity) {
    AlertDialog.Builder(activity).apply {
        setTitle(R.string.permission_rational_dialog_title)
        setMessage(R.string.permission_rational_dialog_message)
        setPositiveButton(R.string.permission_rational_dialog_positive_button_text) { _, _ ->
            ActivityCompat.requestPermissions(activity, permissions,
                PERMISSION_REQUEST)
        }
        setNegativeButton(R.string.permission_rational_dialog_negative_button_text){ dialog, _ ->
            dialog.dismiss()
        }
    }.run {
        create()
        show()
    }
}

fun showSettingsDialog(activity: Activity){
    AlertDialog.Builder(activity).apply {
        setTitle(R.string.settings_dialog_title)
        setMessage(R.string.settings_dialog_message)
        setPositiveButton(R.string.settings_dialog_positive_button_text) { _, _ ->
            startAppSettings(activity)
        }
        setNegativeButton(R.string.settings_dialog_negative_button_text){ dialog, _ ->
            dialog.dismiss()
        }
    }.run {
        create()
        show()
    }
}

private fun startAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri: Uri = Uri.fromParts("package", context.packageName, null)
    intent.data = uri
    context.startActivity(intent)
}