package com.mapconductor.googlemaps

import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapViewHolder
import android.content.Context
import android.graphics.Point
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

internal class GoogleMapViewHolderImpl private constructor(
    override val mapView: MapView,
) : MapViewHolder<MapView, GoogleMap> {
    override lateinit var map: GoogleMap
    override fun toScreenOffset(position: IGeoPoint): Offset? {
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

    companion object {
        @OptIn(ExperimentalCoroutinesApi::class)
        suspend fun create(
            context: Context,
            options: GoogleMapOptions? = null,
        ): MapViewHolder<MapView, GoogleMap> {
            val mapView = MapView(context, options).apply { onCreate(null) }

            val holder = GoogleMapViewHolderImpl(mapView)

            suspendCancellableCoroutine<Unit> { cont ->
                mapView.getMapAsync {
                    holder.map = it
                    cont.resume(Unit) {}
                }
            }

            return holder
        }
    }
}
