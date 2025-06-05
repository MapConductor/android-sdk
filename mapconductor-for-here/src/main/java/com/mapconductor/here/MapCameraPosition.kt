package com.mapconductor.here

import androidx.annotation.Keep
import com.here.sdk.core.GeoOrientation
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraUpdate
import com.here.sdk.mapview.MapCameraUpdateFactory
import com.here.sdk.mapview.MapMeasure
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.MapCameraPosition

@Keep
fun MapCameraPosition.toMapCameraUpdate(): MapCameraUpdate = MapCameraUpdateFactory.lookAt(
    GeoPoint.from(position).toGeoCoordinates().toUpdate(),
    GeoOrientation(bearing, tilt).toUpdate(),
    MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, zoom),
)

@Keep
fun MapCameraPosition.toCameraState(): MapCamera.State = MapCamera.State(
    GeoPoint.from(position).toGeoCoordinates(),
    GeoOrientation(bearing, tilt),
    0.0,
    zoom,
)

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

fun MapCamera.State.toMapCameraPosition() =
    MapCameraPosition(
        position = targetCoordinates.toGeoPoint(),
        zoom = zoomLevel,
        bearing = this.orientationAtTarget.bearing,
        tilt = this.orientationAtTarget.tilt,
    )
