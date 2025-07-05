package com.mapconductor.mapbox.marker

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapconductor.core.marker.MarkerEntity

class MarkerLayer(
    val sourceId: String,
    val layerId: String,
) {
    val layer = SymbolLayer(layerId, sourceId)

    val source: GeoJsonSource =
        geoJsonSource(sourceId) {
            featureCollection(FeatureCollection.fromFeatures(emptyList()))
        }

    fun draw(entities: List<MarkerEntity<Feature>>) {
        val features = entities.map { it.marker }
        source.featureCollection(
            FeatureCollection.fromFeatures(features),
        )
    }
}
