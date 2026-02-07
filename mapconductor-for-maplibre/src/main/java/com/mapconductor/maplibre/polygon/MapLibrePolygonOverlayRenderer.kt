package com.mapconductor.maplibre.polygon

import androidx.compose.ui.graphics.Color
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonEntityInterface
import com.mapconductor.core.polygon.PolygonManagerInterface
import com.mapconductor.core.polygon.PolygonRasterTileRenderer
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.core.tileserver.LocalTileServer
import com.mapconductor.core.tileserver.TileServerRegistry
import com.mapconductor.maplibre.MapLibreActualPolygon
import com.mapconductor.maplibre.MapLibreMapViewHolderInterface
import com.mapconductor.maplibre.createMapLibrePolygons
import com.mapconductor.maplibre.raster.MapLibreRasterLayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapLibrePolygonOverlayRenderer(
    val layer: MapLibrePolygonLayer,
    val polygonManager: PolygonManagerInterface<MapLibreActualPolygon>,
    override val holder: MapLibreMapViewHolderInterface,
    private val rasterLayerController: MapLibreRasterLayerController,
    private val tileServer: LocalTileServer = TileServerRegistry.get(forceNoStoreCache = true),
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<MapLibreActualPolygon>() {
    private data class MaskHandle(
        val routeId: String,
        val provider: PolygonRasterTileRenderer,
        val rasterLayerId: String,
        var cacheVersion: Int,
    )

    private val masks = HashMap<String, MaskHandle>()

    override suspend fun onRemove(data: List<PolygonEntityInterface<MapLibreActualPolygon>>) {
        // Actual removal handled by redrawing remaining polygons in onPostProcess
    }

    override suspend fun onPostProcess() {
        val polygons = getAllPolygonEntities()
        val style = holder.getController()?.getStyleInstance() ?: holder.map.style
        style?.let {
            coroutine.launch {
                this@MapLibrePolygonOverlayRenderer.layer.draw(polygons, it)
            }
        }
    }

    override suspend fun removePolygon(entity: PolygonEntityInterface<MapLibreActualPolygon>) {
        // No-op; we redraw full collection
        removeMaskLayer(entity.state.id)
    }

    override suspend fun createPolygon(state: PolygonState): MapLibreActualPolygon? =
        if (state.holes.isEmpty()) {
            removeMaskLayer(state.id)
            createMapLibrePolygons(
                id = state.id,
                points = state.points,
                holes = state.holes,
                geodesic = state.geodesic,
                fillColor = state.fillColor,
                zIndex = state.zIndex,
            )
        } else {
            ensureMaskLayer(state, forceRecreate = true)
            createMapLibrePolygons(
                id = state.id,
                points = state.points,
                holes = emptyList(),
                geodesic = state.geodesic,
                fillColor = Color.Transparent,
                zIndex = state.zIndex,
            )
        }

    override suspend fun updatePolygonProperties(
        polygon: MapLibreActualPolygon,
        current: PolygonEntityInterface<MapLibreActualPolygon>,
        prev: PolygonEntityInterface<MapLibreActualPolygon>,
    ): MapLibreActualPolygon? {
        val finger = current.fingerPrint
        val prevFinger = prev.fingerPrint

        if (finger != prevFinger) {
            // Recreate features when any polygon property changes
            return createPolygon(current.state)
        }
        return prev.polygon
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
        provider.fillColor = state.fillColor.toMapLibreColorInt()
        provider.strokeColor = android.graphics.Color.TRANSPARENT
        provider.strokeWidthPx = 0f
        provider.geodesic = state.geodesic
        provider.outerBounds =
            com.mapconductor.core.features.GeoRectBounds().also { b ->
                state.points.forEach { b.extend(it) }
            }
    }

    private fun Color.toMapLibreColorInt(): Int =
        android.graphics.Color.argb(
            (alpha * 255).toInt(),
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt(),
        )

    private fun safeId(id: String): String =
        id
            .map { ch ->
                when {
                    ch.isLetterOrDigit() -> ch
                    ch == '-' || ch == '_' || ch == '.' -> ch
                    else -> '_'
                }
            }.joinToString("")

    /**
     * Creates geodesic polygon points by interpolating between each consecutive pair of vertices.
     */
    private fun createGeodesicPolygonPoints(
        points: List<GeoPointInterface>,
        maxSegmentLength: Double = 1000.0,
    ): List<GeoPointInterface> {
        if (points.size < 3) return points

        val results = mutableListOf<GeoPointInterface>()

        for (i in points.indices) {
            val currentPoint = points[i]
            val nextPoint = points[(i + 1) % points.size] // Wrap around to create closed polygon

            results.add(currentPoint)

            // Calculate distance between current and next point
            val distance = Spherical.computeDistanceBetween(currentPoint, nextPoint)

            // Skip interpolation if points are very close
            if (distance <= maxSegmentLength) continue

            // Calculate number of interpolation segments needed
            val numSegments = (distance / maxSegmentLength).toInt().coerceAtLeast(1)
            val step = 1.0 / numSegments

            // Add interpolated points between current and next vertex
            var fraction = step
            while (fraction < 1.0) {
                val interpolatedPoint = Spherical.sphericalInterpolate(currentPoint, nextPoint, fraction)
                results.add(interpolatedPoint)
                fraction += step
            }
        }

        return results
    }

    private fun getAllPolygonEntities(): List<PolygonEntityInterface<MapLibreActualPolygon>> =
        polygonManager
            .allEntities()
}
