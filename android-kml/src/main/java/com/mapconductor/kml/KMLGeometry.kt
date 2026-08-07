package com.mapconductor.kml

sealed class KMLGeometry {
    data class Point(
        val longitude: Double,
        val latitude: Double,
    ) : KMLGeometry()

    data class MultiPoint(
        val points: List<Point>,
    ) : KMLGeometry()

    data class LineString(
        val coordinates: List<LonLat>,
    ) : KMLGeometry()

    data class MultiLineString(
        val lines: List<List<LonLat>>,
    ) : KMLGeometry()

    /**
     * Polygon rings in KML order: first ring is the exterior (outerBoundaryIs),
     * subsequent rings are holes (innerBoundaryIs).
     */
    data class Polygon(
        val rings: List<List<LonLat>>,
    ) : KMLGeometry()

    data class MultiPolygon(
        val polygons: List<List<List<LonLat>>>,
    ) : KMLGeometry()

    /**
     * Represents a KML `<MultiGeometry>` — an unordered collection of heterogeneous geometries.
     */
    data class GeometryCollection(
        val geometries: List<KMLGeometry>,
    ) : KMLGeometry()

    object Empty : KMLGeometry()
}

data class LonLat(
    val longitude: Double,
    val latitude: Double,
)
