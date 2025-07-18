package com.mapconductor.mapbox.circle

import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapconductor.core.circle.CircleEntity
import com.mapconductor.mapbox.MapboxActualCircle

class MapboxCircleLayer(
    val sourceId: String,
    val layerId: String,
) {
    object Prop {
        const val RADIUS = "radius"
        const val FILL_COLOR = "fillColor"
        const val STROKE_COLOR = "strokeColor"
        const val STROKE_WIDTH = "strokeWidth"
    }

    val layer =
        circleLayer(layerId, sourceId) {
            circleRadius(get { literal(Prop.RADIUS) })
            circleColor(
                get { literal(Prop.FILL_COLOR) },
            )
            circleStrokeColor(
                get { literal(Prop.STROKE_COLOR) },
            )
            circleStrokeWidth(
                get { literal(Prop.STROKE_WIDTH) },
            )
        }

    val source: GeoJsonSource = geoJsonSource(sourceId)

    fun draw(entities: List<CircleEntity<MapboxActualCircle>>) {
        val features = entities.map { it.circle }
        source.featureCollection(
            FeatureCollection.fromFeatures(features),
        )
    }
}
