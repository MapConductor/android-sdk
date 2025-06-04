package com.mapconductor.core.map

import com.mapconductor.core.features.IGeoPoint

interface IMapCameraPosition {
    val position: IGeoPoint
    val zoom: Double
    val bearing: Double
    val tilt: Double
    val paddings: MapPaddings?
}

open class MapCameraPositionBase(
    override val position: IGeoPoint,
    override val zoom: Double = 0.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddings? = MapPaddingsImpl.Companion.Zeros,
) : IMapCameraPosition {
    companion object {
        val Default =
            MapCameraPositionBase(
                position =
                    object : IGeoPoint {
                        override val latitude: Double = 0.0
                        override val longitude: Double = 0.0
                        override val altitude: Double? = null

                        fun toUrlValue(precision: Int): String = "0.0,0.0"
                    },
                zoom = 0.0,
                bearing = 0.0,
                tilt = 0.0,
            )
    }
}
