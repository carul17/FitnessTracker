package com.example.callum_arul_myruns5
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Util {
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), 0)
        }

    }

    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
        matrix.setRotate(90f)
        var ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return ret
    }

    fun formatDuration(durationInMinutes: Double): String {
        val wholeMinutes = durationInMinutes.toInt()
        val seconds = ((durationInMinutes - wholeMinutes) * 60).toInt()
        return String.format("%d min %02d sec", wholeMinutes, seconds)
    }

    fun formatDistance(distance: Double, sharedPrefs: SharedPreferences): String {

        val unitPreference = sharedPrefs.getString("unit_preference", "Miles")
        return when (unitPreference) {
            "(Kilometers)" -> {
                // Convert miles to kilometers if the preference is set to kilometers
                val distanceInKilometers = distance * 1.60934
                String.format(Locale.getDefault(), "%.2f Kilometers", distanceInKilometers)
            }
            else -> {
                // Keep distance in miles if the preference is set to miles
                String.format(Locale.getDefault(), "%.2f Miles", distance)
            }
        }
    }

    fun formatCalories(calories: Double): String {
        // Assuming calories and you want to display no decimal places
        return "${calories} cal"
    }

    fun formatSpeed(speed: Double, sharedPrefs: SharedPreferences): String {

        val unitPreference = sharedPrefs.getString("unit_preference", "miles/h")
        return when (unitPreference) {
            "(Kilometers)" -> {
                // Convert miles to kilometers if the preference is set to kilometers
                val speedKmH = speed * 1.60934
                String.format(Locale.getDefault(), "%.2f km/h", speedKmH)
            }
            else -> {
                // Keep distance in miles if the preference is set to miles
                String.format(Locale.getDefault(), "%.2f miles/h", speed)
            }
        }
    }

    fun formatHeartRate(heartRate: Double): String {
        // Assuming heart rate is beats per minute and you want to display no decimal places
        return "${heartRate} bpm"
    }

    fun formatInputType(inputType: Int, context: Context): String {
        val inputTypes = context.resources.getStringArray(R.array.input_type_array)
        return if (inputType in inputTypes.indices) {
            inputTypes[inputType]
        } else {
            "Unknown"
        }
    }

    fun formatActivityType(activityType: Int, context: Context): String {
        val activityTypes = context.resources.getStringArray(R.array.activity_type_array)
        return if (activityType in activityTypes.indices) {
            activityTypes[activityType]
        } else {
            "Unknown"
        }
    }

    fun formatDateTime(calendar: Calendar): String {
        val pattern = "EEE, MMM d, yyyy 'at' h:mm a" // Example pattern, adjust as needed
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }
}
