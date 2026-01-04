package com.mapconductor.googlemaps

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.mapconductor.core.map.MapViewHolderInterface
import com.mapconductor.core.map.StaticHolder
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

typealias GoogleMapViewHolderInterface = MapViewHolderInterface<MapView, GoogleMap>

object GoogleMapViewControllerStore : StaticHolder<GoogleMapViewController>()

internal fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
