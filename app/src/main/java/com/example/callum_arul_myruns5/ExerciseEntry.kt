package com.example.callum_arul_myruns5

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import java.util.Calendar

@Parcelize
@Entity(tableName = "exercise_entries")
@TypeConverters(LocationConverter::class)
data class ExerciseEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var inputType: Int = 0,
    var activityType: Int = 0,
    var dateTime: Calendar = Calendar.getInstance(),
    var duration: Double = 0.0,
    var distance: Double = 0.0,
    var avgPace: Double = 0.0,
    var avgSpeed: Double = 0.0,
    var currSpeed: Double = 0.0,
    var calorie: Double = 0.0,
    var climb: Double = 0.0,
    var heartRate: Double = 0.0,
    var comment: String = "",
    var locationList: ArrayList<LatLng> = arrayListOf()
) : Parcelable {
    fun updateField(fieldTitle: String, value: String) {
        when (fieldTitle) {
            "Duration" -> duration = value.toDoubleOrNull() ?: 0.0
            "Distance" -> distance = value.toDoubleOrNull() ?: 0.0
            "Average Pace" -> avgPace = value.toDoubleOrNull() ?: 0.0
            "Average Speed" -> avgSpeed = value.toDoubleOrNull() ?: 0.0
            "Current Speed" -> avgSpeed = value.toDoubleOrNull() ?: 0.0
            "Calories" -> calorie = value.toDoubleOrNull() ?: 0.0
            "Climb" -> climb = value.toDoubleOrNull() ?: 0.0
            "Heart Rate" -> heartRate = value.toDoubleOrNull() ?: 0.0
            "Comment" -> comment = value
        }

    }
}