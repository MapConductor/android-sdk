package com.mapconductor.core.controller

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexCell
import com.mapconductor.core.geocell.HexCoord
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.OnCameraMoveHandler
import com.mapconductor.core.map.OnMapEventHandler
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerOverlayManagerImpl
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.OnMarkerEventHandler
import com.mapconductor.core.polyline.PolylineOverlayManagerImpl
import com.mapconductor.core.polyline.PolylineState
import com.mapconductor.core.spherical.haversineDistance
import com.mapconductor.settings.Settings
import kotlin.math.min
import kotlin.math.pow
import android.os.SystemClock
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

interface MapViewController<ActualMarker, ActualPolyline> {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope
    val markerOverlayManager: MarkerOverlayManagerImpl<ActualMarker>
    val polylineOverlayManager: PolylineOverlayManagerImpl<ActualPolyline>
    val hexCell: HexGeocell

    suspend fun addMarkers(data: List<MarkerState>)

    suspend fun updateMarker(state: MarkerState)

    suspend fun addPolylines(data: List<PolylineState>)

    suspend fun updatePolyline(state: PolylineState)

    suspend fun addCircles(data: List<CircleState>)

    suspend fun updateCircle(state: CircleState)

    suspend fun clearOverlays()

    fun toScreenOffset(position: IGeoPoint): Offset?

    suspend fun fromScreenOffset(offset: Offset): GeoPoint?
}

data class SearchRangeAnalysis(
    val clickPosition: IGeoPoint,
    val zoom: Double,
    val tolerancePixels: Double,
    val toleranceMeters: Double,
    val clickedCell: HexCell,
    val hexSideLength: Double,
    val hexDistance: Double,
    val searchRadiusHexUnits: Int,
    val searchCells: List<HexCoord>,
    val outlineCells: List<HexCoord>,
    val markersInRange: List<MarkerState>,
)

abstract class BaseMapViewController<ActualCamera, ActualMarker, ActualPolyline> :
    MapViewController<ActualMarker, ActualPolyline> {
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

    protected abstract fun setMarkerPosition(
        markerEntity: MarkerEntity<ActualMarker>,
        position: GeoPoint,
    )

    protected fun findMarkerFromPoint(
        position: IGeoPoint,
        zoom: Double,
        tolerance: Double,
    ): MarkerEntity<ActualMarker>? {
        val meterInMapPixel = zoomToMetersPerPixel(zoom)
        val radius = tolerance * meterInMapPixel
        val entity = markerOverlayManager.markerManager.findNearest(position) ?: return null
        val distance = haversineDistance(position, entity.state.position)
        return if (distance <= radius) {
            entity
        } else {
            null
        }
    }

    protected fun setDraggingState(
        markerState: MarkerState,
        dragging: Boolean,
    ) {
        // Since this "isDragging" property is internal accessor,
        // childViewControllers must call this method instead of "isDragging = true/false".
        markerState.isDragging = dragging
    }

    protected fun animateMarkerDrop(
        markerEntity: MarkerEntity<ActualMarker>, // ラップしたMarkerオブジェクト
        duration: Int = Settings.Default.markerDropAnimateDuration, // アニメションする時間(ms)
    ) {
        // アニメーションの最終的な目標地点(地理座標)
        val target = markerEntity.state.position

        // 線形補間
        val interpolator = LinearInterpolator()

        // 開始地点:x座標はMarkerと同じ、y座標は画面上端。なければreturn
        val startPoint = toScreenOffset(target)?.let { Offset(it.x, 0f) } ?: return

        markerAnimateStartListener?.invoke(markerEntity.state)

        // ここからアニメ本体
        flow {
            val startTime = SystemClock.uptimeMillis()
            var t = 0f
            while (t < 1f) {
                val elapsed = SystemClock.uptimeMillis() - startTime
                t = min(1f, elapsed.toFloat() / duration)
                emit(interpolator.getInterpolation(t))
                delay(16L)
            }
        }.onEach { t: Float ->
            // 開始時の画面座標から緯度経度に戻す(垂直方向アニメーション起点)
            val startLatLng = fromScreenOffset(startPoint)!!

            // 緯度・経度を線形補間
            val lat = t * target.latitude + (1f - t) * startLatLng.latitude
            val lng = t * target.longitude + (1f - t) * startLatLng.longitude

            // 現在の座標をマーカーに適用
            val newPosition = GeoPoint.fromLatLong(lat, lng)
            setMarkerPosition(markerEntity, newPosition)
        }.onCompletion {
            // 最終的にマーカー位置を正確な着地点に戻す（補間誤差などを吸収）
            markerEntity.state.position = target
            markerEntity.state.animation = null
            markerAnimateEndListener?.invoke(markerEntity.state)
        }.launchIn(coroutine)
    }

    protected fun animateMarkerBounce(
        markerEntity: MarkerEntity<ActualMarker>,
        duration: Int = Settings.Default.markerBounceAnimateDuration, // アニメションする時間(ms)
    ) {
        val startTime = SystemClock.uptimeMillis()

        // アニメーションの最終的な目標地点(地理座標)
        val target = markerEntity.state.position

        // 線形補間
        val interpolator = BounceInterpolator()

        // 開始地点:x座標はMarkerと同じ、y座標は画面上端。なければreturn
        val startPoint = toScreenOffset(target)?.let { Offset(it.x, 0f) } ?: return

        markerAnimateStartListener?.invoke(markerEntity.state)
        flow {
            var t = 0f
            while (t < 1f) {
                val elapsed = SystemClock.uptimeMillis() - startTime
                t = interpolator.getInterpolation(min(1f, elapsed.toFloat() / duration))
                emit(t)
                delay(16L)
            }
        }.onEach { t ->
            val startLatLng = this.fromScreenOffset(startPoint) ?: return@onEach
            val lng = target.longitude
            val lat = t * target.latitude + (1f - t) * startLatLng.latitude

            // 現在の座標をマーカーに適用
            val newPosition = GeoPoint.fromLatLong(lat, lng)
            setMarkerPosition(markerEntity, newPosition)
        }.onCompletion {
            // 最終的にマーカー位置を正確な着地点に戻す（補間誤差などを吸収）
            markerEntity.state.position = target
            markerEntity.state.animation = null
            markerAnimateEndListener?.invoke(markerEntity.state)
        }.launchIn(coroutine)
    }

    protected abstract fun clearPolyline()

    protected abstract fun drawPolyline(geoPoints: List<IGeoPoint>)

    protected fun analyzeSearchRange(
        position: IGeoPoint,
        zoom: Double,
        tolerancePixels: Double,
    ): SearchRangeAnalysis {
        val toleranceMeters =
            markerOverlayManager.markerManager.metersPerPixel(
                position, zoom, tolerancePixels,
            )

        val clickedCell = hexCell.latLngToHexCell(position, zoom)

        // Hex metrics
        val scale = 1.0 / (2.0.pow(zoom))
        val latScale = kotlin.math.cos(position.latitude * kotlin.math.PI / 180).coerceAtLeast(0.01)
        val hexSideLength = hexCell.baseHexSideLength * scale / latScale
        val hexDistance = hexSideLength * kotlin.math.sqrt(3.0)
        val searchRadiusHexUnits = kotlin.math.ceil(toleranceMeters / hexDistance).toInt()

        // Search cells
        val searchCells = hexCell.hexRange(clickedCell.coord, searchRadiusHexUnits)
        val outlineCells = findOutlineCells(searchCells)

        // Find markers in range
        val markersInRange = findMarkersInSearchRange(position, searchCells, toleranceMeters, zoom)

        return SearchRangeAnalysis(
            clickPosition = position,
            zoom = zoom,
            tolerancePixels = tolerancePixels,
            toleranceMeters = toleranceMeters,
            clickedCell = clickedCell,
            hexSideLength = hexSideLength,
            hexDistance = hexDistance,
            searchRadiusHexUnits = searchRadiusHexUnits,
            searchCells = searchCells,
            outlineCells = outlineCells,
            markersInRange = markersInRange,
        )
    }

    /**
     * 検索範囲内のマーカーを特定
     */
    protected fun findMarkersInSearchRange(
        clickPosition: IGeoPoint,
        searchCells: List<HexCoord>,
        toleranceMeters: Double,
        zoom: Double,
    ): List<MarkerState> {
        val cellSet = searchCells.toSet()
        val markersInRange = mutableListOf<MarkerState>()

        markerOverlayManager.markerManager.allEntities().forEach { markerEntity ->
            val markerCell = hexCell.latLngToHexCell(markerEntity.state.position, zoom)

            if (markerCell.coord in cellSet) {
                val distance = haversineDistance(clickPosition, markerEntity.state.position)
                if (distance <= toleranceMeters) {
                    markersInRange.add(markerEntity.state)
                }
            }
        }

        return markersInRange
    }

    /**
     * 検索範囲の外郭セルを特定
     */
    protected fun findOutlineCells(searchCells: List<HexCoord>): List<HexCoord> {
        val cellSet = searchCells.toSet()

        return searchCells.filter { cell ->
            // 近隣セルのいずれかが検索範囲外なら、これは外郭セル
            cell.neighbors().any { neighbor ->
                neighbor !in cellSet
            }
        }
    }

    /**
     * 円形の近似ポイントを生成
     */
    protected fun createCirclePoints(
        center: IGeoPoint,
        radiusMeters: Double,
        numPoints: Int = 32,
    ): List<IGeoPoint> {
        val points = mutableListOf<IGeoPoint>()
        val earthRadius = 6371000.0 // 地球半径（メートル）

        for (i in 0 until numPoints) {
            val angle = 2.0 * kotlin.math.PI * i / numPoints
            val deltaLat = radiusMeters * kotlin.math.cos(angle) / earthRadius * 180.0 / kotlin.math.PI
            val deltaLng =
                radiusMeters * kotlin.math.sin(angle) / earthRadius * 180.0 / kotlin.math.PI /
                    kotlin.math.cos(center.latitude * kotlin.math.PI / 180.0)

            points.add(
                object : IGeoPoint {
                    override val latitude = center.latitude + deltaLat
                    override val longitude = center.longitude + deltaLng
                    override val altitude = center.altitude
                },
            )
        }

        // 円を閉じる
        if (points.isNotEmpty()) {
            points.add(points[0])
        }

        return points
    }

    protected fun drawDistanceCircles(analysis: SearchRangeAnalysis) {
        // 距離サークルの近似（正n角形として）
        val circlePoints =
            createCirclePoints(
                center = analysis.clickPosition,
                radiusMeters = analysis.toleranceMeters,
                numPoints = 32,
            )
        drawPolyline(circlePoints)
    }

    /**
     * 各種可視化モード
     */
    protected fun drawClickedCell(analysis: SearchRangeAnalysis) {
        val points =
            hexCell.hexToPolygonLatLng(
                coord = analysis.clickedCell.coord,
                latHint = analysis.clickPosition.latitude,
                zoom = analysis.zoom,
            )
        drawPolyline(points)
    }

    protected fun drawFullSearchRange(analysis: SearchRangeAnalysis) {
        val allPoints = mutableListOf<IGeoPoint>()

        analysis.searchCells.take(50).forEach { coord ->
            // パフォーマンス制限
            val cellPoints =
                hexCell.hexToPolygonLatLng(
                    coord,
                    analysis.clickPosition.latitude,
                    analysis.zoom,
                )
            allPoints.addAll(cellPoints)
            allPoints.add(cellPoints[0]) // 閉じる
        }

        if (allPoints.isNotEmpty()) {
            drawPolyline(allPoints)
        }
    }

    protected fun drawSearchOutline(analysis: SearchRangeAnalysis) {
        val outlinePoints = mutableListOf<IGeoPoint>()

        analysis.outlineCells.forEach { coord ->
            val cellPoints =
                hexCell.hexToPolygonLatLng(
                    coord,
                    analysis.clickPosition.latitude,
                    analysis.zoom,
                )
            outlinePoints.addAll(cellPoints)
        }

        if (outlinePoints.isNotEmpty()) {
            drawPolyline(outlinePoints)
        }
    }
}
