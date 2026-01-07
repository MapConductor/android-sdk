package com.mapconductor.core.groundimage

import com.mapconductor.core.controller.OverlayControllerInterface
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

abstract class GroundImageController<ActualGroundImage>(
    val groundImageManager: GroundImageManagerInterface<ActualGroundImage>,
    open val renderer: GroundImageOverlayRendererInterface<ActualGroundImage>,
    override var clickListener: OnGroundImageEventHandler? = null,
) : OverlayControllerInterface<
        GroundImageState,
        GroundImageEntityInterface<ActualGroundImage>,
        GroundImageEvent,
    > {
    override val zIndex: Int = 2
    val semaphore = Semaphore(1)

    fun dispatchClick(event: GroundImageEvent) {
        event.state.onClick?.invoke(event)
        clickListener?.invoke(event)
    }

    override suspend fun add(data: List<GroundImageState>) {
        semaphore.withPermit {
            val modifiedEntities = mutableListOf<GroundImageEntityInterface<ActualGroundImage>>()
            val previous = groundImageManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<GroundImageOverlayRendererInterface.AddParamsInterface>()
            val updated = mutableListOf<GroundImageOverlayRendererInterface.ChangeParamsInterface<ActualGroundImage>>()
            val removed = mutableListOf<GroundImageEntityInterface<ActualGroundImage>>()

            data.forEach { state ->
                if (previous.contains(state.id)) {
                    val prevEntity = groundImageManager.getEntity(state.id)!!
                    updated.add(
                        object : GroundImageOverlayRendererInterface.ChangeParamsInterface<ActualGroundImage> {
                            override val current: GroundImageEntityInterface<ActualGroundImage> =
                                GroundImageEntity(
                                    groundImage = prevEntity.groundImage,
                                    state = state,
                                )
                            override val prev: GroundImageEntityInterface<ActualGroundImage> = prevEntity
                        },
                    )
                    previous.remove(state.id)
                } else {
                    added.add(
                        object : GroundImageOverlayRendererInterface.AddParamsInterface {
                            override val state: GroundImageState = state
                        },
                    )
                    previous.remove(state.id)
                }
            }

            previous.forEach { remainId ->
                groundImageManager.removeEntity(remainId)?.let { removedEntity ->
                    removed.add(removedEntity)
                }
            }

            if (removed.isNotEmpty()) {
                renderer.onRemove(removed)
            }

            if (added.isNotEmpty()) {
                val actualOverlays = renderer.onAdd(added)
                actualOverlays.forEachIndexed { index, actualOverlay ->
                    actualOverlay?.let {
                        val entity =
                            GroundImageEntity<ActualGroundImage>(
                                groundImage = it,
                                state = added[index].state,
                            )
                        groundImageManager.registerEntity(entity)
                        modifiedEntities.add(entity)
                    }
                }
            }

            if (updated.isNotEmpty()) {
                val actualOverlays: List<ActualGroundImage?> = renderer.onChange(updated)
                actualOverlays.forEachIndexed { index, actualOverlay ->
                    actualOverlay?.let {
                        val state = updated[index].current.state
                        val entity =
                            GroundImageEntity<ActualGroundImage>(
                                groundImage = it,
                                state = state,
                            )
                        groundImageManager.registerEntity(entity)
                    }
                }
            }

            renderer.onPostProcess()
        }
    }

    override suspend fun update(state: GroundImageState) {
        semaphore.withPermit {
            val prevEntity = groundImageManager.getEntity(state.id) ?: return
            val currentFinger = state.fingerPrint()
            val prevFinder = prevEntity.fingerPrint
            if (currentFinger == prevFinder) {
                return
            }

            val groundImage = prevEntity.groundImage
            val entity =
                GroundImageEntity(
                    groundImage = groundImage,
                    state = state,
                )
            val groundImageParams =
                object : GroundImageOverlayRendererInterface.ChangeParamsInterface<ActualGroundImage> {
                    override val current: GroundImageEntityInterface<ActualGroundImage> = entity
                    override val prev: GroundImageEntityInterface<ActualGroundImage> = prevEntity
                }
            val groundImages = renderer.onChange(listOf(groundImageParams))

            groundImages[0]?.let {
                val entity =
                    GroundImageEntity<ActualGroundImage>(
                        groundImage = it,
                        state = state,
                    )
                groundImageManager.registerEntity(entity)
            }
            renderer.onPostProcess()
        }
    }

    override suspend fun clear() {
        semaphore.withPermit {
            val entities: List<GroundImageEntityInterface<ActualGroundImage>> = groundImageManager.allEntities()
            renderer.onRemove(entities)
            renderer.onPostProcess()
            groundImageManager.clear()
        }
    }

    override fun find(position: GeoPointInterface): GroundImageEntityInterface<ActualGroundImage>? =
        groundImageManager
            .find(position)

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {}

    override fun destroy() {
        // No native resources to clean up
    }
}
