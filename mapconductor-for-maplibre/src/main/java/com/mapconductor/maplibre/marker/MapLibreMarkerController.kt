package com.mapconductor.maplibre.marker

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.spherical.Spherical.computeDistanceBetween
import com.mapconductor.maplibre.MapLibreActualMarker
import com.mapconductor.settings.Settings

class MapLibreMarkerController(
    override val renderer: MapLibreMarkerOverlayRenderer,
    renderingStrategy: MarkerRenderingStrategy<MapLibreActualMarker>? = null,
) : AbstractMarkerController<MapLibreActualMarker>(
        markerManager = renderer.markerManager,
        renderer = renderer,
        renderingStrategy = renderingStrategy,
    ) {
    private var internalSelectedMarker: MarkerEntity<MapLibreActualMarker>? = null

    internal var selectedMarker: MarkerEntity<MapLibreActualMarker>?
        set(value) {
            if (value == null) {
                internalSelectedMarker?.let {
                    renderer.dragLayer.updatePosition(GeoPointImpl.from(it.state.position))
                    // Restore the recomposition for the position property
                    setDraggingState(it.state, false)
                    // Clear drag layer selection to avoid duplicate icon after drop
                    renderer.dragLayer.selected = null
                    renderer.drawDragLayer()
                    markerManager.registerEntity(it)
                    renderer.redraw()
                }
                internalSelectedMarker = null
                return
            }
            internalSelectedMarker = value
            markerManager.removeEntity(value.state.id)
            // Suppress the recomposition for the position property
            setDraggingState(value.state, true)
            renderer.dragLayer.selected = value
            renderer.dragLayer.updatePosition(GeoPointImpl.from(value.state.position))
            renderer.redraw()
            renderer.drawDragLayer()
        }
        get() = internalSelectedMarker

    override fun find(position: GeoPoint): MarkerEntity<MapLibreActualMarker>? {
        return markerManager.findNearest(position)?.let { nearest ->
            val zoom = renderer.holder.map.cameraPosition.zoom
            val tolerance =
                Settings.Default.tapTolerance.value
                    .toDouble() * ResourceProvider.getDensity()
            val meterInMapPixel = renderer.zoomToMetersPerPixel(zoom, 256)
            val radius = (tolerance * 0.5) * meterInMapPixel
            val distance = computeDistanceBetween(position, nearest.state.position)
            return if (distance <= radius) {
                nearest
            } else {
                null
            }
        }
    }
}
