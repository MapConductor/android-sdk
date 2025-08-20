package com.mapconductor.core.marker

import com.mapconductor.core.controller.OverlayController
import com.mapconductor.core.controller.OverlayRenderer
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.marker.MarkerRenderer.UpdateParams
import kotlin.collections.forEach
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

interface MarkerCapable<ActualMarker> {
    suspend fun compositionMarkers(data: List<MarkerState>)

    suspend fun updateMarker(state: MarkerState)
}


class MarkerController<ActualMarker>(
    val markerManager: MarkerManager<ActualMarker>,
    override val renderer: OverlayRenderer<ActualMarker, MarkerState, MarkerEntity<ActualMarker>>,
    override var clickListener: OnMarkerEventHandler? = null,
) : OverlayController<
    ActualMarker,
    MarkerState,
    MarkerEntity<ActualMarker>,
    MarkerState,
> {
    override val zIndex: Int = 10
    val entities = mutableMapOf<String, MarkerEntity<ActualMarker>>()
    val semaphore = Semaphore(1)

    override suspend fun add(data: List<MarkerState>) {
        semaphore.withPermit {
            val defaultIcon = DefaultIcon()
            val defaultIconBitmapIcon = defaultIcon.toBitmapIcon()
            val modifiedEntities = mutableListOf<MarkerEntity<ActualMarker>>()
            val previous = markerManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<MarkerState>()
            val updated = mutableListOf<UpdateParams<ActualMarker>>()
            val removed = mutableListOf<MarkerEntity<ActualMarker>>()
            data.forEach { state ->
                if (previous.contains(state.id)) {
                    val prevEntity = markerManager.getEntity(state.id)!!
                    val markerIcon = state.icon ?: defaultIcon
                    updated.add(
                        object : UpdateParams<ActualMarker> {
                            override val entity: MarkerEntity<ActualMarker> =
                                MarkerEntityImpl(
                                    state = state,
                                    marker = prevEntity.marker,
                                )
                            override val bitmapIcon: BitmapIcon
                                get() {
                                    return markerIcon.toBitmapIcon()
                                }
                            override val prevEntity: MarkerEntity<ActualMarker> = prevEntity
                        },
                    )
                    previous.remove(state.id)
                    return@forEach
                }
                added.add(state)
                previous.remove(state.id)
            }
            previous.forEach { remainId ->
                markerManager.removeEntity(remainId)?.let { removedEntity ->
                    removed.add(removedEntity)
                }
            }

            // Remove markers
            if (removed.isNotEmpty()) {
                renderer.onRemove(removed)
            }

            // Add new markers
            if (added.isNotEmpty()) {
                val addedList = added.toList()

                addedList
                    .map { state ->
                        val markerIcon = state.icon?.toBitmapIcon() ?: defaultIconBitmapIcon
                        Pair(state, markerIcon)
                    }.also {
                        val actualMarkers: List<ActualMarker?> = renderer.onAdd(it)
                        actualMarkers.forEachIndexed { index, actualMarker ->
                            actualMarker?.let {
                                val entity =
                                    MarkerEntityImpl<ActualMarker>(
                                        marker = actualMarker,
                                        state = addedList[index],
                                    )
                                markerManager.registerEntity(entity)
                                modifiedEntities.add(entity)
                            }
                        }
                    }
            }

            // Update changed markers
            if (updated.isNotEmpty()) {
                val actualMarkers: List<ActualMarker?> = renderer.onChange(updated)

                actualMarkers.forEachIndexed { index, actualMarker ->
                    actualMarker?.let {
                        val params = updated[index]
                        val entity =
                            MarkerEntityImpl<ActualMarker>(
                                state = params.entity.state,
                                marker = actualMarker,
                            )
                        markerManager.registerEntity(entity)
                    }
                }
            }
            modifiedEntities.forEach { entity ->
                entity.state.animation?.let {
                    renderer.onAnimate(entity)
                }
            }
            renderer.onPostProcess()
        }
    }

    override suspend fun update(state: MarkerState) {
        semaphore.withPermit {
            val updated = mutableListOf<OverlayRenderer.Changes<MarkerEntity<ActualMarker>>>()
            val prevEntity = entities[state.id]!!
            updated.add(
                object : OverlayRenderer.Changes<MarkerEntity<ActualMarker>> {
                    override val current: MarkerEntity<ActualMarker> =
                        MarkerEntityImpl(
                            marker = prevEntity.marker,
                            state = state,
                        )
                    override val prev: MarkerEntity<ActualMarker> = prevEntity
                },
            )

            val actualOverlays: List<ActualMarker?> = renderer.onChange(updated)
            actualOverlays.forEachIndexed { index, actualOverlay ->
                actualOverlay?.let {
                    val entity =
                        MarkerEntityImpl<ActualMarker>(
                            marker = it,
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

    override fun find(position: IGeoPoint): MarkerEntity<ActualMarker>? {
        // TODO: Improve this implementation later
        return entities.values.find { entity ->
            entity.state.bounds.contains(position)
        }
    }
}
