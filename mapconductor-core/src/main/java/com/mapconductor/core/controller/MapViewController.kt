package com.mapconductor.core.controller

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.marker.MarkerEntry
import com.mapconductor.core.spherical.haversineDistance
import kotlinx.coroutines.CoroutineScope
import kotlin.math.pow

interface MapViewController {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope

    suspend fun addMarkers(markerList: List<MarkerEntry>)

    suspend fun clearOverlays()

    fun toScreenOffset(position: IGeoPoint): Offset?
}

abstract class BaseMapViewController : MapViewController {
    protected fun zoomToMetersPerPixel(zoom: Double): Double {
        val earthCircumference = 40075016.686
        val tileSize = 256
        return earthCircumference / (tileSize * 2.0.pow(zoom))
    }

    protected fun findMarkerFromPoint(
        markerOverlayManager: MarkerOverlayManager<*>,
        position: IGeoPoint,
        zoom: Double,
        tolerance: Double,
    ): MarkerEntry? {
        val meterInMapPixel = zoomToMetersPerPixel(zoom)
        val radius = tolerance * meterInMapPixel

        val entry = markerOverlayManager.markerManager.findNearest(position) ?: return null

        val distance = haversineDistance(position, entry.point)
        return if (distance <= radius) {
            entry
        } else {
            null
        }
    }
}
