package com.mapconductor.arcgis.raster

import com.arcgismaps.arcgisservices.LevelOfDetail
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.layers.ArcGISTiledLayer
import com.arcgismaps.mapping.layers.Layer
import com.arcgismaps.mapping.layers.TileImageFormat
import com.arcgismaps.mapping.layers.TileInfo
import com.arcgismaps.mapping.layers.WebTiledLayer
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.core.raster.RasterLayerEntity
import com.mapconductor.core.raster.RasterLayerOverlayRenderer
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.RasterSource
import com.mapconductor.core.raster.TileScheme
import kotlin.math.PI
import kotlin.math.pow
import android.util.Log

class ArcGISRasterLayerOverlayRenderer(
    private val holder: ArcGISMapViewHolder,
) : RasterLayerOverlayRenderer<Layer> {
    override suspend fun onAdd(data: List<RasterLayerOverlayRenderer.AddParams>): List<Layer?> =
        data.map { params ->
            addLayer(params.state)
        }

    override suspend fun onChange(data: List<RasterLayerOverlayRenderer.ChangeParams<Layer>>): List<Layer?> =
        data.map { params ->
            val prev = params.prev
            val next = params.current.state
            if (prev.state.source != next.source) {
                removeLayer(prev)
                addLayer(next)
            } else {
                updateLayer(prev.layer, next)
                prev.layer
            }
        }

    override suspend fun onRemove(data: List<RasterLayerEntity<Layer>>) {
        data.forEach { entity ->
            removeLayer(entity)
        }
    }

    override suspend fun onPostProcess() {}

    private fun addLayer(state: RasterLayerState): Layer? {
        val scene = holder.map.scene ?: return null
        val layer =
            when (val source = state.source) {
                is RasterSource.ArcGisService -> ArcGISTiledLayer(source.serviceUrl)
                is RasterSource.UrlTemplate -> buildWebTiledLayer(source, state.id) ?: return null
                is RasterSource.TileJson -> {
                    Log.w("ArcGIS", "ArcGIS raster layers do not support TileJson sources.")
                    return null
                }
            }
        updateLayer(layer, state)
        scene.operationalLayers.add(layer)
        return layer
    }

    private fun updateLayer(
        layer: Layer,
        state: RasterLayerState,
    ) {
        layer.opacity = state.opacity.coerceIn(0.0f, 1.0f)
        layer.isVisible = state.visible
    }

    private fun removeLayer(entity: RasterLayerEntity<Layer>) {
        val scene = holder.map.scene ?: return
        scene.operationalLayers.remove(entity.layer)
    }

    private fun buildWebTiledLayer(
        source: RasterSource.UrlTemplate,
        id: String,
    ): WebTiledLayer? {
        if (source.scheme == TileScheme.TMS) {
            Log.w("ArcGIS", "TMS scheme is not supported for WebTiledLayer.")
            return null
        }
        val template =
            source.template
                .replace("{z}", "{level}")
                .replace("{x}", "{col}")
                .replace("{y}", "{row}")
        val minZoom = source.minZoom ?: DEFAULT_MIN_ZOOM
        val maxZoom = source.maxZoom ?: DEFAULT_MAX_ZOOM
        val tileInfo = buildWebMercatorTileInfo(source.tileSize, minZoom, maxZoom)
        val fullExtent = buildWebMercatorExtent()
        return WebTiledLayer.create(template, emptyList(), tileInfo, fullExtent)
    }

    private fun buildWebMercatorTileInfo(
        tileSize: Int,
        minZoom: Int,
        maxZoom: Int,
    ): TileInfo {
        val spatialReference = SpatialReference(WEB_MERCATOR_WKID)
        val origin = Point(WEB_MERCATOR_MIN, WEB_MERCATOR_MAX, spatialReference)
        val levels = buildWebMercatorLevels(tileSize, minZoom, maxZoom)
        return TileInfo(
            DEFAULT_DPI,
            TileImageFormat.Png,
            levels,
            origin,
            spatialReference,
            tileSize,
            tileSize,
        )
    }

    private fun buildWebMercatorLevels(
        tileSize: Int,
        minZoom: Int,
        maxZoom: Int,
    ): List<LevelOfDetail> {
        val initialResolution =
            (2.0 * PI * WEB_MERCATOR_RADIUS_METERS) / tileSize.toDouble()
        val levels = mutableListOf<LevelOfDetail>()
        for (level in minZoom..maxZoom) {
            val resolution = initialResolution / 2.0.pow(level.toDouble())
            val scale = resolution * DEFAULT_DPI * INCHES_PER_METER
            levels.add(LevelOfDetail(level, resolution, scale))
        }
        return levels
    }

    private fun buildWebMercatorExtent(): Envelope =
        Envelope(
            WEB_MERCATOR_MIN,
            WEB_MERCATOR_MIN,
            WEB_MERCATOR_MAX,
            WEB_MERCATOR_MAX,
            spatialReference = SpatialReference(WEB_MERCATOR_WKID),
        )

    companion object {
        private const val WEB_MERCATOR_WKID = 3857
        private const val WEB_MERCATOR_RADIUS_METERS = 6378137.0
        private const val WEB_MERCATOR_MAX = 20037508.3427892
        private const val WEB_MERCATOR_MIN = -WEB_MERCATOR_MAX
        private const val DEFAULT_DPI = 96
        private const val INCHES_PER_METER = 39.37
        private const val DEFAULT_MIN_ZOOM = 0
        private const val DEFAULT_MAX_ZOOM = 19
    }
}
