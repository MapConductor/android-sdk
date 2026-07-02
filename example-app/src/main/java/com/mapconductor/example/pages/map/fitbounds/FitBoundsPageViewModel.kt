package com.mapconductor.example.pages.map.fitbounds

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.polygon.PolygonState
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface FitBoundsPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val mapViewState: StateFlow<MapViewStateInterface<*>?>
    val marker: MarkerState
    val boundsPolygon: StateFlow<PolygonState?>

    fun onMapViewChanged(state: MapViewStateInterface<*>)
}

class FitBoundsPageViewModel :
    ViewModel(),
    FitBoundsPageViewModelInterface {
    private val initialPosition = GeoPoint(35.68, 139.76)

    override val initCameraPosition =
        MapCameraPosition(
            position = initialPosition,
            zoom = 10.0,
            bearing = 0.0,
            tilt = 0.0,
            paddings = null,
        )

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    private var dragStartPosition: GeoPoint? = null

    private val _boundsPolygon = MutableStateFlow<PolygonState?>(null)
    override val boundsPolygon: StateFlow<PolygonState?> = _boundsPolygon.asStateFlow()

    override val marker: MarkerState =
        MarkerState(
            id = "fitbounds_marker",
            position = initialPosition,
            draggable = true,
            onDragStart = ::onDragStart,
            onDrag = ::onDrag,
            onDragEnd = ::onDragEnd,
        )

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        _mapViewState.value?.cameraPosition?.let { state.moveCameraTo(it) }
        _mapViewState.value = state
    }

    private fun onDragStart(marker: MarkerState) {
        dragStartPosition = GeoPoint.from(marker.position)
    }

    private fun onDrag(marker: MarkerState) {
        val start = dragStartPosition ?: return
        val current = GeoPoint.from(marker.position)
        _boundsPolygon.value = buildPolygon(start, current)
    }

    private fun onDragEnd(marker: MarkerState) {
        val start = dragStartPosition ?: return
        val current = GeoPoint.from(marker.position)
        val bounds = buildBounds(start, current)
        _mapViewState.value?.fitBounds(bounds)
        dragStartPosition = null
        viewModelScope.launch {
            delay(1500)
            _boundsPolygon.value = null
        }
    }

    private fun buildBounds(
        a: GeoPoint,
        b: GeoPoint,
    ) = GeoRectBounds(
        southWest = GeoPoint(min(a.latitude, b.latitude), min(a.longitude, b.longitude)),
        northEast = GeoPoint(max(a.latitude, b.latitude), max(a.longitude, b.longitude)),
    )

    private fun buildPolygon(
        a: GeoPoint,
        b: GeoPoint,
    ): PolygonState {
        val minLat = min(a.latitude, b.latitude)
        val maxLat = max(a.latitude, b.latitude)
        val minLon = min(a.longitude, b.longitude)
        val maxLon = max(a.longitude, b.longitude)
        return PolygonState(
            id = "fitbounds_polygon",
            points =
                listOf(
                    GeoPoint(minLat, minLon),
                    GeoPoint(minLat, maxLon),
                    GeoPoint(maxLat, maxLon),
                    GeoPoint(maxLat, minLon),
                ),
            strokeColor = Color.Red,
            strokeWidth = 2.dp,
            fillColor = Color.Red.copy(alpha = 0.3f),
        )
    }
}
