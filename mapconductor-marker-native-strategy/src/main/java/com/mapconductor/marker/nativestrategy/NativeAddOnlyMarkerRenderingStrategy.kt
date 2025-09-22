package com.mapconductor.marker.nativestrategy

import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.core.spherical.expandBounds
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Native-optimized marker rendering strategy for HERE and Mapbox providers.
 * This strategy only adds markers when they enter the viewport and never removes them
 * once rendered, avoiding expensive add/remove operations on the map.
 *
 * @param expandMargin The margin for expanding viewport bounds (default 0.5 = 50% expansion)
 * @param semaphore Optional semaphore for synchronizing rendering operations (required for Mapbox)
 * @param geocell Hex geocell for native spatial indexing
 */
class NativeAddOnlyMarkerRenderingStrategy<ActualMarker>(
    private val expandMargin: Double = 0.5,
    semaphore: Semaphore = Semaphore(1),
    geocell: HexGeocell = NativeHexGeocellImpl.defaultGeocell(),
) : NativeAbstractViewportStrategy<ActualMarker>(semaphore, geocell) {

    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPositionImpl,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        val visibleRegion = cameraPosition.visibleRegion ?: return
        val viewportBounds = expandBounds(visibleRegion.bounds, margin = expandMargin)

        // Find markers that need to be added to the viewport
        val toAdd =
            markerManager.allEntities().filter { entity ->
                viewportBounds.contains(entity.state.position) && !entity.isRendered && entity.marker == null
            }

        if (toAdd.isNotEmpty()) {
            semaphore.withPermit {
                val addParams =
                    toAdd.map { entity ->
                        object : MarkerOverlayRenderer.AddParams {
                            override val state = entity.state
                            override val bitmapIcon =
                                entity.state.icon?.toBitmapIcon()
                                    ?: defaultIcon
                        }
                    }
                val newMarkers = renderer.onAdd(addParams)

                toAdd.forEachIndexed { index, entity ->
                    if (index < newMarkers.size) {
                        entity.marker = newMarkers[index]
                        entity.isRendered = newMarkers[index] != null
                    }
                }

                // Post-process for providers that need it (like Mapbox)
                renderer.onPostProcess()
            }
        }

        // Update visibility flags for all entities based on viewport
        markerManager.allEntities().forEach { entity ->
            entity.visible = viewportBounds.contains(entity.state.position)
        }
    }
}
