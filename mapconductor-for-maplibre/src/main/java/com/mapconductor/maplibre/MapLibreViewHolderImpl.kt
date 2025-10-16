package com.mapconductor.maplibre

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapViewHolder
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import android.content.Context
import android.graphics.PointF
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine


typealias MapLibreViewHolder = MapViewHolder<MapView, MapLibreMap>

internal class MapLibreViewHolderImpl(
    override val mapView: MapView,
) : MapViewHolder<MapView, MapLibreMap> {
    override lateinit var map: MapLibreMap

    override fun toScreenOffset(position: GeoPoint): Offset? {
        val pixel =
            map.projection.toScreenLocation(GeoPointImpl.from(position).toLatLng())
        return Offset(
            x = pixel.x,
            y = pixel.y,
        )
    }

    override fun fromScreenOffsetSync(offset: Offset): GeoPointImpl? =
        map.projection.fromScreenLocation(PointF(offset.x, offset.y)).toGeoPoint()

    override suspend fun fromScreenOffset(offset: Offset): GeoPointImpl? = fromScreenOffsetSync(offset)
}
