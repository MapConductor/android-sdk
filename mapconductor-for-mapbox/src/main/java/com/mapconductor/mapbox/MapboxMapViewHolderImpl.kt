package com.mapconductor.mapbox

import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxLifecycleObserver
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.lifecycle.lifecycle
import com.mapconductor.core.map.MapViewHolder
import android.content.Context

typealias MapboxMapViewHolder = MapViewHolder<MapView, MapboxMap>

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
            val cameraOptions =
                CameraOptions
                    .Builder()
                    .center(mapInitOptions.cameraOptions!!.center)
                    .bearing(mapInitOptions.cameraOptions!!.bearing)
                    .zoom(mapInitOptions.cameraOptions!!.zoom!! - 1.0)
                    .pitch(mapInitOptions.cameraOptions!!.pitch)
                    .build()

            val internalOptions =
                MapInitOptions(
                    context = context,
                    textureView = true,
                    styleUri = mapInitOptions.styleUri,
                    cameraOptions = cameraOptions,
                )

            val mapView = MapView(context, internalOptions)
            val holder = MapboxMapViewHolderImpl(mapView)
            holder.map = mapView.mapboxMap
            return holder
        }
    }

    override fun onDestroy() = Unit

    override fun onLowMemory() = Unit

    override fun onStart() = Unit

    override fun onStop() = Unit
}
