package com.mapconductor.here.raster

import com.here.sdk.mapview.MapContentType
import com.here.sdk.mapview.MapLayer
import com.here.sdk.mapview.MapLayerBuilder
import com.here.sdk.mapview.datasource.RasterDataSource
import com.here.sdk.mapview.datasource.RasterDataSourceConfiguration
import com.here.sdk.mapview.datasource.TileUrlProviderCallback
import com.here.sdk.mapview.datasource.TileUrlProviderFactory
import com.here.sdk.mapview.datasource.TilingScheme
import com.mapconductor.core.raster.RasterLayerEntityInterface
import com.mapconductor.core.raster.RasterLayerOverlayRendererInterface
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.here.HereViewHolder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class HereRasterLayerOverlayRenderer(
    private val holder: HereViewHolder,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : RasterLayerOverlayRendererInterface<HereRasterLayerHandle> {
    override suspend fun onAdd(
        data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>,
    ): List<HereRasterLayerHandle?> =
        data.map { params ->
            addLayer(params.state)
        }

    override suspend fun onChange(
        data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<HereRasterLayerHandle>>,
    ): List<HereRasterLayerHandle?> =
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

    override suspend fun onRemove(data: List<RasterLayerEntityInterface<HereRasterLayerHandle>>) {
        data.forEach { entity ->
            removeLayer(entity)
        }
    }

    override suspend fun onPostProcess() {}

    private fun addLayer(state: RasterLayerState): HereRasterLayerHandle? {
        val tileSpec = resolveTileSpec(state)
        if (tileSpec == null) {
            Log.e("HereRasterLayer", "resolveTileSpec returned null!")
            return null
        }
        val urlProvider = tileSpec.provider
        val storageLevels = tileSpec.storageLevels
        val provider =
            RasterDataSourceConfiguration.Provider(
                urlProvider,
                TilingScheme.QUAD_TREE_MERCATOR,
                storageLevels,
            )
        provider.hasAlphaChannel = true
        val cache =
            RasterDataSourceConfiguration.Cache(
                holder.mapView.context.cacheDir.absolutePath,
            )
        val config =
            RasterDataSourceConfiguration(
                tileSpec.sourceName,
                provider,
                cache,
            )
        val dataSource = RasterDataSource(holder.mapView.mapContext, config)

        return try {
            val layer =
                MapLayerBuilder()
                    .withName(tileSpec.layerName)
                    .withDataSource(config.name, MapContentType.RASTER_IMAGE)
                    .forMap(holder.mapView.hereMap)
                    .build()
            layer.setEnabled(state.visible)
            HereRasterLayerHandle(
                dataSource = dataSource,
                layer = layer,
                sourceName = tileSpec.sourceName,
                layerName = tileSpec.layerName,
            )
        } catch (e: MapLayerBuilder.InstantiationException) {
            dataSource.destroy()
            Log.e("HereRasterLayer", "Failed to create raster layer: ${e.message}", e)
            null
        }
    }

    private fun updateLayer(
        handle: HereRasterLayerHandle,
        state: RasterLayerState,
    ) {
        handle.layer.setEnabled(state.visible)
    }

    private fun removeLayer(entity: RasterLayerEntityInterface<HereRasterLayerHandle>) {
        val handle = entity.layer
        handle.layer.destroy()
        handle.dataSource.destroy()
    }

    private fun resolveTileSpec(state: RasterLayerState): TileSpec? =
        when (val source = state.source) {
            is RasterLayerSource.UrlTemplate -> {
                val provider =
                    if (source.scheme == TileScheme.TMS) {
                        TileUrlProviderCallback { x, y, zoom ->
                            val max = 1 shl zoom
                            val tmsY = max - 1 - y
                            source.template
                                .replace("{x}", x.toString())
                                .replace("{y}", tmsY.toString())
                                .replace("{z}", zoom.toString())
                        }
                    } else {
                        // TileUrlProviderFactory can return null for templates with query params etc.
                        // Fall back to simple placeholder replacement.
                        val factoryProvider = TileUrlProviderFactory.fromXyzUrlTemplate(source.template)
                        if (factoryProvider != null) {
                            factoryProvider
                        } else {
                            TileUrlProviderCallback { x, y, zoom ->
                                val url =
                                    source.template
                                        .replace("{x}", x.toString())
                                        .replace("{y}", y.toString())
                                        .replace("{z}", zoom.toString())
                                url
                            }
                        }
                    }
                TileSpec(
                    provider = provider,
                    sourceName = "raster-source-${state.id}",
                    layerName = "raster-layer-${state.id}",
                    storageLevels = buildStorageLevels(source.minZoom, source.maxZoom),
                )
            }
            is RasterLayerSource.TileJson -> {
                Log.w("HereRasterLayer", "HERE SDK does not support TileJson raster sources.")
                null
            }
            is RasterLayerSource.ArcGisService -> {
                val base = source.serviceUrl.trimEnd('/')
                val template = "$base/tile/{z}/{y}/{x}"
                val provider =
                    TileUrlProviderFactory.fromXyzUrlTemplate(template)
                        ?: return null
                TileSpec(
                    provider = provider,
                    sourceName = "raster-source-${state.id}",
                    layerName = "raster-layer-${state.id}",
                    storageLevels = buildStorageLevels(null, null),
                )
            }
        }

    private fun buildStorageLevels(
        minZoom: Int?,
        maxZoom: Int?,
    ): List<Int> {
        val min = minZoom ?: 0
        val max = maxZoom ?: 20
        return (min..max).toList()
    }

    private data class TileSpec(
        val provider: TileUrlProviderCallback,
        val sourceName: String,
        val layerName: String,
        val storageLevels: List<Int>,
    )
}

data class HereRasterLayerHandle(
    val dataSource: RasterDataSource,
    val layer: MapLayer,
    val sourceName: String,
    val layerName: String,
)
