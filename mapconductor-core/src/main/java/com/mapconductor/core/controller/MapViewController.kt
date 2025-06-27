package com.mapconductor.core.controller

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.spherical.haversineDistance
import kotlin.math.pow
import kotlinx.coroutines.CoroutineScope

import com.mapconductor.settings.Settings
import kotlin.math.min
import android.os.SystemClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

import android.view.animation.LinearInterpolator
import kotlinx.coroutines.Dispatchers

interface MapViewController {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope
    val markerOverlayManager: MarkerOverlayManagerImpl<*>

    suspend fun addMarkers(data: List<MarkerState>)

    suspend fun updateMarker(state: MarkerState)

    suspend fun clearOverlays()

    fun toScreenOffset(position: IGeoPoint): Offset?

    suspend fun fromScreenOffset(offset: Offset): GeoPoint?
}

interface CommonMarker {
    /**
     * マーカーの現在の緯度経度を取得する
     * 例：GoogleMaps → marker.position
     *     ArcGIS → (marker.geometry as Point).x/y
     */
    fun getGeoCoordinates(): MapCoordinates

    /**
     * マーカーの位置を緯度・経度で更新する
     * 例：GoogleMaps → marker.position = LatLng(...)
     *     ArcGIS → marker.geometry = Point(...)
     */
    fun setGeoCoordinates(lat: Double, lng: Double)
}

interface CoordinateConverter {
    /**
     * 地理座標から画面座標へ変換（MapViewなどの Projection を使う）
     * 例：GoogleMaps → Projection.toScreenLocation(LatLng)
     *     ArcGIS → mapView.locationToScreen(Point)
     */
    fun toScreenOffset(geo: MapCoordinates): Offset?

    /**
     * 画面座標から地理座標へ変換
     * 例：GoogleMaps → Projection.fromScreenLocation(Point)
     *     ArcGIS → mapView.screenToLocation(Point)
     */
    fun fromScreenOffset(offset: Offset): MapCoordinates?
}

data class MapCoordinates(
    val latitude: Double,   /* 緯度 */
    val longitude: Double   /* 軽度 */
)

abstract class BaseMapViewController<ActualCamera> : MapViewController {
    var cameraMoveListener: (OnCameraMoveHandler<ActualCamera>)? = null
    var mapClickListener: OnMapEventHandler? = null
    var mapLongClickListener: OnMapEventHandler? = null
    var markerClickListener: OnMarkerEventHandler? = null
    var markerDragStartListener: OnMarkerEventHandler? = null
    var markerDragListener: OnMarkerEventHandler? = null
    var markerDragEndListener: OnMarkerEventHandler? = null
    var markerAnimateStartListener: OnMarkerEventHandler? = null
    var markerAnimateEndListener: OnMarkerEventHandler? = null

    protected fun zoomToMetersPerPixel(zoom: Double): Double {
        val earthCircumference = 40075016.686
        val tileSize = 256
        return earthCircumference / (tileSize * 2.0.pow(zoom))
    }

    protected fun findMarkerFromPoint(
        markerOverlayManager: MarkerOverlayManagerImpl<*>,
        position: IGeoPoint,
        zoom: Double,
        tolerance: Double,
    ): MarkerState? {
        val meterInMapPixel = zoomToMetersPerPixel(zoom)
        val radius = tolerance * meterInMapPixel

        val state =
            markerOverlayManager.markerManager.findNearest(position)
                ?: return null

        val distance = haversineDistance(position, state.position)
        return if (distance <= radius) {
            state
        } else {
            null
        }
    }

    protected fun setDraggingState(
        markerState: MarkerState,
        dragging: Boolean,
    ) {
        markerState.isDragging = dragging
    }

    fun animateMarkerDrop(
        marker: CommonMarker,                                                   /* ラップしたMarkerオブジェクト*/
        converter: CoordinateConverter,                                         /* 緯度経度/画面座標変化IF */
        duration: Long = Settings.Default.markerBounceAnimateDuration.toLong(), /* アニメションする時間(ms) */
    ) {
        // アニメーションの最終的な目標地点(地理座標)
        val target = marker.getGeoCoordinates()

        // 線形補間
        val interpolator = LinearInterpolator()

        // 開始地点:x座標はMarkerと同じ、y座標は画面上端。なければreturn
        val startPoint = converter.toScreenOffset(target)?.let { Offset(it.x, 0f) } ?: return

        // ここからアニメ本体
        flow {
            val startTime = SystemClock.uptimeMillis()
            while (true) {
                val elapsed = SystemClock.uptimeMillis() - startTime
                val t = min(1f, elapsed.toFloat() / duration)
                emit(interpolator.getInterpolation(t))
                if (t >= 1f) break
                delay(16)
            }
        }.onEach { t ->
            // 開始時の画面座標から緯度経度に戻す(垂直方向アニメーション起点)
            val startLatLng = converter.fromScreenOffset(startPoint) ?: return@onEach

            // 緯度・経度を線形補間
            val lat = t * target.latitude + (1 - t) * startLatLng.latitude
            val lng = t * target.longitude + (1 - t) * startLatLng.longitude
            val current = MapCoordinates(lat, lng)

            // 現在の座標をマーカーに適用
            marker.setGeoCoordinates(lat, lng)
        }.onCompletion {
            // 最終的にマーカー位置を正確な着地点に戻す（補間誤差などを吸収）
            marker.setGeoCoordinates(target.latitude, target.longitude)
        }.launchIn(coroutine)
    }
}
