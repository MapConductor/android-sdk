package com.mapconductor.here

import androidx.annotation.Keep
import com.here.sdk.mapview.MapCamera

@Keep
data class MapCameraPosition @JvmOverloads constructor(
    val target: GeoPoint,
    val zoom: Double = 2.0,
    val bearing: Double = 0.0,
    val tilt: Double = 0.0,
    val paddings: MapPadding = MapPadding.Zeros,
) {

//    internal fun toCameraState() = MapCamera.State(
//        target.toGeoCoordinates(),
//        GeoOrientation(bearing, tilt),
//        zoom,
//    )
}

internal fun MapCamera.State.toMapCameraPosition() =
    MapCameraPosition(
        target = targetCoordinates.toGeoPoint(),
        zoom = zoomLevel,
        bearing = this.orientationAtTarget.bearing,
        tilt = this.orientationAtTarget.tilt,
    )
