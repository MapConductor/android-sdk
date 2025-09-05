package com.mapconductor.core.controller

import com.mapconductor.core.features.IGeoPoint

interface OverlayController<StateType, EntityType, EventType> {
    val zIndex: Int

    suspend fun add(data: List<StateType>)

    suspend fun update(state: StateType)

    suspend fun clear()

    var clickListener: ((EventType) -> Unit)?

    fun find(position: IGeoPoint): EntityType?
}
