package com.example.callum_arul_myruns5

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class ExerciseEntryRepo(private val entryDao: ExerciseEntryDao) {
    val allEntries: Flow<List<ExerciseEntry>> = entryDao.getAllEntries()


    fun insert(entry: ExerciseEntry) {
        CoroutineScope(Dispatchers.IO).launch{
            entryDao.insertEntry(entry)
        }
    }

    fun delete(id: Long){
        Log.d("HistoryFragment", "Delete operation started for ID: $id")
        CoroutineScope(Dispatchers.IO).launch {
            entryDao.deleteEntry(id)
        }
        Log.d("HistoryFragment", "Delete operation completed for ID: $id")
    }

    suspend fun getEntry(id: Long): ExerciseEntry? {
        return entryDao.getEntryById(id)
    }
}