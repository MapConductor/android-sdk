package com.mapconductor.core.polygon

import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.controller.OverlayRenderer
import com.mapconductor.core.features.IGeoPoint
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class PolygonController<ActualPolygon>(
    val polygonManager: PolygonManager<ActualPolygon>,
    val renderer: OverlayRenderer<ActualPolygon, PolygonState, PolygonEntity<ActualPolygon>>,
    override var clickListener: OnPolygonEventHandler? = null,
) : OverlayController<
        ActualPolygon,
        PolygonState,
        PolygonEntity<ActualPolygon>,
        PolygonEvent,
    > {
    override val zIndex: Int = 3
    val semaphore = Semaphore(1)

    override suspend fun add(data: List<PolygonState>) {
        semaphore.withPermit {
            val previous = polygonManager.allEntities().map { it.state.id }
            val added = mutableListOf<PolygonState>()
            val updated = mutableListOf<OverlayRenderer.ChangeParams<PolygonEntity<ActualPolygon>>>()
            val removed = mutableListOf<PolygonEntity<ActualPolygon>>()

            data.forEach {
                if (previous.contains(it.id)) {
                    val prevEntity = entities[it.id]!!
                    updated.add(
                        object : OverlayRenderer.ChangeParams<PolygonEntity<ActualPolygon>> {
                            override val current =
                                PolygonEntityImpl(
                                    polygon = prevEntity.polygon,
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
                            PolygonEntityImpl<ActualPolygon>(
                                polygon = it,
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
                            PolygonEntityImpl<ActualPolygon>(
                                polygon = it,
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

    override suspend fun update(state: PolygonState) {
        semaphore.withPermit {
            val updated = mutableListOf<OverlayRenderer.ChangeParams<PolygonEntity<ActualPolygon>>>()
            val prevEntity = entities[state.id]!!
            updated.add(
                object : OverlayRenderer.ChangeParams<PolygonEntity<ActualPolygon>> {
                    override val current: PolygonEntity<ActualPolygon> =
                        PolygonEntityImpl(
                            polygon = prevEntity.polygon,
                            state = state,
                        )
                    override val prev: PolygonEntity<ActualPolygon> = prevEntity
                },
            )

            val actualOverlays: List<ActualPolygon?> = renderer.onChange(updated)
            actualOverlays.forEachIndexed { index, actualOverlay ->
                actualOverlay?.let {
                    val entity =
                        PolygonEntityImpl<ActualPolygon>(
                            polygon = it,
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

    override fun find(position: IGeoPoint): PolygonEntity<ActualPolygon>? {
        // TODO: Improve this implementation later
        return entities.values.find { entity ->
            // For polygons, we need a point-in-polygon check
            // This is a simplified version - would need proper implementation
            entity.state.points.isNotEmpty()
        }
    }
}
