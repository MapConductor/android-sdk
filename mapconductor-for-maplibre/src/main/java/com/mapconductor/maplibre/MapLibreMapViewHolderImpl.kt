package com.mapconductor.maplibre

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapViewHolder
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import android.graphics.PointF

interface MapLibreMapViewHolder : MapViewHolder<MapView, MapLibreMap> {
    fun getController(): MapLibreViewControllerImpl?
}

internal class MapLibreMapViewHolderImpl(
    override val mapView: MapView,
    override val map: MapLibreMap,
) : MapLibreMapViewHolder {
    private var controller: MapLibreViewControllerImpl? = null

    fun setController(ctrl: MapLibreViewControllerImpl) {
        controller = ctrl
    }

    override fun getController(): MapLibreViewControllerImpl? = controller

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
