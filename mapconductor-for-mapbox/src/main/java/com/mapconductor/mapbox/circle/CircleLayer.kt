package com.mapconductor.mapbox.circle

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapconductor.core.circle.CircleEntity
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.rgb

class CircleLayerWrapper(
    val sourceId: String,
    val layerId: String,
) {
    object Prop {
        const val RADIUS = "radius"
        const val FILL_ALPHA = "fillAlpha"
        const val FILL_COLOR_RED = "fillColorRed"
        const val FILL_COLOR_GREEN = "fillColorGreen"
        const val FILL_COLOR_BLUE = "fillColorBlue"
        const val STROKE_ALPHA = "strokeAlpha"
        const val STROKE_COLOR_RED = "strokeColorRed"
        const val STROKE_COLOR_GREEN = "strokeColorGreen"
        const val STROKE_COLOR_BLUE = "strokeColorBlue"
        const val STROKE_WIDTH = "strokeWidth"
    }

    val layer = circleLayer(layerId, sourceId) {
        circleRadius(get { literal(Prop.RADIUS) })
        circleColor(rgb {
            get { literal(Prop.FILL_COLOR_RED) }
            get { literal(Prop.FILL_COLOR_GREEN) }
            get { literal(Prop.FILL_COLOR_BLUE) }
        })
        circleOpacity(get { literal(Prop.FILL_ALPHA) })
        circleStrokeOpacity(get { literal(Prop.STROKE_ALPHA) })
        circleStrokeColor(rgb {
            get { literal(Prop.STROKE_COLOR_RED) }
            get { literal(Prop.STROKE_COLOR_BLUE) }
            get { literal(Prop.STROKE_COLOR_GREEN) }
        })
        circleStrokeWidth(get { literal(Prop.STROKE_WIDTH) })
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
