package org.sarangan.gpsreplay2

import kotlin.math.PI

object Data {
    //var play: Boolean = false
    var currentPoint: Int = 0
    var seekBarPoint: Int = 0
    var seekBarMoved = false
    var numOfPoints: Int = 0
    val trackPoints: MutableList<TrackPoint> = mutableListOf()
    var timeOffset: Long = 0
    //var mockGPSEnabled: Boolean = true
    //var trackPlayServiceIsRunning = false
    var mockGPSServiceLaunched = false

    var stopService = false


    class TrackPoint {
        var epoch: Long = 0
        var lat: Double = 0.0
        var lon: Double = 0.0
        var speed: Float = 0.0F
        var altitude: Double = 0.0
        var trueCourse: Float = 0.0F

    }
}
        fun Float.toKts(): Float {
            return ((this * 19.4384).toInt() / 10.0).toFloat()
        }

        fun Float.toM(): Float {
            return (this * 180.0 / PI * 60.0 * 1852.0).toFloat()
        }

        fun Float.toMph(): Float {
            return ((this * 22.3694).toInt() / 10.0).toFloat()
        }

        fun Double.toRad(): Double {
            return (this / 180.0 * PI)
        }

        fun Double.toDeg(): Double {
            return (this / PI * 180.0)
        }

        fun Double.toFt(): Double {
            return (this * 32.8084).toInt() / 10.0
        }

