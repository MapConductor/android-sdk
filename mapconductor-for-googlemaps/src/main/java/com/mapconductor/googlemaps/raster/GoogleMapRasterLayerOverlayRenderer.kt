package com.mapconductor.googlemaps.raster

import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.google.android.gms.maps.model.TileProvider.NO_TILE
import com.mapconductor.core.raster.RasterLayerEntityInterface
import com.mapconductor.core.raster.RasterLayerOverlayRendererInterface
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.googlemaps.GoogleMapViewHolder
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import android.util.Log

class GoogleMapRasterLayerOverlayRenderer(
    private val holder: GoogleMapViewHolder,
    private val okHttpClient: OkHttpClient,
) : RasterLayerOverlayRendererInterface<TileOverlay> {
    override suspend fun onAdd(data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>): List<TileOverlay?> =
        data.map { params ->
            addLayer(params.state)
        }

    override suspend fun onChange(
        data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<TileOverlay>>,
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

    override suspend fun onRemove(data: List<RasterLayerEntityInterface<TileOverlay>>) {
        data.forEach { entity ->
            entity.layer.remove()
        }
    }

    override suspend fun onPostProcess() {}

    private fun addLayer(state: RasterLayerState): TileOverlay? {
        val tileSpec = resolveTileSpec(state) ?: return null
        val headerBuilder = Headers.Builder().also { builder ->
            state.extraHeaders?.let {
                it.forEach { (name, value) ->
                    builder.add(name, value)
                }
            }
        }

        if (state.userAgent?.trim()?.isNotEmpty() == true) {
            headerBuilder.set("User-Agent", state.userAgent!!)
        } else {
            val context = holder.mapView.context
            val userAgent = "Android App(${context.packageName}) powered by MapConductor"
            headerBuilder.set("User-Agent", userAgent)
        }
        val requestHeaders = headerBuilder.build()

        val provider =
            object : TileProvider {
                override fun getTile(
                    x: Int,
                    y: Int,
                    zoom: Int,
                ): Tile {
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

                    val request: Request = Request.Builder().url(url).also { builder ->
                        builder.headers(requestHeaders)
                    }.build()

                    try {
                        okHttpClient.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) {
                                // Return NO_TILE if the request fails
                                return NO_TILE
                            }
                            // Get the image data as a byte array
                            val imageBytes: ByteArray = response.body.bytes()

                            // Return a new Tile with the image data
                            return Tile(tileSpec.tileSize, tileSpec.tileSize, imageBytes)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        // Return NO_TILE in case of network error
                        return NO_TILE
                    }
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
            is RasterLayerSource.UrlTemplate ->
                TileSpec(
                    template = source.template,
                    tileSize = source.tileSize,
                    scheme = source.scheme,
                )
            is RasterLayerSource.ArcGisService -> {
                val base = source.serviceUrl.trimEnd('/')
                TileSpec(
                    template = "$base/tile/{z}/{y}/{x}",
                    tileSize = RasterLayerSource.DEFAULT_TILE_SIZE,
                    scheme = TileScheme.XYZ,
                )
            }
            is RasterLayerSource.TileJson -> {
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
