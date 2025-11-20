package com.mapconductor.core.marker

interface MarkerCapable {
    suspend fun compositionMarkers(data: List<MarkerState>)

    suspend fun updateMarker(state: MarkerState)

    fun setOnMarkerDragStart(listener: OnMarkerEventHandler?)

    fun setOnMarkerDrag(listener: OnMarkerEventHandler?)

    fun setOnMarkerDragEnd(listener: OnMarkerEventHandler?)

    fun setOnMarkerAnimateStart(listener: OnMarkerEventHandler?)

    fun setOnMarkerAnimateEnd(listener: OnMarkerEventHandler?)

    fun setOnMarkerClickListener(listener: OnMarkerEventHandler?)

    fun hasMarker(state: MarkerState): Boolean
}
