package com.example.callum_arul_myruns5

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng

class LocationConverter {
    @TypeConverter
    fun fromLocationList(locations: ArrayList<LatLng>): ByteArray {
        // Convert each LatLng to a string and join them with a separator
        val locationString = locations.joinToString(";") {
            "${it.latitude},${it.longitude}"
        }
        // Convert the string to a byte array
        return locationString.toByteArray(Charsets.UTF_8)
    }

    @TypeConverter
    fun toLocationList(blob: ByteArray): ArrayList<LatLng> {
        val locationString = String(blob, Charsets.UTF_8)
        val locations = ArrayList<LatLng>()

        locationString.split(";").forEach { latLngString ->
            try {
                val (latitude, longitude) = latLngString.split(",").map { it.trim().toDouble() }
                locations.add(LatLng(latitude, longitude))
            } catch (e: NumberFormatException) {
                // Log the error or handle the invalid format
                println("Invalid LatLng format: $latLngString")
            }
        }

        return locations
    }
}