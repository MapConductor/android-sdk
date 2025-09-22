package com.mapconductor.core.controller

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPositionImpl

interface OverlayController<StateType, EntityType, EventType> {
    val zIndex: Int

    suspend fun add(data: List<StateType>)

    suspend fun update(state: StateType)

    suspend fun clear()

    var clickListener: ((EventType) -> Unit)?

    fun find(position: GeoPoint): EntityType?

    suspend fun onCameraChanged(mapCameraPosition: MapCameraPositionImpl)

    /**
     * Cleanup resources when the controller is no longer needed.
     * IMPORTANT: Call this when switching map providers or disposing the map.
     */
    fun destroy()
}
