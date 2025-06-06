package com.mapconductor.mapbox

import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.MapCameraPosition

fun MapCameraPosition.toCameraOptions(): CameraOptions =
    CameraOptions
        .Builder()
        .center(GeoPoint.from(position).toPoint())
        .zoom(zoom)
        .pitch(tilt)
        .bearing(bearing)
        // TODO:
//    .padding(paddings?.toEdgeInsects())
        .build()

fun MapCameraPosition.toCameraState(): CameraState =
    CameraState(
        GeoPoint.from(position).toPoint(),
        EdgeInsets(0.0, 0.0, 0.0, 0.0),
        zoom,
        bearing,
        tilt,
    )

fun MapCameraPosition.Companion.from(cameraPosition: IMapCameraPosition) =
    when (cameraPosition) {
        is MapCameraPosition -> cameraPosition
        else ->
            MapCameraPosition(
                position = GeoPoint.from(cameraPosition.position),
                zoom = cameraPosition.zoom,
                bearing = cameraPosition.bearing,
                tilt = cameraPosition.tilt,
                paddings = MapboxPaddings.fromImpl(cameraPosition.paddings),
            )
    }

fun CameraOptions.toMapCameraPosition() =
    MapCameraPosition(
        position = center?.toGeoPoint() ?: GeoPoint.fromLongLat(0.0, 0.0),
        zoom = zoom ?: 2.0,
        bearing = bearing ?: 0.0,
        tilt = pitch ?: 0.0,
        paddings = padding?.toPaddings(),
    )

fun CameraState.toMapCameraPosition() =
    MapCameraPosition(
        position = center.toGeoPoint(),
        zoom = zoom,
        bearing = bearing,
        tilt = pitch,
    )
