package com.mapconductor.arcgis.polygon

import androidx.compose.ui.graphics.toArgb
import com.arcgismaps.geometry.Geometry
import com.arcgismaps.geometry.PolygonBuilder
import com.arcgismaps.mapping.symbology.SimpleFillSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.mapconductor.arcgis.ArcGISActualPolygon
import com.mapconductor.arcgis.map.ArcGISMapViewHolder
import com.mapconductor.arcgis.raster.ArcGISRasterLayerController
import com.mapconductor.arcgis.toArcGISColor
import com.mapconductor.arcgis.toPoint
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonEntityInterface
import com.mapconductor.core.polygon.PolygonRasterTileRenderer
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.spherical.createInterpolatePoints
import com.mapconductor.core.spherical.createLinearInterpolatePoints
import com.mapconductor.core.tileserver.LocalTileServer
import com.mapconductor.core.tileserver.TileServerRegistry
import kotlin.collections.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArcGISPolygonOverlayRenderer(
    val polygonLayer: GraphicsOverlay,
    override val holder: ArcGISMapViewHolder,
    private val rasterLayerController: ArcGISRasterLayerController,
    private val tileServer: LocalTileServer = TileServerRegistry.get(forceNoStoreCache = true),
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractPolygonOverlayRenderer<ArcGISActualPolygon>() {
    companion object {
        private const val MASK_TILE_SIZE_PX = 256
    }

    private data class MaskHandle(
        val routeId: String,
        val provider: PolygonRasterTileRenderer,
        val rasterLayerId: String,
        var cacheVersion: Int,
    )

    private val masks = HashMap<String, MaskHandle>()

    override suspend fun createPolygon(state: PolygonState): ArcGISActualPolygon? =
        withContext(coroutine.coroutineContext) {
            val geometry =
                if (state.holes.isEmpty()) {
                    removeMaskLayer(state.id)
                    createGeometry(state)
                } else {
                    ensureMaskLayer(state, forceRecreate = true)
                    createGeometry(state.copy(holes = emptyList()))
                }
            val outlineSymbol =
                SimpleLineSymbol().apply {
                    style = SimpleLineSymbolStyle.Solid
                    color = state.strokeColor.toArcGISColor()
                    width = state.strokeWidth.value.toFloat()
                }

            val fillSymbol =
                SimpleFillSymbol().apply {
                    style = SimpleFillSymbolStyle.Solid
                    color =
                        if (state.holes.isEmpty()) {
                            state.fillColor.toArcGISColor()
                        } else {
                            com.arcgismaps.Color(0)
                        }
                    outline = outlineSymbol
                }

            val graphic =
                Graphic(geometry, fillSymbol).also {
                    it.attributes.set("id", state.id)
                    it.attributes.set("zIndex", state.zIndex)
                }

            polygonLayer.graphics.add(graphic)
            graphic
        }

    override suspend fun updatePolygonProperties(
        polygon: ArcGISActualPolygon,
        current: PolygonEntityInterface<ArcGISActualPolygon>,
        prev: PolygonEntityInterface<ArcGISActualPolygon>,
    ): ArcGISActualPolygon? =
        withContext(coroutine.coroutineContext) {
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint
            if (
                finger.points != prevFinger.points ||
                finger.holes != prevFinger.holes ||
                finger.geodesic != prevFinger.geodesic
            ) {
                current.polygon.geometry =
                    if (current.state.holes.isEmpty()) {
                        removeMaskLayer(current.state.id)
                        createGeometry(current.state)
                    } else {
                        ensureMaskLayer(current.state, forceRecreate = true)
                        createGeometry(current.state.copy(holes = emptyList()))
                    }
            }

            (current.polygon.symbol as SimpleFillSymbol).let { symbol ->
                if (current.state.holes.isNotEmpty()) {
                    ensureMaskLayer(current.state, forceRecreate = true)
                    symbol.color = com.arcgismaps.Color(0)
                    symbol.outline?.let { outline ->
                        outline.color = current.state.strokeColor.toArcGISColor()
                        outline.width = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
                    }
                } else {
                    if (finger.fillColor != prevFinger.fillColor) {
                        symbol.color = current.state.fillColor.toArcGISColor()
                    }
                    symbol.outline?.let { outline ->
                        if (finger.strokeColor != prevFinger.strokeColor) {
                            outline.color = current.state.strokeColor.toArcGISColor()
                        }
                        if (finger.strokeWidth != prevFinger.strokeWidth) {
                            outline.width = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
                        }
                    }
                }
            }
            if (finger.zIndex != prevFinger.zIndex) {
                current.polygon.attributes.set("zIndex", current.state.zIndex)
            }
            polygon
        }

    override suspend fun removePolygon(entity: PolygonEntityInterface<ArcGISActualPolygon>) {
        coroutine.launch {
            polygonLayer.graphics.remove(entity.polygon)
        }
        removeMaskLayer(entity.state.id)
    }

    override suspend fun onPostProcess() {
        // Sort graphics by zIndex to ensure correct rendering order
        withContext(coroutine.coroutineContext) {
            val sortedGraphics =
                polygonLayer.graphics.toList().sortedBy { graphic ->
                    (graphic.attributes.get("zIndex") as? Int) ?: 0
                }
            polygonLayer.graphics.clear()
            polygonLayer.graphics.addAll(sortedGraphics)
        }
    }

    private fun createGeometry(state: PolygonState): Geometry {
        // ArcGIS polygons can become extremely dense when geodesic=true (especially for world-mask rings),
        // which may fail to render. Use a larger segment length to keep the geometry size reasonable.
        val geodesicMaxSegmentLengthMeters = 100_000.0

        fun toRing(
            points: List<GeoPointInterface>,
            geodesic: Boolean,
        ): List<GeoPointInterface> =
            when (geodesic) {
                true -> createInterpolatePoints(points, maxSegmentLength = geodesicMaxSegmentLengthMeters)
                false -> createLinearInterpolatePoints(points)
            }

        fun closeRing(points: List<GeoPointInterface>): List<GeoPointInterface> {
            if (points.isEmpty()) return points
            val first = points.first()
            val last = points.last()
            return if (first.latitude == last.latitude && first.longitude == last.longitude) points else points + first
        }

        val outer: List<GeoPointInterface> =
            closeRing(toRing(state.points, state.geodesic)).let(::ensureClockwise)
        val holes: List<List<GeoPointInterface>> =
            state.holes
                .map { ring -> closeRing(toRing(ring, state.geodesic)).let(::ensureCounterClockwise) }
                .filter { it.size >= 4 }

        // Prefer ArcGIS JSON to support holes without relying on PolygonBuilder part APIs.
        if (holes.isNotEmpty()) {
            val json =
                buildString {
                    append("{\"rings\":[")

                    fun appendRing(ring: List<GeoPointInterface>) {
                        append("[")
                        ring.forEachIndexed { idx, p ->
                            if (idx > 0) append(",")
                            append("[")
                            append(p.longitude)
                            append(",")
                            append(p.latitude)
                            append("]")
                        }
                        append("]")
                    }
                    appendRing(outer)
                    holes.forEach { hole ->
                        append(",")
                        appendRing(hole)
                    }
                    append("],\"spatialReference\":{\"wkid\":4326}}")
                }

            Geometry.fromJsonOrNull(json)?.let { return it }
        }

        val polygonBuilder =
            PolygonBuilder().also { builder ->
                outer.forEach {
                    builder.addPoint(GeoPoint.from(it).toPoint())
                }
            }
        return polygonBuilder.toGeometry()
    }

    private suspend fun ensureMaskLayer(
        state: PolygonState,
        forceRecreate: Boolean = false,
    ) {
        val polygonId = state.id
        val handle = masks[polygonId]
        if (handle != null && !forceRecreate) {
            updateMaskBounds(handle, state)
            return
        }

        if (handle != null) {
            removeMaskLayer(polygonId)
        }

        val routeId = "polygon-raster-" + safeId(polygonId)
        val rasterLayerId = "polygon-raster-$polygonId"
        val provider =
            PolygonRasterTileRenderer(
                tileSizePx = MASK_TILE_SIZE_PX,
            )
        updateMaskBounds(provider, state)
        tileServer.register(routeId, provider)

        val cacheVersion = ((System.nanoTime() / 1_000_000) and 0x7fffffff).toInt()
        val urlTemplate = tileServer.urlTemplate(routeId, MASK_TILE_SIZE_PX, cacheVersion.toString())
        val rasterState =
            RasterLayerState(
                source =
                    RasterLayerSource.UrlTemplate(
                        template = urlTemplate,
                        tileSize = MASK_TILE_SIZE_PX,
                        maxZoom = 22,
                        scheme = TileScheme.XYZ,
                    ),
                opacity = 1.0f,
                visible = true,
                zIndex = state.zIndex,
                id = rasterLayerId,
            )
        rasterLayerController.upsert(rasterState)

        if (!rasterLayerController.rasterLayerManager.hasEntity(rasterLayerId)) {
            tileServer.unregister(routeId)
            return
        }

        masks[polygonId] =
            MaskHandle(
                routeId = routeId,
                provider = provider,
                rasterLayerId = rasterLayerId,
                cacheVersion = cacheVersion,
            )
    }

    private suspend fun removeMaskLayer(polygonId: String) {
        val handle = masks.remove(polygonId) ?: return
        tileServer.unregister(handle.routeId)
        rasterLayerController.removeById(handle.rasterLayerId)
    }

    private fun updateMaskBounds(
        handle: MaskHandle,
        state: PolygonState,
    ) {
        updateMaskBounds(handle.provider, state)
    }

    private fun updateMaskBounds(
        provider: PolygonRasterTileRenderer,
        state: PolygonState,
    ) {
        provider.points = state.points
        provider.holes = state.holes
        provider.fillColor = state.fillColor.toArgb()
        provider.strokeColor = android.graphics.Color.TRANSPARENT
        provider.strokeWidthPx = 0f
        provider.geodesic = state.geodesic
        provider.outerBounds = boundsOf(state.points)
    }

    private fun boundsOf(points: List<GeoPointInterface>): GeoRectBounds? {
        if (points.isEmpty()) return null
        val b = GeoRectBounds()
        points.forEach { b.extend(it) }
        val span = b.toSpan()
        if (span == null) return b
        val padLat = if (span.latitude == 0.0) 1e-6 else 0.0
        val padLon = if (span.longitude == 0.0) 1e-6 else 0.0
        return if (padLat != 0.0 || padLon != 0.0) b.expandedByDegrees(padLat, padLon) else b
    }

    private fun safeId(id: String): String =
        id
            .map { ch ->
                when {
                    ch.isLetterOrDigit() -> ch
                    ch == '-' || ch == '_' || ch == '.' -> ch
                    else -> '_'
                }
            }.joinToString("")

    private fun signedAreaLonLat(ring: List<GeoPointInterface>): Double {
        if (ring.size < 3) return 0.0
        var sum = 0.0
        for (i in 0 until ring.size - 1) {
            val a = ring[i]
            val b = ring[i + 1]
            sum += (a.longitude * b.latitude) - (b.longitude * a.latitude)
        }
        return sum / 2.0
    }

    private fun ensureClockwise(ringClosed: List<GeoPointInterface>): List<GeoPointInterface> =
        if (signedAreaLonLat(ringClosed) < 0.0) ringClosed else ringClosed.asReversed()

    private fun ensureCounterClockwise(ringClosed: List<GeoPointInterface>): List<GeoPointInterface> =
        if (signedAreaLonLat(ringClosed) > 0.0) ringClosed else ringClosed.asReversed()
}
