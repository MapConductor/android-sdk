package com.mapconductor.core.polygon

import com.mapconductor.core.map.MapViewHolder
import kotlinx.coroutines.CoroutineScope

interface PolygonRendererFactory<ActualPolygon> {
    fun create(
        onAdd: suspend (List<PolygonState>) -> List<ActualPolygon?>,
        onChange: suspend (List<PolygonRenderer.UpdateParams<ActualPolygon>>) -> List<ActualPolygon?>,
        onRemove: suspend (List<PolygonEntity<ActualPolygon>>) -> Unit,
        onPostProcess: (suspend () -> Unit)? = null,
    ): PolygonOverlayManager<ActualPolygon>
}

interface PolygonRenderer<ActualPolygon> {
    interface UpdateParams<ActualPolygon> {
        val entity: PolygonEntity<ActualPolygon>
        val prevEntity: PolygonEntity<ActualPolygon>
    }

    fun init(polygonOverlayManager: PolygonOverlayManager<ActualPolygon>)

    suspend fun addPolygons(newPolygons: List<PolygonState>): List<ActualPolygon?>

    suspend fun removePolygons(removeEntities: List<PolygonEntity<ActualPolygon>>)

    suspend fun changePolygon(changes: List<UpdateParams<ActualPolygon>>): List<ActualPolygon>
}

abstract class AbstractPolygonRenderer<ActualPolygon> : PolygonRenderer<ActualPolygon> {
    protected lateinit var polygonOverlayManager: PolygonOverlayManager<ActualPolygon>
    abstract val holder: MapViewHolder<*, *>
    abstract val coroutine: CoroutineScope

    override fun init(polygonOverlayManager: PolygonOverlayManager<ActualPolygon>) {
        this.polygonOverlayManager = polygonOverlayManager
    }
}