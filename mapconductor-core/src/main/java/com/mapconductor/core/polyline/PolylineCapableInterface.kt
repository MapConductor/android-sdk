package com.mapconductor.core.polyline

interface PolylineCapableInterface {
    suspend fun compositionPolylines(data: List<PolylineState>)

    suspend fun updatePolyline(state: PolylineState)

    @Deprecated("Use PolylineState.onClick instead.")
    fun setOnPolylineClickListener(listener: OnPolylineEventHandler?)

    fun hasPolyline(state: PolylineState): Boolean
}
