package com.mapconductor.mapbox.raster

import com.mapbox.maps.TileCacheBudget
import com.mapbox.maps.TileCacheBudgetInMegabytes
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.rasterSource
import com.mapconductor.core.raster.RasterLayerEntityInterface
import com.mapconductor.core.raster.RasterLayerOverlayRendererInterface
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.mapbox.MapboxMapViewHolder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MapboxRasterLayerOverlayRenderer(
    private val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : RasterLayerOverlayRendererInterface<MapboxRasterLayerHandle> {
    override suspend fun onAdd(
        data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>,
    ): List<MapboxRasterLayerHandle?> =
        data.map { params ->
            addLayer(params.state)
        }

    override suspend fun onChange(
        data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<MapboxRasterLayerHandle>>,
    ): List<MapboxRasterLayerHandle?> =
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

    override suspend fun onRemove(data: List<RasterLayerEntityInterface<MapboxRasterLayerHandle>>) {
        data.forEach { entity ->
            removeLayer(entity)
        }
    }

    override suspend fun onPostProcess() {}

    private fun addLayer(state: RasterLayerState): MapboxRasterLayerHandle? {
        val sourceId = "raster-source-${state.id}"
        val layerId = "raster-layer-${state.id}"
        val source = buildSource(sourceId, state.source) ?: return null
        val handle = MapboxRasterLayerHandle(sourceId = sourceId, layerId = layerId)
        val style = holder.map.style ?: return handle
        val opacity =
            if (state.visible) {
                state.opacity.coerceIn(0.0f, 1.0f).toDouble()
            } else {
                0.0
            }
        val layer =
            rasterLayer(layerId, sourceId) {
                rasterOpacity(opacity)
            }
        try {
            style.addSource(source)
        } catch (e: Exception) {
            Log.w("Mapbox", "Failed to add raster source: ${e.message}")
        }
        try {
            style.addLayer(layer)
        } catch (e: Exception) {
            Log.w("Mapbox", "Failed to add raster layer: ${e.message}")
        }
        return handle
    }

    private fun updateLayer(
        handle: MapboxRasterLayerHandle,
        state: RasterLayerState,
    ) {
        val style = holder.map.style ?: return
        try {
            style.removeStyleLayer(handle.layerId)
        } catch (_: Exception) {
        }
        val opacity =
            if (state.visible) {
                state.opacity.coerceIn(0.0f, 1.0f).toDouble()
            } else {
                0.0
            }
        val layer =
            rasterLayer(handle.layerId, handle.sourceId) {
                rasterOpacity(opacity)
            }
        try {
            style.addLayer(layer)
        } catch (_: Exception) {
        }
    }

    private fun removeLayer(entity: RasterLayerEntityInterface<MapboxRasterLayerHandle>) {
        val style = holder.map.style ?: return
        val handle = entity.layer
        try {
            style.removeStyleLayer(handle.layerId)
        } catch (_: Exception) {
        }
        try {
            style.removeStyleSource(handle.sourceId)
        } catch (_: Exception) {
        }
    }

    private fun buildSource(
        sourceId: String,
        source: RasterLayerSource,
    ) = when (source) {
        is RasterLayerSource.UrlTemplate ->
            rasterSource(sourceId) {
                tiles(listOf(source.template))
                tileSize(source.tileSize.toLong())
                tileCacheBudget(TileCacheBudget(TileCacheBudgetInMegabytes(0L)))
                source.minZoom?.let { minzoom(it.toLong()) }
                source.maxZoom?.let { maxzoom(it.toLong()) }
                source.attribution?.let { attribution(it) }
                if (source.scheme == TileScheme.TMS) {
                    // Mapbox raster sources default to XYZ; TMS is best-effort.
                    // If needed, provide a TMS-compatible URL template instead.
                }
            }
        is RasterLayerSource.TileJson ->
            rasterSource(sourceId) {
                url(source.url)
            }
        is RasterLayerSource.ArcGisService -> {
            val base = source.serviceUrl.trimEnd('/')
            rasterSource(sourceId) {
                tiles(listOf("$base/tile/{z}/{y}/{x}"))
                tileSize(RasterLayerSource.DEFAULT_TILE_SIZE.toLong())
            }
        }
    }
}

data class MapboxRasterLayerHandle(
    val sourceId: String,
    val layerId: String,
)
