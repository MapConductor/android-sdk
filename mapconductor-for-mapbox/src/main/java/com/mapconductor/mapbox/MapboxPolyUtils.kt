package com.mapconductor.mapbox

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Polygon as MBPolygon
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.normalize
import com.mapconductor.core.spherical.createInterpolatePoints
import com.mapconductor.core.spherical.createLinearInterpolatePoints
import com.mapconductor.core.spherical.splitByMeridian
import com.mapconductor.mapbox.polygon.MapboxPolygonLayer
import com.mapconductor.mapbox.polyline.MapboxPolylineLayer

internal fun createMapboxLines(
    id: String,
    points: List<GeoPointInterface>,
    geodesic: Boolean,
    strokeColor: Color,
    strokeWidth: Dp,
    zIndex: Int = 0,
): List<Feature> {
    val geoPoints: List<GeoPointInterface> =
        when (geodesic) {
            true -> createInterpolatePoints(points)
            false -> createLinearInterpolatePoints(points)
        }.map { it.normalize() }

    return splitByMeridian(geoPoints, geodesic).mapIndexed { index, linePoints ->
        val points = linePoints.map { GeoPoint.from(it).toPoint() }
        val id = "polyline-$id-$index"

        return@mapIndexed Feature.fromGeometry(
            LineString.fromLngLats(points),
            JsonObject().apply {
                addProperty(MapboxPolylineLayer.Prop.STROKE_COLOR, strokeColor.toMapboxColorString())
                addProperty(MapboxPolylineLayer.Prop.STROKE_WIDTH, strokeWidth.value)
                addProperty("zIndex", zIndex)
                addProperty("id", id)
            },
            id,
        )
    }
}

internal fun createMapboxPolygons(
    id: String,
    points: List<GeoPointInterface>,
    holes: List<List<GeoPointInterface>> = emptyList(),
    geodesic: Boolean,
    fillColor: Color,
    zIndex: Int,
): List<Feature> {
    val geoPoints: List<GeoPointInterface> =
        when (geodesic) {
            true -> createInterpolatePoints(points)
            false -> createLinearInterpolatePoints(points)
        }.map { it.normalize() }

    val outerRings = splitByMeridian(geoPoints, geodesic)

    // If the outer ring is split by the antimeridian, keep the current behavior for now.
    // (Holes would need to be split and re-associated per split polygon piece.)
    val includeHoles = holes.isNotEmpty() && outerRings.size == 1

    return outerRings.mapIndexed { index, ringPoints ->
        val pts = ringPoints.map { GeoPoint.from(it).toPoint() }
        val closed = if (pts.first() != pts.last()) pts + pts.first() else pts
        val fid = "polygon-$id-$index"
        val rings = if (includeHoles) listOf(closed) + holesToRings(holes, geodesic) else listOf(closed)

        Feature.fromGeometry(
            MBPolygon.fromLngLats(rings),
            JsonObject().apply {
                addProperty(MapboxPolygonLayer.Prop.FILL_COLOR, fillColor.toMapboxColorString())
                addProperty("zIndex", zIndex)
                addProperty("id", fid)
            },
            fid,
        )
    }
}

private fun holesToRings(
    holes: List<List<GeoPointInterface>>,
    geodesic: Boolean,
): List<List<com.mapbox.geojson.Point>> {
    if (holes.isEmpty()) return emptyList()
    return holes.mapNotNull { hole ->
        val holeGeoPoints: List<GeoPointInterface> =
            when (geodesic) {
                true -> createInterpolatePoints(hole)
                false -> createLinearInterpolatePoints(hole)
            }.map { it.normalize() }

        val pts = holeGeoPoints.map { GeoPoint.from(it).toPoint() }
        if (pts.size < 3) return@mapNotNull null
        val closed = if (pts.first() != pts.last()) pts + pts.first() else pts
        if (closed.size < 4) return@mapNotNull null
        closed
    }
}
