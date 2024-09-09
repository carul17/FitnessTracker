package com.example.callum_arul_myruns5

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class TrackingService : Service(), LocationListener, SensorEventListener {

    private val PENDINGINTENT_REQUEST_CODE = 0
    private val NOTIFY_ID = 10
    private val CHANNEL_ID = "notification channel"
    private lateinit var myBroadcastReceiver: MyBroadcastReceiver
    private lateinit var notificationManager: NotificationManager

    private lateinit var  myBinder: MyBinder
    private var msgHandler: Handler? = null

    private lateinit var exerciseEntry: ExerciseEntry

    private var totalDistance = 0f
    private var totalClimb = 0f
    private var lastLocation: Location? = null
    private var startTimeMillis: Long = 0
    private var lastUpdateTime: Long = 0
    private var totalElevationGain: Double = 0.0
    private var currSpeed: Double = 0.0

    private lateinit var notificationBuilder: NotificationCompat.Builder


    //sensor
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private val accelerometerDataQueue: Queue<FloatArray> = LinkedList()

    private lateinit var mAsyncTask: OnSensorChangedTask
    private lateinit var mAccBuffer: ArrayBlockingQueue<Double>
    private val mFeatLen = Globals.ACCELEROMETER_BLOCK_CAPACITY + 2

    //list of types through the duration of activity
    private val activityTypes = mutableListOf<Int>()

    private var inputType = 0
    //current activity type
    private var activityType = 0

    companion object{
        val ENTRY_KEY = "entry key"
        val MSG_ENTRY_VALUE = 0

        val STOP_SERVICE_ACTION = "stop service action"
    }

    private lateinit var locationManager: LocationManager


    override fun onCreate() {
        super.onCreate()
        exerciseEntry = ExerciseEntry()
        myBinder = MyBinder()

        initLocationManager()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotification()

        myBroadcastReceiver = MyBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(STOP_SERVICE_ACTION)
        registerReceiver(myBroadcastReceiver, intentFilter)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccBuffer = ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY)
    }

    inner class MyBinder : Binder() {
        fun setmsgHandler(msgHandler: Handler) {
            this@TrackingService.msgHandler = msgHandler
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        println("debug: Service onBind() called")
        val notification = notificationBuilder.build()
        notificationManager.notify(NOTIFY_ID, notification)

        return myBinder
    }


    override fun onUnbind(intent: Intent?): Boolean {
        println("debug: Service onUnBind() called~~~")

        msgHandler = null
        notificationManager.cancel(NOTIFY_ID)

        return true
    }



    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        println("debug: onStartCommand called")

        inputType = intent.getIntExtra("InputType", -1)

        //if automatic, register sensor listener, receive values in different thread
        if(inputType == 2) {
            Log.d("Service type", "Automatic, sensor started")
            val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)

            mAsyncTask = OnSensorChangedTask()
            mAsyncTask.execute()

        }
        else { //GPS mode
            activityType = intent.getIntExtra("ActivityType", -1)
            exerciseEntry.activityType = activityType
        }
        exerciseEntry.inputType = inputType

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        if (locationManager != null)
            locationManager.removeUpdates(this)

        //if automatic, unregister sensor listener and end background task
        if(inputType == 2) {
            //pick activity type with highest frequency
            val finalActivityType = activityTypes.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: -1
            Log.d("Service final", "final activity: $finalActivityType")

            exerciseEntry.activityType = finalActivityType
            mAsyncTask.cancel(true)
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            sensorManager.unregisterListener(this)
        }

        println("debug: onDestroy called")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
        notificationManager.cancel(NOTIFY_ID)
        unregisterReceiver(myBroadcastReceiver)
    }


    ////////       Location       ///////////

    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null)
                onLocationChanged(location)

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0f, this)

        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        //Log.d("onLocationChanged start", "${location.latitude},  ${location.longitude}")

        //location
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)

        //duration
        val currentTimeMillis = System.currentTimeMillis()

        if (startTimeMillis == 0L) {
            startTimeMillis = currentTimeMillis
            lastUpdateTime = currentTimeMillis
        }
        val currentDurationInMillis = System.currentTimeMillis() - startTimeMillis
        val currentDurationInHours = currentDurationInMillis / 3600000.0
        val currentDurationInMinutes = currentDurationInHours * 60

        var speed = 0.0

        lastLocation?.let {
            //distance
            val distance = it.distanceTo(location)
            Log.d("Service", "FirstDist: ${distance.toDouble()}")
            totalDistance += distance
            Log.d("Service", "Total Dist: $totalDistance")

            //elevation
            val elevationDifference = location.altitude - it.altitude
            if (elevationDifference > 0) {
                totalElevationGain += (elevationDifference/1000)
            }

            //curr speed
            val timeElapsed = currentTimeMillis - lastUpdateTime
            Log.d("Service", "Time elapsed: $timeElapsed")
            Log.d("Service", "Distance: $distance")
            if (timeElapsed > 0) {
                speed = distance / (timeElapsed / 1000.0) // Speed in meters/second
                Log.d("Service", "Speed: $speed")
                currSpeed = speed * 3.6 / 1.60934 // Convert to miles/h
                exerciseEntry.currSpeed = currSpeed //km/h
                Log.d("Service", "Speed curr: $currSpeed")
            }
        }


        //calories
        var METS = .0;
        if(activityType == 0) // standing
            METS = 1.59
        else if(activityType == 1) // walking
            METS = 2.9
        else // running/other
            METS = 8.0
        // calories = METS * average weight in kg * hours
        val calories = METS * 70 * currentDurationInHours

        //avg speed
        val avgSpeed = if (currentDurationInHours > 0) totalDistance / (currentDurationInHours * 1000) else 0f


        Log.d("Service", "Before apply speed current ${currSpeed}")
        exerciseEntry.apply {
            this.distance = totalDistance.toDouble() / (1000*1.60934) // miles default
            this.duration = currentDurationInMinutes //min
            this.calorie = calories //cal
            this.avgSpeed = avgSpeed.toDouble() / 1.60934 // miles/h default
            Log.d("Service", "Apply speed current ${this.currSpeed}")
            this.climb = totalElevationGain / 1.60934 //miles default
            this.locationList.add(latLng)
        }

        lastLocation = location
        lastUpdateTime = currentTimeMillis

        if (msgHandler != null) {

            val message = msgHandler!!.obtainMessage()
            message.data = Bundle().apply {
                putParcelable(TrackingService.ENTRY_KEY, exerciseEntry)
            }
            message.what = MSG_ENTRY_VALUE
            msgHandler!!.sendMessage(message)
            Log.d(
                "onLocationChanged message sent",
                "${lat},  ${lng}"
            )
        }

    }



    ////////       SENSOR       ///////////

    override fun onSensorChanged(event: SensorEvent?) {
        //add data to queue
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                val m = Math.sqrt((event.values[0] * event.values[0] + event.values[1] * event.values[1] + (event.values[2]
                        * event.values[2])).toDouble())

                try {
                    mAccBuffer.add(m)
                } catch (e: IllegalStateException) {

                    val newBuf = ArrayBlockingQueue<Double>(mAccBuffer.size * 2)
                    mAccBuffer.drainTo(newBuf)
                    mAccBuffer = newBuf
                    mAccBuffer.add(m)
                }
            }
        }
    }

    //create and process feature vector in background
    inner class OnSensorChangedTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg arg0: Void?): Void? {
            val featureVector = DoubleArray(mFeatLen - 1)
            var blockSize = 0
            val accBlock = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val im = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)

            while (true) {
                try {
                    // need to check if the AsyncTask is cancelled or not in the while loop
                    if (isCancelled() == true) {
                        return null
                    }

                    // Dumping buffer
                    accBlock[blockSize++] = mAccBuffer.take().toDouble()
                    if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0
                        processFeatureVector(accBlock, im, featureVector)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun processFeatureVector(accBlock: DoubleArray, im: DoubleArray, featureVector: DoubleArray) {
        var max = Double.MIN_VALUE
        val fft = FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY)

        for (`val` in accBlock) {
            if (max < `val`) max = `val`
        }

        fft.fft(accBlock, im)

        for (i in accBlock.indices) {
            val mag = Math.sqrt(accBlock[i] * accBlock[i] + im[i] * im[i])
            featureVector[i] = mag
            im[i] = 0.0
        }
        featureVector[Globals.ACCELEROMETER_BLOCK_CAPACITY] = max

        classifyAndStoreLabel(featureVector)
    }

    private fun classifyAndStoreLabel(featureVector: DoubleArray) {
        val objectArray = featureVector.map { it as Any }.toTypedArray()
        activityType = WekaClassifier.classify(objectArray).toInt()
        activityTypes.add(activityType)
        exerciseEntry.activityType = activityType
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }




    ////////       NOTIFICATIONS       ///////////

    fun createNotification() {
        val resumeAppIntent = Intent(this, MapDisplayActivity::class.java)
        resumeAppIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, PENDINGINTENT_REQUEST_CODE,
            resumeAppIntent, PendingIntent.FLAG_IMMUTABLE
        )
        notificationBuilder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
        notificationBuilder.setContentTitle("MyRuns")
        notificationBuilder.setContentText("Recording your path now")
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setSmallIcon(R.drawable.rocket)


        if (Build.VERSION.SDK_INT > 26) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "channel name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }


    inner class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stopSelf()
            unregisterReceiver(myBroadcastReceiver)
        }
    }
}