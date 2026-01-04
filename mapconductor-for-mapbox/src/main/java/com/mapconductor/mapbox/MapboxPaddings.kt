package com.mapconductor.mapbox

import androidx.annotation.Keep
import com.mapbox.maps.EdgeInsets
import com.mapconductor.core.map.MapPaddingsInterface
import com.mapconductor.core.map.MapPaddings

interface IMapboxPaddingsInterface : MapPaddingsInterface {
    fun toEdgeInsects(): EdgeInsets
}

data class MapboxPaddings(
    override val top: Double,
    override val left: Double,
    override val bottom: Double,
    override val right: Double,
) : MapPaddings(top, left, bottom, right),
    IMapboxPaddingsInterface {
    override fun toEdgeInsects() =
        EdgeInsets(
            top,
            left,
            bottom,
            right,
        )

    companion object {
        val Zeros = MapboxPaddings(0.0, 0.0, 0.0, 0.0)

        fun from(paddings: MapPaddingsInterface? = null): MapboxPaddings? {
            if (paddings == null) return null

            return when (paddings) {
                is MapboxPaddings -> paddings
                else ->
                    MapboxPaddings(
                        top = paddings.top,
                        left = paddings.left,
                        bottom = paddings.bottom,
                        right = paddings.right,
                    )
            }
        }
    }
}

@Keep
fun EdgeInsets.toPaddings() = MapboxPaddings(top, left, bottom, right)
