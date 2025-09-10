package com.mapconductor.here.marker

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.spherical.expandBounds
import com.mapconductor.core.spherical.haversineDistance
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.here.HereActualMarker
import com.mapconductor.settings.Settings
import kotlinx.coroutines.launch

class HereMarkerController(
    markerManager: MarkerManager<HereActualMarker>,
    override val renderer: HereMarkerRenderer,
) : AbstractMarkerController<HereActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
    ) {
    private var internalSelectedMarker: MarkerEntity<HereActualMarker>? = null

    internal var selectedMarker: MarkerEntity<HereActualMarker>?
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

    companion object {
        private const val ZOOM_ADJUST_VALUE = 0.1 // バイナリテストで確定
    }

    override fun find(position: IGeoPoint): MarkerEntity<HereActualMarker>? {
        return markerManager.findNearest(position)?.let { nearest ->
            val zoom = renderer.holder.mapView.camera.state.zoomLevel - ZOOM_ADJUST_VALUE
            val tolerance =
                Settings.Default.tapTolerance.value
                    .toDouble() * ResourceProvider.getDensity()
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

    override suspend fun onCameraChanged(cameraPosition: MapCameraPosition) {
        val visibleRegion = cameraPosition.visibleRegion ?: return
        val viewportBounds = expandBounds(visibleRegion.bounds, margin = 0.5)
        
        // For HERE Maps optimization: Only add markers, never remove them once rendered
        // This avoids expensive scene add/remove operations
        val toAdd = markerManager.allEntities().filter { entity ->
            viewportBounds.contains(entity.state.position) && !entity.isRendered && entity.marker == null
        }

        if (toAdd.isNotEmpty()) {
            val addParams = toAdd.map { entity ->
                object : MarkerOverlayRenderer.AddParams {
                    override val state = entity.state
                    override val bitmapIcon = entity.state.icon?.toBitmapIcon() ?: DefaultIcon().toBitmapIcon()
                }
            }
            val newMarkers = renderer.onAdd(addParams)
            
            toAdd.forEachIndexed { index, entity ->
                if (index < newMarkers.size) {
                    entity.marker = newMarkers[index]
                    entity.isRendered = newMarkers[index] != null
                }
            }
        }
        
        // Update visibility flags for all entities based on viewport
        markerManager.allEntities().forEach { entity ->
            entity.visible = viewportBounds.contains(entity.state.position)
        }
    }
}
