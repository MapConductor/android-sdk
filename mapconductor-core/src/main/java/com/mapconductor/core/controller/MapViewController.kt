package com.mapconductor.core.controller

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.marker.MarkerEntry
import kotlinx.coroutines.CoroutineScope

interface MapViewController {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope
    suspend fun addMarkers(markerList : List<MarkerEntry>)
    suspend fun clearOverlays()
    fun toScreenOffset(position: IGeoPoint): Offset?
}
