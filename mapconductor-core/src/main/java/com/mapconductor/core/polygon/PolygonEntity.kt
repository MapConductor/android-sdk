package com.mapconductor.core.polygon

interface PolygonEntity<ActualPolygon> {
    val polygon: ActualPolygon
    val state: PolygonState
    val stateHashCode: Int
}

data class PolygonEntityImpl<ActualPolygon>(
    override val polygon: ActualPolygon,
    override val state: PolygonState,
) : PolygonEntity<ActualPolygon> {
    override val stateHashCode: Int = state.hashCode()
}
