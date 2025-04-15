package com.mapconductor.mapbox

import com.mapbox.maps.CameraOptions
import androidx.annotation.Keep
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets

@Keep
data class MapCameraPosition @JvmOverloads constructor(
    val target: GeoPoint,
    val zoom: Double = 2.0,
    val bearing: Double = 0.0,
    val tilt: Double = 0.0,
    val paddings: MapPaddings = MapPaddings.Zeros,
) {
    internal fun toCameraOptions() = CameraOptions.Builder()
        .center(target.toPoint())
        .zoom(zoom)
        .pitch(tilt)
        .bearing(bearing)
        .padding(paddings.toEdgeInsects())
        .build()

    internal fun toCameraState() = CameraState(
        target.toPoint(),
        EdgeInsets(0.0, 0.0, 0.0, 0.0),
        zoom,
        bearing,
        tilt,
    )
}
internal fun CameraOptions.toMapCameraPosition() =
    MapCameraPosition(
        target = center?.toGeoPoint() ?: GeoPoint.fromLongLat(0.0, 0.0),
        zoom = zoom ?: 2.0,
        bearing = bearing ?: 0.0,
        tilt = pitch ?: 0.0,
    )

internal fun CameraState.toMapCameraPosition() =
    MapCameraPosition(
        target = center.toGeoPoint(),
        zoom = zoom,
        bearing = bearing,
        tilt = pitch,
    )
