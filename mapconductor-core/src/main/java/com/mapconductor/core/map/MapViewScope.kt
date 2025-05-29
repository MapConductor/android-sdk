package com.mapconductor.core.map

import com.mapconductor.core.MapOverlayRegistry
import com.mapconductor.core.marker.MarkerEntry
import com.mapconductor.core.marker.MarkerOverlay
import kotlinx.coroutines.flow.MutableStateFlow

open class MapViewScope {
    val markerFlow = MutableStateFlow<List<MarkerEntry>>(emptyList())
    val allMarkerKeys = HashSet<String>()

    fun buildRegistry() : MapOverlayRegistry {
        val registry = MapOverlayRegistry()
        registry.register(MarkerOverlay(markerFlow))
        return registry
    }
}