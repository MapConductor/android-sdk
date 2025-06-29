package com.mapconductor.mapbox

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.MarkerEntity

class MarkerDragLayer(
    val sourceId: String,
    val layerId: String,
) {
    val layer = SymbolLayer(layerId, sourceId)
    val source: GeoJsonSource =
        geoJsonSource(sourceId) {
            featureCollection(FeatureCollection.fromFeatures(emptyList()))
        }

    var selected: MarkerEntity<Feature>? = null

    fun updatePosition(geoPoint: GeoPoint) {
        selected?.let {
            it.state.position = geoPoint
        }
    }

    fun draw() {
        val features =
            selected?.let {
                val feature =
                    Feature.fromGeometry(
                        it.state.position.toPoint(),
                        it.marker.properties(),
                    )
                it.marker = feature
                listOf<Feature>(feature)
            } ?: emptyList<Feature>()

        source.featureCollection(
            FeatureCollection.fromFeatures(features),
        )
    }
}
