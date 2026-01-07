package com.mapconductor.maplibre

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapViewHolderInterface
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import android.graphics.PointF

interface MapLibreMapViewHolderInterface : MapViewHolderInterface<MapView, MapLibreMap> {
    fun getController(): MapLibreViewController?
}

internal class MapLibreMapViewHolder(
    override val mapView: MapView,
    override val map: MapLibreMap,
) : MapLibreMapViewHolderInterface {
    private var controller: MapLibreViewController? = null

    fun setController(ctrl: MapLibreViewController) {
        controller = ctrl
    }

    override fun getController(): MapLibreViewController? = controller

    override fun toScreenOffset(position: GeoPointInterface): Offset? {
        val pixel =
            map.projection.toScreenLocation(GeoPoint.from(position).toLatLng())
        return Offset(
            x = pixel.x,
            y = pixel.y,
        )
    }

    override fun fromScreenOffsetSync(offset: Offset): GeoPoint? =
        map.projection.fromScreenLocation(PointF(offset.x, offset.y)).toGeoPoint()

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? = fromScreenOffsetSync(offset)
}
