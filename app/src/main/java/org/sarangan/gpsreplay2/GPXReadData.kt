package org.sarangan.gpsreplay2

import android.net.Uri
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import java.io.BufferedReader
import java.io.IOException
import java.util.Date
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class GPXReadData(val view: View) : ActivityResultCallback<Uri> {

    //val data = Data()

    override fun onActivityResult(result: Uri) {
        val tvNumberOfPoints = view.findViewById<TextView>(R.id.tvpts)
        val tvStartTime = view.findViewById<TextView>(R.id.tvtime1)
        val tvEndTime = view.findViewById<TextView>(R.id.tvtime2)
        val tvDuration = view.findViewById<TextView>(R.id.tvduration)
        val tvDistance = view.findViewById<TextView>(R.id.tvdistance)


        var inputStream = view.context.contentResolver.openInputStream(result)
        val bufferedReader = BufferedReader(inputStream?.reader())
        //Some GPX files do not contain a speed tag. If that is the case, we will have to calculate the speed.
        var speedExists: Boolean = false
        var line: String?
        do {
            line = bufferedReader.readLine()
            if (line?.contains("<speed>",ignoreCase = true) == true){
                speedExists = true
                break
            }
        } while (line != null)
        inputStream?.close()
        //Log.d("MYCHECK", speedExists.toString())
        //InputStream cannot be rewinded, so we will open it again
        inputStream = view.context.contentResolver.openInputStream(result)

        try {
            val parser = XmlPullParserHandler()
            parser.parse(inputStream,speedExists)
            //Data.trackPoints = parser.trackPoints
            //Data.play = false
            Data.currentPoint = 0

            when (parser.returnCode) {
                0 -> {          //0 means the file was read successfully
                    Data.numOfPoints = Data.trackPoints.size
                    Toast.makeText( //Show a toast message on how many tags were read
                        view.context,
                        "Read ${Data.numOfPoints} points",
                        Toast.LENGTH_LONG
                    ).show()
                    val startDate: Date = Date(Data.trackPoints[0].epoch)
                    val endDate: Date = Date(Data.trackPoints[Data.numOfPoints - 1].epoch)

                    //Set the time offset between the current time and the first track
                    //Data.timeOffset = System.currentTimeMillis() - Data.trackPoints[0].epoch

                    //Write the number of points to the view
                    tvNumberOfPoints.text = Data.numOfPoints.toString()


                    //Write the start date and time to the view
                    tvStartTime.text = startDate.toString()

                    //Write the end date and time to the view
                    tvEndTime.text = endDate.toString()

                    //The default epoch time is in milliseconds
                    val millis: Long = endDate.time - startDate.time
                    val hours: Int = (millis / (1000 * 60 * 60)).toInt()
                    val mins: Int = (millis / (1000 * 60) % 60).toInt()
                    val secs: Int =
                        ((millis - (hours * 3600 + mins * 60) * 1000) / 1000).toInt()

                    //Write duration to the view
                    tvDuration.text = "$hours Hrs $mins Mins $secs secs"

                    //Calculate total distance
                    var distance: Double = 0.0
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
                        lat1 = trackPoint1.lat.toRad()
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
                    tvDistance.text = "${((distance * 10.0).roundToInt() / 10.0)} Statute Miles"
                }

                1 -> {  //1 means there was some error in the file
                    Toast.makeText(view.context, "Invalid File", Toast.LENGTH_SHORT).show()
                    Data.numOfPoints = 0
                    tvNumberOfPoints.text = "-"
                    tvStartTime.text = "-"
                    tvEndTime.text = "-"
                    tvDuration.text = "-"
                    tvDuration.text = "-"
                    tvDistance.text = "-"
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}