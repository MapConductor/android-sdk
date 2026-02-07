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
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleMapRasterLayerOverlayRenderer(
    private val holder: GoogleMapViewHolder,
    private val okHttpClient: OkHttpClient,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : RasterLayerOverlayRendererInterface<TileOverlay> {
    private fun isMarkerTileRaster(state: RasterLayerState): Boolean = state.id.startsWith(MARKER_TILE_RASTER_ID_PREFIX)

    private fun resolveOverlayZIndex(state: RasterLayerState): Float =
        if (isMarkerTileRaster(state)) {
            // Backward-compatible behavior: marker tiles stay above most overlays.
            999f
        } else {
            state.zIndex.toFloat()
        }

    override suspend fun onAdd(data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>): List<TileOverlay?> =
        withContext(coroutine.coroutineContext) {
            data.map { params ->
                addLayer(params.state)
            }
        }

    override suspend fun onChange(
        data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<TileOverlay>>,
    ): List<TileOverlay?> =
        withContext(coroutine.coroutineContext) {
            data.map { params ->
                val prev = params.prev
                val next = params.current.state
                if (prev.state.source != next.source || prev.state.debug != next.debug) {
                    prev.layer.remove()
                    addLayer(next)
                } else {
                    updateLayer(prev.layer, next)
                    prev.layer
                }
            }
        }

    override suspend fun onRemove(data: List<RasterLayerEntityInterface<TileOverlay>>) {
        withContext(coroutine.coroutineContext) {
            data.forEach { entity ->
                entity.layer.remove()
            }
        }
    }

    override suspend fun onPostProcess() {}

    private fun addLayer(state: RasterLayerState): TileOverlay? {
        val tileSpec = resolveTileSpec(state) ?: return null
        val headerBuilder =
            Headers.Builder().also { builder ->
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

                    val request: Request =
                        Request
                            .Builder()
                            .url(url)
                            .also { builder ->
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
                            val renderedBytes =
                                if (state.debug) {
                                    addDebugOverlay(
                                        input = imageBytes,
                                        tileSize = tileSpec.tileSize,
                                        x = x,
                                        y = schemeY,
                                        zoom = zoom,
                                        scheme = tileSpec.scheme,
                                    )
                                } else {
                                    imageBytes
                                }

                            // Return a new Tile with the image data
                            return Tile(tileSpec.tileSize, tileSpec.tileSize, renderedBytes)
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
                .zIndex(resolveOverlayZIndex(state))
                .transparency(opacityToTransparency(state.opacity))
                .visible(state.visible)
        return holder.map.addTileOverlay(options)?.also { overlay ->
            // Google Maps caches tile results (including NO_TILE) per viewport; clear to reflect data changes.
            overlay.clearTileCache()
        }
    }

    private fun updateLayer(
        overlay: TileOverlay,
        state: RasterLayerState,
    ) {
        overlay.isVisible = state.visible
        overlay.transparency = opacityToTransparency(state.opacity)
        overlay.zIndex = resolveOverlayZIndex(state)
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

    private companion object {
        private const val MARKER_TILE_RASTER_ID_PREFIX = "marker-tile-"
    }

    private fun addDebugOverlay(
        input: ByteArray,
        tileSize: Int,
        x: Int,
        y: Int,
        zoom: Int,
        scheme: TileScheme,
    ): ByteArray {
        val decoded =
            BitmapFactory.decodeByteArray(input, 0, input.size)
                ?: return input
        val bitmap =
            when {
                decoded.config != Bitmap.Config.ARGB_8888 ->
                    decoded.copy(Bitmap.Config.ARGB_8888, true).also {
                        decoded
                            .recycle()
                    }
                !decoded.isMutable -> decoded.copy(Bitmap.Config.ARGB_8888, true).also { decoded.recycle() }
                else -> decoded
            }

        val canvas = Canvas(bitmap)
        val strokePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.MAGENTA
                strokeWidth = 2f
            }
        // Draw top and left edges prominently, plus full border.
        canvas.drawRect(0f, 0f, (tileSize - 1).toFloat(), (tileSize - 1).toFloat(), strokePaint)
        canvas.drawLine(0f, 0f, (tileSize - 1).toFloat(), 0f, strokePaint)
        canvas.drawLine(0f, 0f, 0f, (tileSize - 1).toFloat(), strokePaint)

        val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.MAGENTA
                textSize = 22f
                typeface = Typeface.MONOSPACE
                style = Paint.Style.FILL
            }
        val bgPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(140, 0, 0, 0)
                style = Paint.Style.FILL
            }

        val line1 = "z=$zoom x=$x y=$y ${scheme.name}"
        val line2 = "size=$tileSize bytes=${input.size}"
        val padding = 6f
        val lineHeight = textPaint.fontMetrics.run { (descent - ascent) }
        val boxW = maxOf(textPaint.measureText(line1), textPaint.measureText(line2)) + padding * 2
        val boxH = lineHeight * 2 + padding * 3
        canvas.drawRect(0f, 0f, boxW, boxH, bgPaint)
        val baseY = padding - textPaint.fontMetrics.ascent
        canvas.drawText(line1, padding, baseY, textPaint)
        canvas.drawText(line2, padding, baseY + lineHeight + padding, textPaint)

        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        bitmap.recycle()
        return out.toByteArray()
    }
}
