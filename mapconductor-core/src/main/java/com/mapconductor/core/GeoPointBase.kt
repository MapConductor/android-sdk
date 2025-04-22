package com.mapconductor.core

interface GeoPointInterface {
    val latitude: Double
    val longitude: Double
    val altitude: Double?
    fun toUrlValue(precision: Int = 6): String
}

open class GeoPointBase constructor(
    override val latitude: Double,
    override val longitude: Double,
    override val altitude: Double = 0.0,
) : GeoPointInterface {

    override fun toUrlValue(precision: Int): String {
        return "${latitude.toFixed(precision)},${longitude.toFixed(precision)},${altitude.toFixed(precision)}"
    }
}
