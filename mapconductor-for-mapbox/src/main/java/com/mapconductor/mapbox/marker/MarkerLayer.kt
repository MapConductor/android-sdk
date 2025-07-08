package com.mapconductor.mapbox.marker

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapconductor.core.marker.MarkerEntity

open class MarkerLayer(
    open val sourceId: String,
    open val layerId: String,
) {
    val layer = SymbolLayer(layerId, sourceId).apply {
//        iconSize(Expression.get(MapboxMarkerRenderer.Prop.SCALE))
        iconImage(Expression.get(MapboxMarkerRenderer.Prop.ICON_ID))
        iconAllowOverlap(true)
        iconIgnorePlacement(true)
        iconAnchor(IconAnchor.BOTTOM)
    }


    val source: GeoJsonSource =
        geoJsonSource(sourceId) {
            featureCollection(FeatureCollection.fromFeatures(emptyList()))
        }

    fun draw(entities: List<MarkerEntity<Feature>>) {
        val features = entities.map { it.marker }
        source.featureCollection(
            FeatureCollection.fromFeatures(features),
        )
    }
}
