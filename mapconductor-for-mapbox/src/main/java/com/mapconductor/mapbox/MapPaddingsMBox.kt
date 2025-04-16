package com.mapconductor.mapbox

import androidx.annotation.Keep
import com.mapbox.maps.EdgeInsets
import com.mapconductor.core.MapPaddings
import com.mapconductor.core.MapPaddingsImpl

interface MapPaddingsMBoxImpl: MapPaddingsImpl {
    fun toEdgeInsects() : EdgeInsets
}

data class MapPaddingsMbox(
    override val top: Double,
    override val left: Double,
    override val bottom: Double,
    override val right: Double,
): MapPaddings(top, left, bottom, right), MapPaddingsMBoxImpl {

    override fun toEdgeInsects() = EdgeInsets(
        top,
        left,
        bottom,
        right,
    )

    companion object {
        val Zeros = MapPaddingsMbox(0.0, 0.0, 0.0, 0.0)

        fun fromImpl(paddingsImpl: MapPaddingsImpl) =
            when(paddingsImpl) {
                is MapPaddingsMbox -> paddingsImpl
                else -> MapPaddingsMbox(
                    top = paddingsImpl.top,
                    left = paddingsImpl.left,
                    bottom = paddingsImpl.bottom,
                    right = paddingsImpl.right,
                )
            }
    }
}

@Keep
fun EdgeInsets.toPaddings() = MapPaddingsMbox(top, left, bottom, right)
