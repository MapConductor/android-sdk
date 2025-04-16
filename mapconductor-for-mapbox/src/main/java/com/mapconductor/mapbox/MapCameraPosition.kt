package com.mapconductor.mapbox

import androidx.annotation.Keep
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapconductor.core.MapCameraPositionImpl
import com.mapconductor.core.MapPaddingsImpl

interface MapCameraPositionMBoxImpl: MapCameraPositionImpl {
    fun toCameraOptions(): CameraOptions
    fun toCameraState(): CameraState

    fun copy(
        target: GeoPoint? = null,
        zoom: Double? = null,
        bearing: Double? = null,
        tilt: Double? = null,
        paddings: MapPaddingsImpl? = null,
    ): MapCameraPositionMBoxImpl
}

@Keep
data class MapCameraPosition @JvmOverloads constructor(
    override val target: GeoPoint,
    override val zoom: Double = 2.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddingsMBoxImpl = MapPaddingsMbox.Zeros,
): MapCameraPositionMBoxImpl {

    override fun copy(
        target: GeoPoint?,
        zoom: Double?,
        bearing: Double?,
        tilt: Double?,
        paddings: MapPaddingsImpl?
    ) = MapCameraPosition(
            target = target ?: this.target,
            zoom = zoom ?: this.zoom,
            bearing = bearing ?: this.bearing,
            tilt = tilt ?: this.tilt,
            paddings = (paddings ?: this.paddings) as MapPaddingsMBoxImpl
        )

    override fun toCameraOptions() = CameraOptions.Builder()
        .center(target.toPoint())
        .zoom(zoom)
        .pitch(tilt)
        .bearing(bearing)
        .padding(paddings.toEdgeInsects())
        .build()

    override fun toCameraState() = CameraState(
        target.toPoint(),
        EdgeInsets(0.0, 0.0, 0.0, 0.0),
        zoom,
        bearing,
        tilt,
    )

    companion object {
        fun fromImpl(MapCameraPositionImpl: MapCameraPositionImpl) =
            when(MapCameraPositionImpl) {
                is MapCameraPosition -> MapCameraPositionImpl
                else -> MapCameraPosition(
                    target = GeoPoint.fromImpl(MapCameraPositionImpl.target),
                    zoom = MapCameraPositionImpl.zoom,
                    bearing = MapCameraPositionImpl.bearing,
                    tilt = MapCameraPositionImpl.tilt,
                    paddings = MapPaddingsMbox.fromImpl(MapCameraPositionImpl.paddings),
                )
            }
    }
}
fun CameraOptions.toMapCameraPosition() =
    MapCameraPosition(
        target = center?.toGeoPoint() ?: GeoPoint.fromLongLat(0.0, 0.0),
        zoom = zoom ?: 2.0,
        bearing = bearing ?: 0.0,
        tilt = pitch ?: 0.0,
    )

fun CameraState.toMapCameraPosition() =
    MapCameraPosition(
        target = center.toGeoPoint(),
        zoom = zoom,
        bearing = bearing,
        tilt = pitch,
    )
