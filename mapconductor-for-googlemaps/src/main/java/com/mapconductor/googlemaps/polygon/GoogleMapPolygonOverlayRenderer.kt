package com.mapconductor.googlemaps.polygon

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
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
import com.mapconductor.googlemaps.AdaptiveInterpolation
import com.mapconductor.googlemaps.GoogleMapActualPolygon
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.LatLngInterpolationCache
import com.mapconductor.googlemaps.raster.GoogleMapRasterLayerController
import com.mapconductor.googlemaps.toLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleMapPolygonOverlayRenderer(
    override val holder: GoogleMapViewHolder,
    private val rasterLayerController: GoogleMapRasterLayerController,
    private val tileServer: LocalTileServer = TileServerRegistry.get(forceNoStoreCache = true),
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<GoogleMapActualPolygon>() {
    private val interpolationCache = LatLngInterpolationCache(maxEntries = 64)

    private data class MaskHandle(
        val routeId: String,
        val provider: PolygonRasterTileRenderer,
        val rasterLayerId: String,
        var cacheVersion: Int,
    )

    private val masks = HashMap<String, MaskHandle>()

    private fun geodesicPoints(statePoints: List<GeoPointInterface>): List<LatLng> {
        val camera = holder.map.cameraPosition
        val maxSegmentLength =
            AdaptiveInterpolation.maxSegmentLengthMeters(
                zoom = camera.zoom,
                latitude = camera.target.latitude,
            )
        val key = AdaptiveInterpolation.cacheKey(AdaptiveInterpolation.pointsHash(statePoints), maxSegmentLength)
        interpolationCache.get(key)?.let { return it }

        val geoPoints = createInterpolatePoints(statePoints, maxSegmentLength = maxSegmentLength)
        val points = geoPoints.map { GeoPoint.from(it).toLatLng() }
        interpolationCache.put(key, points)
        return points
    }

    private fun toLatLngRing(
        statePoints: List<GeoPointInterface>,
        geodesic: Boolean,
    ): List<LatLng> =
        when (geodesic) {
            true -> geodesicPoints(statePoints)
            false -> createLinearInterpolatePoints(statePoints).map { GeoPoint.from(it).toLatLng() }
        }

    private fun toLatLngHoles(state: PolygonState): List<List<LatLng>> =
        state.holes
            .map { ring -> toLatLngRing(ring, state.geodesic) }
            .filter { it.size >= 3 }

    override suspend fun removePolygon(entity: PolygonEntityInterface<GoogleMapActualPolygon>) {
        coroutine.launch {
            entity.polygon.remove()
        }
        removeMaskLayer(entity.state.id)
    }

    override suspend fun createPolygon(state: PolygonState) =
        withContext(coroutine.coroutineContext) {
            val hasHoles = state.holes.isNotEmpty()
            if (hasHoles) {
                ensureMaskLayer(state, forceRecreate = true)
            } else {
                removeMaskLayer(state.id)
            }

            val points: List<LatLng> = toLatLngRing(state.points, state.geodesic)
            val options =
                PolygonOptions()
                    .addAll(points)
                    .apply { if (!hasHoles) toLatLngHoles(state).forEach { addHole(it) } }
                    .strokeColor(state.strokeColor.toArgb())
                    .strokeWidth(ResourceProvider.dpToPx(state.strokeWidth).toFloat())
                    .fillColor(if (hasHoles) android.graphics.Color.TRANSPARENT else state.fillColor.toArgb())
                    .zIndex(state.zIndex.toFloat())
                    .clickable(false)
            holder.map.addPolygon(options)?.also {
                it.tag = state.id
            }
        }

    override suspend fun updatePolygonProperties(
        polygon: GoogleMapActualPolygon,
        current: PolygonEntityInterface<GoogleMapActualPolygon>,
        prev: PolygonEntityInterface<GoogleMapActualPolygon>,
    ): GoogleMapActualPolygon? =
        withContext(coroutine.coroutineContext) {
            val polygon = current.polygon
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint
            val hasHoles = current.state.holes.isNotEmpty()
            if (hasHoles && finger != prevFinger) {
                ensureMaskLayer(current.state, forceRecreate = true)
            }
            if (
                finger.points != prevFinger.points ||
                finger.holes != prevFinger.holes ||
                finger.geodesic != prevFinger.geodesic
            ) {
                polygon.points = toLatLngRing(current.state.points, current.state.geodesic)
                polygon.holes = if (hasHoles) emptyList() else toLatLngHoles(current.state)
                if (hasHoles) {
                    ensureMaskLayer(current.state, forceRecreate = true)
                } else {
                    removeMaskLayer(current.state.id)
                }
            }
            if (hasHoles) {
                polygon.strokeWidth = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
                polygon.strokeColor = current.state.strokeColor.toArgb()
                polygon.fillColor = android.graphics.Color.TRANSPARENT
            } else {
                polygon.strokeWidth = ResourceProvider.dpToPx(current.state.strokeWidth).toFloat()
                polygon.strokeColor = current.state.strokeColor.toArgb()
                polygon.fillColor = current.state.fillColor.toArgb()
            }
            if (finger.zIndex != prevFinger.zIndex) {
                polygon.zIndex = current.state.zIndex.toFloat()
            }
            polygon
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
                tileSizePx = 256,
            )
        updateMaskBounds(provider, state)
        tileServer.register(routeId, provider)

        val cacheVersion = ((System.nanoTime() / 1_000_000) and 0x7fffffff).toInt()
        val urlTemplate = tileServer.urlTemplate(routeId, 256, cacheVersion.toString())
        val rasterState =
            RasterLayerState(
                source =
                    RasterLayerSource.UrlTemplate(
                        template = urlTemplate,
                        tileSize = 256,
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
        provider.outerBounds = GeoRectBounds().also { b -> state.points.forEach { b.extend(it) } }
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
}
