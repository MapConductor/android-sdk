package com.mapconductor.mapbox.marker

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
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
import com.mapconductor.core.marker.MarkerTileRasterLayerCallback
import com.mapconductor.core.marker.MarkerTileRenderer
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.core.projection.Earth
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.spherical.Spherical.computeDistanceBetween
import com.mapconductor.core.tileserver.TileServerRegistry
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.toMapCameraPosition
import com.mapconductor.settings.Settings
import java.util.UUID
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

class MapboxMarkerController private constructor(
    override val renderer: MapboxMarkerOverlayRenderer,
    markerManager: MarkerManager<MapboxActualMarker>,
    private val tilingOptions: MarkerTilingOptions,
) : AbstractMarkerController<MapboxActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
    ) {
    private var internalSelectedMarker: MarkerEntityInterface<MapboxActualMarker>? = null

    private val defaultMarkerIcon: BitmapIcon = DefaultMarkerIcon().toBitmapIcon()
    private val tiledMarkerIds = LinkedHashSet<String>()
    private val tiledMarkerIconsById = mutableMapOf<String, BitmapIcon>()
    private val tiledBitmapCache = mutableMapOf<Int, Bitmap>()
    private var screenPxPerWorldPx: Double = 1.0
    private var pendingScaleSync: Boolean = false

//    @Volatile
    private lateinit var lastCameraPosition: MapCameraPosition
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
    private var markerTileRenderer: MarkerTileRenderer<MapboxActualMarker>? = null
    private var markerTileGroupId: String? = null
    private var markerTileRasterLayerState: RasterLayerState? = null
    private var rasterLayerCallback: MarkerTileRasterLayerCallback? = null
    private var cacheVersion: Int = 0

    init {
        MarkerTileRenderer.debugLoggingEnabled = tilingOptions.debugLogging
    }

    internal var selectedMarker: MarkerEntityInterface<MapboxActualMarker>?
        set(value) {
            if (value == null) {
                internalSelectedMarker?.let {
                    renderer.dragLayer.updatePosition(GeoPoint.from(it.state.position))
                    setDraggingState(it.state, false)
                    renderer.dragLayer.selected = null
                    renderer.drawDragLayer()
                    markerManager.registerEntity(it)
                    renderer.redraw()
                }
                internalSelectedMarker = null
                return
            }
            internalSelectedMarker = value
            markerManager.removeEntity(value.state.id)
            setDraggingState(value.state, true)
            renderer.dragLayer.selected = value
            renderer.dragLayer.updatePosition(GeoPoint.from(value.state.position))
            renderer.redraw()
            renderer.drawDragLayer()
        }
        get() = internalSelectedMarker

    /**
     * Sets the callback for RasterLayer operations.
     * This must be called before using tiled marker rendering.
     */
    fun setRasterLayerCallback(callback: MarkerTileRasterLayerCallback?) {
        rasterLayerCallback = callback
    }

    override fun find(position: GeoPointInterface): MarkerEntityInterface<MapboxActualMarker>? {
        val nearest = markerManager.findNearest(position) ?: return null

        val touchScreen = renderer.holder.toScreenOffset(position) ?: return null
        val markerScreen = renderer.holder.toScreenOffset(nearest.state.position) ?: return null

        val tolerancePx =
            Settings.Default.tapTolerance.value
                .toDouble() *
                ResourceProvider.getDensity().toDouble()

        val icon = nearest.state.icon

        if (icon == null) {
            val dx = (touchScreen.x - markerScreen.x).toDouble()
            val dy = (touchScreen.y - markerScreen.y).toDouble()
            val distancePx = kotlin.math.hypot(dx, dy)
            return if (distancePx <= tolerancePx) {
                nearest
            } else {
                null
            }
        }

        val baseSizePx = ResourceProvider.dpToPxForBitmap(icon.iconSize).toDouble()
        val iconWidthPx = baseSizePx * icon.scale.toDouble()
        val iconHeightPx = baseSizePx * icon.scale.toDouble()

        val anchorX = icon.anchor.x.toDouble()
        val anchorY = icon.anchor.y.toDouble()

        val dx = (touchScreen.x - markerScreen.x).toDouble()
        val dy = (touchScreen.y - markerScreen.y).toDouble()

        val left = -anchorX * iconWidthPx - tolerancePx
        val right = (1.0 - anchorX) * iconWidthPx + tolerancePx
        val top = -anchorY * iconHeightPx - tolerancePx
        val bottom = (1.0 - anchorY) * iconHeightPx + tolerancePx

        return if (dx in left..right && dy in top..bottom) {
            nearest
        } else {
            null
        }
    }

    override suspend fun add(data: List<MarkerState>) {
        semaphore.withPermit {
            // val tilingEnabled = tilingOptions.enabled && data.size >= tilingOptions.minMarkerCount
            val tilingEnabled = true
            val currentZoom = currentTileZoom()

            val previousIds =
                markerManager
                    .allEntities()
                    .asSequence()
                    .map { it.state.id }
                    .toMutableSet()
            val added = mutableListOf<MarkerOverlayRendererInterface.AddParamsInterface>()
            val updated = mutableListOf<MarkerOverlayRendererInterface.ChangeParamsInterface<MapboxActualMarker>>()
            val removedActualMarkers = mutableListOf<MarkerEntityInterface<MapboxActualMarker>>()

            var tiledDataChanged = false

            data.forEach { state ->
                val wantsTiled = tilingEnabled && !state.draggable && state.getAnimation() == null
                val markerIcon = state.icon?.toBitmapIcon() ?: defaultMarkerIcon

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
                            object : MarkerOverlayRendererInterface.ChangeParamsInterface<MapboxActualMarker> {
                                override val current: MarkerEntityInterface<MapboxActualMarker> =
                                    MarkerEntity(
                                        state = state,
                                        marker = prevEntity.marker,
                                        visible = prevEntity.visible,
                                        isRendered = true,
                                    )
                                override val bitmapIcon: BitmapIcon = markerIcon
                                override val prev: MarkerEntityInterface<MapboxActualMarker> = prevEntity
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
                    } else {
                        added.add(
                            object : MarkerOverlayRendererInterface.AddParamsInterface {
                                override val state: MarkerState = state
                                override val bitmapIcon: BitmapIcon = markerIcon
                            },
                        )
                    }
                }
            }

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
                renderer.onRemove(removedActualMarkers)
            }

            if (added.isNotEmpty()) {
                val actualMarkers = renderer.onAdd(added)
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
                val actualMarkers = renderer.onChange(updated)
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
                    }
                }
            }

            renderer.onPostProcess()

            if (tiledDataChanged) {
                syncTiledOverlay(currentZoom)
            } else if (tiledMarkerIds.isNotEmpty()) {
                syncTiledZoom(currentZoom)
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
        lastCameraPosition = mapCameraPosition
        val zoomInt = floor(lastCameraPosition.zoom).toInt().coerceAtLeast(0)

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
                updateRasterLayerSource()
            }
        }
    }

    override fun destroy() {
        markerTileGroupId?.let { groupId ->
            tileServer.unregister(groupId)
        }
        markerTileGroupId = null
        markerTileRenderer?.clear()
        markerTileRenderer = null

        renderer.coroutine.launch {
            rasterLayerCallback?.onRasterLayerUpdate(null)
        }
        markerTileRasterLayerState = null

        tiledBitmapCache.values.forEach { it.recycle() }
        tiledBitmapCache.clear()
        super.destroy()
    }

    private suspend fun updateRasterLayerSource() {
        val groupId = markerTileGroupId ?: return
        val tileRenderer = markerTileRenderer ?: return
        val oldState = markerTileRasterLayerState ?: return
        cacheVersion = (cacheVersion + 1) and 0x7fffffff

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

    private fun currentTileZoom(): Int = floor(lastCameraPosition.zoom).toInt().coerceAtLeast(0)

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
                        val mercatorX = ((mercatorXRaw % 1.0) + 1.0) % 1.0
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

        val tileRenderer = getOrCreateTileRenderer()

        lastTileIndexByZoom = tileIndexes
        tileRenderer.setMarkers(
            markers = markers,
            indexes = tileIndexes,
            indexedZoom = zoom,
            bitmapPxToWorldPx = markerScale,
            autoScaleReferenceZoom = autoScaleReferenceZoom,
        )
        lastIndexedZoom = zoom
        lastMarkerScaleZoomInt = zoom
        lastAppliedMarkerScale = markerScale

        updateRasterLayerSource()
    }

    private fun getOrCreateTileRenderer(): MarkerTileRenderer<MapboxActualMarker> {
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
                drawRawBitmapInTilePixels = true,
                useCameraZoomForScale = true,
                useCameraZoomCompensation = true,
            )
        markerTileRenderer = tileRenderer

        tileServer.register(groupId, tileRenderer)

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

            val centerGeo =
                renderer.holder.fromScreenOffsetSync(Offset(w / 2f, h / 2f))
                    ?: return@withContext null
            val rightGeo =
                renderer.holder.fromScreenOffsetSync(Offset(w / 2f + 100f, h / 2f))
                    ?: return@withContext null
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
            holder: MapboxMapViewHolder,
            markerManager: MarkerManager<MapboxActualMarker>,
            markerLayer: MarkerLayer,
            dragLayer: MarkerDragLayer,
            tilingOptions: MarkerTilingOptions = MarkerTilingOptions.Default,
        ): MapboxMarkerController {
            val renderer =
                MapboxMarkerOverlayRenderer(
                    holder = holder,
                    markerManager = markerManager,
                    markerLayer = markerLayer,
                    dragLayer = dragLayer,
                )
            val controller =
                MapboxMarkerController(
                    renderer = renderer,
                    markerManager = markerManager,
                    tilingOptions = tilingOptions,
                )
            controller.lastCameraPosition = holder.map.cameraState.toMapCameraPosition()
            return controller
        }
    }
}
