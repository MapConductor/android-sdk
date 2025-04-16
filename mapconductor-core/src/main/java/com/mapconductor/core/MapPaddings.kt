package com.mapconductor.core

interface MapPaddingsImpl {
    val top: Double
    val left: Double
    val bottom: Double
    val right: Double
}
open class MapPaddings @JvmOverloads constructor(
    override val top: Double = 0.0,
    override val left: Double = 0.0,
    override val bottom: Double = 0.0,
    override val right: Double = 0.0,
): MapPaddingsImpl {
    companion object {
        val Zeros: MapPaddings = MapPaddings(0.0, 0.0, 0.0, 0.0)

        fun fromImpl(paddingsImpl: MapPaddingsImpl) = when(paddingsImpl) {
            is MapPaddings -> paddingsImpl
            else -> MapPaddings(
                top = paddingsImpl.top,
                left = paddingsImpl.left,
                bottom = paddingsImpl.bottom,
                right = paddingsImpl.right,
            )
        }
    }
}