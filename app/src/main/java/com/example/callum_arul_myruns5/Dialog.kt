package com.example.callum_arul_myruns5
import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment


class Dialog : DialogFragment(), DialogInterface.OnClickListener{
    companion object{
        const val DIALOG_KEY = "dialog"
        const val TITLE_KEY = "title"
        const val INPUT_KEY = "input"
        const val ENTRY_KEY = "entry"
        const val PREFS_NAME = "MyDialogPrefs" // SharedPreferences name
        const val PREF_KEY_TEXT = "input_text" // Key for storing text in SharedPreferences

        const val BASIC_DIALOG = 1

    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        val bundle = arguments
        val dialogId = bundle?.getInt(DIALOG_KEY)
        val title = bundle?.getString(TITLE_KEY)
        val inputType = bundle?.getInt(INPUT_KEY)
        val entry = bundle?.getParcelable<ExerciseEntry>(ENTRY_KEY)

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0)

        if(dialogId == BASIC_DIALOG){
            val builder = AlertDialog.Builder(requireActivity())
            val view: View = requireActivity().layoutInflater.inflate(R.layout.basic_dialog,
                null)
            val editText = view.findViewById<EditText>(R.id.edit_text)
            editText.inputType = inputType ?: InputType.TYPE_CLASS_TEXT

            //persist text that user inputted previously
            val key = PREF_KEY_TEXT + title
            val storedText = sharedPreferences.getString(key, "")
            editText.setText(storedText)


            builder.setView(view)
            builder.setTitle(title)

            builder.setPositiveButton("OK") { _, _ ->
                // Save the entered text to SharedPreferences
                val enteredText = editText.text.toString()
                val key = PREF_KEY_TEXT + title
                with(sharedPreferences.edit()) {
                    putString(key, enteredText)
                    apply()
                }
                entry?.updateField(title ?: "", enteredText)
            }
            builder.setNegativeButton("CANCEL", this)
            ret = builder.create()
        }

        return ret
    }

    override fun onClick(p0: DialogInterface?, p1: Int) {

    }

    fun clearDialogSharedPreferences() {
        if (::sharedPreferences.isInitialized) {
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
        }
    }


}