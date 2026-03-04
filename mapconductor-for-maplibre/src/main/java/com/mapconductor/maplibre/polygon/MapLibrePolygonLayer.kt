package com.mapconductor.maplibre.polygon

import com.mapconductor.core.polygon.PolygonEntityInterface
import com.mapconductor.maplibre.MapLibreActualPolygon
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

class MapLibrePolygonLayer(
    val sourceId: String,
    val layerId: String,
) {
    object Prop {
        const val FILL_COLOR = "fillColor"
        const val Z_INDEX = "zIndex"
    }

    val source: GeoJsonSource =
        GeoJsonSource(
            sourceId,
            FeatureCollection.fromFeatures(emptyList()),
        )

    val layer: FillLayer =
        FillLayer(layerId, sourceId).apply {
            setProperties(
                fillColor(get(Prop.FILL_COLOR)),
            )
        }

    fun draw(
        entities: List<PolygonEntityInterface<MapLibreActualPolygon>>,
        style: org.maplibre.android.maps.Style,
    ) {
        val features: List<Feature> =
            entities
                .sortedBy { it.state.zIndex }
                .flatMap { it.polygon }

        val styleSource =
            try {
                style.getSource(sourceId)
            } catch (e: IllegalStateException) {
                null
            }

        if (styleSource is GeoJsonSource) {
            try {
                styleSource.setGeoJson(FeatureCollection.fromFeatures(features))
                return
            } catch (_: IllegalStateException) {
                // fall through to fallback
            }
        }
        // Fallback to local source instance if style source is unavailable
        source.setGeoJson(FeatureCollection.fromFeatures(features))
    }
}
