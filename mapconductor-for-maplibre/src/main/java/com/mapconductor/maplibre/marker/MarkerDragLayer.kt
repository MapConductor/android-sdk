package com.mapconductor.maplibre.marker

import com.mapconductor.core.marker.MarkerEntity
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.has
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.switchCase
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
        SymbolLayer(layerId, sourceId)
            .withProperties(
                iconImage(Expression.get(MapLibreMarkerOverlayRenderer.Prop.ICON_ID)),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconAnchor(MapLibreMarkerOverlayRenderer.IconAnchor.TOP_LEFT),
                iconTranslateAnchor(MapLibreMarkerOverlayRenderer.IconTranslateAnchor.MAP),
                iconOffset(
                    switchCase(
                        has(MapLibreMarkerOverlayRenderer.Prop.ICON_ANCHOR),
                        get(MapLibreMarkerOverlayRenderer.Prop.ICON_ANCHOR),
                        literal(listOf(0.0, 0.0)) // center-middle
                    ),
                ),
            )

    val source: GeoJsonSource = GeoJsonSource(sourceId)

    fun draw(entities: List<MarkerEntity<Feature>>) {
        val visibleEntities = entities.filter { it.visible && it.marker != null }
        val features = visibleEntities.mapNotNull { it.marker }
        source.setGeoJson(
            FeatureCollection.fromFeatures(features),
        )
    }
}
