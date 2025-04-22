package com.mapconductor.core

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner

interface MapViewHolder<MapViewType, MapType> {
    val mapView: MapViewType
    val map: MapType

    fun attachTo(container: ViewGroup)

    fun detach()

    fun destroy(owner: LifecycleOwner? = null)
}