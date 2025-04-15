package com.mapconductor.mapbox

import android.content.Context
import android.view.ViewGroup
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import kotlinx.coroutines.suspendCancellableCoroutine

class MapViewHolder private constructor(
    val mapView: MapView
) {
    lateinit var map: MapboxMap

    companion object {
        fun create(context: Context): MapViewHolder {
            val mapView = MapView(context)

            val holder = MapViewHolder(mapView)
            holder.map = mapView.mapboxMap
            return holder
        }
    }

    fun attachTo(container: ViewGroup) {
        if (mapView.parent === container) return
        (mapView.parent as? ViewGroup)?.removeView(mapView)
        container.addView(mapView)
    }

    fun destroy() {
        // Do nothing here
        Unit
    }
}
