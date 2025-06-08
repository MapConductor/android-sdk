package com.mapconductor.googlemaps

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.StaticHolder
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

typealias GoogleMapViewHolder = MapViewHolder<MapView, GoogleMap>

object GoogleMapViewControllerStore : StaticHolder<GoogleMapViewController>() {
    suspend fun getOrCreate(
        context: Context,
        id: String,
        options: GoogleMapOptions,
    ): GoogleMapViewController {
        val existing = this.get(id)
        if (existing != null) {
            return existing
        }

        val holder =
            GoogleMapViewHolderImpl.create(
                context = context,
                options = options,
            )

        val controller =
            GoogleMapViewController(
                holder = holder,
            )
        this.set(id, controller)

        return controller
    }
}

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
