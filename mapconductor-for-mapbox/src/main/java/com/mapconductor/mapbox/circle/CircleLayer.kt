package com.mapconductor.mapbox.circle

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapconductor.core.circle.CircleEntity

class CircleLayerWrapper(
    val sourceId: String,
    val layerId: String,
) {
    object Prop {
        const val RADIUS = "radius"
        const val FILL_ALPHA = "fillAlpha"
        const val FILL_COLOR = "fillColor"
        const val STROKE_ALPHA = "strokeAlpha"
        const val STROKE_COLOR = "strokeColor"
        const val STROKE_WIDTH = "strokeWidth"
    }

    val layer = CircleLayer(layerId, sourceId).apply {
        circleRadius(Expression.get(Prop.RADIUS))
        circleColor(Expression.get(Prop.FILL_COLOR))
        circleOpacity(Expression.get(Prop.FILL_ALPHA))
        circleStrokeOpacity(Expression.get(Prop.STROKE_ALPHA))
        circleStrokeColor(Expression.get(Prop.STROKE_COLOR))
        circleStrokeWidth(Expression.get(Prop.STROKE_WIDTH))
    }

    val source: GeoJsonSource =
        geoJsonSource(sourceId) {
            featureCollection(FeatureCollection.fromFeatures(emptyList()))
        }

    fun draw(entities: List<CircleEntity<Feature>>) {
        val features = entities.map { it.circle }
        source.featureCollection(
            FeatureCollection.fromFeatures(features),
        )
    }
}
