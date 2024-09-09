package com.example.callum_arul_myruns5

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ExerciseEntry::class], version = 5, exportSchema = false)
@TypeConverters(CalendarTypeConverter::class)
abstract class ExerciseEntryDatabase : RoomDatabase() {
    abstract val entryDao: ExerciseEntryDao

    companion object{
        @Volatile
        private var INSTANCE: ExerciseEntryDatabase? = null

        fun getInstance(context: Context) : ExerciseEntryDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        ExerciseEntryDatabase::class.java, "exercise_entries")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
