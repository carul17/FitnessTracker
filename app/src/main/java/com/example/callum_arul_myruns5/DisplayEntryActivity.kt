package com.example.callum_arul_myruns5

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager

class DisplayEntryActivity : AppCompatActivity() {
    private var entryId: Long? = null

    private lateinit var entryDatabase: ExerciseEntryDatabase
    private lateinit var entryDao: ExerciseEntryDao
    private lateinit var repo: ExerciseEntryRepo
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var entryViewModel: ExerciseEntryViewModel

    private lateinit var listView: ListView
    private lateinit var adapter: EntryDetailsAdapter
    //data for the adapter
    private var details = mutableListOf<EntryDetail>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_entry)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        entryDatabase = ExerciseEntryDatabase.getInstance(this)
        entryDao = entryDatabase.entryDao
        repo = ExerciseEntryRepo(entryDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repo)
        entryViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseEntryViewModel::class.java)

        listView = findViewById<ListView>(R.id.lvEntryDetails)

        entryId = intent.getLongExtra("ENTRY_ID", -1)


        //Populate adapter and listView with entry details
        entryViewModel.getEntry(entryId!!).observe(this, Observer { entry ->

            details.clear()
            if (entry != null) {
                details.add(EntryDetail("Input Type", Util.formatInputType(entry.inputType, this)))
                details.add(EntryDetail("Activity Type", Util.formatActivityType(entry.activityType, this)))
                details.add(EntryDetail("Date and Time", Util.formatDateTime(entry.dateTime)))
                details.add(EntryDetail("Duration", Util.formatDuration(entry.duration)))
                details.add(EntryDetail("Distance", Util.formatDistance(entry.distance, sharedPrefs)))
                details.add(EntryDetail("Calories", Util.formatCalories(entry.calorie)))
                details.add(EntryDetail("Heart Rate", Util.formatHeartRate(entry.heartRate)))
                if(!entry.comment.isNullOrEmpty())
                    details.add(EntryDetail("Comment", entry.comment))
            }
            else {
                Log.e("DisplayEntryActivity", "Entry not found")
            }
            adapter.notifyDataSetChanged()
        })
        adapter = EntryDetailsAdapter(this, details, "manual")
        listView.adapter = adapter

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_display_entry, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //handle delete
        return when (item.itemId) {
            R.id.action_delete -> {
                Log.d("DeleteButton", "Clicked: $entryId")
                entryId?.let { id ->
                    entryViewModel.delete(id)

                    val toast = Toast.makeText(this, "Entry deleted.", Toast.LENGTH_SHORT)
                    toast.show()
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}