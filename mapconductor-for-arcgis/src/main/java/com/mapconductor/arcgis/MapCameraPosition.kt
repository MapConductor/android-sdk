package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.Camera
import com.mapconductor.arcgis.zoom.ZoomAltitudeConverter
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapPaddings
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

const val ZOOM0_ALTITUDE = 5_000_000.0

// ArcGIS needs its own calibration constant so that the same "Google-like zoom" yields a similar visible region.
// (ArcGIS camera FOV differs from Google/Mapbox; using DEFAULT_ZOOM0_ALTITUDE drifts the visible scale.)
private val converter = ZoomAltitudeConverter()

fun MapCameraPosition.getAltitudeForArcGIS(): Double = converter.zoomLevelToAltitude(zoom, position.latitude, tilt)

fun MapCameraPosition.toCamera(): Camera {
    val targetPoint = GeoPoint.from(position).toPoint()
    return calculateCameraForOrbitParameters(
        targetPoint = targetPoint,
        distance = converter.zoomLevelToDistance(zoom, position.latitude),
        // For orbit camera: cameraHeadingOffset = bearing + 180 makes Camera.heading == bearing.
        cameraHeadingOffset = bearing + 180,
        cameraPitchOffset = tilt,
    )
}

internal const val EARTH_MEAN_RADIUS_METERS = 6371000.0
internal const val DEFAULT_MAX_GMAPS_TILT = 60.0
internal const val ARCGIS_MAX_PITCH = 90.0
internal const val MIN_ANGLE = 0.0

internal fun Double.toRadians(): Double = Math.toRadians(this)

internal fun Double.toDegrees(): Double = Math.toDegrees(this)

/**
 * 指定された地点から特定の方位と距離にある地点を計算
 */
fun calculateDestinationPoint(
    lat: Double,
    lon: Double,
    bearing: Double,
    distance: Double,
): GeoPoint {
    val latRad = lat.toRadians()
    val lonRad = lon.toRadians()
    val bearingRad = bearing.toRadians()
    val angularDistance = distance / EARTH_MEAN_RADIUS_METERS

    val destLatRad = asin(sin(latRad) * cos(angularDistance) + cos(latRad) * sin(angularDistance) * cos(bearingRad))

    var destLonRad =
        lonRad +
            atan2(
                sin(bearingRad) * sin(angularDistance) * cos(latRad),
                cos(angularDistance) - sin(latRad) * sin(destLatRad),
            )

    // 経度を -180 ～ +180 の範囲に正規化
    destLonRad = (destLonRad + 3 * PI) % (2 * PI) - PI

    return GeoPoint.fromLatLong(
        latitude = destLatRad.toDegrees(),
        longitude = destLonRad.toDegrees(),
    )
}

/**
 * OrbitLocationCameraController用のカメラ計算
 */
fun calculateCameraForOrbitParameters(
    targetPoint: com.arcgismaps.geometry.Point,
    distance: Double,
    cameraHeadingOffset: Double,
    cameraPitchOffset: Double,
): Camera {
    val finalPitch = cameraPitchOffset.coerceIn(MIN_ANGLE, ARCGIS_MAX_PITCH)
    val pitchRad = finalPitch.toRadians()

    val altitude = distance * cos(pitchRad)
    val bearingToTarget = (cameraHeadingOffset + 180.0) % 360.0
    val finalHeading = bearingToTarget
    val horizontalDistance = distance * sin(pitchRad)

    val cameraCoordinates =
        calculateDestinationPoint(
            lat = targetPoint.y,
            lon = targetPoint.x,
            bearing = cameraHeadingOffset,
            distance = horizontalDistance,
        )

    return Camera(
        latitude = cameraCoordinates.latitude,
        longitude = cameraCoordinates.longitude,
        altitude = altitude,
        heading = finalHeading,
        pitch = finalPitch,
        roll = 0.0,
    )
}

fun MapCameraPosition.Companion.from(position: MapCameraPositionInterface): MapCameraPosition =
    when (position) {
        is MapCameraPosition -> position
        else -> {
            val altitude = converter.zoomLevelToAltitude(position.zoom, position.position.latitude, position.tilt)
            MapCameraPosition(
                position =
                    GeoPoint.fromLongLat(
                        longitude = position.position.longitude,
                        latitude = position.position.latitude,
                        altitude = altitude,
                    ),
                zoom = position.zoom,
                bearing = position.bearing,
                tilt = position.tilt,
                paddings = position.paddings,
                visibleRegion = position.visibleRegion,
            )
        }
    }

/**
 * 現在のカメラの zoom レベルを取得
 */
fun Camera.getZoomLevel(): Double = converter.altitudeToZoomLevel(this.location.z ?: 0.0, this.location.y, this.pitch)

/**
 * zoom レベルを指定してカメラの距離を変更
 */
fun Camera.withZoomLevel(zoomLevel: Double): Camera {
    val altitude = converter.zoomLevelToAltitude(zoomLevel, this.location.y, this.pitch)
    return Camera(
        latitude = this.location.y,
        longitude = this.location.x,
        altitude = altitude,
        heading = this.heading,
        pitch = this.pitch,
        roll = this.roll,
    )
}

fun Camera.toMapCameraPosition() =
    MapCameraPosition(
        position =
            GeoPoint.fromLongLat(
                longitude = this.location.x,
                latitude = this.location.y,
                altitude = this.location.z ?: 0.0,
            ),
        zoom =
            converter
                .altitudeToZoomLevel(altitude = this.location.z ?: 0.0, latitude = this.location.y, tilt = this.pitch),
        bearing = ((this.heading % 360) + 360) % 360,
        tilt = this.pitch,
        paddings = MapPaddings.Zeros,
        visibleRegion = null,
    )
