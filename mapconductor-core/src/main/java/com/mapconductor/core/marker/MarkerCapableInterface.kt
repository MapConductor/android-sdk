package com.mapconductor.core.marker

interface MarkerCapableInterface {
    suspend fun compositionMarkers(data: List<MarkerState>)

    suspend fun updateMarker(state: MarkerState)

    @Deprecated("Use MarkerState.onDragStart instead.")
    fun setOnMarkerDragStart(listener: OnMarkerEventHandler?)

    @Deprecated("Use MarkerState.onDrag instead.")
    fun setOnMarkerDrag(listener: OnMarkerEventHandler?)

    @Deprecated("Use MarkerState.onDragEnd instead.")
    fun setOnMarkerDragEnd(listener: OnMarkerEventHandler?)

    @Deprecated("Use MarkerState.onAnimateStart instead.")
    fun setOnMarkerAnimateStart(listener: OnMarkerEventHandler?)

    @Deprecated("Use MarkerState.onAnimateEnd instead.")
    fun setOnMarkerAnimateEnd(listener: OnMarkerEventHandler?)

    @Deprecated("Use MarkerState.onClick instead.")
    fun setOnMarkerClickListener(listener: OnMarkerEventHandler?)

    fun hasMarker(state: MarkerState): Boolean
}
