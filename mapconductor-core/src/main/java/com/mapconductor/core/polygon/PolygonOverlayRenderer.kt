package com.mapconductor.core.polygon

interface PolygonOverlayRendererInterface<ActualPolygon> {
    interface AddParamsInterface {
        val state: PolygonState
    }

    interface ChangeParamsInterface<ActualPolygon> {
        val current: PolygonEntityInterface<ActualPolygon>
        val prev: PolygonEntityInterface<ActualPolygon>
    }

    suspend fun onAdd(data: List<AddParamsInterface>): List<ActualPolygon?>

    suspend fun onChange(data: List<ChangeParamsInterface<ActualPolygon>>): List<ActualPolygon?>

    suspend fun onRemove(data: List<PolygonEntityInterface<ActualPolygon>>)

    suspend fun onPostProcess()
}
