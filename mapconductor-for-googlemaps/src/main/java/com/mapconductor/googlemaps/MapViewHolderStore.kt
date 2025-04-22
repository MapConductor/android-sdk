package com.mapconductor.googlemaps

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.mapconductor.core.MapViewHolder
import com.mapconductor.core.MapViewHolderStoreBaseAsync

internal object MapViewHolderStore : MapViewHolderStoreBaseAsync<MapView, GoogleMap>() {
    override suspend fun getOrCreate(
        context: Context,
        id: String,
    ): MapViewHolder<MapView, GoogleMap> {
        val existing = this.get(id)
        if (existing != null) return existing

        val newHolder = MapViewHolderImpl.create(context.applicationContext)
        this.set(id, newHolder)
        return newHolder
    }
}
