package com.mapconductor.googlemaps.marker

import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.spherical.expandBounds
import com.mapconductor.core.spherical.haversineDistance
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.toGeoPoint
import com.mapconductor.settings.Settings

class GoogleMapMarkerController(
    markerManager: MarkerManager<GoogleMapActualMarker>,
    override val renderer: GoogleMapMarkerRenderer,
) : AbstractMarkerController<GoogleMapActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
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
            clickListener?.invoke(entity.state)
        }
        return true
    }

    override fun onMarkerDrag(marker: GoogleMapActualMarker) {
        val stateId = (marker.tag as? String) ?: return
        markerManager.getEntity(stateId)?.also { entity ->

            // Suppress the recomposition for the position property
            setDraggingState(entity.state, true)

            entity.state.position = marker.position.toGeoPoint()
            dragListener?.invoke(entity.state)
        }
    }

    override fun onMarkerDragEnd(marker: GoogleMapActualMarker) {
        val stateId = (marker.tag as? String) ?: return
        markerManager.getEntity(stateId)?.also { entity ->
            entity.state.position = marker.position.toGeoPoint()
            dragEndListener?.invoke(entity.state)
        }
    }

    override fun onMarkerDragStart(marker: GoogleMapActualMarker) {
        val stateId = (marker.tag as? String) ?: return
        markerManager.getEntity(stateId)?.also { entity ->
            entity.state.position = marker.position.toGeoPoint()
            // Restore the recomposition for the position property
            setDraggingState(entity.state, false)
            dragStartListener?.invoke(entity.state)
        }
    }

    override fun find(position: IGeoPoint): MarkerEntity<GoogleMapActualMarker>? {
        return markerManager.findNearest(position)?.let { nearest ->
            val zoom =
                renderer.holder.map.cameraPosition.zoom
                    .toDouble()
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
