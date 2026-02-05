package com.mapconductor.mapbox.raster

import com.mapbox.maps.TileCacheBudget
import com.mapbox.maps.TileCacheBudgetInMegabytes
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerAbove
import com.mapbox.maps.extension.style.layers.addLayerBelow
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
    private val stateById: MutableMap<String, RasterLayerState> = mutableMapOf()
    private val handleById: MutableMap<String, MapboxRasterLayerHandle> = mutableMapOf()

    private fun isMarkerTileRaster(state: RasterLayerState): Boolean =
        state.id.startsWith(MARKER_TILE_RASTER_ID_PREFIX)

    override suspend fun onAdd(
        data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>,
    ): List<MapboxRasterLayerHandle?> =
        data.map { params ->
            addLayer(params.state).also { handle ->
                if (handle != null) {
                    stateById[params.state.id] = params.state
                    handleById[params.state.id] = handle
                }
            }
        }.also {
            holder.map.style?.let { style -> rebuildNonMarkerRasterLayers(style) }
        }

    override suspend fun onChange(
        data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<MapboxRasterLayerHandle>>,
    ): List<MapboxRasterLayerHandle?> =
        data.map { params ->
            val prev = params.prev
            val next = params.current.state
            val handle =
                if (prev.state.source != next.source) {
                    removeLayer(prev)
                    addLayer(next)
                } else {
                    updateLayer(prev.layer, next)
                    prev.layer
                }
            if (handle != null) {
                stateById[next.id] = next
                handleById[next.id] = handle
            }
            handle
        }.also {
            holder.map.style?.let { style -> rebuildNonMarkerRasterLayers(style) }
        }

    override suspend fun onRemove(data: List<RasterLayerEntityInterface<MapboxRasterLayerHandle>>) {
        data.forEach { entity ->
            stateById.remove(entity.state.id)
            handleById.remove(entity.state.id)
            removeLayer(entity)
        }
        holder.map.style?.let { style -> rebuildNonMarkerRasterLayers(style) }
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
            if (isMarkerTileRaster(state)) {
                addLayerForMarkerTile(style, layer)
            } else {
                style.addLayer(layer)
            }
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
            if (isMarkerTileRaster(state)) {
                addLayerForMarkerTile(style, layer)
            } else {
                style.addLayer(layer)
            }
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
                // tileCacheBudget(TileCacheBudget(TileCacheBudgetInMegabytes(0L)))
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

    private fun addLayerForMarkerTile(
        style: com.mapbox.maps.Style,
        layer: com.mapbox.maps.extension.style.layers.Layer,
    ) {
        // Insert the raster tiles below the marker symbol layer so they don't cover markers,
        // but remain above vector overlays that are anchored below markers (polyline/circle/etc).
        try {
            style.addLayerBelow(layer, MARKERS_LAYER_ID)
            return
        } catch (_: Exception) {
        }

        // Best-effort fallback: place above polylines if marker layer isn't present yet.
        try {
            style.addLayerAbove(layer, POLYLINE_LAYER_ID)
            return
        } catch (_: Exception) {
        }

        style.addLayer(layer)
    }

    private fun rebuildNonMarkerRasterLayers(style: com.mapbox.maps.Style) {
        val ordered =
            stateById.values
                .asSequence()
                .filter { !isMarkerTileRaster(it) }
                .sortedBy { it.zIndex }
                .mapNotNull { state -> handleById[state.id]?.let { handle -> state to handle } }
                .toList()

        // Remove and re-add non-marker raster layers in zIndex order to ensure deterministic stacking.
        ordered.forEach { (_, handle) ->
            try {
                style.removeStyleLayer(handle.layerId)
            } catch (_: Exception) {
            }
        }

        ordered.forEach { (state, handle) ->
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
    }

    private companion object {
        private const val MARKER_TILE_RASTER_ID_PREFIX = "marker-tile-"
        private const val MARKERS_LAYER_ID = "markers-layer"
        private const val POLYLINE_LAYER_ID = "polyline-layer"
    }
}

data class MapboxRasterLayerHandle(
    val sourceId: String,
    val layerId: String,
)
