package com.mapconductor.googlemaps.marker

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.MarkerTileRenderer
import com.mapconductor.core.projection.Earth
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.spherical.Spherical.computeDistanceBetween
import com.mapconductor.core.tileserver.TileServerRegistry
import com.mapconductor.googlemaps.GoogleMapActualMarker
import com.mapconductor.googlemaps.GoogleMapViewHolder
import com.mapconductor.googlemaps.toGeoPoint
import com.mapconductor.settings.Settings
import java.util.UUID
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import android.graphics.Bitmap
import android.graphics.Point
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

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
    private val tilingOptions: GoogleMapMarkerTilingOptions,
) : AbstractMarkerController<GoogleMapActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
    ) {
    init {
        GoogleMapMarkerTilingPerfLog.enabled = tilingOptions.debugLogging
        GoogleMapMarkerTilingPerfLog.slowOpThresholdMs = tilingOptions.slowOpThresholdMs
        GoogleMapMarkerTilingPerfLog.logSampleRate = tilingOptions.logSampleRate
        GoogleMapMarkerTilingPerfLog.tileSummaryEvery = tilingOptions.tileSummaryEvery
        MarkerTileRenderer.debugLoggingEnabled = tilingOptions.debugLogging
        if (tilingOptions.debugLogging) {
            Log.i(
                "MapConductorTiling",
                "enabled=true slowOpThresholdMs=${tilingOptions.slowOpThresholdMs} logSampleRate=${tilingOptions.logSampleRate} tileSummaryEvery=${tilingOptions.tileSummaryEvery}",
            )
        }
    }

    private val defaultMarkerIcon: BitmapIcon = DefaultMarkerIcon().toBitmapIcon()
    private val tiledMarkerIds = LinkedHashSet<String>()
    private val tiledMarkerIconsById = mutableMapOf<String, BitmapIcon>()
    private val tiledBitmapCache = mutableMapOf<Int, Bitmap>()
    private var screenPxPerWorldPx: Double = 1.0
    private var pendingScaleSync: Boolean = false

    @Volatile
    private var lastKnownZoom: Double = 0.0
    private var lastAppliedMarkerScale: Double = 1.0
    private var lastIndexedZoom: Int = -1
    private var lastMarkerScaleZoomInt: Int = -1
    private var lastAutoScaleReferenceZoom: Int = -1

    @Volatile
    private var lastTiledMarkersSnapshot: Map<String, MarkerTileRenderer.RenderMarker> = emptyMap()

    @Volatile
    private var lastTileIndexByZoom: Map<Int, Map<Long, List<String>>> = emptyMap()

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
            val addStart = SystemClock.elapsedRealtime()
            val tilingEnabled = tilingOptions.enabled && data.size >= tilingOptions.minMarkerCount
            val currentZoom = currentTileZoom()

            val previousIds =
                GoogleMapMarkerTilingPerfLog.measure(
                    name = "MarkerController.add:collectPreviousIds",
                    meta = { "entities=${markerManager.getMemoryStats().entityCount}" },
                ) {
                    markerManager
                        .allEntities()
                        .asSequence()
                        .map { it.state.id }
                        .toMutableSet()
                }
            val added = mutableListOf<MarkerOverlayRendererInterface.AddParamsInterface>()
            val updated = mutableListOf<MarkerOverlayRendererInterface.ChangeParamsInterface<GoogleMapActualMarker>>()
            val removedActualMarkers = mutableListOf<MarkerEntityInterface<GoogleMapActualMarker>>()

            var tiledDataChanged = false
            var wantsTiledCount = 0
            var newTiledCount = 0
            var newNonTiledCount = 0
            var iconBitmapConversions = 0

            val iterateStart = SystemClock.elapsedRealtime()
            data.forEach { state ->
                val wantsTiled = tilingEnabled && !state.draggable && state.getAnimation() == null
                if (wantsTiled) wantsTiledCount++
                val markerIcon =
                    state.icon?.let {
                        iconBitmapConversions++
                        it.toBitmapIcon()
                    } ?: defaultMarkerIcon

                if (previousIds.contains(state.id)) {
                    val prevEntity = markerManager.getEntity(state.id)!!
                    val wasTiled = tiledMarkerIds.contains(state.id)

                    if (wantsTiled) {
                        if (!wasTiled) {
                            prevEntity.marker?.let { removedActualMarkers.add(prevEntity) }
                            tiledMarkerIds.add(state.id)
                        }
                        tiledMarkerIconsById[state.id] = markerIcon
                        markerManager.updateEntity(
                            MarkerEntity(
                                marker = null,
                                state = state,
                                visible = prevEntity.visible,
                                isRendered = true,
                            ),
                        )
                        tiledDataChanged = true
                    } else {
                        if (wasTiled) {
                            tiledMarkerIds.remove(state.id)
                            tiledMarkerIconsById.remove(state.id)
                            tiledDataChanged = true
                        }
                        updated.add(
                            object : MarkerOverlayRendererInterface.ChangeParamsInterface<GoogleMapActualMarker> {
                                override val current: MarkerEntityInterface<GoogleMapActualMarker> =
                                    MarkerEntity(
                                        state = state,
                                        marker = prevEntity.marker,
                                        visible = prevEntity.visible,
                                        isRendered = true,
                                    )
                                override val bitmapIcon: BitmapIcon = markerIcon
                                override val prev: MarkerEntityInterface<GoogleMapActualMarker> = prevEntity
                            },
                        )
                    }
                    previousIds.remove(state.id)
                } else {
                    if (wantsTiled) {
                        tiledMarkerIds.add(state.id)
                        tiledMarkerIconsById[state.id] = markerIcon
                        markerManager.registerEntity(
                            MarkerEntity(
                                marker = null,
                                state = state,
                                visible = true,
                                isRendered = true,
                            ),
                        )
                        tiledDataChanged = true
                        newTiledCount++
                    } else {
                        added.add(
                            object : MarkerOverlayRendererInterface.AddParamsInterface {
                                override val state: MarkerState = state
                                override val bitmapIcon: BitmapIcon = markerIcon
                            },
                        )
                        newNonTiledCount++
                    }
                }
            }
            GoogleMapMarkerTilingPerfLog.logSlow(
                name = "MarkerController.add:iterate",
                elapsedMs = SystemClock.elapsedRealtime() - iterateStart,
                meta =
                    "data=${data.size} tilingEnabled=$tilingEnabled wantsTiled=$wantsTiledCount newTiled=$newTiledCount newNonTiled=$newNonTiledCount iconToBitmap=$iconBitmapConversions",
            )

            previousIds.forEach { remainId ->
                markerManager.removeEntity(remainId)?.let { removedEntity ->
                    if (tiledMarkerIds.remove(remainId)) {
                        tiledMarkerIconsById.remove(remainId)
                        tiledDataChanged = true
                    } else {
                        removedActualMarkers.add(removedEntity)
                    }
                }
            }

            if (removedActualMarkers.isNotEmpty()) {
                GoogleMapMarkerTilingPerfLog.measure(
                    name = "MarkerController.add:onRemove",
                    meta = { "count=${removedActualMarkers.size}" },
                ) {
                    renderer.onRemove(removedActualMarkers)
                }
            }

            if (added.isNotEmpty()) {
                val actualMarkers =
                    GoogleMapMarkerTilingPerfLog.measure(
                        name = "MarkerController.add:onAdd",
                        meta = { "count=${added.size}" },
                    ) {
                        renderer.onAdd(added)
                    }
                actualMarkers.forEachIndexed { index, actualMarker ->
                    actualMarker ?: return@forEachIndexed
                    val state = added[index].state
                    markerManager.registerEntity(
                        MarkerEntity(
                            marker = actualMarker,
                            state = state,
                            visible = true,
                            isRendered = true,
                        ),
                    )
                    state.getAnimation()?.let { renderer.onAnimate(markerManager.getEntity(state.id)!!) }
                }
            }

            if (updated.isNotEmpty()) {
                val actualMarkers =
                    GoogleMapMarkerTilingPerfLog.measure(
                        name = "MarkerController.add:onChange",
                        meta = { "count=${updated.size}" },
                    ) {
                        renderer.onChange(updated)
                    }
                actualMarkers.forEachIndexed { index, actualMarker ->
                    val params = updated[index]
                    val marker = actualMarker
                    if (marker != null) {
                        markerManager.updateEntity(
                            MarkerEntity(
                                marker = marker,
                                state = params.current.state,
                                visible = params.current.visible,
                                isRendered = true,
                            ),
                        )
                    } else {
                        // Keep state updated even if marker creation failed
                        markerManager.updateEntity(
                            MarkerEntity(
                                marker = params.prev.marker,
                                state = params.current.state,
                                visible = params.current.visible,
                                isRendered = true,
                            ),
                        )
                    }
                }
            }

            GoogleMapMarkerTilingPerfLog.measure(
                name = "MarkerController.add:onPostProcess",
            ) { renderer.onPostProcess() }

            if (tiledDataChanged) {
                GoogleMapMarkerTilingPerfLog.measure(
                    name = "MarkerController.add:syncTiledOverlay",
                    meta = { "zoom=$currentZoom tiledCount=${tiledMarkerIds.size}" },
                ) {
                    syncTiledOverlay(currentZoom)
                }
            } else if (tiledMarkerIds.isNotEmpty()) {
                // Keep zoom index aligned when markers are static but zoom changed between add() calls.
                syncTiledZoom(currentZoom)
            } else {
                removeTileOverlay()
            }
            GoogleMapMarkerTilingPerfLog.logSlow(
                name = "MarkerController.add:total",
                elapsedMs = SystemClock.elapsedRealtime() - addStart,
                meta =
                    "data=${data.size} tilingEnabled=$tilingEnabled tiledCount=${tiledMarkerIds.size} nonTiledAdded=${added.size} updated=${updated.size}",
            )
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
                tilingOptions.enabled && markerManager.allEntities().size >= tilingOptions.minMarkerCount
            val wantsTiled = tilingEnabled && !state.draggable && state.getAnimation() == null
            val wasTiled = tiledMarkerIds.contains(state.id)
            val markerIcon = state.icon?.toBitmapIcon() ?: defaultMarkerIcon
            val currentZoom = currentTileZoom()

            if (wantsTiled) {
                if (!wasTiled) {
                    prevEntity.marker?.let { renderer.onRemove(listOf(prevEntity)) }
                    tiledMarkerIds.add(state.id)
                }
                tiledMarkerIconsById[state.id] = markerIcon
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
                tiledMarkerIconsById.remove(state.id)
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
            tiledMarkerIconsById.clear()
            tiledBitmapCache.values.forEach { it.recycle() }
            tiledBitmapCache.clear()
            removeTileOverlay()
        }
    }

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        lastKnownZoom = mapCameraPosition.zoom
        val zoomInt = floor(lastKnownZoom).toInt().coerceAtLeast(0)

        // Also update the MarkerTileRenderer's camera zoom for fractional zoom support
        markerTileRenderer?.updateCameraZoom(mapCameraPosition.zoom)

        if (!tilingOptions.enabled || tiledMarkerIds.isEmpty()) return
        // Keep heavy work off the main thread
        withContext(Dispatchers.Default) {
            semaphore.withPermit {
                updateScreenPxPerWorldPxAndCheckChange(zoomInt)
                val markerScale = quantizeMarkerScale((1.0 / screenPxPerWorldPx).coerceAtLeast(1e-6))
                val zoomChanged = zoomInt != lastIndexedZoom
                val scaleChanged = kotlin.math.abs(markerScale - lastAppliedMarkerScale) > 1e-6
                if (!zoomChanged && !scaleChanged) return@withPermit

                val markers = lastTiledMarkersSnapshot
                if (markers.isEmpty()) return@withPermit

                val autoScaleRefZoom =
                    if (lastAutoScaleReferenceZoom >= 0) {
                        lastAutoScaleReferenceZoom
                    } else if (tilingOptions.fixedMarkerPixelSize) {
                        tilingOptions.fixedMarkerPixelSizeReferenceZoom
                    } else {
                        zoomInt
                    }
                if (lastAutoScaleReferenceZoom < 0) lastAutoScaleReferenceZoom = autoScaleRefZoom

                val desiredZooms = computeDesiredZoomWindow(zoomInt, lastIndexedZoom)
                val out = lastTileIndexByZoom.filterKeys { it in desiredZooms }.toMutableMap()
                for (z in desiredZooms) {
                    if (out.containsKey(z)) continue
                    out[z] =
                        MarkerTileRenderer.buildTileIndex(
                            markers = markers,
                            zoom = z,
                            tileSize = tilingOptions.tileSize,
                            bitmapPxToWorldPx = markerScale,
                            markerScaleZoomInt = zoomInt,
                            autoScaleReferenceZoom = autoScaleRefZoom,
                            fixedMarkerPixelSize = tilingOptions.fixedMarkerPixelSize,
                            fixedMarkerPixelSizeReferenceZoom = tilingOptions.fixedMarkerPixelSizeReferenceZoom,
                        )
                }

                val tileRenderer = markerTileRenderer ?: return@withPermit
                lastTileIndexByZoom = out
                lastIndexedZoom = zoomInt
                lastMarkerScaleZoomInt = zoomInt
                lastAppliedMarkerScale = markerScale
                tileRenderer.setTileIndexesAndMarkerScale(
                    indexes = out,
                    indexedZoom = zoomInt,
                    bitmapPxToWorldPx = markerScale,
                    autoScaleReferenceZoom = autoScaleRefZoom,
                )
                // Update RasterLayer source to invalidate cache
                updateRasterLayerSource()
            }
        }
    }

    override fun destroy() {
        // Clean up tile server registration
        markerTileGroupId?.let { groupId ->
            tileServer.unregister(groupId)
        }
        markerTileGroupId = null
        markerTileRenderer?.clear()
        markerTileRenderer = null

        // Remove RasterLayer via callback
        renderer.coroutine.launch {
            rasterLayerCallback?.onRasterLayerUpdate(null)
        }
        markerTileRasterLayerState = null

        tiledBitmapCache.values.forEach { it.recycle() }
        tiledBitmapCache.clear()
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
        if (!tilingOptions.enabled) {
            removeTileOverlay()
            tiledMarkerIds.clear()
            tiledMarkerIconsById.clear()
            return
        }

        // Best-effort update even before the first camera idle (initial render).
        updateScreenPxPerWorldPxAndCheckChange(zoom)
        val shouldDefer =
            withContext(renderer.coroutine.coroutineContext) {
                val mapView = renderer.holder.mapView
                if (!pendingScaleSync && (mapView.width <= 0 || mapView.height <= 0)) {
                    pendingScaleSync = true
                    mapView.post {
                        pendingScaleSync = false
                        renderer.coroutine.launch(Dispatchers.Default) {
                            semaphore.withPermit { syncTiledOverlay(zoom) }
                        }
                    }
                    true
                } else {
                    false
                }
            }
        if (shouldDefer) return

        val markers =
            withContext(Dispatchers.Default) {
                GoogleMapMarkerTilingPerfLog.measure(
                    name = "MarkerController.syncTiledOverlay:buildMarkerSnapshot",
                    meta = { "tiledCount=${tiledMarkerIds.size}" },
                ) {
                    HashMap<String, MarkerTileRenderer.RenderMarker>(tiledMarkerIds.size).also { out ->
                        tiledMarkerIds.forEach { id ->
                            val entity = markerManager.getEntity(id) ?: return@forEach
                            val icon = tiledMarkerIconsById[id] ?: return@forEach
                            val bitmap = normalizeBitmapForTile(icon.bitmap)
                            val latitude = entity.state.position.latitude
                            val longitude = entity.state.position.longitude
                            val clampedLatitude = latitude.coerceIn(-85.05112878, 85.05112878)
                            val sinLatitude = sin(Math.toRadians(clampedLatitude)).coerceIn(-0.9999, 0.9999)
                            val mercatorXRaw = (longitude + 180.0) / 360.0
                            val mercatorY =
                                0.5 - ln((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * Math.PI)
                            val mercatorX =
                                ((mercatorXRaw % 1.0) + 1.0) % 1.0
                            out[id] =
                                MarkerTileRenderer.RenderMarker(
                                    id = id,
                                    mercatorX = mercatorX,
                                    mercatorY = mercatorY,
                                    visible = entity.visible,
                                    bitmap = bitmap,
                                    anchorX = icon.anchor.x,
                                    anchorY = icon.anchor.y,
                                    autoScalable = entity.state.autoScalable,
                                )
                        }
                    }
                }
            }
        lastTiledMarkersSnapshot = markers
        val autoScaleReferenceZoom =
            if (tilingOptions.fixedMarkerPixelSize) {
                tilingOptions.fixedMarkerPixelSizeReferenceZoom
            } else {
                zoom
            }
        lastAutoScaleReferenceZoom = autoScaleReferenceZoom
        val markerScale = quantizeMarkerScale((1.0 / screenPxPerWorldPx).coerceAtLeast(1e-6))
        val tileIndexes =
            withContext(Dispatchers.Default) {
                GoogleMapMarkerTilingPerfLog.measure(
                    name = "MarkerController.syncTiledOverlay:buildTileIndex",
                    meta = { "markers=${markers.size} zoom=$zoom" },
                ) {
                    val desiredZooms = computeDesiredZoomWindow(zoom, prevIndexedZoom = null)
                    desiredZooms.associateWith { z ->
                        MarkerTileRenderer.buildTileIndex(
                            markers = markers,
                            zoom = z,
                            tileSize = tilingOptions.tileSize,
                            bitmapPxToWorldPx = markerScale,
                            markerScaleZoomInt = zoom,
                            autoScaleReferenceZoom = autoScaleReferenceZoom,
                            fixedMarkerPixelSize = tilingOptions.fixedMarkerPixelSize,
                            fixedMarkerPixelSizeReferenceZoom = tilingOptions.fixedMarkerPixelSizeReferenceZoom,
                        )
                    }
                }
            }

        // Get or create the tile renderer
        val tileRenderer = getOrCreateTileRenderer()

        val setStart = SystemClock.elapsedRealtime()
        lastTileIndexByZoom = tileIndexes
        tileRenderer.setMarkers(
            markers = markers,
            indexes = tileIndexes,
            indexedZoom = zoom,
            bitmapPxToWorldPx = markerScale,
            autoScaleReferenceZoom = autoScaleReferenceZoom,
        )
        val zoomTileCount = tileIndexes[zoom]?.size ?: 0
        GoogleMapMarkerTilingPerfLog.logSlow(
            name = "MarkerController.syncTiledOverlay:setMarkers",
            elapsedMs = SystemClock.elapsedRealtime() - setStart,
            meta = "markers=${markers.size} tiles=$zoomTileCount zoom=$zoom",
        )
        lastIndexedZoom = zoom
        lastMarkerScaleZoomInt = zoom
        lastAppliedMarkerScale = markerScale

        // Update RasterLayer
        updateRasterLayerSource()
    }

    private fun getOrCreateTileRenderer(): MarkerTileRenderer<GoogleMapActualMarker> {
        markerTileRenderer?.let { return it }

        val groupId = UUID.randomUUID().toString()
        markerTileGroupId = groupId

        val tileRenderer =
            MarkerTileRenderer(
                markerManager = markerManager,
                tileSize = tilingOptions.tileSize,
                finalTileDownscaleFilter = tilingOptions.finalTileDownscaleFilter,
                debugTileOverlay = tilingOptions.debugTileOverlay,
                renderScaleOverride = tilingOptions.renderScaleOverride,
                declutterEnabled = tilingOptions.declutterEnabled,
                declutterMaxZoomInt = tilingOptions.declutterMaxZoomInt,
                declutterMaxMarkersPerTile = tilingOptions.declutterMaxMarkersPerTile,
                declutterIconPx = tilingOptions.declutterIconPx,
                declutterCellPx = tilingOptions.declutterCellPx,
                fixedMarkerPixelSize = tilingOptions.fixedMarkerPixelSize,
                fixedMarkerPixelSizeReferenceZoom = tilingOptions.fixedMarkerPixelSizeReferenceZoom,
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
        markerTileRenderer?.clear()
        markerTileRenderer = null

        // Remove RasterLayer
        rasterLayerCallback?.onRasterLayerUpdate(null)
        markerTileRasterLayerState = null
    }

    private suspend fun syncTiledZoom(zoom: Int) {
        if (!tilingOptions.enabled || tiledMarkerIds.isEmpty()) return
        if (zoom == lastIndexedZoom) return
        val markers = lastTiledMarkersSnapshot
        if (markers.isEmpty()) return
        val tileRenderer = markerTileRenderer ?: return

        val markerScaleZoomInt = if (lastMarkerScaleZoomInt >= 0) lastMarkerScaleZoomInt else lastIndexedZoom
        val desiredZooms = computeDesiredZoomWindow(zoom, prevIndexedZoom = lastIndexedZoom)
        val indexes =
            withContext(Dispatchers.Default) {
                desiredZooms.associateWith { z ->
                    MarkerTileRenderer.buildTileIndex(
                        markers = markers,
                        zoom = z,
                        tileSize = tilingOptions.tileSize,
                        bitmapPxToWorldPx = lastAppliedMarkerScale,
                        markerScaleZoomInt = markerScaleZoomInt,
                        autoScaleReferenceZoom = lastAutoScaleReferenceZoom,
                        fixedMarkerPixelSize = tilingOptions.fixedMarkerPixelSize,
                        fixedMarkerPixelSizeReferenceZoom = tilingOptions.fixedMarkerPixelSizeReferenceZoom,
                    )
                }
            }
        lastTileIndexByZoom = indexes
        tileRenderer.setTileIndexes(indexes, indexedZoom = zoom, autoScaleReferenceZoom = lastAutoScaleReferenceZoom)
        lastIndexedZoom = zoom

        // Update RasterLayer source to invalidate cache
        updateRasterLayerSource()
    }

    private fun computeDesiredZoomWindow(
        centerZoom: Int,
        prevIndexedZoom: Int?,
        maxZoom: Int = 22,
    ): LinkedHashSet<Int> {
        fun addWindow(
            out: LinkedHashSet<Int>,
            center: Int,
        ) {
            for (z in (center - 1)..(center + 2)) {
                if (z in 0..maxZoom) out.add(z)
            }
        }
        val out = LinkedHashSet<Int>(8)
        addWindow(out, centerZoom.coerceIn(0, maxZoom))
        prevIndexedZoom?.let { prev ->
            if (prev >= 0 && prev != centerZoom) addWindow(out, prev.coerceIn(0, maxZoom))
        }
        return out
    }

    private fun quantizeMarkerScale(scale: Double): Double {
        val step = tilingOptions.markerScaleQuantizationStep
        if (step <= 0.0) return scale
        val safeStep = step.coerceAtLeast(1e-6)
        val q = kotlin.math.round(scale / safeStep) * safeStep
        return q.coerceAtLeast(1e-6)
    }

    private fun normalizeBitmapForTile(bitmap: Bitmap): Bitmap {
        // Render into tiles using density-independent bitmaps to avoid any implicit density scaling.
        // Keep a small cache since most apps reuse a few marker icons.
        val key = System.identityHashCode(bitmap)
        tiledBitmapCache[key]?.let { cached ->
            if (!cached.isRecycled) return cached
            tiledBitmapCache.remove(key)
        }
        val copy = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, false)
        copy.density = Bitmap.DENSITY_NONE
        tiledBitmapCache[key] = copy
        return copy
    }

    private suspend fun updateScreenPxPerWorldPxAndCheckChange(zoomInt: Int): Boolean {
        val next = computeScreenPxPerWorldPx(zoomInt) ?: return false
        val changed = kotlin.math.abs(next - screenPxPerWorldPx) > 1e-3
        screenPxPerWorldPx = next
        return changed
    }

    private suspend fun computeScreenPxPerWorldPx(zoomInt: Int): Double? =
        withContext(renderer.coroutine.coroutineContext) {
            val mapView = renderer.holder.mapView
            val w = mapView.width
            val h = mapView.height
            if (w <= 0 || h <= 0) return@withContext null

            val projection = renderer.holder.map.projection
            val center = Point(w / 2, h / 2)
            val right = Point(w / 2 + 100, h / 2)
            val centerGeo = projection.fromScreenLocation(center)?.toGeoPoint() ?: return@withContext null
            val rightGeo = projection.fromScreenLocation(right)?.toGeoPoint() ?: return@withContext null
            val meters = computeDistanceBetween(centerGeo, rightGeo)
            if (meters <= 0.0) return@withContext null

            val metersPerScreenPx = meters / 100.0
            val metersPerWorldPx = metersPerWorldPixel(centerGeo.latitude, zoomInt, tilingOptions.tileSize)
            return@withContext (metersPerWorldPx / metersPerScreenPx).coerceAtLeast(1e-6)
        }

    private fun metersPerWorldPixel(
        latitude: Double,
        zoomInt: Int,
        tileSize: Int,
    ): Double {
        val pixelsAtZoom = tileSize.toDouble() * 2.0.pow(zoomInt.toDouble())
        return Earth.CIRCUMFERENCE_METERS / pixelsAtZoom * kotlin.math.cos(Math.toRadians(latitude))
    }

    companion object {
        fun create(
            holder: GoogleMapViewHolder,
            tilingOptions: GoogleMapMarkerTilingOptions = GoogleMapMarkerTilingOptions(),
        ): GoogleMapMarkerController {
            val markerManager = MarkerManager.defaultManager<GoogleMapActualMarker>()
            val renderer =
                GoogleMapMarkerRenderer(
                    holder = holder,
                )
            val controller =
                GoogleMapMarkerController(
                    renderer = renderer,
                    markerManager = markerManager,
                    tilingOptions = tilingOptions,
                )
            controller.lastKnownZoom =
                holder.map.cameraPosition.zoom
                    .toDouble()
            return controller
        }
    }
}
