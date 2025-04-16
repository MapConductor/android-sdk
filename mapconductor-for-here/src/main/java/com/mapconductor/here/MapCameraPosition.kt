package com.mapconductor.here

import androidx.annotation.Keep
import com.here.sdk.core.GeoOrientation
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraUpdate
import com.here.sdk.mapview.MapCameraUpdateFactory
import com.here.sdk.mapview.MapMeasure
import com.mapconductor.core.MapCameraPositionImpl
import com.mapconductor.core.MapPaddings
import com.mapconductor.core.MapPaddingsImpl

interface MapCameraPositionHereImpl: MapCameraPositionImpl {
    fun toMapCameraUpdate(): MapCameraUpdate
    fun copy(
        target: GeoPoint? = null,
        zoom: Double? = null,
        bearing: Double? = null,
        tilt: Double? = null,
        paddings: MapPaddingsImpl? = null,
    ): MapCameraPositionHereImpl
}
@Keep
data class MapCameraPosition @JvmOverloads constructor(
    override val target: GeoPoint,
    override val zoom: Double = 2.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddingsImpl = MapPaddings.Zeros,
): MapCameraPositionHereImpl {

    override fun copy(
        target: GeoPoint?,
        zoom: Double?,
        bearing: Double?,
        tilt: Double?,
        paddings: MapPaddingsImpl?
    ): MapCameraPositionHereImpl = MapCameraPosition(
        target = target ?: this.target,
        zoom = zoom ?: this.zoom,
        bearing = bearing ?: this.bearing,
        tilt = tilt ?: this.tilt,
        paddings = paddings ?: this.paddings
    )

    override fun toMapCameraUpdate() = MapCameraUpdateFactory.lookAt(
        target.toGeoCoordinates().toUpdate(),
        GeoOrientation(bearing, tilt).toUpdate(),
        MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, zoom)
    )

    companion object {
        fun fromImpl(mapCameraPositionImpl: MapCameraPositionImpl) =
            when(mapCameraPositionImpl) {
                is MapCameraPosition -> mapCameraPositionImpl
                else -> MapCameraPosition(
                    target = GeoPoint.fromImpl(mapCameraPositionImpl.target),
                    zoom = mapCameraPositionImpl.zoom,
                    bearing = mapCameraPositionImpl.bearing,
                    tilt = mapCameraPositionImpl.tilt,
                    paddings = MapPaddings.fromImpl(mapCameraPositionImpl.paddings),
                )
            }
    }
}

fun MapCamera.State.toMapCameraPosition() =
    MapCameraPosition(
        target = targetCoordinates.toGeoPoint(),
        zoom = zoomLevel,
        bearing = this.orientationAtTarget.bearing,
        tilt = this.orientationAtTarget.tilt,
    )
