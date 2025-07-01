package com.mapconductor.core.marker

interface MarkerEntity<ActualMarker> {
    var marker: ActualMarker
    val state: MarkerState
    val fingerPrint: MarkerFingerPrint
}

class MarkerEntityImpl<ActualMarker>(
    override var marker: ActualMarker,
    override val state: MarkerState,
) : MarkerEntity<ActualMarker> {
    override val fingerPrint: MarkerFingerPrint = state.fingerPrint()
}
