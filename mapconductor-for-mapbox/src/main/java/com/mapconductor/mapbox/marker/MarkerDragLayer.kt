package com.mapconductor.mapbox.marker

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.mapbox.toPoint

class MarkerDragLayer(
    sourceId: String,
    layerId: String,
) : MarkerLayer(sourceId, layerId) {
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
                        GeoPoint.from(it.state.position).toPoint(),
                        it.marker.properties(),
                        it.state.id,
                    )
                it.marker = feature
                listOf<Feature>(feature)
            } ?: emptyList<Feature>()

        source.featureCollection(
            FeatureCollection.fromFeatures(features),
        )
    }
}
