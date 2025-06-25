package com.mapconductor.core.controller

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.MarkerOverlayManagerImpl
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.spherical.haversineDistance
import kotlin.math.pow
import kotlinx.coroutines.CoroutineScope

interface MapViewController {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope
    val markerOverlayManager: MarkerOverlayManagerImpl<*>

    suspend fun addMarkers(data: List<MarkerState>)

    suspend fun updateMarker(state: MarkerState)

    suspend fun clearOverlays()

    fun toScreenOffset(position: IGeoPoint): Offset?

    suspend fun fromScreenOffset(offset: Offset): GeoPoint?
}

abstract class BaseMapViewController<ActualCamera> : MapViewController {
    var cameraMoveListener: (OnCameraMoveHandler<ActualCamera>)? = null
    var mapClickListener: OnMapEventHandler? = null
    var mapLongClickListener: OnMapEventHandler? = null
    var markerClickListener: OnMarkerEventHandler? = null
    var markerDragStartListener: OnMarkerEventHandler? = null
    var markerDragListener: OnMarkerEventHandler? = null
    var markerDragEndListener: OnMarkerEventHandler? = null

    protected fun zoomToMetersPerPixel(zoom: Double): Double {
        val earthCircumference = 40075016.686
        val tileSize = 256
        return earthCircumference / (tileSize * 2.0.pow(zoom))
    }

    protected fun findMarkerFromPoint(
        markerOverlayManager: MarkerOverlayManagerImpl<*>,
        position: IGeoPoint,
        zoom: Double,
        tolerance: Double,
    ): MarkerState? {
        val meterInMapPixel = zoomToMetersPerPixel(zoom)
        val radius = tolerance * meterInMapPixel
        val entity = markerOverlayManager.markerManager.findNearest(position) ?: return null

        val distance = haversineDistance(position, entity.state.position)
        return if (distance <= radius) {
            entity.state
        } else {
            null
        }
    }

    protected fun setDraggingState(
        markerState: MarkerState,
        dragging: Boolean,
    ) {
        markerState.isDragging = dragging
    }

}
