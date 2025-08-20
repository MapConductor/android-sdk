package com.mapconductor.core.groundimage

import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.controller.OverlayRenderer
import com.mapconductor.core.features.IGeoPoint
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

interface GroundImageCapable {
    suspend fun compositionGroundImages(data: List<GroundImageState>)

    suspend fun updateGroundImage(state: GroundImageState)
}

class GroundImageController<ActualGroundImage>(
    override val renderer: OverlayRenderer<ActualGroundImage, GroundImageState, GroundImageEntity<ActualGroundImage>>,
    override var clickListener: OnGroundImageEventHandler? = null,
) : OverlayController<
        ActualGroundImage,
        GroundImageState,
        GroundImageEntity<ActualGroundImage>,
        GroundImageEvent,
    > {
    override val zIndex: Int = 2
    val entities = mutableMapOf<String, GroundImageEntity<ActualGroundImage>>()
    val semaphore = Semaphore(1)

    override suspend fun add(data: List<GroundImageState>) {
        semaphore.withPermit {
            val previous = entities.keys.toMutableSet()
            val added = mutableListOf<GroundImageState>()
            val updated = mutableListOf<OverlayRenderer.Changes<GroundImageEntity<ActualGroundImage>>>()
            val removed = mutableListOf<GroundImageEntity<ActualGroundImage>>()

            data.forEach {
                if (previous.contains(it.id)) {
                    val prevEntity = entities[it.id]!!
                    updated.add(
                        object : OverlayRenderer.Changes<GroundImageEntity<ActualGroundImage>> {
                            override val current =
                                GroundImageEntityImpl(
                                    groundImage = prevEntity.groundImage,
                                    state = it,
                                )
                            override val prev = prevEntity
                        },
                    )
                    previous.remove(it.id)
                } else {
                    added.add(it)
                    previous.remove(it.id)
                }
            }

            previous.forEach { remainId ->
                entities.remove(remainId)?.let { removedEntity ->
                    removed.add(removedEntity)
                }
            }

            if (added.isNotEmpty()) {
                val actualOverlays = renderer.onAdd(added)
                actualOverlays.forEachIndexed { index, actualOverlay ->
                    actualOverlay?.let {
                        val state = added[index]
                        val entity =
                            GroundImageEntityImpl<ActualGroundImage>(
                                groundImage = it,
                                state = state,
                            )
                        entities[state.id] = entity
                    }
                }
            }

            if (updated.isNotEmpty()) {
                val actualOverlays = renderer.onChange(updated.toList())
                actualOverlays.forEachIndexed { index, actualOverlay ->
                    actualOverlay?.let {
                        val state = updated[index].current.state
                        val entity =
                            GroundImageEntityImpl<ActualGroundImage>(
                                groundImage = it,
                                state = state,
                            )
                        entities[state.id] = entity
                    }
                }
            }

            if (removed.isNotEmpty()) {
                renderer.onRemove(removed)
            }

            renderer.onPostProcess()
        }
    }

    override suspend fun update(state: GroundImageState) {
        semaphore.withPermit {
            val updated = mutableListOf<OverlayRenderer.Changes<GroundImageEntity<ActualGroundImage>>>()
            val prevEntity = entities[state.id]!!
            updated.add(
                object : OverlayRenderer.Changes<GroundImageEntity<ActualGroundImage>> {
                    override val current: GroundImageEntity<ActualGroundImage> =
                        GroundImageEntityImpl(
                            groundImage = prevEntity.groundImage,
                            state = state,
                        )
                    override val prev: GroundImageEntity<ActualGroundImage> = prevEntity
                },
            )

            val actualOverlays: List<ActualGroundImage?> = renderer.onChange(updated)
            actualOverlays.forEachIndexed { index, actualOverlay ->
                actualOverlay?.let {
                    val entity =
                        GroundImageEntityImpl<ActualGroundImage>(
                            groundImage = it,
                            state = state,
                        )
                    entities[state.id] = entity
                }
            }
        }
    }

    override suspend fun clear() {
        semaphore.withPermit {
            renderer.onRemove(entities.values.toList())
            entities.clear()
        }
    }

    override fun find(position: IGeoPoint): GroundImageEntity<ActualGroundImage>? {
        // TODO: Improve this implementation later
        return entities.values.find { entity ->
            entity.state.bounds.contains(position)
        }
    }
}
