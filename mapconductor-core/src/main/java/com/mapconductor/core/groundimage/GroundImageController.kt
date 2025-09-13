package com.mapconductor.core.groundimage

import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapCameraPosition
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

abstract class GroundImageController<ActualGroundImage>(
    val groundImageManager: GroundImageManager<ActualGroundImage>,
    open val renderer: GroundImageOverlayRenderer<ActualGroundImage>,
    override var clickListener: OnGroundImageEventHandler? = null,
) : OverlayController<
        GroundImageState,
        GroundImageEntity<ActualGroundImage>,
        GroundImageEvent,
    > {
    override val zIndex: Int = 2
    val semaphore = Semaphore(1)

    override suspend fun add(data: List<GroundImageState>) {
        semaphore.withPermit {
            val modifiedEntities = mutableListOf<GroundImageEntity<ActualGroundImage>>()
            val previous = groundImageManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<GroundImageOverlayRenderer.AddParams>()
            val updated = mutableListOf<GroundImageOverlayRenderer.ChangeParams<ActualGroundImage>>()
            val removed = mutableListOf<GroundImageEntity<ActualGroundImage>>()

            data.forEach { state ->
                if (previous.contains(state.id)) {
                    val prevEntity = groundImageManager.getEntity(state.id)!!
                    updated.add(
                        object : GroundImageOverlayRenderer.ChangeParams<ActualGroundImage> {
                            override val current: GroundImageEntity<ActualGroundImage> =
                                GroundImageEntityImpl(
                                    groundImage = prevEntity.groundImage,
                                    state = state,
                                )
                            override val prev: GroundImageEntity<ActualGroundImage> = prevEntity
                        },
                    )
                    previous.remove(state.id)
                } else {
                    added.add(
                        object : GroundImageOverlayRenderer.AddParams {
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
                            GroundImageEntityImpl<ActualGroundImage>(
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
                            GroundImageEntityImpl<ActualGroundImage>(
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
                GroundImageEntityImpl(
                    groundImage = groundImage,
                    state = state,
                )
            val groundImageParams =
                object : GroundImageOverlayRenderer.ChangeParams<ActualGroundImage> {
                    override val current: GroundImageEntity<ActualGroundImage> = entity
                    override val prev: GroundImageEntity<ActualGroundImage> = prevEntity
                }
            val groundImages = renderer.onChange(listOf(groundImageParams))

            groundImages[0]?.let {
                val entity =
                    GroundImageEntityImpl<ActualGroundImage>(
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
            val entities: List<GroundImageEntity<ActualGroundImage>> = groundImageManager.allEntities()
            renderer.onRemove(entities)
            renderer.onPostProcess()
            groundImageManager.clear()
        }
    }

    override fun find(position: IGeoPoint): GroundImageEntity<ActualGroundImage>? = groundImageManager.find(position)

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {}

    override fun destroy() {
        // No native resources to clean up
    }
}
