package com.mapconductor.core.projection

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.IGeoPoint

interface Projection {
    fun project(position: IGeoPoint) : Offset
    fun unproject(point: Offset): IGeoPoint
}