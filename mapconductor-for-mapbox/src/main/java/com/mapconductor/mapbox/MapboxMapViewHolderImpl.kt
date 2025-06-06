package com.mapconductor.mapbox

import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxLifecycleObserver
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.lifecycle.lifecycle
import com.mapconductor.core.map.MapViewHolder
import android.content.Context

class MapboxMapViewHolderImpl private constructor(
    override val mapView: MapView,
) : MapViewHolder<MapView, MapboxMap>,
    MapboxLifecycleObserver {
    override lateinit var map: MapboxMap

    init {
        this.mapView.lifecycle.registerLifecycleObserver(this.mapView, this)
    }

    companion object {
        fun create(
            context: Context,
            mapInitOptions: MapInitOptions,
        ): MapViewHolder<MapView, MapboxMap> {
            val mapView = MapView(context, mapInitOptions)
            val holder = MapboxMapViewHolderImpl(mapView)
            holder.map = mapView.mapboxMap
            return holder
        }
    }

    //    override fun attachTo(container: ViewGroup) {
//        if (mapView.parent === container) return
//        this.detach()
//        container.addView(mapView)
//    }
//
//    override fun detach() {
//        if (mapView.parent == null) return
//        (mapView.parent as ViewGroup).removeView(mapView)
//    }
//
//    override fun destroy() = Unit
    override fun onDestroy() = Unit

    override fun onLowMemory() = Unit

    override fun onStart() = Unit

    override fun onStop() = Unit
}
