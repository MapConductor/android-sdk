package com.mapconductor.here

import androidx.annotation.Keep
import com.here.sdk.core.GeoOrientation
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraUpdate
import com.here.sdk.mapview.MapCameraUpdateFactory
import com.here.sdk.mapview.MapMeasure
import com.mapconductor.core.IMapCameraPosition
import com.mapconductor.core.MapCameraPositionBase
import com.mapconductor.core.MapPaddings
import com.mapconductor.core.MapPaddingsImpl
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint

interface MapCameraPositionHere: IMapCameraPosition {
    fun toMapCameraUpdate(): MapCameraUpdate
    fun toCameraState(): MapCamera.State
}

@Keep
data class MapCameraPosition @JvmOverloads constructor(
    override val position: IGeoPoint,
    override val zoom: Double = 2.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddings? = MapPaddingsImpl.Zeros,
): MapCameraPositionBase(position, zoom, bearing, tilt, paddings), MapCameraPositionHere {

    override fun toMapCameraUpdate() = MapCameraUpdateFactory.lookAt(
        GeoPoint.from(position).toGeoCoordinates().toUpdate(),
        GeoOrientation(bearing, tilt).toUpdate(),
        MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, zoom),
    )

    override fun toCameraState() = MapCamera.State(
        GeoPoint.from(position).toGeoCoordinates(),
        GeoOrientation(bearing, tilt),
        0.0,
        zoom,
    )

    companion object {
        val Default = MapCameraPosition(
            position = GeoPoint.fromLatLong(
                latitude = 0.0,
                longitude = 0.0,
            ),
            zoom = 0.0,
            bearing = 0.0,
            tilt = 0.0,
        )

        fun from(position: IMapCameraPosition) =
            when(position) {
                is MapCameraPosition -> position
                else -> MapCameraPosition(
                    position = GeoPoint.from(position.position),
                    zoom = position.zoom,
                    bearing = position.bearing,
                    tilt = position.tilt,
                    paddings = position.paddings,
                )
            }
    }
}

fun MapCamera.State.toMapCameraPosition() =
    MapCameraPosition(
        position = targetCoordinates.toGeoPoint(),
        zoom = zoomLevel,
        bearing = this.orientationAtTarget.bearing,
        tilt = this.orientationAtTarget.tilt,
    )
