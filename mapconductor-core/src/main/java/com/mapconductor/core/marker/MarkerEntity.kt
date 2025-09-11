package com.mapconductor.core.marker

interface MarkerEntity<ActualMarker> {
    var marker: ActualMarker?
    val state: MarkerState
    val fingerPrint: MarkerFingerPrint
    var visible: Boolean
    var isRendered: Boolean
}

class MarkerEntityImpl<ActualMarker>(
    override var marker: ActualMarker?,
    override val state: MarkerState,
    override var visible: Boolean = true,
    override var isRendered: Boolean = false,
) : MarkerEntity<ActualMarker> {
    override val fingerPrint: MarkerFingerPrint = state.fingerPrint()
}
