package com.mapconductor.maplibre.groundimage

import com.mapconductor.core.groundimage.AbstractGroundImageOverlayRenderer
import com.mapconductor.core.groundimage.GroundImageEntityInterface
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.GroundImageTileProvider
import com.mapconductor.core.tileserver.LocalTileServer
import com.mapconductor.maplibre.MapLibreActualGroundImage
import com.mapconductor.maplibre.MapLibreMapViewHolderInterface
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource as MapLibreRasterSource
import org.maplibre.android.style.sources.TileSet
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapLibreGroundImageOverlayRenderer(
    override val holder: MapLibreMapViewHolderInterface,
    private val tileServer: LocalTileServer,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractGroundImageOverlayRenderer<MapLibreActualGroundImage>() {
    override suspend fun createGroundImage(state: GroundImageState): MapLibreActualGroundImage? =
        withContext(coroutine.coroutineContext) {
            val style = holder.map.style ?: return@withContext null
            val routeId = buildSafeRouteId(state.id)
            val provider = GroundImageTileProvider(tileSize = state.tileSize)
            provider.update(state, opacity = 1.0f)
            tileServer.register(routeId, provider)

            val sourceId = "groundimage-source-$routeId"
            val layerId = "groundimage-layer-$routeId"
            val cacheKey = tileCacheKey(state)
            val handle =
                MapLibreGroundImageHandle(
                    routeId = routeId,
                    generation = 0L,
                    cacheKey = cacheKey,
                    sourceId = sourceId,
                    layerId = layerId,
                    tileProvider = provider,
                )

            removeSourceAndLayerIfExists(handle)
            addSourceAndLayer(handle, state)
            handle
        }

    override suspend fun updateGroundImageProperties(
        groundImage: MapLibreActualGroundImage,
        current: GroundImageEntityInterface<MapLibreActualGroundImage>,
        prev: GroundImageEntityInterface<MapLibreActualGroundImage>,
    ): MapLibreActualGroundImage? =
        withContext(coroutine.coroutineContext) {
            val style = holder.map.style ?: return@withContext groundImage

            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint

            if (finger.opacity != prevFinger.opacity) {
                updateLayerOpacity(style, groundImage.layerId, current.state.opacity)
            }

            val tileSizeChanged = finger.tileSize != prevFinger.tileSize
            val tileContentChanged =
                finger.bounds != prevFinger.bounds || finger.image != prevFinger.image || tileSizeChanged
            if (!tileContentChanged) {
                return@withContext groundImage
            }

            val provider =
                if (tileSizeChanged) {
                    GroundImageTileProvider(tileSize = current.state.tileSize).also {
                        tileServer.register(groundImage.routeId, it)
                    }
                } else {
                    groundImage.tileProvider
                }
            provider.update(current.state, opacity = 1.0f)

            val nextHandle =
                groundImage.copy(
                    generation = groundImage.generation + 1L,
                    cacheKey = tileCacheKey(current.state),
                    tileProvider = provider,
                )
            removeSourceAndLayerIfExists(nextHandle)
            addSourceAndLayer(nextHandle, current.state)
            nextHandle
        }

    override suspend fun removeGroundImage(entity: GroundImageEntityInterface<MapLibreActualGroundImage>) {
        coroutine.launch {
            val style = holder.map.style ?: return@launch
            val handle = entity.groundImage
            removeSourceAndLayerIfExists(handle)
            tileServer.unregister(handle.routeId)
        }
    }

    private fun addSourceAndLayer(
        handle: MapLibreGroundImageHandle,
        state: GroundImageState,
    ) {
        val style = holder.map.style ?: return
        val tileSet =
            TileSet("2.2.0", tileServer.urlTemplate(handle.routeId, handle.tileProvider.tileSize, handle.cacheKey))
                .apply {
                    scheme = "xyz"
                    setMinZoom(0f)
                    setMaxZoom(22f)
                }
        val source = MapLibreRasterSource(handle.sourceId, tileSet, handle.tileProvider.tileSize)
        val layer = RasterLayer(handle.layerId, handle.sourceId)
        layer.setProperties(
            PropertyFactory.rasterOpacity(state.opacity.coerceIn(0.0f, 1.0f)),
            PropertyFactory.visibility(Property.VISIBLE),
        )

        try {
            style.addSource(source)
        } catch (e: Exception) {
            Log.w("MapLibre", "Failed to add ground image source: ${e.message}")
        }

        try {
            style.addLayerBelow(layer, BELOW_LAYER_ID)
        } catch (_: Exception) {
            try {
                style.addLayer(layer)
            } catch (e: Exception) {
                Log.w("MapLibre", "Failed to add ground image layer: ${e.message}")
            }
        }
    }

    private fun removeSourceAndLayerIfExists(handle: MapLibreGroundImageHandle) {
        val style = holder.map.style ?: return
        try {
            style.removeLayer(handle.layerId)
        } catch (_: Exception) {
        }
        try {
            style.removeSource(handle.sourceId)
        } catch (_: Exception) {
        }
    }

    private fun updateLayerOpacity(
        style: org.maplibre.android.maps.Style,
        layerId: String,
        opacity: Float,
    ) {
        val layer = style.getLayer(layerId) as? RasterLayer ?: return
        layer.setProperties(PropertyFactory.rasterOpacity(opacity.coerceIn(0.0f, 1.0f)))
    }

    private fun buildSafeRouteId(id: String): String =
        buildString(id.length + 16) {
            append("groundimage-")
            id.forEach { ch ->
                when {
                    ch.isLetterOrDigit() -> append(ch)
                    ch == '-' || ch == '_' -> append(ch)
                    else -> append('_')
                }
            }
        }

    private fun tileCacheKey(state: GroundImageState): String =
        buildString(64) {
            append(state.bounds.hashCode())
            append('-')
            append(state.image.hashCode())
            append('-')
            append(state.tileSize.hashCode())
            append('-')
            append(state.extra?.hashCode() ?: 0)
        }

    companion object {
        private const val BELOW_LAYER_ID = "polyline-layer"
    }
}
