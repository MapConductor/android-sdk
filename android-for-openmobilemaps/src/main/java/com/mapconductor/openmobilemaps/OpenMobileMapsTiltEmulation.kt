package com.mapconductor.openmobilemaps

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import com.mapconductor.openmobilemaps.zoom.ZoomAltitudeConverter
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.tan

/**
 * Open Mobile Maps の 2D カメラ向けの tilt 擬似表現。
 *
 * ## なぜ擬似表現なのか
 *
 * [io.openmobilemaps.mapscore.shared.map.MapCameraInterface] にピッチが無い。
 * [io.openmobilemaps.mapscore.shared.map.MapCamera3dInterface] なら
 * `setPoseCamera` で本当に傾けられるが、それは `setupMap(..., is3D = true)` で
 * **地球儀表示**にしたときだけ手に入るもので、平面地図のまま傾けることはできない。
 *
 * そこで android-for-arcgis の 2D（`ArcGIS2DTiltEmulation`）と**同じ方式・同じ定数**を使う:
 *
 * - 遠近感は [OpenMobileMapsMapSurface] がビューを X 軸まわりに回して作る
 * - カメラ位置の付け替えはここが受け持つ
 *
 * ## tilt の符号
 *
 * - `tilt >= 0`: 指定位置は**ターゲット**（画面中心）。カメラが後方へ下がるだけなので
 *   中心もズームも動かさない。
 * - `tilt < 0`: 指定位置は**カメラ位置**。ターゲットが進行方向（bearing）へ前進する。
 *   前進量とズームオフセットは MapLibre / TomTom / Leaflet / ArcGIS2D と同一の式・同一定数。
 */
internal object OpenMobileMapsTiltEmulation {
    /** MapLibre / TomTom / Leaflet / ArcGIS2D と同一値。プロバイダ間で挙動を揃えるため変えないこと。 */
    private const val TARGET_DISTANCE_SCALE = 1.83
    private const val ZOOM_OFFSET_AT_MAX_TILT = -0.9
    const val MAX_TILT_DEGREES = 60.0

    /**
     * 高度の算出にはプラットフォーム非依存の既定値（Google Maps 較正）を使う。
     * ArcGIS2D と同じ理由で、ここを触るとシフト量が他プロバイダとずれる。
     */
    private val converter = ZoomAltitudeConverter(AbstractZoomAltitudeConverter.DEFAULT_ZOOM0_ALTITUDE)

    /** 論理カメラ → 実際に SDK へ渡す中心・統一ズーム。 */
    fun shiftedCamera(position: MapCameraPosition): Pair<GeoPointInterface, Double> {
        if (position.tilt >= 0) return position.position to position.zoom

        val tiltAbsDeg = abs(position.tilt).coerceIn(0.0, MAX_TILT_DEGREES)
        val zoom = position.zoom + ZOOM_OFFSET_AT_MAX_TILT * (tiltAbsDeg / MAX_TILT_DEGREES)
        val tiltAbsRad = Math.toRadians(tiltAbsDeg)
        val altitude = altitudeFor(position.zoom, position.position.latitude)
        val distanceForward = altitude * cos(tiltAbsRad) * tan(tiltAbsRad) * TARGET_DISTANCE_SCALE
        val target = Spherical.computeOffset(position.position, distanceForward, position.bearing)
        return target to zoom
    }

    /** SDK から読み戻した中心・統一ズームを論理カメラへ戻す。 */
    fun restoreLogicalCamera(
        center: GeoPointInterface,
        zoom: Double,
        bearing: Double,
        logicalTilt: Double,
    ): Pair<GeoPointInterface, Double> {
        val tiltAbsDeg = abs(logicalTilt).coerceIn(0.0, MAX_TILT_DEGREES)
        if (logicalTilt >= 0 || tiltAbsDeg == 0.0) return center to zoom

        val originalZoom = zoom - ZOOM_OFFSET_AT_MAX_TILT * (tiltAbsDeg / MAX_TILT_DEGREES)
        val tiltAbsRad = Math.toRadians(tiltAbsDeg)
        val altitude = altitudeFor(originalZoom, center.latitude)
        val distanceBackward = altitude * cos(tiltAbsRad) * tan(tiltAbsRad) * TARGET_DISTANCE_SCALE
        val originalPosition: GeoPoint = Spherical.computeOffset(center, distanceBackward, bearing + 180.0)
        return originalPosition to originalZoom
    }

    /**
     * 統一ズームでの視距離。
     *
     * [ZoomAltitudeConverter.zoomLevelToAltitude] は SDK の縮尺を受け取る形なので、
     * ここでは統一ズームを一度縮尺へ戻してから渡す（他プロバイダの
     * `converter.zoomLevelToAltitude(zoom, ...)` と同じ意味になる）。
     */
    private fun altitudeFor(
        unifiedZoom: Double,
        latitude: Double,
    ): Double = converter.zoomLevelToAltitude(converter.toNativeZoom(unifiedZoom), latitude, 0.0)
}
