package com.mapconductor.here.marker

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.spherical.Spherical.computeDistanceBetween
import com.mapconductor.here.HereActualMarker
import com.mapconductor.here.HereViewHolder
import com.mapconductor.settings.Settings

class HereMarkerController private constructor(
    markerManager: MarkerManager<HereActualMarker>,
    override val renderer: HereMarkerRenderer,
    renderingStrategy: MarkerRenderingStrategy<HereActualMarker>? = null,
) : AbstractMarkerController<HereActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
        renderingStrategy = renderingStrategy,
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

    override fun find(position: GeoPoint): MarkerEntity<HereActualMarker>? {
        return markerManager.findNearest(position)?.let { nearest ->
            val zoom = renderer.holder.mapView.camera.state.zoomLevel - ZOOM_ADJUST_VALUE
            val tolerance =
                Settings.Default.tapTolerance.value
                    .toDouble() * ResourceProvider.getDensity()
            val meterInMapPixel = renderer.zoomToMetersPerPixel(zoom, 256)
            val radius = tolerance * meterInMapPixel
            val distance = computeDistanceBetween(position, nearest.state.position)
            return if (distance <= radius) {
                nearest
            } else {
                null
            }
        }
    }

    companion object {
        private const val ZOOM_ADJUST_VALUE = 0.1 // バイナリテストで確定

        fun create(
            holder: HereViewHolder,
            renderingStrategy: MarkerRenderingStrategy<HereActualMarker>? = null,
        ): HereMarkerController {
            val renderer =
                HereMarkerRenderer(
                    holder = holder,
                )
            val markerManager = renderingStrategy?.markerManager ?: MarkerManager.defaultManager()

            val controller =
                HereMarkerController(
                    markerManager = markerManager,
                    renderer = renderer,
                    renderingStrategy = renderingStrategy,
                )
            return controller
        }
    }
}
