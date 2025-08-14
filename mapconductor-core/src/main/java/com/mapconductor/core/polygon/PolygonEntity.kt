package com.mapconductor.core.polygon

interface PolygonEntity<ActualPolygon> {
    val polygon: ActualPolygon
    val state: PolygonState
    val fingerPrint: PolygonFingerPrint
}

data class PolygonEntityImpl<ActualPolygon>(
    override val polygon: ActualPolygon,
    override val state: PolygonState,
) : PolygonEntity<ActualPolygon> {
    override val fingerPrint: PolygonFingerPrint = state.fingerPrint()
}
