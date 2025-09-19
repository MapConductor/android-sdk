package com.mapconductor.mapbox.marker

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.expressions.dsl.generated.switchCase
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.IconTranslateAnchor
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapconductor.core.marker.MarkerEntity

open class MarkerLayer(
    open val sourceId: String,
    open val layerId: String,
) {
    val layer =
        symbolLayer(layerId, sourceId) {
//            iconSize(Expression.get(MapboxMarkerRenderer.Prop.SCALE))
            iconImage(Expression.get(MapboxMarkerOverlayRenderer.Prop.ICON_ID))
            iconAllowOverlap(true)
            iconIgnorePlacement(true)
            iconAnchor(IconAnchor.TOP_LEFT)
            iconTranslateAnchor(IconTranslateAnchor.MAP)
            iconOffset(
                switchCase {
                    has(MapboxMarkerOverlayRenderer.Prop.ICON_ANCHOR)
                    get(MapboxMarkerOverlayRenderer.Prop.ICON_ANCHOR)
                    literal(listOf(0.0, 0.0)) // center-middle
                },
            )
        }

    val source: GeoJsonSource =
        geoJsonSource(sourceId) {
            featureCollection(FeatureCollection.fromFeatures(emptyList()))
        }

    fun draw(entities: List<MarkerEntity<Feature>>) {
        val visibleEntities = entities.filter { it.visible && it.marker != null }
        val features = visibleEntities.mapNotNull { it.marker }
        source.featureCollection(
            FeatureCollection.fromFeatures(features),
        )
    }
}
