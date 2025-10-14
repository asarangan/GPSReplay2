package org.sarangan.gpsreplay2

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat.startForegroundService
import java.lang.Thread.sleep
import java.util.Date


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private lateinit var myBoundService: GPSMockLocationService
private var isBound = false
private val serviceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as GPSMockLocationService.MyServiceBinder
        myBoundService = binder.getService()
        isBound = true
        // Now you can interact with myBoundService
        //Log.d(TAG, "Service connected: ${myBoundService.doSomethingUseful()}")
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        isBound = false
        Log.d(TAG, "Service disconnected")
    }
}


/**
 * A simple [Fragment] subclass.
 * Use the [RunFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RunFragment : Fragment() {
//    TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null

//    lateinit var data: Data
    lateinit var runFragmentView: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
            //param2 = it.getString(ARG_PARAM2)
        //}

        Log.d(TAG,"Run OnCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "Run OnCreateView")
        runFragmentView = inflater.inflate(R.layout.fragment_run, container, false)
        return runFragmentView
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG,"Run OnStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG,"Run OnStop")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"Run OnViewCreated")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG,"Run OnPause")
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Run OnResume")
        val seekBar: SeekBar = runFragmentView.findViewById<SeekBar>(R.id.seekBar)
        seekBar.max = if (Data.numOfPoints > 1) {
            Data.numOfPoints - 1
        } else {
            0
        }
        //We need at least two points because we are using the subsequent point to calculate speed and heading.
//        val button: Button = runFragmentView.findViewById<Button>(R.id.button)
//
//        fun buttonColor() {
//            if (Data.play) {
//                button.text = "Playing"
//                button.setBackgroundColor(
//                    ContextCompat.getColor(
//                        runFragmentView.context,
//                        R.color.teal_200
//                    )
//                )
//            } else {
//                button.text = "Paused"
//                button.setBackgroundColor(
//                    ContextCompat.getColor(
//                        runFragmentView.context,
//                        R.color.purple_500
//                    )
//                )
//            }
//        }


        // intentService.putExtra("data", Data())

//        button.setOnClickListener {
//            if (Data.numOfPoints > 1) {
//                //Toggle the play/pause button
//                Data.play = !Data.play
//                //If we just switched play to inactive, wait until trackPlayService from the previous instance to stop and the status flag to
//                //indicate correctly before exiting this listener. If the play/pause button is pressed in quick succession it is possible to
//                //launch two service instances. We want to avoid that.
//                if (!Data.play) {
//                    //while (Data.trackPlayServiceIsRunning) {
//                    //} //If play is inactive, wait until service is fully stopped
//                    //} else {   //If play is active, the launch the service
//                    //Calculate the delta time before starting. This is the time offset (in ms) between the current time and the time stamp
//                    //of the currently selected GPS data point
//                    // data.timeOffset =
//                    //System.currentTimeMillis() - Date(data.trackPoints[data.currentPoint].epoch).time
//
//
//                }
//                //Set the play/pause button color
//                buttonColor()
//            }
//        }

        fun launchMockGPSService() {

            val intentService = Intent(context,GPSMockLocationService::class.java)
            Log.d(TAG,"Run - Starting Foreground Service")
            startForegroundService(context, intentService)
            //Intent(requireContext(), GPSMockLocationService::class.java).also { intent ->
            //    requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            //}
            Data.mockGPSServiceLaunched = true


        }


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                //p2 will be true if the seekbar change was caused by screen input. It would be false if the change was caused by code.
                if (p2) {
                    Data.seekBarPoint = p1
                    Data.seekBarMoved = true
                    if (!Data.mockGPSServiceLaunched) {
                        launchMockGPSService()
                    }
                    //seekBar.progress = p1
                    sleep(100)  //Sleep for 100ms to allow the GPSMockLocationService thread to use the seekPoint and update currentPoint
                } else {
                    seekBar.progress = Data.currentPoint
                }
                updateTrackPlotPosition()
            }


            private fun updateTrackPlotPosition() {
                runFragmentView.findViewById<SeekBar>(R.id.seekBar).progress = Data.currentPoint
                runFragmentView.findViewById<TextView>(R.id.tvPoint).text =
                    Data.currentPoint.toString()
                runFragmentView.findViewById<TextView>(R.id.tvTime).text =
                    Date(Data.trackPoints[Data.currentPoint].epoch).toString()
                runFragmentView.findViewById<TextView>(R.id.tvAlt).text =
                    Data.trackPoints[Data.currentPoint].altitude.toFt().toString()
                runFragmentView.findViewById<TextView>(R.id.tvSpeed).text =
                    Data.trackPoints[Data.currentPoint].speed.toKts().toString()
                runFragmentView.findViewById<GPSTrackPlot.GPSTrackPlot>(R.id.plot)
                    .setCirclePoint(Data.currentPoint)
                runFragmentView.findViewById<GPSTrackPlot.GPSTrackPlot>(R.id.plot).postInvalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        fun trackPlot() {
            val trackPlot = runFragmentView.findViewById<GPSTrackPlot.GPSTrackPlot>(R.id.plot)
            trackPlot.setTrackData(Data)
            trackPlot.makeBitmap = true
            trackPlot.setCirclePoint(Data.currentPoint)
            trackPlot.postInvalidate()
        }

        trackPlot()



        if ((!Data.mockGPSServiceLaunched)&&(Data.numOfPoints > 1)) {
            launchMockGPSService()
        }

        Thread {
            while (true) {
                seekBar.progress = Data.currentPoint
//                val pt = seekBar.progress
//                val timeDiff = Data.trackPoints[pt + 1].epoch - Data.trackPoints[pt].epoch
//                seekBar.progress++
//                //Log.d(TAG, "Sleep $timeDiff")
                Thread.sleep(100)
                //               Log.d(TAG,myBoundService.doSomethingUseful())
            }
        }.start()

    }




    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG,"Run OnAttach")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG,"Run OnSaveInstanceState")
    }


}