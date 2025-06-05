package com.mapconductor.googlemaps

import com.google.android.gms.maps.model.CameraPosition
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapPaddings
import com.mapconductor.core.map.MapPaddingsImpl

fun MapCameraPosition.toCameraPosition(): CameraPosition =
    CameraPosition
        .builder()
        .target(GeoPoint.from(position).toLatLng())
        .zoom(zoom.toFloat())
        .tilt(tilt.toFloat())
        .bearing(bearing.toFloat())
        .build()

fun MapCameraPosition.Companion.from(position: IMapCameraPosition): MapCameraPosition = when (position) {
    is MapCameraPosition -> position
    else ->
        MapCameraPosition(
            position = GeoPoint.from(position.position),
            zoom = position.zoom,
            bearing = position.bearing,
            tilt = position.tilt,
            paddings = position.paddings,
        )
}

fun CameraPosition.toMapCameraPosition(paddings: MapPaddings = MapPaddingsImpl.Zeros) =
    MapCameraPosition(
        position = target.toGeoPoint(),
        zoom = zoom.toDouble(),
        bearing = bearing.toDouble(),
        tilt = tilt.toDouble(),
        paddings = paddings,
    )
