package com.mapconductor.mapbox.marker

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.mapbox.toPoint

class MarkerDragLayer(
    sourceId: String,
    layerId: String,
) : MarkerLayer(sourceId, layerId) {
    var selected: MarkerEntityInterface<MapboxActualMarker>? = null

    fun updatePosition(geoPoint: GeoPoint) {
        selected?.let {
            it.state.position = geoPoint
        }
    }

    fun draw() {
        val features =
            selected?.let {
                if (it.marker != null) {
                    val feature =
                        Feature.fromGeometry(
                            GeoPoint.from(it.state.position).toPoint(),
                            it.marker?.properties(),
                            it.state.id,
                        )
                    it.marker = feature
                    listOf<MapboxActualMarker>(feature)
                } else {
                    emptyList<MapboxActualMarker>()
                }
            } ?: emptyList<MapboxActualMarker>()

        source.featureCollection(
            FeatureCollection.fromFeatures(features),
        )
    }
}
