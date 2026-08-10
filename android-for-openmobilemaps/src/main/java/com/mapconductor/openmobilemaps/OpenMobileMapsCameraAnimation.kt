package com.mapconductor.openmobilemaps

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.tan

/**
 * カメラアニメーションの補間。**SDK のアニメーションは使わない。**
 *
 * ## なぜ自前で補間するのか
 *
 * SDK の `moveToCenterPositionZoom(..., animated = true)` は尺を指定できず、
 * 実測で**常に約 300ms** で着地する。アプリが
 * `moveCameraTo(position, durationMillis = 1000)` と言っても 300ms で終わるため、
 * 他プロバイダと並べると明らかに先に着いてしまう（CameraSync ページで顕著）。
 * 尺を渡す API が SDK に無いので、こちらでフレームを刻んで
 * `animated = false` の移動を繰り返す。
 *
 * ## 補間する空間
 *
 * 中心は**Web メルカトルのメートル空間で線形補間**する。緯度経度を直接線形に
 * 動かすと、緯度が大きく変わる移動で画面上の速度が変わって見える（メルカトルは
 * 高緯度ほど引き伸ばされるため）。Google Maps / MapLibre も同じくワールド座標で補間する。
 *
 * ズームは統一ズーム（対数尺）で線形。これで「毎秒 n 倍」の一定速度になる。
 * 方位は近い方向へ回る（0°→350° は 10° 戻る）。
 *
 * ## 精度
 *
 * コアの [com.mapconductor.core.projection.WebMercator] は `Offset`（Float）を返すので
 * ここでは使えない。メルカトルのメートルは ±2,000 万なので、Float32 では
 * 分解能が約 2m しかなく、高ズームでカメラがカクつく。Double で持つこと。
 */
internal object OpenMobileMapsCameraAnimation {
    /** 経度 180 度ぶんのメルカトル距離（m）。 */
    private const val MAX_EXTENT_METERS = 20_037_508.342_789_244

    /** メルカトルが破綻しない緯度の上限。 */
    private const val MAX_LATITUDE = 85.051_128_78

    /**
     * [from] から [to] へ [t]（0..1）だけ進んだカメラ。
     *
     * [t] は**イージング適用後**の値を渡すこと（ここでは線形に混ぜるだけ）。
     */
    fun interpolate(
        from: MapCameraPosition,
        to: MapCameraPosition,
        t: Double,
    ): MapCameraPosition =
        MapCameraPosition(
            position = interpolatePosition(from.position, to.position, t),
            zoom = from.zoom + (to.zoom - from.zoom) * t,
            bearing = interpolateBearing(from.bearing, to.bearing, t),
            tilt = from.tilt + (to.tilt - from.tilt) * t,
            paddings = to.paddings,
        )

    /**
     * 中心の補間。メルカトルのメートル空間で線形に混ぜる。
     *
     * 経度は**近い方向へ**回る。±180 度を跨ぐ移動で世界を逆回りしないため
     * （東京 → ホノルルが太平洋ではなくユーラシア大陸経由になる、という形で出る）。
     */
    private fun interpolatePosition(
        from: GeoPointInterface,
        to: GeoPointInterface,
        t: Double,
    ): GeoPoint {
        val fromX = longitudeToMercatorX(from.longitude)
        var deltaX = longitudeToMercatorX(to.longitude) - fromX
        if (deltaX > MAX_EXTENT_METERS) deltaX -= 2 * MAX_EXTENT_METERS
        if (deltaX < -MAX_EXTENT_METERS) deltaX += 2 * MAX_EXTENT_METERS

        val fromY = latitudeToMercatorY(from.latitude)
        val y = fromY + (latitudeToMercatorY(to.latitude) - fromY) * t

        return GeoPoint.fromLatLong(
            latitude = mercatorYToLatitude(y),
            longitude = mercatorXToLongitude(fromX + deltaX * t),
        )
    }

    /** 方位の補間。近い方向へ回り、0 以上 360 未満へ正規化して返す。 */
    fun interpolateBearing(
        from: Double,
        to: Double,
        t: Double,
    ): Double {
        var delta = (to - from) % 360.0
        if (delta > 180.0) delta -= 360.0
        if (delta < -180.0) delta += 360.0
        val bearing = (from + delta * t) % 360.0
        return if (bearing < 0) bearing + 360.0 else bearing
    }

    /**
     * イージング。Android 標準の `AccelerateDecelerateInterpolator` と同じ余弦カーブ。
     *
     * Google Maps Android のカメラアニメーションもこの系統なので、
     * 並べたときの見え方が揃う。
     */
    fun ease(t: Double): Double = (1.0 - kotlin.math.cos(t.coerceIn(0.0, 1.0) * PI)) / 2.0

    private fun longitudeToMercatorX(longitude: Double): Double = longitude * MAX_EXTENT_METERS / 180.0

    private fun mercatorXToLongitude(x: Double): Double {
        val longitude = x * 180.0 / MAX_EXTENT_METERS
        val wrapped = ((longitude + 180.0) % 360.0 + 360.0) % 360.0 - 180.0
        return wrapped
    }

    private fun latitudeToMercatorY(latitude: Double): Double {
        val clamped = latitude.coerceIn(-MAX_LATITUDE, MAX_LATITUDE)
        return ln(tan((90.0 + clamped) * PI / 360.0)) * MAX_EXTENT_METERS / PI
    }

    private fun mercatorYToLatitude(y: Double): Double =
        180.0 / PI * (2.0 * atan(exp(y * PI / MAX_EXTENT_METERS)) - PI / 2.0)
}
