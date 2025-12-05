package com.mapconductor.googlemaps

import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapViewHolder
import android.graphics.Point

class GoogleMapViewHolderImpl(
    override val mapView: MapView,
    override val map: GoogleMap,
) : MapViewHolder<MapView, GoogleMap> {
    override fun toScreenOffset(position: GeoPoint): Offset? {
        val point =
            map.projection.toScreenLocation(
                GeoPointImpl.from(position).toLatLng(),
            )
        return Offset(
            x = point.x.toFloat(),
            y = point.y.toFloat(),
        )
    }

    override suspend fun fromScreenOffset(offset: Offset): GeoPointImpl? =
        map.projection
            .fromScreenLocation(
                Point(
                    offset.x.toInt(),
                    offset.y.toInt(),
                ),
            ).toGeoPoint()
}
