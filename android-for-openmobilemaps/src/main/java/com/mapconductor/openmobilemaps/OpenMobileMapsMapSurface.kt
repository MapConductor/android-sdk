package com.mapconductor.openmobilemaps

import io.openmobilemaps.mapscore.map.view.MapView
import kotlin.math.abs
import kotlin.math.max
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout

/**
 * SDK の [MapView] を載せる入れ物。**傾きの見た目だけ**を受け持つ。
 *
 * android-for-arcgis の `WrapMapView` と同じ構造。2D の地図はカメラピッチを持てないため、
 * ビューそのものを X 軸まわりに回して遠近感を作る（react-for-leaflet の CSS `rotateX` と同じ方式）。
 * 地図側の中心・縮尺の付け替えは [OpenMobileMapsTiltEmulation] が受け持ち、ここは描画だけを扱う。
 *
 * 負の tilt は中心の前進で表現されるので、描画角度は常に `abs(tilt)` を使う。
 *
 * ## 投影は傾きを補正しない
 *
 * [OpenMobileMapsMapViewHolder] の投影は内側の [MapView] の座標系で返る。傾けているときは
 * 内側が [PLANE_SCALE] 倍に広がっているため、InfoBubble の位置と可視領域はその分ずれる。
 * android-for-arcgis の 2D も同じ割り切りで、tilt = 0 のときは厳密に一致する。
 */
class OpenMobileMapsMapSurface : FrameLayout {
    lateinit var mapView: MapView

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /** 見た目を傾ける角度（論理 tilt、度）。 */
    var visualTilt: Double = 0.0
        set(value) {
            if (field == value) return
            field = value
            applyVisualTilt()
        }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) applyVisualTilt()
    }

    /**
     * 回した平面が元のフレームを覆うよう [PLANE_SCALE] 倍に広げてから回し、親でクリップする。
     * 拡大しても縮尺は変わらない（縮尺は解像度で決まる）ので、単に地図が広く映る
     * ＝傾いたカメラがより広い地表を見るのと同じになる。
     */
    private fun applyVisualTilt() {
        if (!this::mapView.isInitialized) return
        if (width <= 0 || height <= 0) return

        val angle = abs(visualTilt).coerceIn(0.0, OpenMobileMapsTiltEmulation.MAX_TILT_DEGREES).toFloat()
        val scale = if (angle > 0f) PLANE_SCALE else 1.0f
        val targetWidth = (width * scale).toInt()
        val targetHeight = (height * scale).toInt()

        val params = mapView.layoutParams as LayoutParams
        if (params.width != targetWidth || params.height != targetHeight || params.gravity != Gravity.CENTER) {
            params.width = targetWidth
            params.height = targetHeight
            params.gravity = Gravity.CENTER
            mapView.layoutParams = params
        }

        // 遠近は掛けない（正射影）。react-for-leaflet / react-for-openlayers の CSS も
        // perspective を置いておらず、[PLANE_SCALE] = 1 / cos(60°) がちょうど効く前提。
        mapView.cameraDistance = max(targetWidth, targetHeight) * ORTHOGRAPHIC_DISTANCE_FACTOR
        mapView.rotationX = angle
    }

    private companion object {
        /** 回した平面が元のフレームを覆うための拡大率。ArcGIS2D / leaflet / openlayers と同じ 200%。 */
        const val PLANE_SCALE = 2.0f

        /** 正射影に近づけるための視点距離 ÷ ビューサイズ。大きいほど遠近が弱い。 */
        const val ORTHOGRAPHIC_DISTANCE_FACTOR = 200.0f
    }
}
