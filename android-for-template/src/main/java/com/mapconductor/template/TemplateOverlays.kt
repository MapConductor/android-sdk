package com.mapconductor.template

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
class TemplateMarkerRenderer(
    holder: TemplateMapViewHolder,
    coroutine: CoroutineScope,
    val markerManager: MarkerManager<TemplateActualMarker> = MarkerManager.defaultManager(),
) : AbstractMarkerOverlayRenderer<TemplateMapViewHolder, TemplateActualMarker>(holder, coroutine) {
    private val map: TemplateMap get() = holder.map

    /** 画面空間のマーカーアニメーションを使う（ネイティブを一時的に隠せるので）。 */
    override val supportsAnimationOverlay: Boolean = true

    override fun setMarkerVisible(
        markerEntity: MarkerEntityInterface<TemplateActualMarker>,
        visible: Boolean,
    ) {
        markerEntity.marker?.visible = visible
        map.invalidate()
    }

    override fun setMarkerPosition(
        markerEntity: MarkerEntityInterface<TemplateActualMarker>,
        position: GeoPoint,
    ) {
        markerEntity.marker?.position = position
        map.invalidate()
    }

    override suspend fun onAdd(
        data: List<MarkerOverlayRendererInterface.AddParamsInterface>,
    ): List<TemplateActualMarker?> =
        data.map { params ->
            TemplateActualMarker(id = params.state.id, position = params.state.position)
                .also { map.displayList.markers[it.id] = it }
        }

    override suspend fun onChange(
        data: List<MarkerOverlayRendererInterface.ChangeParamsInterface<TemplateActualMarker>>,
    ): List<TemplateActualMarker?> =
        data.map { params ->
            params.current.marker?.apply { position = params.current.state.position }
        }

    override suspend fun onRemove(data: List<MarkerEntityInterface<TemplateActualMarker>>) {
        data.forEach { map.displayList.markers.remove(it.state.id) }
    }

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class TemplateMarkerController(
    renderer: TemplateMarkerRenderer,
) : AbstractMarkerController<TemplateActualMarker>(renderer.markerManager, renderer)

// ── ポリライン ────────────────────────────────────────────────────────────

class TemplatePolylineRenderer(
    val holder: TemplateMapViewHolder,
) : PolylineOverlayRendererInterface<TemplateActualPolyline> {
    private val map: TemplateMap get() = holder.map

    override suspend fun onAdd(
        data: List<PolylineOverlayRendererInterface.AddParamsInterface>,
    ): List<TemplateActualPolyline?> =
        data.map { params ->
            TemplateActualPolyline(id = params.state.id, points = params.state.points)
                .also { map.displayList.polylines[it.id] = it }
        }

    override suspend fun onChange(
        data: List<PolylineOverlayRendererInterface.ChangeParamsInterface<TemplateActualPolyline>>,
    ): List<TemplateActualPolyline?> =
        data.map { params ->
            params.current.polyline?.apply { points = params.current.state.points }
        }

    override suspend fun onRemove(data: List<PolylineEntityInterface<TemplateActualPolyline>>) {
        data.forEach { map.displayList.polylines.remove(it.state.id) }
    }

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class TemplatePolylineController(
    renderer: TemplatePolylineRenderer,
) : PolylineController<TemplateActualPolyline>(PolylineManager(), renderer)

// ── ポリゴン ──────────────────────────────────────────────────────────────

class TemplatePolygonRenderer(
    val holder: TemplateMapViewHolder,
) : PolygonOverlayRendererInterface<TemplateActualPolygon> {
    private val map: TemplateMap get() = holder.map

    override suspend fun onAdd(
        data: List<PolygonOverlayRendererInterface.AddParamsInterface>,
    ): List<TemplateActualPolygon?> =
        data.map { params ->
            TemplateActualPolygon(id = params.state.id, points = params.state.points)
                .also { map.displayList.polygons[it.id] = it }
        }

    override suspend fun onChange(
        data: List<PolygonOverlayRendererInterface.ChangeParamsInterface<TemplateActualPolygon>>,
    ): List<TemplateActualPolygon?> =
        data.map { params ->
            params.current.polygon?.apply { points = params.current.state.points }
        }

    override suspend fun onRemove(data: List<PolygonEntityInterface<TemplateActualPolygon>>) {
        data.forEach { map.displayList.polygons.remove(it.state.id) }
    }

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class TemplatePolygonController(
    renderer: TemplatePolygonRenderer,
) : PolygonController<TemplateActualPolygon>(PolygonManager(), renderer)

// ── 円 ────────────────────────────────────────────────────────────────────

class TemplateCircleRenderer(
    val holder: TemplateMapViewHolder,
) : CircleOverlayRendererInterface<TemplateActualCircle> {
    private val map: TemplateMap get() = holder.map

    override suspend fun onAdd(
        data: List<CircleOverlayRendererInterface.AddParamsInterface>,
    ): List<TemplateActualCircle?> =
        data.map { params ->
            TemplateActualCircle(
                id = params.state.id,
                center = params.state.center,
                radiusMeters = params.state.radiusMeters,
            ).also { map.displayList.circles[it.id] = it }
        }

    override suspend fun onChange(
        data: List<CircleOverlayRendererInterface.ChangeParamsInterface<TemplateActualCircle>>,
    ): List<TemplateActualCircle?> =
        data.map { params ->
            params.current.circle?.apply {
                center = params.current.state.center
                radiusMeters = params.current.state.radiusMeters
            }
        }

    override suspend fun onRemove(data: List<CircleEntityInterface<TemplateActualCircle>>) {
        data.forEach { map.displayList.circles.remove(it.state.id) }
    }

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class TemplateCircleController(
    renderer: TemplateCircleRenderer,
) : CircleController<TemplateActualCircle>(CircleManager(), renderer)

// ── グラウンドイメージ ────────────────────────────────────────────────────

class TemplateGroundImageRenderer(
    val holder: TemplateMapViewHolder,
) : GroundImageOverlayRendererInterface<TemplateActualGroundImage> {
    private val map: TemplateMap get() = holder.map

    override suspend fun onAdd(
        data: List<GroundImageOverlayRendererInterface.AddParamsInterface>,
    ): List<TemplateActualGroundImage?> =
        data.map { params ->
            TemplateActualGroundImage(id = params.state.id)
                .also { map.displayList.groundImages[it.id] = it }
        }

    override suspend fun onChange(
        data: List<GroundImageOverlayRendererInterface.ChangeParamsInterface<TemplateActualGroundImage>>,
    ): List<TemplateActualGroundImage?> = data.map { it.current.groundImage }

    override suspend fun onRemove(data: List<GroundImageEntityInterface<TemplateActualGroundImage>>) {
        data.forEach { map.displayList.groundImages.remove(it.state.id) }
    }

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class TemplateGroundImageController(
    renderer: TemplateGroundImageRenderer,
) : GroundImageController<TemplateActualGroundImage>(GroundImageManager(), renderer)

// ── ラスターレイヤ ────────────────────────────────────────────────────────

class TemplateRasterLayerRenderer(
    val holder: TemplateMapViewHolder,
    override val coroutine: CoroutineScope,
) : RasterLayerOverlayRendererInterface<TemplateActualRasterLayer> {
    private val map: TemplateMap get() = holder.map

    override suspend fun onAdd(
        data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>,
    ): List<TemplateActualRasterLayer?> =
        data.map { params ->
            TemplateActualRasterLayer(id = params.state.id)
                .also { map.displayList.rasterLayers[it.id] = it }
        }

    override suspend fun onChange(
        data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<TemplateActualRasterLayer>>,
    ): List<TemplateActualRasterLayer?> = data.map { it.current.layer }

    override suspend fun onRemove(data: List<RasterLayerEntityInterface<TemplateActualRasterLayer>>) {
        data.forEach { map.displayList.rasterLayers.remove(it.state.id) }
    }

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) = Unit

    override suspend fun onPostProcess() {
        map.invalidate()
    }
}

class TemplateRasterLayerController(
    renderer: TemplateRasterLayerRenderer,
) : RasterLayerController<TemplateActualRasterLayer>(RasterLayerManager(), renderer)
