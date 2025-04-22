package com.mapconductor.mapbox

import android.content.Context
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapconductor.core.MapViewHolder
import com.mapconductor.core.MapViewHolderStoreBaseSync

internal object MapViewHolderStore : MapViewHolderStoreBaseSync<MapView, MapboxMap>() {

    override fun getOrCreate(
        context: Context,
        id: String,
    ): MapViewHolder<MapView, MapboxMap> {
        val existing = this.get(id)
        if (existing != null) return existing

        val newHolder = MapViewHolderImpl.create(context.applicationContext)
        this.set(id, newHolder)
        return newHolder
    }
}
