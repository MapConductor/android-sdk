package com.mapconductor.googlemaps.raster

import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.UrlTileProvider
import com.mapconductor.core.raster.RasterLayerEntity
import com.mapconductor.core.raster.RasterLayerOverlayRenderer
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.RasterSource
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.googlemaps.GoogleMapViewHolder
import java.net.URL
import android.util.Log

class GoogleMapRasterLayerOverlayRenderer(
    private val holder: GoogleMapViewHolder,
) : RasterLayerOverlayRenderer<TileOverlay> {
    override suspend fun onAdd(data: List<RasterLayerOverlayRenderer.AddParams>): List<TileOverlay?> =
        data.map { params ->
            addLayer(params.state)
        }

    override suspend fun onChange(
        data: List<RasterLayerOverlayRenderer.ChangeParams<TileOverlay>>,
    ): List<TileOverlay?> =
        data.map { params ->
            val prev = params.prev
            val next = params.current.state
            if (prev.state.source != next.source) {
                prev.layer.remove()
                addLayer(next)
            } else {
                updateLayer(prev.layer, next)
                prev.layer
            }
        }

    override suspend fun onRemove(data: List<RasterLayerEntity<TileOverlay>>) {
        data.forEach { entity ->
            entity.layer.remove()
        }
    }

    override suspend fun onPostProcess() {}

    private fun addLayer(state: RasterLayerState): TileOverlay? {
        val tileSpec = resolveTileSpec(state) ?: return null
        val provider =
            object : UrlTileProvider(tileSpec.tileSize, tileSpec.tileSize) {
                override fun getTileUrl(
                    x: Int,
                    y: Int,
                    zoom: Int,
                ): URL? {
                    val schemeY =
                        if (tileSpec.scheme == TileScheme.TMS) {
                            val max = 1 shl zoom
                            (max - 1 - y)
                        } else {
                            y
                        }
                    val url =
                        tileSpec.template
                            .replace("{x}", x.toString())
                            .replace("{y}", schemeY.toString())
                            .replace("{z}", zoom.toString())
                    Log.d("DEBUG", url)
                    return URL(url)
                }
            }
        val options =
            TileOverlayOptions()
                .tileProvider(provider)
                .transparency(opacityToTransparency(state.opacity))
                .visible(state.visible)
        return holder.map.addTileOverlay(options)
    }

    private fun updateLayer(
        overlay: TileOverlay,
        state: RasterLayerState,
    ) {
        overlay.isVisible = state.visible
        overlay.transparency = opacityToTransparency(state.opacity)
    }

    private fun resolveTileSpec(state: RasterLayerState): TileSpec? =
        when (val source = state.source) {
            is RasterSource.UrlTemplate ->
                TileSpec(
                    template = source.template,
                    tileSize = source.tileSize,
                    scheme = source.scheme,
                )
            is RasterSource.ArcGisService -> {
                val base = source.serviceUrl.trimEnd('/')
                TileSpec(
                    template = "$base/tile/{z}/{y}/{x}",
                    tileSize = RasterSource.DEFAULT_TILE_SIZE,
                    scheme = TileScheme.XYZ,
                )
            }
            is RasterSource.TileJson -> {
                Log.w("MapConductor", "Google Maps does not support TileJson raster sources.")
                null
            }
        }

    private fun opacityToTransparency(opacity: Float): Float =
        (1.0f - opacity.coerceIn(0.0f, 1.0f)).coerceIn(0.0f, 1.0f)

    private data class TileSpec(
        val template: String,
        val tileSize: Int,
        val scheme: TileScheme,
    )
}
