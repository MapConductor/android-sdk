package com.mapconductor.core

import com.mapconductor.core.features.GeoPoint

fun calculateZIndex(geoPointBase: GeoPoint): Double {
    // 南→北で奥行きを出す
    // 同じ緯度内では西が上（前）に来る
    return (-geoPointBase.latitude * 1_000_000 - geoPointBase.longitude)
}
