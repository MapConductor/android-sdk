package com.mapconductor.openmobilemaps.zoom

import com.mapconductor.core.zoom.AbstractZoomAltitudeConverter
import kotlin.math.log2
import kotlin.math.pow

/**
 * Open Mobile Maps のズーム ⇄ 統一ズーム（Google 準拠）の変換。
 *
 * ## なぜ [com.mapconductor.core.zoom.WebMercatorZoomAltitudeConverter] を使えないのか
 *
 * 他のほとんどの SDK は「ズーム = 2 の指数」なので、統一ズームとの差は定数の
 * オフセット（MapLibre 系なら 1.0）で吸収できる。**Open Mobile Maps のズームは
 * 縮尺の分母**（1:500'000'000 の 500'000'000 の側）で、指数ではない。したがって
 * オフセットの足し算では変換できず、対数を挟む必要がある。
 *
 * ```
 * unifiedZoom = log2(SCALE_AT_ZOOM_0 / nativeScale)
 * nativeScale = SCALE_AT_ZOOM_0 / 2^unifiedZoom
 * ```
 *
 * ## SCALE_AT_ZOOM_0 の導出（実測値ではない）
 *
 * SDK は縮尺の分母から「地図単位 / 物理ピクセル」をこう作る（`MapCamera2d`）:
 *
 * ```
 * mapUnitsPerPixel = nativeScale * 0.0254 / screenDensityPpi
 * ```
 *
 * 統一ズーム Z での Web メルカトルの地図単位 / 物理ピクセルは
 * `156543.034 / (2^Z * density)`。`screenDensityPpi` に Android の
 * `densityDpi`（= 160 × density）を渡すと density が約分され、
 *
 * ```
 * SCALE_AT_ZOOM_0 = 156543.034 x 160 / 0.0254 = 986'097'220
 * ```
 *
 * が端末密度によらない定数として出る。**[com.mapconductor.openmobilemaps.OpenMobileMapsMapView] が
 * `setupMap` に `densityDpi` を渡していることが前提**で、SDK のサンプルのように
 * `xdpi`（実測の物理 dpi。端末によって densityDpi と数 % ずれる）を渡すと
 * その分だけ縮尺が狂う。
 *
 * ## 高度はこの SDK では使わない
 *
 * [zoomLevelToAltitude] / [altitudeToZoomLevel] はカメラが高度で定義される SDK
 * （HERE / ArcGIS）のためのもの。Open Mobile Maps のカメラはズームなので、
 * 統一ズームを経由した Web メルカトルの参照式をそのまま持たせてある。
 */
class ZoomAltitudeConverter(
    zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE,
) : AbstractZoomAltitudeConverter(zoom0Altitude) {
    /** SDK の縮尺 → 統一ズーム。 */
    fun toUnifiedZoom(nativeScale: Double): Double {
        if (nativeScale <= 0.0) return MIN_ZOOM_LEVEL
        return log2(SCALE_AT_ZOOM_0 / nativeScale).coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }

    /** 統一ズーム → SDK の縮尺。 */
    fun toNativeZoom(unifiedZoom: Double): Double =
        SCALE_AT_ZOOM_0 / ZOOM_FACTOR.pow(unifiedZoom.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL))

    override fun zoomLevelToAltitude(
        zoomLevel: Double,
        latitude: Double,
        tilt: Double,
    ): Double {
        val unifiedZoom = toUnifiedZoom(zoomLevel)
        val distance = (zoom0Altitude * cosLatitude(latitude)) / ZOOM_FACTOR.pow(unifiedZoom)
        return (distance * cosTilt(tilt)).coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
    }

    override fun altitudeToZoomLevel(
        altitude: Double,
        latitude: Double,
        tilt: Double,
    ): Double {
        val distance = altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE) / cosTilt(tilt)
        val unifiedZoom = log2((zoom0Altitude * cosLatitude(latitude)) / distance)
        return toNativeZoom(unifiedZoom)
    }

    private fun cosLatitude(latitudeDeg: Double): Double {
        val clamped = latitudeDeg.coerceIn(-85.0, 85.0)
        return kotlin.math.max(MIN_COS_LAT, kotlin.math.abs(kotlin.math.cos(Math.toRadians(clamped))))
    }

    private fun cosTilt(tiltDeg: Double): Double {
        val clamped = tiltDeg.coerceIn(0.0, 90.0)
        return kotlin.math.max(MIN_COS_TILT, kotlin.math.cos(Math.toRadians(clamped)))
    }

    companion object {
        /**
         * 統一ズーム 0 での SDK の縮尺の分母。上のコメントの導出どおりの計算値。
         *
         * `156543.033928 x 160 / 0.0254`。
         */
        const val SCALE_AT_ZOOM_0: Double = 986_097_222.0
    }
}
