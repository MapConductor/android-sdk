package com.mapconductor.core.polygon

import com.mapconductor.core.map.MapViewHolderInterface
import kotlinx.coroutines.CoroutineScope

abstract class AbstractPolygonOverlayRenderer<ActualPolygon> : PolygonOverlayRendererInterface<ActualPolygon> {
    abstract val holder: MapViewHolderInterface<*, *>
    abstract val coroutine: CoroutineScope

    override suspend fun onPostProcess() {
        // Default implementation - can be overridden by subclasses
    }

    abstract suspend fun removePolygon(entity: PolygonEntityInterface<ActualPolygon>)

    abstract suspend fun createPolygon(state: PolygonState): ActualPolygon?

    abstract suspend fun updatePolygonProperties(
        polygon: ActualPolygon,
        current: PolygonEntityInterface<ActualPolygon>,
        prev: PolygonEntityInterface<ActualPolygon>,
    ): ActualPolygon?

    override suspend fun onAdd(data: List<PolygonOverlayRendererInterface.AddParamsInterface>): List<ActualPolygon?> =
        data.map { params -> createPolygon(params.state) }

    override suspend fun onChange(
        data: List<PolygonOverlayRendererInterface.ChangeParamsInterface<ActualPolygon>>,
    ): List<ActualPolygon?> =
        data.map { params ->
            updatePolygonProperties(
                polygon = params.prev.polygon,
                current = params.current,
                prev = params.prev,
            )
        }

    override suspend fun onRemove(data: List<PolygonEntityInterface<ActualPolygon>>) {
        data.forEach { entity ->
            removePolygon(entity)
        }
    }
}
