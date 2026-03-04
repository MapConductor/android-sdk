package com.mapconductor.core.polygon

interface PolygonEntityInterface<ActualPolygon> {
    val polygon: ActualPolygon
    val state: PolygonState
    val fingerPrint: PolygonFingerPrint
}

data class PolygonEntity<ActualPolygon>(
    override val polygon: ActualPolygon,
    override val state: PolygonState,
) : PolygonEntityInterface<ActualPolygon> {
    override val fingerPrint: PolygonFingerPrint = state.fingerPrint()
}
