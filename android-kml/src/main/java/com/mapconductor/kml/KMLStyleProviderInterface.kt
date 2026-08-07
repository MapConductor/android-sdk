package com.mapconductor.kml

/** Resolves the render style for a KML feature. */
fun interface KMLStyleProviderInterface {
    /**
     * Returns the complete style for [feature]. [defaultStyle] contains the current layer-level
     * stroke color, fill color, stroke width, and point radius.
     */
    fun getStyle(
        feature: KMLFeature,
        defaultStyle: KMLTileRenderer.LayerStyle,
    ): KMLTileRenderer.LayerStyle
}

/** Preserves the existing feature-style-over-layer-style behavior. */
object DefaultKMLStyleProvider : KMLStyleProviderInterface {
    override fun getStyle(
        feature: KMLFeature,
        defaultStyle: KMLTileRenderer.LayerStyle,
    ): KMLTileRenderer.LayerStyle =
        KMLTileRenderer.LayerStyle(
            strokeColor = feature.strokeColor ?: defaultStyle.strokeColor,
            fillColor = feature.fillColor ?: defaultStyle.fillColor,
            strokeWidth = feature.strokeWidth ?: defaultStyle.strokeWidth,
            pointRadius = feature.pointRadius ?: defaultStyle.pointRadius,
        )
}
