package com.mapconductor.maplibre.polyline

import com.mapconductor.core.polyline.PolylineEntityInterface
import com.mapconductor.maplibre.MapLibreActualPolyline
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

class MapLibrePolylineLayer(
    val sourceId: String,
    val layerId: String,
) {
    object Prop {
        const val STROKE_COLOR = "strokeColor"
        const val STROKE_WIDTH = "strokeWidth"
        const val Z_INDEX = "zIndex"
    }

    val source: GeoJsonSource =
        GeoJsonSource(
            sourceId,
            FeatureCollection.fromFeatures(emptyList()),
        )

    val layer: LineLayer =
        LineLayer(layerId, sourceId).apply {
            setProperties(
                lineJoin(Property.LINE_JOIN_ROUND),
                lineCap(Property.LINE_CAP_ROUND),
                lineColor(get(Prop.STROKE_COLOR)),
                lineWidth(get(Prop.STROKE_WIDTH)),
            )
        }

    fun draw(
        entities: List<PolylineEntityInterface<MapLibreActualPolyline>>,
        style: org.maplibre.android.maps.Style,
    ) {
        val features: List<Feature> = entities.flatMap { it.polyline }

        val styleSource =
            try {
                style.getSource(sourceId)
            } catch (e: IllegalStateException) {
                // Style might be in transition
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
