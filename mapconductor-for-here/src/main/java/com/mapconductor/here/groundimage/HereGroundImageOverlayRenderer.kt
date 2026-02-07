package com.mapconductor.here.groundimage

import com.here.sdk.mapview.MapContentType
import com.here.sdk.mapview.MapLayerBuilder
import com.here.sdk.mapview.datasource.RasterDataSource
import com.here.sdk.mapview.datasource.RasterDataSourceConfiguration
import com.here.sdk.mapview.datasource.TileUrlProviderFactory
import com.here.sdk.mapview.datasource.TilingScheme
import com.mapconductor.core.groundimage.AbstractGroundImageOverlayRenderer
import com.mapconductor.core.groundimage.GroundImageEntityInterface
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.GroundImageTileProvider
import com.mapconductor.core.tileserver.LocalTileServer
import com.mapconductor.here.HereActualGroundImage
import com.mapconductor.here.HereViewHolder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HereGroundImageOverlayRenderer(
    override val holder: HereViewHolder,
    private val tileServer: LocalTileServer,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractGroundImageOverlayRenderer<HereActualGroundImage>() {
    override suspend fun createGroundImage(state: GroundImageState): HereActualGroundImage? =
        withContext(coroutine.coroutineContext) {
            val routeId = buildSafeRouteId(state.id)
            val provider = GroundImageTileProvider(tileSize = state.tileSize)
            provider.update(state, opacity = state.opacity)
            tileServer.register(routeId, provider)

            val handle =
                createHandle(
                    routeId = routeId,
                    generation = 0L,
                    cacheKey = tileCacheKey(state),
                    provider = provider,
                ) ?: return@withContext null

            try {
                handle.layer.setEnabled(true)
            } catch (_: Exception) {
            }
            handle
        }

    override suspend fun updateGroundImageProperties(
        groundImage: HereActualGroundImage,
        current: GroundImageEntityInterface<HereActualGroundImage>,
        prev: GroundImageEntityInterface<HereActualGroundImage>,
    ): HereActualGroundImage? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint

            val tileNeedsRefresh =
                finger.bounds != prevFinger.bounds ||
                    finger.image != prevFinger.image ||
                    finger.opacity != prevFinger.opacity ||
                    finger.tileSize != prevFinger.tileSize

            if (!tileNeedsRefresh) {
                return@withContext groundImage
            }

            val provider =
                if (finger.tileSize != prevFinger.tileSize) {
                    GroundImageTileProvider(tileSize = current.state.tileSize).also {
                        tileServer.register(groundImage.routeId, it)
                    }
                } else {
                    groundImage.tileProvider
                }
            provider.update(current.state, opacity = current.state.opacity)
            val nextGeneration = groundImage.generation + 1L

            removeHandle(groundImage)
            val nextHandle =
                createHandle(
                    routeId = groundImage.routeId,
                    generation = nextGeneration,
                    cacheKey = tileCacheKey(current.state),
                    provider = provider,
                ) ?: return@withContext null

            try {
                nextHandle.layer.setEnabled(true)
            } catch (_: Exception) {
            }
            nextHandle
        }

    override suspend fun removeGroundImage(entity: GroundImageEntityInterface<HereActualGroundImage>) {
        coroutine.launch {
            val handle = entity.groundImage
            removeHandle(handle)
            tileServer.unregister(handle.routeId)
        }
    }

    private fun createHandle(
        routeId: String,
        generation: Long,
        cacheKey: String,
        provider: GroundImageTileProvider,
    ): HereGroundImageHandle? {
        val urlTemplate = tileServer.urlTemplate(routeId, provider.tileSize, cacheKey)
        val urlProvider =
            TileUrlProviderFactory.fromXyzUrlTemplate(urlTemplate)
                ?: return null

        val providerConfig =
            RasterDataSourceConfiguration
                .Provider(
                    urlProvider,
                    TilingScheme.QUAD_TREE_MERCATOR,
                    STORAGE_LEVELS,
                ).apply {
                    hasAlphaChannel = true
                }

        val cache =
            RasterDataSourceConfiguration.Cache(
                holder.mapView.context.cacheDir.absolutePath,
            )

        val sourceName = "groundimage-source-$routeId"
        val layerName = "groundimage-layer-$routeId"
        val config = RasterDataSourceConfiguration(sourceName, providerConfig, cache)
        val dataSource = RasterDataSource(holder.mapView.mapContext, config)

        return try {
            val layer =
                MapLayerBuilder()
                    .withName(layerName)
                    .withDataSource(config.name, MapContentType.RASTER_IMAGE)
                    .forMap(holder.mapView.hereMap)
                    .build()
            HereGroundImageHandle(
                routeId = routeId,
                generation = generation,
                cacheKey = cacheKey,
                sourceName = sourceName,
                layerName = layerName,
                dataSource = dataSource,
                layer = layer,
                tileProvider = provider,
            )
        } catch (e: MapLayerBuilder.InstantiationException) {
            dataSource.destroy()
            Log.w("HERE", "Failed to create ground image layer: ${e.message}")
            null
        }
    }

    private fun removeHandle(handle: HereGroundImageHandle) {
        try {
            handle.layer.destroy()
        } catch (_: Exception) {
        }
        try {
            handle.dataSource.destroy()
        } catch (_: Exception) {
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

    private fun tileCacheKey(state: GroundImageState): String = state.fingerPrint().hashCode().toString()

    companion object {
        private val STORAGE_LEVELS: List<Int> = (0..22).toList()
    }
}
