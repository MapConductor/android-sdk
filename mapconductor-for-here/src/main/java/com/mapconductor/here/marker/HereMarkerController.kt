package com.mapconductor.here.marker

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.spherical.expandBounds
import com.mapconductor.core.spherical.haversineDistance
import com.mapconductor.here.HereActualMarker
import com.mapconductor.settings.Settings

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
}
