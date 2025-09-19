package com.mapconductor.core.projection

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint

interface Projection {
    fun project(position: GeoPoint): Offset

    fun unproject(point: Offset): GeoPoint
}
