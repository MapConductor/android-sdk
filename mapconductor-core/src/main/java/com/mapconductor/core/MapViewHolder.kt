package com.mapconductor.core

interface MapViewHolder<out TMapView, out TMap> {
    val mapView: TMapView
    val map: TMap
}