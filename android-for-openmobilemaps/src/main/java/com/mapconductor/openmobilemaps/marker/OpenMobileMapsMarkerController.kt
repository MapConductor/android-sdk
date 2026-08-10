package com.mapconductor.openmobilemaps.marker

import com.mapconductor.core.controller.OnCameraChangeReceiverInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.core.marker.MarkerIngestionEngine
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.MarkerTileRasterLayerCallback
import com.mapconductor.core.marker.MarkerTileRenderer
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.core.marker.MarkerViewportSwitch
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.tileserver.TileServerRegistry
import com.mapconductor.openmobilemaps.OpenMobileMapsActualMarker
import com.mapconductor.openmobilemaps.OpenMobileMapsMarkerOverlayRenderer
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit

/**
 * マーカーのコントローラ。**タイル描画の駆動**がここの仕事。
 *
 * 素の追加・更新・削除はコアの [AbstractMarkerController] が持っている。ここが上書きして
 * いるのは、マーカーが多いときに「ネイティブのアイコンをやめてラスタータイルとして焼く」
 * 切り替えを回すためで、android-for-maplibre / mapbox / here などと**同じ構造**である
 * （型引数以外は同型）。
 *
 * ## 経路
 *
 * ```
 * ingest → tiledMarkerIds へ振り分け
 *        → MarkerTileRenderer がタイルを描く
 *        → ローカルタイルサーバに登録
 *        → RasterLayerState として rasterLayerCallback へ渡す
 *        → OpenMobileMapsRasterLayerController が地図へ載せる
 * ```
 *
 * つまり**ラスターレイヤが動かないとタイル方式のマーカーも出ない**。
 */
class OpenMobileMapsMarkerController(
    renderer: OpenMobileMapsMarkerOverlayRenderer,
    private val markerTiling: MarkerTilingOptions = MarkerTilingOptions.Default,
    private val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractMarkerController<OpenMobileMapsActualMarker>(
        markerManager = renderer.markerManager,
        renderer = renderer,
    ),
    OnCameraChangeReceiverInterface {
    private val defaultMarkerIcon: BitmapIcon by lazy { DefaultMarkerIcon().toBitmapIcon() }
    private val tiledMarkerIds = LinkedHashSet<String>()

    private val tileServer = TileServerRegistry.get()
    private var markerTileRenderer: MarkerTileRenderer<OpenMobileMapsActualMarker>? = null
    private var markerTileGroupId: String? = null
    private var markerTileRasterLayerState: RasterLayerState? = null
    private var rasterLayerCallback: MarkerTileRasterLayerCallback? = null
    private var cacheVersion: Int = 0

    /** ビューポート内が少ないときだけタイルをやめてネイティブのアイコンで描く切り替え器。 */
    private val viewportSwitch by lazy {
        MarkerViewportSwitch(
            markerManager = markerManager,
            renderer = renderer,
            defaultMarkerIcon = defaultMarkerIcon,
            semaphore = semaphore,
            policy = markerTiling.viewport,
            setTileLayerVisible = ::setTileLayerVisible,
            invalidateTiles = ::updateRasterLayerSource,
        )
    }

    fun setRasterLayerCallback(callback: MarkerTileRasterLayerCallback?) {
        rasterLayerCallback = callback
    }

    override suspend fun add(data: List<MarkerState>) {
        // ingest はタイル担当 entity を marker = null で登録し直すので、先に昇格を戻す。
        // semaphore は再入不可なので withPermit の外で呼ぶこと。
        viewportSwitch.retract()
        semaphore.withPermit {
            val tilingEnabled = markerTiling.enabled && data.size >= markerManager.minMarkerCount
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
                syncTiledOverlay()
            } else if (result.hasTiledMarkers) {
                if (markerTileRenderer == null || markerTileRasterLayerState == null) {
                    syncTiledOverlay()
                }
            } else {
                removeTileOverlay()
            }
        }
        viewportSwitch.requestReapply()
    }

    override suspend fun update(state: MarkerState) {
        if (!markerManager.hasEntity(state.id)) return

        if (viewportSwitch.isPromoted(state.id)) viewportSwitch.release(state.id)

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
                        // tiling を立てないと MarkerTileRenderer の絞り込みから漏れ、
                        // タイル昇格したのにタイルへ描かれないマーカーになる。
                        tiling = true,
                    ),
                )
                renderer.onPostProcess()
                syncTiledOverlay()
                return@withPermit
            }

            if (wasTiled) {
                tiledMarkerIds.remove(state.id)
            }

            val params =
                object : MarkerOverlayRendererInterface.ChangeParamsInterface<OpenMobileMapsActualMarker> {
                    override val current: MarkerEntityInterface<OpenMobileMapsActualMarker> =
                        MarkerEntity(
                            marker = prevEntity.marker,
                            state = state,
                            visible = prevEntity.visible,
                            isRendered = true,
                        )
                    override val bitmapIcon: BitmapIcon = markerIcon
                    override val prev: MarkerEntityInterface<OpenMobileMapsActualMarker> = prevEntity
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
                syncTiledOverlay()
            } else {
                removeTileOverlay()
            }
        }
        viewportSwitch.requestReapply()
    }

    override suspend fun clear() {
        viewportSwitch.destroy()
        semaphore.withPermit {
            val entities = markerManager.allEntities()
            val toRemove = entities.filter { it.marker != null }
            if (toRemove.isNotEmpty()) {
                renderer.onRemove(toRemove)
            }
            markerManager.clear()
            tiledMarkerIds.clear()
            removeTileOverlay()
            renderer.onPostProcess()
        }
    }

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) {
        viewportSwitch.onCameraChanged(mapCameraPosition)
    }

    /**
     * マーカータイルのラスターレイヤの表示だけを切り替える。
     *
     * source（URL）には触らない。触るとタイルを取り直すことになり、切り替えのたびに
     * タイルキャッシュを捨てるのと同じになる。
     */
    private suspend fun setTileLayerVisible(visible: Boolean) {
        val current = markerTileRasterLayerState ?: return
        if (current.visible == visible) return
        val newState = current.copy(visible = visible)
        markerTileRasterLayerState = newState
        rasterLayerCallback?.onRasterLayerUpdate(newState)
    }

    override fun destroy() {
        viewportSwitch.destroy()
        // タイルサーバはプロセス共有のシングルトン。**stop してはいけない**
        // （他の地図やオーバーレイ拡張のタイルまで止まる）。自分の経路だけ外す。
        markerTileGroupId?.let { tileServer.unregister(it) }
        markerTileGroupId = null
        markerTileRenderer = null

        coroutine.launch { rasterLayerCallback?.onRasterLayerUpdate(null) }
        markerTileRasterLayerState = null
        super.destroy()
    }

    private suspend fun updateRasterLayerSource() {
        val groupId = markerTileGroupId ?: return
        val tileRenderer = markerTileRenderer ?: return
        val oldState = markerTileRasterLayerState ?: return
        cacheVersion = (cacheVersion + 1) and 0x7fffffff
        tileRenderer.invalidate()

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

    private suspend fun syncTiledOverlay() {
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

    private fun getOrCreateTileRenderer(): MarkerTileRenderer<OpenMobileMapsActualMarker> {
        synchronized(this) {
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
    }

    private suspend fun removeTileOverlay() {
        markerTileGroupId?.let { tileServer.unregister(it) }
        markerTileGroupId = null
        markerTileRenderer = null

        rasterLayerCallback?.onRasterLayerUpdate(null)
        markerTileRasterLayerState = null
    }
}
