package com.mapconductor.googlemaps.marker

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.spherical.Spherical.computeDistanceBetween
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.settings.Settings

class GoogleMapMarkerController private constructor(
    override val renderer: GoogleMapMarkerRenderer,
    markerManager: MarkerManager<GoogleMapActualMarker>,
) : AbstractMarkerController<GoogleMapActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
    )
{

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
        ): GoogleMapMarkerController {
            val markerManager = MarkerManager.defaultManager<GoogleMapActualMarker>()
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
