package com.mapconductor.googlemaps.marker

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.core.marker.MarkerIngestionEngine
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.MarkerTileRenderer
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.spherical.Spherical.computeDistanceBetween
import com.mapconductor.core.tileserver.TileServerRegistry
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.settings.Settings
import java.util.UUID
import kotlin.math.floor
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit

/**
 * Callback interface for managing RasterLayer from MarkerController.
 * This is used to decouple the MarkerController from the RasterLayerController.
 */
fun interface MarkerTileRasterLayerCallback {
    /**
     * Called when the marker tile RasterLayer needs to be added, updated, or removed.
     * @param state The RasterLayerState to add/update, or null to remove
     */
    suspend fun onRasterLayerUpdate(state: RasterLayerState?)
}

class GoogleMapMarkerController private constructor(
    override val renderer: GoogleMapMarkerRenderer,
    markerManager: MarkerManager<GoogleMapActualMarker>,
    private val markerTiling: MarkerTilingOptions,
) : AbstractMarkerController<GoogleMapActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
    ) {
    private val defaultMarkerIcon: BitmapIcon = DefaultMarkerIcon().toBitmapIcon()
    private val tiledMarkerIds = LinkedHashSet<String>()

    @Volatile
    private var lastKnownZoom: Double = 0.0

    // Tile rendering via RasterLayer
    private val tileServer = TileServerRegistry.get()
    private var markerTileRenderer: MarkerTileRenderer<GoogleMapActualMarker>? = null
    private var markerTileGroupId: String? = null
    private var markerTileRasterLayerState: RasterLayerState? = null
    private var rasterLayerCallback: MarkerTileRasterLayerCallback? = null
    private var cacheVersion: Int = 0

    /**
     * Sets the callback for RasterLayer operations.
     * This must be called before using tiled marker rendering.
     */
    fun setRasterLayerCallback(callback: MarkerTileRasterLayerCallback?) {
        rasterLayerCallback = callback
    }

    override fun find(position: GeoPointInterface): MarkerEntityInterface<GoogleMapActualMarker>? =
        find(position = position, zoom = lastKnownZoom)

    fun find(
        position: GeoPointInterface,
        zoom: Double,
    ): MarkerEntityInterface<GoogleMapActualMarker>? {
        return markerManager.findNearest(position)?.let { nearest ->
            val tolerance =
                Settings.Default.tapTolerance.value
                    .toDouble() * ResourceProvider.getDensity()
            val meterInMapPixel = renderer.zoomToMetersPerPixel(zoom, 256)
            val radius = tolerance * meterInMapPixel
            val distance = computeDistanceBetween(position, nearest.state.position)
            return if (distance <= radius) {
                nearest
            } else {
                null
            }
        }
    }

    override suspend fun add(data: List<MarkerState>) {
        semaphore.withPermit {
            val currentZoom = currentTileZoom()
            val tilingEnabled =
                markerTiling.enabled && data.size >= markerManager.minMarkerCount

            val result =
                MarkerIngestionEngine.ingest(
                    data = data,
                    markerManager = markerManager,
                    renderer = renderer,
                    defaultMarkerIcon = defaultMarkerIcon,
                    tilingEnabled = tilingEnabled,
                    tiledMarkerIds = tiledMarkerIds,
                    shouldTile = { state -> !state.draggable && state.getAnimation() == null },
                )

            if (result.tiledDataChanged) {
                syncTiledOverlay(currentZoom)
            } else if (result.hasTiledMarkers) {
                // Keep existing tile overlay if present.
                // (No per-zoom indexing needed; renderTile queries MarkerManager directly.)
                if (markerTileRenderer == null || markerTileRasterLayerState == null) {
                    syncTiledOverlay(currentZoom)
                }
            } else {
                removeTileOverlay()
            }
        }
    }

    override suspend fun update(state: MarkerState) {
        if (!markerManager.hasEntity(state.id)) return

        val prevEntity = markerManager.getEntity(state.id) ?: return
        val currentFinger = state.fingerPrint()
        val prevFinger = prevEntity.fingerPrint
        if (currentFinger == prevFinger) return

        semaphore.withPermit {
            val tilingEnabled =
                markerTiling.enabled && markerManager.allEntities().size >= markerManager.minMarkerCount
            val wantsTiled = tilingEnabled && !state.draggable && state.getAnimation() == null
            val wasTiled = tiledMarkerIds.contains(state.id)
            val markerIcon = state.icon?.toBitmapIcon() ?: defaultMarkerIcon
            val currentZoom = currentTileZoom()

            if (wantsTiled) {
                if (!wasTiled) {
                    prevEntity.marker?.let { renderer.onRemove(listOf(prevEntity)) }
                    tiledMarkerIds.add(state.id)
                }
                markerManager.updateEntity(
                    MarkerEntity(
                        marker = null,
                        state = state,
                        visible = prevEntity.visible,
                        isRendered = true,
                    ),
                )
                syncTiledOverlay(currentZoom)
                return
            }

            if (wasTiled) {
                tiledMarkerIds.remove(state.id)
            }

            val renderEntity =
                MarkerEntity(
                    marker = prevEntity.marker,
                    state = state,
                    visible = prevEntity.visible,
                    isRendered = true,
                )

            val markerParams =
                object : MarkerOverlayRendererInterface.ChangeParamsInterface<GoogleMapActualMarker> {
                    override val current: MarkerEntityInterface<GoogleMapActualMarker> = renderEntity
                    override val bitmapIcon: BitmapIcon = markerIcon
                    override val prev: MarkerEntityInterface<GoogleMapActualMarker> = prevEntity
                }
            val markers = renderer.onChange(listOf(markerParams))

            markers.firstOrNull()?.let { actualMarker ->
                markerManager.updateEntity(
                    MarkerEntity(
                        marker = actualMarker,
                        state = state,
                        visible = prevEntity.visible,
                        isRendered = true,
                    ),
                )

                if (prevFinger.animation != currentFinger.animation) {
                    state.getAnimation()?.let { renderer.onAnimate(markerManager.getEntity(state.id)!!) }
                }
            }

            renderer.onPostProcess()

            if (tiledMarkerIds.isNotEmpty()) {
                syncTiledOverlay(currentZoom)
            } else {
                removeTileOverlay()
            }
        }
    }

    override suspend fun clear() {
        semaphore.withPermit {
            val entities = markerManager.allEntities()
            val toRemove = entities.filter { it.marker != null }
            if (toRemove.isNotEmpty()) {
                renderer.onRemove(toRemove)
            }
            markerManager.clear()
            tiledMarkerIds.clear()
            removeTileOverlay()
        }
    }

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        lastKnownZoom = mapCameraPosition.zoom

        // Also update the MarkerTileRenderer's camera zoom for fractional zoom support
    }

    override fun destroy() {
        // Clean up tile server registration
        markerTileGroupId?.let { groupId ->
            tileServer.unregister(groupId)
        }
        markerTileGroupId = null
        markerTileRenderer = null

        // Remove RasterLayer via callback
        renderer.coroutine.launch {
            rasterLayerCallback?.onRasterLayerUpdate(null)
        }
        markerTileRasterLayerState = null
        super.destroy()
    }

    /**
     * Updates the RasterLayer source URL to trigger a cache refresh.
     * Creates a new RasterLayerState to ensure proper change detection.
     */
    private suspend fun updateRasterLayerSource() {
        val groupId = markerTileGroupId ?: return
        val tileRenderer = markerTileRenderer ?: return
        val oldState = markerTileRasterLayerState ?: return
        cacheVersion = (cacheVersion + 1) and 0x7fffffff

        // Create a new state object so RasterLayerController can detect the change
        val newState =
            oldState.copy(
                source =
                    RasterLayerSource.UrlTemplate(
                        template = "${tileServer.urlTemplate(groupId, tileRenderer.tileSize)}?v=$cacheVersion",
                        tileSize = tileRenderer.tileSize,
                        maxZoom = 22,
                        scheme = TileScheme.XYZ,
                    ),
                id = oldState.id,
            )
        markerTileRasterLayerState = newState
        rasterLayerCallback?.onRasterLayerUpdate(newState)
    }

    private fun currentTileZoom(): Int = floor(lastKnownZoom).toInt().coerceAtLeast(0)

    private suspend fun syncTiledOverlay(zoom: Int) {
        if (tiledMarkerIds.isEmpty()) {
            removeTileOverlay()
            return
        }
        if (!markerTiling.enabled) {
            removeTileOverlay()
            tiledMarkerIds.clear()
            return
        }

        // Ensure tile renderer + RasterLayer are created before updating the source.
        getOrCreateTileRenderer()
        updateRasterLayerSource()
    }

    private fun getOrCreateTileRenderer(): MarkerTileRenderer<GoogleMapActualMarker> {
        markerTileRenderer?.let { return it }

        val groupId = UUID.randomUUID().toString()
        markerTileGroupId = groupId

        val tileRenderer =
            MarkerTileRenderer(
                markerManager = markerManager,
                tileSize = 256,
                cacheSizeBytes = markerTiling.cacheSize,
                debugTileOverlay = markerTiling.debugTileOverlay,
                iconScaleCallback = markerTiling.iconScaleCallback,
            )
        markerTileRenderer = tileRenderer

        // Register with tile server
        tileServer.register(groupId, tileRenderer)

        // Create RasterLayerState
        markerTileRasterLayerState =
            RasterLayerState(
                id = "marker-tile-$groupId",
                source =
                    RasterLayerSource.UrlTemplate(
                        template = tileServer.urlTemplate(groupId, tileRenderer.tileSize),
                        tileSize = tileRenderer.tileSize,
                        maxZoom = 22,
                        scheme = TileScheme.XYZ,
                    ),
                opacity = 1.0f,
                visible = true,
            )

        return tileRenderer
    }

    private suspend fun removeTileOverlay() {
        markerTileGroupId?.let { groupId ->
            tileServer.unregister(groupId)
        }
        markerTileGroupId = null
        markerTileRenderer = null

        // Remove RasterLayer
        rasterLayerCallback?.onRasterLayerUpdate(null)
        markerTileRasterLayerState = null
    }

    companion object {
        fun create(
            holder: GoogleMapViewHolder,
            markerTiling: MarkerTilingOptions = MarkerTilingOptions.Default,
        ): GoogleMapMarkerController {
            val markerManager =
                MarkerManager.defaultManager<GoogleMapActualMarker>(
                    minMarkerCount = markerTiling.minMarkerCount,
                )
            val renderer =
                GoogleMapMarkerRenderer(
                    holder = holder,
                )
            val controller =
                GoogleMapMarkerController(
                    renderer = renderer,
                    markerManager = markerManager,
                    markerTiling = markerTiling,
                )
            controller.lastKnownZoom =
                holder.map.cameraPosition.zoom
                    .toDouble()
            return controller
        }
    }
}
