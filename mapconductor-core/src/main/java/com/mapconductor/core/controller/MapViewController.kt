package com.mapconductor.core.controller

import androidx.compose.ui.geometry.Offset
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
import com.mapconductor.core.spherical.haversineDistance
import kotlin.math.pow
import kotlinx.coroutines.CoroutineScope

interface MapViewController {
    val holder: MapViewHolder<*, *>
    val coroutine: CoroutineScope
    val markerOverlayManager: MarkerOverlayManagerImpl<*>
    val hexCell: HexGeocell

    suspend fun addMarkers(data: List<MarkerState>)

    suspend fun updateMarker(state: MarkerState)

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
    val markersInRange: List<MarkerState>
)

abstract class BaseMapViewController<ActualCamera> : MapViewController {
    var cameraMoveListener: (OnCameraMoveHandler<ActualCamera>)? = null
    var mapClickListener: OnMapEventHandler? = null
    var mapLongClickListener: OnMapEventHandler? = null
    var markerClickListener: OnMarkerEventHandler? = null
    var markerDragStartListener: OnMarkerEventHandler? = null
    var markerDragListener: OnMarkerEventHandler? = null
    var markerDragEndListener: OnMarkerEventHandler? = null

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
        val entity = markerOverlayManager.markerManager.findNearest(position) ?: return null

//        return entity.state
        val distance = haversineDistance(position, entity.state.position)
        return if (distance <= radius) {
            entity.state
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

    protected abstract fun clearPolyline()

    protected abstract fun drawPolyline(geoPoints: List<IGeoPoint>)

    protected fun analyzeSearchRange(
        position: IGeoPoint,
        zoom: Double,
        tolerancePixels: Double
    ): SearchRangeAnalysis {

        val toleranceMeters = markerOverlayManager.markerManager.metersPerPixel(
            position, zoom, tolerancePixels
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
            markersInRange = markersInRange
        )
    }
    /**
     * 検索範囲内のマーカーを特定
     */
    protected fun findMarkersInSearchRange(
        clickPosition: IGeoPoint,
        searchCells: List<HexCoord>,
        toleranceMeters: Double,
        zoom: Double
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
        numPoints: Int = 32
    ): List<IGeoPoint> {
        val points = mutableListOf<IGeoPoint>()
        val earthRadius = 6371000.0 // 地球半径（メートル）

        for (i in 0 until numPoints) {
            val angle = 2.0 * kotlin.math.PI * i / numPoints
            val deltaLat = radiusMeters * kotlin.math.cos(angle) / earthRadius * 180.0 / kotlin.math.PI
            val deltaLng = radiusMeters * kotlin.math.sin(angle) / earthRadius * 180.0 / kotlin.math.PI / kotlin.math.cos(center.latitude * kotlin.math.PI / 180.0)

            points.add(object : IGeoPoint {
                override val latitude = center.latitude + deltaLat
                override val longitude = center.longitude + deltaLng
                override val altitude = center.altitude
            })
        }

        // 円を閉じる
        if (points.isNotEmpty()) {
            points.add(points[0])
        }

        return points
    }

    protected fun drawDistanceCircles(analysis: SearchRangeAnalysis) {
        // 距離サークルの近似（正n角形として）
        val circlePoints = createCirclePoints(
            center = analysis.clickPosition,
            radiusMeters = analysis.toleranceMeters,
            numPoints = 32
        )
        drawPolyline(circlePoints)
    }

    /**
     * 各種可視化モード
     */
    protected fun drawClickedCell(analysis: SearchRangeAnalysis) {
        val points = hexCell.hexToPolygonLatLng(
            coord = analysis.clickedCell.coord,
            latHint = analysis.clickPosition.latitude,
            zoom = analysis.zoom
        )
        drawPolyline(points)
    }

    protected fun drawFullSearchRange(analysis: SearchRangeAnalysis) {
        val allPoints = mutableListOf<IGeoPoint>()

        analysis.searchCells.take(50).forEach { coord -> // パフォーマンス制限
            val cellPoints = hexCell.hexToPolygonLatLng(
                coord,
                analysis.clickPosition.latitude,
                analysis.zoom
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
            val cellPoints = hexCell.hexToPolygonLatLng(
                coord,
                analysis.clickPosition.latitude,
                analysis.zoom
            )
            outlinePoints.addAll(cellPoints)
        }

        if (outlinePoints.isNotEmpty()) {
            drawPolyline(outlinePoints)
        }
    }
}
