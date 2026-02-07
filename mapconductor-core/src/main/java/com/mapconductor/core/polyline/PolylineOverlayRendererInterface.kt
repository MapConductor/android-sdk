package com.mapconductor.core.polyline

interface PolylineOverlayRendererInterface<ActualPolyline> {
    interface AddParamsInterface {
        val state: PolylineState
    }

    interface ChangeParamsInterface<ActualPolyline> {
        val current: PolylineEntityInterface<ActualPolyline>
        val prev: PolylineEntityInterface<ActualPolyline>
    }

    suspend fun onAdd(data: List<AddParamsInterface>): List<ActualPolyline?>

    suspend fun onChange(data: List<ChangeParamsInterface<ActualPolyline>>): List<ActualPolyline?>

    suspend fun onRemove(data: List<PolylineEntityInterface<ActualPolyline>>)

    suspend fun onPostProcess()
}
