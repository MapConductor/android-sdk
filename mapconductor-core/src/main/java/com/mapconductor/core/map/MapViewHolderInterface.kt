package com.mapconductor.core.map

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface

interface MapViewHolderInterface<ActualMapView, ActualMap> {
    val mapView: ActualMapView
    val map: ActualMap

    fun toScreenOffset(position: GeoPointInterface): Offset?

    suspend fun fromScreenOffset(offset: Offset): GeoPoint?

    fun fromScreenOffsetSync(offset: Offset): GeoPoint? = null
}
