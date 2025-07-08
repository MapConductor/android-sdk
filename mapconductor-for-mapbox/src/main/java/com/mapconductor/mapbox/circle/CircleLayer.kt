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
    val layer = CircleLayer(layerId, sourceId).apply {
        circleRadius(Expression.get("radius"))
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
