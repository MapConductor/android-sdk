package com.mapconductor.kml

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mapconductor.core.features.GeoPoint
import kotlin.math.pow
import android.graphics.Color

class KMLLayerState(
    opacity: Float = KMLDefaults.DEFAULT_OPACITY,
    strokeColor: Int = KMLDefaults.DEFAULT_STROKE_COLOR,
    fillColor: Int = KMLDefaults.DEFAULT_FILL_COLOR,
    strokeWidth: Float = KMLDefaults.DEFAULT_STROKE_WIDTH,
    pointRadius: Float = KMLDefaults.DEFAULT_POINT_RADIUS,
    visible: Boolean = true,
    minZoom: Int = 0,
    maxZoom: Int = 22,
    styleProvider: KMLStyleProviderInterface = DefaultKMLStyleProvider,
    val onLoadStart: (() -> Unit)? = null,
    val onLoadComplete: ((Throwable?) -> Unit)? = null,
    val onClick: ((feature: KMLFeature, position: GeoPoint) -> Unit)? = null,
) {
    var opacity by mutableStateOf(opacity)
    var strokeColor by mutableStateOf(strokeColor)
    var fillColor by mutableStateOf(fillColor)
    var strokeWidth by mutableStateOf(strokeWidth)
    var pointRadius by mutableStateOf(pointRadius)
    var visible by mutableStateOf(visible)
    var minZoom by mutableStateOf(minZoom)
    var maxZoom by mutableStateOf(maxZoom)
    var styleProvider by mutableStateOf(styleProvider)

    internal var renderer: KMLTileRenderer? = null

    /**
     * Call this from your map's onMapClick handler to perform feature hit-testing.
     * If a feature is found at [geoPoint], the [onClick] callback is invoked
     * and true is returned.
     *
     * Pass [pixelTolerance] and [zoom] to use a pixel-based hit threshold instead of the
     * default world-coordinate tolerances. For example, `processClick(geoPoint, 15.0, zoom)`
     * fires when the click is within 15 pixels of the nearest line segment or point.
     * Polygons always hit on interior containment (holes excluded); the threshold only
     * widens their outline.
     */
    fun processClick(
        geoPoint: GeoPoint,
        pixelTolerance: Double? = null,
        zoom: Double? = null,
    ): Boolean {
        val r = renderer ?: return false
        val lineTolSq: Double?
        val pointTolSq: Double?
        if (pixelTolerance != null && zoom != null) {
            val worldSize = r.tileSize.toDouble() * 2.0.pow(zoom)
            val lineTol = pixelTolerance / worldSize
            val pointTol = pixelTolerance * 2.0 / worldSize
            lineTolSq = lineTol * lineTol
            pointTolSq = pointTol * pointTol
        } else {
            lineTolSq = null
            pointTolSq = null
        }
        val hit = r.hitTest(geoPoint.longitude, geoPoint.latitude, lineTolSq, pointTolSq) ?: return false
        onClick?.invoke(hit.feature, hit.position)
        return true
    }

    fun copy(
        opacity: Float = this.opacity,
        strokeColor: Int = this.strokeColor,
        fillColor: Int = this.fillColor,
        strokeWidth: Float = this.strokeWidth,
        pointRadius: Float = this.pointRadius,
        visible: Boolean = this.visible,
        minZoom: Int = this.minZoom,
        maxZoom: Int = this.maxZoom,
        styleProvider: KMLStyleProviderInterface = this.styleProvider,
        onLoadStart: (() -> Unit)? = this.onLoadStart,
        onLoadComplete: ((Throwable?) -> Unit)? = this.onLoadComplete,
        onClick: ((KMLFeature, GeoPoint) -> Unit)? = this.onClick,
    ): KMLLayerState =
        KMLLayerState(
            opacity = opacity,
            strokeColor = strokeColor,
            fillColor = fillColor,
            strokeWidth = strokeWidth,
            pointRadius = pointRadius,
            visible = visible,
            minZoom = minZoom,
            maxZoom = maxZoom,
            styleProvider = styleProvider,
            onLoadStart = onLoadStart,
            onLoadComplete = onLoadComplete,
            onClick = onClick,
        )
}

object KMLDefaults {
    const val DEFAULT_OPACITY = 1.0f
    val DEFAULT_STROKE_COLOR: Int = Color.argb(255, 30, 136, 229)
    val DEFAULT_FILL_COLOR: Int = Color.argb(128, 30, 136, 229)
    const val DEFAULT_STROKE_WIDTH = 2f
    const val DEFAULT_POINT_RADIUS = 8f
    const val DEFAULT_TILE_SIZE = 512
    const val DEFAULT_MAX_ZOOM = 22
}
