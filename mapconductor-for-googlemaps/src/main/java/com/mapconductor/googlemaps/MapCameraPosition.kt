package com.mapconductor.googlemaps

import androidx.annotation.Keep
import com.google.android.gms.maps.model.CameraPosition
import com.mapconductor.core.IMapCameraPosition
import com.mapconductor.core.MapCameraPositionBase
import com.mapconductor.core.MapPaddings
import com.mapconductor.core.MapPaddingsImpl
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint

interface MapCameraPositionGMaps: IMapCameraPosition {
    fun toCameraPosition(): CameraPosition
}

@Keep
data class MapCameraPosition(
    override val position: IGeoPoint,
    override val zoom: Double = 2.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddings? = MapPaddingsImpl.Zeros,
): MapCameraPositionBase(position, zoom, bearing, tilt, paddings), MapCameraPositionGMaps {

    override fun toCameraPosition() =
        CameraPosition.builder()
            .target(GeoPoint.from(position).toLatLng())
            .zoom(zoom.toFloat())
            .tilt(tilt.toFloat())
            .bearing(bearing.toFloat())
            .build()

    companion object {

        fun from(position: IMapCameraPosition) = when(position) {
            is MapCameraPosition -> position
            else -> MapCameraPosition(
                position = GeoPoint.from(position.position),
                zoom = position.zoom,
                bearing = position.bearing,
                tilt = position.tilt,
                paddings = position.paddings,
            )
        }
    }
}

fun CameraPosition.toMapCameraPosition(
    paddings: MapPaddings = MapPaddingsImpl.Zeros,
) = MapCameraPosition(
        position = target.toGeoPoint(),
        zoom = zoom.toDouble(),
        bearing = bearing.toDouble(),
        tilt = tilt.toDouble(),
        paddings = paddings,
    )
