package com.mapconductor.openmobilemaps

import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCapabilityStatus
import com.mapconductor.core.map.MapUISettings
import com.mapconductor.core.map.MutableMapServiceRegistry
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.map.buildVisibleRegion
import com.mapconductor.core.map.viewportSizePx
import com.mapconductor.core.marker.DefaultMarkerEventController
import com.mapconductor.core.marker.MarkerEventControllerInterface
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.core.marker.MarkerRenderingStrategyInterface
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.marker.StrategyMarkerController
import com.mapconductor.core.marker.dispatchGeoMarkerClick
import com.mapconductor.openmobilemaps.marker.OpenMobileMapsMarkerController
import com.mapconductor.openmobilemaps.tile.WebMercatorTileLayerConfig
import com.mapconductor.openmobilemaps.zoom.ZoomAltitudeConverter
import io.openmobilemaps.mapscore.shared.graphics.common.Vec3D
import io.openmobilemaps.mapscore.shared.map.camera.MapCameraListenerInterface
import io.openmobilemaps.mapscore.shared.map.coordinates.Coord
import io.openmobilemaps.mapscore.shared.map.coordinates.CoordinateSystemIdentifiers
import io.openmobilemaps.mapscore.shared.map.coordinates.RectCoord
import io.openmobilemaps.mapscore.shared.map.layers.icon.IconLayerInterface
import io.openmobilemaps.mapscore.shared.map.layers.tiled.raster.Tiled2dMapRasterLayerInterface
import io.openmobilemaps.mapscore.shared.map.loader.LoaderInterface
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import android.os.SystemClock
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias OpenMobileMapsDesignTypeChangeHandler = (OpenMobileMapsMapDesignTypeInterface) -> Unit

/**
 * ドライバーのコントローラ。**これが実装点のほぼ全部**である。
 *
 * ## 書くもの
 *
 *  1. [holder]                    地図とビューを保持する
 *  2. [readNativeCamera]          SDK の生カメラを [MapCameraPosition] へ直す
 *  3. [moveCamera] / [animateCamera] / [fitBounds]
 *  4. [dispatchMarkerTap]         地図クリックの座標からマーカーを引く（1 行）
 *  5. [installListeners]          SDK のイベントをコアの受け口へ転送する
 *  6. [declareCapabilities]       できること・できないことの宣言
 *
 * ## 書かなくてよいもの（コアが持っている）
 *
 *  - クリックのカスケード（marker → circle → groundImage → polyline → polygon → map）
 *  - オーバーレイの当たり判定、`clickable = false` の透過
 *  - `compositionXxx` / `updateXxx` / `hasXxx`（Capable ファサード）
 *  - マーカーのドラッグの状態保持、リスナーの転送
 *  - VisibleRegion の組み立て
 */
class OpenMobileMapsMapViewController(
    override val holder: OpenMobileMapsMapViewHolder,
    internal val layers: OpenMobileMapsLayers,
    internal val markerController: OpenMobileMapsMarkerController,
    internal val polylineController: OpenMobileMapsPolylineController,
    internal val polygonController: OpenMobileMapsPolygonController,
    internal val circleController: OpenMobileMapsCircleController,
    internal val groundImageController: OpenMobileMapsGroundImageController,
    internal val rasterLayerController: OpenMobileMapsRasterLayerController,
    internal val loaders: ArrayList<LoaderInterface>,
    internal val density: Float,
    override val mainCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    override val defaultCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController() {
    internal val markerEventControllers =
        mutableListOf<DefaultMarkerEventController<OpenMobileMapsActualMarker>>()
    internal var activeDragController: DefaultMarkerEventController<OpenMobileMapsActualMarker>? = null
    internal var dragTouchInterceptor: View.OnTouchListener? = null

    internal var markerClickListener: OnMarkerEventHandler? = null
    internal var markerDragStartListener: OnMarkerEventHandler? = null
    internal var markerDragListener: OnMarkerEventHandler? = null
    internal var markerDragEndListener: OnMarkerEventHandler? = null
    internal var markerAnimateStartListener: OnMarkerEventHandler? = null
    internal var markerAnimateEndListener: OnMarkerEventHandler? = null

    /**
     * アプリが要求した tilt。
     *
     * SDK の 2D カメラは tilt を持たないので**読み戻せない**。要求値をここで覚えておいて
     * [readNativeCamera] に載せる（そうしないと `moveCamera(tilt = 45)` の直後に
     * カメライベントが tilt = 0 で上書きし、Tilt ページが 1 フレームで元に戻る）。
     */
    private var logicalTilt: Double = 0.0

    /** 直近に適用した tilt。アイコンの引き伸ばしを毎フレーム付け直さないための番人。 */
    private var lastAppliedTilt: Double = 0.0

    /** カメラ通知の間引き。GL スレッドからも触るので [AtomicBoolean]。詳細は [notifyCamera]。 */
    private val cameraNotifyPending = AtomicBoolean(false)
    private var lastNotifiedCamera: MapCameraPosition? = null

    /**
     * 走っているカメラアニメーション。詳細は [animateCamera]。
     *
     * [cancelCameraAnimation] はタッチハンドラのスレッドからも呼ばれるので `@Volatile`。
     */
    @Volatile
    private var cameraAnimationJob: Job? = null

    private var mapDesignTypeChangeListener: OpenMobileMapsDesignTypeChangeHandler? = null
    private var currentDesign: OpenMobileMapsMapDesignTypeInterface? = null

    private val cameraListener =
        object : MapCameraListenerInterface() {
            override fun onVisibleBoundsChanged(
                visibleBounds: RectCoord,
                zoom: Double,
            ) = notifyCamera()

            override fun onRotationChanged(angle: Float) = notifyCamera()

            override fun onMapInteraction() = Unit

            override fun onCameraChange(
                viewMatrix: ArrayList<Float>,
                projectionMatrix: ArrayList<Float>,
                origin: Vec3D,
                verticalFov: Float,
                horizontalFov: Float,
                width: Float,
                height: Float,
                focusPointAltitude: Float,
                focusPointPosition: Coord,
                zoom: Float,
            ) = Unit
        }

    init {
        // ★ ここを忘れると Capable ファサードもクリックカスケードも黙って効かなくなる。
        //   「登録したのに反応しない」のほとんどはこの登録漏れである。
        registerOverlayController(markerController)
        registerOverlayController(polylineController)
        registerOverlayController(polygonController)
        registerOverlayController(circleController)
        registerOverlayController(groundImageController)
        registerOverlayController(rasterLayerController)

        registerMarkerEventController(DefaultMarkerEventController(markerController))

        // タイル方式マーカーはラスターレイヤとして地図へ載る。この配線が無いと
        // PostOffice のような大量マーカーのページが白紙になる。
        markerController.setRasterLayerCallback { state ->
            if (state != null) {
                rasterLayerController.upsert(state)
            } else {
                rasterLayerController.rasterLayerManager
                    .allEntities()
                    .filter { it.state.id.startsWith("marker-tile-") }
                    .forEach { rasterLayerController.removeById(it.state.id) }
            }
        }
    }

    /** SDK のイベントをコアの受け口へ転送する。 */
    fun installListeners() {
        holder.map.getTouchHandler().insertListener(OpenMobileMapsTouchListener(this), 0)
        holder.map.getCamera().addListener(cameraListener)
        notifyMapInitialized()
    }

    /**
     * カメラの通知。**必ず 1 フレームに 1 回へ間引くこと。**
     *
     * この SDK は [MapCameraListenerInterface.onVisibleBoundsChanged] を描画フレームごとに、
     * しかも同じ値で何度も呼ぶ（実測: 1 秒のカメラアニメーションで **388 回**、うち大半は
     * まったく同じ値）。1 回ごとに [readNativeCamera] を回すと、可視領域の 4 隅の逆投影
     * （JNI）がメインスレッドで 1 秒に 1,500 回以上走る。
     *
     * その結果**他の地図の動きが止まる**。CameraSync で Google Maps と並べたとき、
     * Google 側のカメラアニメーションが途中で固まる形で表面化した
     * （Google のアニメーションもメインスレッドで進むため）。
     * 他プロバイダの通知は毎秒 60 回程度なので、ここだけが突出していた。
     *
     * 対策は 2 段:
     *  1. 未処理の通知があるあいだは新しい通知を積まない（フレームごとに 1 回へ畳む）
     *  2. 前回とまったく同じカメラなら配らない
     */
    private fun notifyCamera() {
        if (!cameraNotifyPending.compareAndSet(false, true)) return
        mainCoroutine.launch {
            cameraNotifyPending.set(false)
            val position = readNativeCamera()
            if (isSameCamera(lastNotifiedCamera, position)) return@launch
            lastNotifiedCamera = position
            defaultCoroutine.launch { notifyMapCameraPosition(position) }
            cameraMoveCallback?.invoke(position)
        }
    }

    private fun isSameCamera(
        previous: MapCameraPosition?,
        current: MapCameraPosition,
    ): Boolean {
        val last = previous ?: return false
        return last.position.latitude == current.position.latitude &&
            last.position.longitude == current.position.longitude &&
            last.zoom == current.zoom &&
            last.bearing == current.bearing &&
            last.tilt == current.tilt
    }

    /**
     * マーカーのヒットテスト。クリックカスケードの先頭。
     *
     * 地図クリックの座標からそのまま引ける普通のプロバイダなのでこの 1 行でよい。
     */
    override fun dispatchMarkerTap(position: GeoPointInterface): Boolean =
        markerEventControllers.dispatchGeoMarkerClick(position)

    /**
     * SDK の生カメラを統一カメラへ直す。
     *
     * tilt < 0 のときは SDK に渡した中心・ズームが前進済みなので、
     * [OpenMobileMapsTiltEmulation.restoreLogicalCamera] で論理値へ巻き戻す。
     */
    fun readNativeCamera(): MapCameraPosition = readLogicalCamera().copy(visibleRegion = visibleRegion())

    /**
     * 可視領域を載せない軽い読み取り。
     *
     * [visibleRegion] は 4 隅の逆投影（JNI 4 回）なので、アニメーションの開始点を
     * 取るためだけに回したくない。
     */
    private fun readLogicalCamera(): MapCameraPosition {
        val camera = holder.map.getCamera()
        val rawCenter = holder.toWgs84(camera.getCenterPosition())?.toGeoPoint() ?: GeoPoint.fromLatLong(0.0, 0.0)
        val rawZoom = ZOOM_CONVERTER.toUnifiedZoom(camera.getZoom())
        val bearing = bearingFromNativeRotation(camera.getRotation())
        val (center, zoom) =
            OpenMobileMapsTiltEmulation.restoreLogicalCamera(rawCenter, rawZoom, bearing, logicalTilt)

        return MapCameraPosition(position = center, zoom = zoom, bearing = bearing, tilt = logicalTilt)
    }

    private fun visibleRegion(): VisibleRegion? = holder.buildVisibleRegion()

    override fun moveCamera(position: MapCameraPosition) {
        cancelCameraAnimation()
        applyCamera(position)
    }

    /**
     * カメラを [duration] ミリ秒かけて動かす。
     *
     * ## SDK のアニメーションは使わない
     *
     * `moveToCenterPositionZoom(..., animated = true)` は**尺を指定できず、実測で常に
     * 約 300ms** で着地する。アプリが 1000ms と言っても 300ms で終わるので、
     * 他プロバイダと並べると明らかに先に着いてしまう。フレームを刻んで
     * `animated = false` の移動を繰り返し、こちらで尺を守る。
     *
     * 補間の中身（メルカトル空間での線形補間・方位の最短回り・イージング）は
     * [OpenMobileMapsCameraAnimation] にある。
     */
    override fun animateCamera(
        position: MapCameraPosition,
        duration: Long,
    ) {
        cancelCameraAnimation()
        if (duration <= 0L) {
            applyCamera(position)
            return
        }

        val from = readLogicalCamera()
        cameraAnimationJob =
            mainCoroutine.launch {
                val startedAt = SystemClock.uptimeMillis()
                var frame = 0L
                while (true) {
                    val elapsed = SystemClock.uptimeMillis() - startedAt
                    val progress = (elapsed.toDouble() / duration).coerceIn(0.0, 1.0)
                    if (progress >= 1.0) break
                    applyCamera(
                        OpenMobileMapsCameraAnimation.interpolate(
                            from,
                            position,
                            OpenMobileMapsCameraAnimation.ease(progress),
                        ),
                    )
                    // 「16ms 待つ」ではなく「次のフレーム時刻まで待つ」。1 フレームぶんの
                    // 仕事（投影と通知）に 10ms 使ったあと固定で 16ms 待つと 27ms 周期＝37fps
                    // まで落ちる（実測）。境界に合わせれば 60fps 近くを保てる。
                    frame += 1
                    val nextFrameAt = startedAt + frame * FRAME_INTERVAL_MS
                    delay((nextFrameAt - SystemClock.uptimeMillis()).coerceAtLeast(0L))
                }
                // 補間の誤差を残さないよう、最後は要求された値そのものを入れる。
                applyCamera(position)
                cameraAnimationJob = null
            }
    }

    /**
     * 走っているカメラアニメーションを止める。
     *
     * 新しいカメラ指示のたびに呼ぶ。**指が触れたときも呼ぶこと**
     * （[OpenMobileMapsTouchListener] から）。止めないとアニメーションが
     * ユーザーの操作と綱引きになり、地図が引き戻される。
     */
    internal fun cancelCameraAnimation() {
        cameraAnimationJob?.cancel()
        cameraAnimationJob = null
    }

    private fun applyCamera(position: MapCameraPosition) {
        logicalTilt = position.tilt
        holder.mapView.visualTilt = position.tilt
        if (position.tilt != lastAppliedTilt) {
            lastAppliedTilt = position.tilt
            // 傾きが変わったらアイコンの縦の引き伸ばしを付け直す。
            // 詳細は OpenMobileMapsMarkerOverlayRenderer.verticalStretch。
            markerEventControllers
                .mapNotNull { it.renderer as? OpenMobileMapsMarkerOverlayRenderer }
                .forEach { it.onVisualTiltChanged() }
        }

        val (center, zoom) = OpenMobileMapsTiltEmulation.shiftedCamera(position)
        val camera = holder.map.getCamera()
        camera.moveToCenterPositionZoom(center.toOmmCoord(), ZOOM_CONVERTER.toNativeZoom(zoom), false)
        // 方位が変わっていないなら触らない。毎フレーム呼ぶと SDK 側で無駄な
        // 行列の作り直しが走る。
        val rotation = nativeRotationFromBearing(position.bearing)
        if (abs(camera.getRotation() - rotation) > ROTATION_EPSILON) {
            camera.setRotation(rotation, false)
        }
    }

    override fun fitBounds(
        bounds: GeoRectBounds,
        padding: Int,
    ) {
        val southWest = bounds.southWest ?: return
        val northEast = bounds.northEast ?: return
        val viewportWidth = holder.viewportSizePx()?.width ?: return
        // SDK は余白を「ビューポートに対する割合」で受け取る。
        val paddingPc = if (viewportWidth > 0f) (padding / viewportWidth).coerceIn(0f, 0.4f) else 0f

        holder.map.getCamera().moveToBoundingBox(
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
            paddingPc,
            false,
            null,
            null,
        )
    }

    override fun applyUISettings(settings: MapUISettings) {
        holder.map.getCamera().setRotationEnabled(settings.rotateGesture)
    }

    override suspend fun clearOverlays() {
        markerController.clear()
        polylineController.clear()
        polygonController.clear()
        circleController.clear()
        groundImageController.clear()
        rasterLayerController.clear()
        layers.clearAll()
    }

    // ── 地図デザイン ────────────────────────────────────────────────────

    fun setMapDesignType(value: OpenMobileMapsMapDesignTypeInterface) {
        if (currentDesign?.getValue() == value.getValue()) return
        currentDesign = value
        val config =
            WebMercatorTileLayerConfig(
                layerName = "design-${value.id}",
                urlTemplate = value.tileUrlTemplate,
                tileSize = value.tileSize,
            )
        val layer = Tiled2dMapRasterLayerInterface.create(config, loaders)
        layers.setDesignLayer(holder.map, layer.asLayerInterface())
        mapDesignTypeChangeListener?.invoke(value)
    }

    fun setMapDesignTypeChangeListener(listener: OpenMobileMapsDesignTypeChangeHandler) {
        mapDesignTypeChangeListener = listener
    }

    // ── マーカー（クラスタリング等の追加レンダラ用） ──────────────────────

    fun createMarkerRenderer(
        strategy: MarkerRenderingStrategyInterface<OpenMobileMapsActualMarker>,
    ): MarkerOverlayRendererInterface<OpenMobileMapsActualMarker> {
        val iconLayer = IconLayerInterface.create()
        iconLayer.setLayerClickable(false)
        holder.map.addLayer(iconLayer.asLayerInterface())
        return OpenMobileMapsMarkerOverlayRenderer(
            holder = holder,
            markerManager = strategy.markerManager,
            iconLayer = iconLayer,
        )
    }

    fun createMarkerEventController(
        controller: StrategyMarkerController<OpenMobileMapsActualMarker>,
        renderer: MarkerOverlayRendererInterface<OpenMobileMapsActualMarker>,
    ): MarkerEventControllerInterface<OpenMobileMapsActualMarker> = DefaultMarkerEventController(controller)

    fun registerMarkerEventController(controller: MarkerEventControllerInterface<OpenMobileMapsActualMarker>) {
        val typed = controller as? DefaultMarkerEventController<OpenMobileMapsActualMarker> ?: return
        registerMarkerEventController(typed)
    }

    internal fun registerMarkerEventController(controller: DefaultMarkerEventController<OpenMobileMapsActualMarker>) {
        if (markerEventControllers.contains(controller)) return
        markerEventControllers.add(controller)
        controller.setClickListener(markerClickListener)
        controller.setDragStartListener(markerDragStartListener)
        controller.setDragListener(markerDragListener)
        controller.setDragEndListener(markerDragEndListener)
        controller.setAnimateStartListener(markerAnimateStartListener)
        controller.setAnimateEndListener(markerAnimateEndListener)
    }

    fun sendInitialCameraUpdate() {
        mainCoroutine.launch {
            notifyMapInitialized()
            if (holder.viewportSizePx() == null) return@launch
            val position = readNativeCamera()
            defaultCoroutine.launch { notifyMapCameraPosition(position) }
        }
    }

    // ── 拡張ファイル（Gestures）からの入口 ──────────────────────────────

    internal fun emitCameraMoveEndFromGesture() {
        mainCoroutine.launch { cameraMoveEndCallback?.invoke(readNativeCamera()) }
    }

    override fun destroy() {
        cancelCameraAnimation()
        runCatching { holder.map.getCamera().removeListener(cameraListener) }
        removeDragTouchInterceptor()
        super.destroy()
    }

    /**
     * このドライバーで何ができて何ができないかを宣言する。
     *
     * **「宣言しない」＝「使えない」ではない**（[MapCapabilityStatus.Unknown]）。
     * 書く価値があるのは「**できない**と分かっているもの」で、宣言しておくと該当機能が
     * 黙って無反応になる代わりに理由つきのログを 1 回出る。
     */
    fun declareCapabilities(registry: MutableMapServiceRegistry) = OpenMobileMapsCapabilities.declare(registry)

    companion object {
        /**
         * 方位の符号。**SDK は MapConductor と逆回りである。**
         *
         * MapConductor の bearing は Google 準拠で「カメラが向いている方位を北から時計回りに測る」。
         * SDK の `setRotation` は地図を反時計回りに回す量なので、符号を反転する。
         * 反転を忘れると bearing 270 の地図が 90 として描かれ、**ちょうど 180 度ずれる**
         * （tilt ページを MapLibre と並べて気づいた。単独で見ると「回っている」ので正しく見える）。
         */
        internal fun nativeRotationFromBearing(bearing: Double): Float = (-bearing).toFloat()

        /** SDK の回転角 → MapConductor の bearing（0 以上 360 未満）。 */
        internal fun bearingFromNativeRotation(rotation: Float): Double {
            val bearing = -rotation.toDouble() % 360.0
            return if (bearing < 0) bearing + 360.0 else bearing
        }

        /**
         * ズームの往復換算。
         *
         * この SDK のズームは 2 の指数ではなく**縮尺の分母**なので、
         * [com.mapconductor.core.zoom.WebMercatorZoomAltitudeConverter] の
         * オフセット方式では変換できない（[ZoomAltitudeConverter] のコメント参照）。
         */
        val ZOOM_CONVERTER = ZoomAltitudeConverter()

        /** これ未満の方位差では `setRotation` を呼ばない（度）。 */
        private const val ROTATION_EPSILON = 0.01f

        /** カメラアニメーションの刻み。60fps 相当。 */
        private const val FRAME_INTERVAL_MS = 16L
    }
}
