package com.example.callum_arul_myruns5

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.ListView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar

class ManualActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
    val items = arrayOf(
        "Date",
        "Time",
        "Duration",
        "Distance",
        "Calories",
        "Heart Rate",
        "Comment"
    )

    private lateinit var adapter : ArrayAdapter<String>
    private lateinit var listView : ListView
    private lateinit var cancelButton : Button
    private lateinit var saveButton : Button
    private lateinit var myDialog : Dialog

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy")


    private lateinit var entryDatabase: ExerciseEntryDatabase
    private lateinit var entryDao: ExerciseEntryDao
    private lateinit var repo: ExerciseEntryRepo
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var entryViewModel: ExerciseEntryViewModel

    private lateinit var exerciseEntry: ExerciseEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manual_activity)

        exerciseEntry = ExerciseEntry()
        listView = findViewById(R.id.listViewDetails)
        cancelButton = findViewById(R.id.cancel_button)
        saveButton = findViewById(R.id.save_button)
        myDialog = Dialog()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList(items.asList()))
        listView.adapter = adapter


        //initialize database classes
        entryDatabase = ExerciseEntryDatabase.getInstance(this)
        entryDao = entryDatabase.entryDao
        repo = ExerciseEntryRepo(entryDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repo)
        entryViewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseEntryViewModel::class.java)



        val inputType = intent.getIntExtra("InputType", -1)
        val activityType = intent.getIntExtra("ActivityType", -1)
        exerciseEntry.inputType = inputType
        exerciseEntry.activityType = activityType

        handleCancelButtonClick()
        handleSaveButtonClick()
        handleListViewClick()

    }

    private fun handleListViewClick(){
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = adapter.getItem(position)
            val bundle = Bundle()
            bundle.putInt(Dialog.DIALOG_KEY, Dialog.BASIC_DIALOG)

            if (item == "Date") {
                showDatePickerDialog()
            }
            else if (item == "Time") {
                showTimePickerDialog()
            }
            else{
                bundle.putString(Dialog.TITLE_KEY, item)
                bundle.putInt(Dialog.INPUT_KEY, if (item == "Comment") InputType.TYPE_CLASS_TEXT else InputType.TYPE_NUMBER_FLAG_DECIMAL)
                bundle.putParcelable(Dialog.ENTRY_KEY, exerciseEntry)
                myDialog.arguments = bundle
                myDialog.show(supportFragmentManager, "my dialog")
            }
        }
    }
    private fun handleCancelButtonClick() {
        cancelButton.setOnClickListener() {
            myDialog.clearDialogSharedPreferences()
            val toast = Toast.makeText(this, "Entry discarded.", Toast.LENGTH_SHORT)
            toast.show()
            finish()
        }
    }



    private fun handleSaveButtonClick() {
        saveButton.setOnClickListener() {
            myDialog.clearDialogSharedPreferences()


            entryViewModel.insert(exerciseEntry)

            val toast = Toast.makeText(this, "Entry saved.", Toast.LENGTH_SHORT)
            toast.show()
            finish()
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this, this,calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            this, this,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE), true
        )
        timePickerDialog.show()
    }


    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        exerciseEntry.dateTime = calendar
    }
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)

        exerciseEntry.dateTime = calendar
    }

}