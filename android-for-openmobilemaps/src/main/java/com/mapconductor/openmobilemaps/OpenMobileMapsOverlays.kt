package com.mapconductor.openmobilemaps

import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.circle.AbstractCircleOverlayRenderer
import com.mapconductor.core.circle.CircleController
import com.mapconductor.core.circle.CircleEntityInterface
import com.mapconductor.core.circle.CircleManagerInterface
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.geometry.buildPolygonRings
import com.mapconductor.core.geometry.buildPolylineSegments
import com.mapconductor.core.geometry.circleToRing
import com.mapconductor.core.geometry.closeRing
import com.mapconductor.core.groundimage.AbstractGroundImageOverlayRenderer
import com.mapconductor.core.groundimage.GroundImageController
import com.mapconductor.core.groundimage.GroundImageEntityInterface
import com.mapconductor.core.groundimage.GroundImageManager
import com.mapconductor.core.groundimage.GroundImageState
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.marker.AbstractMarkerOverlayRenderer
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.polygon.AbstractPolygonOverlayRenderer
import com.mapconductor.core.polygon.PolygonController
import com.mapconductor.core.polygon.PolygonEntityInterface
import com.mapconductor.core.polygon.PolygonManagerInterface
import com.mapconductor.core.polygon.PolygonState
import com.mapconductor.core.polygon.ensureClockwiseRing
import com.mapconductor.core.polygon.ensureCounterClockwise
import com.mapconductor.core.polygon.unionHoles
import com.mapconductor.core.polyline.AbstractPolylineOverlayRenderer
import com.mapconductor.core.polyline.PolylineController
import com.mapconductor.core.polyline.PolylineEntityInterface
import com.mapconductor.core.polyline.PolylineManagerInterface
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerEntityInterface
import com.mapconductor.core.raster.RasterLayerManager
import com.mapconductor.core.raster.RasterLayerManagerInterface
import com.mapconductor.core.raster.RasterLayerOverlayRendererInterface
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.openmobilemaps.tile.WebMercatorTileLayerConfig
import io.openmobilemaps.mapscore.graphics.BitmapTextureHolder
import io.openmobilemaps.mapscore.shared.graphics.common.Vec2F
import io.openmobilemaps.mapscore.shared.graphics.common.Vec3D
import io.openmobilemaps.mapscore.shared.graphics.objects.TextureHolderInterface
import io.openmobilemaps.mapscore.shared.graphics.shader.BlendMode
import io.openmobilemaps.mapscore.shared.map.coordinates.Coord
import io.openmobilemaps.mapscore.shared.map.coordinates.CoordinateSystemIdentifiers
import io.openmobilemaps.mapscore.shared.map.coordinates.RectCoord
import io.openmobilemaps.mapscore.shared.map.layers.ColorStateList
import io.openmobilemaps.mapscore.shared.map.layers.SizeType
import io.openmobilemaps.mapscore.shared.map.layers.icon.IconFactory
import io.openmobilemaps.mapscore.shared.map.layers.icon.IconLayerInterface
import io.openmobilemaps.mapscore.shared.map.layers.icon.IconType
import io.openmobilemaps.mapscore.shared.map.layers.line.LineCapType
import io.openmobilemaps.mapscore.shared.map.layers.line.LineFactory
import io.openmobilemaps.mapscore.shared.map.layers.line.LineInfoInterface
import io.openmobilemaps.mapscore.shared.map.layers.line.LineJoinType
import io.openmobilemaps.mapscore.shared.map.layers.line.LineLayerInterface
import io.openmobilemaps.mapscore.shared.map.layers.line.LineStyle
import io.openmobilemaps.mapscore.shared.map.layers.polygon.PolygonInfo
import io.openmobilemaps.mapscore.shared.map.layers.polygon.PolygonLayerInterface
import io.openmobilemaps.mapscore.shared.map.layers.polygon.TexturedPolygonLayerInterface
import io.openmobilemaps.mapscore.shared.map.layers.tiled.raster.Tiled2dMapRasterLayerInterface
import io.openmobilemaps.mapscore.shared.map.loader.LoaderInterface
import kotlin.math.abs
import kotlin.math.cos
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/*
 * オーバーレイのレンダラとコントローラ。**ドライバーが本当に書くのはこのファイル**である。
 *
 * ## この SDK の描画モデル
 *
 * Open Mobile Maps は「オーバーレイ 1 つ = ネイティブオブジェクト 1 つ」ではなく、
 * **種別ごとに 1 枚のレイヤがあり、そこへ要素のリストを流し込む**形（MapLibre の
 * GeoJSON ソースに近い）。したがって各レンダラは
 *
 *   onAdd / onChange  … 要素（`IconInfoInterface` / `LineInfoInterface` / `PolygonInfo`）を作る
 *   onPostProcess     … マネージャの全要素を集めてレイヤへ一括で流す
 *
 * という形になる。個別の `onRemove` でレイヤを触らないのは、どのみち
 * [onPostProcess] で全量を流し直すためである。
 *
 * ## 当たり判定・カスケード・ドラッグの状態遷移は書かない
 *
 * すべてコアが持っている。このファイルにあるのは「MapConductor の State を
 * SDK の型へ翻訳する」ことだけ。
 */

// ── マーカー ──────────────────────────────────────────────────────────────

/**
 * マーカーのレンダラ。
 *
 * ## ドラッグ層を持たない
 *
 * MapLibre / Mapbox は GeoJSON ソースを丸ごと差し替える方式なので、ドラッグ中の
 * マーカーを専用レイヤへ逃がさないと指の位置と食い違う。Open Mobile Maps は
 * [io.openmobilemaps.mapscore.shared.map.layers.icon.IconInfoInterface.setCoordinate] で
 * **要素を直接動かせる**ので、その必要が無い（HERE と同じ立場）。
 * [onDragPositionChanged] で座標を書き換えて [IconLayerInterface.invalidate] するだけでよい。
 */
class OpenMobileMapsMarkerOverlayRenderer(
    holder: OpenMobileMapsMapViewHolder,
    val markerManager: MarkerManager<OpenMobileMapsActualMarker>,
    val iconLayer: IconLayerInterface,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractMarkerOverlayRenderer<OpenMobileMapsMapViewHolder, OpenMobileMapsActualMarker>(holder, coroutine) {
    /** 画面空間のマーカーアニメーションを使う（ネイティブを一時的に隠せるので）。 */
    override val supportsAnimationOverlay: Boolean = true

    private var draggingEntity: MarkerEntityInterface<OpenMobileMapsActualMarker>? = null

    /** アイコンごとのテクスチャ。詳しい理由は [textureFor] を参照。 */
    private val textureCache = mutableMapOf<Int, TextureHolderInterface>()

    /** いま地図上のアイコンに掛かっている縦の引き伸ばし。詳細は [verticalStretch]。 */
    private var appliedStretch: Float = 1.0f

    /**
     * アイコン id → 引き伸ばす前の高さ（px）。
     *
     * **比率を掛け続けないこと。** 以前は「今の大きさ × 変化比」で更新していたが、
     * [createIcon] も [appliedStretch] を書くため、マーカーの追加と傾きの変更が
     * 混ざると比率の基準がずれ、アイコンの大きさが本来の値から離れていく。
     * アンカーは割合なので、大きさがずれるとアイコンの見える位置もずれる。
     * 元の高さを覚えておいて**毎回 `元の高さ × 引き伸ばし` を入れる**ほうが、
     * 順序に関係なく必ず正しい。
     */
    private val baseIconHeight = mutableMapOf<String, Float>()

    override fun setMarkerVisible(
        markerEntity: MarkerEntityInterface<OpenMobileMapsActualMarker>,
        visible: Boolean,
    ) {
        markerEntity.visible = visible
        applyIcons()
    }

    override fun setMarkerPosition(
        markerEntity: MarkerEntityInterface<OpenMobileMapsActualMarker>,
        position: GeoPoint,
    ) {
        markerEntity.marker?.setCoordinate(position.toOmmCoord())
        iconLayer.invalidate()
    }

    override suspend fun onAdd(
        data: List<MarkerOverlayRendererInterface.AddParamsInterface>,
    ): List<OpenMobileMapsActualMarker?> = data.map { createIcon(it.state.id, it.state.position, it.bitmapIcon) }

    override suspend fun onChange(
        data: List<MarkerOverlayRendererInterface.ChangeParamsInterface<OpenMobileMapsActualMarker>>,
    ): List<OpenMobileMapsActualMarker?> =
        data.map { params ->
            val state = params.current.state
            val previous = params.prev.marker
            // アイコンの見た目が変わっていなければ座標だけ動かす。作り直すと
            // テクスチャを毎回アップロードし直すことになり、ドラッグ中に目に見えて重くなる。
            if (previous != null && params.current.fingerPrint.icon == params.prev.fingerPrint.icon) {
                previous.setCoordinate(state.position.toOmmCoord())
                previous
            } else {
                createIcon(state.id, state.position, params.bitmapIcon)
            }
        }

    override suspend fun onRemove(data: List<MarkerEntityInterface<OpenMobileMapsActualMarker>>) {
        data.forEach { baseIconHeight.remove(it.state.id) }
        iconLayer.removeIdentifierList(ArrayList(data.map { it.state.id }))
    }

    override suspend fun onPostProcess() {
        applyIcons()
    }

    override fun onDragSelectionChanged(
        previous: MarkerEntityInterface<OpenMobileMapsActualMarker>?,
        current: MarkerEntityInterface<OpenMobileMapsActualMarker>?,
    ) {
        draggingEntity = current
    }

    override fun onDragPositionChanged(position: GeoPointInterface) {
        val entity = draggingEntity ?: return
        entity.marker?.setCoordinate(position.toOmmCoord())
        iconLayer.invalidate()
    }

    /**
     * マネージャの全マーカーをレイヤへ流し直す。
     *
     * `tiling` が立っている entity は [com.mapconductor.core.marker.MarkerTileRenderer] が
     * ラスタータイルとして描くので、ここでは除く（除かないと同じマーカーが二重に出る）。
     */
    private fun applyIcons() {
        val icons =
            markerManager
                .allEntities()
                .asSequence()
                .filter { !it.tiling && it.visible }
                .mapNotNull { it.marker }
                .toCollection(ArrayList())
        iconLayer.setIcons(icons)
    }

    /**
     * 傾きが変わったので、アイコンの縦の引き伸ばしを付け直す。
     *
     * 既にあるアイコンの大きさを比率で直すだけなので、`BitmapIcon` を作り直さない。
     */
    fun onVisualTiltChanged() {
        val stretch = verticalStretch()
        // 目に見えない差でレイヤを作り直さない。カメラアニメーション中は毎フレーム
        // 呼ばれるので、ここを外すと 41 個のアイコンを毎フレーム作り直すことになる。
        if (abs(stretch - appliedStretch) < STRETCH_EPSILON) return
        appliedStretch = stretch
        markerManager.allEntities().forEach { entity ->
            val marker = entity.marker ?: return@forEach
            val base = baseIconHeight[entity.state.id] ?: return@forEach
            marker.setIconSize(Vec2F(marker.getIconSize().x, base * stretch))
        }
        iconLayer.invalidate()
    }

    /**
     * 傾けたときにアイコンを縦へ引き伸ばす量。
     *
     * ## なぜ要るのか
     *
     * tilt は内側の `MapView` を `rotationX` で回して作っている。視点距離を十分大きく
     * 取った**ほぼ正射影**なので、SDK が描いたものは一律に縦が `cos(傾き)` へ潰れる。
     * 地面に寝ているもの（ポリゴン・ポリライン・地図タイル）はそれで正しいが、
     * **マーカーは常に正面を向いていなければならない**ので潰れては困る。
     *
     * Multiple InfoBubble ページ（tilt = 45）で、マーカーが MapLibre の 0.73 倍
     * （≒ cos 45°）の高さになっているのを実測して分かった。
     *
     * 先に `1 / cos(傾き)` だけ縦へ伸ばしておけば、潰れたあとちょうど元の高さになる。
     * アンカーは割合なので、伸ばしても指す位置は変わらない。
     */
    private fun verticalStretch(): Float {
        val angle =
            abs(holder.mapView.visualTilt).coerceIn(0.0, OpenMobileMapsTiltEmulation.MAX_TILT_DEGREES)
        return (1.0 / cos(Math.toRadians(angle))).toFloat()
    }

    /**
     * アイコンを 1 つ作る。
     *
     * [IconType.INVARIANT] は「ズームでも回転でも見た目の大きさが変わらない」種別で、
     * MapConductor のマーカーの意味論（アイコンは画面上で常に同じ大きさ）に一致する。
     * [IconType.FIXED] にすると地図と一緒に拡大され、他プロバイダと挙動が食い違う。
     *
     * 大きさは `BitmapIcon.size`（すでに端末密度を掛けた物理ピクセル）をそのまま渡す。
     * アンカーは 0..1 の割合で、MapConductor と SDK で意味が同じ。
     */
    private fun createIcon(
        id: String,
        position: GeoPointInterface,
        icon: BitmapIcon,
    ): OpenMobileMapsActualMarker {
        // 新しく作るアイコンも、いま掛かっている引き伸ばしに合わせる。
        val stretch = verticalStretch()
        baseIconHeight[id] = icon.size.height
        return IconFactory.createIconWithAnchor(
            id,
            position.toOmmCoord(),
            textureFor(icon),
            Vec2F(icon.size.width, icon.size.height * stretch),
            IconType.INVARIANT,
            BlendMode.NORMAL,
            Vec2F(icon.anchor.x, icon.anchor.y),
        )
    }

    /**
     * アイコンのテクスチャ。**必ずここを通すこと。**
     *
     * ## `BitmapTextureHolder` は渡した Bitmap を recycle する
     *
     * SDK の [BitmapTextureHolder] はコンストラクタでテクスチャ用の Bitmap へ描き写したあと、
     * **元の Bitmap を `recycle()` する**。一方 MapConductor の
     * [com.mapconductor.core.BitmapIconCache] は同じアイコンに対して同一の [BitmapIcon]
     * （＝同一の Bitmap）を配る。素直に `BitmapTextureHolder(icon.bitmap)` と書くと、
     * 1 個目のマーカーで共有 Bitmap が recycle され、
     * **2 個目で `Canvas: trying to use a recycled bitmap` で落ちる**
     * （マーカーが 1 個のページでは再現しないので、気づくのが遅れやすい）。
     *
     * 複製を渡して共有 Bitmap を守り、できたテクスチャはアイコンごとにキャッシュする。
     * 同じアイコンのマーカーが何万個あってもテクスチャは 1 枚で済む。
     */
    private fun textureFor(icon: BitmapIcon): TextureHolderInterface =
        textureCache.getOrPut(icon.hashCode()) {
            val source = icon.bitmap
            BitmapTextureHolder(source.copy(source.config ?: Bitmap.Config.ARGB_8888, false))
        }
}

// ── ポリライン ────────────────────────────────────────────────────────────

/**
 * ポリラインのレンダラ。
 *
 * 1 本のポリラインが複数の [LineInfoInterface] になり得る（geodesic を密度化してから
 * 子午線で分割するため）。[OpenMobileMapsActualPolyline] がリストなのはそのため。
 */
class OpenMobileMapsPolylineOverlayRenderer(
    override val holder: OpenMobileMapsMapViewHolder,
    val polylineManager: PolylineManagerInterface<OpenMobileMapsActualPolyline>,
    val lineLayer: LineLayerInterface,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolylineOverlayRenderer<OpenMobileMapsActualPolyline>() {
    override suspend fun createPolyline(state: PolylineState): OpenMobileMapsActualPolyline =
        buildPolylineSegments(state.points, state.geodesic)
            .mapIndexed { index, segment ->
                LineFactory.createLine(
                    "polyline-${state.id}-$index",
                    segment.toOmmCoords(),
                    strokeStyle(state.strokeColor, state.strokeWidth.value),
                )
            }

    override suspend fun updatePolylineProperties(
        polyline: OpenMobileMapsActualPolyline,
        current: PolylineEntityInterface<OpenMobileMapsActualPolyline>,
        prev: PolylineEntityInterface<OpenMobileMapsActualPolyline>,
    ): OpenMobileMapsActualPolyline = createPolyline(current.state)

    override suspend fun removePolyline(entity: PolylineEntityInterface<OpenMobileMapsActualPolyline>) = Unit

    override suspend fun onPostProcess() {
        lineLayer.setLines(polylineManager.allEntities().flatMapTo(ArrayList()) { it.polyline.orEmpty() })
    }
}

// ── ポリゴン ──────────────────────────────────────────────────────────────

/**
 * ポリゴンのレンダラ。
 *
 * ## 穴はネイティブに渡せる
 *
 * Open Mobile Maps の [io.openmobilemaps.mapscore.shared.map.coordinates.PolygonCoord] は
 * 穴リングをそのまま持てるので、穴をブリッジで外周に繋ぐ細工は要らない。
 * ただし**穴どうしが重なっている場合は先に結合する**（[unionHoles]）。重なった穴を
 * そのまま渡すと、テッセレータの塗り規則しだいで重なり部分が「穴の穴」として塗り戻される。
 *
 * ## 輪郭線は線レイヤで描く
 *
 * [PolygonLayerInterface] は塗りだけで輪郭を持たない。他プロバイダと見た目を揃えるため、
 * 外周と穴のリングを [LineLayerInterface] にも流す。
 */
class OpenMobileMapsPolygonOverlayRenderer(
    override val holder: OpenMobileMapsMapViewHolder,
    val polygonManager: PolygonManagerInterface<OpenMobileMapsActualPolygon>,
    val fillLayer: PolygonLayerInterface,
    val outlineLayer: LineLayerInterface,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractPolygonOverlayRenderer<OpenMobileMapsActualPolygon>() {
    override suspend fun createPolygon(state: PolygonState): OpenMobileMapsActualPolygon {
        val resolved = if (state.holes.size > 1) state.unionHoles() else state
        val rings = buildPolygonRings(resolved.points, resolved.holes, resolved.geodesic)

        val fills =
            rings.outerRings.mapIndexed { index, outer ->
                PolygonInfo(
                    "polygon-${resolved.id}-$index",
                    polygonCoordOf(
                        // 巻き方向を揃えること。SDK のテッセレータは外周 CCW / 穴 CW を前提に
                        // していて、逆向きのリングは**塗りが丸ごと消える**（例外も警告も出ない）。
                        // 円が塗れてポリゴンが塗れない、という形で最初に出た。
                        ensureCounterClockwise(outer),
                        rings.holeRings.map { ensureClockwiseRing(it) },
                    ),
                    resolved.fillColor.toOmmColor(),
                    resolved.fillColor.toOmmColor(),
                )
            }

        val outlines =
            (rings.outerRings + rings.holeRings).mapIndexed { index, ring ->
                LineFactory.createLine(
                    "polygon-outline-${resolved.id}-$index",
                    closeRing(ring).toOmmCoords(),
                    strokeStyle(resolved.strokeColor, resolved.strokeWidth.value),
                )
            }

        return OpenMobileMapsActualPolygon(fills = fills, outlines = outlines)
    }

    override suspend fun updatePolygonProperties(
        polygon: OpenMobileMapsActualPolygon,
        current: PolygonEntityInterface<OpenMobileMapsActualPolygon>,
        prev: PolygonEntityInterface<OpenMobileMapsActualPolygon>,
    ): OpenMobileMapsActualPolygon = createPolygon(current.state)

    override suspend fun removePolygon(entity: PolygonEntityInterface<OpenMobileMapsActualPolygon>) = Unit

    override suspend fun onPostProcess() {
        val entities = polygonManager.allEntities()
        // add / addAll ではなく setPolygons を使うこと。4.0 の PolygonLayer は
        // 原点（第 2 引数）を持つようになっていて、setPolygons を一度も通っていない
        // レイヤに add すると**何も描かれない**（例外も警告も出ない）。
        fillLayer.setPolygons(entities.flatMapTo(ArrayList()) { it.polygon.fills }, RENDER_ORIGIN)
        outlineLayer.setLines(entities.flatMapTo(ArrayList()) { it.polygon.outlines })
    }
}

// ── 円 ────────────────────────────────────────────────────────────────────

/**
 * 円のレンダラ。SDK に円の描画が無いので、コア共通の [circleToRing] でリングに直して
 * ポリゴンとして描く（他プロバイダと同じ分割数・同じ測地線の扱いになる）。
 */
class OpenMobileMapsCircleOverlayRenderer(
    override val holder: OpenMobileMapsMapViewHolder,
    val circleManager: CircleManagerInterface<OpenMobileMapsActualCircle>,
    val fillLayer: PolygonLayerInterface,
    val outlineLayer: LineLayerInterface,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractCircleOverlayRenderer<OpenMobileMapsActualCircle>() {
    override suspend fun createCircle(state: CircleState): OpenMobileMapsActualCircle? {
        val ring = circleToRing(state.center, state.radiusMeters, state.geodesic)
        if (ring.size < 3) return null
        return OpenMobileMapsActualCircle(
            fills =
                listOf(
                    PolygonInfo(
                        "circle-${state.id}",
                        polygonCoordOf(ensureCounterClockwise(ring), emptyList()),
                        state.fillColor.toOmmColor(),
                        state.fillColor.toOmmColor(),
                    ),
                ),
            outlines =
                listOf(
                    LineFactory.createLine(
                        "circle-outline-${state.id}",
                        closeRing(ring).toOmmCoords(),
                        strokeStyle(state.strokeColor, state.strokeWidth.value),
                    ),
                ),
        )
    }

    override suspend fun updateCircleProperties(
        circle: OpenMobileMapsActualCircle,
        current: CircleEntityInterface<OpenMobileMapsActualCircle>,
        prev: CircleEntityInterface<OpenMobileMapsActualCircle>,
    ): OpenMobileMapsActualCircle? = createCircle(current.state)

    override suspend fun removeCircle(entity: CircleEntityInterface<OpenMobileMapsActualCircle>) = Unit

    override suspend fun onPostProcess() {
        val entities = circleManager.allEntities()
        fillLayer.setPolygons(entities.flatMapTo(ArrayList()) { it.circle.fills }, RENDER_ORIGIN)
        outlineLayer.setLines(entities.flatMapTo(ArrayList()) { it.circle.outlines })
    }
}

// ── グラウンドイメージ ────────────────────────────────────────────────────

/**
 * グラウンドイメージのレンダラ。
 *
 * SDK の「テクスチャ付きポリゴンレイヤ」を 1 枚 1 画像として使う。タイル分割
 * （`GroundImageTileProvider`）は要らない。
 */
class OpenMobileMapsGroundImageOverlayRenderer(
    override val holder: OpenMobileMapsMapViewHolder,
    private val layers: OpenMobileMapsLayers,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractGroundImageOverlayRenderer<OpenMobileMapsActualGroundImage>() {
    override suspend fun createGroundImage(state: GroundImageState): OpenMobileMapsActualGroundImage? {
        val southWest = state.bounds.southWest ?: return null
        val northEast = state.bounds.northEast ?: return null

        val layer = TexturedPolygonLayerInterface.create()
        val corners =
            listOf(
                GeoPoint.fromLatLong(northEast.latitude, southWest.longitude),
                GeoPoint.fromLatLong(northEast.latitude, northEast.longitude),
                GeoPoint.fromLatLong(southWest.latitude, northEast.longitude),
                GeoPoint.fromLatLong(southWest.latitude, southWest.longitude),
            )
        layer.setPolygon(
            polygonCoordOf(corners, emptyList()),
            RectCoord(
                Coord(
                    CoordinateSystemIdentifiers.EPSG4326(),
                    southWest.longitude,
                    northEast.latitude,
                    0.0,
                ),
                Coord(
                    CoordinateSystemIdentifiers.EPSG4326(),
                    northEast.longitude,
                    southWest.latitude,
                    0.0,
                ),
            ),
        )
        layer.loadTexture(BitmapTextureHolder(state.image))
        layer.setAlpha(state.opacity)

        return OpenMobileMapsActualGroundImage(
            layer = layer,
            layerInterface = layers.insertBelowOverlays(holder.map, layer.asLayerInterface()),
        )
    }

    override suspend fun updateGroundImageProperties(
        groundImage: OpenMobileMapsActualGroundImage,
        current: GroundImageEntityInterface<OpenMobileMapsActualGroundImage>,
        prev: GroundImageEntityInterface<OpenMobileMapsActualGroundImage>,
    ): OpenMobileMapsActualGroundImage? {
        layers.remove(holder.map, groundImage.layerInterface)
        return createGroundImage(current.state)
    }

    override suspend fun removeGroundImage(entity: GroundImageEntityInterface<OpenMobileMapsActualGroundImage>) {
        layers.remove(holder.map, entity.groundImage.layerInterface)
    }

    override suspend fun onPostProcess() = Unit
}

// ── ラスターレイヤ ────────────────────────────────────────────────────────

/**
 * ラスターレイヤのレンダラ。
 *
 * マーカーのタイル描画（`MarkerTileRenderer` + ローカルタイルサーバ）もこの経路を通る。
 * つまり **ここが動かないと PostOffice のような大量マーカーのページが白紙になる**。
 */
class OpenMobileMapsRasterLayerOverlayRenderer(
    val holder: OpenMobileMapsMapViewHolder,
    private val layers: OpenMobileMapsLayers,
    private val loaders: ArrayList<LoaderInterface>,
    private val density: Float,
    override val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : RasterLayerOverlayRendererInterface<OpenMobileMapsActualRasterLayer> {
    override suspend fun onAdd(
        data: List<RasterLayerOverlayRendererInterface.AddParamsInterface>,
    ): List<OpenMobileMapsActualRasterLayer?> = data.map { createLayer(it.state) }

    override suspend fun onChange(
        data: List<RasterLayerOverlayRendererInterface.ChangeParamsInterface<OpenMobileMapsActualRasterLayer>>,
    ): List<OpenMobileMapsActualRasterLayer?> =
        data.map { params ->
            layers.remove(
                holder.map,
                params.current.layer.layer
                    .asLayerInterface(),
            )
            createLayer(params.current.state)
        }

    override suspend fun onRemove(data: List<RasterLayerEntityInterface<OpenMobileMapsActualRasterLayer>>) {
        data.forEach { entity ->
            layers.remove(holder.map, entity.layer.layerInterface)
        }
    }

    override suspend fun onCameraChanged(mapCameraPosition: MapCameraPosition) = Unit

    override suspend fun onPostProcess() = Unit

    private fun createLayer(state: RasterLayerState): OpenMobileMapsActualRasterLayer? {
        if (!state.visible) return null
        val template = state.source as? RasterLayerSource.UrlTemplate ?: return null

        val config =
            WebMercatorTileLayerConfig(
                layerName = state.id,
                urlTemplate = template.template,
                tileSize = template.tileSize,
                density = density,
                minZoomLevel = template.minZoom ?: 0,
                maxZoomLevel = template.maxZoom ?: 22,
                scheme = template.scheme,
            )
        val layer = Tiled2dMapRasterLayerInterface.create(config, loaders)
        layer.setAlpha(state.opacity)
        return OpenMobileMapsActualRasterLayer(
            layer = layer,
            layerInterface = layers.insertBelowOverlays(holder.map, layer.asLayerInterface()),
        )
    }
}

// ── コントローラ（どれも 1 行） ────────────────────────────────────────────

class OpenMobileMapsPolylineController(
    renderer: OpenMobileMapsPolylineOverlayRenderer,
) : PolylineController<OpenMobileMapsActualPolyline>(renderer.polylineManager, renderer)

class OpenMobileMapsPolygonController(
    renderer: OpenMobileMapsPolygonOverlayRenderer,
) : PolygonController<OpenMobileMapsActualPolygon>(renderer.polygonManager, renderer)

class OpenMobileMapsCircleController(
    renderer: OpenMobileMapsCircleOverlayRenderer,
) : CircleController<OpenMobileMapsActualCircle>(renderer.circleManager, renderer)

class OpenMobileMapsGroundImageController(
    renderer: OpenMobileMapsGroundImageOverlayRenderer,
) : GroundImageController<OpenMobileMapsActualGroundImage>(GroundImageManager(), renderer)

class OpenMobileMapsRasterLayerController(
    renderer: OpenMobileMapsRasterLayerOverlayRenderer,
    manager: RasterLayerManagerInterface<OpenMobileMapsActualRasterLayer> = RasterLayerManager(),
) : RasterLayerController<OpenMobileMapsActualRasterLayer>(manager, renderer)

// ── 描画の共通値 ──────────────────────────────────────────────────────────

/**
 * ポリゴンの座標の原点。
 *
 * 4.0 の [PolygonLayerInterface.setPolygons] は原点を要求する（3D 表示で精度を保つため）。
 * 平面の地図では 0 でよい。
 */
private val RENDER_ORIGIN = Vec3D(0.0, 0.0, 0.0)

/** これ未満の引き伸ばしの差ではアイコンを作り直さない。 */
private const val STRETCH_EPSILON = 0.002f

// ── 線の見た目 ────────────────────────────────────────────────────────────

/**
 * 統一の線スタイル。
 *
 * 幅は dp をピクセルへ直して [SizeType.SCREEN_PIXEL] で渡す。[SizeType.MAP_UNIT] に
 * すると地図と一緒に太さが変わり、他プロバイダと見た目が食い違う。
 */
private fun strokeStyle(
    color: androidx.compose.ui.graphics.Color,
    widthDp: Float,
): LineStyle {
    val colors = ColorStateList(color.toOmmColor(), color.toOmmColor())
    return LineStyle(
        colors,
        ColorStateList(transparentOmmColor(), transparentOmmColor()),
        color.alpha,
        0f,
        SizeType.SCREEN_PIXEL,
        widthDp * ResourceProvider.getDensity(),
        ArrayList(),
        0f,
        0f,
        LineCapType.ROUND,
        LineJoinType.ROUND,
        0f,
        false,
        1f,
    )
}
