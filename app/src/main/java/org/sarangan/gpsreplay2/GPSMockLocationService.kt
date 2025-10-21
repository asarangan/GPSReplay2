package org.sarangan.gpsreplay2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat


class GPSMockLocationService: Service() {


    lateinit var locationManager: LocationManager
    lateinit var mockLocation: Location


    inner class MyServiceBinder : Binder() {
        fun getService(): GPSMockLocationService = this@GPSMockLocationService
    }

    private val myBinder = MyServiceBinder()


    override fun onBind(intent: Intent?): IBinder? {
        //TODO("Not yet implemented")
        return myBinder
    }

    fun doSomethingUseful(): String {
        return "Service is doing something useful!"
    }




    override fun onCreate() {
        Log.d(TAG, "GPSMockLocationService onCreate start")
        super.onCreate()
        Log.d(TAG, "GPSMockLocationService onCreate exit")
    }

    override fun onDestroy() {
        Log.d(TAG, "GPSMockLocationService onDestroy start")
        super.onDestroy()
        Log.d(TAG, "GPSMockLocationService onDestroy exit")
    }


    private fun initGPS() {
        Log.d(TAG, "GPSMockLocationService initGPS")
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.addTestProvider(
            LocationManager.GPS_PROVIDER,
            false,
            false,
            false,
            false,
            true,
            true,
            true,
            ProviderProperties.POWER_USAGE_HIGH,
            ProviderProperties.ACCURACY_FINE
        )
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
        mockLocation = Location(LocationManager.GPS_PROVIDER)

        //mockLocation.elapsedRealtimeNanos = System.nanoTime()


        //mockLocation.elapsedRealtimeNanos = System.nanoTime()
        mockLocation.setAccuracy(5.0F)
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "GPSMockLocationService onStartCommand")

            initGPS()
            val notification = TrackPlayServiceNotification().getNotification(
                "MockGPS is Running",
                applicationContext
            )
            startForeground(1, notification)    //Start foreground with notification.


        Thread {
            var pt = Data.currentPoint
                while (pt < Data.numOfPoints-1) {
                    if (Data.seekBarMoved) {
                        pt = Data.seekBarPoint
                        Data.seekBarMoved = false
                        Data.trackStartTime = Data.trackPoints[pt].epoch
                        Data.serviceStartTime = System.currentTimeMillis()
                    }
                    Data.currentPoint = pt
                    mockLocation.latitude = Data.trackPoints[pt].lat
                    mockLocation.longitude = Data.trackPoints[pt].lon
                    mockLocation.setAltitude(Data.trackPoints[pt].altitude)
                    mockLocation.setSpeed(Data.trackPoints[pt].speed)
                    mockLocation.setBearing(Data.trackPoints[pt].trueCourse)
                    mockLocation.time = System.currentTimeMillis()
                    mockLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()


                    val correction = (mockLocation.time - Data.serviceStartTime) - (Data.trackPoints[pt].epoch- Data.trackStartTime)
                    var sleepTime =  (Data.trackPoints[pt+1].epoch - Data.trackPoints[pt].epoch) - correction
                    if (sleepTime < 0){sleepTime = 0}


                    Log.d(TAG,"MockGPS Service: ${pt.toString()}. Sleep: ${sleepTime}. Correction: ${correction}")

                    locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation)

                    Thread.sleep(sleepTime)
                    pt++

                    Data.mockGPSServiceIsRunning = true
                    if (Data.stopService){break}
                    }
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            stopSelf()
            Data.mockGPSServiceIsRunning = false
        }.start()

        return super.onStartCommand(intent, flags, startId)
    }
}

class TrackPlayServiceNotification() {

    private val channelID = "SERVICESTACK_CHANNEL_ID"
    private val channelName = "SERVICESTACK_CHANNEL_NAME"



    fun getNotification(message:String, trackPlayContext: Context): Notification {

        (trackPlayContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        )

        val aa: NotificationCompat.Builder =
            NotificationCompat.Builder(trackPlayContext, channelID)
        aa.setContentTitle(message)
        aa.setContentText("Mock GPS is Running")
        aa.setSmallIcon(R.drawable.ic_launcher_foreground)
        aa.priority = NotificationCompat.PRIORITY_HIGH

        return aa.build()
    }
}