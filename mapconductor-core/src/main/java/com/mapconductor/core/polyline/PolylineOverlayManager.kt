package com.mapconductor.core.polyline

interface PolylineOverlayManager {
    suspend fun addPolylines(polylines: List<PolylineState>)

    suspend fun updatePolyline(polyline: PolylineState)

    fun getPolylineState(id: String): PolylineState?
}

class PolylineOverlayManagerImpl<ActualPolyline>: PolylineOverlayManager {
    val polylineEntities = mutableListOf<PolylineEntity<ActualPolyline>>()
    override suspend fun addPolylines(polylines: List<PolylineState>) {
        TODO("Not yet implemented")
    }

    override suspend fun updatePolyline(polyline: PolylineState) {
        TODO("Not yet implemented")
    }

    override fun getPolylineState(id: String): PolylineState? {
        TODO("Not yet implemented")
    }
}
