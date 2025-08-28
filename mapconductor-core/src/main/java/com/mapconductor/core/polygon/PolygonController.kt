package com.mapconductor.core.polygon

import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.features.IGeoPoint
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

abstract class PolygonController<ActualPolygon>(
    val polygonManager: PolygonManager<ActualPolygon>,
    open val renderer: PolygonOverlayRenderer<ActualPolygon>,
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
            val modifiedEntities = mutableListOf<PolygonEntity<ActualPolygon>>()
            val previous = polygonManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<PolygonOverlayRenderer.AddParams>()
            val updated = mutableListOf<PolygonOverlayRenderer.ChangeParams<ActualPolygon>>()
            val removed = mutableListOf<PolygonEntity<ActualPolygon>>()

            data.forEach { state ->
                if (previous.contains(state.id)) {
                    val prevEntity = polygonManager.getEntity(state.id)!!
                    updated.add(
                        object : PolygonOverlayRenderer.ChangeParams<ActualPolygon> {
                            override val current: PolygonEntity<ActualPolygon> =
                                PolygonEntityImpl(
                                    state = state,
                                    polygon = prevEntity.polygon,
                                )
                            override val prev: PolygonEntity<ActualPolygon> = prevEntity
                        },
                    )
                    previous.remove(state.id)
                } else {
                    added.add(
                        object : PolygonOverlayRenderer.AddParams {
                            override val state: PolygonState = state
                        }
                    )
                    previous.remove(state.id)
                }
            }

            previous.forEach { remainId ->
                polygonManager.removeEntity(remainId)?.let { removedEntity ->
                    removed.add(removedEntity)
                }
            }

            // Remove polygon
            if (removed.isNotEmpty()) {
                renderer.onRemove(removed)
            }

            // Add new polygons
            if (added.isNotEmpty()) {
                val actualPolygons: List<ActualPolygon?> = renderer.onAdd(added)
                actualPolygons.forEachIndexed { index, polygon ->
                    polygon?.let {
                        val entity =
                            PolygonEntityImpl<ActualPolygon>(
                                polygon = polygon,
                                state = added[index].state,
                            )
                        polygonManager.registerEntity(entity)
                        modifiedEntities.add(entity)
                    }
                }
            }

            // Update changed polygons
            if (updated.isNotEmpty()) {
                val actualPolygons: List<ActualPolygon?> = renderer.onChange(updated)
                actualPolygons.forEachIndexed { index, polygon ->
                    polygon?.let {
                        val params = updated[index]
                        val entity =
                            PolygonEntityImpl<ActualPolygon>(
                                state = params.current.state,
                                polygon = polygon,
                            )
                        polygonManager.registerEntity(entity)
                    }
                }
            }

            renderer.onPostProcess()
        }
    }

    override suspend fun update(state: PolygonState) {
        semaphore.withPermit {
            val prevEntity = polygonManager.getEntity(state.id) ?: return
            val currentFinger = state.fingerPrint()
            val prevFinger = prevEntity.fingerPrint
            if (currentFinger == prevFinger) {
                return
            }

            val polygon = prevEntity.polygon
            val entity =
                PolygonEntityImpl(
                    polygon = polygon,
                    state = state,
                )
            val polygonParams =
                object : PolygonOverlayRenderer.ChangeParams<ActualPolygon> {
                    override val current: PolygonEntity<ActualPolygon> = entity
                    override val prev: PolygonEntity<ActualPolygon> = prevEntity
                }
            val polygons = renderer.onChange(listOf(polygonParams))

            polygons[0]?.let {
                val entity =
                    PolygonEntityImpl<ActualPolygon>(
                        polygon = it,
                        state = state,
                    )
                polygonManager.registerEntity(entity)
            }
        }
    }

    override suspend fun clear() {
        semaphore.withPermit {
            val entities: List<PolygonEntity<ActualPolygon>> = polygonManager.allEntities()
            renderer.onRemove(entities)
            polygonManager.clear()
        }
    }

    override fun find(position: IGeoPoint): PolygonEntity<ActualPolygon>? = polygonManager.find(position)
}
