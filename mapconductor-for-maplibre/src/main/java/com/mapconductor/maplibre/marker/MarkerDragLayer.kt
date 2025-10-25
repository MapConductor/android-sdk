package com.mapconductor.maplibre.marker

import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.maplibre.MapLibreActualMarker
import com.mapconductor.maplibre.toPoint
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

open class MarkerDragLayer(
    sourceId: String,
    layerId: String,
) : MarkerLayer(sourceId, layerId) {
    var selected: MarkerEntity<MapLibreActualMarker>? = null

    fun updatePosition(geoPoint: GeoPointImpl) {
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
                            GeoPointImpl.from(it.state.position).toPoint(),
                            it.marker?.properties(),
                            it.state.id,
                        )
                    it.marker = feature
                    listOf<MapLibreActualMarker>(feature)
                } else {
                    emptyList()
                }
            } ?: emptyList()
        source.setGeoJson(
            FeatureCollection.fromFeatures(features),
        )
    }
}
