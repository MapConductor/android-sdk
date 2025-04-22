package com.mapconductor.mapbox

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapconductor.core.MapViewHolder

class MapViewHolderImpl private constructor(
    override val mapView: MapView
): MapViewHolder<MapView, MapboxMap> {
    override lateinit var map: MapboxMap

    companion object {
        fun create(context: Context): MapViewHolder<MapView, MapboxMap> {
            val mapView = MapView(context)

            val holder = MapViewHolderImpl(mapView)
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
    override fun destroy(owner: LifecycleOwner?) = Unit
}
