package com.mapconductor.core.polyline

import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.features.IGeoPoint
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

interface PolylineCapable {
    suspend fun compositionPolylines(data: List<PolylineState>)

    suspend fun updatePolyline(state: PolylineState)

    fun setOnPolylineClickListener(listener: OnPolylineEventHandler?)
}

interface PolylineOverlayRenderer<ActualPolyline> {
    interface AddParams {
        val state: PolylineState
    }

    interface ChangeParams<ActualPolyline> {
        val current: PolylineEntity<ActualPolyline>
        val prev: PolylineEntity<ActualPolyline>
    }

    suspend fun onAdd(data: List<AddParams>): List<ActualPolyline?>

    suspend fun onChange(data: List<ChangeParams<ActualPolyline>>): List<ActualPolyline?>

    suspend fun onRemove(data: List<PolylineEntity<ActualPolyline>>)

    suspend fun onPostProcess()
}

abstract class PolylineController<ActualPolyline>(
    val polylineManager: PolylineManager<ActualPolyline>,
    open val renderer: PolylineOverlayRenderer<ActualPolyline>,
    override var clickListener: OnPolylineEventHandler? = null,
) : OverlayController<
        ActualPolyline,
        PolylineState,
        PolylineEntity<ActualPolyline>,
        PolylineState,
    > {
    override val zIndex: Int = 5
    val semaphore = Semaphore(1)

    override suspend fun add(data: List<PolylineState>) {
        semaphore.withPermit {
            val modifiedEntities = mutableListOf<PolylineEntity<ActualPolyline>>()
            val previous = polylineManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<PolylineOverlayRenderer.AddParams>()
            val updated = mutableListOf<PolylineOverlayRenderer.ChangeParams<ActualPolyline>>()
            val removed = mutableListOf<PolylineEntity<ActualPolyline>>()

            data.forEach { state ->
                if (previous.contains(state.id)) {
                    val prevEntity = polylineManager.getEntity(state.id)!!
                    updated.add(
                        object : PolylineOverlayRenderer.ChangeParams<ActualPolyline> {
                            override val current: PolylineEntity<ActualPolyline> =
                                PolylineEntityImpl(
                                    state = state,
                                    polyline = prevEntity.polyline,
                                )
                            override val prev: PolylineEntity<ActualPolyline> = prevEntity
                        },
                    )
                    previous.remove(state.id)
                } else {
                    added.add(
                        object : PolylineOverlayRenderer.AddParams {
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
                actualPolylines.forEachIndexed { index, actualPolyline ->
                    actualPolyline?.let {
                        val entity =
                            PolylineEntityImpl<ActualPolyline>(
                                polyline = actualPolyline,
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

                actualPolylines.forEachIndexed { index, actualPolyline ->
                    actualPolyline?.let {
                        val params = updated[index]
                        val entity =
                            PolylineEntityImpl<ActualPolyline>(
                                state = params.current.state,
                                polyline = actualPolyline,
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
                PolylineEntityImpl(
                    polyline = polyline,
                    state = state,
                )
            val polylineParams =
                object : PolylineOverlayRenderer.ChangeParams<ActualPolyline> {
                    override val current: PolylineEntity<ActualPolyline> = entity
                    override val prev: PolylineEntity<ActualPolyline> = prevEntity
                }
            val polylines = renderer.onChange(listOf(polylineParams))

            polylines[0]?.let {
                val entity =
                    PolylineEntityImpl<ActualPolyline>(
                        polyline = it,
                        state = state,
                    )
                polylineManager.registerEntity(entity)
            }
        }
    }

    override suspend fun clear() {
        semaphore.withPermit {
            val entities: List<PolylineEntity<ActualPolyline>> = polylineManager.allEntities()
            renderer.onRemove(entities)
            polylineManager.clear()
        }
    }

    override fun find(position: IGeoPoint): PolylineEntity<ActualPolyline>? = polylineManager.findNearest(position)
}

interface PolylineManager<ActualPolyline> {
    fun registerEntity(entity: PolylineEntity<ActualPolyline>)

    fun removeEntity(id: String): PolylineEntity<ActualPolyline>?

    fun getEntity(id: String): PolylineEntity<ActualPolyline>?

    fun allEntities(): List<PolylineEntity<ActualPolyline>>

    fun clear()

    fun findNearest(position: IGeoPoint): PolylineEntity<ActualPolyline>?
}

class PolylineManagerImpl<ActualPolyline> : PolylineManager<ActualPolyline> {
    private val entities = mutableMapOf<String, PolylineEntity<ActualPolyline>>()

    override fun registerEntity(entity: PolylineEntity<ActualPolyline>) {
        entities[entity.state.id] = entity
    }

    override fun removeEntity(id: String): PolylineEntity<ActualPolyline>? = entities.remove(id)

    override fun getEntity(id: String): PolylineEntity<ActualPolyline>? = entities[id]

    override fun allEntities(): List<PolylineEntity<ActualPolyline>> = entities.values.toList()

    override fun clear() {
        entities.clear()
    }

    override fun findNearest(position: IGeoPoint): PolylineEntity<ActualPolyline>? = entities.values.firstOrNull()
}
