package com.mapconductor.core.polyline

import com.mapconductor.core.map.MapViewHolderInterface
import kotlinx.coroutines.CoroutineScope

abstract class AbstractPolylineOverlayRenderer<ActualPolyline> : PolylineOverlayRendererInterface<ActualPolyline> {
    abstract val holder: MapViewHolderInterface<*, *>
    abstract val coroutine: CoroutineScope

    override suspend fun onPostProcess() {
        // Default implementation - can be overridden by subclasses
    }

    abstract suspend fun createPolyline(state: PolylineState): ActualPolyline?

    abstract suspend fun updatePolylineProperties(
        polyline: ActualPolyline,
        current: PolylineEntityInterface<ActualPolyline>,
        prev: PolylineEntityInterface<ActualPolyline>,
    ): ActualPolyline?

    abstract suspend fun removePolyline(entity: PolylineEntityInterface<ActualPolyline>)

    override suspend fun onAdd(data: List<PolylineOverlayRendererInterface.AddParamsInterface>): List<ActualPolyline?> =
        data.map { params ->
            createPolyline(params.state)
        }

    override suspend fun onChange(
        data: List<PolylineOverlayRendererInterface.ChangeParamsInterface<ActualPolyline>>,
    ): List<ActualPolyline?> =
        data.map { params ->
            updatePolylineProperties(
                polyline = params.prev.polyline,
                current = params.current,
                prev = params.prev,
            )
        }

    override suspend fun onRemove(data: List<PolylineEntityInterface<ActualPolyline>>) {
        data.forEach { entity ->
            removePolyline(entity)
        }
    }
}
