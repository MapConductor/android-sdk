package com.mapconductor.core.controller

import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapCameraPosition

interface OverlayController<StateType, EntityType, EventType> {
    val zIndex: Int

    suspend fun add(data: List<StateType>)

    suspend fun update(state: StateType)

    suspend fun clear()

    var clickListener: ((EventType) -> Unit)?

    fun find(position: IGeoPoint): EntityType?

    suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition)

    /**
     * Cleanup resources when the controller is no longer needed.
     * IMPORTANT: Call this when switching map providers or disposing the map.
     */
    fun destroy()
}
