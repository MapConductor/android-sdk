package com.mapconductor.core.polyline

interface PolylineOverlayRenderer<ActualPolyline> {
    interface AddParams {
        val state: PolylineState
    }

    interface ChangeParams<ActualPolyline> {
        val current: PolylineEntity<ActualPolyline>
        val prev: PolylineEntity<ActualPolyline>
    }

    suspend fun onAdd(data: List<AddParams>): List<ActualPolyline?>

    suspend fun onChange(data: List<ChangeParams<ActualPolyline>>): List<ActualPolyline?>

    suspend fun onRemove(data: List<PolylineEntity<ActualPolyline>>)

    suspend fun onPostProcess()
}
