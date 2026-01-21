package com.mapconductor.here.marker

import androidx.compose.ui.geometry.Offset
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
import com.mapconductor.core.marker.MarkerTileRasterLayerCallback
import com.mapconductor.core.marker.MarkerTileRenderer
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.tileserver.TileServerRegistry
import com.mapconductor.here.HereActualMarker
import com.mapconductor.here.HereViewHolder
import com.mapconductor.settings.Settings
import java.util.UUID
import kotlin.math.floor
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit

class HereMarkerController private constructor(
    markerManager: MarkerManager<HereActualMarker>,
    override val renderer: HereMarkerRenderer,
    private val tilingOptions: MarkerTilingOptions,
    private val markerScaleMultiplier: Double,
) : AbstractMarkerController<HereActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
    ) {
    private var internalSelectedMarker: MarkerEntityInterface<HereActualMarker>? = null

    private val defaultMarkerIcon: BitmapIcon = DefaultMarkerIcon().toBitmapIcon()
    private val tiledMarkerIds = LinkedHashSet<String>()

    @Volatile
    private var lastKnownZoom: Double = 0.0

    private val tileServer = TileServerRegistry.get(forceNoStoreCache = true)
    private var markerTileRenderer: MarkerTileRenderer<HereActualMarker>? = null
    private var markerTileGroupId: String? = null
    private var markerTileRasterLayerState: RasterLayerState? = null
    private var rasterLayerCallback: MarkerTileRasterLayerCallback? = null
    private var cacheVersion: Int = 0

    internal var selectedMarker: MarkerEntityInterface<HereActualMarker>?
        set(value) {
            if (value == null) {
                internalSelectedMarker?.let {
                    setDraggingState(it.state, false)
                }
                internalSelectedMarker = null
                return
            }
            internalSelectedMarker = value
            setDraggingState(value.state, true)
        }
        get() = internalSelectedMarker

    fun setRasterLayerCallback(callback: MarkerTileRasterLayerCallback?) {
        rasterLayerCallback = callback
    }

    override fun find(position: GeoPointInterface): MarkerEntityInterface<HereActualMarker>? {
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
            val tilingEnabled = tilingOptions.enabled && data.size >= tilingOptions.minMarkerCount
            val currentZoom = currentTileZoom()

            val previousIds = markerManager.allEntities().asSequence().map { it.state.id }.toMutableSet()
            val added = mutableListOf<MarkerOverlayRendererInterface.AddParamsInterface>()
            val updated = mutableListOf<MarkerOverlayRendererInterface.ChangeParamsInterface<HereActualMarker>>()
            val removedActualMarkers = mutableListOf<MarkerEntityInterface<HereActualMarker>>()

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
                            tiledDataChanged = true
                        }
                        updated.add(
                            object : MarkerOverlayRendererInterface.ChangeParamsInterface<HereActualMarker> {
                                override val current: MarkerEntityInterface<HereActualMarker> =
                                    MarkerEntity(
                                        state = state,
                                        marker = prevEntity.marker,
                                        visible = prevEntity.visible,
                                        isRendered = true,
                                    )
                                override val bitmapIcon: BitmapIcon = markerIcon
                                override val prev: MarkerEntityInterface<HereActualMarker> = prevEntity
                            },
                        )
                    }
                    previousIds.remove(state.id)
                } else {
                    if (wantsTiled) {
                        tiledMarkerIds.add(state.id)
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
                        tiledDataChanged = true
                    }
                    removedEntity.marker?.let { removedActualMarkers.add(removedEntity) }
                }
            }

            if (removedActualMarkers.isNotEmpty()) {
                renderer.onRemove(removedActualMarkers)
            }

            if (added.isNotEmpty()) {
                val actualMarkers = renderer.onAdd(added)
                actualMarkers.forEachIndexed { index, actualMarker ->
                    val state = added[index].state
                    val entity =
                        MarkerEntity(
                            marker = actualMarker,
                            state = state,
                            visible = true,
                            isRendered = true,
                        )
                    markerManager.registerEntity(entity)
                    state.getAnimation()?.let { renderer.onAnimate(entity) }
                }
            }

            if (updated.isNotEmpty()) {
                val actualMarkers = renderer.onChange(updated)
                actualMarkers.forEachIndexed { index, actualMarker ->
                    val params = updated[index]
                    actualMarker?.let {
                        markerManager.updateEntity(
                            MarkerEntity(
                                marker = actualMarker,
                                state = params.current.state,
                                visible = params.current.visible,
                                isRendered = true,
                            ),
                        )
                    } ?: run {
                        markerManager.updateEntity(
                            MarkerEntity(
                                marker = params.prev.marker,
                                state = params.current.state,
                                visible = params.current.visible,
                                isRendered = true,
                            ),
                        )
                    }
                    val prevFinger = params.prev.fingerPrint
                    val currentFinger = params.current.fingerPrint
                    if (prevFinger.animation != currentFinger.animation) {
                        params.current.state.getAnimation()?.let {
                            renderer.onAnimate(markerManager.getEntity(params.current.state.id)!!)
                        }
                    }
                }
            }

            renderer.onPostProcess()

            if (tiledDataChanged) {
                syncTiledOverlay(currentZoom)
            } else if (tiledMarkerIds.isNotEmpty()) {
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
                markerManager.updateEntity(
                    MarkerEntity(
                        marker = null,
                        state = state,
                        visible = prevEntity.visible,
                        isRendered = true,
                    ),
                )
                renderer.onPostProcess()
                syncTiledOverlay(currentZoom)
                return@withPermit
            }

            if (wasTiled) {
                tiledMarkerIds.remove(state.id)
            }

            val params =
                object : MarkerOverlayRendererInterface.ChangeParamsInterface<HereActualMarker> {
                    override val current: MarkerEntityInterface<HereActualMarker> =
                        MarkerEntity(
                            marker = prevEntity.marker,
                            state = state,
                            visible = prevEntity.visible,
                            isRendered = true,
                        )
                    override val bitmapIcon: BitmapIcon = markerIcon
                    override val prev: MarkerEntityInterface<HereActualMarker> = prevEntity
                }
            val markers = renderer.onChange(listOf(params))
            markers.firstOrNull()?.let { actual ->
                markerManager.updateEntity(
                    MarkerEntity(
                        marker = actual,
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

        if (tilingOptions.debugLogging) {
            val mapZoomInt = floor(lastKnownZoom).toInt().coerceAtLeast(0)
            val tileZoomInt = currentTileZoom()
            Log.d(
                "HereMarkerController",
                "camera zoom=${"%.3f".format(lastKnownZoom)} mapZoomInt=$mapZoomInt tileZoomInt=$tileZoomInt",
            )
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
        super.destroy()
    }

    private suspend fun updateRasterLayerSource() {
        val groupId = markerTileGroupId ?: return
        val tileRenderer = markerTileRenderer ?: return
        val oldState = markerTileRasterLayerState ?: return
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        val cacheBuster = "${cacheVersion}_${SystemClock.elapsedRealtime()}"

        val newState =
            oldState.copy(
                source =
                    RasterLayerSource.UrlTemplate(
                        template = "${tileServer.urlTemplate(groupId, tileRenderer.tileSize)}?cb=$cacheBuster",
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
            return
        }
        val tileRenderer = getOrCreateTileRenderer()
        tileRenderer.invalidate("markerDataChanged")

        updateRasterLayerSource()
    }

    private fun getOrCreateTileRenderer(): MarkerTileRenderer<HereActualMarker> {
        markerTileRenderer?.let { return it }

        val groupId = UUID.randomUUID().toString()
        markerTileGroupId = groupId

        val outputTileSize = ResourceProvider.getOptimalTileSize().coerceAtLeast(tilingOptions.tileSize)

        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        val tileRenderer =
            MarkerTileRenderer(
                markerManager = markerManager,
                // HERE benefits from higher-res output tiles on high-DPI devices (to avoid GPU upscaling blur),
                // but our world-pixel math should stay on the standard 256px tile grid.
                tileSize = outputTileSize,
                debugTileOverlay = tilingOptions.debugTileOverlay,
            )
        markerTileRenderer = tileRenderer

        if (tilingOptions.debugLogging) {
            Log.d(
                "HereMarkerController",
                "tileRenderer outputTileSize=$outputTileSize worldTileSize=${tilingOptions.tileSize} " +
                    "debugTileOverlay=${tilingOptions.debugTileOverlay}",
            )
        }

        tileServer.register(groupId, tileRenderer)

        markerTileRasterLayerState =
            RasterLayerState(
                id = "marker-tile-$groupId",
                source =
                    RasterLayerSource.UrlTemplate(
                        template = "${tileServer.urlTemplate(groupId, tileRenderer.tileSize)}?cb=$cacheVersion",
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

    companion object {
        fun create(
            holder: HereViewHolder,
            tilingOptions: MarkerTilingOptions = MarkerTilingOptions.Default,
            markerScaleMultiplier: Double = 1.0,
        ): HereMarkerController {
            val renderer =
                HereMarkerRenderer(
                    holder = holder,
                )
            val markerManager = MarkerManager.defaultManager<HereActualMarker>()
            val controller =
                HereMarkerController(
                    markerManager = markerManager,
                    renderer = renderer,
                    tilingOptions = tilingOptions,
                    markerScaleMultiplier = markerScaleMultiplier,
                )
            // HERE camera zoom is updated via onCameraChanged; initialize a best-effort value.
            controller.lastKnownZoom = holder.mapView.camera.state.zoomLevel
            return controller
        }
    }
}
