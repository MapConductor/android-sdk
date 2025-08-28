package com.mapconductor.here.polygon
import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.controller.OverlayRenderer
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.polygon.OnPolygonEventHandler
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonEntityImpl
import com.mapconductor.core.polygon.PolygonEvent
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.here.HereActualPolygon
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class HerePolygonController(
    val renderer: OverlayRenderer<HereActualPolygon, PolygonState, PolygonEntity<HereActualPolygon>>,
    override var clickListener: OnPolygonEventHandler? = null,
) : OverlayController<
        HereActualPolygon,
        PolygonState,
        PolygonEntity<HereActualPolygon>,
        PolygonEvent,
    > {
    override val zIndex: Int = 3
    val entities = mutableMapOf<String, PolygonEntity<HereActualPolygon>>()
    val semaphore = Semaphore(1)

    override suspend fun add(data: List<PolygonState>) {
        semaphore.withPermit {
            val previous = entities.keys.toMutableSet()
            val added = mutableListOf<PolygonState>()
            val updated = mutableListOf<OverlayRenderer.ChangeParams<PolygonEntity<HereActualPolygon>>>()
            val removed = mutableListOf<PolygonEntity<HereActualPolygon>>()

            data.forEach {
                if (previous.contains(it.id)) {
                    val prevEntity = entities[it.id]!!
                    updated.add(
                        object : OverlayRenderer.ChangeParams<PolygonEntity<HereActualPolygon>> {
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
                            PolygonEntityImpl<HereActualPolygon>(
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
                            PolygonEntityImpl<HereActualPolygon>(
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
            val updated = mutableListOf<OverlayRenderer.ChangeParams<PolygonEntity<HereActualPolygon>>>()
            val prevEntity = entities[state.id]!!
            updated.add(
                object : OverlayRenderer.ChangeParams<PolygonEntity<HereActualPolygon>> {
                    override val current: PolygonEntity<HereActualPolygon> =
                        PolygonEntityImpl(
                            polygon = prevEntity.polygon,
                            state = state,
                        )
                    override val prev: PolygonEntity<HereActualPolygon> = prevEntity
                },
            )

            val actualOverlays: List<HereActualPolygon?> = renderer.onChange(updated)
            actualOverlays.forEachIndexed { index, actualOverlay ->
                actualOverlay?.let {
                    val entity =
                        PolygonEntityImpl<HereActualPolygon>(
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

    override fun find(position: IGeoPoint): PolygonEntity<HereActualPolygon>? {
        // TODO: Improve this implementation later with proper point-in-polygon check
        return entities.values.find { entity ->
            entity.state.points.isNotEmpty()
        }
    }
}
