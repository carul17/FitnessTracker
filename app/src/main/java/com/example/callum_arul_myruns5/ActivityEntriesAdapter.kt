package com.example.callum_arul_myruns5

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.preference.PreferenceManager
import java.text.DateFormat

class ActivityEntriesAdapter(context: Context, private var entries: List<ExerciseEntry>) :
    ArrayAdapter<ExerciseEntry>(context, 0, entries) {

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_entry, parent, false)

        val entry = getItem(position)

        val titleView = itemView.findViewById<TextView>(R.id.title)
        val textView = itemView.findViewById<TextView>(R.id.text)


        entry?.let {

            val inputTypes = context.resources.getStringArray(R.array.input_type_array)
            val activityTypes = context.resources.getStringArray(R.array.activity_type_array)

            val inputTypeText = inputTypes[it.inputType]
            val activityTypeText = activityTypes[it.activityType]

            val date = it.dateTime.time

            // Set the text with input type, activity type, and formatted date
            titleView.text = "$inputTypeText $activityTypeText ${DateFormat.getDateTimeInstance().format(date)}"

            // Set distance and duration
            textView.text = "${Util.formatDistance(it.distance, sharedPrefs)} ${Util.formatDuration(it.duration)}"
        }

        return itemView
    }



    fun updateData(newData: List<ExerciseEntry>) {
        clear()
        addAll(newData)
        notifyDataSetChanged()
    }
}