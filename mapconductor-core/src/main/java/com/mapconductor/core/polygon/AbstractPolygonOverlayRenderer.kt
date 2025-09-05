package com.mapconductor.core.polygon

import com.mapconductor.core.map.MapViewHolder
import kotlinx.coroutines.CoroutineScope

abstract class AbstractPolygonOverlayRenderer<ActualPolygon> : PolygonOverlayRenderer<ActualPolygon> {
    abstract val holder: MapViewHolder<*, *>
    abstract val coroutine: CoroutineScope

    override suspend fun onPostProcess() {
        // Default implementation - can be overridden by subclasses
    }

    abstract suspend fun removePolygon(entity: PolygonEntity<ActualPolygon>)

    abstract suspend fun createPolygon(state: PolygonState): ActualPolygon?

    abstract suspend fun updatePolygonProperties(
        polygon: ActualPolygon,
        current: PolygonEntity<ActualPolygon>,
        prev: PolygonEntity<ActualPolygon>,
    ): ActualPolygon?

    override suspend fun onAdd(data: List<PolygonOverlayRenderer.AddParams>): List<ActualPolygon?> =
        data.map { params -> createPolygon(params.state) }

    override suspend fun onChange(
        data: List<PolygonOverlayRenderer.ChangeParams<ActualPolygon>>,
    ): List<ActualPolygon?> =
        data.map { params ->
            updatePolygonProperties(
                polygon = params.prev.polygon,
                current = params.current,
                prev = params.prev,
            )
        }

    override suspend fun onRemove(data: List<PolygonEntity<ActualPolygon>>) {
        data.forEach { entity ->
            removePolygon(entity)
        }
    }
}
