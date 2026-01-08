package com.mapconductor.core.projection

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPointInterface

interface ProjectionInterface {
    fun project(position: GeoPointInterface): Offset

    fun unproject(point: Offset): GeoPointInterface
}
