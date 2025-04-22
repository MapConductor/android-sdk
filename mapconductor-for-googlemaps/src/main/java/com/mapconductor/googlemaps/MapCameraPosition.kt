package com.mapconductor.googlemaps

import com.google.android.gms.maps.model.CameraPosition
import androidx.annotation.Keep // 必要に応じて @Keep も追加検討
import com.mapconductor.core.GeoPointInterface
import com.mapconductor.core.MapCameraPositionImpl
import com.mapconductor.core.MapPaddings
import com.mapconductor.core.MapPaddingsImpl

interface MapCameraPositionGMapsImpl: MapCameraPositionImpl {
    fun toCameraPosition(): CameraPosition

    fun copy(
        target: GeoPoint?,
        zoom: Double?,
        bearing: Double?,
        tilt: Double?,
        paddings: MapPaddingsImpl?,
    ): MapCameraPositionGMapsImpl
}

@Keep
data class MapCameraPosition @JvmOverloads constructor(
    override val target: GeoPointInterface,
    override val zoom: Double = 2.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddingsImpl = MapPaddings.Zeros,
): MapCameraPositionGMapsImpl {

    override fun toCameraPosition() =
        CameraPosition.builder()
            .target(GeoPoint.fromImpl(target).toLatLng())
            .zoom(zoom.toFloat())
            .tilt(tilt.toFloat())
            .bearing(bearing.toFloat())
            .build()

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
        paddings = paddings ?: this.paddings,
    )

    companion object {
        fun fromImpl(positionImpl: MapCameraPositionImpl) = when(positionImpl) {
            is MapCameraPosition -> positionImpl
            else -> MapCameraPosition(
                target = GeoPoint.fromImpl(positionImpl.target),
                zoom = positionImpl.zoom,
                bearing = positionImpl.bearing,
                tilt = positionImpl.tilt,
                paddings = positionImpl.paddings,
            )
        }
    }
}

fun CameraPosition.toMapCameraPosition(
    paddings: MapPaddingsImpl = MapPaddings.Zeros,
) = MapCameraPosition(
        target = target.toGeoPoint(),
        zoom = zoom.toDouble(),
        bearing = bearing.toDouble(),
        tilt = tilt.toDouble(),
        paddings = paddings,
    )
