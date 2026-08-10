package com.mapconductor.template

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapViewHolderInterface
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan

/**
 * 雛形が地図SDKの代わりに使う、最小の「地図」。
 *
 * ## これは何か
 *
 * `android-for-template` は**動く**ドライバーである。動かない雛形はすぐ腐るし、
 * 適合テストも書けない。とはいえ本物の地図SDKを持ち込むと API キーや依存で
 * 話が濁るので、Web メルカトルの投影と表示リストだけを持つ最小の地図を用意した。
 *
 * 本物のドライバーを書くときは、このクラスにあたるものが**あなたが使う地図SDK**
 * （`MapLibreMap` / `GoogleMap` / `TomTomMap` …）になる。置き換えるのはここだけで、
 * 周りの [TemplateMapViewHolder] / [TemplateMapViewController] / 各レンダラの
 * **形はそのまま**使える。
 *
 * ## Android に依存しない
 *
 * 投影も表示リストも純粋な Kotlin なので、ドライバー全体が素の JVM テストで動く。
 * これは意図的で、[com.mapconductor.core.conformance.MapDriverConformanceSuite] を
 * CI のユニットテストで回せるようにするためである。
 */
class TemplateMap {
    /** 表示中のカメラ。ズームは統一ズーム（Google 準拠）。 */
    var camera: TemplateCamera = TemplateCamera()
        private set

    /** ビューポートの大きさ（px）。まだ計測前なら [Size.Zero]。 */
    var viewportSize: Size = Size.Zero

    /** 描画対象。レンダラがここへ積み、Compose 側が読んで描く。 */
    val displayList: TemplateDisplayList = TemplateDisplayList()

    /** 地図（オーバーレイ以外）のタップ。 */
    var onMapClick: ((GeoPoint) -> Unit)? = null

    /** 地図の長押し。 */
    var onMapLongClick: ((GeoPoint) -> Unit)? = null

    /** カメラが動いた。 */
    var onCameraMove: ((TemplateCamera) -> Unit)? = null

    /** 表示内容が変わったので描き直してほしい。 */
    var onInvalidate: (() -> Unit)? = null

    fun moveCamera(camera: TemplateCamera) {
        this.camera = camera
        onCameraMove?.invoke(camera)
        invalidate()
    }

    fun invalidate() {
        onInvalidate?.invoke()
    }

    /**
     * 地理座標 → 画面座標。ビューポート未計測なら null。
     *
     * 本物のドライバーではここが SDK の `Projection` にあたる。
     */
    fun project(position: GeoPointInterface): Offset? {
        if (viewportSize == Size.Zero) return null
        val scale = worldScale()
        val centerX = lngToWorldX(camera.position.longitude) * scale
        val centerY = latToWorldY(camera.position.latitude) * scale
        val x = lngToWorldX(position.longitude) * scale - centerX + viewportSize.width / 2.0
        val y = latToWorldY(position.latitude) * scale - centerY + viewportSize.height / 2.0
        return Offset(x.toFloat(), y.toFloat())
    }

    /** 画面座標 → 地理座標。ビューポート未計測なら null。 */
    fun unproject(offset: Offset): GeoPoint? {
        if (viewportSize == Size.Zero) return null
        val scale = worldScale()
        val centerX = lngToWorldX(camera.position.longitude) * scale
        val centerY = latToWorldY(camera.position.latitude) * scale
        val worldX = (offset.x - viewportSize.width / 2.0 + centerX) / scale
        val worldY = (offset.y - viewportSize.height / 2.0 + centerY) / scale
        return GeoPoint.fromLatLong(worldYToLat(worldY), worldXToLng(worldX))
    }

    private fun worldScale(): Double = TILE_SIZE * 2.0.pow(camera.zoom)

    private companion object {
        const val TILE_SIZE = 256.0
        const val MAX_LATITUDE = 85.05112878

        fun lngToWorldX(longitude: Double): Double = (longitude + 180.0) / 360.0

        fun latToWorldY(latitude: Double): Double {
            val clamped = latitude.coerceIn(-MAX_LATITUDE, MAX_LATITUDE)
            val rad = clamped * PI / 180.0
            return (1.0 - ln(tan(rad) + 1.0 / kotlin.math.cos(rad)) / PI) / 2.0
        }

        fun worldXToLng(x: Double): Double = x * 360.0 - 180.0

        fun worldYToLat(y: Double): Double {
            val n = PI - 2.0 * PI * y
            return 180.0 / PI * atan(0.5 * (exp(n) - exp(-n)))
        }

        @Suppress("unused")
        fun unusedSin(v: Double): Double = sin(v)
    }
}

/** 雛形の地図のカメラ。 */
data class TemplateCamera(
    val position: GeoPoint = GeoPoint.fromLatLong(0.0, 0.0),
    val zoom: Double = 2.0,
    val bearing: Double = 0.0,
    val tilt: Double = 0.0,
)

/**
 * 描画対象の入れ物。
 *
 * 本物のドライバーでは、レンダラが SDK のネイティブオブジェクト
 * （`Marker` / `Polygon` …）を作って地図へ追加する。雛形ではその代わりに
 * ここへ積む。**「ActualXxx を作って地図に載せ、id で引けるようにしておく」**
 * という形だけ真似ればよい。
 */
class TemplateDisplayList {
    val markers: MutableMap<String, TemplateActualMarker> = LinkedHashMap()
    val polylines: MutableMap<String, TemplateActualPolyline> = LinkedHashMap()
    val polygons: MutableMap<String, TemplateActualPolygon> = LinkedHashMap()
    val circles: MutableMap<String, TemplateActualCircle> = LinkedHashMap()
    val groundImages: MutableMap<String, TemplateActualGroundImage> = LinkedHashMap()
    val rasterLayers: MutableMap<String, TemplateActualRasterLayer> = LinkedHashMap()

    fun clear() {
        markers.clear()
        polylines.clear()
        polygons.clear()
        circles.clear()
        groundImages.clear()
        rasterLayers.clear()
    }
}

/** 地図に載っているマーカーの実体。本物なら SDK の `Marker`。 */
data class TemplateActualMarker(
    val id: String,
    var position: GeoPointInterface,
    var visible: Boolean = true,
)

data class TemplateActualPolyline(
    val id: String,
    var points: List<GeoPointInterface>,
)

data class TemplateActualPolygon(
    val id: String,
    var points: List<GeoPointInterface>,
)

data class TemplateActualCircle(
    val id: String,
    var center: GeoPointInterface,
    var radiusMeters: Double,
)

data class TemplateActualGroundImage(
    val id: String,
)

data class TemplateActualRasterLayer(
    val id: String,
)

/**
 * 投影の**唯一の注入点**。
 *
 * ドライバーが実装するのはここだけで、コアの機能（InfoBubble、マーカーアニメーション、
 * タイル方式マーカーのヒットテスト、VisibleRegion）はすべてこれを通る。
 *
 * ## 同期変換を持てないときは宣言すること
 *
 * [fromScreenOffsetSync] の既定は null。WebView ブリッジ越しなど同期変換を
 * 持てない SDK はここを実装できないが、**黙って null を返してはいけない**。
 * [com.mapconductor.core.map.MapCapability.ScreenProjectionSync] を
 * `Unsupported` として宣言すると、同期投影を要求する機能が理由つきで 1 回だけ
 * 警告を出して落ちる（黙って無反応にならない）。
 *
 * 逆に「ホルダーには同期変換が無いが、別経路でオーバーレイは配置できている」なら
 * `Degraded` にすること。`Unsupported` にすると**動いている機能が止まる**。
 */
class TemplateMapViewHolder(
    override val mapView: TemplateMapSurface,
    override val map: TemplateMap,
) : MapViewHolderInterface<TemplateMapSurface, TemplateMap> {
    override fun toScreenOffset(position: GeoPointInterface): Offset? = map.project(position)

    override fun fromScreenOffsetSync(offset: Offset): GeoPoint? = map.unproject(offset)
}

/**
 * 「地図のビュー」にあたるもの。
 *
 * 本物のドライバーでは SDK の `MapView`（`android.view.View` のサブクラス）になる。
 * コアは [com.mapconductor.core.map.viewportSizePx] で `View` なら大きさを取れるが、
 * ビューでない描画面のプロバイダはビューポートの大きさを自分で持つ。雛形は後者。
 */
class TemplateMapSurface(
    val map: TemplateMap,
)
