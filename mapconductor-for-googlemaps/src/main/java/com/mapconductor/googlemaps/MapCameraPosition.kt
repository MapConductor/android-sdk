package com.mapconductor.googlemaps

import com.google.android.gms.maps.model.CameraPosition
import androidx.annotation.Keep // 必要に応じて @Keep も追加検討

@Keep
data class MapCameraPosition @JvmOverloads constructor(
    val target: GeoPoint,
    val zoom: Double = 0.0,
    val bearing: Double = 0.0,
    val tilt: Double = 0.0,
    val paddings: MapPadding = MapPadding.Zeros,
)
internal fun CameraPosition.toMapCameraPosition(paddings: MapPadding? = null): MapCameraPosition {
    return MapCameraPosition(
        target = target.toGeoPoint(),
        zoom = zoom.toDouble(),
        bearing = bearing.toDouble(),
        tilt = tilt.toDouble(),
        paddings = paddings ?: MapPadding.Zeros,
    )
}

internal fun MapCameraPosition.toCameraPosition() =
    CameraPosition.builder()
        .target(target.toLatLng())
        .zoom(zoom.toFloat())
        .tilt(tilt.toFloat())
        .bearing(bearing.toFloat())
        .build()
