package com.mapconductor.core.groundimage

import com.mapconductor.core.map.MapViewHolder
import kotlinx.coroutines.CoroutineScope

abstract class AbstractGroundImageOverlayRenderer<ActualGroundImage> : GroundImageOverlayRenderer<ActualGroundImage> {
    abstract val holder: MapViewHolder<*, *>
    abstract val coroutine: CoroutineScope

    override suspend fun onPostProcess() {
        // Default implementation - can be overridden by subclasses
    }

    abstract suspend fun createGroundImage(state: GroundImageState): ActualGroundImage?

    abstract suspend fun updateGroundImageProperties(
        groundImage: ActualGroundImage,
        current: GroundImageEntity<ActualGroundImage>,
        prev: GroundImageEntity<ActualGroundImage>,
    ): ActualGroundImage?

    abstract suspend fun removeGroundImage(entity: GroundImageEntity<ActualGroundImage>)

    override suspend fun onAdd(data: List<GroundImageOverlayRenderer.AddParams>): List<ActualGroundImage?> =
        data.map { params ->
            createGroundImage(params.state)
        }

    override suspend fun onChange(
        data: List<GroundImageOverlayRenderer.ChangeParams<ActualGroundImage>>,
    ): List<ActualGroundImage?> =
        data.map { params ->
            updateGroundImageProperties(
                groundImage = params.prev.groundImage,
                current = params.current,
                prev = params.prev,
            )
        }

    override suspend fun onRemove(data: List<GroundImageEntity<ActualGroundImage>>) {
        data.forEach { entity ->
            removeGroundImage(entity)
        }
    }
}
