package com.mapconductor.maplibre.marker

import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.maplibre.MapLibreActualMarker
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconAnchor
import org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconOffset
import org.maplibre.android.style.layers.PropertyFactory.iconTranslateAnchor
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

open class MarkerLayer(
    open val sourceId: String,
    open val layerId: String,
) {
    val layer =
        SymbolLayer(layerId, sourceId).apply {
            setProperties(
                iconImage(get(MapLibreMarkerOverlayRenderer.Prop.ICON_ID)),
                // iconSize(2.0f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconAnchor(MapLibreMarkerOverlayRenderer.IconAnchor.TOP_LEFT),
                iconTranslateAnchor(MapLibreMarkerOverlayRenderer.IconTranslateAnchor.MAP),
                // Each feature always carries icon-offset in properties; use it directly
                iconOffset(get(MapLibreMarkerOverlayRenderer.Prop.ICON_ANCHOR)),
            )
        }

    val source: GeoJsonSource =
        GeoJsonSource(
            sourceId,
            FeatureCollection.fromFeatures(emptyList<MapLibreActualMarker>()),
        )

    fun draw(
        entities: List<MarkerEntity<Feature>>,
        style: org.maplibre.android.maps.Style,
    ) {
        val visibleEntities = entities.filter { it.visible && it.marker != null }
        val features = visibleEntities.mapNotNull { it.marker }

        // Try to get the source from the style (wrapped in try-catch in case style is being replaced)
        val sourceFromStyle =
            try {
                style.getSource(sourceId)
            } catch (e: IllegalStateException) {
                android.util.Log.w("MapLibre", "Cannot get source, style is being replaced: ${e.message}")
                null
            }

        if (sourceFromStyle is GeoJsonSource) {
            try {
                sourceFromStyle.setGeoJson(FeatureCollection.fromFeatures(features))
            } catch (e: IllegalStateException) {
                android.util.Log.w("MapLibre", "Cannot update source, style is being replaced: ${e.message}")
                // Fallback to original source instance
                source.setGeoJson(FeatureCollection.fromFeatures(features))
            }
        } else {
            android.util.Log.w("MapLibre", "Could not get source from style! Using original source instance")
            // Fallback to original method
            source.setGeoJson(FeatureCollection.fromFeatures(features))
        }
    }
}
