package com.mapconductor.maplibre.raster

import com.mapconductor.core.raster.RasterLayerEntityInterface
import com.mapconductor.core.raster.RasterLayerOverlayRendererInterface
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.RasterSource
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.maplibre.MapLibreMapViewHolderInterface
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource as MapLibreRasterSource
import org.maplibre.android.style.sources.TileSet
import android.util.Log

class MapLibreRasterLayerOverlayRenderer(
    private val holder: MapLibreMapViewHolderInterface,
) : RasterLayerOverlayRendererInterface<MapLibreRasterLayerHandle> {
    override suspend fun onAdd(data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>): List<MapLibreRasterLayerHandle?> =
        data.map { params ->
            addLayer(params.state)
        }

    override suspend fun onChange(
        data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<MapLibreRasterLayerHandle>>,
    ): List<MapLibreRasterLayerHandle?> =
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

    override suspend fun onRemove(data: List<RasterLayerEntityInterface<MapLibreRasterLayerHandle>>) {
        data.forEach { entity ->
            removeLayer(entity)
        }
    }

    override suspend fun onPostProcess() {}

    private fun addLayer(state: RasterLayerState): MapLibreRasterLayerHandle? {
        val sourceId = "raster-source-${state.id}"
        val layerId = "raster-layer-${state.id}"
        val source = buildSource(sourceId, state.source) ?: return null
        val handle = MapLibreRasterLayerHandle(sourceId = sourceId, layerId = layerId)
        val style = holder.map.style ?: return handle
        val layer = RasterLayer(layerId, sourceId)
        val opacity = state.opacity.coerceIn(0.0f, 1.0f)
        layer.setProperties(
            PropertyFactory.rasterOpacity(opacity),
            PropertyFactory.visibility(
                if (state.visible) Property.VISIBLE else Property.NONE,
            ),
        )
        try {
            style.addSource(source)
        } catch (e: Exception) {
            Log.w("MapLibre", "Failed to add raster source: ${e.message}")
        }
        try {
            style.addLayer(layer)
        } catch (e: Exception) {
            Log.w("MapLibre", "Failed to add raster layer: ${e.message}")
        }
        return handle
    }

    private fun updateLayer(
        handle: MapLibreRasterLayerHandle,
        state: RasterLayerState,
    ) {
        val style = holder.map.style ?: return
        val layer = style.getLayer(handle.layerId) as? RasterLayer ?: return
        val opacity = state.opacity.coerceIn(0.0f, 1.0f)
        layer.setProperties(
            PropertyFactory.rasterOpacity(opacity),
            PropertyFactory.visibility(
                if (state.visible) Property.VISIBLE else Property.NONE,
            ),
        )
    }

    private fun removeLayer(entity: RasterLayerEntityInterface<MapLibreRasterLayerHandle>) {
        val style = holder.map.style ?: return
        val handle = entity.layer
        try {
            style.removeLayer(handle.layerId)
        } catch (_: Exception) {
        }
        try {
            style.removeSource(handle.sourceId)
        } catch (_: Exception) {
        }
    }

    private fun buildSource(
        sourceId: String,
        source: RasterSource,
    ): MapLibreRasterSource? =
        when (source) {
            is RasterSource.UrlTemplate -> {
                val tileSet =
                    TileSet("2.2.0", source.template).apply {
                        source.attribution?.let { attribution = it }
                        source.minZoom?.let { setMinZoom(it.toFloat()) }
                        source.maxZoom?.let { setMaxZoom(it.toFloat()) }
                        scheme = if (source.scheme == TileScheme.TMS) "tms" else "xyz"
                    }
                MapLibreRasterSource(sourceId, tileSet, source.tileSize)
            }
            is RasterSource.TileJson -> MapLibreRasterSource(sourceId, source.url)
            is RasterSource.ArcGisService -> {
                val base = source.serviceUrl.trimEnd('/')
                val tileSet =
                    TileSet("2.2.0", "$base/tile/{z}/{y}/{x}").apply {
                        scheme = "xyz"
                    }
                MapLibreRasterSource(sourceId, tileSet, RasterSource.DEFAULT_TILE_SIZE)
            }
        }
}

data class MapLibreRasterLayerHandle(
    val sourceId: String,
    val layerId: String,
)
