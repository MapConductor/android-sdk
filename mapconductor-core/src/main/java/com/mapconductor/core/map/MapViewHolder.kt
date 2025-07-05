package com.mapconductor.core.map

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint

interface MapViewHolder<out TMapView, out TMap> {
    val mapView: TMapView
    val map: TMap

    fun toScreenOffset(position: IGeoPoint): Offset?

    suspend fun fromScreenOffset(offset: Offset): GeoPoint?

    fun fromScreenOffsetSync(offset: Offset): GeoPoint? = null
}
