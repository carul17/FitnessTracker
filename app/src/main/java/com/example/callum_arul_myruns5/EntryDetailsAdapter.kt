package com.example.callum_arul_myruns5

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView

//to store details in DisplayEntryActivity
data class EntryDetail(val title: String, val value: String)

class EntryDetailsAdapter(context: Context, details: List<EntryDetail>, type: String) : ArrayAdapter<EntryDetail>(context, 0, details) {
    private val inputType = type
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)

        val view = convertView ?: inflater.inflate(R.layout.list_item_entry, parent, false)

        if(inputType == "map") {
            val layout = view.findViewById<LinearLayout>(R.id.linearLayout)
            layout.orientation = LinearLayout.HORIZONTAL

            val value = view.findViewById<TextView>(R.id.text)
            value.setTypeface(null, Typeface.BOLD)

        }

        val detail = getItem(position)
        val titleTextView = view.findViewById<TextView>(R.id.title)
        val valueTextView = view.findViewById<TextView>(R.id.text)

        titleTextView.text = detail?.title
        valueTextView.text = detail?.value

        return view
    }
}