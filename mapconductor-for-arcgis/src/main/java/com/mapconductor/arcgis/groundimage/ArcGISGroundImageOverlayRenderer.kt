package com.mapconductor.arcgis.groundimage

import com.arcgismaps.arcgisservices.LevelOfDetail
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.layers.Layer
import com.arcgismaps.mapping.layers.TileImageFormat
import com.arcgismaps.mapping.layers.TileInfo
import com.arcgismaps.mapping.layers.WebTiledLayer
import com.mapconductor.arcgis.ArcGISActualGroundImage
import com.mapconductor.arcgis.map.ArcGISMapViewHolder
import com.mapconductor.core.groundimage.AbstractGroundImageOverlayRenderer
import com.mapconductor.core.groundimage.GroundImageEntityInterface
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.groundimage.GroundImageTileProvider
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.tileserver.LocalTileServer
import kotlin.math.PI
import kotlin.math.pow
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArcGISGroundImageOverlayRenderer(
    override val holder: ArcGISMapViewHolder,
    private val tileServer: LocalTileServer,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : AbstractGroundImageOverlayRenderer<ArcGISActualGroundImage>() {
    @Volatile
    private var cameraPosition: MapCameraPosition? = null

    override suspend fun createGroundImage(state: GroundImageState): ArcGISActualGroundImage? =
        withContext(coroutine.coroutineContext) {
            val scene = holder.map.scene ?: return@withContext null
            val routeId = buildSafeRouteId(state.id)
            // ArcGIS WebTiledLayer is most compatible with common 256px WebMercator LODs.
            val provider = GroundImageTileProvider(tileSize = DEFAULT_TILE_SIZE)
            provider.update(state, opacity = 1.0f)
            tileServer.register(routeId, provider)

            val handle =
                ArcGISGroundImageHandle(
                    routeId = routeId,
                    version = 0L,
                    layer = buildWebTiledLayer(routeId, version = 0L, tileSize = provider.tileSize) ?: return@withContext null,
                    tileProvider = provider,
                )
            updateLayer(handle.layer, state)
            scene.operationalLayers.add(handle.layer)
            handle
        }

    override suspend fun updateGroundImageProperties(
        groundImage: ArcGISActualGroundImage,
        current: GroundImageEntityInterface<ArcGISActualGroundImage>,
        prev: GroundImageEntityInterface<ArcGISActualGroundImage>,
    ): ArcGISActualGroundImage? =
        withContext(coroutine.coroutineContext) {
            val scene = holder.map.scene ?: return@withContext groundImage
            val finger = current.fingerPrint
            val prevFinger = prev.fingerPrint

            val opacityChanged = finger.opacity != prevFinger.opacity
            val tileContentChanged = finger.bounds != prevFinger.bounds || finger.image != prevFinger.image

            if (opacityChanged && !tileContentChanged) {
                updateLayer(groundImage.layer, current.state)
                return@withContext groundImage
            }

            if (!tileContentChanged) {
                return@withContext groundImage
            }

            groundImage.tileProvider.update(current.state, opacity = 1.0f)
            val nextVersion = groundImage.version + 1L
            val nextLayer = buildWebTiledLayer(groundImage.routeId, version = nextVersion, tileSize = groundImage.tileProvider.tileSize)
            if (nextLayer == null) {
                return@withContext groundImage
            }
            updateLayer(nextLayer, current.state)

            try {
                scene.operationalLayers.remove(groundImage.layer)
            } catch (_: Exception) {
            }
            scene.operationalLayers.add(nextLayer)

            groundImage.copy(version = nextVersion, layer = nextLayer)
        }

    override suspend fun removeGroundImage(entity: GroundImageEntityInterface<ArcGISActualGroundImage>) {
        coroutine.launch {
            val scene = holder.map.scene ?: return@launch
            val handle = entity.groundImage
            try {
                scene.operationalLayers.remove(handle.layer)
            } catch (_: Exception) {
            }
            tileServer.unregister(handle.routeId)
        }
    }

    private fun updateLayer(
        layer: Layer,
        state: GroundImageState,
    ) {
        layer.opacity = state.opacity.coerceIn(0.0f, 1.0f)
        layer.isVisible = true
    }

    private fun buildWebTiledLayer(
        routeId: String,
        version: Long,
        tileSize: Int,
    ): WebTiledLayer? {
        val template =
            tileServer
                .urlTemplate(routeId, version, tileSize)
                .replace("{z}", "{level}")
                .replace("{x}", "{col}")
                .replace("{y}", "{row}")
        val tileInfo = buildWebMercatorTileInfo(tileSize, DEFAULT_MIN_ZOOM, DEFAULT_MAX_ZOOM)
        val fullExtent = buildWebMercatorExtent()
        return try {
            WebTiledLayer.create(template, emptyList(), tileInfo, fullExtent)
        } catch (e: Exception) {
            Log.w("ArcGIS", "Failed to create WebTiledLayer for ground image: ${e.message}")
            null
        }
    }

    private fun buildWebMercatorTileInfo(
        tileSize: Int,
        minZoom: Int,
        maxZoom: Int,
    ): TileInfo {
        val spatialReference = SpatialReference(WEB_MERCATOR_WKID)
        val origin = Point(WEB_MERCATOR_MIN, WEB_MERCATOR_MAX, spatialReference)
        val levels = buildWebMercatorLevels(tileSize, minZoom, maxZoom)
        return TileInfo(
            DEFAULT_DPI,
            TileImageFormat.Png,
            levels,
            origin,
            spatialReference,
            tileSize,
            tileSize,
        )
    }

    private fun buildWebMercatorLevels(
        tileSize: Int,
        minZoom: Int,
        maxZoom: Int,
    ): List<LevelOfDetail> {
        val initialResolution =
            (2.0 * PI * WEB_MERCATOR_RADIUS_METERS) / tileSize.toDouble()
        val levels = mutableListOf<LevelOfDetail>()
        for (level in minZoom..maxZoom) {
            val resolution = initialResolution / 2.0.pow(level.toDouble())
            val scale = resolution * DEFAULT_DPI * INCHES_PER_METER
            levels.add(LevelOfDetail(level, resolution, scale))
        }
        return levels
    }

    private fun buildWebMercatorExtent(): Envelope =
        Envelope(
            WEB_MERCATOR_MIN,
            WEB_MERCATOR_MIN,
            WEB_MERCATOR_MAX,
            WEB_MERCATOR_MAX,
            spatialReference = SpatialReference(WEB_MERCATOR_WKID),
        )

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
        private const val DEFAULT_TILE_SIZE = 256
        private const val WEB_MERCATOR_WKID = 3857
        private const val WEB_MERCATOR_RADIUS_METERS = 6378137.0
        private const val WEB_MERCATOR_MAX = 20037508.3427892
        private const val WEB_MERCATOR_MIN = -WEB_MERCATOR_MAX
        private const val DEFAULT_DPI = 96
        private const val INCHES_PER_METER = 39.37
        private const val DEFAULT_MIN_ZOOM = 0
        // Keep consistent with ArcGISRasterLayerOverlayRenderer defaults.
        private const val DEFAULT_MAX_ZOOM = 19
    }
}
