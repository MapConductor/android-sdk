package com.mapconductor.mapbox

import android.content.Context
import android.view.ViewGroup
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapconductor.core.MapViewHolderImpl

class MapViewHolder private constructor(
    override val mapView: MapView
): MapViewHolderImpl<MapView, MapboxMap> {
    override lateinit var map: MapboxMap

    companion object {
        fun create(context: Context): MapViewHolder {
            val mapView = MapView(context)

            val holder = MapViewHolder(mapView)
            holder.map = mapView.mapboxMap
            return holder
        }
    }

    override fun attachTo(container: ViewGroup) {
        if (mapView.parent === container) return
        this.detach()
        container.addView(mapView)
    }

    override fun detach() {
        if (mapView.parent == null) return
        (mapView.parent as ViewGroup).removeView(mapView)
    }

    // Do nothing here
    override fun destroy() = Unit
}
