package com.example.callum_arul_myruns5


import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LiveEntryViewModel : ViewModel(), ServiceConnection {
    private var myMessageHandler: MyMessageHandler

    private val _exerciseEntry = MutableLiveData<ExerciseEntry>()
    val exerciseEntry: LiveData<ExerciseEntry> = _exerciseEntry


    init {
        myMessageHandler = MyMessageHandler(Looper.getMainLooper())
    }


    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
        println("debug: ViewModel: onServiceConnected() called; ComponentName: $name")
        val tempBinder = iBinder as TrackingService.MyBinder
        tempBinder.setmsgHandler(myMessageHandler)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        println("debug: Activity: onServiceDisconnected() called~~~")
    }

    inner class MyMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == TrackingService.MSG_ENTRY_VALUE) {
                val bundle = msg.data
                val exerciseEntry = bundle.getParcelable<ExerciseEntry>(TrackingService.ENTRY_KEY)
                exerciseEntry?.let {
                    _exerciseEntry.value = it
                }

            }
        }
    }
}


