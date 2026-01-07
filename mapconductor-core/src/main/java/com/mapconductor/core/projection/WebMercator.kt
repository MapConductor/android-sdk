package com.mapconductor.core.projection

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.tan

object WebMercator : ProjectionInterface {
    override fun project(position: GeoPointInterface): Offset {
        val x = position.longitude * 20037508.34 / 180
        val y = ln(tan((90 + position.latitude) * Math.PI / 360)) * 20037508.34 / Math.PI
        return Offset(x.toFloat(), y.toFloat())
    }

    override fun unproject(point: Offset): GeoPointInterface {
        val longitude = point.x * 180 / 20037508.34
        val latitude = 180 / Math.PI * (2 * atan(exp(point.y * Math.PI / 20037508.34)) - Math.PI / 2)
        return object : GeoPointInterface {
            override val latitude: Double = latitude
            override val longitude: Double = longitude
            override val altitude: Double? = null

            override fun wrap(): GeoPointInterface = GeoPoint(latitude, longitude, altitude ?: 0.0).wrap()
        }
    }
}
