package com.mapconductor.mapbox.marker

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.spherical.haversineDistance
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.settings.Settings

class MapboxMarkerController(
    override val renderer: MapboxMarkerOverlayRenderer,
    renderingStrategy: MarkerRenderingStrategy<MapboxActualMarker>? = null,
) : AbstractMarkerController<MapboxActualMarker>(
        markerManager = renderer.markerManager,
        renderer = renderer,
        renderingStrategy = renderingStrategy,
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
}
