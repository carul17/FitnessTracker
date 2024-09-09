package com.example.callum_arul_myruns5

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.ViewModelProvider

class HistoryFragment : Fragment() {

    private lateinit var entryDatabase: ExerciseEntryDatabase
    private lateinit var entryDao: ExerciseEntryDao
    private lateinit var repo: ExerciseEntryRepo
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var entryViewModel: ExerciseEntryViewModel

    private lateinit var adapter: ActivityEntriesAdapter
    private lateinit var listView: ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.history, container, false)

        entryDatabase = ExerciseEntryDatabase.getInstance(requireContext())
        entryDao = entryDatabase.entryDao
        repo = ExerciseEntryRepo(entryDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repo)
        entryViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseEntryViewModel::class.java)

        listView = view.findViewById(R.id.lvExerciseEntries)



        //fill the adapter with table data
        entryViewModel.allExerciseEntriesLiveData.observe(viewLifecycleOwner) { exerciseEntries ->
            Log.d("HistoryFragment", "Observing ${exerciseEntries.size} entries")
            if (!::adapter.isInitialized) {
                adapter = ActivityEntriesAdapter(requireContext(), exerciseEntries)
                listView.adapter = adapter
            } else {
                adapter.updateData(exerciseEntries)
            }

        }


        listView.setOnItemClickListener { _, _, position, _ ->
            val entry = adapter.getItem(position)
            val inputType = entry?.let { Util.formatInputType(entry.inputType, requireContext()) }
            Log.d("HistoryFragment, ListItemCLick", "$inputType")
            if (entry != null) {
                Log.d("HistoryFragment, ListItemCLick", "${entry.duration}")
            }
            if(inputType == "Manual Entry") {
                Log.d("HistoryFragment", "starting manual")
                val manualIntent = Intent(activity, DisplayEntryActivity::class.java)
                manualIntent.putExtra("ENTRY_ID", entry?.id)
                startActivity(manualIntent)
            } else {
                Log.d("HistoryFragment", "starting map")
                val mapIntent = Intent(activity, MapDisplayActivity::class.java)
                mapIntent.putExtra("ENTRY_ID", entry?.id)
                mapIntent.putExtra("ModeType", MapDisplayActivity.HISTORY_MODE)
                startActivity(mapIntent)
                Log.d("HistoryFragment", "started map")
            }
            Log.d("HistoryFragment", "ending if")
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        // Check if the adapter has been initialized
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged() // Refresh the adapter's data
        }

        // Re-establish the click listener for the list view
        listView.setOnItemClickListener { _, _, position, _ ->
            val entry = adapter.getItem(position)
            val inputType = entry?.let { Util.formatInputType(entry.inputType, requireContext()) }
            Log.d("HistoryFragment, ListItemCLick", "$inputType")
            if(inputType == "Manual Entry") {
                val manualIntent = Intent(activity, DisplayEntryActivity::class.java)
                manualIntent.putExtra("ENTRY_ID", entry?.id)
                startActivity(manualIntent)
            }
            else { //GPS or Automatic
                val mapIntent = Intent(activity, MapDisplayActivity::class.java)
                mapIntent.putExtra("ENTRY_ID", entry?.id)
                mapIntent.putExtra("ModeType", MapDisplayActivity.HISTORY_MODE)
                startActivity(mapIntent)
                Log.d("HistoryFragment", "started map")
            }
        }
    }

}