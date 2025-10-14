package org.sarangan.gpsreplay2

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FileFragment : Fragment() {
    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//    lateinit var gpxReadData: GPXReadData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
        Log.d(TAG,"File OnCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"File OnCreateView")
        // Inflate the layout for this fragment
        val fileFragmentView: View = inflater.inflate(R.layout.fragment_file, container, false)

        //This is for reading the external file, and the callback object (gpxReadData)
        val contract = ActivityResultContracts.GetContent()
        val gpxReadData = GPXReadData(fileFragmentView)
        val getContentActivity = registerForActivityResult(contract,gpxReadData)


        //Listener for the button launches the file selector
        val gpxReadFileButton: Button = fileFragmentView.findViewById<Button>(R.id.button2)
        gpxReadFileButton.setOnClickListener {
            getContentActivity.launch("*/*")
        }
        return fileFragmentView
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG,"File OnStart")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG,"File OnPause")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"File OnViewCreated")
    }
    override fun onStop() {
        super.onStop()
        Log.d(TAG,"File OnStop")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG,"File OnResume")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG,"File OnAttach")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG,"File OnSaveInstanceState")
    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment FileFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            FileFragment(Data()).apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}