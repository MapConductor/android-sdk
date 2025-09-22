package com.mapconductor.arcgis.marker

import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.SurfacePlacement
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.getZoomLevel
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.haversineDistance
import com.mapconductor.settings.Settings

internal data class SelectedMarker(
    val state: MarkerState,
    val graphic: Graphic,
)

class ArcGISMarkerController private constructor(
    markerManager: MarkerManager<ArcGISActualMarker>,
    override val renderer: ArcGISMarkerRenderer,
    renderingStrategy: MarkerRenderingStrategy<ArcGISActualMarker>? = null,
) : AbstractMarkerController<ArcGISActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
        renderingStrategy = renderingStrategy,
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

    override fun find(position: GeoPoint): MarkerEntity<ArcGISActualMarker>? {
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

    companion object {
        fun create(
            holder: ArcGISMapViewHolder,
            renderingStrategy: MarkerRenderingStrategy<ArcGISActualMarker>? = null,
        ): ArcGISMarkerController {
            val markerLayer: GraphicsOverlay =
                GraphicsOverlay().apply {
                    sceneProperties.surfacePlacement = SurfacePlacement.Relative
                }

            val renderer =
                ArcGISMarkerRenderer(
                    markerLayer = markerLayer,
                    holder = holder,
                )

            val markerManager = renderingStrategy?.markerManager ?: MarkerManager.defaultManager()

            val controller =
                ArcGISMarkerController(
                    markerManager = markerManager,
                    renderer = renderer,
                    renderingStrategy = renderingStrategy,
                )
            return controller
        }
    }
}
