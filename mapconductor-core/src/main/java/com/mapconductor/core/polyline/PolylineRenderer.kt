package com.mapconductor.core.polyline

import com.mapconductor.core.map.MapViewHolder
import kotlinx.coroutines.CoroutineScope

interface PolylineRendererFactory<ActualPolyline> {

    fun create(
        onAdd: suspend (List<PolylineState>) -> List<ActualPolyline?>,
        onChange: suspend (List<PolylineRenderer.UpdateParams<ActualPolyline>>) -> List<ActualPolyline?>,
        onRemove: suspend (List<PolylineEntity<ActualPolyline>>) -> Unit,
        onPostProcess: (suspend () -> Unit)? = null,
    ): PolylineOverlayManager<ActualPolyline>
}

interface PolylineRenderer<ActualPolyline> {

    interface UpdateParams<ActualPolyline> {
        val entity: PolylineEntity<ActualPolyline>
        val prevEntity: PolylineEntity<ActualPolyline>
    }

    fun init(polylineOverlayManager: PolylineOverlayManager<ActualPolyline>)

    suspend fun addLines(newLines: List<PolylineState>) : List<ActualPolyline?>

    suspend fun removeLines(removeEntities: List<PolylineEntity<ActualPolyline>>)

    suspend fun changeLine(changes: List<UpdateParams<ActualPolyline>>) : List<ActualPolyline>
}

abstract class AbstractPolylineRenderer<ActualPolyline> : PolylineRenderer<ActualPolyline> {

    protected lateinit var polylineOverlayManager: PolylineOverlayManager<ActualPolyline>
    abstract val holder: MapViewHolder<*, *>
    abstract val coroutine: CoroutineScope

    override fun init(polylineOverlayManager: PolylineOverlayManager<ActualPolyline>) {
        this.polylineOverlayManager = polylineOverlayManager
    }
}
