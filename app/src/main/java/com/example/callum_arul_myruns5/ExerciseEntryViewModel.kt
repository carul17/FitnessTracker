package com.example.callum_arul_myruns5

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class ExerciseEntryViewModel(private val repository: ExerciseEntryRepo) : ViewModel() {
    val allExerciseEntriesLiveData: LiveData<List<ExerciseEntry>> = repository.allEntries.asLiveData()

    fun insert(entry: ExerciseEntry) {
        repository.insert(entry)
    }

    fun delete(id: Long){
        repository.delete(id)
    }
    fun getEntry(id: Long): LiveData<ExerciseEntry?> {
        val result = MutableLiveData<ExerciseEntry?>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(repository.getEntry(id))
        }
        return result
    }
}

class ExerciseEntryViewModelFactory (private val repository: ExerciseEntryRepo) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(ExerciseEntryViewModel::class.java))
            return ExerciseEntryViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}