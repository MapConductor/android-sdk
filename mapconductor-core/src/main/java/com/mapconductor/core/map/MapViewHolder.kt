package com.mapconductor.core.map

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl

interface MapViewHolder<ActualMapView, ActualMap> {
    val mapView: ActualMapView
    val map: ActualMap

    fun toScreenOffset(position: GeoPoint): Offset?

    suspend fun fromScreenOffset(offset: Offset): GeoPointImpl?

    fun fromScreenOffsetSync(offset: Offset): GeoPointImpl? = null
}
