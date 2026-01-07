package com.mapconductor.marker.strategy

import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.geocell.HexGeocellInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.AbstractMarkerRenderingStrategy
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerState
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Simple fallback marker rendering strategy for when no advanced strategy is provided.
 * This basic strategy renders all markers without viewport-based optimizations.
 */
class SimpleMarkerStrategy<ActualMarker>(
    semaphore: Semaphore = Semaphore(1),
    geocell: HexGeocellInterface = HexGeocell.defaultGeocell(),
) : AbstractMarkerRenderingStrategy<ActualMarker>(semaphore) {
    /**
     * Default MarkerManager instance provided by dependency injection.
     */
    override val markerManager: MarkerManager<ActualMarker> = MarkerManager(geocell)

    override fun clear() {
        markerManager.clear()
    }

    override suspend fun onCameraChanged(
        cameraPosition: MapCameraPosition,
        renderer: MarkerOverlayRendererInterface<ActualMarker>,
    ) {
        semaphore.withPermit {
            // Simple strategy: just render all markers that aren't already rendered
            val allMarkers = markerManager.allEntities()
            val markersToRender = allMarkers.filter { !it.isRendered }

            if (markersToRender.isNotEmpty()) {
                val defaultIcon = ColorDefaultIcon()
                val addParams =
                    markersToRender.map { entity ->
                        object : MarkerOverlayRendererInterface.AddParamsInterface {
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
