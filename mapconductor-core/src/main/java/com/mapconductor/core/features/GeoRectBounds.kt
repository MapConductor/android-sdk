package com.mapconductor.core.features

class GeoRectBounds(
    southWest: GeoPoint? = null,
    northEast: GeoPoint? = null,
) {
    private var _southWest: GeoPoint? = southWest
    private var _northEast: GeoPoint? = northEast

    val isEmpty: Boolean
        get() = _southWest == null || _northEast == null

    val southWest: GeoPoint?
        get() = _southWest

    val northEast: GeoPoint?
        get() = _northEast

    fun extend(point: GeoPointInterface) {
        val position = GeoPoint.from(point).wrap() as GeoPoint

        when {
            // 初期化
            _southWest == null && _northEast == null -> {
                _southWest = position
                _northEast = position
                return
            }

            // southWest のみ存在
            _southWest != null && _northEast == null -> {
                val sw = _southWest!!
                val south = minOf(sw.latitude, position.latitude)
                val north = maxOf(sw.latitude, position.latitude)
                val west = minOf(sw.longitude, position.longitude)
                val east = maxOf(sw.longitude, position.longitude)

                _southWest = GeoPoint(south, west)
                _northEast = GeoPoint(north, east)
                return
            }

            // northEast のみ存在
            _southWest == null && _northEast != null -> {
                val ne = _northEast!!
                val south = minOf(ne.latitude, position.latitude)
                val north = maxOf(ne.latitude, position.latitude)
                val west = minOf(ne.longitude, position.longitude)
                val east = maxOf(ne.longitude, position.longitude)

                _southWest = GeoPoint(south, west)
                _northEast = GeoPoint(north, east)
                return
            }

            else -> {
                val south = minOf(position.latitude, _southWest!!.latitude)
                val north = maxOf(position.latitude, _northEast!!.latitude)

                var west = _southWest!!.longitude
                var east = _northEast!!.longitude

                if (west > 0 && east < 0) {
                    if (position.longitude > 0) {
                        west = minOf(position.longitude, west)
                    } else {
                        east = maxOf(position.longitude, east)
                    }
                } else {
                    west = minOf(position.longitude, _southWest!!.longitude)
                    east = maxOf(position.longitude, _northEast!!.longitude)
                }

                // Ensure longitudinal span uses the minimal arc (handle antimeridian)
                val span = ((east - west + 360) % 360)
                if (span > 180.0) {
                    // Flip to crossing-dateline representation so that west > east
                    val newWest = east
                    val newEast = west
                    west = newWest
                    east = newEast
                }

                _southWest = GeoPoint(south, west)
                _northEast = GeoPoint(north, east)
            }
        }
    }

    private fun distanceEast(
        lon1: Double,
        lon2: Double,
    ): Double {
        val distance = (lon2 - lon1 + 360) % 360
        return if (distance <= 180) distance else 360 - distance
    }

    private fun distanceWest(
        lon1: Double,
        lon2: Double,
    ): Double {
        val distance = (lon1 - lon2 + 360) % 360
        return if (distance <= 180) distance else 360 - distance
    }

    private fun containsLongitude(
        lon: Double,
        west: Double,
        east: Double,
    ): Boolean =
        if (west <= east) {
            lon in west..east
        } else {
            lon >= west || lon <= east
        }

    fun contains(point: GeoPointInterface): Boolean {
        if (isEmpty) return false

        val wrappedPoint = GeoPoint.from(point).wrap()
        val sw = _southWest!!.wrap()
        val ne = _northEast!!.wrap()

        val withinLat = wrappedPoint.latitude in sw.latitude..ne.latitude
        val withinLng = containsLongitude(wrappedPoint.longitude, sw.longitude, ne.longitude)

        return withinLat && withinLng
    }

    val center: GeoPoint?
        get() {
            if (isEmpty) return null

            val sw = _southWest!!.wrap()
            val ne = _northEast!!.wrap()

            val centerLat = (sw.latitude + ne.latitude) / 2.0

            val lng1 = sw.longitude
            val lng2 = ne.longitude
            val centerLng =
                if (lng1 <= lng2) {
                    (lng1 + lng2) / 2.0
                } else {
                    val centerLongitude = (lng1 + (lng2 + 360)) / 2.0
                    if (centerLongitude > 180) centerLongitude - 360 else centerLongitude
                }

            return GeoPoint(centerLat, centerLng)
        }

    fun union(other: GeoRectBounds): GeoRectBounds {
        if (other.isEmpty) return this
        if (this.isEmpty) {
            this._southWest = other.southWest
            this._northEast = other.northEast
            return this
        }
        val newBounds =
            GeoRectBounds(
                southWest = this.southWest,
                northEast = this.northEast,
            )

        newBounds.extend(other._southWest!!.wrap())
        newBounds.extend(other._northEast!!.wrap())
        return newBounds
    }

    fun toSpan(): GeoPoint? {
        if (isEmpty) return null

        val sw = _southWest!!.wrap()
        val ne = _northEast!!.wrap()

        val latSpan = ne.latitude - sw.latitude
        val lngSpan = ((ne.longitude - sw.longitude + 360) % 360).takeIf { it != 0.0 } ?: 360.0

        return GeoPoint(latSpan, lngSpan)
    }

    fun toUrlValue(precision: Int = 6): String {
        if (isEmpty) return "1.0,180.0,-1.0,-180.0"

        val sw = _southWest!!.wrap()
        val ne = _northEast!!.wrap()

        fun Double.toFixed(p: Int): String = "%.${p}f".format(this)

        return listOf(
            sw.latitude.toFixed(precision),
            sw.longitude.toFixed(precision),
            ne.latitude.toFixed(precision),
            ne.longitude.toFixed(precision),
        ).joinToString(",")
    }

    /**
     * Returns a new bounds expanded by the given degrees in latitude/longitude.
     * Positive pads expand outward in all directions. Handles antimeridian safely.
     */
    fun expandedByDegrees(
        latPad: Double,
        lonPad: Double,
    ): GeoRectBounds {
        if (isEmpty) return this

        val sw = _southWest!!.wrap()
        val ne = _northEast!!.wrap()

        val south = (sw.latitude - latPad).coerceIn(-90.0, 90.0)
        val north = (ne.latitude + latPad).coerceIn(-90.0, 90.0)

        fun norm(lon: Double): Double = (((lon + 180.0) % 360.0 + 360.0) % 360.0) - 180.0

        var west = norm(sw.longitude - lonPad)
        var east = norm(ne.longitude + lonPad)

        // Keep minimal longitudinal arc representation
        val span = ((east - west + 360) % 360)
        if (span > 180.0) {
            val newWest = east
            val newEast = west
            west = newWest
            east = newEast
        }

        return GeoRectBounds(
            southWest = GeoPoint(south, west),
            northEast = GeoPoint(north, east),
        )
    }

    fun intersects(other: GeoRectBounds): Boolean {
        if (this.isEmpty || other.isEmpty) return false

        val sw1 = this._southWest!!.wrap()
        val ne1 = this._northEast!!.wrap()
        val sw2 = other._southWest!!.wrap()
        val ne2 = other._northEast!!.wrap()

        // Latitude overlap (simple interval intersection)
        val epsilon = 1e-9
        val latOverlap =
            ne1.latitude >= sw2.latitude - epsilon &&
                ne2.latitude >= sw1.latitude - epsilon
        if (!latOverlap) {
            return false
        }

        fun norm(lon: Double): Double = (((lon + 180.0) % 360.0 + 360.0) % 360.0) - 180.0

        // Normalize longitudes to [-180, 180] for robustness
        val w1 = norm(sw1.longitude)
        val e1 = norm(ne1.longitude)
        val w2 = norm(sw2.longitude)
        val e2 = norm(ne2.longitude)

        // Longitude overlap: represent each bounds as up to two intervals to handle antimeridian
        fun lonIntervals(
            west: Double,
            east: Double,
        ): List<Pair<Double, Double>> =
            if (west <= east) {
                val span = east - west
                if (span <= 180.0) {
                    listOf(west to east)
                } else {
                    // Large span means minimal interval crosses the dateline
                    listOf(west to 180.0, -180.0 to east)
                }
            } else {
                // Crosses the antimeridian: [west, 180] U [-180, east]
                listOf(west to 180.0, -180.0 to east)
            }

        val intervals1 = lonIntervals(w1, e1)
        val intervals2 = lonIntervals(w2, e2)

        // Check if any pair of intervals overlaps (inclusive)
        for ((aStart, aEnd) in intervals1) {
            for ((bStart, bEnd) in intervals2) {
                val overlap = aStart <= bEnd && aEnd >= bStart
                if (overlap) return true
            }
        }
        return false
    }

    override fun toString(): String =
        if (isEmpty) {
            "((1, 180), (-1, -180))"
        } else {
            "((${_southWest!!.latitude}, ${_southWest!!.longitude}), (${_northEast!!.latitude}, ${_northEast!!.longitude}))"
        }

    fun equals(other: GeoRectBounds): Boolean = this.southWest == other.southWest && this.northEast == other.northEast
}
