package com.mapconductor.maplibre.marker

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.maplibre.MapLibreActualMarker
import com.mapconductor.maplibre.toPoint
import org.maplibre.android.maps.Style
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

open class MarkerDragLayer(
    sourceId: String,
    layerId: String,
) : MarkerLayer(sourceId, layerId) {
    var selected: MarkerEntityInterface<MapLibreActualMarker>? = null

    fun updatePosition(geoPoint: GeoPoint) {
        selected?.let {
            it.state.position = geoPoint
        }
    }

    fun draw(style: Style) {
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
                    listOf<MapLibreActualMarker>(feature)
                } else {
                    emptyList()
                }
            } ?: emptyList()
        val collection = FeatureCollection.fromFeatures(features)
        val styleSource =
            try {
                style.getSource(sourceId)
            } catch (_: IllegalStateException) {
                null
            }
        if (styleSource is GeoJsonSource) {
            try {
                styleSource.setGeoJson(collection)
                return
            } catch (_: IllegalStateException) {
            }
        }
        source.setGeoJson(collection)
    }
}
