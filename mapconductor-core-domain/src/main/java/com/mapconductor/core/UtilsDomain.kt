package com.mapconductor.core

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import kotlin.math.roundToInt
import android.util.Log

fun printPoints(
    tag: String,
    points: List<GeoPointInterface>,
) {
    Log.d(tag, "-----------")
    points.forEach { point ->
        Log.d(tag, GeoPoint.from(point).toUrlValue())
    }
}

fun calculateZIndex(geoPointBase: GeoPointInterface): Int {
    // 南→北で奥行きを出す
    // 同じ緯度内では西が上（前）に来る
    return (-geoPointBase.latitude * 1_000_000 - geoPointBase.longitude).roundToInt()
}

fun normalizeLng(lng: Double): Double {
    // [-180, 180] に収める
    return (((lng + 180.0) % 360.0 + 360.0) % 360.0) - 180.0
}
