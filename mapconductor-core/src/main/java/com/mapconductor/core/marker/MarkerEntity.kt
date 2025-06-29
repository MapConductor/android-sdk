package com.mapconductor.core.marker

interface MarkerEntity<ActualMarker> {
    var marker: ActualMarker
    val state: MarkerState
    val stateHashCode: Int
}

class MarkerEntityImpl<ActualMarker>(
    override var marker: ActualMarker,
    override val state: MarkerState,
) : MarkerEntity<ActualMarker> {
    override val stateHashCode: Int = state.hashCode()
}
