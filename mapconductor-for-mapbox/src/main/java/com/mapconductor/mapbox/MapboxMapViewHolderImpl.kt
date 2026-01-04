package com.mapconductor.mapbox

import androidx.compose.ui.geometry.Offset
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxLifecycleObserver
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.plugin.lifecycle.lifecycle
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapViewHolderInterface

typealias MapboxMapViewHolderInterface = MapViewHolderInterface<MapView, MapboxMap>

class MapboxMapViewHolder(
    override val mapView: MapView,
    override val map: MapboxMap,
) : MapViewHolderInterface<MapView, MapboxMap>,
    MapboxLifecycleObserver {
    init {
        this.mapView.lifecycle.registerLifecycleObserver(this.mapView, this)
    }

    override fun toScreenOffset(position: GeoPointInterface): Offset? {
        val pixel =
            map.pixelForCoordinate(
                coordinate = GeoPoint.from(position).toPoint(),
            )
        return Offset(
            x = pixel.x.toFloat(),
            y = pixel.y.toFloat(),
        )
    }

    override fun fromScreenOffsetSync(offset: Offset): GeoPoint? =
        map.coordinateForPixel(ScreenCoordinate(offset.x.toDouble(), offset.y.toDouble())).toGeoPoint()

    fun fromScreenOffset(coordinate: ScreenCoordinate): GeoPoint? = map.coordinateForPixel(coordinate).toGeoPoint()

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? =
        fromScreenOffset(
            ScreenCoordinate(
                offset.x.toDouble(),
                offset.y.toDouble(),
            ),
        )

    override fun onDestroy() {
    }

    override fun onLowMemory() {
    }

    override fun onStart() {
    }

    override fun onStop() {
    }
}
