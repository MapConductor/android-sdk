package com.mapconductor.openmobilemaps

import com.mapconductor.core.circle.CircleController
import com.mapconductor.core.circle.CircleEntityInterface
import com.mapconductor.core.circle.CircleManager
import com.mapconductor.core.circle.CircleOverlayRendererInterface
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.groundimage.GroundImageController
import com.mapconductor.core.groundimage.GroundImageEntityInterface
import com.mapconductor.core.groundimage.GroundImageManager
import com.mapconductor.core.groundimage.GroundImageOverlayRendererInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.AbstractMarkerController
import com.mapconductor.core.marker.AbstractMarkerOverlayRenderer
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonEntityInterface
import com.mapconductor.core.polygon.PolygonManager
import com.mapconductor.core.polygon.PolygonOverlayRendererInterface
import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineEntityInterface
import com.mapconductor.core.polyline.PolylineManager
import com.mapconductor.core.polyline.PolylineOverlayRendererInterface
import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerEntityInterface
import com.mapconductor.core.raster.RasterLayerManager
import com.mapconductor.core.raster.RasterLayerOverlayRendererInterface
import kotlinx.coroutines.CoroutineScope

/*
 * オーバーレイのレンダラとコントローラ。
 *
 * ## ドライバーが本当に書くのはこのファイルだけ
 *
 * ここが「地図SDK固有の翻訳」であり、共通化できない本体である。やることは 4 つだけ:
 *
 *   onAdd        コアの `State` を SDK のネイティブオブジェクトへ変換して地図に載せ、
 *                作った `ActualXxx` を**同じ並び順で**返す（null は「作れなかった」）
 *   onChange     既にあるネイティブオブジェクトを更新する
 *   onRemove     地図から外す
 *   onPostProcess  まとめて反映する SDK 向けのフラッシュ点（不要なら空でよい）
 *
 * **当たり判定・クリックの配送・ドラッグの状態遷移・カメラの換算は書かない。**
 * すべてコアが持っている。
 *
 * ## コントローラは 1 行
 *
 * コアの `XxxController` を継承するだけ。`find` / `has` / `resolveTap` /
 * `dispatchClick` はコアの実装がそのまま使われる。
 */

// ── マーカー ──────────────────────────────────────────────────────────────

/** マーカーのレンダラ。SDK のマーカーを作って地図へ載せる。 */
class OpenMobileMapsMarkerRenderer(
    holder: OpenMobileMapsMapViewHolder,
    coroutine: CoroutineScope,
    val markerManager: MarkerManager<OpenMobileMapsActualMarker> = MarkerManager.defaultManager(),
) : AbstractMarkerOverlayRenderer<OpenMobileMapsMapViewHolder, OpenMobileMapsActualMarker>(holder, coroutine) {
    private val map: OpenMobileMapsMap get() = holder.map

    /** 画面空間のマーカーアニメーションを使う（ネイティブを一時的に隠せるので）。 */
    override val supportsAnimationOverlay: Boolean = true

    override fun setMarkerVisible(
        markerEntity: MarkerEntityInterface<OpenMobileMapsActualMarker>,
        visible: Boolean,
    ) {
        markerEntity.marker?.visible = visible
        map.invalidate()
    }

    override fun setMarkerPosition(
        markerEntity: MarkerEntityInterface<OpenMobileMapsActualMarker>,
        position: GeoPoint,
    ) {
        markerEntity.marker?.position = position
        map.invalidate()
    }

    override suspend fun onAdd(
        data: List<MarkerOverlayRendererInterface.AddParamsInterface>,
    ): List<OpenMobileMapsActualMarker?> =
        data.map { params ->
            OpenMobileMapsActualMarker(id = params.state.id, position = params.state.position)
                .also { map.displayList.markers[it.id] = it }
        }

    override suspend fun onChange(
        data: List<MarkerOverlayRendererInterface.ChangeParamsInterface<OpenMobileMapsActualMarker>>,
    ): List<OpenMobileMapsActualMarker?> =
        data.map { params ->
            params.current.marker?.apply { position = params.current.state.position }
        }

    override suspend fun onRemove(data: List<MarkerEntityInterface<OpenMobileMapsActualMarker>>) {
        data.forEach { map.displayList.markers.remove(it.state.id) }
    }

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class OpenMobileMapsMarkerController(
    renderer: OpenMobileMapsMarkerRenderer,
) : AbstractMarkerController<OpenMobileMapsActualMarker>(renderer.markerManager, renderer)

// ── ポリライン ────────────────────────────────────────────────────────────

class OpenMobileMapsPolylineRenderer(
    val holder: OpenMobileMapsMapViewHolder,
) : PolylineOverlayRendererInterface<OpenMobileMapsActualPolyline> {
    private val map: OpenMobileMapsMap get() = holder.map

    override suspend fun onAdd(
        data: List<PolylineOverlayRendererInterface.AddParamsInterface>,
    ): List<OpenMobileMapsActualPolyline?> =
        data.map { params ->
            OpenMobileMapsActualPolyline(id = params.state.id, points = params.state.points)
                .also { map.displayList.polylines[it.id] = it }
        }

    override suspend fun onChange(
        data: List<PolylineOverlayRendererInterface.ChangeParamsInterface<OpenMobileMapsActualPolyline>>,
    ): List<OpenMobileMapsActualPolyline?> =
        data.map { params ->
            params.current.polyline?.apply { points = params.current.state.points }
        }

    override suspend fun onRemove(data: List<PolylineEntityInterface<OpenMobileMapsActualPolyline>>) {
        data.forEach { map.displayList.polylines.remove(it.state.id) }
    }

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class OpenMobileMapsPolylineController(
    renderer: OpenMobileMapsPolylineRenderer,
) : PolylineController<OpenMobileMapsActualPolyline>(PolylineManager(), renderer)

// ── ポリゴン ──────────────────────────────────────────────────────────────

class OpenMobileMapsPolygonRenderer(
    val holder: OpenMobileMapsMapViewHolder,
) : PolygonOverlayRendererInterface<OpenMobileMapsActualPolygon> {
    private val map: OpenMobileMapsMap get() = holder.map

    override suspend fun onAdd(
        data: List<PolygonOverlayRendererInterface.AddParamsInterface>,
    ): List<OpenMobileMapsActualPolygon?> =
        data.map { params ->
            OpenMobileMapsActualPolygon(id = params.state.id, points = params.state.points)
                .also { map.displayList.polygons[it.id] = it }
        }

    override suspend fun onChange(
        data: List<PolygonOverlayRendererInterface.ChangeParamsInterface<OpenMobileMapsActualPolygon>>,
    ): List<OpenMobileMapsActualPolygon?> =
        data.map { params ->
            params.current.polygon?.apply { points = params.current.state.points }
        }

    override suspend fun onRemove(data: List<PolygonEntityInterface<OpenMobileMapsActualPolygon>>) {
        data.forEach { map.displayList.polygons.remove(it.state.id) }
    }

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class OpenMobileMapsPolygonController(
    renderer: OpenMobileMapsPolygonRenderer,
) : PolygonController<OpenMobileMapsActualPolygon>(PolygonManager(), renderer)

// ── 円 ────────────────────────────────────────────────────────────────────

class OpenMobileMapsCircleRenderer(
    val holder: OpenMobileMapsMapViewHolder,
) : CircleOverlayRendererInterface<OpenMobileMapsActualCircle> {
    private val map: OpenMobileMapsMap get() = holder.map

    override suspend fun onAdd(
        data: List<CircleOverlayRendererInterface.AddParamsInterface>,
    ): List<OpenMobileMapsActualCircle?> =
        data.map { params ->
            OpenMobileMapsActualCircle(
                id = params.state.id,
                center = params.state.center,
                radiusMeters = params.state.radiusMeters,
            ).also { map.displayList.circles[it.id] = it }
        }

    override suspend fun onChange(
        data: List<CircleOverlayRendererInterface.ChangeParamsInterface<OpenMobileMapsActualCircle>>,
    ): List<OpenMobileMapsActualCircle?> =
        data.map { params ->
            params.current.circle?.apply {
                center = params.current.state.center
                radiusMeters = params.current.state.radiusMeters
            }
        }

    override suspend fun onRemove(data: List<CircleEntityInterface<OpenMobileMapsActualCircle>>) {
        data.forEach { map.displayList.circles.remove(it.state.id) }
    }

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class OpenMobileMapsCircleController(
    renderer: OpenMobileMapsCircleRenderer,
) : CircleController<OpenMobileMapsActualCircle>(CircleManager(), renderer)

// ── グラウンドイメージ ────────────────────────────────────────────────────

class OpenMobileMapsGroundImageRenderer(
    val holder: OpenMobileMapsMapViewHolder,
) : GroundImageOverlayRendererInterface<OpenMobileMapsActualGroundImage> {
    private val map: OpenMobileMapsMap get() = holder.map

    override suspend fun onAdd(
        data: List<GroundImageOverlayRendererInterface.AddParamsInterface>,
    ): List<OpenMobileMapsActualGroundImage?> =
        data.map { params ->
            OpenMobileMapsActualGroundImage(id = params.state.id)
                .also { map.displayList.groundImages[it.id] = it }
        }

    override suspend fun onChange(
        data: List<GroundImageOverlayRendererInterface.ChangeParamsInterface<OpenMobileMapsActualGroundImage>>,
    ): List<OpenMobileMapsActualGroundImage?> = data.map { it.current.groundImage }

    override suspend fun onRemove(data: List<GroundImageEntityInterface<OpenMobileMapsActualGroundImage>>) {
        data.forEach { map.displayList.groundImages.remove(it.state.id) }
    }

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class OpenMobileMapsGroundImageController(
    renderer: OpenMobileMapsGroundImageRenderer,
) : GroundImageController<OpenMobileMapsActualGroundImage>(GroundImageManager(), renderer)

// ── ラスターレイヤ ────────────────────────────────────────────────────────

class OpenMobileMapsRasterLayerRenderer(
    val holder: OpenMobileMapsMapViewHolder,
    override val coroutine: CoroutineScope,
) : RasterLayerOverlayRendererInterface<OpenMobileMapsActualRasterLayer> {
    private val map: OpenMobileMapsMap get() = holder.map

    override suspend fun onAdd(
        data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>,
    ): List<OpenMobileMapsActualRasterLayer?> =
        data.map { params ->
            OpenMobileMapsActualRasterLayer(id = params.state.id)
                .also { map.displayList.rasterLayers[it.id] = it }
        }

    override suspend fun onChange(
        data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<OpenMobileMapsActualRasterLayer>>,
    ): List<OpenMobileMapsActualRasterLayer?> = data.map { it.current.layer }

    override suspend fun onRemove(data: List<RasterLayerEntityInterface<OpenMobileMapsActualRasterLayer>>) {
        data.forEach { map.displayList.rasterLayers.remove(it.state.id) }
    }

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) = Unit

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class OpenMobileMapsRasterLayerController(
    renderer: OpenMobileMapsRasterLayerRenderer,
) : RasterLayerController<OpenMobileMapsActualRasterLayer>(RasterLayerManager(), renderer)
