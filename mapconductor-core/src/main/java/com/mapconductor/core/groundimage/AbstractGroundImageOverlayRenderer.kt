package com.mapconductor.core.groundimage

import com.mapconductor.core.map.MapViewHolderInterface
import kotlinx.coroutines.CoroutineScope

abstract class AbstractGroundImageOverlayRenderer<ActualGroundImage> :
    GroundImageOverlayRendererInterface<ActualGroundImage> {
    abstract val holder: MapViewHolderInterface<*, *>
    abstract val coroutine: CoroutineScope

    override suspend fun onPostProcess() {
        // Default implementation - can be overridden by subclasses
    }

    abstract suspend fun createGroundImage(state: GroundImageState): ActualGroundImage?

    abstract suspend fun updateGroundImageProperties(
        groundImage: ActualGroundImage,
        current: GroundImageEntityInterface<ActualGroundImage>,
        prev: GroundImageEntityInterface<ActualGroundImage>,
    ): ActualGroundImage?

    abstract suspend fun removeGroundImage(entity: GroundImageEntityInterface<ActualGroundImage>)

    override suspend fun onAdd(
        data: List<GroundImageOverlayRendererInterface.AddParamsInterface>,
    ): List<ActualGroundImage?> =
        data.map { params ->
            createGroundImage(params.state)
        }

    override suspend fun onChange(
        data: List<GroundImageOverlayRendererInterface.ChangeParamsInterface<ActualGroundImage>>,
    ): List<ActualGroundImage?> =
        data.map { params ->
            updateGroundImageProperties(
                groundImage = params.prev.groundImage,
                current = params.current,
                prev = params.prev,
            )
        }

    override suspend fun onRemove(data: List<GroundImageEntityInterface<ActualGroundImage>>) {
        data.forEach { entity ->
            removeGroundImage(entity)
        }
    }
}
