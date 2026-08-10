package com.mapconductor.template

import androidx.compose.ui.geometry.Size
import com.mapconductor.core.controller.BaseMapViewController
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCapability
import com.mapconductor.core.map.MapCapabilityStatus
import com.mapconductor.core.map.MutableMapServiceRegistry
import com.mapconductor.core.map.VisibleRegion
import com.mapconductor.core.map.buildVisibleRegion
import com.mapconductor.core.marker.DefaultMarkerEventController
import com.mapconductor.core.marker.dispatchGeoMarkerClick
import com.mapconductor.core.zoom.WebMercatorZoomAltitudeConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ドライバーのコントローラ。**これが実装点のほぼ全部**である。
 *
 * ## 書くもの
 *
 *  1. [holder]                地図とビューを保持する
 *  2. [readNativeCamera]      SDK の生カメラを [MapCameraPosition] へ直す
 *  3. [moveCamera] / [animateCamera] / [fitBounds]
 *  4. [dispatchMarkerTap]     地図クリックの座標からマーカーを引く（1 行）
 *  5. SDK のイベントをコアの受け口へ転送する（[installListeners]、各 1 行）
 *  6. capability の宣言（[declareCapabilities]）
 *
 * ## 書かなくてよいもの（コアが持っている）
 *
 *  - クリックのカスケード（marker → circle → groundImage → polyline → polygon → map）
 *  - オーバーレイの当たり判定、`clickable = false` の透過
 *  - `compositionXxx` / `updateXxx` / `hasXxx`（Capable ファサード。登録さえすれば既定が働く）
 *  - マーカーのドラッグの状態保持、リスナーの転送
 *  - VisibleRegion の組み立て
 *  - ズームの往復換算
 */
class TemplateMapViewController(
    override val holder: TemplateMapViewHolder,
    internal val markerController: TemplateMarkerController,
    internal val polylineController: TemplatePolylineController,
    internal val polygonController: TemplatePolygonController,
    internal val circleController: TemplateCircleController,
    internal val groundImageController: TemplateGroundImageController,
    internal val rasterLayerController: TemplateRasterLayerController,
    override val mainCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    override val defaultCoroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : BaseMapViewController() {
    internal val markerEventControllers =
        mutableListOf<DefaultMarkerEventController<TemplateActualMarker>>()

    init {
        // ★ ここを忘れると Capable ファサードもクリックカスケードも黙って効かなくなる。
        //   「登録したのに反応しない」のほとんどはこの登録漏れである。
        registerOverlayController(markerController)
        registerOverlayController(polylineController)
        registerOverlayController(polygonController)
        registerOverlayController(circleController)
        registerOverlayController(groundImageController)
        registerOverlayController(rasterLayerController)

        markerEventControllers += DefaultMarkerEventController(markerController)
    }

    /**
     * マーカーのヒットテスト。クリックカスケードの先頭。
     *
     * 地図クリックの座標からそのまま引ける普通のプロバイダはこの 1 行でよい。
     * ネイティブのマーカークリックリスナーを使わざるを得ない SDK
     * （Google Maps / TomTom）は、ここは既定のままにして
     * [com.mapconductor.core.marker.dispatchNativeMarkerClick] を使う。
     */
    override fun dispatchMarkerTap(position: GeoPointInterface): Boolean =
        markerEventControllers.dispatchGeoMarkerClick(position)

    /** SDK のイベントをコアの受け口へ転送する。各 1 行。 */
    fun installListeners() {
        holder.map.onMapClick = { point -> dispatchTap(point) }
        holder.map.onMapLongClick = { point -> emitMapLongClick(point) }
        holder.map.onCameraMove = { camera -> notifyCamera(camera) }
        notifyMapInitialized()
    }

    private fun notifyCamera(camera: TemplateCamera) {
        mainCoroutine.launch { notifyMapCameraPosition(readNativeCamera(camera)) }
    }

    /**
     * SDK の生カメラを統一カメラへ直す。
     *
     * ズームは統一ズーム（Google 準拠）。SDK の体系が違うなら
     * [com.mapconductor.core.zoom.WebMercatorZoomAltitudeConverter] の
     * `zoomOffset` で吸収する（MapLibre 系は 1.0、TomTom は緯度依存）。
     * 可視領域はコアの [buildVisibleRegion] が 4 隅の逆投影から組み立てる。
     */
    fun readNativeCamera(camera: TemplateCamera = holder.map.camera): MapCameraPosition =
        MapCameraPosition(
            position = camera.position,
            zoom = ZOOM_CONVERTER.toUnifiedZoom(camera.zoom),
            bearing = camera.bearing,
            tilt = camera.tilt,
            visibleRegion = visibleRegion(),
        )

    private fun visibleRegion(): VisibleRegion? {
        val size = holder.map.viewportSize
        if (size == Size.Zero) return null
        return holder.buildVisibleRegion(size)
    }

    override fun moveCamera(position: MapCameraPosition) {
        holder.map.moveCamera(
            TemplateCamera(
                position = GeoPoint.from(position.position),
                zoom = ZOOM_CONVERTER.toNativeZoom(position.zoom),
                bearing = position.bearing,
                tilt = position.tilt,
            ),
        )
    }

    override fun animateCamera(
        position: MapCameraPosition,
        duration: Long,
    ) {
        // 雛形の地図はアニメーションを持たないので即座に移動する。
        // 本物の SDK では duration を渡したアニメーション API を呼ぶこと。
        moveCamera(position)
    }

    override fun fitBounds(
        bounds: GeoRectBounds,
        padding: Int,
    ) {
        val center = bounds.center ?: return
        holder.map.moveCamera(holder.map.camera.copy(position = GeoPoint.from(center)))
    }

    override suspend fun clearOverlays() {
        markerController.clear()
        polylineController.clear()
        polygonController.clear()
        circleController.clear()
        groundImageController.clear()
        rasterLayerController.clear()
    }

    /**
     * このドライバーで何ができて何ができないかを宣言する。
     *
     * **「宣言しない」＝「使えない」ではない**（[MapCapabilityStatus.Unknown]）。
     * コアは Unknown を非対応と断定しないので、書かなければ従来どおり動く。
     * 書く価値があるのは「**できない**と分かっているもの」で、それを宣言しておくと
     * 該当機能が黙って無反応になる代わりに理由つきのログを 1 回出す。
     */
    fun declareCapabilities(registry: MutableMapServiceRegistry) {
        registry.declare(MapCapability.ScreenProjectionSync, MapCapabilityStatus.Supported)
        registry.declare(MapCapability.PolygonHoles, MapCapabilityStatus.Supported)
        registry.declareUnsupported(
            MapCapability.CameraTilt,
            "the template map renders a flat web-mercator surface and has no tilt",
        )
        registry.declareUnsupported(
            MapCapability.CameraRotate,
            "the template map renders a north-up surface and has no bearing",
        )
    }

    companion object {
        /**
         * ズームの往復換算。
         *
         * 雛形の地図は Google と同じ体系なのでオフセット 0。MapLibre / Mapbox /
         * MapTiler はタイルが 512px なので 1.0、TomTom は緯度依存なので
         * `GroundScaleZoomAltitudeConverter` を使う。
         */
        val ZOOM_CONVERTER = WebMercatorZoomAltitudeConverter(zoomOffset = 0.0)
    }
}
