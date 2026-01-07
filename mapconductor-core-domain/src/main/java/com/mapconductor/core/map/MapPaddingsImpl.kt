package com.mapconductor.core.map

interface MapPaddingsInterface {
    val top: Double
    val left: Double
    val bottom: Double
    val right: Double
}

open class MapPaddings
    @JvmOverloads
    constructor(
        override val top: Double = 0.0,
        override val left: Double = 0.0,
        override val bottom: Double = 0.0,
        override val right: Double = 0.0,
    ) : MapPaddingsInterface {
        companion object {
            val Zeros: MapPaddings = MapPaddings(0.0, 0.0, 0.0, 0.0)

            fun from(paddings: MapPaddingsInterface) =
                when (paddings) {
                    is MapPaddings -> paddings
                    else ->
                        MapPaddings(
                            top = paddings.top,
                            left = paddings.left,
                            bottom = paddings.bottom,
                            right = paddings.right,
                        )
                }
        }
    }
