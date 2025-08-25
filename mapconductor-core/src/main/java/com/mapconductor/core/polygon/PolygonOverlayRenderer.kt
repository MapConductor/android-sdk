package com.mapconductor.core.polygon

interface PolygonOverlayRenderer<ActualPolygon> {
    interface AddParams {
        val state: PolygonState
    }
    interface ChangeParams<ActualPolygon> {
        val current: PolygonEntity<ActualPolygon>
        val prev: PolygonEntity<ActualPolygon>
    }

    suspend fun onAdd(data: List<AddParams>): List<ActualPolygon>
    suspend fun onChange(data: List<ChangeParams<ActualPolygon>>): List<ActualPolygon?>
    suspend fun onRemove(data: PolygonEntity<ActualPolygon>)
    suspend fun onPostProcess()
}
