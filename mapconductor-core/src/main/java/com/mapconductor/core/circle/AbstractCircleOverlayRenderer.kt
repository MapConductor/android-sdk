package com.mapconductor.core.circle

import com.mapconductor.core.map.MapViewHolder
import kotlinx.coroutines.CoroutineScope

abstract class AbstractCircleOverlayRenderer<ActualCircle> : CircleOverlayRenderer<ActualCircle> {
    abstract val holder: MapViewHolder<*, *>
    abstract val coroutine: CoroutineScope

    override suspend fun onPostProcess() {
        // Default implementation - can be overridden by subclasses
    }

    abstract suspend fun removeCircle(entity: CircleEntity<ActualCircle>)

    abstract suspend fun createCircle(state: CircleState): ActualCircle?

    abstract suspend fun updateCircleProperties(
        circle: ActualCircle,
        current: CircleEntity<ActualCircle>,
        prev: CircleEntity<ActualCircle>,
    ): ActualCircle?

    override suspend fun onAdd(data: List<CircleOverlayRenderer.AddParams>): List<ActualCircle?> =
        data.map { params -> createCircle(params.state) }

    override suspend fun onChange(data: List<CircleOverlayRenderer.ChangeParams<ActualCircle>>): List<ActualCircle?> =
        data.map { params ->
            updateCircleProperties(
                circle = params.prev.circle,
                current = params.current,
                prev = params.prev,
            )
        }

    override suspend fun onRemove(data: List<CircleEntity<ActualCircle>>) {
        data.forEach { entity ->
            removeCircle(entity)
        }
    }
}
