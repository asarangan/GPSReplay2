package org.sarangan.gpsreplay2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView


const val TAG:String = "GPS"
const val GPSUpdate_ms:Long = 50 //MockGPS will update every 50 ms

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
    private lateinit var fileFragment: FileFragment
    private lateinit var runFragment: RunFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"MainActivity onCreate - start")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout)

        fileFragment = FileFragment()
        Log.d(TAG,"finished instanting fileFragment")
        runFragment = RunFragment()
        Log.d(TAG,"finished instanting runFragment")


        supportFragmentManager.beginTransaction().apply {
            add(R.id.frameLayout, runFragment)
            add(R.id.frameLayout, fileFragment)
            hide(runFragment)
            Log.d(TAG,"before commit")
            commit()
            Log.d(TAG,"after commit")
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.idFile ->

                    supportFragmentManager.beginTransaction().apply {
                        runFragment.onPause()
                        fileFragment.onResume()
                        hide(runFragment)
                        show(fileFragment)
                        Log.d(TAG, "Switching to File")
                        commit()
                    }

                R.id.idRun -> supportFragmentManager.beginTransaction().apply {
                    //remove(fileFragment)
                    fileFragment.onPause()
                    runFragment.onResume()
                    hide(fileFragment)
                    show(runFragment)
                    Log.d(TAG, "Switching to Run")
                    commit()
                }
            }
            true
        }

        Log.d(TAG,"MainActivity onCreate - exit")
    }


    override fun onStart() {
        Log.d(TAG,"MainActivity onStart - start")
        super.onStart()
        Log.d(TAG,"MainActivity onStart - exit")
    }


    override fun onResume() {
        Log.d(TAG,"MainActivity onResume - start")
        super.onResume()
//        val data = fileFragment.gpxReadData.data
//        runFragment.data = data

//        if (!Data.mockGPSServiceRunning){
//            val intentService = Intent(applicationContext,GPSMockLocationService::class.java)
//            Log.d(TAG,"MainActivity - Starting Foreground Service")
//            ContextCompat.startForegroundService(applicationContext, intentService)
//            Data.mockGPSServiceRunning = true
//        }


        Log.d(TAG,"MainActivity onResume - exit")
    }

    override fun onStop() {
        Log.d(TAG,"MainActivity onRStop - start")
        super.onStop()
        Log.d(TAG,"MainActivity onRStop - exit")
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
}
