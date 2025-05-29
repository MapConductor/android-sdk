package com.mapconductor.core.projection

import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.Offset
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.tan
import kotlin.math.atan

object WebMercator: Projection {
    override fun project(position: IGeoPoint): Offset {
        val x = position.longitude * 20037508.34 / 180
        val y = ln(tan((90 + position.latitude) * Math.PI / 360)) * 20037508.34 / Math.PI
        return Offset(x, y)
    }

    override fun unproject(point: Offset): IGeoPoint {
        val longitude = point.x * 180 / 20037508.34
        val latitude = 180 / Math.PI * (2 * atan(exp(point.y * Math.PI / 20037508.34)) - Math.PI / 2)
        return object : IGeoPoint {
            override val latitude: Double = latitude
            override val longitude: Double = longitude
            override val altitude: Double? = null
        }
    }

}