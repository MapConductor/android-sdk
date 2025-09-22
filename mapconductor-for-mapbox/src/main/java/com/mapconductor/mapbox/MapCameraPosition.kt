package com.mapconductor.mapbox

import com.mapbox.maps.CameraChanged
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import com.mapconductor.mapbox.zoom.ZoomAltitudeConverter

private val converter = ZoomAltitudeConverter(AbstractZoomAltitudeConverter.DEFAULT_ZOOM0_ALTITUDE)

internal const val MAPBOX_CAMERA_ZOOM_ADJUST_VALUE = 1.0

fun CameraChanged.toMapCameraPosition() =
    CameraOptions
        .Builder()
        .padding(cameraState.padding)
        .center(cameraState.center)
        .zoom(cameraState.zoom + MAPBOX_CAMERA_ZOOM_ADJUST_VALUE)
        .bearing(cameraState.bearing)
        .pitch(cameraState.pitch)
        .build()

fun MapCameraPositionImpl.toCameraOptions(): CameraOptions =
    CameraOptions
        .Builder()
        .center(GeoPointImpl.from(position).toPoint())
        .zoom(zoom - MAPBOX_CAMERA_ZOOM_ADJUST_VALUE)
        .pitch(tilt)
        .bearing(bearing)
        // TODO:
//    .padding(paddings?.toEdgeInsects())
        .build()

fun MapCameraPositionImpl.toCameraState(): CameraState =
    CameraState(
        GeoPointImpl.from(position).toPoint(),
        EdgeInsets(0.0, 0.0, 0.0, 0.0),
        zoom - MAPBOX_CAMERA_ZOOM_ADJUST_VALUE,
        bearing,
        tilt,
    )

fun MapCameraPositionImpl.Companion.from(cameraPosition: MapCameraPosition) =
    when (cameraPosition) {
        is MapCameraPositionImpl -> cameraPosition
        else ->
            MapCameraPositionImpl(
                position = GeoPointImpl.from(cameraPosition.position),
                zoom = cameraPosition.zoom,
                bearing = cameraPosition.bearing,
                tilt = cameraPosition.tilt,
                paddings = MapboxPaddings.fromImpl(cameraPosition.paddings),
                visibleRegion = cameraPosition.visibleRegion,
            )
    }

fun CameraOptions.toMapCameraPosition() =
    MapCameraPositionImpl(
        position = center?.toGeoPoint() ?: GeoPointImpl.fromLongLat(0.0, 0.0),
        zoom = (zoom ?: 2.0) + MAPBOX_CAMERA_ZOOM_ADJUST_VALUE,
        bearing = bearing ?: 0.0,
        tilt = pitch ?: 0.0,
        paddings = padding?.toPaddings(),
        visibleRegion = null,
    )

fun CameraState.toMapCameraPosition() =
    MapCameraPositionImpl(
        position = center.toGeoPoint(),
        zoom = zoom + MAPBOX_CAMERA_ZOOM_ADJUST_VALUE,
        bearing = bearing,
        tilt = pitch,
        visibleRegion = null,
    )
