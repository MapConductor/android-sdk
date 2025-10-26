package com.mapconductor.maplibre

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.google.gson.JsonObject
import com.mapconductor.core.createInterpolatePoints
import com.mapconductor.core.createLinearInterpolatePoints
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.normalize
import com.mapconductor.core.splitByMeridian
import com.mapconductor.maplibre.polygon.MapLibrePolygonLayer
import com.mapconductor.maplibre.polyline.MapLibrePolylineLayer
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Polygon as GLPolygon

internal fun createMapLibreLines(
    id: String,
    points: List<GeoPoint>,
    geodesic: Boolean,
    strokeColor: Color,
    strokeWidth: Dp,
    zIndex: Int = 0,
): List<Feature> {
    val geoPoints: List<GeoPoint> =
        when (geodesic) {
            true -> createInterpolatePoints(points)
            false -> createLinearInterpolatePoints(points)
        }.map { it.normalize() }

    return splitByMeridian(geoPoints, geodesic).mapIndexed { index, linePoints ->
        val pts = linePoints.map { GeoPointImpl.from(it).toPoint() }
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
    points: List<GeoPoint>,
    geodesic: Boolean,
    fillColor: Color,
    zIndex: Int,
): List<Feature> {
    val geoPoints: List<GeoPoint> =
        when (geodesic) {
            true -> createInterpolatePoints(points)
            false -> createLinearInterpolatePoints(points)
        }.map { it.normalize() }

    // Split to avoid antimeridian artifacts and produce multiple polygons if needed
    return splitByMeridian(geoPoints, geodesic).mapIndexed { index, ringPoints ->
        val pts = ringPoints.map { GeoPointImpl.from(it).toPoint() }
        // Ensure closed ring
        val closed = if (pts.first() != pts.last()) pts + pts.first() else pts
        val fid = "polygon-$id-$index"

        Feature.fromGeometry(
            GLPolygon.fromLngLats(listOf(closed)),
            JsonObject().apply {
                addProperty(MapLibrePolygonLayer.Prop.FILL_COLOR, fillColor.toMapLibreColorString())
                addProperty("zIndex", zIndex)
                addProperty("id", fid)
            },
            fid,
        )
    }
}
