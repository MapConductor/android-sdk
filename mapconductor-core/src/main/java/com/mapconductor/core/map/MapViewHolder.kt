package com.mapconductor.core.map

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint

interface MapViewHolder<out ActualMapViewType, out ActualMapType> {
    val mapView: ActualMapViewType
    val map: ActualMapType

    fun toScreenOffset(position: IGeoPoint): Offset?

    suspend fun fromScreenOffset(offset: Offset): GeoPoint?

    fun fromScreenOffsetSync(offset: Offset): GeoPoint? = null
}
