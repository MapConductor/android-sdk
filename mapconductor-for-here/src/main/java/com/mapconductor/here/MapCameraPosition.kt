package com.mapconductor.here

import androidx.annotation.Keep
import com.here.sdk.core.GeoOrientation
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraUpdate
import com.here.sdk.mapview.MapCameraUpdateFactory
import com.here.sdk.mapview.MapMeasure
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.here.zoom.ZoomAltitudeConverter

@Keep
fun MapCameraPosition.toMapCameraUpdate(): MapCameraUpdate {
    val hereZoom = ZoomAltitudeConverter.googleZoomToHereZoom(zoom, position.latitude)
    return MapCameraUpdateFactory.lookAt(
        GeoPoint.from(position).toGeoCoordinates().toUpdate(),
        GeoOrientation(bearing, tilt).toUpdate(),
        MapMeasure(
            MapMeasure.Kind.ZOOM_LEVEL,
            hereZoom,
        ),
    )
}

fun MapCameraPosition.Companion.from(position: MapCameraPositionInterface): MapCameraPosition =
    when (position) {
        is MapCameraPosition -> position
        else ->
            MapCameraPosition(
                position = GeoPoint.from(position.position),
                zoom = position.zoom,
                bearing = position.bearing,
                tilt = position.tilt,
                paddings = position.paddings,
                visibleRegion = position.visibleRegion,
            )
    }

fun MapCamera.State.toMapCameraPosition(): MapCameraPosition {
    val position = targetCoordinates.toGeoPoint()
    val ourZoom = ZoomAltitudeConverter.hereZoomToGoogleZoom(zoomLevel, position.latitude)
    return MapCameraPosition(
        position = position,
        zoom = ourZoom,
        bearing = this.orientationAtTarget.bearing,
        tilt = this.orientationAtTarget.tilt,
        visibleRegion = null,
    )
}
