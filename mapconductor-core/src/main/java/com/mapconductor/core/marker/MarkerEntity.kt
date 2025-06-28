package com.mapconductor.core.marker

interface MarkerEntity<ActualMarker> {
    val marker: ActualMarker
    val state: MarkerState
    val stateHashCode: Int
}

data class MarkerEntityImpl<ActualMarker>(
    override val marker: ActualMarker,
    override val state: MarkerState
) : MarkerEntity<ActualMarker> {
    override val stateHashCode: Int = state.hashCode()
}
