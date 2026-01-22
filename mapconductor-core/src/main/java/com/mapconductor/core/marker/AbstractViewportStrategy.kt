package com.mapconductor.core.marker

import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.geocell.HexGeocellInterface
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Abstract base class for marker rendering strategies that use viewport-based optimization.
 * This class handles the logic of deciding which markers to render based on the current viewport,
 * while delegating the specific rendering logic to subclasses.
 */
abstract class AbstractViewportStrategy<ActualMarker>(
    semaphore: Semaphore,
    geocell: HexGeocellInterface,
) : AbstractMarkerRenderingStrategy<ActualMarker>(semaphore) {
    /**
     * Default MarkerManager instance provided by dependency injection.
     */
    override val markerManager: MarkerManager<ActualMarker> = MarkerManager(geocell, 0)

    /**
     * Handle adding markers with viewport optimization.
     * Only renders markers that are within the current viewport.
     * This method is called from the strategy's onCameraChanged implementation.
     */
    override suspend fun onAdd(
        data: List<MarkerState>,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ): Boolean {
        semaphore.withPermit {
            val modifiedEntities = mutableListOf<MarkerEntityInterface<ActualMarker>>()
            val previous = markerManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<MarkerOverlayRendererInterface.AddParamsInterface>()
            val updated = mutableListOf<MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker>>()
            val removed = mutableListOf<MarkerEntityInterface<ActualMarker>>()
            val viewportBounds = viewport

            data.forEach { state ->
                val isInViewport = viewportBounds.contains(state.position)

                if (previous.contains(state.id)) {
                    val prevEntity = markerManager.getEntity(state.id)!!
                    val markerIcon = state.icon?.toBitmapIcon() ?: defaultMarkerIcon

                    // Only add to update list if marker is in viewport
                    if (isInViewport) {
                        updated.add(
                            object : MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker> {
                                override val current: MarkerEntityInterface<ActualMarker> =
                                    MarkerEntity(
                                        state = state,
                                        marker = prevEntity.marker,
                                        isRendered = true,
                                    )
                                override val bitmapIcon: BitmapIcon = markerIcon
                                override val prev: MarkerEntityInterface<ActualMarker> = prevEntity
                            },
                        )
                    } else {
                        // Register entity without rendering for markers outside viewport
                        val entity =
                            MarkerEntity(
                                state = state,
                                marker = prevEntity.marker,
                                isRendered = true,
                            )
                        markerManager.registerEntity(entity)
                    }
                    previous.remove(state.id)
                } else {
                    // Only add to render list if marker is in viewport
                    if (isInViewport) {
                        added.add(
                            object : MarkerOverlayRendererInterface.AddParamsInterface {
                                override val state: MarkerState = state
                                override val bitmapIcon: BitmapIcon =
                                    state.icon?.toBitmapIcon() ?: defaultMarkerIcon
                            },
                        )
                    } else {
                        // Register entity without rendering for new markers outside viewport
                        val entity =
                            MarkerEntity<ActualMarker>(
                                marker = null,
                                state = state,
                                isRendered = true,
                            )
                        markerManager.registerEntity(entity)
                    }
                    previous.remove(state.id)
                }
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
                val actualMarkers: List<ActualMarker?> = renderer.onAdd(added)
                actualMarkers.forEachIndexed { index, actualMarker ->
                    actualMarker?.let {
                        val entity =
                            MarkerEntity<ActualMarker>(
                                marker = actualMarker,
                                state = added[index].state,
                                isRendered = true,
                            )
                        markerManager.registerEntity(entity)
                        modifiedEntities.add(entity)
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
                            MarkerEntity<ActualMarker>(
                                state = params.current.state,
                                marker = actualMarker,
                                isRendered = true,
                            )
                        markerManager.registerEntity(entity)
                    }
                }
            }
            // Note: Animation handling removed due to internal visibility constraints
            renderer.onPostProcess()
        }
        return true
    }

    /**
     * Handle updating a marker with viewport optimization.
     * Only renders the marker if it's within the current viewport.
     * This method is called from the strategy's onCameraChanged implementation.
     */
    override suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ): Boolean {
        // Fast path: Check entity existence without semaphore to avoid blocking during initial marker addition
        if (!markerManager.hasEntity(state.id)) return true

        semaphore.withPermit {
            val prevEntity = markerManager.getEntity(state.id) ?: return true
            val currentFinger = state.fingerPrint()
            val prevFinger = prevEntity.fingerPrint
            if (currentFinger == prevFinger) {
                return true
            }

            // Always update the entity in the manager
            val entity =
                MarkerEntity(
                    marker = prevEntity.marker,
                    state = state,
                    isRendered = prevEntity.isRendered,
                )
            markerManager.registerEntity(entity)

            // Only render if in viewport
            val isInViewport = viewport.contains(state.position)
            if (isInViewport) {
                val marker = prevEntity.marker
                val markerIcon = state.icon?.toBitmapIcon() ?: defaultMarkerIcon

                val renderEntity =
                    MarkerEntity(
                        marker = marker,
                        state = state,
                    )
                val markerParams =
                    object : MarkerOverlayRendererInterface.ChangeParamsInterface<ActualMarker> {
                        override val current: MarkerEntityInterface<ActualMarker> = renderEntity
                        override val bitmapIcon: BitmapIcon = markerIcon
                        override val prev: MarkerEntityInterface<ActualMarker> = prevEntity
                    }
                val markers = renderer.onChange(listOf(markerParams))

                markers[0]?.let {
                    val finalEntity =
                        MarkerEntity<ActualMarker>(
                            marker = it,
                            state = state,
                            isRendered = true,
                        )
                    markerManager.registerEntity(finalEntity)

                    // Note: Animation handling removed due to internal visibility constraints
                }
            }
        }
        return true
    }

    override fun clear() {
        markerManager.clear()
    }
}
