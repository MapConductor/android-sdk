package com.mapconductor.core.polyline

import com.mapconductor.core.map.MapViewHolder
import kotlinx.coroutines.CoroutineScope

abstract class AbstractPolylineOverlayRenderer<ActualPolyline> : PolylineOverlayRenderer<ActualPolyline> {
    abstract val holder: MapViewHolder<*, *>
    abstract val coroutine: CoroutineScope

    override suspend fun onPostProcess() {
        // Default implementation - can be overridden by subclasses
    }

    abstract suspend fun createPolyline(state: PolylineState): ActualPolyline?

    abstract suspend fun updatePolylineProperties(
        polyline: ActualPolyline,
        current: PolylineEntity<ActualPolyline>,
        prev: PolylineEntity<ActualPolyline>,
    ): ActualPolyline?

    abstract suspend fun removePolyline(entity: PolylineEntity<ActualPolyline>)

    override suspend fun onAdd(data: List<PolylineOverlayRenderer.AddParams>): List<ActualPolyline?> =
        data.map { params ->
            createPolyline(params.state)
        }

    override suspend fun onChange(
        data: List<PolylineOverlayRenderer.ChangeParams<ActualPolyline>>,
    ): List<ActualPolyline?> =
        data.map { params ->
            updatePolylineProperties(
                polyline = params.prev.polyline,
                current = params.current,
                prev = params.prev,
            )
        }

    override suspend fun onRemove(data: List<PolylineEntity<ActualPolyline>>) {
        data.forEach { entity ->
            removePolyline(entity)
        }
    }
}
