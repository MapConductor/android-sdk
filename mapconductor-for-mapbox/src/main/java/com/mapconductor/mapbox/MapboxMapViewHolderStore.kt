package com.mapconductor.mapbox

import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.MapViewHolderStoreBaseAsync
import com.mapconductor.core.map.StaticHolder
import android.annotation.SuppressLint
import android.content.Context

typealias MapboxMapViewHolder = MapViewHolder<MapView, MapboxMap>

object MapboxMapViewOptionsStore : StaticHolder<MapInitOptions?>()

object MapboxMapViewHolderStore : MapViewHolderStoreBaseAsync<MapView, MapboxMap, MapInitOptions>() {
    @SuppressLint("RestrictedApi")
    override suspend fun getOrCreate(
        context: Context,
        id: String,
        options: MapInitOptions,
    ): MapboxMapViewHolder {
        val existing = this.get(id)
        // The below code does not effect at all.
        // if (existing != null) {
        //     options.cameraOptions?.let {
        //         existing.mapView.mapboxMap.setCamera(it)
        //     }
        //     return existing
        // }
        val mapInitOptions =
            MapInitOptions(
                context = context,
                textureView = true,
            )
        options.mapOptions?.let {
            mapInitOptions.mapOptions = it
        }
        options.cameraOptions?.let {
            mapInitOptions.cameraOptions = it
        }

        val newHolder = MapboxMapViewHolderImpl.create(context, mapInitOptions)
        this.set(id, newHolder)
        return newHolder
    }
}
