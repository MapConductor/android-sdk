package com.mapconductor.core

import android.view.ViewGroup

interface MapViewHolderImpl<T, M> {
    val mapView: T
    val map: M

    fun attachTo(container: ViewGroup)

    fun detach()

    fun destroy()
}