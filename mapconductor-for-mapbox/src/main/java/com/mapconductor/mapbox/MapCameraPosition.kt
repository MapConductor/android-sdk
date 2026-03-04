package com.mapconductor.mapbox

import com.mapbox.maps.CameraChanged
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.mapbox.zoom.ZoomAltitudeConverter

fun CameraChanged.toMapCameraPosition() =
    CameraOptions
        .Builder()
        .padding(cameraState.padding)
        .center(cameraState.center)
        .zoom(ZoomAltitudeConverter.mapboxZoomToGoogleZoom(cameraState.zoom))
        .bearing(cameraState.bearing)
        .pitch(cameraState.pitch)
        .build()

fun MapCameraPosition.toCameraOptions(): CameraOptions =
    CameraOptions
        .Builder()
        .center(GeoPoint.from(position).toPoint())
        .zoom(ZoomAltitudeConverter.googleZoomToMapboxZoom(zoom))
        .pitch(tilt)
        .bearing(bearing)
        // TODO:
//    .padding(paddings?.toEdgeInsects())
        .build()

fun MapCameraPosition.toCameraState(): CameraState =
    CameraState(
        GeoPoint.from(position).toPoint(),
        EdgeInsets(0.0, 0.0, 0.0, 0.0),
        ZoomAltitudeConverter.googleZoomToMapboxZoom(zoom),
        bearing,
        tilt,
    )

fun MapCameraPosition.Companion.from(cameraPosition: MapCameraPositionInterface) =
    when (cameraPosition) {
        is MapCameraPosition -> cameraPosition
        else ->
            MapCameraPosition(
                position = cameraPosition.position,
                zoom = cameraPosition.zoom,
                bearing = cameraPosition.bearing,
                tilt = cameraPosition.tilt,
                paddings = MapboxPaddings.from(cameraPosition.paddings),
                visibleRegion = cameraPosition.visibleRegion,
            )
    }

fun CameraOptions.toMapCameraPosition() =
    MapCameraPosition(
        position = center?.toGeoPoint() ?: GeoPoint.fromLongLat(0.0, 0.0),
        zoom = ZoomAltitudeConverter.mapboxZoomToGoogleZoom(zoom ?: 2.0),
        bearing = bearing ?: 0.0,
        tilt = pitch ?: 0.0,
        paddings = padding?.toPaddings(),
        visibleRegion = null,
    )

fun CameraState.toMapCameraPosition() =
    MapCameraPosition(
        position = center.toGeoPoint(),
        zoom = ZoomAltitudeConverter.mapboxZoomToGoogleZoom(zoom),
        bearing = bearing,
        tilt = pitch,
        visibleRegion = null,
    )
