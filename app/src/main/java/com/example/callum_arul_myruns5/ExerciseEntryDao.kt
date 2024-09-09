package com.example.callum_arul_myruns5

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseEntryDao {
    @Query("SELECT * FROM exercise_entries")
    fun getAllEntries(): Flow<List<ExerciseEntry>>

    @Insert
    suspend fun insertEntry(entry: ExerciseEntry)

    @Query("DELETE FROM exercise_entries WHERE id = :key")
    suspend fun deleteEntry(key: Long)

    @Query("SELECT * FROM exercise_entries WHERE id = :key")
    suspend fun getEntryById(key: Long): ExerciseEntry?
}