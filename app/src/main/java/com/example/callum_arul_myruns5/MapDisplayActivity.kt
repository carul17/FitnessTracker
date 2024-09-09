package com.example.callum_arul_myruns5

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class MapDisplayActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    companion object{
        val TRACE_MODE = 0
        val HISTORY_MODE = 1
    }
    private var mode = -1

    private var entryId: Long? = null

    //buttons
    private lateinit var cancelButton : Button
    private lateinit var saveButton : Button

    //map
    private lateinit var mMap: GoogleMap
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>

    //service
    private var isBind = false
    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var appContext: Context
    private lateinit var liveEntryViewModel: LiveEntryViewModel
    private val BIND_STATUS_KEY = "bind_status_key"
    private lateinit var backPressedCallback: OnBackPressedCallback
    private lateinit var serviceIntent: Intent
    private lateinit var stopIntent: Intent

    //database
    private lateinit var entryDatabase: ExerciseEntryDatabase
    private lateinit var entryDao: ExerciseEntryDao
    private lateinit var repo: ExerciseEntryRepo
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var exerciseEntryViewModel: ExerciseEntryViewModel

    //status
    private lateinit var statusListView: ListView
    private lateinit var adapter: EntryDetailsAdapter
    //data for the adapter
    private var details = mutableListOf<EntryDetail>()

    private lateinit var sharedPrefs : SharedPreferences

    private fun isMapInitialized() = ::mMap.isInitialized

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_activity)
        statusListView = findViewById(R.id.status_list_view)
        statusListView.divider = null
        statusListView.setOnTouchListener { _, _ ->
            true
        }

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        //initialize map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment
        mapFragment.getMapAsync(this)


        cancelButton = findViewById(R.id.cancel_button)
        saveButton = findViewById(R.id.save_button)

        Log.d("Map", "Created map fragment")

        mode = intent.getIntExtra("ModeType", -1)
        Log.d("Map", "ModeType: $mode")

        //initialize database
        entryDatabase = ExerciseEntryDatabase.getInstance(this)
        entryDao = entryDatabase.entryDao
        repo = ExerciseEntryRepo(entryDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repo)
        exerciseEntryViewModel =
            ViewModelProvider(this, viewModelFactory).get(ExerciseEntryViewModel::class.java)

        stopIntent = Intent()
        stopIntent.action = TrackingService.STOP_SERVICE_ACTION

        adapter = EntryDetailsAdapter(this, details, "map")
        statusListView.adapter = adapter

        if (mode == TRACE_MODE) {
            Log.d("Map", "TraceMode")
            displayTrace()
            if(savedInstanceState != null)
                isBind = savedInstanceState.getBoolean(BIND_STATUS_KEY)
        }
        else if(mode == HISTORY_MODE){
            entryId = intent.getLongExtra("ENTRY_ID", -1)
            Log.d("Map", "Hist mode")
            displayHistory()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()

        if(mode == TRACE_MODE) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                startService(serviceIntent)
            else
                Toast.makeText(this, "Location permission is required for tracing", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unBindService()

        sendBroadcast(stopIntent)
    }

    override fun onStart(){
        super.onStart()
        bindService()
    }


    private fun bindService(){
        if(mode == TRACE_MODE) {
            if (!isBind) {
                appContext.bindService(serviceIntent, liveEntryViewModel, Context.BIND_AUTO_CREATE)
                isBind = true
            }
        }
    }
    private fun unBindService(){
        if(mode == TRACE_MODE) {
            if (isBind) {
                appContext.unbindService(liveEntryViewModel)
                isBind = false
            }
        }
    }

    override fun onMapClick(p0: LatLng) {
    }

    override fun onMapLongClick(p0: LatLng) {
    }


    ////////       History Mode       ///////////

    fun displayHistory(){
        cancelButton.visibility = View.GONE
        saveButton.visibility = View.GONE
        Log.d("MapActivity DisplayHistory", "Entered")
        exerciseEntryViewModel.getEntry(entryId!!).observe(this, Observer { entry ->
            Log.d("MapActivity DisplayHistory", "observing entry")
            if (entry != null) {
                val locationList = entry.locationList
                Log.d("MapActivityDisplayHistory", "${locationList.size}")
                if(!locationList.isEmpty()) {
                    // Clear existing markers and polylines
                    mMap.clear()

                    // Create and add the start marker
                    val startLatLng = locationList.first()
                    val startMarkerOptions = MarkerOptions().position(startLatLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    mMap.addMarker(startMarkerOptions)

                    // Create and add the end marker
                    val endLatLng = locationList.last()
                    val endMarkerOptions = MarkerOptions().position(endLatLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    mMap.addMarker(endMarkerOptions)

                    // Create and add the polyline
                    val polylineOptions = PolylineOptions().addAll(locationList).color(Color.BLACK)
                    mMap.addPolyline(polylineOptions)

                    // Move the camera to the starting point
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(startLatLng, 17f)
                    mMap.animateCamera(cameraUpdate)

                    //display status
                    fillDetails(entry)
                    adapter.notifyDataSetChanged()
                }
            } else {
                Log.e("MapActivity", "Entry not found")
            }

        })
    }

    fun fillDetails(entry: ExerciseEntry){
        details.clear()

        details.add(EntryDetail("Activity Type :   ", Util.formatActivityType(entry.activityType, this)))
        details.add(EntryDetail("Avg Speed :   ", Util.formatSpeed(entry.avgSpeed, sharedPrefs)))
        details.add(EntryDetail("Current Speed :   ", Util.formatSpeed(entry.currSpeed, sharedPrefs)))
        Log.d("Map", "In current speed ${entry.currSpeed}")
        details.add(EntryDetail("Climb :   ", Util.formatDistance(entry.climb, sharedPrefs)))
        details.add(EntryDetail("Calories :   ", Util.formatCalories(entry.calorie)))
        details.add(EntryDetail("Distance :   ", Util.formatDistance(entry.distance, sharedPrefs)))
    }


    ////////       Trace Mode       ///////////

    fun displayTrace(){

        var initialLatLng: LatLng? = null

        //service intent
        serviceIntent = Intent(this, TrackingService::class.java)
        val inputType = intent.getIntExtra("InputType", -1)
        val activityType = intent.getIntExtra("ActivityType", -1)
        serviceIntent.putExtra("InputType", inputType)
        serviceIntent.putExtra("ActivityType", activityType)

        appContext = this.applicationContext
        liveEntryViewModel = ViewModelProvider(this).get(LiveEntryViewModel::class.java)


        //update Map
        liveEntryViewModel.exerciseEntry.observe(this, Observer { entry->
            if (entry.locationList.isNotEmpty() && isMapInitialized()) {
                val latestLocation = entry.locationList.last()
                val latLng = LatLng(latestLocation.latitude, latestLocation.longitude)
                Log.d("MapActivity", "${latLng.latitude},  ${latLng.longitude}")

                if (initialLatLng == null) {
                    // Place the initial green marker
                    val initialMarkerOptions = MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    mMap.addMarker(initialMarkerOptions)
                    initialLatLng = latestLocation
                } else {
                    // Remove all markers except the initial one
                    mMap.clear()
                    val initialMarkerOptions =
                        initialLatLng?.let {
                            MarkerOptions().position(it)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        }
                    if (initialMarkerOptions != null) {
                        mMap.addMarker(initialMarkerOptions)
                    }

                    // Place the new red marker
                    val markerOptions = MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    mMap.addMarker(markerOptions)

                    // Add new point to polyline
                    polylineOptions.add(latLng)
                    mMap.addPolyline(polylineOptions)

                    fillDetails(entry)
                    adapter.notifyDataSetChanged()
                }

                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
                mMap.animateCamera(cameraUpdate)

            }
        })

        handleButtonClick()
    }

    fun handleButtonClick(){

        cancelButton.setOnClickListener() {
            val toast = Toast.makeText(this, "Entry discarded.", Toast.LENGTH_SHORT)
            toast.show()
            unBindService()

            sendBroadcast(stopIntent)

            finish()
        }

        saveButton.setOnClickListener() {
            val toast = Toast.makeText(this, "Entry saved.", Toast.LENGTH_SHORT)
            toast.show()
            unBindService()

            sendBroadcast(stopIntent)
            liveEntryViewModel.exerciseEntry.value?.let { exerciseEntry ->
                exerciseEntryViewModel.insert(exerciseEntry)
            }
            finish()
        }
    }


    ////////       Delete button        ///////////

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if(mode == HISTORY_MODE) {
            menuInflater.inflate(R.menu.menu_display_entry, menu)
            return true
        }
        return false
    }

    //handle delete
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(mode == HISTORY_MODE) {
            return when (item.itemId) {
                R.id.action_delete -> {
                    Log.d("DeleteButton", "Clicked: $entryId")
                    entryId?.let { id ->
                        exerciseEntryViewModel.delete(id)

                        val toast = Toast.makeText(this, "Entry deleted.", Toast.LENGTH_SHORT)
                        toast.show()
                        finish()
                    }
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }
        return false
    }
}