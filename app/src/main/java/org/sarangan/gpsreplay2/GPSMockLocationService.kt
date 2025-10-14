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


    private fun initGPS() {
        Log.d(TAG, "GPSMockLocationService initGPS")
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mockLocation = Location(LocationManager.GPS_PROVIDER)
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
        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos())
        //mockLocation.elapsedRealtimeNanos = System.nanoTime()
        mockLocation.setAccuracy(5.0F)
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "GPSMockLocationService onStartCommand")

        //val gpsUpdateInterval: Long = 100 //Update every 100ms
        //var ticks: Int   //This is the counter to indicate how many 100ms intervals have lapsed between each GPS data

 //       try{
            initGPS()
            val notification = TrackPlayServiceNotification().getNotification(
                "MockGPS is Running",
                applicationContext
            )
            startForeground(1, notification)    //Start foreground with notification.
            //Log.d(TAG, "GPSMockLocationService has been started")
//        }
//        catch (e: SecurityException){//if initGPS crashes because mock GPS has not been enabled, disable play and enable mockGPSEnabled.
//            val notification = TrackPlayServiceNotification().getNotification(
//                "Mock GPS is not enabled",
//                applicationContext
//            )
//            startForeground(1, notification)
//            Log.d(TAG, "Track Play Service Security Exception")
//        }

        Thread {
            var pt = 0
//            while (true) {
                while (pt < Data.numOfPoints) {
                    if (Data.seekBarMoved) {
                        pt = Data.seekBarPoint
                        Data.seekBarMoved = false
                    }
                    Data.currentPoint = pt
                    mockLocation.latitude = Data.trackPoints[pt].lat
                    mockLocation.longitude = Data.trackPoints[pt].lon
                    mockLocation.setAltitude(Data.trackPoints[pt].altitude)
                    mockLocation.setSpeed(Data.trackPoints[pt].speed)
                    mockLocation.setBearing(Data.trackPoints[pt].trueCourse)
                    mockLocation.time = System.currentTimeMillis()
                    //mockLocation.setTime(Data.trackPoints[pt].epoch + Data.timeOffset)
                    locationManager.setTestProviderLocation(
                        LocationManager.GPS_PROVIDER,
                        mockLocation
                    )
                    Thread.sleep(GPSUpdate_ms)
                    pt++
                        //Log.d(TAG, "GPSMockLocationService is Running")
                    }
            //Data.numOfPoints = 0
            stopSelf()
            Data.mockGPSServiceLaunched = false
            val notification = TrackPlayServiceNotification().getNotification(
                "MockGPS has ended",
                applicationContext
            )
            startForeground(2, notification)    //Start foreground with notification.
//                }
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