package com.mapconductor.core.polygon

import com.mapconductor.core.controller.OverlayControllerInterface
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

abstract class PolygonController<ActualPolygon>(
    val polygonManager: PolygonManagerInterface<ActualPolygon>,
    open val renderer: PolygonOverlayRendererInterface<ActualPolygon>,
    override var clickListener: OnPolygonEventHandler? = null,
) : OverlayControllerInterface<
        PolygonState,
        PolygonEntityInterface<ActualPolygon>,
        PolygonEvent,
    > {
    override val zIndex: Int = 3
    val semaphore = Semaphore(1)

    fun dispatchClick(event: PolygonEvent) {
        event.state.onClick?.invoke(event)
        clickListener?.invoke(event)
    }

    override suspend fun add(data: List<PolygonState>) {
        semaphore.withPermit {
            val modifiedEntities = mutableListOf<PolygonEntityInterface<ActualPolygon>>()
            val previous = polygonManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<PolygonOverlayRendererInterface.AddParamsInterface>()
            val updated = mutableListOf<PolygonOverlayRendererInterface.ChangeParamsInterface<ActualPolygon>>()
            val removed = mutableListOf<PolygonEntityInterface<ActualPolygon>>()

            data.forEach { state ->
                if (previous.contains(state.id)) {
                    val prevEntity = polygonManager.getEntity(state.id)!!
                    updated.add(
                        object : PolygonOverlayRendererInterface.ChangeParamsInterface<ActualPolygon> {
                            override val current: PolygonEntityInterface<ActualPolygon> =
                                PolygonEntity(
                                    state = state,
                                    polygon = prevEntity.polygon,
                                )
                            override val prev: PolygonEntityInterface<ActualPolygon> = prevEntity
                        },
                    )
                    previous.remove(state.id)
                } else {
                    added.add(
                        object : PolygonOverlayRendererInterface.AddParamsInterface {
                            override val state: PolygonState = state
                        },
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
                            PolygonEntity<ActualPolygon>(
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
                            PolygonEntity<ActualPolygon>(
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
                PolygonEntity(
                    polygon = polygon,
                    state = state,
                )
            val polygonParams =
                object : PolygonOverlayRendererInterface.ChangeParamsInterface<ActualPolygon> {
                    override val current: PolygonEntityInterface<ActualPolygon> = entity
                    override val prev: PolygonEntityInterface<ActualPolygon> = prevEntity
                }
            val polygons = renderer.onChange(listOf(polygonParams))

            polygons[0]?.let {
                val entity =
                    PolygonEntity<ActualPolygon>(
                        polygon = it,
                        state = state,
                    )
                polygonManager.registerEntity(entity)
            }
        }
    }

    override suspend fun clear() {
        semaphore.withPermit {
            val entities: List<PolygonEntityInterface<ActualPolygon>> = polygonManager.allEntities()
            renderer.onRemove(entities)
            polygonManager.clear()
        }
    }

    override fun find(position: GeoPointInterface): PolygonEntityInterface<ActualPolygon>? =
        polygonManager
            .find(position)

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {}

    override fun destroy() {
        // No native resources to clean up for polygons
    }
}
