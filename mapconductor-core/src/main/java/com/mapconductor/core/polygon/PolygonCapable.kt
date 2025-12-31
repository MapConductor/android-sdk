package com.mapconductor.core.polygon

interface PolygonCapable {
    suspend fun compositionPolygons(data: List<PolygonState>)

    suspend fun updatePolygon(state: PolygonState)

    @Deprecated("Use PolygonState.onClick instead.")
    fun setOnPolygonClickListener(listener: OnPolygonEventHandler?)

    fun hasPolygon(state: PolygonState): Boolean
}
