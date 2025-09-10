package com.mapconductor.arcgis.marker

import com.arcgismaps.mapping.view.Graphic
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.getZoomLevel
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.spherical.expandBounds
import com.mapconductor.core.spherical.haversineDistance
import com.mapconductor.settings.Settings

internal data class SelectedMarker(
    val state: MarkerState,
    val graphic: Graphic,
)

class ArcGISMarkerController(
    markerManager: MarkerManager<ArcGISActualMarker>,
    override val renderer: ArcGISMarkerRenderer,
) : AbstractMarkerController<ArcGISActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
    ) {
    private var internalSelectedMarker: SelectedMarker? = null

    internal var selectedMarker: SelectedMarker?
        set(value) {
            if (value == null) {
                internalSelectedMarker?.let {
                    // Restore the recomposition for the position property
                    setDraggingState(it.state, false)
                }
                return
            }
            internalSelectedMarker = value
            // Suppress the recomposition for the position property
            setDraggingState(value.state, true)
        }
        get() = internalSelectedMarker

    override fun find(position: IGeoPoint): MarkerEntity<ArcGISActualMarker>? {
        return markerManager.findNearest(position)?.let { nearest ->
            val tolerance =
                Settings.Default.tapTolerance.value
                    .toDouble() * ResourceProvider.getDensity()
            val zoom =
                renderer.holder.map
                    .getCurrentViewpointCamera()
                    .getZoomLevel()
            val meterInMapPixel = renderer.zoomToMetersPerPixel(zoom)
            val radius = tolerance * meterInMapPixel
            val distance = haversineDistance(position, nearest.state.position)
            return if (distance <= radius) {
                nearest
            } else {
                null
            }
        }
    }

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        mapCameraPosition.visibleRegion?.bounds?.let { bounds ->
            // Expand bounds by 20% margin for better performance
            val expandedBounds = expandBounds(bounds, 0.2)

            // Get markers within expanded bounds
            val visibleMarkers = markerManager.findMarkersInBounds(expandedBounds)
            val allMarkers = markerManager.allEntities()

            // Show markers in bounds, hide others
            visibleMarkers.forEach { entity ->
                entity.visible = true
            }

            allMarkers.filterNot { visibleMarkers.contains(it) }.forEach { entity ->
                entity.visible = false
            }
        }
    }
}
