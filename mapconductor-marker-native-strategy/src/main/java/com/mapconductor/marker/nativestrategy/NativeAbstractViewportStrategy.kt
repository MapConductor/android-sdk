package com.mapconductor.marker.nativestrategy

import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.marker.AbstractMarkerRenderingStrategy
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerEntityImpl
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerState
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Abstract base class for native marker rendering strategies that use viewport-based optimization.
 * This class handles the logic of deciding which markers to render based on the current viewport,
 * while maintaining synchronization with the native C++ spatial index for optimal performance.
 */
abstract class NativeAbstractViewportStrategy<ActualMarker>(
    semaphore: Semaphore,
    geocell: HexGeocell,
) : AbstractMarkerRenderingStrategy<ActualMarker>(semaphore) {
    /**
     * Native-optimized MarkerManager that eliminates Java-based redundant storage
     */
    override val markerManager: MarkerManager<ActualMarker> = NativeMarkerManager(geocell)

    protected var isInitialized = false

    /**
     * Handle adding markers with viewport optimization and native index synchronization.
     * Only renders markers that are within the current viewport, but keeps all markers in native index.
     * This method is called from the strategy's onCameraChanged implementation.
     */
    override suspend fun onAdd(
        data: List<MarkerState>,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ): Boolean {
        semaphore.withPermit {
            val modifiedEntities = mutableListOf<MarkerEntity<ActualMarker>>()
            val previous = markerManager.allEntities().map { it.state.id }.toMutableSet()
            val added = mutableListOf<MarkerOverlayRenderer.AddParams>()
            val updated = mutableListOf<MarkerOverlayRenderer.ChangeParams<ActualMarker>>()
            val removed = mutableListOf<MarkerEntity<ActualMarker>>()
            val viewportBounds = viewport

            data.forEach { state ->
                val isInViewport = viewportBounds.contains(state.position)

                if (previous.contains(state.id)) {
                    val prevEntity = markerManager.getEntity(state.id)!!
                    val markerIcon = state.icon?.toBitmapIcon() ?: defaultIcon

                    // Marker update handled by NativeMarkerManager

                    // Only add to update list if marker is in viewport
                    if (isInViewport) {
                        updated.add(
                            object : MarkerOverlayRenderer.ChangeParams<ActualMarker> {
                                override val current: MarkerEntity<ActualMarker> =
                                    MarkerEntityImpl(
                                        state = state,
                                        marker = prevEntity.marker,
                                        isRendered = true,
                                    )
                                override val bitmapIcon: BitmapIcon = markerIcon
                                override val prev: MarkerEntity<ActualMarker> = prevEntity
                            },
                        )
                    } else {
                        // Register entity without rendering for markers outside viewport
                        val entity =
                            MarkerEntityImpl(
                                state = state,
                                marker = prevEntity.marker,
                                isRendered = false, // Not rendered since outside viewport
                            )
                        markerManager.registerEntity(entity)
                    }
                    previous.remove(state.id)
                } else {
                    // New marker registration handled by NativeMarkerManager

                    // Only add to render list if marker is in viewport
                    if (isInViewport) {
                        added.add(
                            object : MarkerOverlayRenderer.AddParams {
                                override val state: MarkerState = state
                                override val bitmapIcon: BitmapIcon =
                                    state.icon?.toBitmapIcon() ?: defaultIcon
                            },
                        )
                    } else {
                        // Register entity without rendering for new markers outside viewport
                        val entity =
                            MarkerEntityImpl<ActualMarker>(
                                marker = null,
                                state = state,
                                isRendered = false, // Not rendered since outside viewport
                            )
                        markerManager.registerEntity(entity)
                    }
                    previous.remove(state.id)
                }
            }

            // Remove markers from manager (native index cleanup handled by NativeMarkerManager)
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
                            MarkerEntityImpl<ActualMarker>(
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
                            MarkerEntityImpl<ActualMarker>(
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
     * Handle updating a marker with viewport optimization and native index synchronization.
     * Only renders the marker if it's within the current viewport, but always updates native index.
     * This method is called from the strategy's onCameraChanged implementation.
     */
    override suspend fun onUpdate(
        state: MarkerState,
        viewport: GeoRectBounds,
        renderer: MarkerOverlayRenderer<ActualMarker>,
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

            // Marker update handled by NativeMarkerManager

            // Always update the entity in the manager
            val entity =
                MarkerEntityImpl(
                    marker = prevEntity.marker,
                    state = state,
                    isRendered = prevEntity.isRendered,
                )
            markerManager.registerEntity(entity)

            // Only render if in viewport
            val isInViewport = viewport.contains(state.position)
            if (isInViewport) {
                val marker = prevEntity.marker
                val markerIcon = state.icon?.toBitmapIcon() ?: defaultIcon

                val renderEntity =
                    MarkerEntityImpl(
                        marker = marker,
                        state = state,
                    )
                val markerParams =
                    object : MarkerOverlayRenderer.ChangeParams<ActualMarker> {
                        override val current: MarkerEntity<ActualMarker> = renderEntity
                        override val bitmapIcon: BitmapIcon = markerIcon
                        override val prev: MarkerEntity<ActualMarker> = prevEntity
                    }
                val markers = renderer.onChange(listOf(markerParams))

                markers[0]?.let {
                    val finalEntity =
                        MarkerEntityImpl<ActualMarker>(
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

    /**
     * Clean up native resources. Should be called when the strategy is no longer needed.
     */
    fun destroy() {
        markerManager.destroy()
    }
}
