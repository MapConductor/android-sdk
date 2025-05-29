package com.mapconductor.googlemaps

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.mapconductor.core.MapViewHolder
import com.mapconductor.core.MapViewHolderStoreBaseAsync

typealias GoogleMapViewHolder = MapViewHolder<MapView, GoogleMap>

object GoogleMapViewHolderStore : MapViewHolderStoreBaseAsync<MapView, GoogleMap, GoogleMapOptions>() {
    override suspend fun getOrCreate(
        context: Context,
        id: String,
        options: GoogleMapOptions,
    ): GoogleMapViewHolder {
        val existing = this.get(id)
        if (existing != null) {
            return existing
        }

        val holder = GoogleMapViewHolderImpl.create(
            context = context,
            options = options,
        )
        this.set(id, holder)
        return holder
    }
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }