package com.mapconductor.core.projection

import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.Offset

interface Projection {
    fun project(position: IGeoPoint) : Offset
    fun unproject(point: Offset): IGeoPoint
}