package com.mapconductor.googlemaps

import com.google.android.gms.maps.model.CameraPosition
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapPaddings
import com.mapconductor.core.map.MapPaddingsInterface
import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import com.mapconductor.googlemaps.zoom.ZoomAltitudeConverter

private val converter = ZoomAltitudeConverter(AbstractZoomAltitudeConverter.DEFAULT_ZOOM0_ALTITUDE)

fun MapCameraPosition.toCameraPosition(): CameraPosition =
    CameraPosition
        .builder()
        .target(GeoPoint.from(position).toLatLng())
        .zoom(zoom.toFloat())
        .tilt(tilt.toFloat())
        .bearing(bearing.toFloat())
        .build()

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

fun CameraPosition.toMapCameraPosition(paddings: MapPaddingsInterface = MapPaddings.Zeros): MapCameraPosition {
    val altitude =
        converter.zoomLevelToAltitude(
            zoomLevel = zoom.toDouble(),
            latitude = target.latitude,
            tilt = tilt.toDouble(),
        )
    val position = target.toGeoPoint().copy(altitude = altitude)
    return MapCameraPosition(
        position = position,
        zoom = zoom.toDouble(),
        bearing = bearing.toDouble(),
        tilt = tilt.toDouble(),
        paddings = paddings,
        visibleRegion = null,
    )
}
