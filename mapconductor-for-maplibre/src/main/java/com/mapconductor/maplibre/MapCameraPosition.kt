package com.mapconductor.maplibre

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import org.maplibre.android.camera.CameraPosition
import kotlin.math.max

internal const val MAPLIBRE_CAMERA_ZOOM_ADJUST_VALUE = 1.0

fun MapCameraPosition.toCameraPosition(): CameraPosition =
    CameraPosition
        .Builder()
        .target(GeoPoint.from(position).toLatLng())
        .zoom(max(zoom - MAPLIBRE_CAMERA_ZOOM_ADJUST_VALUE, 0.0))
        .tilt(tilt)
        .bearing(bearing)
        // TODO:
//    .padding(paddings?.toEdgeInsects())
        .build()

fun MapCameraPosition.Companion.from(cameraPosition: MapCameraPositionInterface) =
    when (cameraPosition) {
        is MapCameraPosition -> cameraPosition
        else ->
            MapCameraPosition(
                position = cameraPosition.position,
                zoom = cameraPosition.zoom,
                bearing = cameraPosition.bearing,
                tilt = cameraPosition.tilt,
                visibleRegion = cameraPosition.visibleRegion,
            )
    }

fun CameraPosition.toMapCameraPosition() =
    MapCameraPosition(
        position = target?.toGeoPoint() ?: GeoPoint.fromLongLat(0.0, 0.0),
        zoom = (zoom) + MAPLIBRE_CAMERA_ZOOM_ADJUST_VALUE,
        bearing = bearing ?: 0.0,
        tilt = tilt ?: 0.0,
        visibleRegion = null,
    )
