package com.mapconductor.mapbox.polyline

import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapconductor.core.polyline.PolylineEntity
import com.mapconductor.mapbox.MapboxActualPolyline

class MapboxPolylineLayer(
    val sourceId: String,
    val layerId: String,
) {
    object Prop {
        const val STROKE_COLOR = "strokeColor"
        const val STROKE_WIDTH = "strokeWidth"
    }

    val source = geoJsonSource(sourceId)
    val layer =
        lineLayer(layerId, sourceId) {
            lineJoin(LineJoin.ROUND)
            lineCap(LineCap.ROUND)
            lineColor(
                get {
                    literal(Prop.STROKE_COLOR)
                },
            )
            lineWidth(
                get {
                    literal(Prop.STROKE_WIDTH)
                },
            )
        }

    fun draw(entities: List<PolylineEntity<MapboxActualPolyline>>) {
        val features = entities.map { it.polyline }
        source.featureCollection(
            FeatureCollection.fromFeatures(features),
        )
    }
}
