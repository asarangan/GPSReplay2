package org.sarangan.gpsreplay2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.net.Uri
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import java.io.BufferedReader
import java.io.IOException
import java.lang.Thread.sleep
import java.util.Date
import kotlin.Int
import kotlin.Long
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


const val TAG:String = "GPS"


//Description of this program:
//The main screen layout contains two FrameLayouts. The top one occupies 80% of the area. Bottom one occupies 20%. The navigation menu is on the bottom.
//The top FrameLayout will be used to display the fragments.
//There are two fragments - runFragment and fileFragment. Both fragments are initialized, but one will be put on pause and hidden. There is a listener on the
//bottom navigation menu which will trigger which fragment will be resumed and shown and which one will be paused and hidden.
//None of the fragment lifecycles will be run until the MainActivity onCreate exits, and onStart runs. Then each fragment will go through
//onAttach -> onCreate -> onCreateView -> onViewCreated -> onStart
//In another lifecycle of the MainActivity (not sure which one), the fragments also undergo onResume
//After that, when switching between the fragments we will run onPause on one and onResume on the other, as well as hide and show operation.
//When fileFragment was instantiated, it will have also created a dummy gpxReadData object (it is declared in the constructor as a lateinit var, but it will get
//properly initialized in onCreateView cycle). Inside gpxReadData there is a data object - it will be all zeros.
//A few other tasks are done in runFragment's onResume.
//seekbar is set to the maximum number of points in the data file. In order to make sure that the data object is not null when the fragments are first created, we need to
//pass the data object to runFragment before onResume is called. This can be done in the MainActivity's onResume, which is the last call before the fragment's onResume is
//executed.
//In fileFragment, there is a button for loading a file. The button has a listener. When clicked it opens a file selector. It passes the selected file to
//gpxReadData object (through ActivityResultContracts). Actually, it is the onActivityResult cycle inside the gpxReadData object that gets executed.
//This is where all the data is read in. It will populate the data object.
//While file selector is open, both fragments will execute the onPause and onStop lifecycles (not relevant for us, but could come in useful).
//Once the file is read, and the gpxReadData object returns with populated data, the fragments will run onStart and onResume (we have code in runFragment's onResume)



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"MainActivity onCreate - start")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout)

        //This is for reading the external file, and passing the Uri to the callback object (which is also defined here within {}
        val gpxFilePicker = registerForActivityResult(ActivityResultContracts.GetContent())
        { uri: Uri? ->
            uri?.let {

                var inputStream = contentResolver.openInputStream(uri)
                val bufferedReader = BufferedReader(inputStream?.reader())
                //Some GPX files do not contain a speed tag. If that is the case, we will have to calculate the speed.
                var speedExists = false
                var line: String?
                do {
                    line = bufferedReader.readLine()
                    if (line?.contains("<speed>", ignoreCase = true) == true) {
                        speedExists = true
                        break
                    }
                } while (line != null)
                inputStream?.close()

                //InputStream cannot be rewound, so we will open it again
                inputStream = contentResolver.openInputStream(uri)
                try {
                    val parser = XmlPullParserHandler()
                    parser.parse(inputStream, speedExists)
                    Data.currentPoint = 0
                    when (parser.returnCode) {
                        0 -> {          //0 means the file was read successfully
                            Data.numOfPoints = Data.trackPoints.size
                            Toast.makeText( //Show a toast message on how many tags were read
                                baseContext,
                                "Read ${Data.numOfPoints} points",
                                Toast.LENGTH_LONG
                            ).show()

                            val startDate = Date(Data.trackPoints[0].epoch)
                            val endDate = Date(Data.trackPoints[Data.numOfPoints - 1].epoch)
                            //The default epoch time is in milliseconds
                            val millis: Long = endDate.time - startDate.time
                            val hours: Int = (millis / (1000 * 60 * 60)).toInt()
                            val mins: Int = (millis / (1000 * 60) % 60).toInt()

                            //Calculate total distance
                            var distance = 0.0
                            var trackPoint1: Data.TrackPoint
                            var trackPoint2: Data.TrackPoint
                            //incremental distance
                            var dDistance: Double
                            var lat1: Double
                            var lat2: Double
                            var lon1: Double
                            var lon2: Double
                            for (i in 0..Data.numOfPoints - 2) {
                                trackPoint1 = Data.trackPoints[i]
                                trackPoint2 = Data.trackPoints[i + 1]
                                lat1 = Data.trackPoints[i].lat.toRad()
                                lat2 = trackPoint2.lat.toRad()
                                lon1 = trackPoint1.lon.toRad()
                                lon2 = trackPoint2.lon.toRad()
                                dDistance = 2.0 * asin(
                                    sqrt(
                                        sin((lat1 - lat2) / 2.0).pow(2.0) + cos(lat1) * cos(lat2) * sin(
                                            (lon1 - lon2) / 2.0
                                        ).pow(2.0)
                                    )
                                ) * (180.0 * 60.0) * 1.15078 / PI
                                distance += dDistance
                            }
                        }

                        1 -> {  //1 means there was some error in the file
                            Toast.makeText(baseContext, "Invalid File", Toast.LENGTH_SHORT).show()
                            Data.numOfPoints = 0
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }


        //This is the listener for the button that launches the file selector
        val gpxReadFileButton: Button = findViewById<Button>(R.id.button3)
        gpxReadFileButton.setOnClickListener {
            if (Data.mockGPSServiceIsRunning){
                Data.stopService = true
            }
            gpxFilePicker.launch("*/*")
        }

        Log.d(TAG,"MainActivity onCreate - exit")
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        Log.d(TAG,"MainActivity onCreateView - start")
        val v = super.onCreateView(name, context, attrs)




        Log.d(TAG,"MainActivity onCreateView - exit")
        return v

    }


    override fun onStart() {
        Log.d(TAG,"MainActivity onStart - start")
        super.onStart()
        Log.d(TAG,"MainActivity onStart - exit")
    }


    override fun onResume() {
        Log.d(TAG,"MainActivity onResume - start")
        super.onResume()

        //We execute the operations that require the data file in onResume because that is what get's called when the file picker closes and the main app opens.
        //OnCreate doesn't get called. onResume is also called during first invocation, so we need to address both cases of when there is data and when there is not.
        //Data is an object class (doesn't need instantiation).
        //I supposed we could also do all of this under onStart or onRestart as well.

        //Set the length of the seekbar. Upon initial invocation, the datafile will be empty, so we set the length to 0.
        val seekBar: SeekBar = findViewById<SeekBar>(R.id.seekBar)
        Log.d(TAG,"Seekbar set: ${Data.numOfPoints}")
        seekBar.max = if (Data.numOfPoints > 1) {
            Data.numOfPoints - 1
        } else {
            0
        }

        //The seekbar is a display of the current position, but the user can also drag it to change the current position.
        //When user drags it (p2 will be true), we simply set a flag (seekBarMoved) and the new position, and let the Service take care of moving the data position.
        //This listener is also triggered every time seekbar.progress is changed - which is done through a Thread that polls currentPosition and sets the seekbar position.
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) { //p2 will be true if the seekbar change was caused by screen input. It would be false if the change was caused by the Thread (below).
                    Data.seekBarPoint = p1
                    Data.seekBarMoved = true
                    sleep(500)  //Sleep for 500ms to allow the GPSMockLocationService thread to use the seekBarPoint and update it to its currentPoint
                }
                updateTrackPlotPosition()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            private fun updateTrackPlotPosition() {
                findViewById<SeekBar>(R.id.seekBar).progress = Data.currentPoint
                findViewById<TextView>(R.id.tvPoint).text =
                    Data.currentPoint.toString()
                findViewById<TextView>(R.id.tvTime).text =
                    Date(Data.trackPoints[Data.currentPoint].epoch).toString()
                findViewById<TextView>(R.id.tvAlt).text =
                    Data.trackPoints[Data.currentPoint].altitude.toFt().toString()
                findViewById<TextView>(R.id.tvSpeed).text =
                    Data.trackPoints[Data.currentPoint].speed.toKts().toString()
                findViewById<GPSTrackPlot.GPSTrackPlot>(R.id.plot)
                    .setCirclePoint(Data.currentPoint)
                findViewById<GPSTrackPlot.GPSTrackPlot>(R.id.plot).postInvalidate()
            }
        }
        )

        fun trackPlot() {
            val trackPlot = findViewById<GPSTrackPlot.GPSTrackPlot>(R.id.plot)
            trackPlot.setTrackData(Data)
            trackPlot.makeBitmap = true
            trackPlot.setCirclePoint(Data.currentPoint)
            trackPlot.postInvalidate()
        }

        trackPlot()



        //We launch a thread that continuoisly checks the data position (which is done by Service) and update the seekbar position. This will
        //automatically trigger the seekbar listener every time (once per 500ms).
        Thread {
            while (true) {
                seekBar.progress = Data.currentPoint
                Thread.sleep(50)
            }
        }.start()

        if ((!Data.mockGPSServiceIsRunning)&&(Data.numOfPoints > 1)) {
            Data.stopService = false
            launchMockGPSService()
        }

        Log.d(TAG,"MainActivity onResume - exit")
    }

    override fun onStop() {
        Log.d(TAG,"MainActivity onStop - start")
        super.onStop()
        Log.d(TAG,"MainActivity onStop - exit")
    }

    override fun onPause() {
        Log.d(TAG,"MainActivity onPause - start")
        super.onPause()
        Log.d(TAG,"MainActivity onPause - exit")
    }

    override fun onRestart() {
        Log.d(TAG,"MainActivity onRestart - start")
        super.onRestart()
        Log.d(TAG,"MainActivity onRestart - exit")
    }

    override fun onDestroy() {
        Log.d(TAG,"MainActivity onDestroy - start")
        super.onDestroy()
        Data.stopService = true
        Log.d(TAG,"MainActivity onDestroy - exit")
    }


    fun launchMockGPSService() {

        val intentService = Intent(baseContext,GPSMockLocationService::class.java)
        Log.d(TAG,"Run - Starting Foreground Service")
        Data.serviceStartTime = System.currentTimeMillis()
        Data.trackStartTime = Data.trackPoints[0].epoch
        ContextCompat.startForegroundService(baseContext, intentService)
    }

}
