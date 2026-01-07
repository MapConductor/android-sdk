package com.mapconductor.here

import androidx.annotation.Keep
import com.here.sdk.core.GeoOrientation
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraUpdate
import com.here.sdk.mapview.MapCameraUpdateFactory
import com.here.sdk.mapview.MapMeasure
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import com.mapconductor.here.zoom.ZoomAltitudeConverter

private val converter = ZoomAltitudeConverter(AbstractZoomAltitudeConverter.DEFAULT_ZOOM0_ALTITUDE)

@Keep
fun MapCameraPosition.toMapCameraUpdate(): MapCameraUpdate =
    MapCameraUpdateFactory.lookAt(
        GeoPoint.from(position).toGeoCoordinates().toUpdate(),
        GeoOrientation(bearing, tilt).toUpdate(),
        MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, zoom),
    )

@Keep
fun MapCameraPosition.toCameraState(): MapCamera.State =
    MapCamera.State(
        GeoPoint.from(position).toGeoCoordinates(),
        GeoOrientation(bearing, tilt),
        0.0,
        zoom,
    )

fun MapCameraPosition.Companion.from(position: MapCameraPositionInterface): MapCameraPosition =
    when (position) {
        is MapCameraPosition -> position
        else ->
            MapCameraPosition(
                position = position.position,
                zoom = position.zoom,
                bearing = position.bearing,
                tilt = position.tilt,
                paddings = position.paddings,
                visibleRegion = position.visibleRegion,
            )
    }

fun MapCamera.State.toMapCameraPosition(): MapCameraPosition {
    val altitude =
        converter.zoomLevelToAltitude(
            zoomLevel = zoomLevel,
            latitude = targetCoordinates.latitude,
            tilt = orientationAtTarget.tilt,
        )
    val position = targetCoordinates.toGeoPoint().copy(altitude = altitude)
    return MapCameraPosition(
        position = position,
        zoom = zoomLevel,
        bearing = this.orientationAtTarget.bearing,
        tilt = this.orientationAtTarget.tilt,
        visibleRegion = null,
    )
}
