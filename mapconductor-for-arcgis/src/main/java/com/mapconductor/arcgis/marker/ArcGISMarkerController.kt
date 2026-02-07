package com.mapconductor.arcgis.marker

import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.SurfacePlacement
import com.mapconductor.arcgis.ArcGISActualMarker
import com.mapconductor.arcgis.map.ArcGISMapViewHolder
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
import com.mapconductor.core.marker.MarkerTileRasterLayerCallback
import com.mapconductor.core.marker.MarkerTileRenderer
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.tileserver.TileServerRegistry
import com.mapconductor.settings.Settings
import java.util.UUID
import kotlin.math.floor
import android.os.SystemClock
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit

internal data class SelectedMarker(
    val state: MarkerState,
    val graphic: Graphic,
)

class ArcGISMarkerController private constructor(
    markerManager: MarkerManager<ArcGISActualMarker>,
    override val renderer: ArcGISMarkerRenderer,
    private val markerTiling: MarkerTilingOptions,
) : AbstractMarkerController<ArcGISActualMarker>(
        markerManager = markerManager,
        renderer = renderer,
    ) {
    private var internalSelectedMarker: SelectedMarker? = null

    private val defaultMarkerIcon: BitmapIcon = DefaultMarkerIcon().toBitmapIcon()
    private val tiledMarkerIds = LinkedHashSet<String>()

    @Volatile
    private var lastKnownZoom: Double = 0.0

    private val tileServer = TileServerRegistry.get(forceNoStoreCache = true)
    private var markerTileRenderer: MarkerTileRenderer<ArcGISActualMarker>? = null
    private var markerTileGroupId: String? = null
    private var markerTileRasterLayerState: RasterLayerState? = null
    private var rasterLayerCallback: MarkerTileRasterLayerCallback? = null
    private var cacheVersion: Int = 0

    internal var selectedMarker: SelectedMarker?
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

    override fun find(position: GeoPointInterface): MarkerEntityInterface<ArcGISActualMarker>? {
        val nearest = markerManager.findNearest(position) ?: return null

        val touchScreen = renderer.holder.toScreenOffset(position) ?: return null
        val markerScreen = renderer.holder.toScreenOffset(nearest.state.position) ?: return null

        val tolerancePx =
            Settings.Default.tapTolerance.value
                .toDouble() *
                ResourceProvider.getDensity().toDouble()

        val icon = nearest.state.icon ?: DefaultMarkerIcon()

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
                renderer.onPostProcess()
                syncTiledOverlay(currentZoom)
                return@withPermit
            }

            if (wasTiled) {
                tiledMarkerIds.remove(state.id)
            }

            val params =
                object : MarkerOverlayRendererInterface.ChangeParamsInterface<ArcGISActualMarker> {
                    override val current: MarkerEntityInterface<ArcGISActualMarker> =
                        MarkerEntity(
                            marker = prevEntity.marker,
                            state = state,
                            visible = prevEntity.visible,
                            isRendered = true,
                        )
                    override val bitmapIcon: BitmapIcon = markerIcon
                    override val prev: MarkerEntityInterface<ArcGISActualMarker> = prevEntity
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
    }

    override fun destroy() {
        markerTileGroupId?.let { groupId ->
            tileServer.unregister(groupId)
        }
        markerTileGroupId = null
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
        if (!markerTiling.enabled) {
            removeTileOverlay()
            tiledMarkerIds.clear()
            return
        }

        getOrCreateTileRenderer()
        updateRasterLayerSource()
    }

    private fun getOrCreateTileRenderer(): MarkerTileRenderer<ArcGISActualMarker> {
        markerTileRenderer?.let { return it }

        val groupId = UUID.randomUUID().toString()
        markerTileGroupId = groupId

        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        val tileRenderer =
            MarkerTileRenderer<ArcGISActualMarker>(
                markerManager = markerManager,
                tileSize = 256,
                cacheSizeBytes = markerTiling.cacheSize,
                debugTileOverlay = markerTiling.debugTileOverlay,
                iconScaleCallback = markerTiling.iconScaleCallback,
            )
        markerTileRenderer = tileRenderer

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
        markerTileRenderer = null

        rasterLayerCallback?.onRasterLayerUpdate(null)
        markerTileRasterLayerState = null
    }

    companion object {
        fun create(
            holder: ArcGISMapViewHolder,
            markerTiling: MarkerTilingOptions = MarkerTilingOptions.Default,
        ): ArcGISMarkerController {
            val markerLayer: GraphicsOverlay =
                GraphicsOverlay().apply {
                    sceneProperties.surfacePlacement = SurfacePlacement.Relative
                }

            val renderer =
                ArcGISMarkerRenderer(
                    markerLayer = markerLayer,
                    holder = holder,
                )

            val markerManager =
                MarkerManager.defaultManager<ArcGISActualMarker>(
                    minMarkerCount = markerTiling.minMarkerCount,
                )

            val controller =
                ArcGISMarkerController(
                    markerManager = markerManager,
                    renderer = renderer,
                    markerTiling = markerTiling,
                )
            return controller
        }
    }
}
