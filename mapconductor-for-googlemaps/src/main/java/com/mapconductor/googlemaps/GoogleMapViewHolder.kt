package com.mapconductor.googlemaps

import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapViewHolderInterface
import android.graphics.Point

class GoogleMapViewHolder(
    override val mapView: MapView,
    override val map: GoogleMap,
) : MapViewHolderInterface<MapView, GoogleMap> {
    override fun toScreenOffset(position: GeoPointInterface): Offset? {
        val point =
            map.projection.toScreenLocation(
                GeoPoint.from(position).toLatLng(),
            )
        return Offset(
            x = point.x.toFloat(),
            y = point.y.toFloat(),
        )
    }

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? =
        map.projection
            .fromScreenLocation(
                Point(
                    offset.x.toInt(),
                    offset.y.toInt(),
                ),
            ).toGeoPoint()
}
