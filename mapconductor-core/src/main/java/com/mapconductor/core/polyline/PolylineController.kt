package com.mapconductor.core.polyline

import com.mapconductor.core.controller.OverlayControllerInterface
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

abstract class PolylineController<ActualPolyline>(
    val polylineManager: PolylineManagerInterface<ActualPolyline>,
    open val renderer: PolylineOverlayRendererInterface<ActualPolyline>,
    override var clickListener: OnPolylineEventHandler? = null,
) : OverlayControllerInterface<
        PolylineState,
        PolylineEntityInterface<ActualPolyline>,
        PolylineEvent,
    > {
    override val zIndex: Int = 5
    val semaphore = Semaphore(1)
    private var currentCameraPosition: MapCameraPosition? = null

    fun dispatchClick(event: PolylineEvent) {
        event.state.onClick?.invoke(event)
        clickListener?.invoke(event)
    }

    override suspend fun add(data: List<PolylineState>) {
        semaphore.withPermit {
            val modifiedEntities = mutableListOf<PolylineEntityInterface<ActualPolyline>>()
            val previous = polylineManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<PolylineOverlayRendererInterface.AddParamsInterface>()
            val updated = mutableListOf<PolylineOverlayRendererInterface.ChangeParamsInterface<ActualPolyline>>()
            val removed = mutableListOf<PolylineEntityInterface<ActualPolyline>>()

            data.forEach { state ->
                if (previous.contains(state.id)) {
                    val prevEntity = polylineManager.getEntity(state.id)!!
                    updated.add(
                        object : PolylineOverlayRendererInterface.ChangeParamsInterface<ActualPolyline> {
                            override val current: PolylineEntityInterface<ActualPolyline> =
                                PolylineEntity(
                                    state = state,
                                    polyline = prevEntity.polyline,
                                )
                            override val prev: PolylineEntityInterface<ActualPolyline> = prevEntity
                        },
                    )
                    previous.remove(state.id)
                } else {
                    added.add(
                        object : PolylineOverlayRendererInterface.AddParamsInterface {
                            override val state: PolylineState = state
                        },
                    )
                    previous.remove(state.id)
                }
            }

            previous.forEach { remainId ->
                polylineManager.removeEntity(remainId)?.let { removedEntity ->
                    removed.add(removedEntity)
                }
            }

            // Remove polylines
            if (removed.isNotEmpty()) {
                renderer.onRemove(removed)
            }

            // Add new polylines
            if (added.isNotEmpty()) {
                val actualPolylines: List<ActualPolyline?> = renderer.onAdd(added)
                actualPolylines.forEachIndexed { index, polyline ->
                    polyline?.let {
                        val entity =
                            PolylineEntity<ActualPolyline>(
                                polyline = polyline,
                                state = added[index].state,
                            )
                        polylineManager.registerEntity(entity)
                        modifiedEntities.add(entity)
                    }
                }
            }

            // Update changed polylines
            if (updated.isNotEmpty()) {
                val actualPolylines: List<ActualPolyline?> = renderer.onChange(updated)

                actualPolylines.forEachIndexed { index, polyline ->
                    polyline?.let {
                        val params = updated[index]
                        val entity =
                            PolylineEntity<ActualPolyline>(
                                state = params.current.state,
                                polyline = polyline,
                            )
                        polylineManager.registerEntity(entity)
                    }
                }
            }

            renderer.onPostProcess()
        }
    }

    override suspend fun update(state: PolylineState) {
        semaphore.withPermit {
            val prevEntity = polylineManager.getEntity(state.id) ?: return
            val currentFinger = state.fingerPrint()
            val prevFinger = prevEntity.fingerPrint
            if (currentFinger == prevFinger) {
                return
            }

            val polyline = prevEntity.polyline
            val entity =
                PolylineEntity(
                    polyline = polyline,
                    state = state,
                )
            val polylineParams =
                object : PolylineOverlayRendererInterface.ChangeParamsInterface<ActualPolyline> {
                    override val current: PolylineEntityInterface<ActualPolyline> = entity
                    override val prev: PolylineEntityInterface<ActualPolyline> = prevEntity
                }
            val polylines = renderer.onChange(listOf(polylineParams))

            polylines[0]?.let {
                val entity =
                    PolylineEntity<ActualPolyline>(
                        polyline = it,
                        state = state,
                    )
                polylineManager.registerEntity(entity)
            }
            renderer.onPostProcess()
        }
    }

    override suspend fun clear() {
        semaphore.withPermit {
            val entities: List<PolylineEntityInterface<ActualPolyline>> = polylineManager.allEntities()
            renderer.onRemove(entities)
            renderer.onPostProcess()
            polylineManager.clear()
        }
    }

    override fun find(position: GeoPointInterface): PolylineEntityInterface<ActualPolyline>? =
        polylineManager.find(position, currentCameraPosition)?.entity

    fun findWithClosestPoint(position: GeoPointInterface): PolylineHitResult<ActualPolyline>? =
        polylineManager.find(position, currentCameraPosition)

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        currentCameraPosition = mapCameraPosition
    }

    override fun destroy() {
        // No native resources to clean up for polylines
    }
}
