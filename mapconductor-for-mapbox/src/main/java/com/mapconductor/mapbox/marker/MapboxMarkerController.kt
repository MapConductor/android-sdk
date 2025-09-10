package com.mapconductor.mapbox.marker

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.core.spherical.expandBounds
import com.mapconductor.core.spherical.haversineDistance
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.settings.Settings
import kotlinx.coroutines.sync.withPermit

class MapboxMarkerController(
    override val renderer: MapboxMarkerOverlayRenderer,
) : AbstractMarkerController<MapboxActualMarker>(
        markerManager = renderer.markerManager,
        renderer = renderer,
    ) {
    private var internalSelectedMarker: MarkerEntity<MapboxActualMarker>? = null

    internal var selectedMarker: MarkerEntity<MapboxActualMarker>?
        set(value) {
            if (value == null) {
                internalSelectedMarker?.let {
                    renderer.dragLayer.updatePosition(GeoPoint.from(it.state.position))
                    // Restore the recomposition for the position property
                    setDraggingState(it.state, false)
                    renderer.drawDragLayer()
                    markerManager.registerEntity(it)
                    renderer.redraw()
                }
                return
            }
            internalSelectedMarker = value
            markerManager.removeEntity(value.state.id)
            // Suppress the recomposition for the position property
            setDraggingState(value.state, true)
            renderer.dragLayer.selected = value
            renderer.dragLayer.updatePosition(GeoPoint.from(value.state.position))
            renderer.redraw()
            renderer.drawDragLayer()
        }
        get() = internalSelectedMarker

    override fun find(position: IGeoPoint): MarkerEntity<MapboxActualMarker>? {
        return markerManager.findNearest(position)?.let { nearest ->
            val zoom = renderer.holder.map.cameraState.zoom
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
        val viewportBounds = expandBounds(visibleRegion.bounds, margin = 0.2)

        // For Mapbox optimization: Only add markers, never remove them once rendered
        // This avoids expensive FeatureCollection recreation
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

                // For Mapbox, we need to redraw the layer after adding new markers
                renderer.onPostProcess()
            }
        }

        // Update visibility flags for all entities based on viewport
        markerManager.allEntities().forEach { entity ->
            entity.visible = viewportBounds.contains(entity.state.position)
        }
    }
}
