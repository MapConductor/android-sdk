package com.mapconductor.maplibre.marker

import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.maplibre.MapLibreActualMarker
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.PropertyFactory
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
                // iconSize(get(MapLibreMarkerOverlayRenderer.Prop.SCALE)),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                PropertyFactory.symbolSortKey(get(MapLibreMarkerOverlayRenderer.Prop.Z_INDEX)),
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
        entities: List<MarkerEntityInterface<Feature>>,
        style: org.maplibre.android.maps.Style,
    ) {
        val visibleEntities = entities.filter { it.visible && it.marker != null }
        val features = visibleEntities.mapNotNull { it.marker }
        val collection = FeatureCollection.fromFeatures(features)

        try {
            // Always update the source attached to the current style
            var styleSource = style.getSourceAs<GeoJsonSource>(sourceId)
            if (styleSource == null) {
                // Source might not be attached yet (e.g., after style reload). Try to attach ours.
                try {
                    style.addSource(source)
                } catch (_: Exception) {
                    // ignore if already added or style busy
                }
                styleSource = style.getSourceAs(sourceId)
            }
            styleSource?.setGeoJson(collection)
        } catch (e: Exception) {
            android.util.Log.w("MapLibre", "Failed to update marker source: ${e.message}")
        }
    }
}
