package com.mapconductor.core

interface GeoPointImpl {
    val latitude: Double
    val longitude: Double
    fun toUrlValue(precision: Int = 6): String
}

open class GeoPointBase constructor(
    override val latitude: Double,
    override val longitude: Double
) : GeoPointImpl {

    override fun toUrlValue(precision: Int): String {
        return "${latitude.toFixed(precision)},${longitude.toFixed(precision)}"
    }
}
