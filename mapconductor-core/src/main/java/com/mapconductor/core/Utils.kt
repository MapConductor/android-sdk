package com.mapconductor.core

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.polyline.PolylineState
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import android.util.Log

fun calculateZIndex(geoPointBase: IGeoPoint): Int {
    // 南→北で奥行きを出す
    // 同じ緯度内では西が上（前）に来る
    return (-geoPointBase.latitude * 1_000_000 - geoPointBase.longitude).roundToInt()
}

fun meterToPixel(
    meter: Double,
    latitude: Double,
    zoom: Double,
    tileSize: Double = 256.0, // Google Mapsはデフォルト256pxだが、Mapbox v10+はデフォルト512px
): Double {
    val earthCircumference = 2 * Math.PI * 6378137
    val metersPerPixel = cos(Math.toRadians(latitude)) * earthCircumference / (tileSize * 2.0.pow(zoom))
    return meter / metersPerPixel
}

fun printPoints(
    tag: String,
    points: List<IGeoPoint>,
) {
    Log.d(tag, "-----------")
    points.forEach { point ->
        Log.d(tag, GeoPoint.from(point).toUrlValue())
    }
}

fun toRadius(degValue: Double): Double {
    return degValue * (Math.PI / 180.0)
}

fun toDegree(radValue: Double): Double {
    return radValue / (Math.PI / 180.0)
}

/**
 * Calculate waypoints from start to finish on geodesic line
 * @ref http://jamesmccaffrey.wordpress.com/2011/04/17/drawing-a-geodesic-line-for-bing-maps-ajax/
 */
fun interpolateGeodesicPolyline(origin: IGeoPoint, dest: IGeoPoint): List<IGeoPoint> {
    // convert to radians
    val lat1 = toRadius(origin.latitude)
    val lng1 = toRadius(origin.longitude)
    val lat2 = toRadius(dest.latitude)
    val lng2 = toRadius(dest.longitude)

    val distance = 2 * abs(
        sqrt(
            pow(sin((lat1 - lat2) / 2.0), 2.0) +
            cos(lat1) * cos(lat2) * pow(sin((lng1 - lng2) / 2.0), 2.0)
        )
    )
    val wayPoints = mutableListOf<IGeoPoint>()
    var fraction: Float = 0f // fraction of the curve
    val fractionI: Float = 0.01f // fraction increment

    val sinD = sin(distance)
    while (fraction <= 1f) {
        val argA = sin((1.0f - fraction) * distance) / sinD
        val argB = sin(fraction * distance) / sinD

        val cosLat1 = cos(lat1)
        val cosLng1 = cos(lng1)
        val cosLat2 = cos(lat2)
        val cosLng2 = cos(lng2)
        val sinLat1 = sin(lat1)
        val sinLat2 = sin(lat2)
        val sinLng1 = sin(lng1)
        val sinLng2 = sin(lng2)
        val x = argA * cosLat1 * cosLng1 + argB * cosLat2 * cosLng2
        val y = argA * cosLat1 * sinLng1 + argB * cosLat2 * sinLng2
        val z = argA * sinLat1 + argB * sinLat2
        val lat = atan2(z, sqrt((x * x) + (y * y)))
        val lng = atan2(y, x)

        val point = object : IGeoPoint {
            override val latitude: Double = toDegree(lat)
            override val longitude: Double = toDegree(lng)
            override val altitude: Double? = null
        }
//        if (point.longitude> 179) break
        wayPoints.add(point)
        Log.d("debug", " ${GeoPoint.from(point).toUrlValue()}")
        fraction += fractionI
    }

    return wayPoints
}

fun createGeodesicPoints(points: List<IGeoPoint>): List<IGeoPoint> {
    val results = mutableListOf<IGeoPoint>()
    for (i in 1..points.size - 1) {
        val interpolatedPoints = interpolateGeodesicPolyline(points[i - 1], points[i])
        results.addAll(interpolatedPoints)
//        if (points[i].longitude < 0) break
    }
    return results
}
