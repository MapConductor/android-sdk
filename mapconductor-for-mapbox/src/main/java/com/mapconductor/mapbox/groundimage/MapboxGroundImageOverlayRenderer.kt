package com.mapconductor.mapbox.groundimage

import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.rasterSource
import com.mapconductor.core.groundimage.AbstractGroundImageOverlayRenderer
import com.mapconductor.core.groundimage.GroundImageEntityInterface
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.GroundImageTileProvider
import com.mapconductor.core.tileserver.LocalTileServer
import com.mapconductor.mapbox.MapboxActualGroundImage
import com.mapconductor.mapbox.MapboxMapViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

class MapboxGroundImageOverlayRenderer(
    override val holder: MapboxMapViewHolder,
    private val tileServer: LocalTileServer,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractGroundImageOverlayRenderer<MapboxActualGroundImage>() {
    override suspend fun createGroundImage(state: GroundImageState): MapboxActualGroundImage? =
        withContext(coroutine.coroutineContext) {
            val routeId = buildSafeRouteId(state.id)
            val provider = GroundImageTileProvider(tileSize = state.tileSize)
            provider.update(state, opacity = 1.0f)
            tileServer.register(routeId, provider)

            val sourceId = "groundimage-source-$routeId"
            val layerId = "groundimage-layer-$routeId"
            val handle =
                MapboxGroundImageHandle(
                    routeId = routeId,
                    version = 0L,
                    sourceId = sourceId,
                    layerId = layerId,
                    tileProvider = provider,
                )

            holder.map.style?.let { style ->
                removeSourceAndLayerIfExists(style, handle)
                addSourceAndLayer(style, handle, state)
            }
            handle
        }

    override suspend fun updateGroundImageProperties(
        groundImage: MapboxActualGroundImage,
        current: GroundImageEntityInterface<MapboxActualGroundImage>,
        prev: GroundImageEntityInterface<MapboxActualGroundImage>,
    ): MapboxActualGroundImage? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint

            val tileSizeChanged = finger.tileSize != prevFinger.tileSize
            val tileContentChanged = finger.bounds != prevFinger.bounds || finger.image != prevFinger.image || tileSizeChanged
            val opacityChanged = finger.opacity != prevFinger.opacity

            if (!tileContentChanged && !opacityChanged) {
                return@withContext groundImage
            }

            val nextHandle =
                if (tileContentChanged) {
                    val provider =
                        if (tileSizeChanged) {
                            GroundImageTileProvider(tileSize = current.state.tileSize).also {
                                tileServer.register(groundImage.routeId, it)
                            }
                        } else {
                            groundImage.tileProvider
                        }
                    provider.update(current.state, opacity = 1.0f)
                    groundImage.copy(version = groundImage.version + 1L, tileProvider = provider)
                } else {
                    groundImage
                }

            holder.map.style?.let { style ->
                if (tileContentChanged) {
                    removeSourceAndLayerIfExists(style, nextHandle)
                    addSourceAndLayer(style, nextHandle, current.state)
                } else if (opacityChanged) {
                    updateLayerOpacity(
                        style = style,
                        sourceId = nextHandle.sourceId,
                        layerId = nextHandle.layerId,
                        opacity = current.state.opacity,
                    )
                }
            }

            nextHandle
        }

    override suspend fun removeGroundImage(entity: GroundImageEntityInterface<MapboxActualGroundImage>) {
        coroutine.launch {
            val style = holder.map.style
            val handle = entity.groundImage
            if (style != null) {
                removeSourceAndLayerIfExists(style, handle)
            }
            tileServer.unregister(handle.routeId)
        }
    }

    private fun addSourceAndLayer(
        style: Style,
        handle: MapboxGroundImageHandle,
        state: GroundImageState,
    ) {
        val source =
            rasterSource(handle.sourceId) {
                tiles(listOf(tileServer.urlTemplate(handle.routeId, handle.version, handle.tileProvider.tileSize)))
                tileSize(handle.tileProvider.tileSize.toLong())
                minzoom(0L)
                maxzoom(22L)
            }

        val opacity =
            state.opacity
                .coerceIn(0.0f, 1.0f)
                .toDouble()

        val layer =
            rasterLayer(handle.layerId, handle.sourceId) {
                rasterOpacity(opacity)
                visibility(Visibility.VISIBLE)
            }

        try {
            style.addSource(source)
        } catch (e: Exception) {
            Log.w("Mapbox", "Failed to add ground image source: ${e.message}")
        }

        try {
            style.addLayerBelow(layer, BELOW_LAYER_ID)
        } catch (_: Exception) {
            try {
                style.addLayer(layer)
            } catch (e: Exception) {
                Log.w("Mapbox", "Failed to add ground image layer: ${e.message}")
            }
        }
    }

    private fun removeSourceAndLayerIfExists(
        style: Style,
        handle: MapboxGroundImageHandle,
    ) {
        try {
            style.removeStyleLayer(handle.layerId)
        } catch (_: Exception) {
        }
        try {
            style.removeStyleSource(handle.sourceId)
        } catch (_: Exception) {
        }
    }

    private fun updateLayerOpacity(
        style: Style,
        sourceId: String,
        layerId: String,
        opacity: Float,
    ) {
        try {
            style.removeStyleLayer(layerId)
        } catch (_: Exception) {
        }

        val safeOpacity = opacity.coerceIn(0.0f, 1.0f).toDouble()
        val layer =
            rasterLayer(layerId, sourceId) {
                rasterOpacity(safeOpacity)
                visibility(Visibility.VISIBLE)
            }

        try {
            style.addLayerBelow(layer, BELOW_LAYER_ID)
        } catch (_: Exception) {
            try {
                style.addLayer(layer)
            } catch (_: Exception) {
            }
        }
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

    companion object {
        private const val BELOW_LAYER_ID = "polyline-layer"
    }
}
