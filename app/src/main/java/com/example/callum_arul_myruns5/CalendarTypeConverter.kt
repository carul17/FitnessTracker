package com.example.callum_arul_myruns5

import androidx.room.TypeConverter
import java.util.Calendar

class CalendarTypeConverter {
    @TypeConverter
    fun fromCalendar(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun toCalendar(timeInMillis: Long?): Calendar? {
        if (timeInMillis == null) return null

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return calendar
    }
}