package com.mapconductor.core

import com.mapconductor.core.controller.MapViewController
import kotlinx.coroutines.flow.StateFlow

interface MapOverlay<T> {
    val flow: StateFlow<List<T>>
    suspend fun render(data: List<T>, controller: MapViewController)
}

class MapOverlayRegistry {
    private val overlays = mutableListOf<MapOverlay<*>>()

    fun register(overlay: MapOverlay<*>) {
        if (overlays.toSet().contains(overlay)) return
        overlays.add(overlay)
    }

    fun getAll(): List<MapOverlay<*>> = overlays
}