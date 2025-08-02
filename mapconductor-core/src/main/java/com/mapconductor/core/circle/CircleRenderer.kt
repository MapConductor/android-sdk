package com.mapconductor.core.circle

import com.mapconductor.core.map.MapViewHolder
import kotlinx.coroutines.CoroutineScope

interface CircleRendererFactory<ActualCircle> {
    fun create(
        onAdd: suspend (List<CircleState>) -> List<ActualCircle?>,
        onChange: suspend (List<CircleRenderer.UpdateParams<ActualCircle>>) -> List<ActualCircle?>,
        onRemove: suspend (List<CircleEntity<ActualCircle>>) -> Unit,
        onPostProcess: (suspend () -> Unit)? = null,
    ): CircleOverlayManager<ActualCircle>
}

interface CircleRenderer<ActualCircle> {
    interface UpdateParams<ActualCircle> {
        val entity: CircleEntity<ActualCircle>
        val prevEntity: CircleEntity<ActualCircle>
    }

    fun init(circleOverlayManager: CircleOverlayManager<ActualCircle>)

    suspend fun addCircles(newCircles: List<CircleState>): List<ActualCircle?>

    suspend fun removeCircles(removeEntities: List<CircleEntity<ActualCircle>>)

    suspend fun changeCircle(changes: List<UpdateParams<ActualCircle>>): List<ActualCircle>
}

abstract class AbstractCircleRenderer<ActualCircle> : CircleRenderer<ActualCircle> {
    protected lateinit var circleOverlayManager: CircleOverlayManager<ActualCircle>
    abstract val holder: MapViewHolder<*, *>
    abstract val coroutine: CoroutineScope

    override fun init(circleOverlayManager: CircleOverlayManager<ActualCircle>) {
        this.circleOverlayManager = circleOverlayManager
    }
}
