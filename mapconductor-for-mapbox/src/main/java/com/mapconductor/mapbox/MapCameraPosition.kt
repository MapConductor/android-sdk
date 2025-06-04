package com.mapconductor.mapbox

import androidx.annotation.Keep
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.IMapCameraPosition
import com.mapconductor.core.map.MapCameraPositionBase

interface MapCameraPositionMBox : IMapCameraPosition {
    fun toCameraOptions(): CameraOptions

    fun toCameraState(): CameraState

//    fun copy(
//        position: GeoPoint? = null,
//        zoom: Double? = null,
//        bearing: Double? = null,
//        tilt: Double? = null,
//        paddings: MapPaddings? = null,
//    ): MapCameraPositionMBox
}

@Keep
data class MapCameraPosition(
    override val position: IGeoPoint,
    override val zoom: Double = 2.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: IMapboxPaddings? = MapboxPaddings.Zeros,
) : MapCameraPositionBase(position, zoom, bearing, tilt, paddings),
    MapCameraPositionMBox {
    override fun toCameraOptions() =
        CameraOptions
            .Builder()
            .center(GeoPoint.from(position).toPoint())
            .zoom(zoom)
            .pitch(tilt)
            .bearing(bearing)
            .padding(paddings?.toEdgeInsects())
            .build()

    override fun toCameraState() =
        CameraState(
            GeoPoint.from(position).toPoint(),
            EdgeInsets(0.0, 0.0, 0.0, 0.0),
            zoom,
            bearing,
            tilt,
        )

    companion object {
        val Default =
            MapCameraPosition(
                position =
                    GeoPoint.fromLatLong(
                        latitude = 0.0,
                        longitude = 0.0,
                    ),
                zoom = 0.0,
                bearing = 0.0,
                tilt = 0.0,
            )

        fun from(cameraPosition: IMapCameraPosition) =
            when (cameraPosition) {
                is MapCameraPosition -> cameraPosition
                else ->
                    MapCameraPosition(
                        position = GeoPoint.from(cameraPosition.position),
                        zoom = cameraPosition.zoom,
                        bearing = cameraPosition.bearing,
                        tilt = cameraPosition.tilt,
                        paddings = MapboxPaddings.fromImpl(cameraPosition.paddings),
                    )
            }
    }
}

fun CameraOptions.toMapCameraPosition() =
    MapCameraPosition(
        position = center?.toGeoPoint() ?: GeoPoint.fromLongLat(0.0, 0.0),
        zoom = zoom ?: 2.0,
        bearing = bearing ?: 0.0,
        tilt = pitch ?: 0.0,
        paddings = padding?.toPaddings(),
    )

fun CameraState.toMapCameraPosition() =
    MapCameraPosition(
        position = center.toGeoPoint(),
        zoom = zoom,
        bearing = bearing,
        tilt = pitch,
    )
