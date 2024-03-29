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
)

fun Activity.requestPermission() {
    ActivityCompat.requestPermissions(this, permissions,
        PERMISSION_REQUEST)
    Log.d("requestPermission", "requestPermissions")
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
         return permissionsGranted.all { it } && PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
             this,
             Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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