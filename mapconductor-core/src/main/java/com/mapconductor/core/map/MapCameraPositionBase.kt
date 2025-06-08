package com.mapconductor.core.map

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint

interface IMapCameraPosition {
    val position: IGeoPoint
    val zoom: Double
    val bearing: Double
    val tilt: Double
    val paddings: MapPaddings?
}

data class MapCameraPosition(
    override val position: GeoPoint,
    override val zoom: Double = 0.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddings? = MapPaddingsImpl.Companion.Zeros,
) : IMapCameraPosition {
    companion object {
        val Default =
            MapCameraPosition(
                position =
                    GeoPoint(
                        latitude = 0.0,
                        longitude = 0.0,
                        altitude = 0.0,
                    ),
                zoom = 0.0,
                bearing = 0.0,
                tilt = 0.0,
            )
    }
}
