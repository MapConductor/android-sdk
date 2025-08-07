package com.mapconductor.core

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
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
