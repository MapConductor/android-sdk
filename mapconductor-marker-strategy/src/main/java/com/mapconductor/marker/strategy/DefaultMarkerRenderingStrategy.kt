package com.mapconductor.marker.strategy

import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.geocell.HexGeocellImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.AbstractViewportStrategy
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.expandBounds
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Default marker rendering strategy used by Google Maps and ArcGIS providers.
 * This strategy dynamically adds and removes markers based on viewport changes,
 * providing optimal memory usage and performance for providers that handle
 * add/remove operations efficiently.
 *
 * @param expandMargin The margin for expanding viewport bounds (default 0.2 = 20% expansion)
 * @param semaphore The semaphore for synchronizing rendering operations
 * @param geocell Hex geocell for spatial indexing
 */
class DefaultMarkerRenderingStrategy<ActualMarker>(
    private val expandMargin: Double = 0.2,
    semaphore: Semaphore = Semaphore(1),
    geocell: HexGeocell = HexGeocellImpl.defaultGeocell(),
) : AbstractViewportStrategy<ActualMarker>(semaphore, geocell) {
    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPositionImpl,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        semaphore.withPermit {
            val visibleRegion = cameraPosition.visibleRegion ?: return

            visibleRegion.bounds.let { bounds ->
                // Expand bounds by the specified margin for better performance
                val expandedBounds = expandBounds(bounds, expandMargin)

                // Get all entities and separate them by viewport status
                val allMarkers = markerManager.allEntities()
                val markersToRender = mutableListOf<MarkerEntity<ActualMarker>>()
                val markersToRemove = mutableListOf<MarkerEntity<ActualMarker>>()

                allMarkers.forEach { entity ->
                    val isInViewport = expandedBounds.contains(entity.state.position)

                    if (isInViewport && !entity.isRendered) {
                        // Marker entered viewport, need to render
                        markersToRender.add(entity)
                        entity.visible = true
                    } else if (!isInViewport && entity.isRendered) {
                        // Marker left viewport, need to remove from rendering
                        markersToRemove.add(entity)
                        entity.visible = false
                    } else if (isInViewport) {
                        // Marker is in viewport and already rendered
                        entity.visible = true
                    } else {
                        // Marker is outside viewport and not rendered
                        entity.visible = false
                    }
                }

                // Remove markers that left the viewport
                if (markersToRemove.isNotEmpty()) {
                    renderer.onRemove(markersToRemove)
                    markersToRemove.forEach { entity ->
                        entity.isRendered = false
                        entity.marker = null
                    }
                }

                // Add markers that entered the viewport
                if (markersToRender.isNotEmpty()) {
                    val addParams =
                        markersToRender.map { entity ->
                            object : MarkerOverlayRenderer.AddParams {
                                override val state: MarkerState = entity.state
                                override val bitmapIcon: BitmapIcon =
                                    entity.state.icon?.toBitmapIcon() ?: defaultIcon
                            }
                        }

                    val actualMarkers = renderer.onAdd(addParams)
                    actualMarkers.forEachIndexed { index, actualMarker ->
                        actualMarker?.let {
                            markersToRender[index].marker = it
                            markersToRender[index].isRendered = true
                        }
                    }
                }

                if (markersToRender.isNotEmpty() || markersToRemove.isNotEmpty()) {
                    renderer.onPostProcess()
                }
            }
        }
    }
}
