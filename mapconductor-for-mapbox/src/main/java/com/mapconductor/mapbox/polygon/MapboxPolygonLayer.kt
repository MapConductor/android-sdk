package com.mapconductor.mapbox.polygon

import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.mapbox.MapboxActualPolygon

class MapboxPolygonLayer(
    val sourceId: String,
    val layerId: String,
) {
    object Prop {
        const val FILL_COLOR = "fillColor"
        const val Z_INDEX = "zIndex"
    }

    val source = geoJsonSource(sourceId)
    val layer =
        fillLayer(layerId, sourceId) {
            fillColor(
                get {
                    literal(Prop.FILL_COLOR)
                },
            )
            // Sort rendering within this layer by zIndex (higher draws on top)
            fillSortKey(
                get {
                    literal(Prop.Z_INDEX)
                },
            )
        }

    fun draw(entities: List<PolygonEntity<MapboxActualPolygon>>) {
        val features = entities
            .sortedBy { it.state.zIndex }
            .map { it.polygon }
        source.featureCollection(
            FeatureCollection.fromFeatures(features.flatten()),
        )
    }
}
