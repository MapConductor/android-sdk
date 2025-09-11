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

    fun extend(point: IGeoPoint) {
        val position = GeoPoint.from(point)

        when {
            // まだ何もない：両方に同じ点を入れて初期化
            _southWest == null && _northEast == null -> {
                _southWest = position
                _northEast = position
                return
            }

            // southWest だけある：既存点と position の2点から SW/NE を決める
            _southWest != null && _northEast == null -> {
                val sw = _southWest!!
                val south = minOf(sw.latitude, position.latitude)
                val north = maxOf(sw.latitude, position.latitude)
                // 1点ずつなので子午線跨ぎの判定は不要。単純に min/max でOK
                val west = minOf(sw.longitude, position.longitude)
                val east = maxOf(sw.longitude, position.longitude)

                _southWest = GeoPoint(south, west)
                _northEast = GeoPoint(north, east)
                return
            }

            // northEast だけある：既存点と position の2点から SW/NE を決める
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
                // どちらもある：従来ロジック（子午線跨ぎ考慮）＋ 緯度/経度の参照を修正
                val south = minOf(position.latitude, _southWest!!.latitude)
                val north = maxOf(position.latitude, _northEast!!.latitude)

                var west = _southWest!!.longitude
                var east = _northEast!!.longitude

                if (west > 0 && east < 0) {
                    // すでに経度が + と - に分かれている＝日付変更線跨ぎの矩形
                    if (position.longitude > 0) {
                        west = minOf(position.longitude, west)
                    } else {
                        east = maxOf(position.longitude, east)
                    }
                } else {
                    // 通常ケース：単純に min/max
                    west = minOf(position.longitude, _southWest!!.longitude)
                    east = maxOf(position.longitude, _northEast!!.longitude)
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
        val d = (lon2 - lon1 + 360) % 360
        return if (d <= 180) d else 360 - d
    }

    private fun distanceWest(
        lon1: Double,
        lon2: Double,
    ): Double {
        val d = (lon1 - lon2 + 360) % 360
        return if (d <= 180) d else 360 - d
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

    fun contains(point: IGeoPoint): Boolean {
        if (isEmpty) return false

        val p = GeoPoint.from(point)
        val sw = _southWest!!
        val ne = _northEast!!

        val withinLat = p.latitude in sw.latitude..ne.latitude
        val withinLng = containsLongitude(p.longitude, sw.longitude, ne.longitude)

        return withinLat && withinLng
    }

    val center: GeoPoint?
        get() {
            if (isEmpty) return null

            val sw = _southWest!!
            val ne = _northEast!!

            val centerLat = (sw.latitude + ne.latitude) / 2.0

            val lng1 = sw.longitude
            val lng2 = ne.longitude
            val centerLng =
                if (lng1 <= lng2) {
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
            this._southWest = other.southWest
            this._northEast = other.northEast
            return this
        }

        extend(other._southWest!!)
        extend(other._northEast!!)
        return this
    }

    fun toSpan(): GeoPoint? {
        if (isEmpty) return null

        val sw = _southWest!!
        val ne = _northEast!!

        val latSpan = ne.latitude - sw.latitude
        val lngSpan = ((ne.longitude - sw.longitude + 360) % 360).takeIf { it != 0.0 } ?: 360.0

        return GeoPoint(latSpan, lngSpan)
    }

    fun toUrlValue(precision: Int = 6): String {
        if (isEmpty) return "1.0,180.0,-1.0,-180.0"

        val sw = _southWest!!
        val ne = _northEast!!

        fun Double.toFixed(p: Int): String = "%.${p}f".format(this)

        return listOf(
            sw.latitude.toFixed(precision),
            sw.longitude.toFixed(precision),
            ne.latitude.toFixed(precision),
            ne.longitude.toFixed(precision),
        ).joinToString(",")
    }

    fun intersects(other: GeoRectBounds): Boolean {
        if (this.isEmpty || other.isEmpty) return false

        val sw1 = this._southWest!!
        val ne1 = this._northEast!!
        val sw2 = other._southWest!!
        val ne2 = other._northEast!!

        val latOverlap = sw1.latitude <= ne2.latitude && ne1.latitude >= sw2.latitude

        val lngOverlap =
            containsLongitude(sw2.longitude, sw1.longitude, ne1.longitude) ||
                containsLongitude(ne2.longitude, sw1.longitude, ne1.longitude)

        return latOverlap && lngOverlap
    }

    override fun toString(): String =
        if (isEmpty) {
            "((1, 180), (-1, -180))"
        } else {
            "((${_southWest!!.latitude}, ${_southWest!!.longitude}), (${_northEast!!.latitude}, ${_northEast!!.longitude}))"
        }

    fun equalsTo(other: GeoRectBounds): Boolean = this.southWest == other.southWest && this.northEast == other.northEast
}
