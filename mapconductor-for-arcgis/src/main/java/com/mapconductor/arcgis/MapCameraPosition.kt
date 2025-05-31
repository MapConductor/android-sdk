package com.mapconductor.arcgis

import androidx.annotation.Keep
import com.arcgismaps.geometry.Point
import com.arcgismaps.mapping.view.Camera
import com.mapconductor.core.IMapCameraPosition
import com.mapconductor.core.MapPaddings
import com.mapconductor.core.MapPaddingsImpl
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin


interface MapCameraPositionArcGIS: IMapCameraPosition {
    fun toCamera(): Camera
}
@Keep
data class MapCameraPosition @JvmOverloads constructor(
    override val position: IGeoPoint,
    override val zoom: Double = 2.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddings? = MapPaddingsImpl.Zeros,
): MapCameraPositionArcGIS {

    override fun toCamera(): Camera = calculateCameraForOrbitParameters(
        targetPoint = GeoPoint.from(position).toPoint(),
        distance = zoomLevelToAltitude(zoom),
        cameraHeadingOffset = 360 - (bearing + 180),
        cameraPitchOffset = tilt,
    )


//
//    override fun copy(
//        target: GeoPoint?,
//        zoom: Double?,
//        bearing: Double?,
//        tilt: Double?,
//        paddings: MapPaddingsImpl?
//    ) = MapCameraPosition(
//        target = target ?: this.target,
//        zoom = zoom ?: this.zoom,
//        bearing = bearing ?: this.bearing,
//        tilt = tilt ?: this.tilt,
//        paddings = paddings ?: this.paddings,
//    )

    companion object {
        val Default = MapCameraPosition(
            position = GeoPoint.fromLatLong(
                latitude = 0.0,
                longitude = 0.0,
            ),
            zoom = 0.0,
            bearing = 0.0,
            tilt = 0.0,
        )

        fun from(position: IMapCameraPosition): MapCameraPosition {
            return when(position) {
                is MapCameraPosition -> position
                else -> {
//                    val altitude = calculateZoomLevelFromScale(
//                        positionImpl.zoom,
//                        positionImpl.target.latitude,
//                        Resources.getSystem().displayMetrics.densityDpi.toDouble(),
//                    ) * 2.0
                    val altitude = calculateScaleFromZoomLevel(position.zoom)
                    MapCameraPosition(
                        position = GeoPoint.fromLongLat(
                            longitude = position.position.longitude,
                            latitude = position.position.latitude,
                            altitude = altitude,
                        ),
                        zoom = position.zoom,
                        bearing = position.bearing,
                        tilt = position.tilt,
                        paddings = position.paddings,
                    )
                }
            }
        }
    }
}


/**
 * Google Maps の zoomLevel を基にArcGIS 用の scale を計算します。
 *
 * @param zoomLevel Google Maps のズームレベル（0～など）
 * @param latitude 中心とする位置の緯度（度）; デフォルトは赤道上（0.0）
 * @param dpi ディスプレイのdpi（dots per inch）、通常は96
 * @return 計算されたスケール（例: 1:scale の scale 部分）
 */
fun calculateScaleFromZoomLevel(zoomLevel: Double, latitude: Double = 0.0, dpi: Double = 96.0): Double {
    // Google Maps の解像度（メートル/ピクセル）
    val resolution = (156543.03392 * Math.cos(Math.toRadians(latitude))) / (2.0.pow(zoomLevel))
    // 1インチは0.0254メートル。scale = resolution × (dpi / 0.0254)
    return resolution * (dpi / 0.0254)
}
/**
 * ArcGIS 用のscaleからGoogle Mapsの zoomLevel を近似計算します。
 *
 * @param scale ArcGIS の Viewpoint で設定するスケール値（1:scale の scale 部分）
 * @param latitude 位置の緯度（度）。解像度の補正用。デフォルトは赤道上（0.0）
 * @param dpi ディスプレイのdpi。通常は96dpiを使用します。
 * @return 計算された zoomLevel (少数点以下の値)
 */
fun calculateZoomLevelFromScale(scale: Double, latitude: Double = 0.0, dpi: Double = 96.0): Double {
    // 定数部分: 156543.03392 * (dpi/0.0254) は、赤道上でのスケール0の時の値となる
    val constant = 156543.03392 * cos(Math.toRadians(latitude)) * (dpi / 0.0254)
    return log2(constant / scale)
}






// 定数: この値は経験的に調整されることがあります。
// Webメルカトル図法の世界幅（ピクセル単位）と地球の円周（メートル単位）の関係から導出されることが多いです。
// 一般的なタイルサイズ(256px)と赤道半径に基づいた値の一例です。
private const val ALTITUDE_ZOOM_CONSTANT = 591657550.0 // Meters (approximate world circumference related scale)

// Google Mapsの一般的な最小・最大ズームレベル
private const val MIN_ZOOM_LEVEL = 1.0
private const val MAX_ZOOM_LEVEL = 22.0 // Google Mapsは最大22程度までサポートすることが多い

private const val DEFAULT_MAX_Maps_TILT = 60.0 // Google Maps Tilt の想定最大値
private const val ARCGIS_MAX_PITCH = 90.0
private const val MIN_ANGLE = 0.0
private const val EARTH_MEAN_RADIUS_METERS = 6371000.0 // 地球の平均半径 (より正確には楕円体を使うべき)
// --- 角度変換ヘルパー ---
internal fun Double.toRadians(): Double = Math.toRadians(this)
internal fun Double.toDegrees(): Double = Math.toDegrees(this)

// --- 以前の角度変換関数 ---
fun arcgisPitchToGoogleMapsTilt(pitch: Double, maxTilt: Double = DEFAULT_MAX_Maps_TILT): Double {
    return pitch.coerceIn(MIN_ANGLE, maxTilt)
}

fun googleMapsTiltToArcgisPitch(tilt: Double): Double {
    return tilt.coerceIn(MIN_ANGLE, ARCGIS_MAX_PITCH)
}

// --- 以前の Altitude/Zoom 変換関数 (近似) ---
fun altitudeToZoomLevel(altitude: Double): Double {
    if (altitude <= 0) return MIN_ZOOM_LEVEL
    // この式は Tilt/Pitch を考慮していない単純な近似であることに注意
    val zoom = log2(ALTITUDE_ZOOM_CONSTANT) - log2(altitude)
    return zoom.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
}

fun zoomLevelToAltitude(zoomLevel: Double): Double {
    val clampedZoom = zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    // この式は Tilt/Pitch を考慮していない単純な近似であることに注意
    val altitude = ALTITUDE_ZOOM_CONSTANT / (2.0.pow(clampedZoom))
    return max(1.0, altitude) // 高度が0以下にならないように
}

// --- 緯度経度オフセット計算ヘルパー (簡易版 - 球体近似) ---

/**
 * 指定された地点から特定の方位と距離にある地点を計算します (簡易球体モデル)。
 * @param lat 出発点の緯度 (度)
 * @param lon 出発点の経度 (度)
 * @param bearing 方位 (度、時計回り、真北0度)
 * @param distance 距離 (メートル)
 * @return Pair<Double, Double> 新しい地点の緯度と経度 (度)
 */
fun calculateDestinationPoint(lat: Double, lon: Double, bearing: Double, distance: Double): Pair<Double, Double> {
    val latRad = lat.toRadians()
    val lonRad = lon.toRadians()
    val bearingRad = bearing.toRadians()
    val angularDistance = distance / EARTH_MEAN_RADIUS_METERS

    val destLatRad = asin(sin(latRad) * cos(angularDistance) + cos(latRad) * sin(angularDistance) * cos(bearingRad))

    var destLonRad = lonRad + atan2(
        sin(bearingRad) * sin(angularDistance) * cos(latRad),
        cos(angularDistance) - sin(latRad) * sin(destLatRad)
    )
    // 経度を -180 ～ +180 の範囲に正規化 (atan2の結果による)
    destLonRad = (destLonRad + 3 * PI) % (2 * PI) - PI

    return Pair(destLatRad.toDegrees(), destLonRad.toDegrees())
}
/**
 * ArcGISの高度 (メートル単位) を Google Maps のズームレベル (近似値) に変換します。
 *
 * @param altitude カメラの高度 (メートル単位)。正の値である必要があります。
 * @param minZoom 許容される最小ズームレベル。
 * @param maxZoom 許容される最大ズームレベル。
 * @return 近似されたGoogle Mapsのズームレベル。指定された範囲内に制限されます。
 * altitudeが0以下の場合はminZoomを返します。
 */
fun altitudeToZoomLevel(
    altitude: Double,
    minZoom: Double = MIN_ZOOM_LEVEL,
    maxZoom: Double = MAX_ZOOM_LEVEL
): Double {
    if (altitude <= 0) {
        // 高度が無効な場合は最小ズームレベルを返す
        return minZoom
    }

    // 式: zoom = log2(CONSTANT / altitude)
    // log2(C) - log2(altitude) とも書けます
    val zoom = log2(ALTITUDE_ZOOM_CONSTANT) - log2(altitude)

    // 結果を許容範囲内に制限する
    return zoom.coerceIn(minZoom, maxZoom)
}

/**
 * Google Maps のズームレベルを ArcGIS の高度 (メートル単位, 近似値) に変換します。
 *
 * @param zoomLevel Google Mapsのズームレベル。
 * @param minZoom 許容される最小ズームレベル (入力値の制限用)。
 * @param maxZoom 許容される最大ズームレベル (入力値の制限用)。
 * @return 近似されたArcGISのカメラ高度 (メートル単位)。
 */
fun zoomLevelToAltitude(
    zoomLevel: Double,
    minZoom: Double = MIN_ZOOM_LEVEL,
    maxZoom: Double = MAX_ZOOM_LEVEL
): Double {
    // 入力ズームレベルを許容範囲内に制限する
    val clampedZoom = zoomLevel.coerceIn(minZoom, maxZoom)

    // 式: altitude = CONSTANT / (2^zoom)
    val altitude = ALTITUDE_ZOOM_CONSTANT / (2.0.pow(clampedZoom))

    // 高度は常に正の値になるはず
    return altitude
}



/**
 * OrbitLocationCameraControllerで使用するパラメータから、
 * 対応するArcGIS Cameraオブジェクトを計算します。
 * (ターゲット中心のパラメータからカメラ中心のパラメータへの変換)
 *
 * @param targetPoint ターゲット地点
 * @param distance カメラとターゲットの直線距離
 * @param cameraHeadingOffset ターゲットから見たカメラの方位オフセット (0=北, 90=東 ...)
 * @param cameraPitchOffset ターゲットに対するカメラのピッチ (0=真下, 90=水平)
 * @return 計算された Camera オブジェクト
 */
fun calculateCameraForOrbitParameters(
    targetPoint: Point,
    distance: Double,
    cameraHeadingOffset: Double,
    cameraPitchOffset: Double
): Camera {
    // 1. カメラの最終的な Pitch は cameraPitchOffset と同じ
    val finalPitch = cameraPitchOffset.coerceIn(MIN_ANGLE, ARCGIS_MAX_PITCH)
    val pitchRad = finalPitch.toRadians()

    // 2. カメラの最終的な Altitude (高度)
    // distance * cos(pitch) = altitude
    val altitude = distance * cos(pitchRad)

    // 3. カメラの最終的な Heading (カメラ自身の向き)
    // ターゲットからカメラへの方位 (cameraHeadingOffset) の逆が、カメラからターゲットへの方位
    val bearingToTarget = (cameraHeadingOffset + 180.0) % 360.0
    val finalHeading = bearingToTarget // カメラはこの方位を向く

    // 4. カメラの水平距離
    // distance * sin(pitch) = horizontalDistance
    val horizontalDistance = distance * sin(pitchRad)

    // 5. カメラの位置 (Location)
    // ターゲット地点から、逆方位 (cameraHeadingOffset) に水平距離だけ離れた地点を計算
    val cameraCoordinates = calculateDestinationPoint(
        targetPoint.x, // latitude
        targetPoint.y, // longitude
        cameraHeadingOffset, // ターゲットからカメラへの方位
        horizontalDistance
    )

    val cameraLocation = Point(cameraCoordinates.second, cameraCoordinates.first, altitude, targetPoint.spatialReference)

    // 6. 最終的なCameraオブジェクトを作成
    return Camera(cameraLocation, finalHeading, finalPitch, 0.0) // roll = 0
}

fun Camera.toMapCameraPosition() = MapCameraPosition(
        position = GeoPoint.fromLongLat(
            longitude = this.location.x,
            latitude = this.location.y,
            altitude = this.location.z ?: 0.0,
        ),
        zoom = altitudeToZoomLevel(
            altitude = this.location.z ?: 0.0,
        ),
        bearing = 360 - this.heading,
        tilt = this.pitch,
        paddings = MapPaddingsImpl.Zeros,
    )