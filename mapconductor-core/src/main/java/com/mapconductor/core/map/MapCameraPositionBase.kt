package com.mapconductor.core.map

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.GeoRectBounds

data class VisibleRegion(
    val bounds: GeoRectBounds,
    val nearLeft: GeoPoint?,
    val nearRight: GeoPoint?,
    val farLeft: GeoPoint?,
    val farRight: GeoPoint?,
)

interface MapCameraPosition {
    val position: GeoPoint
    val zoom: Double
    val bearing: Double
    val tilt: Double
    val paddings: MapPaddings?
    val visibleRegion: VisibleRegion?
}

data class MapCameraPositionImpl(
    override val position: GeoPointImpl,
    override val zoom: Double = 0.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddings? = MapPaddingsImpl.Companion.Zeros,
    override val visibleRegion: VisibleRegion? = null,
) : MapCameraPosition {
    companion object {
        val Default =
            MapCameraPositionImpl(
                position =
                    GeoPointImpl(
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
