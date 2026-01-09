package com.mapconductor.core.map

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds
import kotlin.math.abs

data class VisibleRegion(
    val bounds: GeoRectBounds,
    val nearLeft: GeoPointInterface?,
    val nearRight: GeoPointInterface?,
    val farLeft: GeoPointInterface?,
    val farRight: GeoPointInterface?,
)

interface MapCameraPositionInterface {
    val position: GeoPointInterface
    val zoom: Double
    val bearing: Double
    val tilt: Double
    val paddings: MapPaddingsInterface?
    val visibleRegion: VisibleRegion?
}

class MapCameraPosition(
    position: GeoPointInterface,
    override val zoom: Double = 0.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddingsInterface? = MapPaddings.Companion.Zeros,
    override val visibleRegion: VisibleRegion? = null,
) : MapCameraPositionInterface {
    override val position: GeoPoint = GeoPoint.from(position)

    fun equals(other: MapCameraPositionInterface): Boolean =
        this.position.equals(other = other.position) &&
            this.zoomEquals(other) &&
            this.bearingEquals(other) &&
            this.tiltEquals(other)

    fun copy(
        position: GeoPointInterface? = this.position,
        zoom: Double? = this.zoom,
        bearing: Double? = this.bearing,
        tilt: Double? = this.tilt,
        paddings: MapPaddingsInterface? = this.paddings,
        visibleRegion: VisibleRegion? = this.visibleRegion,
    ) = MapCameraPosition(
        position = position ?: this.position,
        zoom = zoom ?: this.zoom,
        bearing = bearing ?: this.bearing,
        tilt = tilt ?: this.tilt,
        paddings = paddings ?: this.paddings,
        visibleRegion = visibleRegion ?: this.visibleRegion,
    )

    private fun zoomEquals(other: MapCameraPositionInterface): Boolean {
        val tolerance = 1e-2
        return abs(this.zoom - other.zoom) < tolerance
    }

    private fun bearingEquals(other: MapCameraPositionInterface): Boolean {
        val tolerance = 1e-2
        return abs(this.bearing - other.bearing) < tolerance
    }

    private fun tiltEquals(other: MapCameraPositionInterface): Boolean {
        val tolerance = 1e-2
        return abs(this.tilt - other.tilt) < tolerance
    }

    override fun hashCode(): Int {
        var result = this.position.hashCode()
        result = 31 * result + zoom.hashCode()
        result = 31 * result + bearing.hashCode()
        result = 31 * result + tilt.hashCode()
        result = 31 * result + paddings.hashCode()
        result = 31 * result + visibleRegion.hashCode()
        return result
    }

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
