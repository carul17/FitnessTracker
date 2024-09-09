package com.example.callum_arul_myruns5

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner


class StartFragment : Fragment() {
    private lateinit var inputSpinner: Spinner
    private lateinit var activitySpinner: Spinner
    private lateinit var inputAdapter: ArrayAdapter<CharSequence>
    private lateinit var activityAdapter: ArrayAdapter<CharSequence>
    private lateinit var startButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.start, container, false)
        inputSpinner = view.findViewById(R.id.input_type_spinner)
        activitySpinner = view.findViewById(R.id.activity_type_spinner)
        startButton = view.findViewById(R.id.start_button)

        inputAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.input_type_array, android.R.layout.simple_spinner_item)
        activityAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.activity_type_array, android.R.layout.simple_spinner_item)

        inputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        inputSpinner.adapter = inputAdapter
        activitySpinner.adapter = activityAdapter

        inputSpinner.setSelection(0)
        activitySpinner.setSelection(0)

        startButton.setOnClickListener() {
            val inputType = inputSpinner.selectedItemPosition
            val activityType = activitySpinner.selectedItemPosition

            val input = inputSpinner.selectedItem.toString()

            if (input == "Manual Entry") {
                val intent = Intent(requireContext(), ManualActivity::class.java)
                intent.putExtra("InputType", inputType)
                intent.putExtra("ActivityType", activityType)
                startActivity(intent)
            } else { //GPS or Automatic
                val mapIntent = Intent(requireContext(), MapDisplayActivity::class.java)
                mapIntent.putExtra("InputType", inputType)
                mapIntent.putExtra("ActivityType", activityType)
                mapIntent.putExtra("ModeType", MapDisplayActivity.TRACE_MODE)
                startActivity(mapIntent)
            }
        }

        return view
    }
}