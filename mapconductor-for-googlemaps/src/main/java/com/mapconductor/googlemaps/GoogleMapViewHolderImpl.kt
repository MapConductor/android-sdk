package com.mapconductor.googlemaps

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.mapconductor.core.map.MapViewHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import android.content.Context

internal class GoogleMapViewHolderImpl private constructor(
    override val mapView: MapView,
) : MapViewHolder<MapView, GoogleMap> {
    override lateinit var map: GoogleMap

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
