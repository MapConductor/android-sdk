package com.mapconductor.mapbox

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Polygon as MBPolygon
import com.mapconductor.core.createInterpolatePoints
import com.mapconductor.core.createLinearInterpolatePoints
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.features.normalize
import com.mapconductor.core.splitByMeridian
import com.mapconductor.mapbox.polyline.MapboxPolylineLayer
import com.mapconductor.mapbox.polygon.MapboxPolygonLayer

internal fun createMapboxLines(
    id: String,
    points: List<GeoPoint>,
    geodesic: Boolean,
    strokeColor: Color,
    strokeWidth: Dp,
): List<Feature> {
    val geoPoints: List<GeoPoint> =
        when (geodesic) {
            true -> createInterpolatePoints(points)
            false -> createLinearInterpolatePoints(points)
        }.map { it.normalize() }

    return splitByMeridian(geoPoints, geodesic).mapIndexed { index, linePoints ->
        val points = linePoints.map { GeoPointImpl.from(it).toPoint() }
        val id = "polyline-$id-$index"

        return@mapIndexed Feature.fromGeometry(
            LineString.fromLngLats(points),
            JsonObject().apply {
                addProperty(MapboxPolylineLayer.Prop.STROKE_COLOR, strokeColor.toMapboxColorString())
                addProperty(MapboxPolylineLayer.Prop.STROKE_WIDTH, strokeWidth.value)
                addProperty("id", id)
            },
            id,
        )
    }
}

internal fun createMapboxPolygons(
    id: String,
    points: List<GeoPoint>,
    geodesic: Boolean,
    fillColor: Color,
): List<Feature> {
    val geoPoints: List<GeoPoint> =
        when (geodesic) {
            true -> createInterpolatePoints(points)
            false -> createLinearInterpolatePoints(points)
        }.map { it.normalize() }

    return splitByMeridian(geoPoints, geodesic).mapIndexed { index, ringPoints ->
        val pts = ringPoints.map { GeoPointImpl.from(it).toPoint() }
        val closed = if (pts.first() != pts.last()) pts + pts.first() else pts
        val fid = "polygon-$id-$index"

        Feature.fromGeometry(
            MBPolygon.fromLngLats(listOf(closed)),
            JsonObject().apply {
                addProperty(MapboxPolygonLayer.Prop.FILL_COLOR, fillColor.toMapboxColorString())
                addProperty("id", fid)
            },
            fid,
        )
    }
}
