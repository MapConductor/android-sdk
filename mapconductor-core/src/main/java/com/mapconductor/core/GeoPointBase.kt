package com.mapconductor.core

interface GeoPointImpl {
    val latitude: Double;
    val longitude: Double;
    fun toUrlValue(precision: Int = 6): String
}

abstract class GeoPointBase: GeoPointImpl {

    override fun toUrlValue(precision: Int): String {
        return "${latitude.toFixed(precision)},${longitude.toFixed(precision)}"
    }
}
