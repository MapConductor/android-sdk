package com.mapconductor.core.map

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.GeoRectBounds
import kotlin.math.abs

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

class MapCameraPositionImpl(
    position: GeoPoint,
    override val zoom: Double = 0.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddings? = MapPaddingsImpl.Companion.Zeros,
    override val visibleRegion: VisibleRegion? = null,
) : MapCameraPosition {

    override val position: GeoPointImpl = GeoPointImpl.from(position)

    fun equals(other: MapCameraPosition): Boolean {
        return this.position.equals(other = other.position) &&
            this.zoomEquals(other) &&
            this.bearingEquals(other) &&
            this.tiltEquals(other)
    }

    fun copy(
        position: GeoPoint? = this.position,
        zoom: Double? = this.zoom,
        bearing: Double? = this.bearing,
        tilt: Double? = this.tilt,
        paddings: MapPaddings? = this.paddings,
        visibleRegion: VisibleRegion? = this.visibleRegion,
    ) = MapCameraPositionImpl(
        position = position ?: this.position,
        zoom = zoom ?: this.zoom,
        bearing = bearing ?: this.bearing,
        tilt = tilt ?: this.tilt,
        paddings = paddings ?: this.paddings,
        visibleRegion = visibleRegion ?: this.visibleRegion,
    )

    private fun zoomEquals(other: MapCameraPosition): Boolean {
        val tolerance = 1e-2
        return abs(this.zoom - other.zoom) < tolerance
    }
    private fun bearingEquals(other: MapCameraPosition): Boolean {
        val tolerance = 1e-2
        return abs(this.bearing - other.bearing) < tolerance
    }
    private fun tiltEquals(other: MapCameraPosition): Boolean {
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
