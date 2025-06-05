package com.mapconductor.core.map

interface MapPaddings {
    val top: Double
    val left: Double
    val bottom: Double
    val right: Double
}

open class MapPaddingsImpl
    @JvmOverloads
    constructor(
        override val top: Double = 0.0,
        override val left: Double = 0.0,
        override val bottom: Double = 0.0,
        override val right: Double = 0.0,
    ) : MapPaddings {
        companion object {
            val Zeros: MapPaddingsImpl = MapPaddingsImpl(0.0, 0.0, 0.0, 0.0)

            fun from(paddingsImpl: MapPaddings) =
                when (paddingsImpl) {
                    is MapPaddingsImpl -> paddingsImpl
                    else ->
                        MapPaddingsImpl(
                            top = paddingsImpl.top,
                            left = paddingsImpl.left,
                            bottom = paddingsImpl.bottom,
                            right = paddingsImpl.right,
                        )
                }
        }
    }
