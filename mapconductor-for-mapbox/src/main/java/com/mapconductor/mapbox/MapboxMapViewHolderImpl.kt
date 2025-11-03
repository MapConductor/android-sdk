package com.mapconductor.mapbox

import androidx.compose.ui.geometry.Offset
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxLifecycleObserver
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.plugin.lifecycle.lifecycle
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapViewHolder

typealias MapboxMapViewHolder = MapViewHolder<MapView, MapboxMap>

class MapboxMapViewHolderImpl(
    override val mapView: MapView,
    override val map: MapboxMap,
) : MapViewHolder<MapView, MapboxMap>,
    MapboxLifecycleObserver {
    init {
        this.mapView.lifecycle.registerLifecycleObserver(this.mapView, this)
    }

    override fun toScreenOffset(position: GeoPoint): Offset? {
        val pixel =
            map.pixelForCoordinate(
                coordinate = GeoPointImpl.from(position).toPoint(),
            )
        return Offset(
            x = pixel.x.toFloat(),
            y = pixel.y.toFloat(),
        )
    }

    override fun fromScreenOffsetSync(offset: Offset): GeoPointImpl? =
        map.coordinateForPixel(ScreenCoordinate(offset.x.toDouble(), offset.y.toDouble())).toGeoPoint()

    fun fromScreenOffset(coordinate: ScreenCoordinate): GeoPointImpl? = map.coordinateForPixel(coordinate).toGeoPoint()

    override suspend fun fromScreenOffset(offset: Offset): GeoPointImpl? =
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
