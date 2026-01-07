package com.mapconductor.core.circle

import com.mapconductor.core.map.MapViewHolderInterface
import kotlinx.coroutines.CoroutineScope

abstract class AbstractCircleOverlayRenderer<ActualCircle> : CircleOverlayRendererInterface<ActualCircle> {
    abstract val holder: MapViewHolderInterface<*, *>
    abstract val coroutine: CoroutineScope

    override suspend fun onPostProcess() {
        // Default implementation - can be overridden by subclasses
    }

    abstract suspend fun removeCircle(entity: CircleEntityInterface<ActualCircle>)

    abstract suspend fun createCircle(state: CircleState): ActualCircle?

    abstract suspend fun updateCircleProperties(
        circle: ActualCircle,
        current: CircleEntityInterface<ActualCircle>,
        prev: CircleEntityInterface<ActualCircle>,
    ): ActualCircle?

    override suspend fun onAdd(data: List<CircleOverlayRendererInterface.AddParamsInterface>): List<ActualCircle?> =
        data.map { params -> createCircle(params.state) }

    override suspend fun onChange(
        data: List<CircleOverlayRendererInterface.ChangeParamsInterface<ActualCircle>>,
    ): List<ActualCircle?> =
        data.map { params ->
            updateCircleProperties(
                circle = params.prev.circle,
                current = params.current,
                prev = params.prev,
            )
        }

    override suspend fun onRemove(data: List<CircleEntityInterface<ActualCircle>>) {
        data.forEach { entity ->
            removeCircle(entity)
        }
    }
}
