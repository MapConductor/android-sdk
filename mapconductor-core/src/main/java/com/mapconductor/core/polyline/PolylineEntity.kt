package com.mapconductor.core.polyline

interface PolylineEntity<ActualPolyline> {
    val polyline: ActualPolyline
    val state: PolylineState
    val stateHashCode: Int
}
data class PolylineEntityImpl<ActualPolyline>(
    override val polyline: ActualPolyline,
    override val state: PolylineState,
) : PolylineEntity<ActualPolyline> {
    override val stateHashCode: Int = state.hashCode()
}
