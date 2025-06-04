package com.mapconductor.core.map

interface MapViewHolder<out TMapView, out TMap> {
    val mapView: TMapView
    val map: TMap
}
