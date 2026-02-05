package com.mapconductor.maplibre

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.google.gson.JsonObject
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.normalize
import com.mapconductor.core.spherical.createInterpolatePoints
import com.mapconductor.core.spherical.createLinearInterpolatePoints
import com.mapconductor.core.spherical.splitByMeridian
import com.mapconductor.maplibre.polygon.MapLibrePolygonLayer
import com.mapconductor.maplibre.polyline.MapLibrePolylineLayer
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Polygon as GLPolygon

internal fun createMapLibreLines(
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
        val pts = linePoints.map { GeoPoint.from(it).toPoint() }
        val fid = "polyline-$id-$index"

        Feature.fromGeometry(
            LineString.fromLngLats(pts),
            JsonObject().apply {
                addProperty(MapLibrePolylineLayer.Prop.STROKE_COLOR, strokeColor.toMapLibreColorString())
                addProperty(MapLibrePolylineLayer.Prop.STROKE_WIDTH, strokeWidth.value)
                addProperty("zIndex", zIndex)
                addProperty("id", fid)
            },
            fid,
        )
    }
}

fun Color.toMapLibreColorString(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    val alpha = this.alpha
    return "rgba($red, $green, $blue, $alpha)"
}

internal fun createMapLibrePolygons(
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
    val includeHoles = holes.isNotEmpty() && outerRings.size == 1

    fun holeRings(): List<List<org.maplibre.geojson.Point>> =
        holes.mapNotNull { hole ->
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

    // Split to avoid antimeridian artifacts and produce multiple polygons if needed
    return outerRings.mapIndexed { index, ringPoints ->
        val pts = ringPoints.map { GeoPoint.from(it).toPoint() }
        // Ensure closed ring
        val closed = if (pts.first() != pts.last()) pts + pts.first() else pts
        val fid = "polygon-$id-$index"
        val rings = if (includeHoles) listOf(closed) + holeRings() else listOf(closed)

        Feature.fromGeometry(
            GLPolygon.fromLngLats(rings),
            JsonObject().apply {
                addProperty(MapLibrePolygonLayer.Prop.FILL_COLOR, fillColor.toMapLibreColorString())
                addProperty("zIndex", zIndex)
                addProperty("id", fid)
            },
            fid,
        )
    }
}
