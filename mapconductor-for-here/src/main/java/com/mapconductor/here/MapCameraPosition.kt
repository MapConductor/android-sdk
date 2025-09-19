package com.mapconductor.here

import androidx.annotation.Keep
import com.here.sdk.core.GeoOrientation
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraUpdate
import com.here.sdk.mapview.MapCameraUpdateFactory
import com.here.sdk.mapview.MapMeasure
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import com.mapconductor.here.zoom.ZoomAltitudeConverter

private val converter = ZoomAltitudeConverter(AbstractZoomAltitudeConverter.DEFAULT_ZOOM0_ALTITUDE)

@Keep
fun MapCameraPositionImpl.toMapCameraUpdate(): MapCameraUpdate =
    MapCameraUpdateFactory.lookAt(
        GeoPointImpl.from(position).toGeoCoordinates().toUpdate(),
        GeoOrientation(bearing, tilt).toUpdate(),
        MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, zoom),
    )

@Keep
fun MapCameraPositionImpl.toCameraState(): MapCamera.State =
    MapCamera.State(
        GeoPointImpl.from(position).toGeoCoordinates(),
        GeoOrientation(bearing, tilt),
        0.0,
        zoom,
    )

fun MapCameraPositionImpl.Companion.from(position: MapCameraPosition): MapCameraPositionImpl =
    when (position) {
        is MapCameraPositionImpl -> position
        else ->
            MapCameraPositionImpl(
                position = GeoPointImpl.from(position.position),
                zoom = position.zoom,
                bearing = position.bearing,
                tilt = position.tilt,
                paddings = position.paddings,
                visibleRegion = position.visibleRegion,
            )
    }

fun MapCamera.State.toMapCameraPosition(): MapCameraPositionImpl {
    val altitude =
        converter.zoomLevelToAltitude(
            zoomLevel = zoomLevel,
            latitude = targetCoordinates.latitude,
            tilt = orientationAtTarget.tilt,
        )
    val position = targetCoordinates.toGeoPoint().copy(altitude = altitude)
    return MapCameraPositionImpl(
        position = position,
        zoom = zoomLevel,
        bearing = this.orientationAtTarget.bearing,
        tilt = this.orientationAtTarget.tilt,
        visibleRegion = null,
    )
}
