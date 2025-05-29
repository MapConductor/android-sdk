package com.mapconductor.core.features

import kotlin.math.max
import kotlin.math.min


data class GeoRectBounds(
    var southWest: GeoPoint? = null,
    var northEast: GeoPoint? = null
) {
    val isEmpty: Boolean
        get() = southWest == null || northEast == null

    fun extend(point: IGeoPoint) {
        val target = GeoPoint.from(point)

        if (isEmpty) {
            southWest = target
            northEast = target
            return
        }

        val sw = southWest!!
        val ne = northEast!!

        val south = min(sw.latitude, target.latitude)
        val north = max(ne.latitude, target.latitude)

        var west = sw.longitude
        var east = ne.longitude

        val lon = target.longitude

        if (!containsLongitude(lon, west, east)) {
            val distToEast = distanceEast(lon, east)
            val distToWest = distanceWest(west, lon)

            if (distToEast < distToWest) {
                east = lon
            } else {
                west = lon
            }
        }

        southWest = GeoPoint(south, west)
        northEast = GeoPoint(north, east)
    }

    private fun distanceEast(lon1: Double, lon2: Double): Double {
        val d = (lon2 - lon1 + 360) % 360
        return if (d <= 180) d else 360 - d
    }

    private fun distanceWest(lon1: Double, lon2: Double): Double {
        val d = (lon1 - lon2 + 360) % 360
        return if (d <= 180) d else 360 - d
    }

    private fun containsLongitude(lon: Double, west: Double, east: Double): Boolean {
        return if (west <= east) {
            lon in west..east
        } else {
            lon >= west || lon <= east
        }
    }

    fun contains(point: IGeoPoint): Boolean {
        if (isEmpty) return false

        val p = GeoPoint.from(point)
        val sw = southWest!!
        val ne = northEast!!

        val withinLat = p.latitude in sw.latitude..ne.latitude
        val withinLng = containsLongitude(p.longitude, sw.longitude, ne.longitude)

        return withinLat && withinLng
    }

    fun getCenter(): GeoPoint? {
        if (isEmpty) return null

        val sw = southWest!!
        val ne = northEast!!

        val centerLat = (sw.latitude + ne.latitude) / 2.0

        val lng1 = sw.longitude
        val lng2 = ne.longitude
        val centerLng = if (lng1 <= lng2) {
            (lng1 + lng2) / 2.0
        } else {
            val mid = (lng1 + (lng2 + 360)) / 2.0
            if (mid > 180) mid - 360 else mid
        }

        return GeoPoint(centerLat, centerLng)
    }

    fun union(other: GeoRectBounds): GeoRectBounds {
        if (other.isEmpty) return this
        if (this.isEmpty) {
            this.southWest = other.southWest
            this.northEast = other.northEast
            return this
        }

        extend(other.southWest!!)
        extend(other.northEast!!)
        return this
    }

    fun toSpan(): GeoPoint? {
        if (isEmpty) return null

        val sw = southWest!!
        val ne = northEast!!

        val latSpan = ne.latitude - sw.latitude
        val lngSpan = ((ne.longitude - sw.longitude + 360) % 360).takeIf { it != 0.0 } ?: 360.0

        return GeoPoint(latSpan, lngSpan)
    }

    fun toUrlValue(precision: Int = 6): String {
        if (isEmpty) return "1.0,180.0,-1.0,-180.0"

        val sw = southWest!!
        val ne = northEast!!

        fun Double.toFixed(p: Int): String = "%.${p}f".format(this)

        return listOf(
            sw.latitude.toFixed(precision),
            sw.longitude.toFixed(precision),
            ne.latitude.toFixed(precision),
            ne.longitude.toFixed(precision)
        ).joinToString(",")
    }

    fun intersects(other: GeoRectBounds): Boolean {
        if (this.isEmpty || other.isEmpty) return false

        val sw1 = this.southWest!!
        val ne1 = this.northEast!!
        val sw2 = other.southWest!!
        val ne2 = other.northEast!!

        val latOverlap = sw1.latitude <= ne2.latitude && ne1.latitude >= sw2.latitude

        val lngOverlap = containsLongitude(sw2.longitude, sw1.longitude, ne1.longitude) ||
                containsLongitude(ne2.longitude, sw1.longitude, ne1.longitude)

        return latOverlap && lngOverlap
    }

    override fun toString(): String {
        return if (isEmpty) {
            "((1, 180), (-1, -180))"
        } else {
            "((${southWest!!.latitude}, ${southWest!!.longitude}), (${northEast!!.latitude}, ${northEast!!.longitude}))"
        }
    }

    fun equalsTo(other: GeoRectBounds): Boolean {
        return this.southWest == other.southWest && this.northEast == other.northEast
    }
}
