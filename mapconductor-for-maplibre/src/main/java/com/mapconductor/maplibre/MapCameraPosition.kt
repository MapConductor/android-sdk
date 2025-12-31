package com.mapconductor.maplibre

import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionImpl
import org.maplibre.android.camera.CameraPosition
import kotlin.math.max

internal const val MAPLIBRE_CAMERA_ZOOM_ADJUST_VALUE = 1.0

fun MapCameraPositionImpl.toCameraPosition(): CameraPosition =
    CameraPosition
        .Builder()
        .target(GeoPointImpl.from(position).toLatLng())
        .zoom(max(zoom - MAPLIBRE_CAMERA_ZOOM_ADJUST_VALUE, 0.0))
        .tilt(tilt)
        .bearing(bearing)
        // TODO:
//    .padding(paddings?.toEdgeInsects())
        .build()

fun MapCameraPositionImpl.Companion.from(cameraPosition: MapCameraPosition) =
    when (cameraPosition) {
        is MapCameraPositionImpl -> cameraPosition
        else ->
            MapCameraPositionImpl(
                position = cameraPosition.position,
                zoom = cameraPosition.zoom,
                bearing = cameraPosition.bearing,
                tilt = cameraPosition.tilt,
                visibleRegion = cameraPosition.visibleRegion,
            )
    }

fun CameraPosition.toMapCameraPosition() =
    MapCameraPositionImpl(
        position = target?.toGeoPoint() ?: GeoPointImpl.fromLongLat(0.0, 0.0),
        zoom = (zoom) + MAPLIBRE_CAMERA_ZOOM_ADJUST_VALUE,
        bearing = bearing ?: 0.0,
        tilt = tilt ?: 0.0,
        visibleRegion = null,
    )
