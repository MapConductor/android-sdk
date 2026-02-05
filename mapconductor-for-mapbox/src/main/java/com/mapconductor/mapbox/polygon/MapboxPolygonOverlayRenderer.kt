package com.mapconductor.mapbox.polygon

import com.mapconductor.core.features.GeoPointInterface
import androidx.compose.ui.graphics.Color
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonEntityInterface
import com.mapconductor.core.polygon.PolygonManagerInterface
import com.mapconductor.core.polygon.PolygonRasterTileRenderer
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.tileserver.LocalTileServer
import com.mapconductor.core.tileserver.TileServerRegistry
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.mapbox.MapboxActualPolygon
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.createMapboxPolygons
import com.mapconductor.mapbox.raster.MapboxRasterLayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapboxPolygonOverlayRenderer(
    val layer: MapboxPolygonLayer,
    val polygonManager: PolygonManagerInterface<MapboxActualPolygon>,
    override val holder: MapboxMapViewHolder,
    private val rasterLayerController: MapboxRasterLayerController,
    private val tileServer: LocalTileServer = TileServerRegistry.get(forceNoStoreCache = true),
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<MapboxActualPolygon>() {
    private data class MaskHandle(
        val routeId: String,
        val provider: PolygonRasterTileRenderer,
        val rasterLayerId: String,
        var cacheVersion: Int,
    )

    private val masks = HashMap<String, MaskHandle>()
    override suspend fun onRemove(data: List<PolygonEntityInterface<MapboxActualPolygon>>) {
//        val featureIds = data.map { entity ->
//            entity.polygon.getStringProperty("id")
//        }
//        layer.source.removeGeoJSONSourceFeatures(featureIds)
    }

    override suspend fun onPostProcess() {
        val polygons = getAllPolygonEntities()
        coroutine.launch {
            this@MapboxPolygonOverlayRenderer.layer.draw(polygons)
        }
    }

    override suspend fun removePolygon(entity: PolygonEntityInterface<MapboxActualPolygon>) {
//        val featureIds =
//            listOf(entity.polygon.getStringProperty("id"))
//        layer.source.removeGeoJSONSourceFeatures(featureIds)
        removeMaskLayer(entity.state.id)
    }

    override suspend fun createPolygon(state: PolygonState): MapboxActualPolygon? =
        if (state.holes.isEmpty()) {
            removeMaskLayer(state.id)
            createMapboxPolygons(
                id = state.id,
                points = state.points,
                holes = state.holes,
                geodesic = state.geodesic,
                fillColor = state.fillColor,
                zIndex = state.zIndex,
            )
        } else {
            ensureMaskLayer(state, forceRecreate = true)
            createMapboxPolygons(
                id = state.id,
                points = state.points,
                holes = emptyList(),
                geodesic = state.geodesic,
                fillColor = Color.Transparent,
                zIndex = state.zIndex,
            )
        }

    override suspend fun updatePolygonProperties(
        polygon: MapboxActualPolygon,
        current: PolygonEntityInterface<MapboxActualPolygon>,
        prev: PolygonEntityInterface<MapboxActualPolygon>,
    ): MapboxActualPolygon? {
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
        provider.fillColor = state.fillColor.toMapboxColorInt()
        provider.strokeColor = android.graphics.Color.TRANSPARENT
        provider.strokeWidthPx = 0f
        provider.geodesic = state.geodesic
        provider.outerBounds = com.mapconductor.core.features.GeoRectBounds().also { b ->
            state.points.forEach { b.extend(it) }
        }
    }

    private fun Color.toMapboxColorInt(): Int =
        android.graphics.Color.argb(
            (alpha * 255).toInt(),
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt(),
        )

    private fun safeId(id: String): String =
        id.map { ch ->
            when {
                ch.isLetterOrDigit() -> ch
                ch == '-' || ch == '_' || ch == '.' -> ch
                else -> '_'
            }
        }.joinToString("")

    /**
     * Creates geodesic polygon points by interpolating between each consecutive pair of vertices.
     * This ensures that polygon edges follow great circle paths instead of straight lines.
     *
     * @param points Original polygon vertices
     * @param maxSegmentLength Maximum distance between interpolated points in meters
     * @return List of points with interpolated vertices along geodesic paths
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
            if (distance <= maxSegmentLength) {
                continue
            }

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

    private fun getAllPolygonEntities(): List<PolygonEntityInterface<MapboxActualPolygon>> {
        // This would need access to the polygon manager
        // For now, we'll implement a simple workaround
        return polygonManager.allEntities()
    }
}
