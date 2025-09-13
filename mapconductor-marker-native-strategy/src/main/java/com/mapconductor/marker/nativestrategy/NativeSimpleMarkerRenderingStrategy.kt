package com.mapconductor.marker.nativestrategy

import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.AbstractMarkerRenderingStrategy
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerState
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Simple fallback marker rendering strategy for when no advanced strategy is provided.
 * This basic strategy renders all markers without viewport-based optimizations.
 */
class NativeSimpleMarkerRenderingStrategy<ActualMarker>(
    semaphore: Semaphore,
) : AbstractMarkerRenderingStrategy<ActualMarker>(semaphore) {
    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPosition,
        markerManager: MarkerManager<ActualMarker>,
        renderer: MarkerOverlayRenderer<ActualMarker>,
    ) {
        semaphore.withPermit {
            // Simple strategy: just render all markers that aren't already rendered
            val allMarkers = markerManager.allEntities()
            val markersToRender = allMarkers.filter { !it.isRendered }

            if (markersToRender.isNotEmpty()) {
                val defaultIcon = ColorDefaultIcon()
                val addParams =
                    markersToRender.map { entity ->
                        object : MarkerOverlayRenderer.AddParams {
                            override val state: MarkerState = entity.state
                            override val bitmapIcon: BitmapIcon =
                                entity.state.icon?.toBitmapIcon() ?: defaultIcon.toBitmapIcon()
                        }
                    }

                val actualMarkers = renderer.onAdd(addParams)
                actualMarkers.forEachIndexed { index, actualMarker ->
                    actualMarker?.let {
                        markersToRender[index].marker = it
                        markersToRender[index].isRendered = true
                        markersToRender[index].visible = true
                    }
                }

                renderer.onPostProcess()
            }
        }
    }
}
