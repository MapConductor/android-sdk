package com.mapconductor.core

interface MapCameraPositionImpl {
    val target: GeoPointImpl
    val zoom: Double
    val bearing: Double
    val tilt: Double
    val paddings: MapPaddingsImpl
}

open class MapCameraPositionBase @JvmOverloads constructor(
    override val target: GeoPointImpl,
    override val zoom: Double = 0.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddingsImpl = MapPaddings.Zeros,
): MapCameraPositionImpl