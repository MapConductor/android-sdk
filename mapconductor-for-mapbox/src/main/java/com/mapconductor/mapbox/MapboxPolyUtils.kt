package com.mapconductor.mapbox

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapconductor.core.createInterpolatePoints
import com.mapconductor.core.createLinearInterpolatePoints
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.features.normalize
import com.mapconductor.core.splitByMeridian
import com.mapconductor.mapbox.polyline.MapboxPolylineLayer

internal fun createMapboxLines(
    id: String,
    points: List<IGeoPoint>,
    geodesic: Boolean,
    strokeColor: Color,
    strokeWidth: Dp,
): List<Feature> {
    val geoPoints: List<IGeoPoint> =
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
                addProperty("id", id)
            },
            id,
        )
    }
}
