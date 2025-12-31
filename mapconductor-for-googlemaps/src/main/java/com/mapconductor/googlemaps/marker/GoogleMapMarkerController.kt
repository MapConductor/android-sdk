package com.mapconductor.googlemaps.marker

import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerRenderingStrategy
import com.mapconductor.core.spherical.Spherical.computeDistanceBetween
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toGeoPoint
import com.mapconductor.settings.Settings

class GoogleMapMarkerController private constructor(
    override val renderer: GoogleMapMarkerRenderer,
    markerManager: MarkerManager<GoogleMapActualMarker>,
    renderingStrategy: MarkerRenderingStrategy<GoogleMapActualMarker>? = null,
) : AbstractMarkerController<GoogleMapActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
        renderingStrategy = renderingStrategy,
    ),
    OnMarkerClickListener,
    OnMarkerDragListener {
    init {
        renderer.holder.map.apply {
            setOnMarkerClickListener(this@GoogleMapMarkerController)
            setOnMarkerDragListener(this@GoogleMapMarkerController)
        }
    }

    override fun onMarkerClick(marker: GoogleMapActualMarker): Boolean {
        val stateId = (marker.tag as? String) ?: return false
        markerManager.getEntity(stateId)?.also { entity ->
            if (!entity.state.clickable) return true
            dispatchClick(entity.state)
        }
        return true
    }

    override fun onMarkerDrag(marker: GoogleMapActualMarker) {
        val stateId = (marker.tag as? String) ?: return
        markerManager.getEntity(stateId)?.also { entity ->

            // Suppress the recomposition for the position property
            setDraggingState(entity.state, true)

            entity.state.position = marker.position.toGeoPoint()
            dispatchDrag(entity.state)
        }
    }

    override fun onMarkerDragEnd(marker: GoogleMapActualMarker) {
        val stateId = (marker.tag as? String) ?: return
        markerManager.getEntity(stateId)?.also { entity ->
            entity.state.position = marker.position.toGeoPoint()
            dispatchDragEnd(entity.state)
        }
    }

    override fun onMarkerDragStart(marker: GoogleMapActualMarker) {
        val stateId = (marker.tag as? String) ?: return
        markerManager.getEntity(stateId)?.also { entity ->
            entity.state.position = marker.position.toGeoPoint()
            // Restore the recomposition for the position property
            setDraggingState(entity.state, false)
            dispatchDragStart(entity.state)
        }
    }

    override fun find(position: GeoPoint): MarkerEntity<GoogleMapActualMarker>? {
        return markerManager.findNearest(position)?.let { nearest ->
            val zoom =
                renderer.holder.map.cameraPosition.zoom
                    .toDouble()
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
        fun create(
            holder: GoogleMapViewHolder,
            renderingStrategy: MarkerRenderingStrategy<GoogleMapActualMarker>? = null,
        ): GoogleMapMarkerController {
            val markerManager = renderingStrategy?.markerManager ?: MarkerManager.defaultManager()
            val renderer =
                GoogleMapMarkerRenderer(
                    holder = holder,
                )
            val controller =
                GoogleMapMarkerController(
                    renderer = renderer,
                    markerManager = markerManager,
                )
            return controller
        }
    }
}
