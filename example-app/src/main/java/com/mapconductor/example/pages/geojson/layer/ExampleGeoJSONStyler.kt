package com.mapconductor.example.pages.geojson.layer

import com.mapconductor.geojson.DefaultGeoJSONStyleProvider
import com.mapconductor.geojson.GeoJSONFeature
import com.mapconductor.geojson.GeoJSONStyleProviderInterface
import com.mapconductor.geojson.GeoJSONTileRenderer

class ExampleGeoJSONStyler(
    private val routeColors: Map<RouteKey, Int>,
) : GeoJSONStyleProviderInterface {
    override fun getStyle(
        feature: GeoJSONFeature,
        defaultStyle: GeoJSONTileRenderer.LayerStyle,
    ): GeoJSONTileRenderer.LayerStyle {
        val baseStyle = DefaultGeoJSONStyleProvider.getStyle(feature, defaultStyle)
        val companyName = feature.properties[COMPANY_PROPERTY]?.toString() ?: return baseStyle
        val lineName = feature.properties[LINE_PROPERTY]?.toString() ?: return baseStyle
        val color = routeColors[RouteKey(companyName, lineName)] ?: return baseStyle
        return baseStyle.copy(strokeColor = color)
    }

    data class RouteKey(
        val companyName: String,
        val lineName: String,
    )

    companion object {
        private const val COMPANY_PROPERTY = "N02_004"
        private const val LINE_PROPERTY = "N02_003"
    }
}
